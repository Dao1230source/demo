package org.source.spring.doc.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.source.spring.object.entity.ObjectEntityDefiner;

import java.time.LocalDateTime;

/**
 * 文档对象实体
 * <p>
 * 对应数据库表 doc_object，存储文档对象的基本信息。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "doc_object", uniqueConstraints = {
        @UniqueConstraint(name = "uk_object_id_version", columnNames = {"object_id", "version"})
})
public class DocObjectEntity implements ObjectEntityDefiner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_id", nullable = false, length = 64)
    private String objectId;

    @Column(name = "space_id", length = 64)
    private String spaceId;

    @Column(name = "type", nullable = false)
    private Integer type;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "DRAFT";

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

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