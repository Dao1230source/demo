package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.source.spring.doc.domain.element.*;
import org.source.spring.doc.domain.tree.DocEnhanceTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DocTagParserTest {

    private DocTagParser parser;

    @BeforeEach
    void setUp() {
        parser = new DocTagParser();
    }

    @Test
    void testParseParamTag() {
        String docContent = "保存用户\n@param user 用户对象\n@return 结果";
        
        Map<String, String> params = parser.parseParamTags(docContent);
        
        assertEquals(1, params.size());
        assertEquals("用户对象", params.get("user"));
    }

    @Test
    void testParseReturnTag() {
        String docContent = "保存用户\n@return boolean";
        
        String returnValue = parser.parseReturnTag(docContent);
        
        assertEquals("boolean", returnValue);
    }

    @Test
    void testParseThrowsTag() {
        String docContent = "保存用户\n@throws Exception 保存失败";
        
        Map<String, String> throwsMap = parser.parseThrowsTags(docContent);
        
        assertEquals(1, throwsMap.size());
        assertEquals("保存失败", throwsMap.get("Exception"));
    }

    @Test
    void testParseSeeTag() {
        String docContent = "保存用户\n@see UserService\n@see UserRepository";
        
        List<String> seeList = parser.parseSeeTags(docContent);
        
        assertEquals(2, seeList.size());
        assertTrue(seeList.contains("UserService"));
    }

    @Test
    void testParseAuthorTag() {
        String docContent = "保存用户\n@author zhangsan";
        
        String author = parser.parseAuthorTag(docContent);
        
        assertEquals("zhangsan", author);
    }

    @Test
    void testParseVersionTag() {
        String docContent = "保存用户\n@version 1.0.0";
        
        String version = parser.parseVersionTag(docContent);
        
        assertEquals("1.0.0", version);
    }

    @Test
    void testParseSinceTag() {
        String docContent = "保存用户\n@since 1.0.0";
        
        String since = parser.parseSinceTag(docContent);
        
        assertEquals("1.0.0", since);
    }

    @Test
    void testParseAllTags() {
        String docContent = """
            保存用户
            @param user 用户对象
            @return boolean
            @throws Exception 保存失败
            @author zhangsan
            @version 1.0.0
            """;
        
        Map<String, Object> allTags = parser.parseAllTags(docContent);
        
        assertNotNull(allTags);
        assertTrue(allTags.containsKey("params"));
        assertTrue(allTags.containsKey("return"));
        assertTrue(allTags.containsKey("throws"));
        assertTrue(allTags.containsKey("author"));
        assertTrue(allTags.containsKey("version"));
    }

    @Test
    void testParseNullContent() {
        assertTrue(parser.parseParamTags(null).isEmpty());
        assertNull(parser.parseReturnTag(null));
        assertTrue(parser.parseThrowsTags(null).isEmpty());
    }

    @Test
    void testParseNoTags() {
        String docContent = "这是一个简单的描述";
        
        Map<String, String> params = parser.parseParamTags(docContent);
        String returnValue = parser.parseReturnTag(docContent);
        
        assertTrue(params.isEmpty());
        assertNull(returnValue);
    }
}