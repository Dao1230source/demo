package org.source.spring.doc.domain.service;

import org.source.jpa.enhance.AbstractJpaHelper;
import org.source.spring.doc.domain.entity.UserEntity;
import org.source.spring.doc.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 用户领域服务
 *
 * <p>
 * 提供用户相关的业务逻辑处理，继承自 AbstractJpaHelper，
 * 封装了常用的数据库操作方法，包括增删改查等基础操作。
 * </p>
 *
 * <p>
 * 继承的方法包括：
 * <ul>
 *   <li>getById - 根据ID获取实体</li>
 *   <li>findAll - 获取所有实体</li>
 *   <li>add - 添加实体</li>
 *   <li>update - 更新实体</li>
 *   <li>saveAll - 批量保存实体</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用场景：处理用户相关的核心业务逻辑，如用户信息管理、数据验证等。
 * 一个领域服务可能涉及多个数据表的操作。
 * </p>
 *
 * @author source
 * @since 1.0.0
 * @see AbstractJpaHelper
 */
@Service
public class UserService extends AbstractJpaHelper<UserEntity, Long> {

    /**
     * 构造函数
     *
     * @param repository 用户数据仓库
     */
    protected UserService(UserRepository repository) {
        super(repository);
    }
}