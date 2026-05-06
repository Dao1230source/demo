package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import org.source.spring.doc.domain.value.*;
import org.source.spring.doc.domain.object.DocObjectTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JavaDoc 注释解析器
 * <p>
 * 基于 JavaParser 实现的 JavaDoc 注释解析工具，
 * 直接返回 Value 对象。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocCommentParser {

    private final JavaParser javaParser;

    private final Map<String, SharedVariableData> sharedVariableCache = new ConcurrentHashMap<>();

    private CompilationUnit cachedCompilationUnit;

    private String cachedSourceCode;

    private boolean parseValidationAnnotations = true;

    public DocCommentParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    public DocCommentParser(boolean parseValidationAnnotations) {
        this();
        this.parseValidationAnnotations = parseValidationAnnotations;
    }

    public void setParseValidationAnnotations(boolean parseValidationAnnotations) {
        this.parseValidationAnnotations = parseValidationAnnotations;
    }

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

    public void clearCache() {
        cachedCompilationUnit = null;
        cachedSourceCode = null;
        sharedVariableCache.clear();
    }

    /**
     * 解析类的 JavaDoc 注释
     *
     * @return 类文档值对象，如果解析失败返回 null
     */
    public ClassDocData parseClassDoc(String sourceCode, String classQualifiedName, String moduleName, int sorted) {
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
        ClassDocData value = new ClassDocData();
        value.setName(classQualifiedName);
        value.setParentName(moduleName);
        value.setSorted(String.valueOf(sorted));
        value.setRelationType(DocObjectTypeEnum.CLASS.getType());
        value.setClassName(simpleName);
        value.setModifiers(classDecl.getModifiers().toString());

        // 解析废弃注解
        String[] deprecatedInfo = parseDeprecatedAnnotation(classDecl.getAnnotations());
        if (deprecatedInfo != null) {
            value.setDeprecated(true);
            value.setDeprecatedReason(deprecatedInfo[1]);
        }

        Optional<JavadocComment> javadoc = classDecl.getJavadocComment();
        if (javadoc.isPresent()) {
            value.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
        } else {
            value.setDocContent("");
        }

        return value;
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

    /**
     * 解析所有方法
     *
     * @return 方法文档值对象列表
     */
    public List<MethodDocData> parseAllMethods(String sourceCode, String classQualifiedName) {
        List<MethodDocData> methods = new ArrayList<>();

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
        int sorted = 0;

        // 解析普通方法
        for (MethodDeclaration method : classDecl.getMethods()) {
            MethodDocData value = new MethodDocData();
            String paramTypes = method.getParameters().stream()
                    .map(p -> p.getType().asString())
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            String methodId = classQualifiedName + "#" + method.getNameAsString();
            if (!paramTypes.isEmpty()) {
                methodId = methodId + "(" + paramTypes + ")";
            }
            value.setName(methodId);
            value.setParentName(classQualifiedName);
            value.setSorted(String.valueOf(sorted++));
            value.setRelationType(DocObjectTypeEnum.METHOD.getType());
            value.setMethodName(method.getNameAsString());
            value.setReturnType(method.getType().asString());
            value.setReturnTypeQualifiedName(method.getType().asString());
            value.setParameterTypes(paramTypes);
            value.setIsConstructor(false);
            value.setPrivate(method.isPrivate());

            // 解析废弃注解
            String[] deprecatedInfo = parseDeprecatedAnnotation(method.getAnnotations());
            if (deprecatedInfo != null) {
                value.setDeprecated(true);
                value.setDeprecatedReason(deprecatedInfo[1]);
            }

            Optional<JavadocComment> javadoc = method.getJavadocComment();
            if (javadoc.isPresent()) {
                value.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            }

            methods.add(value);
        }

        // 解析构造函数
        for (ConstructorDeclaration constructor : classDecl.getConstructors()) {
            MethodDocData value = new MethodDocData();
            String paramTypes = constructor.getParameters().stream()
                    .map(p -> p.getType().asString())
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            String methodId = classQualifiedName + "#" + simpleName + "(" + paramTypes + ")";
            value.setName(methodId);
            value.setParentName(classQualifiedName);
            value.setSorted(String.valueOf(sorted++));
            value.setRelationType(DocObjectTypeEnum.METHOD.getType());
            value.setMethodName(simpleName);
            value.setReturnType(simpleName);
            value.setReturnTypeQualifiedName(classQualifiedName);
            value.setParameterTypes(paramTypes);
            value.setIsConstructor(true);
            value.setPrivate(constructor.isPrivate());

            // 解析废弃注解
            String[] deprecatedInfo = parseDeprecatedAnnotation(constructor.getAnnotations());
            if (deprecatedInfo != null) {
                value.setDeprecated(true);
                value.setDeprecatedReason(deprecatedInfo[1]);
            }

            Optional<JavadocComment> javadoc = constructor.getJavadocComment();
            if (javadoc.isPresent()) {
                value.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            }

            methods.add(value);
        }

        return methods;
    }

    /**
     * 解析方法参数
     *
     * @return 参数变量值对象列表
     */
    public List<ParameterVariableData> parseMethodParameters(String sourceCode, String classQualifiedName, String methodName) {
        List<ParameterVariableData> parameters = new ArrayList<>();

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

        ValidationAnnotationParser validationParser = parseValidationAnnotations ? new ValidationAnnotationParser() : null;
        int order = 0;
        for (Parameter param : method.getParameters()) {
            String type = param.getType().asString();
            SharedVariableData sharedVar = getOrCreateSharedVariable(param.getNameAsString(), type);

            ParameterVariableData value = new ParameterVariableData();
            value.setName(methodId + "#" + param.getNameAsString());
            value.setParentName(methodId);
            value.setSorted(String.valueOf(order));
            value.setRelationType(DocObjectTypeEnum.PARAMETER_VARIABLE.getType());
            value.setParameterOrder(order);
            value.setSharedVariable(sharedVar);

            if (parseValidationAnnotations && validationParser != null) {
                List<String> validations = validationParser.parseParameterValidations(param);
                if (!validations.isEmpty()) {
                    value.setValidationAnnotations(validations);
                }
            }

            Optional<Comment> paramComment = param.getComment();
            if (paramComment.isPresent()) {
                value.setDocContent(cleanJavadocContent(paramComment.get().getContent()));
            }

            parameters.add(value);
            order++;
        }

        return parameters;
    }

    /**
     * 解析方法返回值
     *
     * @return 参数变量值对象（返回值）
     */
    public ParameterVariableData parseMethodReturnValue(String sourceCode, String classQualifiedName, String methodName) {
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

        ParameterVariableData value = new ParameterVariableData();
        value.setName(methodId + "#return");
        value.setParentName(methodId);
        value.setSorted("-1");
        value.setRelationType(DocObjectTypeEnum.PARAMETER_VARIABLE.getType());
        value.setParameterOrder(-1);
        value.setVariableName("return");
        value.setVariableType(type);
        value.setVariableTypeQualifiedName(type);
        value.setPrimitive(isPrimitiveType(type));

        Optional<JavadocComment> javadoc = method.getJavadocComment();
        if (javadoc.isPresent()) {
            String returnDoc = extractReturnDoc(javadoc.get().getContent());
            if (returnDoc != null) {
                value.setDocContent(returnDoc);
            }
        }

        return value;
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

    /**
     * 解析所有成员变量
     *
     * @return 成员变量值对象列表
     */
    public List<MemberVariableData> parseAllMemberVariables(String sourceCode, String classQualifiedName) {
        List<MemberVariableData> fields = new ArrayList<>();

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
        ValidationAnnotationParser validationParser = parseValidationAnnotations ? new ValidationAnnotationParser() : null;
        int sorted = 0;
        for (FieldDeclaration field : classDecl.getFields()) {
            String fieldName = field.getVariable(0).getNameAsString();
            String type = field.getCommonType().asString();

            SharedVariableData sharedVar = getOrCreateSharedVariable(fieldName, type);

            MemberVariableData value = new MemberVariableData();
            value.setName(classQualifiedName + "#" + fieldName);
            value.setParentName(classQualifiedName);
            value.setSorted(String.valueOf(sorted++));
            value.setRelationType(DocObjectTypeEnum.MEMBER_VARIABLE.getType());
            value.setSharedVariable(sharedVar);
            value.setPrivate(field.isPrivate());

            // 解析废弃注解
            String[] deprecatedInfo = parseDeprecatedAnnotation(field.getAnnotations());
            if (deprecatedInfo != null) {
                value.setDeprecated(true);
                value.setDeprecatedReason(deprecatedInfo[1]);
            }

            if (parseValidationAnnotations && validationParser != null) {
                List<String> validations = validationParser.parseFieldValidations(field.getAnnotations());
                if (!validations.isEmpty()) {
                    value.setValidationAnnotations(validations);
                }
            }

            Optional<JavadocComment> javadoc = field.getJavadocComment();
            if (javadoc.isPresent()) {
                value.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            }

            fields.add(value);
        }

        return fields;
    }

    private SharedVariableData getOrCreateSharedVariable(String variableName, String variableType) {
        String key = variableType + "#" + variableName;
        return sharedVariableCache.computeIfAbsent(key, k -> {
            SharedVariableData value = new SharedVariableData();
            value.setName(key);
            value.setRelationType(DocObjectTypeEnum.SHARED_VARIABLE.getType());
            value.setVariableName(variableName);
            value.setVariableType(variableType);
            value.setVariableTypeQualifiedName(variableType);
            value.setPrimitive(isPrimitiveType(variableType));
            return value;
        });
    }

    public Map<String, SharedVariableData> getSharedVariableCache() {
        return sharedVariableCache;
    }

    /**
     * 解析内部类（返回 InnerClassValue）
     *
     * @return 内部类文档值对象列表
     */
    public List<InnerClassData> parseInnerClassValues(String sourceCode, String classQualifiedName) {
        List<InnerClassData> innerClasses = new ArrayList<>();

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
        int sorted = 0;

        // 解析内部类和接口
        for (ClassOrInterfaceDeclaration innerClass : classDecl.getMembers().stream()
                .filter(m -> m instanceof ClassOrInterfaceDeclaration)
                .map(m -> (ClassOrInterfaceDeclaration) m)
                .toList()) {

            InnerClassData value = new InnerClassData();
            String innerClassName = innerClass.getNameAsString();
            String innerQualifiedName = classQualifiedName + "$" + innerClassName;
            value.setName(innerQualifiedName);
            value.setParentName(classQualifiedName);
            value.setSorted(String.valueOf(sorted++));
            value.setRelationType(DocObjectTypeEnum.INNER_CLASS.getType());
            value.setInnerClassName(innerClassName);
            value.setModifiers(innerClass.getModifiers().toString());
            value.setType(innerClass.isInterface() ? "interface" : "class");

            // 解析废弃注解
            String[] deprecatedInfo = parseDeprecatedAnnotation(innerClass.getAnnotations());
            if (deprecatedInfo != null) {
                value.setDeprecated(true);
                value.setDeprecatedReason(deprecatedInfo[1]);
            }

            Optional<JavadocComment> javadoc = innerClass.getJavadocComment();
            if (javadoc.isPresent()) {
                value.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            } else {
                value.setDocContent("");
            }

            innerClasses.add(value);
        }

        // 解析内部枚举
        for (EnumDeclaration innerEnum : classDecl.getMembers().stream()
                .filter(m -> m instanceof EnumDeclaration)
                .map(m -> (EnumDeclaration) m)
                .toList()) {

            InnerClassData value = new InnerClassData();
            String innerClassName = innerEnum.getNameAsString();
            String innerQualifiedName = classQualifiedName + "$" + innerClassName;
            value.setName(innerQualifiedName);
            value.setParentName(classQualifiedName);
            value.setSorted(String.valueOf(sorted++));
            value.setRelationType(DocObjectTypeEnum.INNER_CLASS.getType());
            value.setInnerClassName(innerClassName);
            value.setModifiers(innerEnum.getModifiers().toString());
            value.setType("enum");

            // 解析废弃注解
            String[] deprecatedInfo = parseDeprecatedAnnotation(innerEnum.getAnnotations());
            if (deprecatedInfo != null) {
                value.setDeprecated(true);
                value.setDeprecatedReason(deprecatedInfo[1]);
            }

            Optional<JavadocComment> javadoc = innerEnum.getJavadocComment();
            if (javadoc.isPresent()) {
                value.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            } else {
                value.setDocContent("");
            }

            innerClasses.add(value);
        }

        return innerClasses;
    }

    private static boolean isPrimitiveType(String type) {
        return "byte".equals(type) || "short".equals(type) || "int".equals(type) || "long".equals(type)
                || "float".equals(type) || "double".equals(type) || "boolean".equals(type) || "char".equals(type);
    }

    /**
     * 解析 @Deprecated 注解
     *
     * @param annotations 注解列表
     * @return [是否废弃, 废弃说明]，如果没有废弃注解返回 null
     */
    private String[] parseDeprecatedAnnotation(List<AnnotationExpr> annotations) {
        for (AnnotationExpr annotation : annotations) {
            String annotationName = annotation.getNameAsString();
            if ("Deprecated".equals(annotationName) || "java.lang.Deprecated".equals(annotationName)) {
                String reason = "";
                if (annotation instanceof NormalAnnotationExpr normal) {
                    for (com.github.javaparser.ast.expr.MemberValuePair pair : normal.getPairs()) {
                        String name = pair.getNameAsString();
                        if ("since".equals(name) || "forRemoval".equals(name)) {
                            reason = reason.isEmpty() ? pair.getValue().toString() : reason + "," + pair.getValue().toString();
                        }
                    }
                }
                return new String[]{"true", reason.replaceAll("\"", "")};
            }
        }
        return null;
    }
}