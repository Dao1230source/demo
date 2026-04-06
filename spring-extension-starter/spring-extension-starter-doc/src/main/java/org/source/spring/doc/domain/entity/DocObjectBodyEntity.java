package org.source.spring.doc.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.source.spring.object.entity.ObjectBodyEntityDefiner;

import java.time.LocalDateTime;

/**
 * 文档对象内容实体
 * <p>
 * 对应数据库表 doc_object_body，存储文档对象的详细内容（JSON格式）。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "doc_object_body", indexes = {
        @Index(name = "idx_object_id", columnList = "object_id")
})
public class DocObjectBodyEntity implements ObjectBodyEntityDefiner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_id", nullable = false, length = 64)
    private String objectId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "create_user", length = 64)
    private String createUser;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_user", length = 64)
    private String updateUser;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}