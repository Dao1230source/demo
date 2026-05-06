package org.source.spring.doc.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 类文档值对象
 * <p>
 * 表示 Java 类的文档信息，继承自 {@link DocData}，
 * 包含类名、修饰符、JavaDoc 注释内容以及 JPA 相关信息。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassDocData extends DocData {

    /**
     * 类名（不含包名）
     */
    private String className;

    /**
     * 修饰符（如 public、abstract、final 等）
     */
    private String modifiers;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 是否为 JPA 实体类
     */
    private boolean isEntity;

    /**
     * JPA 表名（仅当 isEntity 为 true 时有效）
     */
    private String tableName;

    /**
     * 是否为接口
     */
    private Boolean isInterface;

    /**
     * 是否为枚举
     */
    private Boolean isEnum;

    /**
     * 是否已废弃（@Deprecated）
     */
    private boolean deprecated;

    /**
     * 废弃说明（@Deprecated 的 since 或 forRemoval 属性）
     */
    private String deprecatedReason;

    /**
     * Spring 组件注解信息
     */
    private Map<String, Object> springAnnotations;

    /**
     * Feign 客户端信息
     */
    private Map<String, Object> feignInfo;

    /**
     * MyBatis Mapper 信息
     */
    private Map<String, Object> myBatisInfo;
}