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

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 对象 ID，关联 doc_object 表
     */
    @Column(name = "object_id", nullable = false, length = 64)
    private String objectId;

    /**
     * 名称
     */
    @Column(name = "name", length = 255)
    private String name;

    /**
     * 对象内容（JSON 格式）
     */
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

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

    /**
     * 更新人
     */
    @Column(name = "update_user", length = 64)
    private String updateUser;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 持久化前回调，自动设置创建时间和更新时间
     */
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }

    /**
     * 更新前回调，自动设置更新时间
     */
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}