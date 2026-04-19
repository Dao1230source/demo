package org.source.spring.doc.infrastructure.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.source.spring.doc.domain.element.*;
import org.source.spring.doc.domain.object.DocObjectProcessor;
import org.source.spring.doc.domain.object.DocValue;
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
 * 用于解析 Java 源码中的 JavaDoc 注释、注解信息等。
 * </p>
 * <p>
 * 支持解析的内容：
 * <ul>
 *     <li>类、方法、字段的 JavaDoc 注释</li>
 *     <li>JPA 注解（@Entity, @Table, @Column, @Id）</li>
 *     <li>REST 注解（@RestController, @RequestMapping 等）</li>
 *     <li>Spring 注解（@Component, @Service, @Transactional 等）</li>
 *     <li>Feign 注解（@FeignClient）</li>
 *     <li>MyBatis 注解（@Mapper, @Select 等）</li>
 *     <li>验证注解（@NotNull, @NotBlank 等）</li>
 * </ul>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Slf4j
@Getter
public class DocParser {

    /**
     * 文档对象处理器，用于保存解析结果
     */
    private final DocObjectProcessor objectProcessor;

    /**
     * 文档解析配置
     */
    private final DocConfig docConfig;

    /**
     * 解析报告，记录解析过程中的统计信息
     */
    private DocParserReport report;

    /**
     * 构造文档解析器
     *
     * @param objectProcessor 文档对象处理器
     */
    public DocParser(DocObjectProcessor objectProcessor) {
        this.objectProcessor = objectProcessor;
        this.docConfig = new DocConfig();
    }

    /**
     * 构造文档解析器（带配置）
     *
     * @param objectProcessor 文档对象处理器
     * @param docConfig       文档解析配置
     */
    public DocParser(DocObjectProcessor objectProcessor, DocConfig docConfig) {
        this.objectProcessor = objectProcessor;
        this.docConfig = docConfig;
    }

    /**
     * 解析指定目录下的所有 Java 文件
     * <p>
     * 解析流程：
     * <ol>
     *     <li>解析项目模块结构</li>
     *     <li>遍历每个模块下的 Java 文件</li>
     *     <li>解析类、方法、字段、REST 接口等</li>
     *     <li>保存解析结果到数据库</li>
     *     <li>生成解析报告</li>
     * </ol>
     * </p>
     *
     * @param directoryPath 项目根目录路径
     * @return 解析报告
     * @throws IOException 如果读取文件失败
     */
    public DocParserReport parseDirectory(String directoryPath) throws IOException {
        if (objectProcessor == null) {
            throw new IllegalStateException("DocObjectProcessor not configured");
        }

        report = new DocParserReport();
        report.start();

        ModuleParser moduleParser = new ModuleParser();
        List<ModuleDocElement> modules = moduleParser.parseProjectModules(directoryPath);
        modules.forEach(m -> report.incrementModules());

        List<DocValue> allValues = Collections.synchronizedList(new ArrayList<>());

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

        if (!allValues.isEmpty()) {
            objectProcessor.save(allValues);
        }

        report.end();
        if (docConfig.isGenerateReport()) {
            log.info(report.generateSummary());
        }

        return report;
    }

    private void parseModule(ModuleDocElement module, List<DocValue> allValues) {
        String modulePath = module.getModulePath();
        try {
            DocValue moduleValue = convertElementToValue(module);
            if (moduleValue != null) {
                allValues.add(moduleValue);
            }

            List<String> javaFiles = collectJavaFiles(modulePath);
            for (String javaFile : javaFiles) {
                if (shouldExclude(javaFile)) {
                    continue;
                }
                List<DocValue> values = parseJavaFileToValues(javaFile, modulePath);
                allValues.addAll(values);
                report.incrementFiles();
            }
        } catch (IOException e) {
            report.addFailedFile(modulePath, e.getMessage());
        }
    }

