package org.source.spring.doc.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.source.spring.object.definer.entity.RelationEntityDefiner;

import java.time.LocalDateTime;

/**
 * 文档对象关系实体
 * <p>
 * 对应数据库表 doc_relation，存储文档对象之间的父子关系。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "relation", indexes = {
        @Index(name = "idx_object_id", columnList = "object_id"),
        @Index(name = "idx_parent_object_id", columnList = "parent_object_id")
})
public class RelationEntity implements RelationEntityDefiner {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 子对象 ID
     */
    @Column(name = "object_id", nullable = false, length = 64)
    private String objectId;

    /**
     * 父对象 ID
     */
    @Column(name = "parent_object_id", nullable = false, length = 64)
    private String parentObjectId;

    /**
     * 关系类型
     */
    @Column(name = "type")
    private Integer type;

    /**
     * 排序字段
     */
    @Column(name = "sorted", length = 50)
    private String sorted;

    /**
     * 创建人
     */
    @Column(name = "create_user", length = 64)
    private String createUser;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;
}