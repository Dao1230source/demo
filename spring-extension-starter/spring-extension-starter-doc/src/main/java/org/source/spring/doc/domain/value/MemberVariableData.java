package org.source.spring.doc.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 成员变量值对象
 * <p>
 * 表示 Java 类成员变量的引用信息，继承自 {@link DocData}，
 * 包含自身的 JavaDoc 注释内容以及关联的共用变量值对象。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberVariableData extends DocData {

    /**
     * JavaDoc 注释内容（自身特有）
     */
    private String docContent;

    /**
     * 关联的共用变量值对象
     */
    private SharedVariableData sharedVariable;

    /**
     * 是否为私有成员
     */
    private boolean isPrivate;

    /**
     * 验证注解列表
     */
    private List<String> validationAnnotations;

    /**
     * 是否已废弃（@Deprecated）
     */
    private boolean deprecated;

    /**
     * 废弃说明（@Deprecated 的 since 或 forRemoval 属性）
     */
    private String deprecatedReason;

    /**
     * 获取变量名
     */
    public String getVariableName() {
        return sharedVariable != null ? sharedVariable.getVariableName() : null;
    }

    /**
     * 获取变量类型
     */
    public String getVariableType() {
        return sharedVariable != null ? sharedVariable.getVariableType() : null;
    }

    /**
     * 获取变量类型的全限定名
     */
    public String getVariableTypeQualifiedName() {
        return sharedVariable != null ? sharedVariable.getVariableTypeQualifiedName() : null;
    }

    /**
     * 是否为原始类型
     */
    public boolean isPrimitive() {
        return sharedVariable != null && sharedVariable.isPrimitive();
    }
}