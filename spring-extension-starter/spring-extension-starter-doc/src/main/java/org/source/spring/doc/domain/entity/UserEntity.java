package org.source.spring.doc.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.source.spring.doc.infrastructure.enums.UserStatusEnum;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 
 * <p>这个实体类代表系统中的用户信息，包含基本信息、状态和时间戳。</p>
 * 
 * @author System Admin
 * @version 1.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * 用户ID - 主键
     * 
     * <p>自增主键，唯一标识每个用户</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户名 - 唯一标识
     * 
     * <p>用户的登录用户名，必须唯一且不能为空</p>
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 用户邮箱
     * 
     * <p>用户的电子邮箱地址，用于通知和找回密码</p>
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * 用户状态
     * 
     * <p>用户账户状态：ACTIVE=激活, INACTIVE=未激活, LOCKED=锁定</p>
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatusEnum status;

    /**
     * 创建时间
     * 
     * <p>记录用户账户的创建时间</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 
     * <p>记录用户信息的最后更新时间</p>
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 构造函数
    public UserEntity(String username, String email) {
        this.username = username;
        this.email = email;
        this.status = UserStatusEnum.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}