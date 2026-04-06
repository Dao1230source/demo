package org.source.spring.doc.domain.repository;

import org.source.jpa.repository.UnifiedJpaRepository;
import org.source.spring.doc.domain.entity.DocObjectEntity;
import org.springframework.stereotype.Repository;

/**
 * 文档对象数据仓库
 * <p>
 * 继承 {@link UnifiedJpaRepository}，提供文档对象实体的数据访问能力。
 * 封装了 JPA 的基础 CRUD 操作，并扩展了更多便捷的数据访问方法。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Repository
public interface DocObjectRepository extends UnifiedJpaRepository<DocObjectEntity, Long> {
}