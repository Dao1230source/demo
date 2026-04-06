package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JPA 列变量引用元素
 * <p>
 * 表示 JPA 实体类中标注了 {@code @Column} 或 {@code @Id} 注解的成员变量，
 * 继承自 {@link MemberVariableElement}，额外保存 JPA 相关的列信息。
 * </p>
 * <p>
 * 包含以下 JPA 特有信息：
 * <ul>
 *     <li>{@code columnName} - 数据库列名</li>
 *     <li>{@code isPrimaryKey} - 是否为主键字段</li>
 * </ul>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JpaColumnVariableElement extends MemberVariableElement {

    /**
     * JPA 列名（对应数据库列名）
     */
    private String columnName;

    /**
     * 是否为主键字段
     */
    private boolean primaryKey;
}