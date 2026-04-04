package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.FieldDocElement;
import org.source.spring.doc.domain.element.MethodDocElement;
import org.source.spring.doc.domain.element.RestDocElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST注解解析器，用于解析Spring MVC的REST接口注解信息。
 * 
 * <p>该类基于JavaParser实现，能够解析Spring MVC中的@RestController、@RequestMapping、
 * @GetMapping、@PostMapping等注解，提取REST接口的HTTP方法、请求路径、路径变量、
 * 请求参数、请求体等信息。</p>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>解析Spring MVC控制器类，提取REST接口信息</li>
 *   <li>生成API文档时获取接口定义信息</li>
 *   <li>接口测试工具中自动发现接口定义</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * RestAnnotationParser parser = new RestAnnotationParser();
 * List<RestDocElement> endpoints = parser.parseRestEndpoints(sourceCode, "com.example.UserController");
 * }</pre>
 * 
 * @author source
 * @since 1.0.0
 */
public class RestAnnotationParser {

    /**
     * JavaParser实例，用于解析Java源代码。
     */
    private final JavaParser javaParser;

    /**
     * 构造REST注解解析器实例。
     * 
     * <p>初始化JavaParser配置，不启用符号解析器以提高解析速度。</p>
     */
    public RestAnnotationParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析源代码中的REST接口端点信息。
     * 
     * <p>扫描指定类的所有方法，识别Spring MVC的请求映射注解（@GetMapping、@PostMapping等），
     * 提取完整的请求路径、HTTP方法、参数信息等。</p>
     * 
     * @param sourceCode Java源代码内容
     * @param classQualifiedName 类的全限定名
     * @return REST接口端点列表，如果没有找到REST接口则返回空列表
     */
    public List<RestDocElement> parseRestEndpoints(String sourceCode, String classQualifiedName) {
        List<RestDocElement> endpoints = new ArrayList<>();
        
        ParseResult<CompilationUnit> result = javaParser.parse(sourceCode);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            return endpoints;
        }

        CompilationUnit cu = result.getResult().get();
        String simpleName = classQualifiedName.substring(classQualifiedName.lastIndexOf('.') + 1);

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(simpleName);
        if (classOpt.isEmpty()) {
            return endpoints;
        }

        ClassOrInterfaceDeclaration classDecl = classOpt.get();
        
        String classPath = "";
        String classBasePath = "";
        
        for (AnnotationExpr annotation : classDecl.getAnnotations()) {
            if (isRequestMapping(annotation)) {
                classBasePath = extractPath(annotation);
                classPath = classQualifiedName;
                break;
            }
        }

        for (MethodDeclaration method : classDecl.getMethods()) {
            RestDocElement endpoint = parseMethodEndpoint(method, classPath, classBasePath);
            if (endpoint != null) {
                endpoints.add(endpoint);
            }
        }

