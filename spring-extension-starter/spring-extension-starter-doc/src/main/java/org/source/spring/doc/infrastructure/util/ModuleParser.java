package org.source.spring.doc.infrastructure.util;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.source.spring.doc.domain.value.ModuleDocData;
import org.source.spring.doc.domain.object.DocObjectTypeEnum;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * 模块解析器
 * <p>
 * 解析 Maven 模块结构，直接返回 ModuleDocValue 对象。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Getter
public class ModuleParser {

    private int moduleSorted = 0;

    /**
     * 解析项目根目录下的所有模块
     *
     * @return 模块值对象列表
     */
    public List<ModuleDocData> parseProjectModules(String projectRootPath) throws IOException {
        List<ModuleDocData> modules = new ArrayList<>();
        moduleSorted = 0;
        parseModuleRecursive(projectRootPath, null, modules);
        return modules;
    }

    private void parseModuleRecursive(String modulePath, String parentModulePath, List<ModuleDocData> modules) throws IOException {
        Path pomPath = Paths.get(modulePath, "pom.xml");

        if (!Files.exists(pomPath)) {
            return;
        }

        String moduleName = parseModuleArtifactId(pomPath);
        boolean isSpringBootModule = detectSpringBootModule(pomPath);

        ModuleDocData moduleValue = new ModuleDocData();
        moduleValue.setName(modulePath);
        moduleValue.setParentName(parentModulePath != null ? parentModulePath : "");
        moduleValue.setSorted(String.valueOf(moduleSorted++));
        moduleValue.setRelationType(DocObjectTypeEnum.MODULE.getType());
        moduleValue.setModuleName(moduleName);
        moduleValue.setSpringBootModule(isSpringBootModule);

        if (parentModulePath != null) {
            Path parentPomPath = Paths.get(parentModulePath, "pom.xml");
            if (Files.exists(parentPomPath)) {
                String parentModuleName = parseModuleArtifactId(parentPomPath);
                moduleValue.setParentModuleName(parentModuleName);
            }
        }

        modules.add(moduleValue);

        List<String> subModules = parseSubModules(pomPath);
        for (String subModule : subModules) {
            Path subModulePath = Paths.get(modulePath, subModule);
            if (Files.isDirectory(subModulePath)) {
                parseModuleRecursive(subModulePath.toString(), modulePath, modules);
            }
        }
    }

    private String parseModuleArtifactId(Path pomPath) throws IOException {
        String content = Files.readString(pomPath);

        int artifactIdStart = content.indexOf("<artifactId>");
        if (artifactIdStart == -1) {
            return pomPath.getParent().getFileName().toString();
        }

        int artifactIdEnd = content.indexOf("</artifactId>", artifactIdStart);
        if (artifactIdEnd == -1) {
            return pomPath.getParent().getFileName().toString();
        }

        return content.substring(artifactIdStart + "<artifactId>".length(), artifactIdEnd).trim();
    }

    private boolean detectSpringBootModule(Path pomPath) throws IOException {
        String content = Files.readString(pomPath);
        return content.contains("spring-boot-starter") || content.contains("spring-boot-maven-plugin");
    }

    private List<String> parseSubModules(Path pomPath) throws IOException {
        List<String> subModules = new ArrayList<>();
        String content = Files.readString(pomPath);

        int modulesStart = content.indexOf("<modules>");
        if (modulesStart == -1) {
            return subModules;
        }

        int modulesEnd = content.indexOf("</modules>", modulesStart);
        if (modulesEnd == -1) {
            return subModules;
        }

        String modulesSection = content.substring(modulesStart, modulesEnd);

        int moduleStart = modulesSection.indexOf("<module>");
        while (moduleStart != -1) {
            int moduleEnd = modulesSection.indexOf("</module>", moduleStart);
            if (moduleEnd == -1) {
                break;
            }

            String moduleName = modulesSection.substring(moduleStart + "<module>".length(), moduleEnd).trim();
            subModules.add(moduleName);

            moduleStart = modulesSection.indexOf("<module>", moduleEnd);
        }

        return subModules;
    }

    /**
     * 根据类的全限定名查找所属模块
     */
    public ModuleDocData findModuleByClassQualifiedName(String classQualifiedName, List<ModuleDocData> modules) {
        if (StringUtils.isBlank(classQualifiedName)) {
            return null;
        }

        for (ModuleDocData module : modules) {
            String modulePath = module.getName();
            String expectedPackagePath = classQualifiedName.replace('.', '/');

            Path javaFileBase = Paths.get(modulePath, "src/main/java");
            if (Files.exists(javaFileBase)) {
                try {
                    Path expectedPath = javaFileBase.resolve(expectedPackagePath.substring(0, expectedPackagePath.lastIndexOf('/')));
                    if (Files.exists(expectedPath)) {
                        return module;
                    }
                } catch (Exception e) {
                }
            }
        }

        return modules.isEmpty() ? null : modules.get(0);
    }

    /**
     * 查找模块树的根节点
     */
    public ModuleDocData findRootModule(List<ModuleDocData> modules) {
        for (ModuleDocData module : modules) {
            if (!module.isSpringBootModule() &&
                StringUtils.isBlank(module.getParentName())) {
                return module;
            }
        }

        for (ModuleDocData module : modules) {
            if (StringUtils.isBlank(module.getParentName())) {
                return module;
            }
        }

        return modules.isEmpty() ? null : modules.get(0);
    }
}