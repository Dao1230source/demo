package org.source.spring.doc.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 方法文档值对象
 * <p>
 * 表示 Java 方法的文档信息，继承自 {@link DocData}，
 * 包含方法名、返回类型、JavaDoc 注释内容。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MethodDocData extends DocData {

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 返回类型（简单名称）
     */
    private String returnType;

    /**
     * 返回类型的全限定名
     */
    private String returnTypeQualifiedName;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 方法参数类型列表
     */
    private String parameterTypes;

    /**
     * 是否为构造函数
     */
    private Boolean isConstructor;

    /**
     * 是否为私有方法
     */
    private boolean isPrivate;

    /**
     * 是否已废弃（@Deprecated）
     */
    private boolean deprecated;

    /**
     * 废弃说明（@Deprecated 的 since 或 forRemoval 属性）
     */
    private String deprecatedReason;
}