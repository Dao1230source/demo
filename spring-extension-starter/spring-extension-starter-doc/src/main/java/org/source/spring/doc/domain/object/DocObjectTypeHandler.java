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

    /**
     * 类型编码到枚举的映射
     */
    private final Map<Integer, DocObjectTypeEnum> typeMap;

    /**
     * 值类到枚举的映射
     */
    private final Map<Class<? extends DocValue>, DocObjectTypeEnum> classMap;

    /**
     * 类型到处理器对象的映射
     */
    private final Map<Integer, AbstractObjectProcessor<ObjectEntityDefiner, RelationEntityDefiner,
            ObjectBodyEntityDefiner, ObjectBodyValueHandlerDefiner, ObjectTypeDefiner, Object>> processorMap = new ConcurrentHashMap<>();

    /**
     * 类型到 Assigner 的映射
     */
    private final Map<Integer, Function<Collection<ObjectElement<ObjectBodyValueHandlerDefiner>>, 
            Assign<ObjectElement<ObjectBodyValueHandlerDefiner>>>> assignerMap = new ConcurrentHashMap<>();

    /**
     * 类型到 Consumer 的映射
     */
    private final Map<Integer, Consumer<Collection<ObjectEntityDefiner>>> consumerMap = new ConcurrentHashMap<>();

    /**
     * 构造文档对象类型处理器
     * <p>
     * 初始化类型映射和类映射
     * </p>
     */
    @SuppressWarnings("unchecked")
    public DocObjectTypeHandler() {
        this.typeMap = Arrays.stream(DocObjectTypeEnum.values())
                .collect(Collectors.toMap(DocObjectTypeEnum::getType, Function.identity()));
        this.classMap = (Map<Class<? extends DocValue>, DocObjectTypeEnum>) (Map<?, ?>) Arrays.stream(DocObjectTypeEnum.values())
                .collect(Collectors.toMap(DocObjectTypeEnum::getValueClass, Function.identity()));
    }

    /**
     * 获取类型到处理器的映射
     *
     * @return 类型到处理器的映射
     */
    @Override
    public Map<Integer, AbstractObjectProcessor<ObjectEntityDefiner, RelationEntityDefiner,
            ObjectBodyEntityDefiner, ObjectBodyValueHandlerDefiner, ObjectTypeDefiner, Object>> objectType2ProcessorMap() {
        return processorMap;
    }

    /**
     * 获取类型到枚举的映射
     *
     * @return 类型到枚举的映射
     */
    @Override
    public Map<Integer, DocObjectTypeEnum> type2ObjectTypeMap() {
        return typeMap;
    }

    /**
     * 获取类到枚举的映射
     *
     * @return 类到枚举的映射
     */
    @Override
    public Map<Class<? extends DocValue>, DocObjectTypeEnum> class2ObjectTypeMap() {
        return classMap;
    }

    /**
     * 获取所有类型的处理器
     *
     * @return 所有类型的处理器映射
     */
    @Override
    public Map<Integer, AbstractObjectProcessor<ObjectEntityDefiner, RelationEntityDefiner,
            ObjectBodyEntityDefiner, ObjectBodyValueHandlerDefiner, ObjectTypeDefiner, Object>> allTypeProcessors() {
        return processorMap;
    }

    /**
     * 获取所有类型的 Assigner
     *
     * @return 所有类型的 Assigner 映射
     */
    @Override
    public Map<Integer, Function<Collection<ObjectElement<ObjectBodyValueHandlerDefiner>>,
            Assign<ObjectElement<ObjectBodyValueHandlerDefiner>>>> allTypeAssigners() {
        return assignerMap;
    }

    /**
     * 获取所有类型的 Consumer
     *
     * @return 所有类型的 Consumer 映射
     */
    @Override
    public Map<Integer, Consumer<Collection<ObjectEntityDefiner>>> allTypeObjectConsumers() {
        return consumerMap;
    }
}