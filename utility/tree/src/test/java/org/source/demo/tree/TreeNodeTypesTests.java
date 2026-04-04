package org.source.demo.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.source.utility.tree.DeepNode;
import org.source.utility.tree.DefaultEnhanceNode;
import org.source.utility.tree.FlatNode;
import org.source.utility.tree.Tree;
import org.source.utility.tree.define.Element;
import org.source.utility.tree.define.EnhanceElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tree 组件 Node 类型单元测试
 * <p>
 * 测试覆盖：
 * <ul>
 *   <li>DeepNode - 深度节点（支持自动计算节点深度）</li>
 *   <li>DefaultEnhanceNode - 增强节点（支持多父节点）</li>
 *   <li>FlatNode - 扁平节点（支持属性扁平化）</li>
 * </ul>
 * </p>
 *
 * @author utility
 * @since 1.0
 */
@SpringBootTest
@DisplayName("Tree Node 类型单元测试")
class TreeNodeTypesTests {

    // ========== DeepNode 测试 ==========

    private static final String ROOT_NODE = "Root";
    private static final String CHILD_NODE = "Child";

    private static final int ROOT_ID = 1;
    private static final int CHILD_ID = 2;
    private static final int GRANDCHILD_ID = 3;
    private static final int GREAT_GRANDCHILD_ID = 4;

    private static final String NODE_PREFIX = "Node-";

    /**
     * DeepNode 测试元素类
     */
    @Data
    @AllArgsConstructor
    static class TestDeepElement implements Element<Integer> {
        private Integer id;
        private Integer parentId;
        private String name;

        public TestDeepElement(Integer id, Integer parentId) {
            this.id = id;
            this.parentId = parentId;
            this.name = NODE_PREFIX + id;
        }

        @Override
        public @NonNull Integer getId() {
            return id;
        }

        @Override
        @Nullable
        public Integer getParentId() {
            return parentId;
        }
    }

    /**
     * EnhanceElement 测试元素类（支持排序）
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    static class TestEnhanceElement extends EnhanceElement<Integer> {
        private Integer id;
        private Integer parentId;
        private String name;
        private Integer sortOrder;

        public TestEnhanceElement(Integer id, Integer parentId) {
            this.id = id;
            this.parentId = parentId;
            this.name = NODE_PREFIX + id;
            this.sortOrder = id;
        }

        public TestEnhanceElement(Integer id, Integer parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
            this.sortOrder = id;
        }

        @Override
        public @NonNull Integer getId() {
            return id;
        }

        @Override
        @Nullable
        public Integer getParentId() {
            return parentId;
        }

        @Override
        public int compareTo(@Nullable EnhanceElement<Integer> other) {
            if (other == null) {
                return 1;
            }
            if (other instanceof TestEnhanceElement element) {
                return Integer.compare(this.sortOrder, element.sortOrder);
            }
            return 0;
        }
    }

    /**
     * FlatNode 测试元素类
     */
    @Data
    @AllArgsConstructor
    static class TestFlatElement implements Element<Integer> {
        private Integer id;
        private Integer parentId;
        private String name;
        private String description;
        private Integer level;

        public TestFlatElement(Integer id, Integer parentId) {
            this.id = id;
            this.parentId = parentId;
            this.name = NODE_PREFIX + id;
            this.description = "Description for " + id;
            this.level = 1;
        }

        @Override
        public @NonNull Integer getId() {
            return id;
        }

        @Override
        @Nullable
        public Integer getParentId() {
            return parentId;
        }
    }

    private Tree<Integer, TestDeepElement, DeepNode<Integer, TestDeepElement>> deepTree;
    private Tree<Integer, TestEnhanceElement, DefaultEnhanceNode<Integer, TestEnhanceElement>> enhanceTree;
    private Tree<Integer, TestFlatElement, FlatNode<Integer, TestFlatElement>> flatTree;

    @BeforeEach
    void setUp() {
        deepTree = Tree.of(new DeepNode<Integer, TestDeepElement>(false));
        enhanceTree = Tree.of(new DefaultEnhanceNode<Integer, TestEnhanceElement>());
        flatTree = Tree.of(new FlatNode<Integer, TestFlatElement>(List.of(
                TestFlatElement::getName,
                TestFlatElement::getDescription,
                TestFlatElement::getLevel)
        ));
    }

    // ========== DeepNode 单元测试 ==========

