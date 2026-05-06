package org.source.spring.doc.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 方法入参变量值对象
 * <p>
 * 表示方法入参的引用信息，继承自 {@link DocData}，
 * 包含参数顺序、自身的 JavaDoc 注释内容，
 * 以及关联的共用变量值对象。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterVariableData extends DocData {

    /**
     * 参数顺序（从 0 开始，返回值为 -1）
     */
    private int parameterOrder;

    /**
     * JavaDoc 注释内容（自身特有）
     */
    private String docContent;

    /**
     * 关联的共用变量值对象
     */
    private SharedVariableData sharedVariable;

    /**
     * 变量名（用于返回值等不使用共用变量的场景）
     */
    private String variableName;

    /**
     * 变量类型（用于返回值等不使用共用变量的场景）
     */
    private String variableType;

    /**
     * 变量类型的全限定名
     */
    private String variableTypeQualifiedName;

    /**
     * 是否为原始类型
     */
    private boolean primitive;

    /**
     * 验证注解列表
     */
    private List<String> validationAnnotations;

    /**
     * 获取变量名
     */
    public String getVariableName() {
        if (sharedVariable != null) {
            return sharedVariable.getVariableName();
        }
        return variableName;
    }

    /**
     * 获取变量类型
     */
    public String getVariableType() {
        if (sharedVariable != null) {
            return sharedVariable.getVariableType();
        }
        return variableType;
    }

    /**
     * 获取变量类型的全限定名
     */
    public String getVariableTypeQualifiedName() {
        if (sharedVariable != null) {
            return sharedVariable.getVariableTypeQualifiedName();
        }
        return variableTypeQualifiedName;
    }

    /**
     * 是否为原始类型
     */
    public boolean isPrimitive() {
        if (sharedVariable != null) {
            return sharedVariable.isPrimitive();
        }
        return primitive;
    }
}