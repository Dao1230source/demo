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
 * MyBatis 注解解析器
 * <p>
 * 解析 MyBatis 相关注解：
 * - @Select, @Insert, @Update, @Delete - 注解式 SQL
 * - @Results, @Result - 结果映射
 * - @Param - 参数命名
 * - @Mapper - Mapper 接口标记
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class MyBatisAnnotationParser {

    private final JavaParser javaParser;

    public MyBatisAnnotationParser() {
        ParserConfiguration config = new ParserConfiguration()
                .setSymbolResolver(null);
        this.javaParser = new JavaParser(config);
    }

    /**
     * 解析 MyBatis Mapper 接口
     *
     * @param sourceCode Java 源代码
     * @param classQualifiedName 类全限定名
     * @return MyBatis Mapper 信息
     */
    public Map<String, Object> parseMyBatisMapper(String sourceCode, String classQualifiedName) {
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
        
        boolean isMapper = classDecl.getAnnotations().stream()
                .anyMatch(a -> "Mapper".equals(a.getNameAsString()) ||
                              "org.apache.ibatis.annotations.Mapper".equals(a.getNameAsString()));
        
        if (isMapper) {
            result.put("mapperName", simpleName);
            result.put("className", classQualifiedName);
            result.put("isMapper", true);
            
            List<Map<String, Object>> sqlMethods = parseSqlMethods(classDecl);
            result.put("sqlMethods", sqlMethods);
        }

        return result;
    }

    /**
     * 解析 SQL 注解方法
     *
     * @param classDecl 类声明
     * @return SQL 方法列表
     */
    private List<Map<String, Object>> parseSqlMethods(ClassOrInterfaceDeclaration classDecl) {
        List<Map<String, Object>> methods = new ArrayList<>();
        
        for (MethodDeclaration method : classDecl.getMethods()) {
            for (AnnotationExpr annotation : method.getAnnotations()) {
                String name = annotation.getNameAsString();
                if (isSqlAnnotation(name)) {
                    Map<String, Object> methodInfo = new HashMap<>();
                    methodInfo.put("methodName", method.getNameAsString());
                    methodInfo.put("returnType", method.getType().asString());
                    methodInfo.put("sqlType", getSqlType(name));
                    methodInfo.put("sql", extractSql(annotation));
                    
                    List<String> params = extractParams(method);
                    methodInfo.put("params", params);
                    
                    methods.add(methodInfo);
                }
            }
        }
        
        return methods;
    }

    /**
     * 判断是否为 SQL 注解
     */
    private boolean isSqlAnnotation(String name) {
        return "Select".equals(name) || "Insert".equals(name) ||
               "Update".equals(name) || "Delete".equals(name) ||
               "org.apache.ibatis.annotations.Select".equals(name) ||
               "org.apache.ibatis.annotations.Insert".equals(name) ||
               "org.apache.ibatis.annotations.Update".equals(name) ||
               "org.apache.ibatis.annotations.Delete".equals(name);
    }

    /**
     * 获取 SQL 类型
     */
    private String getSqlType(String annotationName) {
        String simpleName = annotationName.contains(".") 
            ? annotationName.substring(annotationName.lastIndexOf('.') + 1) 
            : annotationName;
        return simpleName.toUpperCase();
    }

    /**
     * 提取 SQL 语句
     */
    private String extractSql(AnnotationExpr annotation) {
        if (annotation instanceof SingleMemberAnnotationExpr single) {
            return single.getMemberValue().toString()
                    .replace("\"", "")
                    .replace("\n", " ")
                    .trim();
        }
        if (annotation instanceof NormalAnnotationExpr normal) {
            for (var pair : normal.getPairs()) {
                if ("value".equals(pair.getNameAsString())) {
                    return pair.getValue().toString()
                            .replace("\"", "")
                            .replace("\n", " ")
                            .trim();
                }
            }
        }
        return "";
    }

    /**
     * 提取参数列表
     */
    private List<String> extractParams(MethodDeclaration method) {
        List<String> params = new ArrayList<>();
        
        for (var param : method.getParameters()) {
            String paramName = param.getNameAsString();
            String paramType = param.getType().asString();
            
            boolean hasParamAnnotation = param.getAnnotations().stream()
                    .anyMatch(a -> "Param".equals(a.getNameAsString()) ||
                                  "org.apache.ibatis.annotations.Param".equals(a.getNameAsString()));
            
            Map<String, String> paramInfo = new HashMap<>();
            paramInfo.put("name", paramName);
            paramInfo.put("type", paramType);
            paramInfo.put("hasParamAnnotation", String.valueOf(hasParamAnnotation));
            
            params.add(paramInfo.toString());
        }
        
        return params;
    }
}