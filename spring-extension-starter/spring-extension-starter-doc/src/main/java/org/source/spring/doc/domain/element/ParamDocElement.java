package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * 参数文档元素
 * <p>
 * 表示方法参数的文档信息，包含参数名、参数类型、
 * 参数类型的全限定名、JavaDoc 注释内容以及所属方法的 ID。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ParamDocElement extends DocElement {

    /**
     * 参数名
     */
    private String paramName;

    /**
     * 参数类型（简单名称）
     */
    private String paramType;

    /**
     * 参数类型的全限定名
     */
    private String paramTypeQualifiedName;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 所属方法的 ID（格式：类全限定名#方法名）
     */
    private String methodId;

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
        if (methodId == null || methodId.isEmpty()) {
            return paramName;
        }
        return methodId + "#" + paramName;
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
}