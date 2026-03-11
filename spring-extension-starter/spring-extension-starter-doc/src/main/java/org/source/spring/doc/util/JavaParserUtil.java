package org.source.spring.doc.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.body.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JavaParser 工具类
 * 
 * <p>用于解析Java源代码文件，提取注释和注解信息</p>
 */
public class JavaParserUtil {

    private static final Logger logger = LoggerFactory.getLogger(JavaParserUtil.class);
    
    private final JavaParser javaParser;

    public JavaParserUtil() {
        this.javaParser = new JavaParser();
    }

    /**
     * 解析单个Java文件并打印注释和注解信息
     * 
     * @param filePath 文件路径
     */
    public void parseAndPrintCommentsAndAnnotations(String filePath) {
        if (org.apache.commons.lang3.StringUtils.isBlank(filePath)) {
            logger.warn("File path is blank, skipping parsing");
            return;
        }
        
        try {
            logger.info("=== Parsing file: {} ===", filePath);
            
            ParseResult<CompilationUnit> result = javaParser.parse(Files.readString(Paths.get(filePath)));
            
            if (result.isSuccessful()) {
                CompilationUnit compilationUnit = result.getResult().get();
                
                // 提取所有注释
                extractAndPrintComments(compilationUnit);
                
                // 提取所有注解
                extractAndPrintAnnotations(compilationUnit);
                
            } else {
                logger.error("Parsing failed: {}", filePath);
                result.getProblems().forEach(problem -> 
                    logger.error("  - {}", problem));
            }
            
        } catch (IOException e) {
            logger.error("Failed to read file: {} - {}", filePath, e.getMessage(), e);
        }
    }

    /**
     * 递归提取并打印所有注释
     * 
     * @param node AST节点
     */
    private void extractAndPrintComments(com.github.javaparser.ast.Node node) {
        if (node == null) {
            return;
        }
        // 处理当前节点的注释
        if (node.getComment().isPresent()) {
            Comment comment = node.getComment().get();
            String commentText = comment.getContent().trim();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(commentText)) {
                String nodeType = node.getClass().getSimpleName();
                String nodeInfo = getNodeInfo(node);
                logger.info("注释 - 类型: {}, 内容: \"{}\", 节点: {}", nodeType, commentText, nodeInfo);
            }
        }
        
        // 递归处理子节点
        List<com.github.javaparser.ast.Node> childNodes = node.getChildNodes();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(childNodes)) {
            for (com.github.javaparser.ast.Node child : childNodes) {
                extractAndPrintComments(child);
            }
        }
    }

    /**
     * 递归提取并打印所有注解
     * 
     * @param node AST节点
     */
    private void extractAndPrintAnnotations(com.github.javaparser.ast.Node node) {
        if (node == null) {
            return;
        }
        // 检查节点是否支持注解
        if (node instanceof BodyDeclaration) {
            BodyDeclaration<?> bodyDeclaration = (BodyDeclaration<?>) node;
            NodeList<AnnotationExpr> annotations = bodyDeclaration.getAnnotations();
            
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(annotations)) {
                String nodeType = node.getClass().getSimpleName();
                String nodeInfo = getNodeInfo(node);
                
                for (AnnotationExpr annotation : annotations) {
                    String annotationName = annotation.getNameAsString();
                    String annotationDetails = getAnnotationDetails(annotation);
                    logger.info("注解 - 类型: {}, 名称: @{}{}, 节点: {}", 
                        nodeType, annotationName, 
                        annotationDetails.isEmpty() ? "" : " " + annotationDetails, 
                        nodeInfo);
                }
            }
        }
        
        // 递归处理子节点
        List<com.github.javaparser.ast.Node> childNodes = node.getChildNodes();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(childNodes)) {
            for (com.github.javaparser.ast.Node child : childNodes) {
                extractAndPrintAnnotations(child);
            }
        }
    }

    /**
     * 获取节点信息（用于显示）
     */
    private String getNodeInfo(com.github.javaparser.ast.Node node) {
        if (node instanceof TypeDeclaration) {
            return "类/接口: " + ((TypeDeclaration<?>) node).getName();
        } else if (node instanceof MethodDeclaration) {
            return "方法: " + ((MethodDeclaration) node).getName();
        } else if (node instanceof FieldDeclaration) {
            return "字段: " + getFieldNames((FieldDeclaration) node);
        } else if (node instanceof EnumConstantDeclaration) {
            return "枚举常量: " + ((EnumConstantDeclaration) node).getName();
        }
        return node.toString();
    }

    /**
     * 获取字段名称列表
     */
    private String getFieldNames(FieldDeclaration field) {
        if (field == null) {
            return "";
        }
        return field.getVariables().stream()
                .map(v -> v.getNameAsString())
                .collect(Collectors.joining(", "));
    }

    /**
     * 获取注解详细信息
     */
    private String getAnnotationDetails(AnnotationExpr annotation) {
        if (annotation == null) {
            return "";
        }
        if (annotation instanceof MarkerAnnotationExpr) {
            return "";
        } else if (annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr single = (SingleMemberAnnotationExpr) annotation;
            return "(" + single.getMemberValue() + ")";
        } else if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normal = (NormalAnnotationExpr) annotation;
            if (normal.getPairs().isEmpty()) {
                return "()";
            }
            String pairs = normal.getPairs().stream()
                    .map(pair -> pair.getName() + "=" + pair.getValue())
                    .collect(Collectors.joining(", "));
            return "(" + pairs + ")";
        }
        return "";
    }

    /**
     * 解析整个目录下的所有Java文件
     * 
     * @param directoryPath 目录路径
     */
    public void parseDirectory(String directoryPath) {
        if (org.apache.commons.lang3.StringUtils.isBlank(directoryPath)) {
            logger.warn("Directory path is blank, skipping parsing");
            return;
        }
        
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            List<String> javaFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(Path::toString)
                    .collect(Collectors.toList());
            
            logger.info("找到 {} 个Java文件", javaFiles.size());
            
            for (String javaFile : javaFiles) {
                parseAndPrintCommentsAndAnnotations(javaFile);
                logger.info(""); // 空行分隔
            }
            
        } catch (IOException e) {
            logger.error("遍历目录失败: {} - {}", directoryPath, e.getMessage(), e);
        }
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        JavaParserUtil parserUtil = new JavaParserUtil();
        
        // 测试单个文件
        String testFilePath = "/Users/zengfugen/IdeaProjects/dao1230.source/demo/spring-extension-starter/spring-extension-starter-doc/src/main/java/org/source/spring/doc/entity/UserEntity.java";
        File testFile = new File(testFilePath);
        if (testFile.exists()) {
            parserUtil.parseAndPrintCommentsAndAnnotations(testFilePath);
        } else {
            logger.warn("测试文件不存在: {}", testFilePath);
        }
        
        // 测试整个目录（可选）
        // parserUtil.parseDirectory("src/main/java/org/source/spring/doc");
    }
}