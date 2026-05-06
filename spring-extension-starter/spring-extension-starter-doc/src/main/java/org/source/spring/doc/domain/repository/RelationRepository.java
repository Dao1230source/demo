package org.source.spring.doc.domain.repository;

import org.source.jpa.ExtendedRepository;
import org.source.spring.doc.domain.entity.RelationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档对象关系数据仓库
 * <p>
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Repository
public interface RelationRepository extends ExtendedRepository<RelationEntity, Long> {

    /**
     * 根据对象 ID 查询关系列表
     *
     * @param objectId 对象 ID
     * @return 关系列表
     */
    List<RelationEntity> findByObjectId(String objectId);

    /**
     * 根据父对象 ID 查询关系列表
     *
     * @param parentObjectId 父对象 ID
     * @return 关系列表
     */
    List<RelationEntity> findByParentObjectId(String parentObjectId);

    /**
     * 根据对象 ID 列表批量查询关系
     *
     * @param objectIds 对象 ID 列表
     * @return 关系列表
     */
    List<RelationEntity> findByObjectIdIn(List<String> objectIds);

    /**
     * 根据父对象 ID 列表批量查询关系
     *
     * @param parentObjectIds 父对象 ID 列表
     * @return 关系列表
     */
    List<RelationEntity> findByParentObjectIdIn(List<String> parentObjectIds);
}