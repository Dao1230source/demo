package org.source.spring.doc.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.source.spring.doc.domain.element.ModuleDocElement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模块解析器测试
 * <p>
 * 测试模块层级结构的解析功能：
 * <ul>
 *     <li>Module → Class → Method → Field</li>
 *     <li>模块父子关系</li>
 *     <li>SpringBoot 模块识别</li>
 * </ul>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@TestMethodOrder(OrderAnnotation.class)
class ModuleParserTest {

    private ModuleParser moduleParser;
    private String projectRootPath;

    @BeforeEach
    void setUp() {
        moduleParser = new ModuleParser();
        projectRootPath = "/Users/zengfugen/IdeaProjects/dao1230.source/demo";
    }

    @org.junit.jupiter.api.Test
    @Order(1)
    @DisplayName("测试创建 ModuleDocElement")
    void testModuleDocElementCreation() {
        ModuleDocElement module = new ModuleDocElement();
        module.setModuleName("spring-extension-starter-doc");
        module.setModulePath("/path/to/module");
        module.setParentModulePath("/path/to/parent");
        module.setSpringBootModule(true);

        assertEquals("spring-extension-starter-doc", module.getModuleName());
        assertEquals("/path/to/module", module.getModulePath());
        assertEquals("/path/to/parent", module.getParentModulePath());
        assertTrue(module.isSpringBootModule());
        assertEquals("/path/to/module", module.getId());
        assertEquals("/path/to/parent", module.getParentId());
    }

    @org.junit.jupiter.api.Test
    @Order(2)
    @DisplayName("测试 ModuleDocElement 无父模块时的 getParentId")
    void testModuleDocElementGetParentIdWhenNoParent() {
        ModuleDocElement module = new ModuleDocElement();
        module.setModuleName("demo");
        module.setModulePath("/path/to/demo");
        module.setParentModulePath(null);

        assertEquals("", module.getParentId());
    }

    @org.junit.jupiter.api.Test
    @Order(3)
    @DisplayName("测试解析项目模块")
    void testParseProjectModules() throws IOException {
        List<ModuleDocElement> modules = moduleParser.parseProjectModules(projectRootPath);

        assertFalse(modules.isEmpty(), "应该解析出至少一个模块");

        boolean hasSpringBootModule = modules.stream()
                .anyMatch(ModuleDocElement::isSpringBootModule);
        assertTrue(hasSpringBootModule, "项目中应该包含 SpringBoot 模块");
    }

    @org.junit.jupiter.api.Test
    @Order(4)
    @DisplayName("测试模块父子关系")
    void testModuleParentChildRelation() throws IOException {
        List<ModuleDocElement> modules = moduleParser.parseProjectModules(projectRootPath);

        boolean hasParentChildRelation = modules.stream()
                .anyMatch(m -> m.getParentModulePath() != null && !m.getParentModulePath().isEmpty());

        assertTrue(hasParentChildRelation, "应该存在模块父子关系");
    }

    @org.junit.jupiter.api.Test
    @Order(5)
    @DisplayName("测试查找根模块（非 SpringBoot）")
    void testFindRootModule() throws IOException {
        List<ModuleDocElement> modules = moduleParser.parseProjectModules(projectRootPath);

        ModuleDocElement rootModule = moduleParser.findRootModule(modules);

        assertNotNull(rootModule, "应该找到根模块");
    }

    @org.junit.jupiter.api.Test
    @Order(6)
    @DisplayName("测试 class 属于模块")
    void testClassBelongsToModule() throws IOException {
        List<ModuleDocElement> modules = moduleParser.parseProjectModules(projectRootPath);

        String classQualifiedName = "org.source.spring.doc.domain.element.ClassDocElement";
        ModuleDocElement module = moduleParser.findModuleByClassQualifiedName(classQualifiedName, modules);

        assertNotNull(module, "ClassDocElement 应该属于一个模块");
        assertTrue(module.getModulePath().contains("spring-extension-starter-doc"));
    }

    @org.junit.jupiter.api.Test
    @Order(7)
    @DisplayName("测试 SpringBoot 模块识别")
    void testSpringBootModuleDetection() {
        Path pomPath = Paths.get(projectRootPath, "spring-extension-starter", "spring-extension-starter-doc", "pom.xml");

        if (Files.exists(pomPath)) {
            try {
                String content = Files.readString(pomPath);
                boolean isSpringBoot = content.contains("spring-boot-starter") ||
                        content.contains("spring-boot-maven-plugin");

                assertTrue(isSpringBoot, "spring-extension-starter-doc 应该是 SpringBoot 模块");
            } catch (IOException e) {
                fail("读取 pom.xml 失败: " + e.getMessage());
            }
        }
    }

    @org.junit.jupiter.api.Test
    @Order(8)
    @DisplayName("测试模块层级树结构")
    void testModuleHierarchyTree() throws IOException {
        List<ModuleDocElement> modules = moduleParser.parseProjectModules(projectRootPath);

        for (ModuleDocElement module : modules) {
            if (module.isSpringBootModule() && module.getParentModulePath() != null) {
                boolean hasParentModule = modules.stream()
                        .anyMatch(m -> m.getModulePath().equals(module.getParentModulePath()));

                assertTrue(hasParentModule,
                        "SpringBoot 模块的父模块路径应该在模块列表中存在: " + module.getModuleName());
            }
        }
    }

    @org.junit.jupiter.api.Test
    @Order(9)
    @DisplayName("测试模块 artifactId 解析")
    void testModuleArtifactIdParsing() throws IOException {
        Path pomPath = Paths.get(projectRootPath, "spring-extension-starter", "spring-extension-starter-doc", "pom.xml");

        if (Files.exists(pomPath)) {
            String content = Files.readString(pomPath);
            
            int artifactIdStart = content.indexOf("<artifactId>");
            if (artifactIdStart != -1) {
                artifactIdStart = content.indexOf("<artifactId>", artifactIdStart + "<artifactId>".length());
            }
            int artifactIdEnd = content.indexOf("</artifactId>", artifactIdStart);

            if (artifactIdStart != -1 && artifactIdEnd != -1) {
                String artifactId = content.substring(
                        artifactIdStart + "<artifactId>".length(),
                        artifactIdEnd
                ).trim();

                assertEquals("spring-extension-starter-doc", artifactId,
                        "artifactId 应该是 spring-extension-starter-doc");
            }
        }
    }

    @org.junit.jupiter.api.Test
    @Order(10)
    @DisplayName("测试子模块列表解析")
    void testSubModulesParsing() throws IOException {
        Path parentPomPath = Paths.get(projectRootPath, "spring-extension-starter", "pom.xml");

        if (Files.exists(parentPomPath)) {
            String content = Files.readString(parentPomPath);

            int modulesStart = content.indexOf("<modules>");
            int modulesEnd = content.indexOf("</modules>", modulesStart);

            if (modulesStart != -1 && modulesEnd != -1) {
                String modulesSection = content.substring(modulesStart, modulesEnd);

                assertTrue(modulesSection.contains("<module>"),
                        "父 pom.xml 应该包含 modules 列表");

                int moduleCount = 0;
                int moduleStart = modulesSection.indexOf("<module>");
                while (moduleStart != -1) {
                    moduleCount++;
                    int moduleEnd = modulesSection.indexOf("</module>", moduleStart);
                    moduleStart = modulesSection.indexOf("<module>", moduleEnd);
                }

                assertTrue(moduleCount > 0, "应该至少有一个子模块");
            }
        }
    }
}