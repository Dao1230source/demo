package org.source.spring.doc.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内部类文档值对象
 * <p>
 * 表示嵌套类/内部类的文档信息，继承自 {@link DocData}
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InnerClassData extends DocData {

    /**
     * 内部类名
     */
    private String innerClassName;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 内部类类型（enum, interface, class）
     */
    private String type;

    /**
     * 修饰符
     */
    private String modifiers;

    /**
     * 是否已废弃（@Deprecated）
     */
    private boolean deprecated;

    /**
     * 废弃说明
     */
    private String deprecatedReason;
}