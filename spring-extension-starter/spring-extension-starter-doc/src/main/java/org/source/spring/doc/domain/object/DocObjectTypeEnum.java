package org.source.spring.doc.domain.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.source.spring.object.AbstractObjectProcessor;
import org.source.spring.object.entity.ObjectBodyEntityDefiner;
import org.source.spring.object.entity.ObjectEntityDefiner;
import org.source.spring.object.entity.RelationEntityDefiner;
import org.source.spring.object.enums.ObjectTypeDefiner;
import org.source.spring.object.handler.ObjectBodyValueHandlerDefiner;

/**
 * 文档对象类型枚举
 * <p>
 * 定义文档解析器支持的对象类型，实现 {@link ObjectTypeDefiner} 接口。
 * 每种类型对应一种文档元素。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum DocObjectTypeEnum implements ObjectTypeDefiner {

    /**
     * 类文档
     */
    CLASS(1, "类文档", DocValue.class, DocObjectProcessor.class),

    /**
     * 方法文档
     */
    METHOD(2, "方法文档", DocValue.class, DocObjectProcessor.class),

    /**
     * 共用变量
     */
    SHARED_VARIABLE(3, "共用变量", DocValue.class, DocObjectProcessor.class),

    /**
     * 成员变量
     */
    MEMBER_VARIABLE(4, "成员变量", DocValue.class, DocObjectProcessor.class),

    /**
     * JPA 列变量
     */
    JPA_COLUMN_VARIABLE(5, "JPA列变量", DocValue.class, DocObjectProcessor.class),

    /**
     * 方法入参
     */
    PARAMETER_VARIABLE(6, "方法入参", DocValue.class, DocObjectProcessor.class),

    /**
     * REST 接口
     */
    REST_ENDPOINT(7, "REST接口", DocValue.class, DocObjectProcessor.class),

    /**
     * 模块
     */
    MODULE(8, "模块", DocValue.class, DocObjectProcessor.class);

    private final Integer type;
    private final String desc;
    @SuppressWarnings("rawtypes")
    private final Class<? extends ObjectBodyValueHandlerDefiner> valueClass;
    @SuppressWarnings("rawtypes")
    private final Class<? extends AbstractObjectProcessor> objectProcessor;

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V extends ObjectBodyValueHandlerDefiner> Class<V> getValueClass() {
        return (Class<V>) valueClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O extends ObjectEntityDefiner, R extends RelationEntityDefiner, B extends ObjectBodyEntityDefiner,
            V extends ObjectBodyValueHandlerDefiner, T extends ObjectTypeDefiner, K>
    Class<? extends AbstractObjectProcessor<O, R, B, V, T, K>> getObjectProcessor() {
        return (Class<? extends AbstractObjectProcessor<O, R, B, V, T, K>>) objectProcessor;
    }

    /**
     * 根据类型值获取枚举
     *
     * @param type 类型值
     * @return 枚举值，未找到返回 null
     */
    public static DocObjectTypeEnum fromType(Integer type) {
        if (type == null) {
            return null;
        }
        for (DocObjectTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }
}