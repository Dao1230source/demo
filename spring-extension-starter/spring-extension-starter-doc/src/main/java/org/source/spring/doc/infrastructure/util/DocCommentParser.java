package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import org.source.spring.doc.domain.element.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JavaDoc 注释解析器
 * <p>
 * 基于 JavaParser 实现的 JavaDoc 注释解析工具，
 * 用于解析类、方法、字段的 JavaDoc 注释内容。
 * </p>
 * <p>
 * 支持解析：
 * <ul>
 *     <li>类级别的 JavaDoc 注释</li>
 *     <li>方法级别的 JavaDoc 注释</li>
 *     <li>字段级别的 JavaDoc 注释</li>
 *     <li>方法入参和返回值信息（作为独立元素）</li>
 *     <li>批量解析所有方法、字段</li>
 * </ul>
 * </p>
 * <p>
 * 变量通过共用变量元素（SharedVariableElement）实现共享，
 * 同一变量在多处使用时共享同一个实例。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocCommentParser {

    private final JavaParser javaParser;

    private final Map<String, SharedVariableElement> sharedVariableCache = new ConcurrentHashMap<>();

    /**
     * 缓存的 CompilationUnit，避免重复解析同一源文件
     */
    private CompilationUnit cachedCompilationUnit;

    /**
     * 缓存的源代码，用于判断是否需要重新解析
     */
    private String cachedSourceCode;

    /**
     * 构造 JavaDoc 注释解析器实例
     * <p>
     * 初始化 JavaParser 配置，不启用符号解析器以提高解析速度。
     * </p>
     */
    public DocCommentParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析源代码并缓存 CompilationUnit
     * <p>
     * 如果源代码与缓存相同，直接返回缓存的 CompilationUnit；
     * 否则重新解析并更新缓存。
     * </p>
     *
     * @param sourceCode Java 源代码内容
     * @return 解析后的 CompilationUnit，如果解析失败返回 null
     */
    public CompilationUnit parseOnce(String sourceCode) {
        if (sourceCode != null && sourceCode.equals(cachedSourceCode) && cachedCompilationUnit != null) {
            return cachedCompilationUnit;
        }
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (result.isSuccessful() && result.getResult().isPresent()) {
            cachedCompilationUnit = result.getResult().get();
            cachedSourceCode = sourceCode;
            return cachedCompilationUnit;
        }
        return null;
    }

    /**
     * 清空缓存
     * <p>
     * 清空 CompilationUnit 缓存和共用变量缓存，
     * 用于解析完一个文件后准备解析下一个文件。
     * </p>
     */
    public void clearCache() {
        cachedCompilationUnit = null;
        cachedSourceCode = null;
        sharedVariableCache.clear();
    }

    /**
     * 解析类的 JavaDoc 注释
     * <p>
     * 解析指定类的 JavaDoc 注释内容，提取类名、全限定名、修饰符等信息。
     * </p>
     *
     * @param sourceCode         Java 源代码内容
     * @param classQualifiedName 类的全限定名
     * @return 类文档元素，如果解析失败返回 null
     */
    public ClassDocElement parseClassDoc(String sourceCode, String classQualifiedName) {
        CompilationUnit cu = parseOnce(sourceCode);
        if (cu == null) {
            return null;
        }
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.findFirst(ClassOrInterfaceDeclaration.class,
                c -> c.getNameAsString().equals(simpleName));
        if (classOpt.isEmpty()) {
            return null;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        ClassDocElement element = new ClassDocElement();
        element.setClassName(simpleName);
        element.setClassQualifiedName(classQualifiedName);
        element.setModifiers(classDecl.getModifiers().toString());

        Optional<JavadocComment> javadoc = classDecl.getJavadocComment();
        if (javadoc.isPresent()) {
            element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
        } else {
            element.setDocContent("");
        }

        return element;
    }

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

    public MethodDocElement parseMethodDoc(String sourceCode, String classQualifiedName, String methodName) {
        CompilationUnit cu = parseOnce(sourceCode);
        if (cu == null) {
            return null;
        }
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return null;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        Optional<MethodDeclaration> methodOpt = classDecl.getMethodsByName(methodName).stream().findFirst();
        if (methodOpt.isEmpty()) {
            return null;
        }

        MethodDeclaration methodDecl = methodOpt.get();
        MethodDocElement element = new MethodDocElement();
        element.setMethodName(methodName);
        element.setReturnType(methodDecl.getType().asString());
        element.setReturnTypeQualifiedName(methodDecl.getType().asString());
        element.setClassQualifiedName(classQualifiedName);

        Optional<JavadocComment> javadoc = methodDecl.getJavadocComment();
        if (javadoc.isPresent()) {
            element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
        }

        return element;
    }

    public MemberVariableElement parseMemberVariableDoc(String sourceCode, String classQualifiedName, String variableName) {
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return null;
        }

        CompilationUnit cu = result.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return null;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        Optional<FieldDeclaration> fieldOpt = classDecl.getFields().stream()
                .filter(f -> f.getVariable(0).getNameAsString().equals(variableName))
                .findFirst();

        if (fieldOpt.isEmpty()) {
            return null;
        }

        FieldDeclaration fieldDecl = fieldOpt.get();
        String type = fieldDecl.getCommonType().asString();

        SharedVariableElement sharedVar = getOrCreateSharedVariable(variableName, type);

        MemberVariableElement element = new MemberVariableElement();
        element.setClassQualifiedName(classQualifiedName);
        element.setSharedVariable(sharedVar);

        Optional<JavadocComment> javadoc = fieldDecl.getJavadocComment();
        if (javadoc.isPresent()) {
            element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
        }

        return element;
    }

    public List<MethodDocElement> parseAllMethods(String sourceCode, String classQualifiedName) {
        List<MethodDocElement> methods = new ArrayList<>();

        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return methods;
        }

        CompilationUnit cu = result.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return methods;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        // 解析普通方法
        for (MethodDeclaration method : classDecl.getMethods()) {
            MethodDocElement element = new MethodDocElement();
            element.setMethodName(method.getNameAsString());
            element.setReturnType(method.getType().asString());
            element.setReturnTypeQualifiedName(method.getType().asString());
            element.setClassQualifiedName(classQualifiedName);
            element.setIsConstructor(false);

            String paramTypes = method.getParameters().stream()
                    .map(p -> p.getType().asString())
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            element.setParameterTypes(paramTypes);

            Optional<JavadocComment> javadoc = method.getJavadocComment();
            if (javadoc.isPresent()) {
                element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            }

            methods.add(element);
        }

        // 解析构造函数
        for (ConstructorDeclaration constructor : classDecl.getConstructors()) {
            MethodDocElement element = new MethodDocElement();
            element.setMethodName(simpleName);
            element.setReturnType(simpleName);
            element.setReturnTypeQualifiedName(classQualifiedName);
            element.setClassQualifiedName(classQualifiedName);
            element.setIsConstructor(true);

            String paramTypes = constructor.getParameters().stream()
                    .map(p -> p.getType().asString())
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            element.setParameterTypes(paramTypes);

            Optional<JavadocComment> javadoc = constructor.getJavadocComment();
            if (javadoc.isPresent()) {
                element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            }

            methods.add(element);
        }

        return methods;
    }

    public List<ParameterVariableElement> parseMethodParameters(String sourceCode, String classQualifiedName, String methodName) {
        List<ParameterVariableElement> parameters = new ArrayList<>();

        CompilationUnit cu = parseOnce(sourceCode);
        if (cu == null) {
            return parameters;
        }
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return parameters;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        Optional<MethodDeclaration> methodOpt = classDecl.getMethodsByName(methodName).stream().findFirst();
        if (methodOpt.isEmpty()) {
            return parameters;
        }

        MethodDeclaration method = methodOpt.get();
        String methodId = classQualifiedName + "#" + methodName;

        int order = 0;
        for (Parameter param : method.getParameters()) {
            String type = param.getType().asString();
            SharedVariableElement sharedVar = getOrCreateSharedVariable(param.getNameAsString(), type);

            ParameterVariableElement element = new ParameterVariableElement();
            element.setMethodId(methodId);
            element.setParameterOrder(order);
            element.setSharedVariable(sharedVar);

            Optional<Comment> paramComment = param.getComment();
            if (paramComment.isPresent()) {
                element.setDocContent(cleanJavadocContent(paramComment.get().getContent()));
            }

            parameters.add(element);
            order++;
        }

        return parameters;
    }

    public ParameterVariableElement parseMethodReturnValue(String sourceCode, String classQualifiedName, String methodName) {
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return null;
        }

        CompilationUnit cu = result.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return null;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        Optional<MethodDeclaration> methodOpt = classDecl.getMethodsByName(methodName).stream().findFirst();
        if (methodOpt.isEmpty()) {
            return null;
        }

        MethodDeclaration method = methodOpt.get();
        String methodId = classQualifiedName + "#" + methodName;
        String type = method.getType().asString();

        ParameterVariableElement element = new ParameterVariableElement();
        element.setMethodId(methodId);
        element.setParameterOrder(-1);
        element.setVariableName("return");
        element.setVariableType(type);
        element.setVariableTypeQualifiedName(type);
        element.setPrimitive(isPrimitiveType(type));

        Optional<JavadocComment> javadoc = method.getJavadocComment();
        if (javadoc.isPresent()) {
            String returnDoc = extractReturnDoc(javadoc.get().getContent());
            if (returnDoc != null) {
                element.setDocContent(returnDoc);
            }
        }

        return element;
    }

    private String extractReturnDoc(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("*")) {
                trimmed = trimmed.substring(1).trim();
            }
            if (trimmed.startsWith("@return")) {
                return trimmed.substring("@return".length()).trim();
            }
        }
        return null;
    }

    public List<MemberVariableElement> parseAllMemberVariables(String sourceCode, String classQualifiedName) {
        List<MemberVariableElement> fields = new ArrayList<>();

        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return fields;
        }

        CompilationUnit cu = result.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return fields;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        for (FieldDeclaration field : classDecl.getFields()) {
            String fieldName = field.getVariable(0).getNameAsString();
            String type = field.getCommonType().asString();

            SharedVariableElement sharedVar = getOrCreateSharedVariable(fieldName, type);

            MemberVariableElement element = new MemberVariableElement();
            element.setClassQualifiedName(classQualifiedName);
            element.setSharedVariable(sharedVar);

            Optional<JavadocComment> javadoc = field.getJavadocComment();
            if (javadoc.isPresent()) {
                element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            }

            fields.add(element);
        }

        return fields;
    }

    private SharedVariableElement getOrCreateSharedVariable(String variableName, String variableType) {
        String key = variableType + "#" + variableName;
        return sharedVariableCache.computeIfAbsent(key, k -> {
            SharedVariableElement element = new SharedVariableElement();
            element.setVariableName(variableName);
            element.setVariableType(variableType);
            element.setVariableTypeQualifiedName(variableType);
            element.setPrimitive(isPrimitiveType(variableType));
            return element;
        });
    }

    public Map<String, SharedVariableElement> getSharedVariableCache() {
        return sharedVariableCache;
    }

    /**
     * 解析类的内部类/嵌套类信息
     * <p>
     * 递归解析类中的所有内部类，返回内部类的文档元素列表。
     * 内部类的全限定名为：外部类全限定名$内部类名
     * </p>
     *
     * @param sourceCode         Java 源代码内容
     * @param classQualifiedName 外部类的全限定名
     * @return 内部类文档元素列表
     */
    public List<ClassDocElement> parseInnerClasses(String sourceCode, String classQualifiedName) {
        List<ClassDocElement> innerClasses = new ArrayList<>();

        CompilationUnit cu = parseOnce(sourceCode);
        if (cu == null) {
            return innerClasses;
        }

        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return innerClasses;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();

        // 解析内部类和接口
        for (ClassOrInterfaceDeclaration innerClass : classDecl.getMembers().stream()
                .filter(m -> m instanceof ClassOrInterfaceDeclaration)
                .map(m -> (ClassOrInterfaceDeclaration) m)
                .toList()) {

            ClassDocElement element = new ClassDocElement();
            String innerClassName = innerClass.getNameAsString();
            String innerQualifiedName = classQualifiedName + "$" + innerClassName;
            element.setClassName(innerClassName);
            element.setClassQualifiedName(innerQualifiedName);
            element.setModifiers(innerClass.getModifiers().toString());
            element.setIsInterface(innerClass.isInterface());
            element.setIsEnum(false);

            Optional<JavadocComment> javadoc = innerClass.getJavadocComment();
            if (javadoc.isPresent()) {
                element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            } else {
                element.setDocContent("");
            }

            innerClasses.add(element);
        }

        // 解析内部枚举
        for (EnumDeclaration innerEnum : classDecl.getMembers().stream()
                .filter(m -> m instanceof EnumDeclaration)
                .map(m -> (EnumDeclaration) m)
                .toList()) {

            ClassDocElement element = new ClassDocElement();
            String innerClassName = innerEnum.getNameAsString();
            String innerQualifiedName = classQualifiedName + "$" + innerClassName;
            element.setClassName(innerClassName);
            element.setClassQualifiedName(innerQualifiedName);
            element.setModifiers(innerEnum.getModifiers().toString());
            element.setIsInterface(false);
            element.setIsEnum(true);

            Optional<JavadocComment> javadoc = innerEnum.getJavadocComment();
            if (javadoc.isPresent()) {
                element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            } else {
                element.setDocContent("");
            }

            innerClasses.add(element);
        }

        return innerClasses;
    }

    private static boolean isPrimitiveType(String type) {
        return "byte".equals(type) || "short".equals(type) || "int".equals(type) || "long".equals(type)
                || "float".equals(type) || "double".equals(type) || "boolean".equals(type) || "char".equals(type);
    }
}