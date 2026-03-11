package org.source.spring.doc;

import org.source.spring.doc.util.JavaParserUtil;

/**
 * 验证JavaParser功能
 */
public class JavaParserValidationTest {
    public static void main(String[] args) {
        JavaParserUtil parserUtil = new JavaParserUtil();
        String testFilePath = "src/main/java/org/source/spring/doc/entity/UserEntity.java";
        parserUtil.parseAndPrintCommentsAndAnnotations(testFilePath);
    }
}