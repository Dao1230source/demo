package org.source.spring.doc.domain.object;

import org.source.spring.doc.domain.entity.DocObjectBodyEntity;
import org.source.spring.object.ObjectElement;
import org.source.spring.object.enums.ObjectTypeDefiner;
import org.source.spring.object.handler.ObjectTypeHandlerDefiner;
import org.source.spring.object.AbstractObjectProcessor;
import org.source.spring.object.entity.ObjectBodyEntityDefiner;
import org.source.spring.object.entity.ObjectEntityDefiner;
import org.source.spring.object.entity.RelationEntityDefiner;
import org.source.spring.object.handler.ObjectBodyValueHandlerDefiner;
import org.source.utility.assign.Assign;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文档对象类型处理器
 * <p>
 * 实现 {@link ObjectTypeHandlerDefiner} 接口，处理文档对象类型的转换和操作。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Component
public class DocObjectTypeHandler implements ObjectTypeHandlerDefiner<DocObjectBodyEntity, DocValue, DocObjectTypeEnum> {

    private final Map<Integer, DocObjectTypeEnum> typeMap;
    private final Map<Class<? extends DocValue>, DocObjectTypeEnum> classMap;
    private final Map<Integer, AbstractObjectProcessor<ObjectEntityDefiner, RelationEntityDefiner,
            ObjectBodyEntityDefiner, ObjectBodyValueHandlerDefiner, ObjectTypeDefiner, Object>> processorMap = new ConcurrentHashMap<>();
    private final Map<Integer, Function<Collection<ObjectElement<ObjectBodyValueHandlerDefiner>>, 
            Assign<ObjectElement<ObjectBodyValueHandlerDefiner>>>> assignerMap = new ConcurrentHashMap<>();
    private final Map<Integer, Consumer<Collection<ObjectEntityDefiner>>> consumerMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public DocObjectTypeHandler() {
        this.typeMap = Arrays.stream(DocObjectTypeEnum.values())
                .collect(Collectors.toMap(DocObjectTypeEnum::getType, Function.identity()));
        this.classMap = (Map<Class<? extends DocValue>, DocObjectTypeEnum>) (Map<?, ?>) Arrays.stream(DocObjectTypeEnum.values())
                .collect(Collectors.toMap(DocObjectTypeEnum::getValueClass, Function.identity()));
    }

    @Override
    public Map<Integer, AbstractObjectProcessor<ObjectEntityDefiner, RelationEntityDefiner,
            ObjectBodyEntityDefiner, ObjectBodyValueHandlerDefiner, ObjectTypeDefiner, Object>> objectType2ProcessorMap() {
        return processorMap;
    }

    @Override
    public Map<Integer, DocObjectTypeEnum> type2ObjectTypeMap() {
        return typeMap;
    }

    @Override
    public Map<Class<? extends DocValue>, DocObjectTypeEnum> class2ObjectTypeMap() {
        return classMap;
    }

    @Override
    public Map<Integer, AbstractObjectProcessor<ObjectEntityDefiner, RelationEntityDefiner,
            ObjectBodyEntityDefiner, ObjectBodyValueHandlerDefiner, ObjectTypeDefiner, Object>> allTypeProcessors() {
        return processorMap;
    }

    @Override
    public Map<Integer, Function<Collection<ObjectElement<ObjectBodyValueHandlerDefiner>>,
            Assign<ObjectElement<ObjectBodyValueHandlerDefiner>>>> allTypeAssigners() {
        return assignerMap;
    }

    @Override
    public Map<Integer, Consumer<Collection<ObjectEntityDefiner>>> allTypeObjectConsumers() {
        return consumerMap;
    }
}