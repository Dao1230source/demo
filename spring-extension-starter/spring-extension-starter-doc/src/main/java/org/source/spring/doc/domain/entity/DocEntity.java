package org.source.spring.doc.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.source.jpa.annotation.CheckExists;
import org.source.jpa.enums.OperateEnum;
import org.source.spring.object.definer.entity.ObjectBodyEntityDefiner;

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
@Table(name = "doc", indexes = {
        @Index(name = "idx_object_id", columnList = "object_id")
})
public class DocEntity implements ObjectBodyEntityDefiner {

    /**
     * 主键 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 对象 ID，关联 doc_object 表
     */
    @CheckExists(operate = {OperateEnum.UPDATE, OperateEnum.DELETE})
    @Column(name = "object_id", nullable = false, length = 64)
    private String objectId;

    /**
     * 名称
     */
    @Column(name = "name", length = 255)
    private String name;

    /**
     * 父名称
     */
    @Column(name = "parent_name", length = 255)
    private String parentName;

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
}