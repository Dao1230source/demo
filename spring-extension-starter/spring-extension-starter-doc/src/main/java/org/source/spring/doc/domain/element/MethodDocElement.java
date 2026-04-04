package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * 方法文档元素
 * <p>
 * 表示 Java 方法的文档信息，包含方法名、返回类型、
 * 返回类型的全限定名、JavaDoc 注释内容以及所属类的全限定名。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MethodDocElement extends DocElement {

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
     * 所属类的全限定名
     */
    private String classQualifiedName;

    /**
     * 获取元素的唯一标识
     * <p>
     * 格式：类全限定名#方法名
     * </p>
     *
     * @return 方法的唯一标识
     */
    @Override
    public @NonNull String getId() {
        return classQualifiedName + "#" + methodName;
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
}