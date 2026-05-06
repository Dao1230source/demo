package org.source.spring.doc.infrastructure.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.source.spring.doc.domain.object.DocObjectProcessor;
import org.source.spring.doc.domain.value.*;
import org.source.spring.doc.infrastructure.config.DocConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

/**
 * 统一文档解析器
 * <p>
 * 基于 JavaParser 实现的统一文档解析工具，
 * 直接返回 DocValue 对象。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Slf4j
@Getter
public class DocParser {

    private final DocObjectProcessor objectProcessor;
    private final DocConfig docConfig;
    private DocParserReport report;
    private int classSortedCounter = 0;

    public DocParser(DocObjectProcessor objectProcessor) {
        this.objectProcessor = objectProcessor;
        this.docConfig = new DocConfig();
    }

    public DocParser(DocObjectProcessor objectProcessor, DocConfig docConfig) {
        this.objectProcessor = objectProcessor;
        this.docConfig = docConfig;
    }

    /**
     * 解析文档（从配置读取扫描范围）
     *
     * @return 解析报告
     */
    public DocParserReport parse() throws IOException {
        if (objectProcessor == null) {
            throw new IllegalStateException("DocObjectProcessor not configured");
        }

        report = new DocParserReport();
        report.start();
        classSortedCounter = 0;

        List<String> effectiveScanPaths = resolveScanPaths();

        String projectRoot = resolveProjectRoot();
        ModuleParser moduleParser = new ModuleParser();
        List<ModuleDocData> modules = moduleParser.parseProjectModules(projectRoot);
        report.addModules(modules.size());

        List<DocData> allValues = Collections.synchronizedList(new ArrayList<>());

        if (effectiveScanPaths.isEmpty()) {
            if (docConfig.isEnableParallel()) {
                ForkJoinPool pool = new ForkJoinPool(docConfig.getParallelThreads());
                try {
                    pool.submit(() -> modules.parallelStream().forEach(module -> parseModule(module, allValues))).get();
                } catch (Exception e) {
                    log.error("Parallel parsing failed", e);
                    modules.forEach(module -> parseModule(module, allValues));
                } finally {
                    pool.shutdown();
                }
            } else {
                modules.forEach(module -> parseModule(module, allValues));
            }
        } else {
            List<String> javaFiles = collectJavaFilesFromScanPaths(effectiveScanPaths);
            if (docConfig.isEnableParallel()) {
                ForkJoinPool pool = new ForkJoinPool(docConfig.getParallelThreads());
                try {
                    pool.submit(() -> javaFiles.parallelStream()
                            .filter(f -> !shouldExclude(f))
                            .forEach(f -> parseJavaFile(f, allValues, modules))).get();
                } catch (Exception e) {
                    log.error("Parallel parsing failed", e);
                    javaFiles.stream()
                            .filter(f -> !shouldExclude(f))
                            .forEach(f -> parseJavaFile(f, allValues, modules));
                } finally {
                    pool.shutdown();
                }
            } else {
                javaFiles.stream()
                        .filter(f -> !shouldExclude(f))
                        .forEach(f -> parseJavaFile(f, allValues, modules));
            }
        }

        if (!allValues.isEmpty()) {
            objectProcessor.save(allValues);
        }

        report.end();
        if (docConfig.isGenerateReport()) {
            log.info(report.generateSummary());
        }

        return report;
    }

    private List<String> resolveScanPaths() {
        List<String> scanPackages = docConfig.getScanPackages();
        if (scanPackages == null || scanPackages.isEmpty()) {
            return Collections.emptyList();
        }

        String projectRoot = resolveProjectRoot();
        List<String> resolvedPaths = new ArrayList<>();

        for (String scanPath : scanPackages) {
            if (ScanPathUtils.isAbsolutePath(scanPath)) {
                resolvedPaths.add(scanPath);
            } else {
                String fullPath = ScanPathUtils.convertPackageToPath(scanPath, projectRoot);
                resolvedPaths.add(fullPath);
            }
        }

        return resolvedPaths;
    }

    private String resolveProjectRoot() {
        String configuredPath = docConfig.getProjectRootPath();
        if (StringUtils.isNotBlank(configuredPath)) {
            return configuredPath;
        }
        return System.getProperty("user.dir");
    }

    private List<String> collectJavaFilesFromScanPaths(List<String> scanPaths) {
        List<String> javaFiles = new ArrayList<>();
        for (String scanPath : scanPaths) {
            Path path = Paths.get(scanPath);
            if (Files.exists(path) && Files.isDirectory(path)) {
                try (Stream<Path> paths = Files.walk(path)) {
                    paths.filter(Files::isRegularFile)
                            .filter(p -> matchesExtension(p.toString()))
                            .forEach(p -> javaFiles.add(p.toString()));
                } catch (IOException e) {
                    if (docConfig.isFailOnError()) {
                        throw new RuntimeException("Failed to walk path: " + scanPath, e);
                    }
                    log.warn("Failed to walk path: {}", scanPath, e);
                    report.addFailedFile(scanPath, e.getMessage());
                }
            } else {
                log.warn("Scan path does not exist or is not a directory: {}", scanPath);
            }
        }
        return javaFiles;
    }

    private boolean matchesExtension(String filePath) {
        List<String> extensions = docConfig.getFileExtensions();
        if (extensions == null || extensions.isEmpty()) {
            return filePath.endsWith(".java");
        }
        return extensions.stream().anyMatch(filePath::endsWith);
    }

    private void parseModule(ModuleDocData module, List<DocData> allValues) {
        String modulePath = module.getName();
        try {
            allValues.add(module);

            List<String> javaFiles = collectJavaFiles(modulePath);
            int localClassSorted = 0;
            for (String javaFile : javaFiles) {
                if (shouldExclude(javaFile)) {
                    continue;
                }
                List<DocData> values = parseJavaFileToValues(javaFile, modulePath, localClassSorted);
                allValues.addAll(values);
                localClassSorted++;
            }
            report.addFiles(javaFiles.size());
        } catch (IOException e) {
            if (docConfig.isFailOnError()) {
                throw new RuntimeException("Failed to parse module: " + modulePath, e);
            }
            report.addFailedFile(modulePath, e.getMessage());
        }
    }

    private void parseJavaFile(String filePath, List<DocData> allValues, List<ModuleDocData> modules) {
        String modulePath = findModulePath(filePath, modules);
        List<DocData> values = parseJavaFileToValues(filePath, modulePath, classSortedCounter++);
        allValues.addAll(values);
        report.addFiles(1);
    }

    private String findModulePath(String filePath, List<ModuleDocData> modules) {
        for (ModuleDocData module : modules) {
            if (filePath.startsWith(module.getName())) {
                return module.getName();
            }
        }
        return null;
    }

    private List<DocData> parseJavaFileToValues(String filePath, String modulePath, int classSorted) {
        DocCommentParser commentParser = new DocCommentParser(docConfig.isParseValidationAnnotations());
        JpaAnnotationParser jpaParser = new JpaAnnotationParser();
        DocTagParser tagParser = new DocTagParser();
        RestAnnotationParser restParser = new RestAnnotationParser();
        SpringAnnotationParser springParser = new SpringAnnotationParser();
        FeignAnnotationParser feignParser = new FeignAnnotationParser();
        MyBatisAnnotationParser myBatisParser = new MyBatisAnnotationParser();

        List<DocData> values = new ArrayList<>();
        try {
            Path path = Paths.get(filePath);
            String sourceCode = Files.readString(path);
            String fileName = path.getFileName().toString();
            String className = fileName.replace(".java", "");

            String packageName = extractPackageName(sourceCode);
            String qualifiedName = StringUtils.isBlank(packageName) ? className : packageName + "." + className;

            commentParser.parseOnce(sourceCode);

            // 解析类
            ClassDocData classValue = commentParser.parseClassDoc(sourceCode, qualifiedName, modulePath != null ? modulePath : "", classSorted);
            if (classValue != null) {
                classValue = jpaParser.parseJpaAnnotations(sourceCode, classValue);

                if (docConfig.isParseSpringAnnotations()) {
                    Map<String, Object> springAnnotations = springParser.parseComponentAnnotations(sourceCode, qualifiedName);
                    if (!springAnnotations.isEmpty()) {
                        classValue.setSpringAnnotations(springAnnotations);
                    }
                }

                if (StringUtils.isBlank(classValue.getDocContent())) {
                    report.addClassesWithoutJavaDoc(1);
                }
                report.addClasses(1);
                values.add(classValue);

                // 内部类（使用 InnerClassValue）
                if (docConfig.isIncludeInnerClasses()) {
                    List<InnerClassData> innerClasses = commentParser.parseInnerClassValues(sourceCode, qualifiedName);
                    report.addClasses(innerClasses.size());
                    values.addAll(innerClasses);
                }
            }

            // Spring Bean 解析
            List<SpringBeanData> springBeans = springParser.parseSpringBeanValues(sourceCode, qualifiedName);
            report.addSpringBeans(springBeans.size());
            values.addAll(springBeans);

            // 解析方法
            List<MethodDocData> methods = commentParser.parseAllMethods(sourceCode, qualifiedName);
            int methodCount = 0;
            int methodWithoutDocCount = 0;
            int paramCount = 0;
            for (MethodDocData method : methods) {
                if (!docConfig.isIncludePrivateMembers() && method.isPrivate()) {
                    continue;
                }

                if (StringUtils.isNotBlank(method.getDocContent())) {
                    var tags = tagParser.parseAllTags(method.getDocContent());
                    method.setDocContent((String) tags.getOrDefault("return", method.getDocContent()));
                }

                if (StringUtils.isBlank(method.getDocContent())) {
                    methodWithoutDocCount++;
                }
                methodCount++;
                values.add(method);

                // 方法参数
                List<ParameterVariableData> params = commentParser.parseMethodParameters(sourceCode, qualifiedName, method.getName());
                paramCount += params.size();
                values.addAll(params);

                // 返回值
                ParameterVariableData returnValue = commentParser.parseMethodReturnValue(sourceCode, qualifiedName, method.getName());
                if (returnValue != null) {
                    paramCount++;
                    values.add(returnValue);
                }
            }
            report.addMethodsWithoutJavaDoc(methodWithoutDocCount);
            report.addMethods(methodCount);
            report.addParameters(paramCount);

            // 解析字段
            List<MemberVariableData> fields = commentParser.parseAllMemberVariables(sourceCode, qualifiedName);
            int fieldCount = 0;
            int fieldWithoutDocCount = 0;
            int fieldSorted = 0;
            for (MemberVariableData field : fields) {
                if (!docConfig.isIncludePrivateMembers() && field.isPrivate()) {
                    continue;
                }

                // JPA 列信息
                JpaColumnVariableData jpaColumn = jpaParser.parseJpaColumnVariable(
                        sourceCode, qualifiedName, field.getVariableName(), field.getSharedVariable(), fieldSorted);
                DocData fieldValue = Objects.requireNonNullElse(jpaColumn, field);

                if (StringUtils.isBlank(field.getDocContent())) {
                    fieldWithoutDocCount++;
                }
                fieldCount++;
                values.add(fieldValue);
                fieldSorted++;
            }
            report.addFieldsWithoutJavaDoc(fieldWithoutDocCount);
            report.addFields(fieldCount);

            // REST 接口
            List<RestDocData> endpoints = restParser.parseRestEndpoints(sourceCode, qualifiedName);
            report.addEndpoints(endpoints.size());
            values.addAll(endpoints);

            if (docConfig.isParseFeignAnnotations()) {
                Map<String, Object> feignInfo = feignParser.parseFeignClient(sourceCode, qualifiedName);
                if (!feignInfo.isEmpty() && classValue != null) {
                    classValue.setFeignInfo(feignInfo);
                }
            }

            if (docConfig.isParseMyBatisAnnotations()) {
                Map<String, Object> myBatisInfo = myBatisParser.parseMyBatisMapper(sourceCode, qualifiedName);
                if (!myBatisInfo.isEmpty() && classValue != null) {
                    classValue.setMyBatisInfo(myBatisInfo);
                }
            }

            // 共用变量
            Collection<SharedVariableData> sharedVariables = commentParser.getSharedVariableCache().values();
            report.addSharedVariables(sharedVariables.size());
            values.addAll(sharedVariables);

            commentParser.clearCache();

        } catch (IOException e) {
            if (docConfig.isFailOnError()) {
                throw new RuntimeException("Failed to parse file: " + filePath, e);
            }
            report.addFailedFile(filePath, e.getMessage());
        }
        return values;
    }

    private boolean shouldExclude(String filePath) {
        List<String> excludePackages = docConfig.getExcludePackages();
        if (excludePackages == null || excludePackages.isEmpty()) {
            return false;
        }
        for (String exclude : excludePackages) {
            if (filePath.contains(exclude.replace('.', '/'))) {
                return true;
            }
        }
        return false;
    }

    private List<String> collectJavaFiles(String directoryPath) throws IOException {
        List<String> javaFiles = new ArrayList<>();

        Path srcMainJavaPath = Paths.get(directoryPath, "src", "main", "java");
        if (Files.exists(srcMainJavaPath)) {
            try (Stream<Path> paths = Files.walk(srcMainJavaPath)) {
                paths.filter(Files::isRegularFile)
                        .filter(p -> matchesExtension(p.toString()))
                        .forEach(p -> javaFiles.add(p.toString()));
            }
        }

        if (docConfig.isIncludeTestClasses()) {
            Path srcTestJavaPath = Paths.get(directoryPath, "src", "test", "java");
            if (Files.exists(srcTestJavaPath)) {
                try (Stream<Path> paths = Files.walk(srcTestJavaPath)) {
                    paths.filter(Files::isRegularFile)
                            .filter(p -> matchesExtension(p.toString()))
                            .forEach(p -> javaFiles.add(p.toString()));
                }
            }
        }

        return javaFiles;
    }

    private String extractPackageName(String sourceCode) {
        for (String line : sourceCode.split("\n")) {
            line = line.trim();
            if (line.startsWith("package ")) {
                int start = "package ".length();
                int end = line.indexOf(";");
                if (end > start) {
                    return line.substring(start, end).trim();
                }
            }
        }
        return "";
    }
}