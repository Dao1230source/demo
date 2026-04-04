package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * 字段文档元素
 * <p>
 * 表示 Java 字段的文档信息，包含字段名、字段类型、
 * 字段类型的全限定名、JavaDoc 注释内容、所属类的全限定名，
 * 以及 JPA 相关信息（列名、是否为主键等）。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FieldDocElement extends DocElement {

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段类型（简单名称）
     */
    private String fieldType;

    /**
     * 字段类型的全限定名
     */
    private String fieldTypeQualifiedName;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 所属类的全限定名
     */
    private String classQualifiedName;

    /**
     * JPA 列名（对应数据库列名）
     */
    private String columnName;

    /**
     * 是否为主键字段
     */
    private boolean isPrimaryKey;

    /**
     * 获取元素的唯一标识
     * <p>
     * 格式：类全限定名#字段名
     * </p>
     *
     * @return 字段的唯一标识
     */
    @Override
    public @NonNull String getId() {
        return classQualifiedName + "#" + fieldName;
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