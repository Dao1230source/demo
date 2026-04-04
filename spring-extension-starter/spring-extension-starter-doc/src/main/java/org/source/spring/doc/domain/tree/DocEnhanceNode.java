package org.source.spring.doc.domain.tree;

import org.source.spring.doc.domain.element.DocElement;
import org.source.utility.tree.EnhanceNode;
import org.springframework.lang.NonNull;

/**
 * 文档增强节点
 * <p>
 * Doc 专用的 {@link EnhanceNode} 实现，
 * 用于构建文档元素的树形结构，支持多父节点（DAG）。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
public class DocEnhanceNode extends EnhanceNode<String, DocElement, DocEnhanceNode> {

    /**
     * 创建空节点实例
     * <p>
     * 用于树形结构的初始化和节点创建
     * </p>
     *
     * @return 空的 DocEnhanceNode 实例
     */
    @Override
    public @NonNull DocEnhanceNode emptyNode() {
        return new DocEnhanceNode();
    }
}