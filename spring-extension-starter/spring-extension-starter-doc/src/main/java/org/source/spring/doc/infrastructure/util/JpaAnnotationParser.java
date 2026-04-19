package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.JpaColumnVariableElement;
import org.source.spring.doc.domain.element.SharedVariableElement;

import java.util.Optional;

/**
 * JPA注解解析器，用于解析Java类和成员变量上的JPA注解信息。
 * 
 * <p>该类基于JavaParser实现，能够解析JPA规范中的@Entity、@Table、@Column、@Id等注解，
 * 提取实体类的表名、成员变量的列名、主键标识等元数据信息。支持javax.persistence和
 * jakarta.persistence两种命名空间。</p>
 *
 * @author source
 * @since 1.0.0
 */
public class JpaAnnotationParser {

    /**
     * JavaParser 实例
     */
    private final JavaParser javaParser;

    /**
     * 构造 JPA 注解解析器
     */
    public JpaAnnotationParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析类上的 JPA 注解
     * <p>
     * 解析 @Entity、@Table 注解，设置实体标识和表名
     * </p>
     *
     * @param sourceCode Java 源代码
     * @param classElement 类文档元素
     * @return 解析后的类文档元素
     */
    public ClassDocElement parseJpaAnnotations(String sourceCode, ClassDocElement classElement) {
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return classElement;
        }

        CompilationUnit cu = result.getResult().get();
        String simpleName = classElement.getClassName();

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return classElement;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();

        boolean isEntity = classDecl.getAnnotations().stream()
                .anyMatch(a -> "Entity".equals(a.getNameAsString()) || "jakarta.persistence.Entity".equals(a.getNameAsString())
                        || "javax.persistence.Entity".equals(a.getNameAsString()));
        classElement.setEntity(isEntity);

        if (isEntity) {
            Optional<AnnotationExpr> tableAnnotation = classDecl.getAnnotations().stream()
                    .filter(a -> "Table".equals(a.getNameAsString()) || "jakarta.persistence.Table".equals(a.getNameAsString())
                            || "javax.persistence.Table".equals(a.getNameAsString()))
                    .findFirst();

            if (tableAnnotation.isPresent()) {
                String tableName = extractAnnotationValue(tableAnnotation.get(), "name");
                if (tableName != null && !tableName.isEmpty()) {
                    classElement.setTableName(tableName);
                } else {
                    classElement.setTableName(simpleName.toLowerCase());
                }
            } else {
                classElement.setTableName(simpleName.toLowerCase());
            }
        }

        return classElement;
    }

    /**
     * 解析字段的 JPA 列信息
     * <p>
     * 解析 @Column、@Id 注解，提取列名和主键标识
     * </p>
     *
     * @param sourceCode Java 源代码
     * @param classQualifiedName 类的全限定名
     * @param variableName 变量名
     * @param sharedVariable 共用变量元素
     * @return JPA 列变量元素，如果字段不是 JPA 列则返回 null
     */
    public JpaColumnVariableElement parseJpaColumnVariable(String sourceCode, String classQualifiedName, 
                                                            String variableName, SharedVariableElement sharedVariable) {
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
        JpaColumnVariableElement element = new JpaColumnVariableElement();
        element.setClassQualifiedName(classQualifiedName);
        element.setSharedVariable(sharedVariable);

        Optional<AnnotationExpr> columnAnnotation = fieldDecl.getAnnotations().stream()
                .filter(a -> "Column".equals(a.getNameAsString()) || "jakarta.persistence.Column".equals(a.getNameAsString())
                        || "javax.persistence.Column".equals(a.getNameAsString()))
                .findFirst();

        if (columnAnnotation.isPresent()) {
            String columnName = extractAnnotationValue(columnAnnotation.get(), "name");
            if (columnName != null && !columnName.isEmpty()) {
                element.setColumnName(columnName);
            } else {
                element.setColumnName(variableName);
            }
        } else {
            element.setColumnName(variableName);
        }

        boolean isPrimaryKey = fieldDecl.getAnnotations().stream()
                .anyMatch(a -> "Id".equals(a.getNameAsString()) || "jakarta.persistence.Id".equals(a.getNameAsString())
                        || "javax.persistence.Id".equals(a.getNameAsString()));
        element.setPrimaryKey(isPrimaryKey);

        return element;
    }

    /**
     * 从注解中提取指定属性的值
     *
     * @param annotation 注解表达式
     * @param attributeName 属性名
     * @return 属性值，如果未找到则返回 null
     */
    private String extractAnnotationValue(AnnotationExpr annotation, String attributeName) {
        if (annotation == null) {
            return null;
        }

        if (!annotation.toString().contains("(")) {
            return null;
        }

        String annotationStr = annotation.toString();
        
        int attrIndex = annotationStr.indexOf(attributeName + " =");
        if (attrIndex == -1) {
            attrIndex = annotationStr.indexOf(attributeName + "=");
        }
        
        if (attrIndex == -1) {
            return null;
        }

        int valueStart = annotationStr.indexOf("\"", attrIndex);
        if (valueStart == -1) {
            return null;
        }

        int valueEnd = annotationStr.indexOf("\"", valueStart + 1);
        if (valueEnd == -1) {
            return null;
        }

        return annotationStr.substring(valueStart + 1, valueEnd);
    }
}