package org.source.spring.doc.domain.object;

import org.source.spring.doc.domain.entity.DocObjectBodyEntity;
import org.source.spring.doc.domain.entity.DocObjectEntity;
import org.source.spring.doc.domain.entity.DocRelationEntity;
import org.source.spring.object.AbstractObjectProcessor;
import org.springframework.stereotype.Component;

/**
 * 文档对象处理器
 * <p>
 * 继承自 {@link AbstractObjectProcessor}，用于处理文档对象的保存、查询、删除等操作。
 * 支持增量保存和版本管理。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Component
public class DocObjectProcessor extends AbstractObjectProcessor<
        DocObjectEntity,
        DocRelationEntity,
        DocObjectBodyEntity,
        DocValue,
        DocObjectTypeEnum,
        String> {

    public DocObjectProcessor(DocObjectDbHandler objectDbHandler,
                              DocObjectBodyDbHandler objectBodyDbHandler,
                              DocRelationDbHandler relationDbHandler,
                              DocObjectTypeHandler objectTypeHandler) {
        super(objectDbHandler, objectBodyDbHandler, relationDbHandler, objectTypeHandler);
    }
}