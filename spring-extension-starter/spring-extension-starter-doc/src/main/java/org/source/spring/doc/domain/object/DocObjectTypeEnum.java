package org.source.spring.doc.domain.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.source.spring.doc.domain.entity.DocEntity;
import org.source.spring.doc.domain.entity.ObjectEntity;
import org.source.spring.doc.domain.entity.RelationEntity;
import org.source.spring.doc.domain.value.*;
import org.source.spring.object.definer.enums.ObjectTypeDefiner;

/**
 * 文档对象类型枚举
 */
@Getter
@AllArgsConstructor
public enum DocObjectTypeEnum implements ObjectTypeDefiner<ObjectEntity, RelationEntity, DocEntity, DocData,
        DocObjectTypeEnum, DocObjectProcessor> {

    CLASS(1, "类文档", ClassDocData.class, DocObjectProcessor.class),
    METHOD(2, "方法文档", MethodDocData.class, DocObjectProcessor.class),
    SHARED_VARIABLE(3, "共用变量", SharedVariableData.class, DocObjectProcessor.class),
    MEMBER_VARIABLE(4, "成员变量", MemberVariableData.class, DocObjectProcessor.class),
    JPA_COLUMN_VARIABLE(5, "JPA列变量", JpaColumnVariableData.class, DocObjectProcessor.class),
    PARAMETER_VARIABLE(6, "方法入参", ParameterVariableData.class, DocObjectProcessor.class),
    REST_ENDPOINT(7, "REST接口", RestDocData.class, DocObjectProcessor.class),
    MODULE(8, "模块", ModuleDocData.class, DocObjectProcessor.class),
    INNER_CLASS(9, "内部类", InnerClassData.class, DocObjectProcessor.class),
    SPRING_BEAN(10, "SpringBean", SpringBeanData.class, DocObjectProcessor.class);

    private final Integer type;
    private final String desc;
    private final Class<? extends DocData> valueClass;
    private final Class<? extends DocObjectProcessor> objectProcessor;

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<DocData> getValueClass() {
        return (Class<DocData>) valueClass;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<DocObjectProcessor> getObjectProcessor() {
        return (Class<DocObjectProcessor>) objectProcessor;
    }
}