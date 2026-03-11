package org.source.spring.doc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.source.spring.doc.util.JavaParserUtil;

/**
 * 独立的JavaParser测试类（非Spring依赖）
 * 
 * <p>用于测试JavaParser功能，不依赖Spring Boot上下文</p>
 */
public class StandaloneJavaParserTest {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneJavaParserTest.class);

    public static void main(String[] args) {
        logger.info("=== 开始独立JavaParser测试 ===");
        
        JavaParserUtil parserUtil = new JavaParserUtil();
        String filePath = "src/main/java/org/source/spring/doc/entity/UserEntity.java";
        
        parserUtil.parseAndPrintCommentsAndAnnotations(filePath);
        
        logger.info("=== 独立JavaParser测试完成 ===");
    }
}