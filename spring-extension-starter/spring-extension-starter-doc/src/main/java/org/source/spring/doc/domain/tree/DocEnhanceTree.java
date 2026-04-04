package org.source.spring.doc.domain.tree;

import lombok.Getter;
import org.source.spring.doc.domain.element.ClassDocElement;
import org.source.spring.doc.domain.element.DocElement;
import org.source.spring.doc.domain.element.FieldDocElement;
import org.source.spring.doc.domain.element.MethodDocElement;
import org.source.spring.doc.domain.element.RestDocElement;
import org.source.utility.tree.EnhanceTree;

import java.util.List;
import java.util.Objects;

/**
 * 文档增强树
 * <p>
 * 基于 {@link EnhanceTree} 的文档树形结构管理器，
 * 提供文档元素的存储、查询和分类功能。
 * </p>
 * <p>
 * 支持快速按 ID 查询（O(1)）、条件过滤、按类型分类等操作。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@Getter
public class DocEnhanceTree {

    /**
     * 内部树形结构实例
     */
    private final EnhanceTree<String, DocElement, DocEnhanceNode> tree;

    /**
     * 构造函数 - 创建空的文档树
     */
    public DocEnhanceTree() {
        this.tree = EnhanceTree.of(new DocEnhanceNode());
    }

    /**
     * 添加单个文档元素到树中
     *
     * @param element 文档元素
     */
    public void addElement(DocElement element) {
        tree.add(List.of(element));
    }

    /**
     * 批量添加文档元素到树中
     *
     * @param elements 文档元素列表
     */
    public void addElements(List<DocElement> elements) {
        tree.add(elements);
    }

    /**
     * 根据 ID 获取文档元素
     * <p>
     * 时间复杂度 O(1)，最快的查询方式
     * </p>
     *
     * @param id 元素 ID
     * @return 文档元素，如果不存在则返回 null
     */
    public DocElement getElement(String id) {
        DocEnhanceNode node = tree.getById(id);
        return node != null ? node.getElement() : null;
    }

    /**
     * 获取树中所有文档元素
     *
     * @return 所有文档元素列表
     */
    public List<DocElement> getAllElements() {
        return tree.find(n -> Objects.nonNull(n.getElement()))
                .stream()
                .map(DocEnhanceNode::getElement)
                .toList();
    }

    /**
     * 获取所有类文档元素
     *
     * @return 类文档元素列表
     */
    public List<ClassDocElement> getClasses() {
        return tree.find(n -> Objects.nonNull(n.getElement()) && n.getElement() instanceof ClassDocElement)
                .stream()
                .map(n -> (ClassDocElement) n.getElement())
                .toList();
    }

    /**
     * 获取指定类的所有方法文档元素
     *
     * @param classQualifiedName 类的全限定名
     * @return 方法文档元素列表
     */
    public List<MethodDocElement> getMethodsOfClass(String classQualifiedName) {
        return tree.find(n -> {
                    DocElement e = n.getElement();
                    return Objects.nonNull(e) && e instanceof MethodDocElement method
                            && classQualifiedName.equals(method.getParentId());
                })
                .stream()
                .map(n -> (MethodDocElement) n.getElement())
                .toList();
    }

    /**
     * 获取指定类的所有字段文档元素
     *
     * @param classQualifiedName 类的全限定名
     * @return 字段文档元素列表
     */
    public List<FieldDocElement> getFieldsOfClass(String classQualifiedName) {
        return tree.find(n -> {
                    DocElement e = n.getElement();
                    return Objects.nonNull(e) && e instanceof FieldDocElement field
                            && classQualifiedName.equals(field.getParentId());
                })
                .stream()
                .map(n -> (FieldDocElement) n.getElement())
                .toList();
    }

    /**
     * 获取所有 REST 接口文档元素
     *
     * @return REST 接口文档元素列表
     */
    public List<RestDocElement> getRestEndpoints() {
        return tree.find(n -> Objects.nonNull(n.getElement()) && n.getElement() instanceof RestDocElement)
                .stream()
                .map(n -> (RestDocElement) n.getElement())
                .toList();
    }

    /**
     * 获取指定类的所有 REST 接口文档元素
     *
     * @param classPath 类路径（全限定名）
     * @return REST 接口文档元素列表
     */
    public List<RestDocElement> getRestEndpointsOfClass(String classPath) {
        return tree.find(n -> {
                    DocElement e = n.getElement();
                    return Objects.nonNull(e) && e instanceof RestDocElement rest
                            && classPath.equals(rest.getClassPath());
                })
                .stream()
                .map(n -> (RestDocElement) n.getElement())
                .toList();
    }

    /**
     * 获取树中节点总数
     *
     * @return 节点数量
     */
    public int size() {
        return tree.size();
    }
}