package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.MemberVariableElement;
import org.source.spring.doc.domain.element.MethodDocElement;
import org.source.spring.doc.domain.element.ParameterVariableElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocCommentParser测试
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocCommentParserTest {

    private DocCommentParser parser;

    @BeforeEach
    void setUp() {
        parser = new DocCommentParser();
    }

    @Test
    void testParseClassDocComment() {
        String sourceCode = """
            package org.example;
            
            /**
             * 用户实体类
             */
            public class UserEntity {
            }
            """;

        ClassDocElement element = parser.parseClassDoc(sourceCode, "org.example.UserEntity");

        assertNotNull(element);
        assertEquals("UserEntity", element.getClassName());
        assertEquals("org.example.UserEntity", element.getClassQualifiedName());
        assertEquals("用户实体类", element.getDocContent());
    }

    @Test
    void testParseMethodDocComment() {
        String sourceCode = """
            package org.example;
            
            public class UserService {
                /**
                 * 根据ID查询用户
                 * @param id 用户ID
                 * @return 用户对象
                 */
                public UserEntity findById(Long id) {
                    return null;
                }
            }
            """;

        MethodDocElement element = parser.parseMethodDoc(sourceCode, 
            "org.example.UserService", "findById");

        assertNotNull(element);
        assertEquals("findById", element.getMethodName());
        assertEquals("UserEntity", element.getReturnType());
        assertEquals("根据ID查询用户", element.getDocContent());
    }

    @Test
    void testParseMemberVariableDocComment() {
        String sourceCode = """
            package org.example;
            
            public class UserEntity {
                /**
                 * 用户名
                 */
                private String username;
            }
            """;

        MemberVariableElement element = parser.parseMemberVariableDoc(sourceCode,
            "org.example.UserEntity", "username");

        assertNotNull(element);
        assertEquals("username", element.getVariableName());
        assertEquals("String", element.getVariableType());
        assertEquals("用户名", element.getDocContent());
    }

    @Test
    void testParseMultipleMethods() {
        String sourceCode = """
            package org.example;
            
            public class UserService {
                /**
                 * 保存用户
                 */
                public void save(UserEntity user) {
                }
                
                /**
                 * 删除用户
                 */
                public void delete(Long id) {
                }
            }
            """;

        List<MethodDocElement> elements = parser.parseAllMethods(sourceCode, "org.example.UserService");

        assertEquals(2, elements.size());
    }

    @Test
    void testParseMethodParameters() {
        String sourceCode = """
            package org.example;
            
            public class UserService {
                /**
                 * 根据ID和状态查询用户
                 * @param id 用户ID
                 * @param status 用户状态
                 * @return 用户对象
                 */
                public UserEntity findByIdAndStatus(Long id, String status) {
                    return null;
                }
            }
            """;

        List<ParameterVariableElement> params = parser.parseMethodParameters(
                sourceCode, "org.example.UserService", "findByIdAndStatus");

        assertEquals(2, params.size());
        
        assertEquals("id", params.get(0).getVariableName());
        assertEquals("Long", params.get(0).getVariableType());
        assertEquals(0, params.get(0).getParameterOrder());
        assertEquals("org.example.UserService#findByIdAndStatus", params.get(0).getParentId());
        
        assertEquals("status", params.get(1).getVariableName());
        assertEquals("String", params.get(1).getVariableType());
        assertEquals(1, params.get(1).getParameterOrder());
    }

    @Test
    void testParseMethodReturnValue() {
        String sourceCode = """
            package org.example;
            
            public class UserService {
                /**
                 * 获取用户信息
                 * @return 用户对象，包含完整信息
                 */
                public UserEntity getUser() {
                    return null;
                }
            }
            """;

        ParameterVariableElement returnValue = parser.parseMethodReturnValue(
                sourceCode, "org.example.UserService", "getUser");

        assertNotNull(returnValue);
        assertEquals("return", returnValue.getVariableName());
        assertEquals("UserEntity", returnValue.getVariableType());
        assertEquals("org.example.UserService#getUser", returnValue.getParentId());
    }

    @Test
    void testParsePrimitiveTypeVariable() {
        String sourceCode = """
            package org.example;
            
            public class UserEntity {
                /**
                 * 年龄
                 */
                private int age;
                
                /**
                 * 是否激活
                 */
                private boolean active;
            }
            """;

        List<MemberVariableElement> elements = parser.parseAllMemberVariables(sourceCode, "org.example.UserEntity");

        assertEquals(2, elements.size());
        
        MemberVariableElement ageField = elements.stream()
                .filter(f -> "age".equals(f.getVariableName()))
                .findFirst()
                .orElse(null);
        assertNotNull(ageField);
        assertEquals("int", ageField.getVariableType());
        assertTrue(ageField.isPrimitive());
        
        MemberVariableElement activeField = elements.stream()
                .filter(f -> "active".equals(f.getVariableName()))
                .findFirst()
                .orElse(null);
        assertNotNull(activeField);
        assertEquals("boolean", activeField.getVariableType());
        assertTrue(activeField.isPrimitive());
    }

    @Test
    void testParseNonPrimitiveTypeVariable() {
        String sourceCode = """
            package org.example;
            
            public class UserEntity {
                /**
                 * 用户名
                 */
                private String username;
                
                /**
                 * 用户ID
                 */
                private Long id;
            }
            """;

        List<MemberVariableElement> elements = parser.parseAllMemberVariables(sourceCode, "org.example.UserEntity");

        assertEquals(2, elements.size());
        
        for (MemberVariableElement element : elements) {
            assertFalse(element.isPrimitive());
        }
    }
}