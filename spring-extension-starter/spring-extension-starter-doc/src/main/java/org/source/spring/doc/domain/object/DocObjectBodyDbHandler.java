package org.source.spring.doc.domain.object;

import lombok.AllArgsConstructor;
import org.source.jpa.Condition;
import org.source.spring.doc.domain.entity.DocEntity;
import org.source.spring.doc.domain.repository.DocRepository;
import org.source.spring.object.definer.handler.ObjectBodyDbHandlerDefiner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 文档对象内容数据库处理器
 */
@AllArgsConstructor
@Component
public class DocObjectBodyDbHandler implements ObjectBodyDbHandlerDefiner<DocEntity> {

    private final DocRepository repository;

    @Override
    public DocEntity newObjectBodyEntity() {
        return new DocEntity();
    }

    @Override
    public List<DocEntity> findObjectBodies(Collection<String> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(objectIds.stream().toList());
    }

    @Override
    public List<DocEntity> findObjectBodiesByKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findAll(new Condition<DocEntity>().in(DocEntity::getName, keys));
    }

    @Override
    public void saveObjectBodies(Collection<DocEntity> objectBodyEntities) {
        if (objectBodyEntities == null || objectBodyEntities.isEmpty()) {
            return;
        }
        repository.onDuplicateUpdateBatch(objectBodyEntities);
    }

    @Override
    public void removeObjectBodies(Collection<String> objectIds) {
        // 暂不实现删除
    }
}