        return endpoints;
    }

    /**
     * 解析单个方法的REST接口信息。
     * 
     * <p>检查方法上的请求映射注解，提取HTTP方法、路径、参数、返回值等信息。
     * 同时提取方法的JavaDoc注释内容。</p>
     * 
     * @param method 方法声明对象
     * @param classPath 类的全限定路径
     * @param classBasePath 类级别的请求路径前缀
     * @return REST接口端点信息，如果方法没有请求映射注解则返回null
     */
    private RestDocElement parseMethodEndpoint(MethodDeclaration method, String classPath, String classBasePath) {
        for (AnnotationExpr annotation : method.getAnnotations()) {
            String httpMethod = getHttpMethod(annotation);
            if (httpMethod != null) {
                RestDocElement endpoint = new RestDocElement();
                endpoint.setHttpMethod(httpMethod);
                endpoint.setPath(classBasePath + extractPath(annotation));
                endpoint.setClassPath(classPath);
                endpoint.setReturnType(method.getType().asString());
                
                Optional<com.github.javaparser.ast.comments.JavadocComment> javadoc = method.getJavadocComment();
                if (javadoc.isPresent()) {
                    endpoint.setDocContent(cleanJavadocContent(javadoc.get().getContent()));
                }

                endpoint.setPathVariables(extractPathVariables(method));
                endpoint.setRequestParams(extractRequestParams(method));
                endpoint.setRequestBody(extractRequestBody(method));

                return endpoint;
            }
        }
        return null;
    }

    /**
     * 根据注解名称获取HTTP方法类型。
     * 
     * @param annotation 注解表达式对象
     * @return HTTP方法名称（GET、POST、PUT、DELETE、PATCH），如果不是请求映射注解则返回null
     */
    private String getHttpMethod(AnnotationExpr annotation) {
        String name = annotation.getNameAsString();
        return switch (name) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "DeleteMapping" -> "DELETE";
            case "PatchMapping" -> "PATCH";
            default -> null;
        };
    }

    /**
     * 判断注解是否为@RequestMapping注解。
     * 
     * @param annotation 注解表达式对象
     * @return 如果是@RequestMapping注解返回true，否则返回false
     */
    private boolean isRequestMapping(AnnotationExpr annotation) {
        String name = annotation.getNameAsString();
        return "RequestMapping".equals(name);
    }

    /**
     * 从请求映射注解中提取路径值。
     * 
     * <p>支持两种注解格式：
     * <ul>
     *   <li>@GetMapping("/path") - 单值注解形式</li>
     *   <li>@GetMapping(value = "/path") 或 @GetMapping(path = "/path") - 正常注解形式</li>
     * </ul>
     * </p>
     * 
     * @param annotation 注解表达式对象
     * @return 请求路径字符串，如果没有指定路径则返回空字符串
     */
    private String extractPath(AnnotationExpr annotation) {
        if (annotation instanceof com.github.javaparser.ast.expr.NormalAnnotationExpr normal) {
            for (com.github.javaparser.ast.expr.MemberValuePair pair : normal.getPairs()) {
                if ("value".equals(pair.getNameAsString()) || "path".equals(pair.getNameAsString())) {
                    return extractStringValue(pair.getValue());
                }
            }
        } else if (annotation instanceof com.github.javaparser.ast.expr.SingleMemberAnnotationExpr single) {
            return extractStringValue(single.getMemberValue());
        }
        return "";
    }

    /**
     * 从表达式中提取字符串字面值。
     * 
     * @param value 表达式对象
     * @return 字符串字面值，如果不是字符串字面量则返回空字符串
     */
    private String extractStringValue(com.github.javaparser.ast.expr.Expression value) {
        if (value instanceof com.github.javaparser.ast.expr.StringLiteralExpr stringLiteral) {
            return stringLiteral.getValue();
        }
        return "";
    }

    /**
     * 提取方法参数中标注了@PathVariable注解的路径变量。
     * 
     * @param method 方法声明对象
     * @return 路径变量名称数组
     */
    private String[] extractPathVariables(MethodDeclaration method) {
        List<String> variables = new ArrayList<>();
        for (com.github.javaparser.ast.body.Parameter param : method.getParameters()) {
            for (AnnotationExpr annotation : param.getAnnotations()) {
                if ("PathVariable".equals(annotation.getNameAsString())) {
                    variables.add(param.getNameAsString());
                }
            }
        }
        return variables.toArray(new String[0]);
    }

    /**
     * 提取方法参数中标注了@RequestParam注解的请求参数。
     * 
     * @param method 方法声明对象
     * @return 请求参数名称数组
     */
    private String[] extractRequestParams(MethodDeclaration method) {
        List<String> params = new ArrayList<>();
        for (com.github.javaparser.ast.body.Parameter param : method.getParameters()) {
            for (AnnotationExpr annotation : param.getAnnotations()) {
                if ("RequestParam".equals(annotation.getNameAsString())) {
                    params.add(param.getNameAsString());
                }
            }
        }
        return params.toArray(new String[0]);
    }

    /**
     * 提取方法参数中标注了@RequestBody注解的请求体类型。
     * 
     * @param method 方法声明对象
     * @return 请求体的类型名称，如果没有@RequestBody参数则返回null
     */
    private String extractRequestBody(MethodDeclaration method) {
        for (com.github.javaparser.ast.body.Parameter param : method.getParameters()) {
            for (AnnotationExpr annotation : param.getAnnotations()) {
                if ("RequestBody".equals(annotation.getNameAsString())) {
                    return param.getType().asString();
                }
            }
        }
        return null;
    }

    /**
     * 清理JavaDoc注释内容，移除格式标记和标签部分。
     * 
     * <p>处理JavaDoc注释，移除行首的星号标记，提取纯文本描述部分，
     * 在遇到第一个标签（以@开头）时停止提取。</p>
     * 
     * @param content 原始JavaDoc注释内容
     * @return 清理后的纯文本描述，如果内容为空则原样返回
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