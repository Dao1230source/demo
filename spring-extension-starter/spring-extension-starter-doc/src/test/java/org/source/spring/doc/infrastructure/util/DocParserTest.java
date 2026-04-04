package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.doc.domain.element.*;
import org.source.spring.doc.domain.tree.DocEnhanceTree;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocParser测试 - 使用src/main/java下的实际代码
 */
public class DocParserTest {

    private DocParser parser;
    private static final String SOURCE_DIR = "/Users/zengfugen/IdeaProjects/dao1230.source/demo/spring-extension-starter/spring-extension-starter-doc";

    @BeforeEach
    void setUp() {
        parser = new DocParser();
    }

    @Test
    void testParseUserEntity() throws IOException {
        DocEnhanceTree tree = parser.parseDirectory(SOURCE_DIR);
        
        List<ClassDocElement> classes = tree.getClasses();
        assertTrue(classes.size() > 0);
        
        ClassDocElement userEntity = classes.stream()
                .filter(c -> "UserEntity".equals(c.getClassName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(userEntity);
        assertEquals("org.source.spring.doc.domain.entity.UserEntity", userEntity.getClassQualifiedName());
        assertTrue(userEntity.isEntity());
    }

    @Test
    void testParseUserService() throws IOException {
        DocEnhanceTree tree = parser.parseDirectory(SOURCE_DIR);
        
        List<MethodDocElement> methods = tree.getMethodsOfClass("org.source.spring.doc.domain.service.UserService");
        
        assertNotNull(methods);
    }

    @Test
    void testParseUserEntityFields() throws IOException {
        DocEnhanceTree tree = parser.getDocTree();
        parser.parseDirectory(SOURCE_DIR);
        
        List<FieldDocElement> fields = tree.getFieldsOfClass("org.source.spring.doc.domain.entity.UserEntity");
        
        assertTrue(fields.size() > 0);
        
        FieldDocElement idField = fields.stream()
                .filter(f -> "id".equals(f.getFieldName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(idField);
        assertTrue(idField.isPrimaryKey());
    }

    @Test
    void testParseAllClasses() throws IOException {
        DocEnhanceTree tree = parser.parseDirectory(SOURCE_DIR);
        
        List<ClassDocElement> classes = tree.getClasses();
        
        assertTrue(classes.size() > 0);
        
        String[] expectedClasses = {"UserEntity", "UserService", "UserRepository", 
                "UserController", "UserApp", "UserFacade"};
        
        for (String className : expectedClasses) {
            boolean found = classes.stream().anyMatch(c -> c.getClassName().equals(className));
            assertTrue(found, "Class " + className + " should be found");
        }
    }

    @Test
    void testParseRestController() throws IOException {
        DocEnhanceTree tree = parser.parseDirectory(SOURCE_DIR);
        
        List<RestDocElement> endpoints = tree.getRestEndpoints();
        
        assertNotNull(endpoints);
    }

    @Test
    void testUserEntityJavadoc() throws IOException {
        DocEnhanceTree tree = parser.parseDirectory(SOURCE_DIR);
        
        List<ClassDocElement> classes = tree.getClasses();
        ClassDocElement userEntity = classes.stream()
                .filter(c -> "UserEntity".equals(c.getClassName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(userEntity);
        assertNotNull(userEntity.getDocContent());
        assertTrue(userEntity.getDocContent().contains("用户实体类"));
    }

    @Test
    void testDocTreeNotEmpty() throws IOException {
        DocEnhanceTree tree = parser.parseDirectory(SOURCE_DIR);
        
        assertTrue(tree.size() > 0);
    }

    @Test
    void testGetParserInstances() {
        assertNotNull(parser.getCommentParser());
        assertNotNull(parser.getRestParser());
        assertNotNull(parser.getTagParser());
        assertNotNull(parser.getDocTree());
    }

    @Test
    void testParseWithExistingTree() throws IOException {
        DocEnhanceTree existingTree = new DocEnhanceTree();
        DocParser parserWithTree = new DocParser(existingTree);
        
        parserWithTree.parseDirectory(SOURCE_DIR);
        
        assertTrue(existingTree.size() > 0);
    }
}