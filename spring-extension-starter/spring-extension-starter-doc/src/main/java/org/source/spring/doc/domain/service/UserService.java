package org.source.spring.doc.domain.service;

import org.source.jpa.enhance.AbstractJpaHelper;
import org.source.spring.doc.domain.entity.UserEntity;
import org.source.spring.doc.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * 用户服务接口
 *
 * <p>提供用户相关的业务逻辑处理</p>
 */
@Service
public class UserService extends AbstractJpaHelper<UserEntity, Long> {

    protected UserService(UserRepository repository) {
        super(repository);
    }
}