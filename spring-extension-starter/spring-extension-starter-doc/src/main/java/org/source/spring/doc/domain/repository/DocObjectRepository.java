package org.source.spring.doc.domain.repository;

import org.source.jpa.repository.UnifiedJpaRepository;
import org.source.spring.doc.domain.entity.DocObjectEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文档对象数据仓库
 * <p>
 * 继承 {@link UnifiedJpaRepository}，提供文档对象实体的数据访问能力。
 * 封装了 JPA 的基础 CRUD 操作，并扩展了更多便捷的数据访问方法。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Repository
public interface DocObjectRepository extends UnifiedJpaRepository<DocObjectEntity, Long> {

    /**
     * 根据对象 ID 查询文档对象
     *
     * @param objectId 对象 ID
     * @return 文档对象
     */
    Optional<DocObjectEntity> findByObjectId(String objectId);

    /**
     * 根据对象类型查询文档对象列表
     *
     * @param objectType 对象类型编码
     * @return 文档对象列表
     */
    List<DocObjectEntity> findByObjectType(Integer objectType);

    /**
     * 根据关键词搜索文档对象
     *
     * @param keyword 关键词
     * @return 匹配的文档对象列表
     */
    List<DocObjectEntity> findByObjectIdContaining(String keyword);

    /**
     * 根据父 ID 查询子元素
     *
     * @param parentId 父对象 ID
     * @return 子元素列表
     */
    List<DocObjectEntity> findByParentId(String parentId);

    /**
     * 根据模块路径查询文档对象
     *
     * @param modulePath 模块路径
     * @return 文档对象列表
     */
    List<DocObjectEntity> findByModulePath(String modulePath);
}