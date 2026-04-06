package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.Comment;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.MemberVariableElement;
import org.source.spring.doc.domain.element.MethodDocElement;
import org.source.spring.doc.domain.element.ParameterVariableElement;
import org.source.spring.doc.domain.element.SharedVariableElement;

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

    public DocCommentParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    public ClassDocElement parseClassDoc(String sourceCode, String classQualifiedName) {
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return null;
        }

        CompilationUnit cu = result.getResult().get();
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
        for (MethodDeclaration method : classDecl.getMethods()) {
            MethodDocElement element = new MethodDocElement();
            element.setMethodName(method.getNameAsString());
            element.setReturnType(method.getType().asString());
            element.setReturnTypeQualifiedName(method.getType().asString());
            element.setClassQualifiedName(classQualifiedName);

            Optional<JavadocComment> javadoc = method.getJavadocComment();
            if (javadoc.isPresent()) {
                element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            }

            methods.add(element);
        }

        return methods;
    }

    public List<ParameterVariableElement> parseMethodParameters(String sourceCode, String classQualifiedName, String methodName) {
        List<ParameterVariableElement> parameters = new ArrayList<>();
        
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return parameters;
        }

        CompilationUnit cu = result.getResult().get();
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

    private static boolean isPrimitiveType(String type) {
        return "byte".equals(type) || "short".equals(type) || "int".equals(type) || "long".equals(type)
                || "float".equals(type) || "double".equals(type) || "boolean".equals(type) || "char".equals(type);
    }
}