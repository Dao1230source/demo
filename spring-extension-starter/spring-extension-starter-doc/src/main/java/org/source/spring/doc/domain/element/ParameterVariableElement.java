package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * 方法入参变量引用元素
 * <p>
 * 表示方法入参的引用信息，继承自 {@link DocElement}，
 * 包含所属方法的 ID、参数顺序、自身的 JavaDoc 注释内容，
 * 以及关联的共用变量元素。
 * </p>
 * <p>
 * 同一个变量（如 username）可能在多个方法中使用，
 * 它们通过 {@code sharedVariable} 属性共享同一个 {@link SharedVariableElement} 实例。
 * </p>
 * <p>
 * 方法返回值（return）不使用共用变量，直接存储变量信息。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ParameterVariableElement extends DocElement {

    /**
     * 所属方法的 ID（格式：类全限定名#方法名）
     */
    private String methodId;

    /**
     * 参数顺序（从 0 开始，返回值为 -1）
     */
    private int parameterOrder;

    /**
     * JavaDoc 注释内容（自身特有）
     */
    private String docContent;

    /**
     * 关联的共用变量元素
     * <p>
     * 方法返回值（return）不使用共用变量，此字段为 null
     * </p>
     */
    private SharedVariableElement sharedVariable;

    /**
     * 变量名（用于返回值等不使用共用变量的场景）
     */
    private String variableName;

    /**
     * 变量类型（用于返回值等不使用共用变量的场景）
     */
    private String variableType;

    /**
     * 变量类型的全限定名（用于返回值等不使用共用变量的场景）
     */
    private String variableTypeQualifiedName;

    /**
     * 是否为原始类型（用于返回值等不使用共用变量的场景）
     */
    private boolean primitive;

    /**
     * 获取元素的唯一标识
     * <p>
     * 格式：方法ID#参数名，如果 methodId 为空则直接返回参数名
     * </p>
     *
     * @return 参数的唯一标识
     */
    @Override
    public @NonNull String getId() {
        String varName = getVariableName();
        if (methodId == null || methodId.isEmpty()) {
            return varName != null ? varName : String.valueOf(parameterOrder);
        }
        return methodId + "#" + (varName != null ? varName : parameterOrder);
    }

    /**
     * 获取父元素 ID
     * <p>
     * 返回所属方法的 ID
     * </p>
     *
     * @return 所属方法的 ID
     */
    @Override
    public String getParentId() {
        return methodId;
    }

    /**
     * 获取变量名
     * <p>
     * 优先返回共用变量的变量名，如果没有共用变量则返回自身的变量名
     * </p>
     *
     * @return 变量名
     */
    public String getVariableName() {
        if (sharedVariable != null) {
            return sharedVariable.getVariableName();
        }
        return variableName;
    }

    /**
     * 获取变量类型
     * <p>
     * 优先返回共用变量的变量类型，如果没有共用变量则返回自身的变量类型
     * </p>
     *
     * @return 变量类型
     */
    public String getVariableType() {
        if (sharedVariable != null) {
            return sharedVariable.getVariableType();
        }
        return variableType;
    }

    /**
     * 获取变量类型的全限定名
     * <p>
     * 优先返回共用变量的变量类型全限定名，如果没有共用变量则返回自身的变量类型全限定名
     * </p>
     *
     * @return 变量类型的全限定名
     */
    public String getVariableTypeQualifiedName() {
        if (sharedVariable != null) {
            return sharedVariable.getVariableTypeQualifiedName();
        }
        return variableTypeQualifiedName;
    }

    /**
     * 是否为原始类型
     * <p>
     * 优先返回共用变量的原始类型标识，如果没有共用变量则返回自身的原始类型标识
     * </p>
     *
     * @return 是否为原始类型
     */
    public boolean isPrimitive() {
        if (sharedVariable != null) {
            return sharedVariable.isPrimitive();
        }
        return primitive;
    }
}