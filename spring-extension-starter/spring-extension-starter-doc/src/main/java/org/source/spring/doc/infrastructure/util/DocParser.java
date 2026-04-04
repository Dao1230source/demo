package org.source.spring.doc.infrastructure.util;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.source.spring.doc.domain.element.*;
import org.source.spring.doc.domain.tree.DocEnhanceTree;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * 统一文档解析器
 * <p>
 * 整合所有解析功能的统一入口，支持：
 * <ul>
 *     <li>JavaDoc 注释解析</li>
 *     <li>REST 注解解析（@GetMapping、@PostMapping 等）</li>
 *     <li>JPA 注解解析（@Entity、@Column 等）</li>
 *     <li>JavaDoc 标签解析（@param、@return 等）</li>
 * </ul>
 * </p>
 * <p>
 * 支持多线程并行解析，提高大规模代码库的解析效率。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Getter
public class DocParser {

    /**
     * JavaDoc 注释解析器
     */
    private final DocCommentParser commentParser;

    /**
     * REST 注解解析器
     */
    private final RestAnnotationParser restParser;

    /**
     * JavaDoc 标签解析器
     */
    private final DocTagParser tagParser;

    /**
     * JPA 注解解析器
     */
    private final JpaAnnotationParser jpaParser;

    /**
     * 模块解析器
     */
    private final ModuleParser moduleParser;

    /**
     * 文档树形结构
     */
    private final DocEnhanceTree docTree;

    /**
     * 默认构造函数 - 创建空的文档树
     */
    public DocParser() {
        this.commentParser = new DocCommentParser();
        this.restParser = new RestAnnotationParser();
        this.tagParser = new DocTagParser();
        this.jpaParser = new JpaAnnotationParser();
        this.moduleParser = new ModuleParser();
        this.docTree = new DocEnhanceTree();
    }

    /**
     * 构造函数 - 使用已有的文档树
     *
     * @param docTree 文档树实例
     */
    public DocParser(DocEnhanceTree docTree) {
        this.commentParser = new DocCommentParser();
        this.restParser = new RestAnnotationParser();
        this.tagParser = new DocTagParser();
        this.jpaParser = new JpaAnnotationParser();
        this.moduleParser = new ModuleParser();
        this.docTree = docTree;
    }

    /**
     * 解析整个目录 - 多线程并行版本
     * <p>
     * 自动检测并建立完整的层级结构：Module → Class → Method → Field
     * </p>
     * <p>
     * 流程：
     * <ol>
     *     <li>检测并解析项目模块结构（ModuleParser）</li>
     *     <li>将模块元素添加到树中</li>
     *     <li>并发处理每个模块，在模块内解析所有 Java 文件</li>
     * </ol>
     * </p>
     * <p>
     * 使用 parallelStream 并行处理每个模块，
     * 在每个模块内解析所有 Java 文件，提高大规模代码库的解析效率。
     * </p>
     *
     * @param directoryPath 项目或模块的绝对路径
     * @return 包含所有解析结果的文档树
     * @throws IOException 如果目录读取失败
     */
    public DocEnhanceTree parseDirectory(String directoryPath) throws IOException {
        List<ModuleDocElement> modules = moduleParser.parseProjectModules(directoryPath);
        
        if (!modules.isEmpty()) {
            List<DocElement> moduleElements = new ArrayList<>(modules);
            docTree.addElements(moduleElements);
        }

        List<DocElement> allElements = Collections.synchronizedList(new ArrayList<>());
        
        modules.parallelStream().forEach(module -> {
            String modulePath = module.getModulePath();
            try {
                List<String> javaFiles = collectJavaFiles(modulePath);
                
                javaFiles.forEach(javaFile -> {
                    DocCommentParser localCommentParser = new DocCommentParser();
                    JpaAnnotationParser localJpaParser = new JpaAnnotationParser();
                    DocTagParser localTagParser = new DocTagParser();
                    RestAnnotationParser localRestParser = new RestAnnotationParser();
                    
                    List<DocElement> elements = parseJavaFileToElements(
                        javaFile, 
                        localCommentParser, 
                        localJpaParser, 
                        localTagParser, 
                        localRestParser, 
                        modulePath
                    );
                    allElements.addAll(elements);
                });
            } catch (IOException e) {
                System.err.println("Error parsing module: " + modulePath + " - " + e.getMessage());
            }
        });

        if (!allElements.isEmpty()) {
            docTree.addElements(allElements);
        }

        return docTree;
    }

