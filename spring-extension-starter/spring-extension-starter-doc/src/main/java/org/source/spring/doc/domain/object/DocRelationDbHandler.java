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

    private final DocRelationRepository repository;

    public DocRelationDbHandler(DocRelationRepository repository) {
        this.repository = repository;
    }

    @Override
    public DocRelationEntity newRelationEntity() {
        return new DocRelationEntity();
    }

    @Override
    public List<DocRelationEntity> findRelationsByObjectIds(Collection<String> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(objectIds.stream().toList());
    }

    @Override
    public List<DocRelationEntity> findRelationsByParentObjectIds(Collection<String> parentObjectIds) {
        if (parentObjectIds == null || parentObjectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByParentObjectIdIn(parentObjectIds.stream().toList());
    }

    @Override
    public List<DocRelationEntity> findRelationsByBelongIds(Collection<String> belongIds) {
        if (belongIds == null || belongIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(belongIds.stream().toList());
    }

    @Override
    public void saveRelations(Collection<DocRelationEntity> relationEntities) {
        if (relationEntities == null || relationEntities.isEmpty()) {
            return;
        }
        repository.saveAll(relationEntities);
    }

    @Override
    public void removeRelations(Collection<String> objectIds) {
        // 暂不实现移除
    }
}