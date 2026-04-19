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

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 对象 ID，唯一标识
     */
    @Column(name = "object_id", nullable = false, length = 64)
    private String objectId;

    /**
     * 空间 ID（模块 ID）
     */
    @Column(name = "space_id", length = 64)
    private String spaceId;

    /**
     * 对象类型编码
     * <p>
     * 取值见 {@link org.source.spring.doc.domain.object.DocObjectTypeEnum}
     * </p>
     */
    @Column(name = "type", nullable = false)
    private Integer type;

    /**
     * 版本号，用于版本管理
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    /**
     * 状态：DRAFT（草稿）、PUBLISHED（已发布）、MERGED（已合并）
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "DRAFT";

    /**
     * 是否已删除（逻辑删除标识）
     */
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

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