    /**
     * 收集目录下的所有 Java 文件（仅限 src/main/java）
     * <p>
     * 只收集 src/main/java 目录下的 Java 文件，
     * 排除 test、target、build 等目录下的文件。
     * </p>
     *
     * @param directoryPath 目录路径
     * @return Java 文件路径列表
     * @throws IOException 如果目录遍历失败
     */
    private List<String> collectJavaFiles(String directoryPath) throws IOException {
        Path srcMainJavaPath = Paths.get(directoryPath, "src", "main", "java");
        
        if (!Files.exists(srcMainJavaPath)) {
            return Collections.emptyList();
        }
        
        try (Stream<Path> paths = Files.walk(srcMainJavaPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(Path::toString)
                    .filter(string -> string.endsWith(".java"))
                    .toList();
        }
    }

    /**
     * 解析单个 Java 文件，返回元素列表
     * <p>
     * 线程安全方法，每个线程创建独立的解析器实例。
     * </p>
     *
     * @param filePath 文件路径
     * @param localCommentParser 本线程的注释解析器
     * @param localJpaParser 本线程的 JPA 解析器
     * @param localTagParser 本线程的标签解析器
     * @param localRestParser 本线程的 REST 解析器
     * @param modulePath 所属模块路径（可选，用于建立 class → module 层级关系）
     * @return 解析得到的文档元素列表
     */
    private List<DocElement> parseJavaFileToElements(String filePath, DocCommentParser localCommentParser, 
                                                       JpaAnnotationParser localJpaParser, 
                                                       DocTagParser localTagParser,
                                                       RestAnnotationParser localRestParser,
                                                       String modulePath) {
        List<DocElement> elements = new ArrayList<>();
        try {
            String sourceCode = Files.readString(Paths.get(filePath));
            String fileName = Paths.get(filePath).getFileName().toString();
            String className = fileName.replace(".java", "");

            String packageName = extractPackageName(sourceCode);
            String qualifiedName = StringUtils.isBlank(packageName) ? className : packageName + "." + className;

            ClassDocElement classElement = localCommentParser.parseClassDoc(sourceCode, qualifiedName);
            if (classElement != null) {
                classElement = localJpaParser.parseJpaAnnotations(sourceCode, classElement);
                if (modulePath != null) {
                    classElement.setModuleName(modulePath);
                }
                elements.add(classElement);
            }

            List<MethodDocElement> methods = localCommentParser.parseAllMethods(sourceCode, qualifiedName);
            for (MethodDocElement method : methods) {
                if (StringUtils.isNotBlank(method.getDocContent())) {
                    var tags = localTagParser.parseAllTags(method.getDocContent());
                    method.setDocContent((String) tags.getOrDefault("return", method.getDocContent()));
                }
                elements.add(method);
            }

            List<FieldDocElement> fields = localCommentParser.parseAllFields(sourceCode, qualifiedName);
            for (FieldDocElement field : fields) {
                field = localJpaParser.parseJpaFieldAnnotations(sourceCode, qualifiedName, field);
                elements.add(field);
            }

            List<RestDocElement> endpoints = localRestParser.parseRestEndpoints(sourceCode, qualifiedName);
            elements.addAll(endpoints);

        } catch (IOException e) {
            System.err.println("Error parsing file: " + filePath + " - " + e.getMessage());
        }
        return elements;
    }

    /**
     * 解析单个 Java 文件
     * <p>
     * 单线程版本，保留向后兼容性
     * </p>
     *
     * @param filePath 文件路径
     */
    public void parseJavaFile(String filePath) {
        List<DocElement> elements = parseJavaFileToElements(filePath, commentParser, jpaParser, tagParser, restParser, null);
        for (DocElement element : elements) {
            docTree.addElement(element);
        }
    }

    /**
     * 从源代码中提取包名
     *
     * @param sourceCode Java 源代码
     * @return 包名，如果没有包名则返回空字符串
     */
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