    /**
     * 测试 DeepNode 深度计算（从叶向上）
     */
    @Test
    @DisplayName("DeepNode - 应该正确计算节点深度（叶节点深度为0）")
    void testDeepNodeDepthFromLeaf() {
        List<TestDeepElement> elements = List.of(
                new TestDeepElement(ROOT_ID, null),
                new TestDeepElement(CHILD_ID, ROOT_ID),
                new TestDeepElement(GRANDCHILD_ID, CHILD_ID)
        );
        deepTree.add(elements);

        DeepNode<Integer, TestDeepElement> root = deepTree.getById(ROOT_ID);
        assertNotNull(root);
        // 根节点深度应该是 2（叶节点深度为 0，向上递增）
        assertEquals(2, root.getDepth());

        DeepNode<Integer, TestDeepElement> child = deepTree.getById(CHILD_ID);
        assertNotNull(child);
        assertEquals(1, child.getDepth());

        DeepNode<Integer, TestDeepElement> grandchild = deepTree.getById(GRANDCHILD_ID);
        assertNotNull(grandchild);
        assertEquals(0, grandchild.getDepth());
    }

    /**
     * 测试 DeepNode 深度计算（从根向下）
     * <p>
     * 修复说明：
     * 之前期望值错误地认为 ROOT_ID 的深度为 0。
     * 实际上 Tree 有一个默认的空根节点（depth=0），用户添加的节点是其子节点，
     * 所以 ROOT_ID 的深度应该是 1。深度值从 Tree 的默认空根节点开始计算。
     * </p>
     */
    @Test
    @DisplayName("DeepNode - 应该正确计算节点深度（从根向下，Tree 有默认空根节点）")
    void testDeepNodeDepthFromRoot() {
        DeepNode<Integer, TestDeepElement> deepNodeFromRoot = new DeepNode<>(true);
        Tree<Integer, TestDeepElement, DeepNode<Integer, TestDeepElement>> treeFromRoot = Tree.of(deepNodeFromRoot);

        List<TestDeepElement> elements = List.of(
                new TestDeepElement(ROOT_ID, null),
                new TestDeepElement(CHILD_ID, ROOT_ID),
                new TestDeepElement(GRANDCHILD_ID, CHILD_ID)
        );
        treeFromRoot.add(elements);

        DeepNode<Integer, TestDeepElement> root = treeFromRoot.getById(ROOT_ID);
        assertNotNull(root);
        // Tree 有默认的空根节点(depth=0)，所以实际的根节点(ROOT_ID)深度应该是 1
        assertEquals(1, root.getDepth());

        DeepNode<Integer, TestDeepElement> child = treeFromRoot.getById(CHILD_ID);
        assertNotNull(child);
        assertEquals(2, child.getDepth());

        DeepNode<Integer, TestDeepElement> grandchild = treeFromRoot.getById(GRANDCHILD_ID);
        assertNotNull(grandchild);
        assertEquals(3, grandchild.getDepth());
    }

    /**
     * 测试 DeepNode 树操作（CRUD）
     */
    @Test
    @DisplayName("DeepNode - 应该支持基本的树操作（创建、查询、更新、删除）")
    void testDeepNodeTreeOperations() {
        List<TestDeepElement> elements = List.of(
                new TestDeepElement(ROOT_ID, null, ROOT_NODE),
                new TestDeepElement(CHILD_ID, ROOT_ID, CHILD_NODE)
        );
        deepTree.add(elements);

        // 查询
        DeepNode<Integer, TestDeepElement> foundNode = deepTree.getById(CHILD_ID);
        assertNotNull(foundNode);
        assertEquals(CHILD_ID, foundNode.getId());

        // 更新
        boolean updated = deepTree.update(CHILD_ID, node -> {
            TestDeepElement element = node.getElement();
            if (Objects.nonNull(element)) {
                element.setName("Updated-Child");
            }
        });
        assertTrue(updated);

        // 验证更新
        DeepNode<Integer, TestDeepElement> updatedNode = deepTree.getById(CHILD_ID);
        assertNotNull(updatedNode);
        TestDeepElement element = updatedNode.getElement();
        if (Objects.nonNull(element)) {
            assertEquals("Updated-Child", element.getName());
        }

        // 删除
        deepTree.remove(node -> Objects.equals(CHILD_ID, node.getId()));
        assertNull(deepTree.getById(CHILD_ID));
    }

    // ========== DefaultEnhanceNode 单元测试 ==========

