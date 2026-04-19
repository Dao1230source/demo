package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

import java.util.ArrayList;
import java.util.List;

/**
 * 验证注解解析器
 * <p>
 * 解析 JSR-303/JSR-380 验证注解，包括：
 * - @Valid, @Validated
 * - @NotNull, @NotBlank, @NotEmpty
 * - @Size, @Min, @Max
 * - @Pattern, @Email
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class ValidationAnnotationParser {

    /**
     * 解析参数的验证注解
     *
     * @param parameter 方法参数
     * @return 验证注解列表
     */
    public List<String> parseParameterValidations(Parameter parameter) {
        List<String> validations = new ArrayList<>();
        
        for (AnnotationExpr annotation : parameter.getAnnotations()) {
            String annotationName = annotation.getNameAsString();
            
            if (isValidationAnnotation(annotationName)) {
                String validation = parseValidationAnnotation(annotation);
                if (validation != null) {
                    validations.add(validation);
                }
            }
        }
        
        return validations;
    }

    /**
     * 解析字段的验证注解
     *
     * @param annotations 字段的注解列表
     * @return 验证注解列表
     */
    public List<String> parseFieldValidations(List<AnnotationExpr> annotations) {
        List<String> validations = new ArrayList<>();
        
        for (AnnotationExpr annotation : annotations) {
            String annotationName = annotation.getNameAsString();
            
            if (isValidationAnnotation(annotationName)) {
                String validation = parseValidationAnnotation(annotation);
                if (validation != null) {
                    validations.add(validation);
                }
            }
        }
        
        return validations;
    }

    /**
     * 判断是否为验证注解
     *
     * @param annotationName 注解名称
     * @return true 如果是验证注解
     */
    private boolean isValidationAnnotation(String annotationName) {
        return annotationName.equals("Valid") ||
               annotationName.equals("javax.validation.Valid") ||
               annotationName.equals("Validated") ||
               annotationName.equals("org.springframework.validation.annotation.Validated") ||
               annotationName.equals("NotNull") ||
               annotationName.equals("javax.validation.constraints.NotNull") ||
               annotationName.equals("NotBlank") ||
               annotationName.equals("javax.validation.constraints.NotBlank") ||
               annotationName.equals("NotEmpty") ||
               annotationName.equals("javax.validation.constraints.NotEmpty") ||
               annotationName.equals("Size") ||
               annotationName.equals("javax.validation.constraints.Size") ||
               annotationName.equals("Min") ||
               annotationName.equals("javax.validation.constraints.Min") ||
               annotationName.equals("Max") ||
               annotationName.equals("javax.validation.constraints.Max") ||
               annotationName.equals("Pattern") ||
               annotationName.equals("javax.validation.constraints.Pattern") ||
               annotationName.equals("Email") ||
               annotationName.equals("javax.validation.constraints.Email");
    }

    /**
     * 解析验证注解
     *
     * @param annotation 注解表达式
     * @return 验证注解字符串表示
     */
    private String parseValidationAnnotation(AnnotationExpr annotation) {
        String name = annotation.getNameAsString();
        
        if (annotation instanceof MarkerAnnotationExpr) {
            return name;
        }
        
        if (annotation instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr single = (SingleMemberAnnotationExpr) annotation;
            return name + "(" + single.getMemberValue().toString().replaceAll("\"", "") + ")";
        }
        
        if (annotation instanceof NormalAnnotationExpr) {
            NormalAnnotationExpr normal = (NormalAnnotationExpr) annotation;
            StringBuilder sb = new StringBuilder();
            sb.append(name).append("(");
            
            boolean first = true;
            for (var pair : normal.getPairs()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(pair.getNameAsString())
                  .append("=")
                  .append(pair.getValue().toString().replaceAll("\"", ""));
                first = false;
            }
            
            sb.append(")");
            return sb.toString();
        }
        
        return name;
    }
}
