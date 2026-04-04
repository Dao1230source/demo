package org.source.spring.doc.infrastructure.util;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.source.spring.doc.domain.element.ModuleDocElement;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * 模块解析器
 * <p>
 * 解析 Maven 模块结构，建立模块层级关系：
 * <ul>
 *     <li>class 的父级是 module</li>
 *     <li>module 的父级是 module 或 project</li>
 *     <li>追溯到非 SpringBoot 服务为止</li>
 * </ul>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Getter
public class ModuleParser {

    /**
     * 解析项目根目录下的所有模块
     *
     * @param projectRootPath 项目根路径
     * @return 模块元素列表
     * @throws IOException 如果目录读取失败
     */
    public List<ModuleDocElement> parseProjectModules(String projectRootPath) throws IOException {
        List<ModuleDocElement> modules = new ArrayList<>();
        parseModuleRecursive(projectRootPath, null, modules);
        return modules;
    }

    /**
     * 递归解析模块及其子模块
     *
     * @param modulePath 模块路径
     * @param parentModulePath 父模块路径（可选）
     * @param modules 模块列表（累积结果）
     * @throws IOException 如果目录读取失败
     */
    private void parseModuleRecursive(String modulePath, String parentModulePath, List<ModuleDocElement> modules) throws IOException {
        Path pomPath = Paths.get(modulePath, "pom.xml");
        
        if (!Files.exists(pomPath)) {
            return;
        }

        String moduleName = parseModuleArtifactId(pomPath);
        boolean isSpringBootModule = detectSpringBootModule(pomPath);
        
        ModuleDocElement moduleElement = new ModuleDocElement();
        moduleElement.setModuleName(moduleName);
        moduleElement.setModulePath(modulePath);
        moduleElement.setParentModulePath(parentModulePath);
        moduleElement.setSpringBootModule(isSpringBootModule);
        
        if (parentModulePath != null) {
            Path parentPomPath = Paths.get(parentModulePath, "pom.xml");
            if (Files.exists(parentPomPath)) {
                String parentModuleName = parseModuleArtifactId(parentPomPath);
                moduleElement.setParentModuleName(parentModuleName);
            }
        }
        
        modules.add(moduleElement);

        List<String> subModules = parseSubModules(pomPath);
        for (String subModule : subModules) {
            Path subModulePath = Paths.get(modulePath, subModule);
            if (Files.isDirectory(subModulePath)) {
                parseModuleRecursive(subModulePath.toString(), modulePath, modules);
            }
        }
    }

    /**
     * 从 pom.xml 中解析 artifactId（模块名）
     *
     * @param pomPath pom.xml 文件路径
     * @return artifactId
     * @throws IOException 如果文件读取失败
     */
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

    /**
     * 检测是否为 SpringBoot 模块
     * <p>
     * 判断依据：
     * <ul>
     *     <li>pom.xml 中包含 spring-boot-starter 依赖</li>
     *     <li>或包含 spring-boot-maven-plugin</li>
     * </ul>
     * </p>
     *
     * @param pomPath pom.xml 文件路径
     * @return true 如果是 SpringBoot 模块
     * @throws IOException 如果文件读取失败
     */
    private boolean detectSpringBootModule(Path pomPath) throws IOException {
        String content = Files.readString(pomPath);
        return content.contains("spring-boot-starter") || content.contains("spring-boot-maven-plugin");
    }

    /**
     * 从 pom.xml 中解析子模块列表
     * <p>
     * 提取 <modules> 标签中的所有 <module> 子标签
     * </p>
     *
     * @param pomPath pom.xml 文件路径
     * @return 子模块名称列表
     * @throws IOException 如果文件读取失败
     */
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
     * <p>
     * 通过类的包名推断模块路径
     * </p>
     *
     * @param classQualifiedName 类的全限定名
     * @param modules 模块列表
     * @return 所属模块元素，如果未找到则返回 null
     */
    public ModuleDocElement findModuleByClassQualifiedName(String classQualifiedName, List<ModuleDocElement> modules) {
        if (StringUtils.isBlank(classQualifiedName)) {
            return null;
        }
        
        for (ModuleDocElement module : modules) {
            String modulePath = module.getModulePath();
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
     * 查找模块树的根节点（非 SpringBoot 模块）
     *
     * @param modules 模块列表
     * @return 根模块元素，如果未找到则返回 null
     */
    public ModuleDocElement findRootModule(List<ModuleDocElement> modules) {
        for (ModuleDocElement module : modules) {
            if (!module.isSpringBootModule() && 
                StringUtils.isBlank(module.getParentModulePath())) {
                return module;
            }
        }
        
        for (ModuleDocElement module : modules) {
            if (StringUtils.isBlank(module.getParentModulePath())) {
                return module;
            }
        }
        
        return modules.isEmpty() ? null : modules.get(0);
    }
}