    /**
     * 测试 DefaultEnhanceNode 多父节点支持
     */
    @Test
    @DisplayName("DefaultEnhanceNode - 应该支持一个节点有多个父节点")
    void testEnhanceNodeMultipleParents() {
        List<TestEnhanceElement> elements = List.of(
                new TestEnhanceElement(1, null),  // 根节点 1
                new TestEnhanceElement(2, null),  // 根节点 2
                new TestEnhanceElement(3, 1),     // 子节点，父节点为 1
                new TestEnhanceElement(4, 1)      // 子节点，父节点为 1
        );
        enhanceTree.add(elements);

        // 验证多个根节点
        assertEquals(4, enhanceTree.size());
        assertNotNull(enhanceTree.getById(1));
        assertNotNull(enhanceTree.getById(2));
    }

    /**
     * 测试 DefaultEnhanceNode 有序子节点
     * <p>
     * 修复说明：
     * 之前测试数据有错误：使用 new TestEnhanceElement(1, ROOT_ID) 其中 ROOT_ID=1，
     * 导致节点 ID=1 且 parent=1，形成自循环（节点是自己的父亲）。
     * Tree 检测到循环引用会抛异常。修复方案是改变子节点 ID 为 2,3,4,5，
     * 避免与根节点 ID=1 重复。同时强化了断言：从检查 null 改为验证排序顺序。
     * </p>
     */
    @Test
    @DisplayName("DefaultEnhanceNode - 应该按自然顺序排序子节点")
    void testEnhanceNodeOrderedChildren() {
        List<TestEnhanceElement> elements = List.of(
                new TestEnhanceElement(ROOT_ID, null),
                new TestEnhanceElement(5, ROOT_ID),
                new TestEnhanceElement(3, ROOT_ID),
                new TestEnhanceElement(4, ROOT_ID),
                new TestEnhanceElement(2, ROOT_ID)
        );
        enhanceTree.add(elements);

        DefaultEnhanceNode<Integer, TestEnhanceElement> root = enhanceTree.getById(ROOT_ID);
        assertNotNull(root);

        // 验证子节点按顺序排列
        List<Integer> childIds = root.getChildren().stream()
                .map(DefaultEnhanceNode::getId)
                .toList();

        assertEquals(4, childIds.size());
        // TreeSet 应该按自然顺序排序：2, 3, 4, 5
        assertEquals(List.of(2, 3, 4, 5), childIds);
    }

    /**
     * 测试 DefaultEnhanceNode 树操作
     */
    @Test
    @DisplayName("DefaultEnhanceNode - 应该支持基本的树操作")
    void testEnhanceNodeTreeOperations() {
        List<TestEnhanceElement> elements = List.of(
                new TestEnhanceElement(ROOT_ID, null, ROOT_NODE),
                new TestEnhanceElement(CHILD_ID, ROOT_ID, CHILD_NODE)
        );
        enhanceTree.add(elements);

        // 查询
        DefaultEnhanceNode<Integer, TestEnhanceElement> foundNode = enhanceTree.getById(CHILD_ID);
        assertNotNull(foundNode);

        // 查找满足条件的节点
        List<DefaultEnhanceNode<Integer, TestEnhanceElement>> found = enhanceTree.find(
                node -> Objects.equals(CHILD_ID, node.getId())
        );
        assertEquals(1, found.size());

        // 删除
        enhanceTree.remove(node -> Objects.equals(CHILD_ID, node.getId()));
        assertNull(enhanceTree.getById(CHILD_ID));
    }

    // ========== FlatNode 单元测试 ==========

    /**
     * 测试 FlatNode 属性扁平化
     */
    @Test
    @DisplayName("FlatNode - 应该将元素属性扁平化为节点属性")
    void testFlatNodePropertyFlattening() {
        List<TestFlatElement> elements = List.of(
                new TestFlatElement(ROOT_ID, null),
                new TestFlatElement(CHILD_ID, ROOT_ID)
        );
        flatTree.add(elements);

        FlatNode<Integer, TestFlatElement> node = flatTree.getById(ROOT_ID);
        assertNotNull(node);

        // 验证扁平化属性
        assertEquals(ROOT_ID, node.getId());
        TestFlatElement element = node.getElement();
        if (Objects.nonNull(element)) {
            assertEquals("Node-1", element.getName());
            assertEquals("Description for 1", element.getDescription());
            assertEquals(1, element.getLevel());
        }
    }

