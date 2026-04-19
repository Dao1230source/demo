package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Feign 客户端注解解析器
 * <p>
 * 解析 Spring Cloud OpenFeign 相关注解：
 * - @FeignClient - 声明式 HTTP 客户端
 * - @RequestMapping, @GetMapping, @PostMapping 等
 * - @RequestHeader - 请求头参数
 * - @SpringQueryMap - 查询对象参数
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class FeignAnnotationParser {

    private final JavaParser javaParser;

    public FeignAnnotationParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析 Feign 客户端接口
     *
     * @param sourceCode Java 源代码
     * @param classQualifiedName 类全限定名
     * @return Feign 客户端信息 Map
     */
    public Map<String, Object> parseFeignClient(String sourceCode, String classQualifiedName) {
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
            if ("FeignClient".equals(annotationName) || 
                "org.springframework.cloud.openfeign.FeignClient".equals(annotationName)) {
                result.put("feignClient", extractAnnotationAttributes(annotation));
                result.put("className", classQualifiedName);
            }
        }

        if (!result.isEmpty()) {
            List<Map<String, Object>> methods = parseFeignMethods(classDecl);
            result.put("methods", methods);
        }

        return result;
    }

    /**
     * 解析 Feign 接口中的方法
     *
     * @param classDecl 类声明
     * @return 方法列表
     */
    private List<Map<String, Object>> parseFeignMethods(ClassOrInterfaceDeclaration classDecl) {
        List<Map<String, Object>> methods = new ArrayList<>();
        
        for (MethodDeclaration method : classDecl.getMethods()) {
            Map<String, Object> methodInfo = new HashMap<>();
            methodInfo.put("methodName", method.getNameAsString());
            methodInfo.put("returnType", method.getType().asString());
            
            Map<String, Object> requestInfo = new HashMap<>();
            for (AnnotationExpr annotation : method.getAnnotations()) {
                String name = annotation.getNameAsString();
                if (isRequestMappingAnnotation(name)) {
                    requestInfo.put("httpMethod", getHttpMethod(name));
                    requestInfo.put("path", extractPath(annotation));
                }
            }
            
            if (!requestInfo.isEmpty()) {
                methodInfo.put("request", requestInfo);
                methods.add(methodInfo);
            }
        }
        
        return methods;
    }

    /**
     * 判断是否为请求映射注解
     */
    private boolean isRequestMappingAnnotation(String name) {
        return "RequestMapping".equals(name) ||
               "GetMapping".equals(name) ||
               "PostMapping".equals(name) ||
               "PutMapping".equals(name) ||
               "DeleteMapping".equals(name) ||
               "PatchMapping".equals(name);
    }

    /**
     * 获取 HTTP 方法
     */
    private String getHttpMethod(String annotationName) {
        return switch (annotationName) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "DeleteMapping" -> "DELETE";
            case "PatchMapping" -> "PATCH";
            case "RequestMapping" -> "REQUEST";
            default -> "UNKNOWN";
        };
    }

    /**
     * 提取路径
     */
    private String extractPath(AnnotationExpr annotation) {
        if (annotation instanceof SingleMemberAnnotationExpr single) {
            return single.getMemberValue().toString().replace("\"", "");
        }
        if (annotation instanceof NormalAnnotationExpr normal) {
            for (var pair : normal.getPairs()) {
                if ("value".equals(pair.getNameAsString()) || "path".equals(pair.getNameAsString())) {
                    return pair.getValue().toString().replace("\"", "");
                }
            }
        }
        return "";
    }

    /**
     * 提取注解属性
     */
    private Map<String, String> extractAnnotationAttributes(AnnotationExpr annotation) {
        Map<String, String> attrs = new HashMap<>();
        
        if (annotation instanceof SingleMemberAnnotationExpr single) {
            attrs.put("value", single.getMemberValue().toString().replace("\"", ""));
        }
        if (annotation instanceof NormalAnnotationExpr normal) {
            for (var pair : normal.getPairs()) {
                attrs.put(pair.getNameAsString(), pair.getValue().toString().replace("\"", ""));
            }
        }
        
        return attrs;
    }
}