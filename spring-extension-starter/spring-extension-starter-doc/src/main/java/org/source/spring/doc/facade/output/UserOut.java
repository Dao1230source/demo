package org.source.spring.doc.facade.output;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户输出视图对象
 *
 * <p>用于向前端或外部系统返回用户信息。</p>
 * <p>包含了用户展示所需的基本字段，以及状态描述等扩展信息。</p>
 *
 * @author System Admin
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOut {

    /**
     * 用户ID
     *
     * <p>用户唯一标识，用于前端进行数据关联和操作。</p>
     */
    private Long id;

    /**
     * 用户名
     *
     * <p>用户的登录用户名，用于展示用户身份。</p>
     */
    private String username;

    /**
     * 用户邮箱
     *
     * <p>用户的电子邮箱地址，用于展示联系方式。</p>
     */
    private String email;

    /**
     * 账户状态描述
     *
     * <p>用户账户状态的文字描述，如"激活"、"未激活"、"锁定"等。</p>
     */
    private String statusDescription;
}