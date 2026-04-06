package org.source.spring.doc.domain.object;

import org.junit.jupiter.api.Test;
import org.source.spring.doc.domain.element.ClassDocElement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocValue 测试类
 * <p>
 * 测试文档值对象的创建和转换功能
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocValueTest {

    @Test
    void testDocValueCreation() {
        DocValue value = new DocValue();
        
        value.setObjectId("org.example.UserEntity");
        value.setName("UserEntity");
        value.setElementType(DocObjectTypeEnum.CLASS.getType());
        value.setClassName("UserEntity");
        value.setClassQualifiedName("org.example.UserEntity");
        value.setDocContent("用户实体类");
        
        assertEquals("org.example.UserEntity", value.getObjectId());
        assertEquals("UserEntity", value.getName());
        assertEquals(DocObjectTypeEnum.CLASS.getType(), value.getElementType());
        assertEquals("UserEntity", value.getClassName());
        assertEquals("org.example.UserEntity", value.getClassQualifiedName());
        assertEquals("用户实体类", value.getDocContent());
    }

    @Test
    void testDocValueFromClassElement() {
        ClassDocElement classElement = new ClassDocElement();
        classElement.setClassName("UserEntity");
        classElement.setClassQualifiedName("org.example.UserEntity");
        classElement.setDocContent("用户实体类");
        classElement.setEntity(true);
        classElement.setTableName("users");
        
        DocValue value = new DocValue();
        value.setObjectId(classElement.getId());
        value.setName(classElement.getClassName());
        value.setElementType(DocObjectTypeEnum.CLASS.getType());
        value.setClassName(classElement.getClassName());
        value.setClassQualifiedName(classElement.getClassQualifiedName());
        value.setDocContent(classElement.getDocContent());
        value.setIsEntity(classElement.isEntity());
        value.setTableName(classElement.getTableName());
        
        assertEquals("org.example.UserEntity", value.getObjectId());
        assertEquals("UserEntity", value.getName());
        assertEquals(DocObjectTypeEnum.CLASS.getType(), value.getElementType());
        assertTrue(value.getIsEntity());
        assertEquals("users", value.getTableName());
    }

    @Test
    void testDocValueWithPrimitiveType() {
        DocValue value = new DocValue();
        
        value.setObjectId("int#age");
        value.setName("age");
        value.setElementType(DocObjectTypeEnum.SHARED_VARIABLE.getType());
        value.setVariableName("age");
        value.setVariableType("int");
        value.setVariableTypeQualifiedName("int");
        value.setPrimitive(true);
        
        assertEquals("age", value.getVariableName());
        assertEquals("int", value.getVariableType());
        assertTrue(value.getPrimitive());
    }

    @Test
    void testDocValueWithMethodParameter() {
        DocValue value = new DocValue();
        
        value.setObjectId("org.example.UserService#save#id");
        value.setName("id");
        value.setElementType(DocObjectTypeEnum.PARAMETER_VARIABLE.getType());
        value.setVariableName("id");
        value.setVariableType("Long");
        value.setVariableTypeQualifiedName("java.lang.Long");
        value.setParameterOrder(0);
        value.setDocContent("用户ID");
        
        assertEquals("id", value.getVariableName());
        assertEquals("Long", value.getVariableType());
        assertEquals("java.lang.Long", value.getVariableTypeQualifiedName());
        assertEquals(0, value.getParameterOrder());
        assertEquals("用户ID", value.getDocContent());
    }

    @Test
    void testDocValueWithRestEndpoint() {
        DocValue value = new DocValue();
        
        value.setObjectId("org.example.UserController#GET:/users/{id}");
        value.setName("getUser");
        value.setElementType(DocObjectTypeEnum.REST_ENDPOINT.getType());
        value.setHttpMethod("GET");
        value.setPath("/users/{id}");
        value.setDocContent("获取用户详情");
        
        assertEquals("GET", value.getHttpMethod());
        assertEquals("/users/{id}", value.getPath());
        assertEquals("获取用户详情", value.getDocContent());
    }

    @Test
    void testDocValueEquality() {
        DocValue value1 = new DocValue();
        value1.setObjectId("org.example.UserEntity");
        value1.setName("UserEntity");
        
        DocValue value2 = new DocValue();
        value2.setObjectId("org.example.UserEntity");
        value2.setName("UserEntity");
        
        assertEquals(value1.getObjectId(), value2.getObjectId());
        assertEquals(value1.getName(), value2.getName());
    }

    @Test
    void testDocObjectTypeEnum() {
        assertEquals(1, DocObjectTypeEnum.CLASS.getType());
        assertEquals(2, DocObjectTypeEnum.METHOD.getType());
        assertEquals(3, DocObjectTypeEnum.SHARED_VARIABLE.getType());
        assertEquals(4, DocObjectTypeEnum.MEMBER_VARIABLE.getType());
        assertEquals(5, DocObjectTypeEnum.JPA_COLUMN_VARIABLE.getType());
        assertEquals(6, DocObjectTypeEnum.PARAMETER_VARIABLE.getType());
        assertEquals(7, DocObjectTypeEnum.REST_ENDPOINT.getType());
        assertEquals(8, DocObjectTypeEnum.MODULE.getType());
    }

    @Test
    void testDocObjectTypeEnumFromType() {
        assertEquals(DocObjectTypeEnum.CLASS, DocObjectTypeEnum.fromType(1));
        assertEquals(DocObjectTypeEnum.METHOD, DocObjectTypeEnum.fromType(2));
        assertNull(DocObjectTypeEnum.fromType(999));
        assertNull(DocObjectTypeEnum.fromType(null));
    }
}