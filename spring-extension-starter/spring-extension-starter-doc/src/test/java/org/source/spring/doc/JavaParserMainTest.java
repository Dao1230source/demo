package org.source.spring.doc;

import org.source.spring.doc.util.JavaParserUtil;

/**
 * 独立的JavaParser测试主类
 * 
 * <p>不依赖Spring Boot，直接运行JavaParser测试</p>
 */
public class JavaParserMainTest {

    public static void main(String[] args) {
        System.out.println("=== JavaParser 注释和注解提取测试 ===\n");
        
        JavaParserUtil parserUtil = new JavaParserUtil();
        
        // 测试UserEntity
        String userEntityPath = "src/main/java/org/source/spring/doc/entity/UserEntity.java";
        parserUtil.parseAndPrintCommentsAndAnnotations(userEntityPath);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // 测试UserController
        String userControllerPath = "src/main/java/org/source/spring/doc/controller/UserController.java";
        parserUtil.parseAndPrintCommentsAndAnnotations(userControllerPath);
        
        System.out.println("\n=== 测试完成 ===");
    }
}