package org.source.spring.doc.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模块文档值对象
 * <p>
 * 表示 Maven 模块的文档信息，继承自 {@link DocData}，
 * 包含模块名、父模块名以及是否为 SpringBoot 模块。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModuleDocData extends DocData {

    /**
     * 模块名称（通常从 pom.xml 的 artifactId 获取）
     */
    private String moduleName;

    /**
     * 父模块名称（可选）
     */
    private String parentModuleName;

    /**
     * 是否为 SpringBoot 模块
     */
    private boolean isSpringBootModule;
}