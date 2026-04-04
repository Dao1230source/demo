package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.FieldDocElement;

import java.util.List;
import java.util.Optional;

/**
 * JPA注解解析器，用于解析Java类和字段上的JPA注解信息。
 * 
 * <p>该类基于JavaParser实现，能够解析JPA规范中的@Entity、@Table、@Column、@Id等注解，
 * 提取实体类的表名、字段的列名、主键标识等元数据信息。支持javax.persistence和
 * jakarta.persistence两种命名空间。</p>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>解析JPA实体类，提取数据库表映射信息</li>
 *   <li>生成数据库文档时获取表名和列名信息</li>
 *   <li>代码分析工具中提取实体元数据</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * JpaAnnotationParser parser = new JpaAnnotationParser();
 * ClassDocElement classElement = new ClassDocElement();
 * classElement.setClassName("User");
 * parser.parseJpaAnnotations(sourceCode, classElement);
 * }</pre>
 * 
 * @author source
 * @since 1.0.0
 */
public class JpaAnnotationParser {

    /**
     * JavaParser实例，用于解析Java源代码。
     */
    private final JavaParser javaParser;

    /**
     * 构造JPA注解解析器实例。
     * 
     * <p>初始化JavaParser配置，不启用符号解析器以提高解析速度。</p>
     */
    public JpaAnnotationParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析类的JPA注解信息。
     * 
     * <p>解析类上的@Entity和@Table注解，提取实体标识和表名信息。
     * 如果类标注了@Entity但没有@Table注解，则默认使用类名的小写形式作为表名。</p>
     * 
     * @param sourceCode Java源代码内容
     * @param classElement 类文档元素，包含类的基本信息，解析结果会填充到此对象中
     * @return 填充了JPA注解信息的类文档元素，如果解析失败则返回原始元素
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
     * 解析字段的JPA注解信息。
     * 
     * <p>解析字段上的@Column和@Id注解，提取列名和主键标识信息。
     * 如果字段没有@Column注解，则默认使用字段名作为列名。</p>
     * 
     * @param sourceCode Java源代码内容
     * @param classQualifiedName 类的全限定名
     * @param fieldElement 字段文档元素，包含字段的基本信息，解析结果会填充到此对象中
     * @return 填充了JPA注解信息的字段文档元素，如果解析失败则返回原始元素
     */
    public FieldDocElement parseJpaFieldAnnotations(String sourceCode, String classQualifiedName, FieldDocElement fieldElement) {
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return fieldElement;
        }

        CompilationUnit cu = result.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return fieldElement;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();

        Optional<FieldDeclaration> fieldOpt = classDecl.getFields().stream()
                .filter(f -> f.getVariable(0).getNameAsString().equals(fieldElement.getFieldName()))
                .findFirst();

        if (fieldOpt.isEmpty()) {
            return fieldElement;
        }

        FieldDeclaration fieldDecl = fieldOpt.get();

        Optional<AnnotationExpr> columnAnnotation = fieldDecl.getAnnotations().stream()
                .filter(a -> "Column".equals(a.getNameAsString()) || "jakarta.persistence.Column".equals(a.getNameAsString())
                        || "javax.persistence.Column".equals(a.getNameAsString()))
                .findFirst();

        if (columnAnnotation.isPresent()) {
            String columnName = extractAnnotationValue(columnAnnotation.get(), "name");
            if (columnName != null && !columnName.isEmpty()) {
                fieldElement.setColumnName(columnName);
            } else {
                fieldElement.setColumnName(fieldElement.getFieldName());
            }
        } else {
            fieldElement.setColumnName(fieldElement.getFieldName());
        }

        boolean isPrimaryKey = fieldDecl.getAnnotations().stream()
                .anyMatch(a -> "Id".equals(a.getNameAsString()) || "jakarta.persistence.Id".equals(a.getNameAsString())
                        || "javax.persistence.Id".equals(a.getNameAsString()));
        fieldElement.setPrimaryKey(isPrimaryKey);

        return fieldElement;
    }

    /**
     * 从注解表达式中提取指定属性的值。
     * 
     * <p>支持解析格式为@AnnotationName(attributeName = "value")的注解形式，
     * 返回属性的字符串值。</p>
     * 
     * @param annotation 注解表达式对象
     * @param attributeName 要提取的属性名称
     * @return 属性值的字符串形式，如果注解为null或未找到指定属性则返回null
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