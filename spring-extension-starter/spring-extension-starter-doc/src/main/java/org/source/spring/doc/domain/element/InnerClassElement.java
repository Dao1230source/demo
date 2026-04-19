package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * 内部类文档元素
 * <p>
 * 表示嵌套类/内部类的文档信息
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InnerClassElement extends DocElement {

    /**
     * 内部类名
     */
    private String innerClassName;

    /**
     * 内部类全限定名
     */
    private String innerClassQualifiedName;

    /**
     * 外部类全限定名
     */
    private String outerClassQualifiedName;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 内部类类型（enum, interface, class）
     */
    private String type;

    @Override
    public @NonNull String getId() {
        return innerClassQualifiedName;
    }

    @Override
    public String getParentId() {
        return outerClassQualifiedName;
    }
}