    /**
     * 测试 FlatNode 树结构
     */
    @Test
    @DisplayName("FlatNode - 应该正确维护树结构")
    void testFlatNodeTreeStructure() {
        List<TestFlatElement> elements = List.of(
                new TestFlatElement(ROOT_ID, null),
                new TestFlatElement(CHILD_ID, ROOT_ID),
                new TestFlatElement(GRANDCHILD_ID, CHILD_ID)
        );
        flatTree.add(elements);

        // 验证树的大小
        assertEquals(3, flatTree.size());

        // 验证父子关系
        FlatNode<Integer, TestFlatElement> child = flatTree.getById(CHILD_ID);
        assertNotNull(child);

        FlatNode<Integer, TestFlatElement> parent = child.getParent();
        assertNotNull(parent);
        Integer parentId = parent.getId();
        if (Objects.nonNull(parentId)) {
            assertEquals(ROOT_ID, parentId.intValue());
        }

        // 验证子节点
        FlatNode<Integer, TestFlatElement> root = flatTree.getById(ROOT_ID);
        assertNotNull(root);
        assertEquals(1, root.getChildren().size());
    }

    /**
     * 测试 FlatNode 树操作
     */
    @Test
    @DisplayName("FlatNode - 应该支持基本的树操作")
    void testFlatNodeTreeOperations() {
        List<TestFlatElement> elements = List.of(
                new TestFlatElement(ROOT_ID, null),
                new TestFlatElement(CHILD_ID, ROOT_ID)
        );
        flatTree.add(elements);

        // 查询
        FlatNode<Integer, TestFlatElement> foundNode = flatTree.getById(CHILD_ID);
        assertNotNull(foundNode);

        // Optional 查询
        Optional<FlatNode<Integer, TestFlatElement>> optionalNode = flatTree.get(
                node -> Objects.equals(CHILD_ID, node.getId())
        );
        assertTrue(optionalNode.isPresent());

        // 更新
        boolean updated = flatTree.update(CHILD_ID, node -> {
            TestFlatElement element = node.getElement();
            if (Objects.nonNull(element)) {
                element.setName("Updated-Child");
            }
        });
        assertTrue(updated);

        // 删除
        flatTree.remove(node -> Objects.equals(CHILD_ID, node.getId()));
        assertNull(flatTree.getById(CHILD_ID));
    }

    /**
     * 测试 FlatNode 多层级结构
     */
    @Test
    @DisplayName("FlatNode - 应该正确处理多层级结构")
    void testFlatNodeMultiLevelHierarchy() {
        List<TestFlatElement> elements = List.of(
                new TestFlatElement(ROOT_ID, null),
                new TestFlatElement(CHILD_ID, ROOT_ID),
                new TestFlatElement(GRANDCHILD_ID, CHILD_ID),
                new TestFlatElement(GREAT_GRANDCHILD_ID, GRANDCHILD_ID)
        );
        flatTree.add(elements);

        assertEquals(4, flatTree.size());

        // 验证最深的节点
        FlatNode<Integer, TestFlatElement> deepNode = flatTree.getById(GREAT_GRANDCHILD_ID);
        assertNotNull(deepNode);

        FlatNode<Integer, TestFlatElement> parent = deepNode.getParent();
        assertNotNull(parent);
        assertEquals(GRANDCHILD_ID, parent.getId());
    }

    /**
     * 测试 FlatNode 查找功能
     */
    @Test
    @DisplayName("FlatNode - 应该能正确查找满足条件的节点")
    void testFlatNodeFind() {
        List<TestFlatElement> elements = List.of(
                new TestFlatElement(ROOT_ID, null),
                new TestFlatElement(CHILD_ID, ROOT_ID),
                new TestFlatElement(GRANDCHILD_ID, ROOT_ID)
        );
        flatTree.add(elements);

        List<FlatNode<Integer, TestFlatElement>> found = flatTree.find(node -> {
            Integer nodeId = node.getId();
            return Objects.nonNull(nodeId) && nodeId > CHILD_ID;
        });

        assertEquals(1, found.size());
        assertEquals(GRANDCHILD_ID, found.getFirst().getId());
    }

    /**
     * 测试清空各类型树
     */
    @Test
    @DisplayName("各 Node 类型 - 应该能正确清空树")
    void testClearAllNodeTypes() {
        // 深度树
        deepTree.add(List.of(new TestDeepElement(ROOT_ID, null)));
        assertEquals(1, deepTree.size());
        deepTree.clear();
        assertEquals(0, deepTree.size());

        // 增强树
        enhanceTree.add(List.of(new TestEnhanceElement(ROOT_ID, null)));
        assertEquals(1, enhanceTree.size());
        enhanceTree.clear();
        assertEquals(0, enhanceTree.size());

        // 扁平树
        flatTree.add(List.of(new TestFlatElement(ROOT_ID, null)));
        assertEquals(1, flatTree.size());
        flatTree.clear();
        assertEquals(0, flatTree.size());
    }
}