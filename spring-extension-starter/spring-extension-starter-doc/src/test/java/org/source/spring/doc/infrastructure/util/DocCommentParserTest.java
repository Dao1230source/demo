package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.doc.domain.value.ClassDocData;
import org.source.spring.doc.domain.value.MemberVariableData;
import org.source.spring.doc.domain.value.MethodDocData;
import org.source.spring.doc.domain.value.ParameterVariableData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocCommentParser测试
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

        ClassDocData value = parser.parseClassDoc(sourceCode, "org.example.UserEntity", "test-module", 0);

        assertNotNull(value);
        assertEquals("UserEntity", value.getClassName());
        assertEquals("org.example.UserEntity", value.getName());
        assertEquals("test-module", value.getParentName());
        assertEquals("用户实体类", value.getDocContent());
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

        List<MethodDocData> values = parser.parseAllMethods(sourceCode, "org.example.UserService");

        assertEquals(2, values.size());
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

        List<ParameterVariableData> params = parser.parseMethodParameters(
                sourceCode, "org.example.UserService", "findByIdAndStatus");

        assertEquals(2, params.size());

        assertEquals("id", params.get(0).getVariableName());
        assertEquals("Long", params.get(0).getVariableType());
        assertEquals(0, params.get(0).getParameterOrder());
        assertEquals("org.example.UserService#findByIdAndStatus", params.get(0).getParentName());

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

        ParameterVariableData returnValue = parser.parseMethodReturnValue(
                sourceCode, "org.example.UserService", "getUser");

        assertNotNull(returnValue);
        assertEquals("return", returnValue.getVariableName());
        assertEquals("UserEntity", returnValue.getVariableType());
        assertEquals("org.example.UserService#getUser", returnValue.getParentName());
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

        List<MemberVariableData> values = parser.parseAllMemberVariables(sourceCode, "org.example.UserEntity");

        assertEquals(2, values.size());

        MemberVariableData ageField = values.stream()
                .filter(f -> "age".equals(f.getVariableName()))
                .findFirst()
                .orElse(null);
        assertNotNull(ageField);
        assertEquals("int", ageField.getVariableType());
        assertTrue(ageField.isPrimitive());

        MemberVariableData activeField = values.stream()
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

        List<MemberVariableData> values = parser.parseAllMemberVariables(sourceCode, "org.example.UserEntity");

        assertEquals(2, values.size());

        for (MemberVariableData value : values) {
            assertFalse(value.isPrimitive());
        }
    }
}