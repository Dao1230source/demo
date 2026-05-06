package org.source.spring.doc.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 共用变量值对象
 * <p>
 * 表示变量的共用定义，同一变量在多处使用时共享同一个实例。
 * 继承自 {@link DocData}。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SharedVariableData extends DocData {

    /**
     * 变量名
     */
    private String variableName;

    /**
     * 变量类型（简单名称）
     */
    private String variableType;

    /**
     * 变量类型的全限定名
     */
    private String variableTypeQualifiedName;

    /**
     * 是否为原始类型
     */
    private boolean primitive;
}