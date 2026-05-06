package org.source.spring.doc.domain.object;

import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.source.spring.doc.domain.entity.RelationEntity;
import org.source.spring.doc.domain.repository.RelationRepository;
import org.source.spring.object.definer.handler.RelationDbHandlerDefiner;
import org.springframework.stereotype.Component;

import java.util.Collection;
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
@AllArgsConstructor
@Component
public class DocRelationDbHandler implements RelationDbHandlerDefiner<RelationEntity> {

    /**
     * 文档对象关系数据仓库
     */
    private final RelationRepository repository;

    /**
     * 创建新的文档对象关系实体
     *
     * @return 新的 DocRelationEntity 实例
     */
    @Override
    public @NonNull RelationEntity newRelationEntity() {
        return new RelationEntity();
    }

    /**
     * 根据对象 ID 集合查找关系
     *
     * @param objectIds 对象 ID 集合
     * @return 关系实体列表
     */
    @Override
    public @NonNull List<RelationEntity> findRelationsByObjectIds(@NonNull Collection<String> objectIds) {
        return repository.findByObjectIdIn(objectIds.stream().toList());
    }

    /**
     * 根据父对象 ID 集合查找关系
     *
     * @param parentObjectIds 父对象 ID 集合
     * @return 关系实体列表
     */
    @Override
    public @NonNull List<RelationEntity> findRelationsByParentObjectIds(@NonNull Collection<String> parentObjectIds) {
        return repository.findByParentObjectIdIn(parentObjectIds.stream().toList());
    }

    /**
     * 根据归属 ID 集合查找关系
     *
     * @param belongIds 归属 ID 集合
     * @return 关系实体列表
     */
    @Override
    public @NonNull List<RelationEntity> findRelationsByBelongIds(@NonNull Collection<String> belongIds) {
        return repository.findByObjectIdIn(belongIds.stream().toList());
    }

    /**
     * 批量保存关系实体
     *
     * @param relationEntities 关系实体集合
     */
    @Override
    public void saveRelations(@NonNull Collection<RelationEntity> relationEntities) {
        repository.onDuplicateUpdateBatch(relationEntities);
    }

    /**
     * 移除关系实体（暂不实现）
     *
     * @param objectIds 对象 ID 合
     */
    @Override
    public void removeRelations(@NonNull Collection<String> objectIds) {
        // 暂不实现移除
    }
}