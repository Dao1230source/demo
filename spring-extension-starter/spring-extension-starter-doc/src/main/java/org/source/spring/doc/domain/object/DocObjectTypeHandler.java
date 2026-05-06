package org.source.spring.doc.domain.object;

import org.source.spring.doc.domain.entity.DocEntity;
import org.source.spring.doc.domain.entity.ObjectEntity;
import org.source.spring.doc.domain.entity.RelationEntity;
import org.source.spring.doc.domain.value.DocData;
import org.source.spring.object.definer.handler.AbstractObjectTypeHandler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档对象类型处理器
 */
@Component
public class DocObjectTypeHandler extends AbstractObjectTypeHandler<ObjectEntity, RelationEntity, DocEntity,
        DocData, DocObjectTypeEnum, DocObjectProcessor> {

    @Override
    protected @NonNull List<DocObjectTypeEnum> allObjectTypes() {
        return List.of(DocObjectTypeEnum.values());
    }
}