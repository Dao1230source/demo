package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * 注解文档元素
 * <p>
 * 表示 Java 注解的文档信息，包含注解名称和注解成员信息。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AnnotationDocElement extends DocElement {

    /**
     * 注解名称
     */
    private String annotationName;

    /**
     * 注解成员（键值对形式）
     */
    private Map<String, String> annotationMembers;

    /**
     * 获取元素的唯一标识
     * <p>
     * 使用注解名称作为 ID
     * </p>
     *
     * @return 注解名称
     */
    @Override
    public @NonNull String getId() {
        return annotationName;
    }

    /**
     * 获取父元素 ID
     * <p>
     * 注解元素没有父元素，返回 null
     * </p>
     *
     * @return null
     */
    @Override
    public String getParentId() {
        return null;
    }
}