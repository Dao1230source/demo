package org.source.spring.doc.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.source.spring.object.entity.RelationEntityDefiner;

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
@Table(name = "doc_relation", indexes = {
        @Index(name = "idx_object_id", columnList = "object_id"),
        @Index(name = "idx_parent_object_id", columnList = "parent_object_id")
})
public class DocRelationEntity implements RelationEntityDefiner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_id", nullable = false, length = 64)
    private String objectId;

    @Column(name = "parent_object_id", nullable = false, length = 64)
    private String parentObjectId;

    @Column(name = "type")
    private Integer type;

    @Column(name = "sorted", length = 50)
    private String sorted;

    @Column(name = "create_user", length = 64)
    private String createUser;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
    }
}