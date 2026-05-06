package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import org.source.spring.doc.domain.value.SpringBeanData;
import org.source.spring.doc.domain.object.DocObjectTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring 核心注解解析器
 * <p>
 * 基于 JavaParser 实现的 Spring 核心注解解析工具，
 * 用于解析组件注解、配置注解、行为注解等信息。
 * </p>
 * <p>
 * 支持解析的注解类别：
 * <ul>
 *     <li>组件注解：@Component, @Service, @Repository, @Controller</li>
 *     <li>配置注解：@Configuration, @ConfigurationProperties</li>
 *     <li>行为注解：@Transactional, @Scheduled, @Async</li>
 *     <li>条件注解：@ConditionalOnProperty, @ConditionalOnBean, @Profile</li>
 * </ul>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class SpringAnnotationParser {

    private final JavaParser javaParser;

    /**
     * 构造 Spring 注解解析器实例
     * <p>
     * 初始化 JavaParser 配置，不启用符号解析器以提高解析速度
     * </p>
     */
    public SpringAnnotationParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析类上的 Spring 组件注解信息
     * <p>
     * 解析 @Component, @Service, @Repository, @Controller, @Configuration 等注解，
     * 返回包含注解名称和属性值的 Map
     * </p>
     *
     * @param sourceCode Java 源代码内容
     * @param classQualifiedName 类的全限定名
     * @return Spring 组件注解信息 Map，如果没有组件注解返回空 Map
     */
    public Map<String, Object> parseComponentAnnotations(String sourceCode, String classQualifiedName) {
        Map<String, Object> result = new HashMap<>();

        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);
        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            return result;
        }

        CompilationUnit cu = parseResult.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return result;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (isComponentAnnotation(annotationName)) {
                Map<String, String> attributes = extractAnnotationAttributes(annotation);
                result.put(annotationName, attributes);
            }
        }

        return result;
    }

    /**
     * 解析方法上的行为注解信息
     * <p>
     * 解析 @Transactional, @Scheduled, @Async 等行为注解，
     * 返回包含方法名和注解信息的 Map
     * </p>
     *
     * @param sourceCode Java 源代码内容
     * @param classQualifiedName 类的全限定名
     * @return 方法行为注解信息列表
     */
    public List<Map<String, Object>> parseBehaviorAnnotations(String sourceCode, String classQualifiedName) {
        List<Map<String, Object>> result = new ArrayList<>();

        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);
        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            return result;
        }

        CompilationUnit cu = parseResult.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return result;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        for (MethodDeclaration method : classDecl.getMethods()) {
            Map<String, Object> methodInfo = new HashMap<>();
            methodInfo.put("methodName", method.getNameAsString());

            Map<String, Map<String, String>> annotations = new HashMap<>();
            for (AnnotationExpr annotation : method.getAnnotations()) {
                String annotationName = annotation.getNameAsString();
                if (isBehaviorAnnotation(annotationName)) {
                    annotations.put(annotationName, extractAnnotationAttributes(annotation));
                }
            }

            if (!annotations.isEmpty()) {
                methodInfo.put("annotations", annotations);
                result.add(methodInfo);
            }
        }

        return result;
    }

    /**
     * 解析类上的条件注解信息
     * <p>
     * 解析 @ConditionalOnProperty, @ConditionalOnBean, @Profile 等条件注解
     * </p>
     *
     * @param sourceCode Java 源代码内容
     * @param classQualifiedName 类的全限定名
     * @return 条件注解信息 Map
     */
    public Map<String, Object> parseConditionalAnnotations(String sourceCode, String classQualifiedName) {
        Map<String, Object> result = new HashMap<>();

        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);
        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            return result;
        }

        CompilationUnit cu = parseResult.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return result;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (isConditionalAnnotation(annotationName)) {
                Map<String, String> attributes = extractAnnotationAttributes(annotation);
                result.put(annotationName, attributes);
            }
        }

        return result;
    }

    /**
     * 判断是否为组件类型注解
     *
     * @param annotationName 注解名称
     * @return 如果是组件注解返回 true
     */
    private boolean isComponentAnnotation(String annotationName) {
        return "Component".equals(annotationName)
                || "Service".equals(annotationName)
                || "Repository".equals(annotationName)
                || "Controller".equals(annotationName)
                || "Configuration".equals(annotationName)
                || "ConfigurationProperties".equals(annotationName)
                || "RestController".equals(annotationName);
    }

    /**
     * 判断是否为行为类型注解
     *
     * @param annotationName 注解名称
     * @return 如果是行为注解返回 true
     */
    private boolean isBehaviorAnnotation(String annotationName) {
        return "Transactional".equals(annotationName)
                || "Scheduled".equals(annotationName)
                || "Async".equals(annotationName);
    }

    /**
     * 判断是否为条件类型注解
     *
     * @param annotationName 注解名称
     * @return 如果是条件注解返回 true
     */
    private boolean isConditionalAnnotation(String annotationName) {
        return "ConditionalOnProperty".equals(annotationName)
                || "ConditionalOnBean".equals(annotationName)
                || "ConditionalOnClass".equals(annotationName)
                || "ConditionalOnMissingBean".equals(annotationName)
                || "Profile".equals(annotationName)
                || "Conditional".equals(annotationName);
    }

    /**
     * 提取注解属性值
     * <p>
     * 使用 JavaParser AST API 提取注解的属性键值对
     * </p>
     *
     * @param annotation 注解表达式
     * @return 注解属性 Map
     */
    private Map<String, String> extractAnnotationAttributes(AnnotationExpr annotation) {
        Map<String, String> attributes = new HashMap<>();

        if (annotation instanceof NormalAnnotationExpr normal) {
            for (com.github.javaparser.ast.expr.MemberValuePair pair : normal.getPairs()) {
                attributes.put(pair.getNameAsString(), pair.getValue().toString());
            }
        } else if (annotation instanceof SingleMemberAnnotationExpr single) {
            attributes.put("value", single.getMemberValue().toString());
        }

        return attributes;
    }

    /**
     * 解析 Spring Bean 信息
     * <p>
     * 解析类上的 Spring 组件注解，提取 Bean 名称、类型、依赖、事务、异步等信息
     * </p>
     *
     * @param sourceCode Java 源代码内容
     * @param classQualifiedName 类的全限定名
     * @return SpringBeanValue 列表，如果没有组件注解返回空列表
     */
    public List<SpringBeanData> parseSpringBeanValues(String sourceCode, String classQualifiedName) {
        List<SpringBeanData> result = new ArrayList<>();

        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);
        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            return result;
        }

        CompilationUnit cu = parseResult.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return result;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();

        // 查找组件注解
        String beanType = null;
        String beanName = null;
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if (isComponentAnnotation(annotationName)) {
                beanType = annotationName;
                // 提取 value 属性作为 bean 名称
                Map<String, String> attrs = extractAnnotationAttributes(annotation);
                beanName = attrs.get("value");
                if (beanName != null) {
                    beanName = beanName.replaceAll("\"", "");
                }
                break;
            }
        }

        if (beanType == null) {
            return result;
        }

        SpringBeanData value = new SpringBeanData();
        value.setName(beanName != null ? beanName : simpleName);
        value.setParentName(classQualifiedName);
        value.setSorted("0");
        value.setRelationType(DocObjectTypeEnum.SPRING_BEAN.getType());
        value.setBeanName(beanName != null ? beanName : simpleName);
        value.setBeanType(beanType);

        // 解析 JavaDoc
        Optional<JavadocComment> javadoc = classDecl.getJavadocComment();
        if (javadoc.isPresent()) {
            value.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
        }

        // 检查事务注解
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            if ("Transactional".equals(annotationName)) {
                value.setTransactional(true);
            }
            if ("Async".equals(annotationName)) {
                value.setAsync(true);
            }
        }

        // 解析依赖注入（查找 @Autowired、@Resource 字段）
        List<String> dependencies = new ArrayList<>();
        for (FieldDeclaration field : classDecl.getFields()) {
            for (AnnotationExpr fieldAnnotation : field.getAnnotations()) {
                String fieldName = fieldAnnotation.getNameAsString();
                if ("Autowired".equals(fieldName) || "Resource".equals(fieldName) || "Inject".equals(fieldName)) {
                    String fieldType = field.getCommonType().asString();
                    dependencies.add(fieldType);
                }
            }
        }
        if (!dependencies.isEmpty()) {
            value.setDependencies(dependencies);
        }

        // 解析方法上的 @Scheduled 注解
        for (MethodDeclaration method : classDecl.getMethods()) {
            for (AnnotationExpr methodAnnotation : method.getAnnotations()) {
                if ("Scheduled".equals(methodAnnotation.getNameAsString())) {
                    Map<String, String> attrs = extractAnnotationAttributes(methodAnnotation);
                    String cron = attrs.get("cron");
                    if (cron != null) {
                        value.setCronExpression(cron.replaceAll("\"", ""));
                    }
                }
            }
        }

        // 解析废弃注解
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            if ("Deprecated".equals(annotation.getNameAsString())) {
                value.setDeprecated(true);
                if (annotation instanceof NormalAnnotationExpr normal) {
                    for (com.github.javaparser.ast.expr.MemberValuePair pair : normal.getPairs()) {
                        if ("since".equals(pair.getNameAsString()) || "forRemoval".equals(pair.getNameAsString())) {
                            value.setDeprecatedReason(pair.getValue().toString().replaceAll("\"", ""));
                        }
                    }
                }
            }
        }

        result.add(value);
        return result;
    }

    /**
     * 清理 JavaDoc 内容
     */
    private String cleanJavadocContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String[] lines = content.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("*")) {
                trimmed = trimmed.substring(1).trim();
            }
            if (trimmed.startsWith("@")) {
                break;
            }
            if (!trimmed.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(trimmed);
            }
        }
        return sb.toString();
    }
}