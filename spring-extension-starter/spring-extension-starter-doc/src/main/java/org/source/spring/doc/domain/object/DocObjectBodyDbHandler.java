package org.source.spring.doc.domain.object;

import org.source.spring.doc.domain.entity.DocObjectBodyEntity;
import org.source.spring.doc.domain.repository.DocObjectBodyRepository;
import org.source.spring.object.handler.ObjectBodyDbHandlerDefiner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 文档对象内容数据库处理器
 * <p>
 * 实现 {@link ObjectBodyDbHandlerDefiner} 接口，处理文档对象内容的数据库操作。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Component
public class DocObjectBodyDbHandler implements ObjectBodyDbHandlerDefiner<DocObjectBodyEntity, DocValue, String> {

    private final DocObjectBodyRepository repository;

    public DocObjectBodyDbHandler(DocObjectBodyRepository repository) {
        this.repository = repository;
    }

    @Override
    public DocObjectBodyEntity newObjectBodyEntity() {
        return new DocObjectBodyEntity();
    }

    @Override
    public List<DocObjectBodyEntity> findObjectBodies(Collection<String> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(objectIds.stream().toList());
    }

    @Override
    public List<DocObjectBodyEntity> findObjectBodiesByKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(keys.stream().toList());
    }

    @Override
    public void saveObjectBodies(Collection<DocObjectBodyEntity> objectBodyEntities) {
        if (objectBodyEntities == null || objectBodyEntities.isEmpty()) {
            return;
        }
        repository.saveAll(objectBodyEntities);
    }

    @Override
    public void removeObjectBodies(Collection<String> objectIds) {
        // 暂不实现删除
    }

    @Override
    public String objectBodyToKey(DocObjectBodyEntity objectBodyEntity) {
        return objectBodyEntity != null ? objectBodyEntity.getObjectId() : null;
    }

    @Override
    public String valueToKey(DocValue value) {
        return value != null ? value.getObjectId() : null;
    }

    @Override
    public String valueToParentKey(DocValue value) {
        return null;
    }
}