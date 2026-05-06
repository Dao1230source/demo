package org.source.spring.doc.domain.repository;

import org.source.jpa.ExtendedRepository;
import org.source.spring.doc.domain.entity.DocEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档对象内容数据仓库
 * <p>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Repository
public interface DocRepository extends ExtendedRepository<DocEntity, Long> {

    /**
     * 根据对象 ID 查询文档对象内容
     *
     * @param objectId 对象 ID
     * @return 文档对象内容
     */
    DocEntity findByObjectId(String objectId);

    /**
     * 根据名称查询文档对象内容
     *
     * @param name 名称
     * @return 文档对象内容
     */
    DocEntity findByName(String name);

    /**
     * 根据对象 ID 列表批量查询
     *
     * @param objectIds 对象 ID 列表
     * @return 文档对象内容列表
     */
    List<DocEntity> findByObjectIdIn(List<String> objectIds);

    /**
     * 根据名称列表批量查询
     *
     * @param names 名称列表
     * @return 文档对象内容列表
     */
    List<DocEntity> findByNameIn(List<String> names);
}