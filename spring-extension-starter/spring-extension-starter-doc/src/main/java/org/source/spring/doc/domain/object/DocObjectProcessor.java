package org.source.spring.doc.domain.object;

import org.source.spring.doc.domain.entity.DocEntity;
import org.source.spring.doc.domain.entity.ObjectEntity;
import org.source.spring.doc.domain.entity.RelationEntity;
import org.source.spring.doc.domain.value.DocData;
import org.source.spring.object.ObjectElement;
import org.source.spring.object.ObjectNode;
import org.source.spring.object.definer.processor.AbstractObjectProcessor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 文档对象处理器
 */
@Component
public class DocObjectProcessor extends AbstractObjectProcessor<ObjectEntity, RelationEntity, DocEntity, DocData,
        DocObjectTypeEnum, DocObjectProcessor> {

    public DocObjectProcessor(DocObjectDbHandler objectDbHandler,
                              DocObjectBodyDbHandler objectBodyDbHandler,
                              DocRelationDbHandler relationDbHandler,
                              DocObjectTypeHandler objectTypeHandler) {
        super(objectDbHandler, objectBodyDbHandler, relationDbHandler, objectTypeHandler);
    }

    @Override
    public @Nullable String dataId(@Nullable DocData value) {
        return Objects.isNull(value) ? null : value.getName();
    }

    @Override
    public @Nullable String dataParentId(@Nullable DocData value) {
        return Objects.isNull(value) ? null : value.getParentName();
    }

    @Override
    public @Nullable String objectBodyId(@Nullable DocEntity docEntity) {
        return Objects.isNull(docEntity) ? null : docEntity.getName();
    }

    @Override
    public void extendObjectBodyEntity(DocEntity entity, ObjectElement<DocData> element) {
        entity.setName(element.getData().getName());
        entity.setParentName(element.getData().getParentName());
    }

    @Override
    public boolean nodeEquals(ObjectNode<DocData> n, ObjectNode<DocData> old) {
        ObjectElement<DocData> ne = n.getElement();
        ObjectElement<DocData> oe = old.getElement();
        return Objects.nonNull(ne) && Objects.nonNull(oe) && Objects.equals(ne.getData(), oe.getData());
    }
}