package org.source.spring.doc.facade.output;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户返回对象 (VO - View Object)
 * 
 * <p>用于向前端返回用户信息，包含额外的展示字段</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserOut {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 账户状态描述
     */
    private String statusDescription;
}