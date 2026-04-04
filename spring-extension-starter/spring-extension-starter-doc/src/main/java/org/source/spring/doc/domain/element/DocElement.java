package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.source.utility.tree.define.EnhanceElement;

/**
 * 文档元素基类
 * <p>
 * 所有文档元素（类、方法、字段、REST接口等）的抽象基类，
 * 继承自 {@link EnhanceElement}，提供树形结构支持。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class DocElement extends EnhanceElement<String> {

    /**
     * 比较两个文档元素
     * <p>
     * 基于 ID 进行字符串比较，用于树形结构排序
     * </p>
     *
     * @param other 另一个文档元素
     * @return 比较结果，如果 other 为 null 则返回 1
     */
    @Override
    public int compareTo(org.source.utility.tree.define.EnhanceElement<String> other) {
        if (other == null) {
            return 1;
        }
        return this.getId().compareTo(other.getId());
    }
}