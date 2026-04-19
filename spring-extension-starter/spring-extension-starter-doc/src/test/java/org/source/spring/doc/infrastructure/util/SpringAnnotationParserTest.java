package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringAnnotationParser 测试
 * <p>
 * 测试 Spring 核心注解解析功能
 * </p>
 *
 * @author test-author
 * @since 1.0.0
 */
class SpringAnnotationParserTest {

    private SpringAnnotationParser parser;
    private String testServiceSource;

    @BeforeEach
    void setUp() throws IOException {
        parser = new SpringAnnotationParser();
        Path fixtureDir = Path.of("src/test/resources/fixtures");
        testServiceSource = Files.readString(fixtureDir.resolve("TestService.java"));
    }

    @Test
    @DisplayName("解析组件注解 @Service")
    void testParseServiceAnnotation() {
        Map<String, Object> result = parser.parseComponentAnnotations(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(result);
        assertTrue(result.containsKey("Service"));
    }

    @Test
    @DisplayName("解析行为注解 @Transactional")
    void testParseTransactionalAnnotation() {
        List<Map<String, Object>> result = parser.parseBehaviorAnnotations(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        boolean hasTransactional = result.stream()
                .anyMatch(m -> {
                    Map<String, Map<String, String>> annotations = (Map<String, Map<String, String>>) m.get("annotations");
                    return annotations != null && annotations.containsKey("Transactional");
                });
        assertTrue(hasTransactional);
    }

    @Test
    @DisplayName("解析行为注解 @Scheduled")
    void testParseScheduledAnnotation() {
        List<Map<String, Object>> result = parser.parseBehaviorAnnotations(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(result);
        
        Map<String, Object> scheduledMethod = result.stream()
                .filter(m -> "scheduledTask".equals(m.get("methodName")))
                .findFirst()
                .orElse(null);
        
        assertNotNull(scheduledMethod);
        Map<String, Map<String, String>> annotations = (Map<String, Map<String, String>>) scheduledMethod.get("annotations");
        assertNotNull(annotations);
        assertTrue(annotations.containsKey("Scheduled"));
        
        Map<String, String> scheduledAttrs = annotations.get("Scheduled");
        assertTrue(scheduledAttrs.containsKey("cron"));
    }

    @Test
    @DisplayName("解析空类返回空 Map")
    void testParseNonExistingClass() {
        Map<String, Object> result = parser.parseComponentAnnotations(testServiceSource, 
                "org.source.spring.doc.test.fixture.NonExisting");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("解析行为注解返回空列表")
    void testParseBehaviorNonExistingClass() {
        List<Map<String, Object>> result = parser.parseBehaviorAnnotations(testServiceSource, 
                "org.source.spring.doc.test.fixture.NonExisting");
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("解析条件注解（当前 fixture 无条件注解）")
    void testParseConditionalAnnotations() {
        Map<String, Object> result = parser.parseConditionalAnnotations(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(result);
    }
}