    private List<DocValue> parseJavaFileToValues(String filePath, String modulePath) {
        DocCommentParser commentParser = new DocCommentParser();
        JpaAnnotationParser jpaParser = new JpaAnnotationParser();
        DocTagParser tagParser = new DocTagParser();
        RestAnnotationParser restParser = new RestAnnotationParser();
        SpringAnnotationParser springParser = new SpringAnnotationParser();
        FeignAnnotationParser feignParser = new FeignAnnotationParser();
        MyBatisAnnotationParser myBatisParser = new MyBatisAnnotationParser();

        List<DocValue> values = new ArrayList<>();
        try {
            Path path = Paths.get(filePath);
            String sourceCode = Files.readString(path);
            String fileName = path.getFileName().toString();
            String className = fileName.replace(".java", "");

            String packageName = extractPackageName(sourceCode);
            String qualifiedName = StringUtils.isBlank(packageName) ? className : packageName + "." + className;

            commentParser.parseOnce(sourceCode);

            ClassDocElement classElement = commentParser.parseClassDoc(sourceCode, qualifiedName);
            if (classElement != null) {
                classElement = jpaParser.parseJpaAnnotations(sourceCode, classElement);
                if (modulePath != null) {
                    classElement.setModuleName(modulePath);
                }

                if (docConfig.isParseSpringAnnotations()) {
                    Map<String, Object> springAnnotations = springParser.parseComponentAnnotations(sourceCode, qualifiedName);
                    if (!springAnnotations.isEmpty()) {
                        classElement.setSpringAnnotations(springAnnotations);
                    }
                }

                if (StringUtils.isBlank(classElement.getDocContent())) {
                    report.incrementClassesWithoutJavaDoc();
                }
                report.incrementClasses();

                DocValue classValue = convertElementToValue(classElement);
                if (classValue != null) {
                    values.add(classValue);
                }

                List<ClassDocElement> innerClasses = commentParser.parseInnerClasses(sourceCode, qualifiedName);
                for (ClassDocElement inner : innerClasses) {
                    report.incrementClasses();
                    DocValue innerValue = convertElementToValue(inner);
                    if (innerValue != null) {
                        values.add(innerValue);
                    }
                }
            }

            List<MethodDocElement> methods = commentParser.parseAllMethods(sourceCode, qualifiedName);
            for (MethodDocElement method : methods) {
                if (StringUtils.isNotBlank(method.getDocContent())) {
                    var tags = tagParser.parseAllTags(method.getDocContent());
                    method.setDocContent((String) tags.getOrDefault("return", method.getDocContent()));
                }

                if (StringUtils.isBlank(method.getDocContent())) {
                    report.incrementMethodsWithoutJavaDoc();
                }
                report.incrementMethods();

                DocValue methodValue = convertElementToValue(method);
                if (methodValue != null) {
                    values.add(methodValue);
                }

                List<ParameterVariableElement> params = commentParser.parseMethodParameters(sourceCode, qualifiedName, method.getMethodName());
                for (ParameterVariableElement param : params) {
                    report.incrementParameters();
                    DocValue paramValue = convertElementToValue(param);
                    if (paramValue != null) {
                        values.add(paramValue);
                    }
                }

                ParameterVariableElement returnValue = commentParser.parseMethodReturnValue(sourceCode, qualifiedName, method.getMethodName());
                if (returnValue != null) {
                    report.incrementParameters();
                    DocValue returnValueDoc = convertElementToValue(returnValue);
                    if (returnValueDoc != null) {
                        values.add(returnValueDoc);
                    }
                }
            }

            List<MemberVariableElement> fields = commentParser.parseAllMemberVariables(sourceCode, qualifiedName);
            for (MemberVariableElement field : fields) {
                JpaColumnVariableElement jpaColumn = jpaParser.parseJpaColumnVariable(sourceCode, qualifiedName, field.getVariableName(), field.getSharedVariable());
                DocElement fieldElement = Objects.requireNonNullElse(jpaColumn, field);

                if (StringUtils.isBlank(field.getDocContent())) {
                    report.incrementFieldsWithoutJavaDoc();
                }
                report.incrementFields();

                DocValue fieldValue = convertElementToValue(fieldElement);
                if (fieldValue != null) {
                    values.add(fieldValue);
                }
            }

            List<RestDocElement> endpoints = restParser.parseRestEndpoints(sourceCode, qualifiedName);
            for (RestDocElement endpoint : endpoints) {
                report.incrementEndpoints();
                DocValue endpointValue = convertElementToValue(endpoint);
                if (endpointValue != null) {
                    values.add(endpointValue);
                }
            }

            if (docConfig.isParseFeignAnnotations()) {
                Map<String, Object> feignInfo = feignParser.parseFeignClient(sourceCode, qualifiedName);
                if (!feignInfo.isEmpty() && classElement != null) {
                    classElement.setFeignInfo(feignInfo);
                }
            }

            if (docConfig.isParseMyBatisAnnotations()) {
                Map<String, Object> myBatisInfo = myBatisParser.parseMyBatisMapper(sourceCode, qualifiedName);
                if (!myBatisInfo.isEmpty() && classElement != null) {
                    classElement.setMyBatisInfo(myBatisInfo);
                }
            }

            Collection<SharedVariableElement> sharedVariables = commentParser.getSharedVariableCache().values();
            for (SharedVariableElement sharedVar : sharedVariables) {
                DocValue sharedValue = convertElementToValue(sharedVar);
                if (sharedValue != null) {
                    values.add(sharedValue);
                }
            }

            commentParser.clearCache();

        } catch (IOException e) {
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

    private DocValue convertElementToValue(DocElement element) {
        if (element == null) {
            return null;
        }

        DocValue value = new DocValue();
        value.setObjectId(element.getId());
        value.setName(element.getId());

        if (element instanceof ClassDocElement classElement) {
            value.setName(classElement.getClassName());
        } else if (element instanceof MethodDocElement methodElement) {
            value.setName(methodElement.getMethodName());
        } else if (element instanceof MemberVariableElement memberElement) {
            value.setName(memberElement.getVariableName());
            if (element instanceof JpaColumnVariableElement jpaElement) {
                value.setName(jpaElement.getColumnName());
            }
        } else if (element instanceof SharedVariableElement sharedElement) {
            value.setName(sharedElement.getVariableName());
        } else if (element instanceof RestDocElement restElement) {
            value.setName(restElement.getHttpMethod() + ":" + restElement.getPath());
        } else if (element instanceof ModuleDocElement moduleElement) {
            value.setName(moduleElement.getModuleName());
        } else if (element instanceof ParameterVariableElement paramElement) {
            value.setName(paramElement.getVariableName());
        }

        return value;
    }

    private List<String> collectJavaFiles(String directoryPath) throws IOException {
        Path srcMainJavaPath = Paths.get(directoryPath, "src", "main", "java");
        if (!Files.exists(srcMainJavaPath)) {
            return Collections.emptyList();
        }
        try (Stream<Path> paths = Files.walk(srcMainJavaPath)) {
            return paths.filter(Files::isRegularFile).map(Path::toString).filter(s -> s.endsWith(".java")).toList();
        }
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