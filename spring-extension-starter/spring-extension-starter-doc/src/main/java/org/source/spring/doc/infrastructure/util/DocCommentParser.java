package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.JavadocComment;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.FieldDocElement;
import org.source.spring.doc.domain.element.MethodDocElement;
import org.source.spring.doc.domain.element.ParamDocElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
 *     <li>批量解析所有方法、字段</li>
 * </ul>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocCommentParser {

    /**
     * JavaParser 实例
     */
    private final JavaParser javaParser;

    /**
     * 构造函数 - 初始化 JavaParser
     */
    public DocCommentParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析类的 JavaDoc 注释
     *
     * @param sourceCode Java 源代码
     * @param classQualifiedName 类的全限定名
     * @return 类文档元素，如果解析失败则返回 null
     */
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

    /**
     * 清理 JavaDoc 注释内容
     * <p>
     * 移除注释中的 * 符号、@ 标签及其内容，
     * 只保留描述文本。
     * </p>
     *
     * @param content 原始注释内容
     * @return 清理后的注释内容
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

    /**
     * 解析方法的 JavaDoc 注释
     *
     * @param sourceCode Java 源代码
     * @param classQualifiedName 类的全限定名
     * @param methodName 方法名
     * @return 方法文档元素，如果解析失败则返回 null
     */
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

    /**
     * 解析字段的 JavaDoc 注释
     *
     * @param sourceCode Java 源代码
     * @param classQualifiedName 类的全限定名
     * @param fieldName 字段名
     * @return 字段文档元素，如果解析失败则返回 null
     */
    public FieldDocElement parseFieldDoc(String sourceCode, String classQualifiedName, String fieldName) {
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
                .filter(f -> f.getVariable(0).getNameAsString().equals(fieldName))
                .findFirst();
        
        if (fieldOpt.isEmpty()) {
            return null;
        }

        FieldDeclaration fieldDecl = fieldOpt.get();
        FieldDocElement element = new FieldDocElement();
        element.setFieldName(fieldName);
        element.setFieldType(fieldDecl.getCommonType().asString());
        element.setFieldTypeQualifiedName(fieldDecl.getCommonType().asString());
        element.setClassQualifiedName(classQualifiedName);

        Optional<JavadocComment> javadoc = fieldDecl.getJavadocComment();
        if (javadoc.isPresent()) {
            element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
        }

        return element;
    }

    /**
     * 解析类中所有方法的 JavaDoc 注释
     *
     * @param sourceCode Java 源代码
     * @param classQualifiedName 类的全限定名
     * @return 方法文档元素列表
     */
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

    /**
     * 解析类中所有字段的 JavaDoc 注释
     *
     * @param sourceCode Java 源代码
     * @param classQualifiedName 类的全限定名
     * @return 字段文档元素列表
     */
    public List<FieldDocElement> parseAllFields(String sourceCode, String classQualifiedName) {
        List<FieldDocElement> fields = new ArrayList<>();
        
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
            FieldDocElement element = new FieldDocElement();
            element.setFieldName(fieldName);
            element.setFieldType(field.getCommonType().asString());
            element.setFieldTypeQualifiedName(field.getCommonType().asString());
            element.setClassQualifiedName(classQualifiedName);

            Optional<JavadocComment> javadoc = field.getJavadocComment();
            if (javadoc.isPresent()) {
                element.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
            }

            fields.add(element);
        }

        return fields;
    }
}