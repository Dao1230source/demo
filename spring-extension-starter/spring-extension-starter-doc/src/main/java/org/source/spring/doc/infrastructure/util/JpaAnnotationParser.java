package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.source.spring.doc.domain.value.ClassDocData;
import org.source.spring.doc.domain.value.JpaColumnVariableData;
import org.source.spring.doc.domain.value.SharedVariableData;
import org.source.spring.doc.domain.object.DocObjectTypeEnum;

import java.util.Optional;

/**
 * JPA注解解析器
 *
 * @author source
 * @since 1.0.0
 */
public class JpaAnnotationParser {

    private final JavaParser javaParser;

    public JpaAnnotationParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析类上的 JPA 注解
     *
     * @return 解析后的类文档值对象
     */
    public ClassDocData parseJpaAnnotations(String sourceCode, ClassDocData classValue) {
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return classValue;
        }

        CompilationUnit cu = result.getResult().get();
        String simpleName = classValue.getClassName();

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return classValue;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();

        boolean isEntity = classDecl.getAnnotations().stream()
                .anyMatch(a -> "Entity".equals(a.getNameAsString()) || "jakarta.persistence.Entity".equals(a.getNameAsString())
                        || "javax.persistence.Entity".equals(a.getNameAsString()));
        classValue.setEntity(isEntity);

        if (isEntity) {
            Optional<AnnotationExpr> tableAnnotation = classDecl.getAnnotations().stream()
                    .filter(a -> "Table".equals(a.getNameAsString()) || "jakarta.persistence.Table".equals(a.getNameAsString())
                            || "javax.persistence.Table".equals(a.getNameAsString()))
                    .findFirst();

            if (tableAnnotation.isPresent()) {
                String tableName = extractAnnotationValue(tableAnnotation.get(), "name");
                if (tableName != null && !tableName.isEmpty()) {
                    classValue.setTableName(tableName);
                } else {
                    classValue.setTableName(simpleName.toLowerCase());
                }
            } else {
                classValue.setTableName(simpleName.toLowerCase());
            }
        }

        return classValue;
    }

    /**
     * 解析字段的 JPA 列信息
     *
     * @return JPA 列变量值对象，如果字段不是 JPA 列则返回 null
     */
    public JpaColumnVariableData parseJpaColumnVariable(String sourceCode, String classQualifiedName,
                                                        String variableName, SharedVariableData sharedVariable, int sorted) {
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
        JpaColumnVariableData value = new JpaColumnVariableData();
        value.setName(classQualifiedName + "#" + variableName);
        value.setParentName(classQualifiedName);
        value.setSorted(String.valueOf(sorted));
        value.setRelationType(DocObjectTypeEnum.JPA_COLUMN_VARIABLE.getType());
        value.setSharedVariable(sharedVariable);

        Optional<AnnotationExpr> columnAnnotation = fieldDecl.getAnnotations().stream()
                .filter(a -> "Column".equals(a.getNameAsString()) || "jakarta.persistence.Column".equals(a.getNameAsString())
                        || "javax.persistence.Column".equals(a.getNameAsString()))
                .findFirst();

        if (columnAnnotation.isPresent()) {
            String columnName = extractAnnotationValue(columnAnnotation.get(), "name");
            if (columnName != null && !columnName.isEmpty()) {
                value.setColumnName(columnName);
            } else {
                value.setColumnName(variableName);
            }
        } else {
            value.setColumnName(variableName);
        }

        boolean isPrimaryKey = fieldDecl.getAnnotations().stream()
                .anyMatch(a -> "Id".equals(a.getNameAsString()) || "jakarta.persistence.Id".equals(a.getNameAsString())
                        || "javax.persistence.Id".equals(a.getNameAsString()));
        value.setPrimaryKey(isPrimaryKey);

        return value;
    }

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