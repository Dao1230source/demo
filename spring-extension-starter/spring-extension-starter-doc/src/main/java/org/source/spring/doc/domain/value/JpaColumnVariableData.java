package org.source.spring.doc.domain.value;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * JPA 列变量值对象
 * <p>
 * 表示 JPA 实体类中标注了 {@code @Column} 或 {@code @Id} 注解的成员变量，
 * 继承自 {@link MemberVariableData}，额外保存 JPA 相关的列信息。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JpaColumnVariableData extends MemberVariableData {

    /**
     * JPA 列名（对应数据库列名）
     */
    private String columnName;

    /**
     * 是否为主键字段
     */
    private boolean primaryKey;
}