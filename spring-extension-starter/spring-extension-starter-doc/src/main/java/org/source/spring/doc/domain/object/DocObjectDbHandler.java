package org.source.spring.doc.domain.object;

import org.source.spring.doc.domain.entity.DocObjectEntity;
import org.source.spring.doc.domain.repository.DocObjectRepository;
import org.source.spring.object.handler.ObjectDbHandlerDefiner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 文档对象数据库处理器
 * <p>
 * 实现 {@link ObjectDbHandlerDefiner} 接口，处理文档对象的数据库操作。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Component
public class DocObjectDbHandler implements ObjectDbHandlerDefiner<DocObjectEntity> {

    private final DocObjectRepository repository;

    public DocObjectDbHandler(DocObjectRepository repository) {
        this.repository = repository;
    }

    @Override
    public DocObjectEntity newObjectEntity() {
        return new DocObjectEntity();
    }

    @Override
    public List<DocObjectEntity> findObjects(Collection<String> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    @Override
    public void saveObjects(Collection<DocObjectEntity> objectEntities) {
        if (objectEntities == null || objectEntities.isEmpty()) {
            return;
        }
        repository.saveAll(objectEntities);
    }

    @Override
    public void deleteObjects(Collection<String> objectIds) {
        // 暂不实现删除
    }

    @Override
    public void removeObjects(Collection<String> objectIds) {
        // 暂不实现移除
    }
}