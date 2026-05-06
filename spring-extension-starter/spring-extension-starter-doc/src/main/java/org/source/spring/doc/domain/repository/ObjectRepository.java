package org.source.spring.doc.domain.repository;

import org.source.jpa.ExtendedRepository;
import org.source.spring.doc.domain.entity.ObjectEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文档对象数据仓库
 * <p>
 * 封装了 JPA 的基础 CRUD 操作，并扩展了更多便捷的数据访问方法。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Repository
public interface ObjectRepository extends ExtendedRepository<ObjectEntity, Long> {

    /**
     * 根据对象 ID 查询文档对象
     *
     * @param objectId 对象 ID
     * @return 文档对象
     */
    Optional<ObjectEntity> findByObjectId(String objectId);

    /**
     * 根据对象类型查询文档对象列表
     *
     * @param type 对象类型编码
     * @return 文档对象列表
     */
    List<ObjectEntity> findByType(Integer type);

    /**
     * 根据关键词搜索文档对象
     *
     * @param keyword 关键词
     * @return 匹配的文档对象列表
     */
    List<ObjectEntity> findByObjectIdContaining(String keyword);

    /**
     * 根据空间 ID 查询文档对象
     *
     * @param spaceId 空间 ID
     * @return 文档对象列表
     */
    List<ObjectEntity> findBySpaceId(String spaceId);
}