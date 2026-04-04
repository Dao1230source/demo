package org.source.spring.doc.facade.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 用户输入数据传输对象
 *
 * <p>用于接收前端或外部系统传入的用户数据。</p>
 * <p>包含用户创建和更新时所需的基本字段，并提供了数据校验注解。</p>
 * <p>实现了 {@link Serializable} 接口，支持序列化传输。</p>
 *
 * @author System Admin
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIn implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     *
     * <p>用户唯一标识，更新时必填，创建时可为空。</p>
     */
    private Long id;

    /**
     * 用户名
     *
     * <p>用户的登录用户名，必填字段，最大长度50字符。</p>
     */
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    /**
     * 用户邮箱
     *
     * <p>用户的电子邮箱地址，必填字段，必须是有效的邮箱格式。</p>
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
}