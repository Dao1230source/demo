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
 * <p>对应数据库表 users，存储系统用户的基本信息。</p>
 * <p>包含用户的账号信息、联系方式、账户状态以及时间戳等数据。</p>
 * <p>使用 JPA 进行持久化操作，支持用户的增删改查等基础操作。</p>
 *
 * @author System Admin
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * 用户ID
     *
     * <p>主键，自增生成，唯一标识每个用户。</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 用户名
     *
     * <p>用户的登录用户名，必须唯一且不能为空，最大长度50字符。</p>
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 用户邮箱
     *
     * <p>用户的电子邮箱地址，用于系统通知和密码找回，最大长度100字符。</p>
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * 用户状态
     *
     * <p>用户账户状态，使用枚举类型存储。</p>
     * <p>可选值：ACTIVE（激活）、INACTIVE（未激活）、LOCKED（锁定）。</p>
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserStatusEnum status;

    /**
     * 创建时间
     *
     * <p>记录用户账户的创建时间，创建后不可更新。</p>
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     *
     * <p>记录用户信息的最后更新时间，每次更新时自动刷新。</p>
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 构造函数，创建用户实体
     *
     * <p>根据用户名和邮箱创建用户实体，自动设置默认状态为激活，并初始化创建时间和更新时间。</p>
     *
     * @param username 用户名，用于登录标识
     * @param email    用户邮箱，用于通知和找回密码
     */
    public UserEntity(String username, String email) {
        this.username = username;
        this.email = email;
        this.status = UserStatusEnum.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}