package org.source.spring.doc.domain.repository;

import org.source.jpa.repository.UnifiedJpaRepository;
import org.source.spring.doc.domain.entity.DocObjectBodyEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档对象内容数据仓库
 * <p>
 * 继承 {@link UnifiedJpaRepository}，提供文档对象内容实体的数据访问能力。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Repository
public interface DocObjectBodyRepository extends UnifiedJpaRepository<DocObjectBodyEntity, Long> {

    /**
     * 根据对象 ID 查询文档对象内容
     *
     * @param objectId 对象 ID
     * @return 文档对象内容
     */
    DocObjectBodyEntity findByObjectId(String objectId);

    /**
     * 根据对象 ID 列表批量查询
     *
     * @param objectIds 对象 ID 列表
     * @return 文档对象内容列表
     */
    List<DocObjectBodyEntity> findByObjectIdIn(List<String> objectIds);
}