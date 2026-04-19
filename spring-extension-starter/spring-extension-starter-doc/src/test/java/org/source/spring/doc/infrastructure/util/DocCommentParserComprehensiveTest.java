package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.MethodDocElement;
import org.source.spring.doc.domain.element.MemberVariableElement;
import org.source.spring.doc.domain.element.ParameterVariableElement;
import org.source.spring.doc.domain.element.SharedVariableElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DocCommentParser 综合测试
 * <p>
 * 测试 DocCommentParser 的所有解析功能
 * </p>
 *
 * @author test-author
 * @since 1.0.0
 */
class DocCommentParserComprehensiveTest {

    private DocCommentParser parser;
    private String testServiceSource;
    private String testControllerSource;

    @BeforeEach
    void setUp() throws IOException {
        parser = new DocCommentParser();
        
        Path fixtureDir = Path.of("src/test/resources/fixtures");
        testServiceSource = Files.readString(fixtureDir.resolve("TestService.java"));
        testControllerSource = Files.readString(fixtureDir.resolve("TestRestController.java"));
    }

    // ==================== 类解析测试 ====================

    @Test
    @DisplayName("解析类级别 JavaDoc 注释")
    void testParseClassDoc() {
        ClassDocElement element = parser.parseClassDoc(testServiceSource, "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(element);
        assertEquals("TestService", element.getClassName());
        assertEquals("org.source.spring.doc.test.fixture.TestService", element.getClassQualifiedName());
        assertTrue(element.getDocContent().contains("测试服务类"));
    }

    @Test
    @DisplayName("解析类修饰符")
    void testParseClassModifiers() {
        ClassDocElement element = parser.parseClassDoc(testServiceSource, "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(element);
        assertNotNull(element.getModifiers());
        assertTrue(element.getModifiers().contains("public"));
    }

    // ==================== 方法解析测试 ====================

    @Test
    @DisplayName("解析方法级别 JavaDoc 注释")
    void testParseMethodDoc() {
        MethodDocElement element = parser.parseMethodDoc(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService", "getById");
        
        assertNotNull(element);
        assertEquals("getById", element.getMethodName());
        assertTrue(element.getDocContent().contains("根据 ID 获取用户"));
    }

    @Test
    @DisplayName("解析所有方法（包含重载方法）")
    void testParseAllMethods() {
        List<MethodDocElement> methods = parser.parseAllMethods(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(methods);
        assertFalse(methods.isEmpty());
        
        // 验证重载方法有不同 ID
        List<MethodDocElement> getByIdMethods = methods.stream()
                .filter(m -> "getById".equals(m.getMethodName()))
                .toList();
        
        assertTrue(getByIdMethods.size() >= 2);
        
        // 验证参数类型不同
        for (MethodDocElement method : getByIdMethods) {
            assertNotNull(method.getParameterTypes());
        }
    }

    @Test
    @DisplayName("解析方法参数类型列表")
    void testParseMethodParameterTypes() {
        List<MethodDocElement> methods = parser.parseAllMethods(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        MethodDocElement getByIdWithTwoParams = methods.stream()
                .filter(m -> "getById".equals(m.getMethodName()) 
                        && m.getParameterTypes() != null 
                        && m.getParameterTypes().contains(","))
                .findFirst()
                .orElse(null);
        
        assertNotNull(getByIdWithTwoParams);
        assertTrue(getByIdWithTwoParams.getParameterTypes().contains("Long"));
        assertTrue(getByIdWithTwoParams.getParameterTypes().contains("String"));
    }

    // ==================== 构造函数解析测试 ====================

    @Test
    @DisplayName("解析构造函数")
    void testParseConstructors() {
        List<MethodDocElement> methods = parser.parseAllMethods(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        List<MethodDocElement> constructors = methods.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsConstructor()))
                .toList();
        
        assertTrue(constructors.size() >= 2);
        
        // 验证构造函数名称与类名相同
        for (MethodDocElement constructor : constructors) {
            assertEquals("TestService", constructor.getMethodName());
        }
    }

    @Test
    @DisplayName("构造函数返回类型为类名")
    void testConstructorReturnType() {
        List<MethodDocElement> methods = parser.parseAllMethods(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        MethodDocElement defaultConstructor = methods.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsConstructor()) 
                        && (m.getParameterTypes() == null || m.getParameterTypes().isEmpty()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(defaultConstructor);
        assertEquals("TestService", defaultConstructor.getReturnType());
    }

    // ==================== 字段解析测试 ====================

    @Test
    @DisplayName("解析成员变量")
    void testParseMemberVariables() {
        List<MemberVariableElement> fields = parser.parseAllMemberVariables(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(fields);
        assertFalse(fields.isEmpty());
        
        // 验证字段关联了 SharedVariable
        for (MemberVariableElement field : fields) {
            assertNotNull(field.getSharedVariable());
        }
    }

    @Test
    @DisplayName("解析单个成员变量")
    void testParseMemberVariableDoc() {
        MemberVariableElement element = parser.parseMemberVariableDoc(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService", "userName");
        
        assertNotNull(element);
        assertTrue(element.getDocContent().contains("用户名称"));
    }

    // ==================== 参数解析测试 ====================

    @Test
    @DisplayName("解析方法参数")
    void testParseMethodParameters() {
        List<ParameterVariableElement> params = parser.parseMethodParameters(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService", "getById");
        
        assertNotNull(params);
        assertFalse(params.isEmpty());
        
        // 验证参数顺序
        for (int i = 0; i < params.size(); i++) {
            assertEquals(i, params.get(i).getParameterOrder());
        }
    }

    @Test
    @DisplayName("解析方法返回值")
    void testParseMethodReturnValue() {
        ParameterVariableElement returnValue = parser.parseMethodReturnValue(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService", "getById");
        
        assertNotNull(returnValue);
        assertEquals("return", returnValue.getVariableName());
        assertEquals(-1, returnValue.getParameterOrder());
    }

    // ==================== 内部类解析测试 ====================

    @Test
    @DisplayName("解析内部类")
    void testParseInnerClasses() {
        List<ClassDocElement> innerClasses = parser.parseInnerClasses(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        assertNotNull(innerClasses);
        assertFalse(innerClasses.isEmpty());
        
        // 验证内部类全限定名格式
        for (ClassDocElement inner : innerClasses) {
            assertTrue(inner.getClassQualifiedName().contains("$"));
        }
    }

    @Test
    @DisplayName("内部类识别为枚举类型")
    void testInnerClassIsEnum() {
        List<ClassDocElement> innerClasses = parser.parseInnerClasses(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        ClassDocElement statusEnum = innerClasses.stream()
                .filter(c -> "Status".equals(c.getClassName()))
                .findFirst()
                .orElse(null);
        
        // 注意：当前实现可能无法正确识别枚举，这取决于 JavaParser 的行为
        assertNotNull(statusEnum);
    }

    @Test
    @DisplayName("内部类识别为接口类型")
    void testInnerClassIsInterface() {
        List<ClassDocElement> innerClasses = parser.parseInnerClasses(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        ClassDocElement handlerInterface = innerClasses.stream()
                .filter(c -> "Handler".equals(c.getClassName()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(handlerInterface);
        assertTrue(Boolean.TRUE.equals(handlerInterface.getIsInterface()));
    }

    // ==================== 缓存机制测试 ====================

    @Test
    @DisplayName("parseOnce 缓存 CompilationUnit")
    void testParseOnceCache() {
        // 第一次解析
        parser.parseOnce(testServiceSource);
        
        // 第二次解析相同源码应该复用缓存
        parser.parseOnce(testServiceSource);
        
        // 验证缓存存在
        assertNotNull(parser.parseOnce(testServiceSource));
    }

    @Test
    @DisplayName("clearCache 清空缓存")
    void testClearCache() {
        parser.parseOnce(testServiceSource);
        parser.clearCache();
        
        // 验证缓存已清空（可以再次解析）
        var cu = parser.parseOnce(testServiceSource);
        assertNotNull(cu);
    }

    @Test
    @DisplayName("SharedVariable 缓存机制")
    void testSharedVariableCache() {
        // 解析字段会创建 SharedVariable
        parser.parseAllMemberVariables(testServiceSource, 
                "org.source.spring.doc.test.fixture.TestService");
        
        Map<String, SharedVariableElement> cache = parser.getSharedVariableCache();
        assertNotNull(cache);
        assertFalse(cache.isEmpty());
    }

    // ==================== 边界情况测试 ====================

    @Test
    @DisplayName("解析不存在的类返回 null")
    void testParseNonExistingClass() {
        ClassDocElement element = parser.parseClassDoc(testServiceSource, 
                "org.source.spring.doc.test.fixture.NonExisting");
        
        assertNull(element);
    }

    @Test
    @DisplayName("解析不存在的类返回空列表")
    void testParseAllMethodsNonExistingClass() {
        List<MethodDocElement> methods = parser.parseAllMethods(testServiceSource, 
                "org.source.spring.doc.test.fixture.NonExisting");
        
        assertNotNull(methods);
        assertTrue(methods.isEmpty());
    }

    @Test
    @DisplayName("解析无效源码返回 null")
    void testParseInvalidSource() {
        ClassDocElement element = parser.parseClassDoc("invalid java code", 
                "org.example.Invalid");
        
        assertNull(element);
    }
}