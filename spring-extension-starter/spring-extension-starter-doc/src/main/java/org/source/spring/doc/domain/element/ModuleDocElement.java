package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

/**
 * 模块文档元素
 * <p>
 * 表示 Maven 模块的文档信息，包含模块名、模块路径、
 * 父模块名以及是否为 SpringBoot 模块。
 * </p>
 * <p>
 * 模块层级结构：
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
@EqualsAndHashCode(callSuper = true)
@Data
public class ModuleDocElement extends DocElement {

    /**
     * 模块名称（通常从 pom.xml 的 artifactId 获取）
     */
    private String moduleName;

    /**
     * 模块路径（pom.xml 所在目录）
     */
    private String modulePath;

    /**
     * 父模块名称（可选，若无父模块则为空）
     */
    private String parentModuleName;

    /**
     * 父模块路径（可选，若无父模块则为空）
     */
    private String parentModulePath;

    /**
     * 是否为 SpringBoot 模块
     * <p>
     * 判断依据：
     * <ul>
     *     <li>pom.xml 中包含 spring-boot-starter 依赖</li>
     *     <li>或存在 @SpringBootApplication 注解的类</li>
     * </ul>
     * </p>
     */
    private boolean isSpringBootModule;

    /**
     * 获取元素的唯一标识
     * <p>
     * 使用模块路径作为 ID，确保唯一性
     * </p>
     *
     * @return 模块路径
     */
    @Override
    public @NonNull String getId() {
        return modulePath;
    }

    /**
     * 获取父元素 ID
     * <p>
     * 返回父模块路径，若无父模块则返回项目根路径
     * </p>
     *
     * @return 父模块路径或项目根路径
     */
    @Override
    public String getParentId() {
        if (StringUtils.isNotBlank(parentModulePath)) {
            return parentModulePath;
        }
        return "";
    }
}