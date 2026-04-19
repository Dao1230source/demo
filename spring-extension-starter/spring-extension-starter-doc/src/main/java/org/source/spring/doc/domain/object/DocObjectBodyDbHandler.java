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

    /**
     * 文档对象内容数据仓库
     */
    private final DocObjectBodyRepository repository;

    /**
     * 构造文档对象内容数据库处理器
     *
     * @param repository 文档对象内容数据仓库
     */
    public DocObjectBodyDbHandler(DocObjectBodyRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建新的文档对象内容实体
     *
     * @return 新的 DocObjectBodyEntity 实例
     */
    @Override
    public DocObjectBodyEntity newObjectBodyEntity() {
        return new DocObjectBodyEntity();
    }

    /**
     * 根据对象 ID 集合查找文档对象内容
     *
     * @param objectIds 对象 ID 集合
     * @return 文档对象内容列表
     */
    @Override
    public List<DocObjectBodyEntity> findObjectBodies(Collection<String> objectIds) {
        if (objectIds == null || objectIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(objectIds.stream().toList());
    }

    /**
     * 根据键集合查找文档对象内容
     *
     * @param keys 键集合
     * @return 文档对象内容列表
     */
    @Override
    public List<DocObjectBodyEntity> findObjectBodiesByKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByObjectIdIn(keys.stream().toList());
    }

    /**
     * 批量保存文档对象内容
     *
     * @param objectBodyEntities 文档对象内容实体集合
     */
    @Override
    public void saveObjectBodies(Collection<DocObjectBodyEntity> objectBodyEntities) {
        if (objectBodyEntities == null || objectBodyEntities.isEmpty()) {
            return;
        }
        repository.saveAll(objectBodyEntities);
    }

    /**
     * 移除文档对象内容（暂不实现）
     *
     * @param objectIds 对象 ID 集合
     */
    @Override
    public void removeObjectBodies(Collection<String> objectIds) {
        // 暂不实现删除
    }

    /**
     * 从文档对象内容实体提取键
     *
     * @param objectBodyEntity 文档对象内容实体
     * @return 对象 ID
     */
    @Override
    public String objectBodyToKey(DocObjectBodyEntity objectBodyEntity) {
        return objectBodyEntity != null ? objectBodyEntity.getObjectId() : null;
    }

    /**
     * 从值对象提取键
     *
     * @param value 值对象
     * @return 对象 ID
     */
    @Override
    public String valueToKey(DocValue value) {
        return value != null ? value.getObjectId() : null;
    }

    /**
     * 从值对象提取父键
     *
     * @param value 值对象
     * @return 父键（本实现返回 null）
     */
    @Override
    public String valueToParentKey(DocValue value) {
        return null;
    }
}