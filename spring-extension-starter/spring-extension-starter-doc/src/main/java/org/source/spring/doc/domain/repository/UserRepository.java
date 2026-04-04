package org.source.spring.doc.domain.repository;

import org.source.jpa.repository.UnifiedJpaRepository;
import org.source.spring.doc.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户数据仓库
 *
 * <p>
 * 继承 UnifiedJpaRepository，提供用户实体的数据访问能力。
 * 封装了 JPA 的基础 CRUD 操作，并扩展了更多便捷的数据访问方法。
 * </p>
 *
 * <p>
 * 继承的方法包括：
 * <ul>
 *   <li>基础 CRUD 操作（保存、删除、查询）</li>
 *   <li>分页查询</li>
 *   <li>条件查询</li>
 *   <li>批量操作</li>
 * </ul>
 * </p>
 *
 * <p>
 * 使用场景：作为数据访问层，负责与数据库交互，执行用户表的单表操作。
 * 复杂的多表查询应在 Service 层处理。
 * </p>
 *
 * @author source
 * @since 1.0.0
 * @see UnifiedJpaRepository
 * @see UserEntity
 */
@Repository
public interface UserRepository extends UnifiedJpaRepository<UserEntity, Long> {
}