package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * 共用变量元素
 * <p>
 * 表示变量的共用定义，同一变量在多处使用时共享同一个实例。
 * 例如：username 变量可能是 UserEntity、UserIn、UserOut 的成员属性，
 * 也可能是某个方法的入参，此时它们共享同一个 SharedVariableElement。
 * </p>
 * <p>
 * 共用变量元素没有父级，通过变量名和类型进行唯一标识。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SharedVariableElement extends DocElement {

    /**
     * 变量名
     */
    private String variableName;

    /**
     * 变量类型（简单名称）
     */
    private String variableType;

    /**
     * 变量类型的全限定名
     */
    private String variableTypeQualifiedName;

    /**
     * 是否为原始类型
     * <p>
     * 原始类型包括：byte, short, int, long, float, double, boolean, char
     * </p>
     */
    private boolean primitive;

    /**
     * 获取元素的唯一标识
     * <p>
     * 格式：变量类型全限定名#变量名
     * </p>
     *
     * @return 共用变量的唯一标识
     */
    @Override
    public @NonNull String getId() {
        String type = variableTypeQualifiedName != null ? variableTypeQualifiedName : variableType;
        return type + "#" + variableName;
    }

    /**
     * 获取父元素 ID
     * <p>
     * 共用变量没有父级，返回 null
     * </p>
     *
     * @return null
     */
    @Override
    public String getParentId() {
        return null;
    }
}