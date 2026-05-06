package org.source.spring.doc.domain.object;

import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.source.jpa.Condition;
import org.source.spring.doc.domain.entity.ObjectEntity;
import org.source.spring.doc.domain.repository.ObjectRepository;
import org.source.spring.object.definer.handler.ObjectDbHandlerDefiner;
import org.springframework.stereotype.Component;

import java.util.Collection;
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
@AllArgsConstructor
@Component
public class DocObjectDbHandler implements ObjectDbHandlerDefiner<ObjectEntity> {

    /**
     * 文档对象数据仓库
     */
    private final ObjectRepository repository;

    /**
     * 创建新的文档对象实体
     *
     * @return 新的 DocObjectEntity 实例
     */
    @Override
    public @NonNull ObjectEntity newObjectEntity() {
        return new ObjectEntity();
    }

    /**
     * 根据对象 ID 集合查找文档对象
     *
     * @param objectIds 对象 ID 集合
     * @return 文档对象列表
     */
    @Override
    public @NonNull List<ObjectEntity> findObjects(@NonNull Collection<String> objectIds) {
        return this.repository.findAll(new Condition<ObjectEntity>().in(ObjectEntity::getObjectId, objectIds));
    }

    /**
     * 批量保存文档对象
     *
     * @param objectEntities 文档对象实体集合
     */
    @Override
    public void saveObjects(@NonNull Collection<ObjectEntity> objectEntities) {
        repository.onDuplicateUpdateBatch(objectEntities);
    }

    /**
     * 删除文档对象（暂不实现）
     *
     * @param objectIds 对象 ID 集合
     */
    @Override
    public void deleteObjects(@NonNull Collection<String> objectIds) {
        // 暂不实现删除
    }

    /**
     * 移除文档对象（暂不实现）
     *
     * @param objectIds 对象 ID 集合
     */
    @Override
    public void removeObjects(@NonNull Collection<String> objectIds) {
        // 暂不实现移除
    }
}