package org.source.spring.doc.domain.element;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 元素创建测试
 * <p>
 * 测试各类文档元素的创建和基本功能
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
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
    void testSharedVariableElementCreation() {
        SharedVariableElement element = new SharedVariableElement();
        
        element.setVariableName("username");
        element.setVariableType("String");
        element.setVariableTypeQualifiedName("java.lang.String");
        element.setPrimitive(false);
        
        assertEquals("username", element.getVariableName());
        assertEquals("String", element.getVariableType());
        assertEquals("java.lang.String", element.getVariableTypeQualifiedName());
        assertFalse(element.isPrimitive());
        
        assertEquals("java.lang.String#username", element.getId());
        assertNull(element.getParentId());
    }

    @Test
    void testSharedVariableElementWithPrimitiveType() {
        SharedVariableElement element = new SharedVariableElement();
        
        element.setVariableName("age");
        element.setVariableType("int");
        element.setVariableTypeQualifiedName("int");
        element.setPrimitive(true);
        
        assertEquals("int", element.getVariableType());
        assertTrue(element.isPrimitive());
        assertEquals("int#age", element.getId());
    }

    @Test
    void testMemberVariableElementCreation() {
        SharedVariableElement sharedVar = new SharedVariableElement();
        sharedVar.setVariableName("username");
        sharedVar.setVariableType("String");
        sharedVar.setVariableTypeQualifiedName("java.lang.String");
        sharedVar.setPrimitive(false);
        
        MemberVariableElement element = new MemberVariableElement();
        element.setClassQualifiedName("org.example.UserEntity");
        element.setSharedVariable(sharedVar);
        element.setDocContent("用户名");
        
        assertEquals("username", element.getVariableName());
        assertEquals("String", element.getVariableType());
        assertEquals("java.lang.String", element.getVariableTypeQualifiedName());
        assertEquals("用户名", element.getDocContent());
        assertFalse(element.isPrimitive());
        
        assertNotNull(element.getId());
        assertEquals("org.example.UserEntity#username", element.getId());
        assertEquals("org.example.UserEntity", element.getParentId());
    }

    @Test
    void testMemberVariableElementWithPrimitiveType() {
        SharedVariableElement sharedVar = new SharedVariableElement();
        sharedVar.setVariableName("age");
        sharedVar.setVariableType("int");
        sharedVar.setVariableTypeQualifiedName("int");
        sharedVar.setPrimitive(true);
        
        MemberVariableElement element = new MemberVariableElement();
        element.setClassQualifiedName("org.example.UserEntity");
        element.setSharedVariable(sharedVar);
        
        assertEquals("int", element.getVariableType());
        assertTrue(element.isPrimitive());
    }

    @Test
    void testJpaColumnVariableElementCreation() {
        SharedVariableElement sharedVar = new SharedVariableElement();
        sharedVar.setVariableName("username");
        sharedVar.setVariableType("String");
        sharedVar.setVariableTypeQualifiedName("java.lang.String");
        sharedVar.setPrimitive(false);
        
        JpaColumnVariableElement element = new JpaColumnVariableElement();
        element.setClassQualifiedName("org.example.UserEntity");
        element.setSharedVariable(sharedVar);
        element.setDocContent("用户名");
        element.setColumnName("user_name");
        element.setPrimaryKey(false);
        
        assertEquals("username", element.getVariableName());
        assertEquals("String", element.getVariableType());
        assertEquals("user_name", element.getColumnName());
        assertFalse(element.isPrimaryKey());
        
        assertNotNull(element.getId());
        assertEquals("org.example.UserEntity#username", element.getId());
    }

    @Test
    void testJpaColumnVariableElementWithPrimaryKey() {
        SharedVariableElement sharedVar = new SharedVariableElement();
        sharedVar.setVariableName("id");
        sharedVar.setVariableType("Long");
        sharedVar.setVariableTypeQualifiedName("java.lang.Long");
        sharedVar.setPrimitive(false);
        
        JpaColumnVariableElement element = new JpaColumnVariableElement();
        element.setClassQualifiedName("org.example.UserEntity");
        element.setSharedVariable(sharedVar);
        element.setColumnName("id");
        element.setPrimaryKey(true);
        
        assertTrue(element.isPrimaryKey());
        assertEquals("id", element.getColumnName());
        assertEquals("org.example.UserEntity#id", element.getId());
    }

    @Test
    void testParameterVariableElementCreation() {
        SharedVariableElement sharedVar = new SharedVariableElement();
        sharedVar.setVariableName("id");
        sharedVar.setVariableType("Long");
        sharedVar.setVariableTypeQualifiedName("java.lang.Long");
        sharedVar.setPrimitive(false);
        
        ParameterVariableElement element = new ParameterVariableElement();
        element.setMethodId("org.example.UserService#save");
        element.setParameterOrder(0);
        element.setSharedVariable(sharedVar);
        element.setDocContent("用户ID");
        
        assertEquals("id", element.getVariableName());
        assertEquals("Long", element.getVariableType());
        assertEquals("java.lang.Long", element.getVariableTypeQualifiedName());
        assertEquals("用户ID", element.getDocContent());
        assertEquals(0, element.getParameterOrder());
        assertFalse(element.isPrimitive());
        
        assertEquals("org.example.UserService#save#id", element.getId());
        assertEquals("org.example.UserService#save", element.getParentId());
    }

    @Test
    void testParameterVariableElementWithPrimitiveType() {
        SharedVariableElement sharedVar = new SharedVariableElement();
        sharedVar.setVariableName("count");
        sharedVar.setVariableType("int");
        sharedVar.setVariableTypeQualifiedName("int");
        sharedVar.setPrimitive(true);
        
        ParameterVariableElement element = new ParameterVariableElement();
        element.setMethodId("org.example.UserService#delete");
        element.setParameterOrder(0);
        element.setSharedVariable(sharedVar);
        
        assertEquals("int", element.getVariableType());
        assertTrue(element.isPrimitive());
    }

    @Test
    void testSharedVariableReuse() {
        SharedVariableElement sharedVar = new SharedVariableElement();
        sharedVar.setVariableName("username");
        sharedVar.setVariableType("String");
        sharedVar.setVariableTypeQualifiedName("java.lang.String");
        sharedVar.setPrimitive(false);
        
        MemberVariableElement member1 = new MemberVariableElement();
        member1.setClassQualifiedName("org.example.UserEntity");
        member1.setSharedVariable(sharedVar);
        member1.setDocContent("实体类用户名");
        
        MemberVariableElement member2 = new MemberVariableElement();
        member2.setClassQualifiedName("org.example.UserIn");
        member2.setSharedVariable(sharedVar);
        member2.setDocContent("输入类用户名");
        
        ParameterVariableElement param = new ParameterVariableElement();
        param.setMethodId("org.example.UserService#save");
        param.setParameterOrder(0);
        param.setSharedVariable(sharedVar);
        param.setDocContent("方法参数用户名");
        
        assertSame(sharedVar, member1.getSharedVariable());
        assertSame(sharedVar, member2.getSharedVariable());
        assertSame(sharedVar, param.getSharedVariable());
        
        assertEquals("username", member1.getVariableName());
        assertEquals("username", member2.getVariableName());
        assertEquals("username", param.getVariableName());
        
        assertEquals("实体类用户名", member1.getDocContent());
        assertEquals("输入类用户名", member2.getDocContent());
        assertEquals("方法参数用户名", param.getDocContent());
    }

    @Test
    void testReturnValueNotUsingSharedVariable() {
        ParameterVariableElement returnValue = new ParameterVariableElement();
        returnValue.setMethodId("org.example.UserService#getUser");
        returnValue.setParameterOrder(-1);
        returnValue.setVariableName("return");
        returnValue.setVariableType("UserEntity");
        returnValue.setVariableTypeQualifiedName("org.example.UserEntity");
        returnValue.setPrimitive(false);
        returnValue.setDocContent("用户对象");
        
        assertNull(returnValue.getSharedVariable());
        assertEquals("return", returnValue.getVariableName());
        assertEquals("UserEntity", returnValue.getVariableType());
        assertEquals("org.example.UserEntity", returnValue.getVariableTypeQualifiedName());
        assertFalse(returnValue.isPrimitive());
        assertEquals(-1, returnValue.getParameterOrder());
        assertEquals("org.example.UserService#getUser#return", returnValue.getId());
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