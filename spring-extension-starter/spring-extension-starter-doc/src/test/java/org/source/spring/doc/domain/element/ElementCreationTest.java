package org.source.spring.doc.domain.element;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElementCreationTest {

    @Test
    void testClassDocElementCreation() {
        ClassDocElement element = new ClassDocElement();
        
        element.setClassName("UserEntity");
        element.setClassQualifiedName("org.source.spring.doc.domain.entity.UserEntity");
        element.setModifiers("public");
        element.setDocContent("用户实体类");
        element.setEntity(true);
        element.setTableName("users");
        
        assertEquals("UserEntity", element.getClassName());
        assertEquals("org.source.spring.doc.domain.entity.UserEntity", element.getClassQualifiedName());
        assertEquals("public", element.getModifiers());
        assertEquals("用户实体类", element.getDocContent());
        assertTrue(element.isEntity());
        assertEquals("users", element.getTableName());
        
        assertEquals("org.source.spring.doc.domain.entity.UserEntity", element.getId());
        assertNull(element.getParentId());
    }

    @Test
    void testMethodDocElementCreation() {
        MethodDocElement element = new MethodDocElement();
        
        element.setMethodName("findById");
        element.setReturnType("UserEntity");
        element.setReturnTypeQualifiedName("org.source.spring.doc.domain.entity.UserEntity");
        element.setDocContent("根据ID查询用户");
        element.setClassQualifiedName("org.example.UserService");
        
        assertEquals("findById", element.getMethodName());
        assertEquals("UserEntity", element.getReturnType());
        assertEquals("org.source.spring.doc.domain.entity.UserEntity", element.getReturnTypeQualifiedName());
        assertEquals("根据ID查询用户", element.getDocContent());
        
        assertNotNull(element.getId());
    }

    @Test
    void testFieldDocElementCreation() {
        FieldDocElement element = new FieldDocElement();
        
        element.setFieldName("username");
        element.setFieldType("String");
        element.setFieldTypeQualifiedName("java.lang.String");
        element.setDocContent("用户名");
        element.setColumnName("username");
        element.setPrimaryKey(false);
        element.setClassQualifiedName("org.example.UserEntity");
        
        assertEquals("username", element.getFieldName());
        assertEquals("String", element.getFieldType());
        assertEquals("java.lang.String", element.getFieldTypeQualifiedName());
        assertEquals("用户名", element.getDocContent());
        assertEquals("username", element.getColumnName());
        assertFalse(element.isPrimaryKey());
        
        assertNotNull(element.getId());
    }

    @Test
    void testAnnotationDocElementCreation() {
        AnnotationDocElement element = new AnnotationDocElement();
        
        element.setAnnotationName("Entity");
        element.setAnnotationMembers(java.util.Map.of("name", "users"));
        
        assertEquals("Entity", element.getAnnotationName());
        assertEquals("users", element.getAnnotationMembers().get("name"));
        
        assertEquals("Entity", element.getId());
    }

    @Test
    void testParamDocElementCreation() {
        ParamDocElement element = new ParamDocElement();
        
        element.setParamName("id");
        element.setParamType("Long");
        element.setParamTypeQualifiedName("java.lang.Long");
        element.setDocContent("用户ID");
        element.setMethodId("org.example.UserService#save");
        
        assertEquals("id", element.getParamName());
        assertEquals("Long", element.getParamType());
        assertEquals("java.lang.Long", element.getParamTypeQualifiedName());
        assertEquals("用户ID", element.getDocContent());
        
        assertEquals("org.example.UserService#save#id", element.getId());
    }

    @Test
    void testFieldDocElementWithPrimaryKey() {
        FieldDocElement element = new FieldDocElement();
        element.setFieldName("id");
        element.setFieldType("Long");
        element.setFieldTypeQualifiedName("java.lang.Long");
        element.setColumnName("id");
        element.setPrimaryKey(true);
        element.setClassQualifiedName("org.example.UserEntity");
        
        assertTrue(element.isPrimaryKey());
        assertEquals("id", element.getColumnName());
    }

    @Test
    void testRestDocElementCreation() {
        RestDocElement element = new RestDocElement();
        
        element.setHttpMethod("GET");
        element.setPath("/users/{id}");
        element.setClassPath("org.example.UserController");
        element.setReturnType("UserEntity");
        element.setDocContent("获取用户详情");
        
        assertEquals("GET", element.getHttpMethod());
        assertEquals("/users/{id}", element.getPath());
        assertEquals("org.example.UserController", element.getClassPath());
        assertEquals("UserEntity", element.getReturnType());
        assertEquals("获取用户详情", element.getDocContent());
        
        assertNotNull(element.getId());
    }
}