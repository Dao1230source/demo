package org.source.spring.doc.domain.object;

import org.source.spring.doc.domain.entity.DocRelationEntity;
import org.source.spring.doc.domain.repository.DocRelationRepository;
import org.source.spring.object.handler.RelationDbHandlerDefiner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 文档对象关系数据库处理器
 * <p>
 * 实现 {@link RelationDbHandlerDefiner} 接口，处理文档对象关系的数据库操作。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Component
public class DocRelationDbHandler implements RelationDbHandlerDefiner<DocRelationEntity> {

    /**
     * 文档对象关系数据仓库
     */
    private final DocRelationRepository repository;

    /**
     * 构造文档对象关系数据库处理器
     *
     * @param repository 文档对象关系数据仓库
     */
    public DocRelationDbHandler(DocRelationRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建新的文档对象关系实体
     *
     * @return 新的 DocRelationEntity 实例
     */
    @Override
    public DocRelationEntity newRelationEntity() {
        return new DocRelationEntity();
    }

    /**
     * 根据对象 ID 集合查找关系
     *
     * @param objectIds 对象 ID 集合
     * @return 关系实体列表
     */
    @Override
    public List<DocRelationEntity> findRelationsByObjectIds(Collection<String> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(objectIds.stream().toList());
    }

    /**
     * 根据父对象 ID 集合查找关系
     *
     * @param parentObjectIds 父对象 ID 集合
     * @return 关系实体列表
     */
    @Override
    public List<DocRelationEntity> findRelationsByParentObjectIds(Collection<String> parentObjectIds) {
        if (parentObjectIds == null || parentObjectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByParentObjectIdIn(parentObjectIds.stream().toList());
    }

    /**
     * 根据归属 ID 集合查找关系
     *
     * @param belongIds 归属 ID 集合
     * @return 关系实体列表
     */
    @Override
    public List<DocRelationEntity> findRelationsByBelongIds(Collection<String> belongIds) {
        if (belongIds == null || belongIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(belongIds.stream().toList());
    }

    /**
     * 批量保存关系实体
     *
     * @param relationEntities 关系实体集合
     */
    @Override
    public void saveRelations(Collection<DocRelationEntity> relationEntities) {
        if (relationEntities == null || relationEntities.isEmpty()) {
            return;
        }
        repository.saveAll(relationEntities);
    }

    /**
     * 移除关系实体（暂不实现）
     *
     * @param objectIds 对象 ID 合
     */
    @Override
    public void removeRelations(Collection<String> objectIds) {
        // 暂不实现移除
    }
}