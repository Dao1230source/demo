package org.source.spring.doc.infrastructure.enums;

/**
 * 用户状态枚举
 * 
 * <p>定义用户账户的不同状态</p>
 */
public enum UserStatusEnum {
    /**
     * 激活状态 - 用户可以正常登录和使用系统功能
     */
    ACTIVE,
    
    /**
     * 未激活状态 - 用户注册后但尚未激活账户
     */
    INACTIVE,
    
    /**
     * 锁定状态 - 用户因安全原因被锁定，无法登录
     */
    LOCKED
}