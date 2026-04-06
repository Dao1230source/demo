package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * 成员变量引用元素
 * <p>
 * 表示 Java 类成员变量的引用信息，继承自 {@link DocElement}，
 * 包含所属类的全限定名、自身的 JavaDoc 注释内容，
 * 以及关联的共用变量元素。
 * </p>
 * <p>
 * 同一个变量（如 username）可能在多个类中使用（UserEntity、UserIn、UserOut），
 * 它们通过 {@code sharedVariable} 属性共享同一个 {@link SharedVariableElement} 实例。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MemberVariableElement extends DocElement {

    /**
     * 所属类的全限定名
     */
    private String classQualifiedName;

    /**
     * JavaDoc 注释内容（自身特有）
     */
    private String docContent;

    /**
     * 关联的共用变量元素
     */
    private SharedVariableElement sharedVariable;

    /**
     * 获取元素的唯一标识
     * <p>
     * 格式：类全限定名#变量名
     * </p>
     *
     * @return 成员变量的唯一标识
     */
    @Override
    public @NonNull String getId() {
        return classQualifiedName + "#" + getVariableName();
    }

    /**
     * 获取父元素 ID
     * <p>
     * 返回所属类的全限定名
     * </p>
     *
     * @return 所属类的全限定名
     */
    @Override
    public String getParentId() {
        return classQualifiedName;
    }

    /**
     * 获取变量名
     *
     * @return 变量名，如果 sharedVariable 为 null 则返回 null
     */
    public String getVariableName() {
        return sharedVariable != null ? sharedVariable.getVariableName() : null;
    }

    /**
     * 获取变量类型
     *
     * @return 变量类型，如果 sharedVariable 为 null 则返回 null
     */
    public String getVariableType() {
        return sharedVariable != null ? sharedVariable.getVariableType() : null;
    }

    /**
     * 获取变量类型的全限定名
     *
     * @return 变量类型的全限定名，如果 sharedVariable 为 null 则返回 null
     */
    public String getVariableTypeQualifiedName() {
        return sharedVariable != null ? sharedVariable.getVariableTypeQualifiedName() : null;
    }

    /**
     * 是否为原始类型
     *
     * @return 是否为原始类型，如果 sharedVariable 为 null 则返回 false
     */
    public boolean isPrimitive() {
        return sharedVariable != null && sharedVariable.isPrimitive();
    }
}