package org.source.spring.doc.infrastructure.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.FieldDocElement;
import org.source.spring.doc.domain.element.MethodDocElement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocCommentParser测试 - TDD Phase 1.2
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
    void testParseFieldDocComment() {
        String sourceCode = """
            package org.example;
            
            public class UserEntity {
                /**
                 * 用户名
                 */
                private String username;
            }
            """;

        FieldDocElement element = parser.parseFieldDoc(sourceCode,
            "org.example.UserEntity", "username");

        assertNotNull(element);
        assertEquals("username", element.getFieldName());
        assertEquals("String", element.getFieldType());
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
}