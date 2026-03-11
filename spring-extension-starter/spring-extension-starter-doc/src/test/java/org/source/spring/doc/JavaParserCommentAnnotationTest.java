package org.source.spring.doc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.source.spring.doc.util.JavaParserUtil;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * JavaParser 测试类
 * 
 * <p>测试使用JavaParser提取注释和注解的功能</p>
 */
@SpringBootTest
public class JavaParserCommentAnnotationTest {

    private static final Logger logger = LoggerFactory.getLogger(JavaParserCommentAnnotationTest.class);

    /**
     * 测试解析UserEntity类
     * 
     * <p>验证能够正确提取JPA注解、字段注释等信息</p>
     */
    @Test
    public void testParseUserEntity() {
        JavaParserUtil parserUtil = new JavaParserUtil();
        String filePath = "src/main/java/org/source/spring/doc/entity/UserEntity.java";
        
        logger.info("=== 开始测试 UserEntity 解析 ===");
        parserUtil.parseAndPrintCommentsAndAnnotations(filePath);
        logger.info("=== UserEntity 解析测试完成 ===\n");
    }

    /**
     * 测试解析UserController类
     * 
     * <p>验证能够正确提取REST注解、方法注释等信息</p>
     */
    @Test
    public void testParseUserController() {
        JavaParserUtil parserUtil = new JavaParserUtil();
        String filePath = "src/main/java/org/source/spring/doc/controller/UserController.java";
        
        logger.info("=== 开始测试 UserController 解析 ===");
        parserUtil.parseAndPrintCommentsAndAnnotations(filePath);
        logger.info("=== UserController 解析测试完成 ===\n");
    }

    /**
     * 测试解析所有核心类
     * 
     * <p>批量测试多个类的注释和注解提取</p>
     */
    @Test
    public void testParseAllCoreClasses() {
        JavaParserUtil parserUtil = new JavaParserUtil();
        String[] coreFiles = {
            "src/main/java/org/source/spring/doc/entity/UserEntity.java",
            "src/main/java/org/source/spring/doc/entity/UserStatus.java",
            "src/main/java/org/source/spring/doc/dto/UserDto.java",
            "src/main/java/org/source/spring/doc/dto/UserVo.java",
            "src/main/java/org/source/spring/doc/controller/UserController.java",
            "src/main/java/org/source/spring/doc/service/UserService.java"
        };
        
        logger.info("=== 开始批量测试所有核心类 ===");
        for (String filePath : coreFiles) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(filePath)) {
                parserUtil.parseAndPrintCommentsAndAnnotations(filePath);
            }
        }
        logger.info("=== 批量测试完成 ===\n");
    }
}