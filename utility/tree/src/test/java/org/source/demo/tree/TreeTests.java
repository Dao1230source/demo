package org.source.demo.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.source.utility.enums.BaseExceptionEnum;
import org.source.utility.tree.DefaultNode;
import org.source.utility.tree.Tree;
import org.source.utility.tree.define.Element;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tree 组件单元测试
 * <p>
 * 测试覆盖：
 * <ul>
 *   <li>基本的树结构创建和节点添加</li>
 *   <li>节点查询、更新、删除操作</li>
 *   <li>并发安全性</li>
 *   <li>循环引用检测</li>
 *   <li>合并策略处理</li>
 * </ul>
 * </p>
 *
 * @author utility
 * @since 1.0
 */
@SpringBootTest
@DisplayName("Tree 组件单元测试")
class TreeTests {

    private static final String ROOT_NODE = "Root";
    private static final String CHILD_NODE = "Child";
    private static final String UPDATED_NODE = "Updated-Node";
    private static final String MODIFIED_NODE = "Modified";
    private static final String BATCH_UPDATED = "Batch-Updated";
    private static final String NODE_PREFIX = "Node-";

    private static final int ROOT_ID = 1;
    private static final int CHILD_ID = 2;
    private static final int GRANDCHILD_ID = 3;
    private static final int GREAT_GRANDCHILD_ID = 4;
    private static final int SINGLE_NODE_ID = 100;
    private static final int NON_EXISTENT_ID = 999;
    private static final int BATCH_SIZE = 5;
    private static final int CONCURRENT_ITERATIONS = 100;
    private static final int INITIAL_CONCURRENT_NODES = 10;

    /**
     * 测试元素类
     */
    @Data
    @AllArgsConstructor
    static class TestElement implements Element<Integer> {
        private Integer id;
        private Integer parentId;
        private String name;

        public TestElement(Integer id, Integer parentId) {
            this.id = id;
            this.parentId = parentId;
            this.name = NODE_PREFIX + id;
        }

        @Override
        public @NotNull Integer getId() {
            return id;
        }

        @Override
        @Nullable
        public Integer getParentId() {
            return parentId;
        }
    }

    private Tree<Integer, TestElement, DefaultNode<Integer, TestElement>> tree;

    @BeforeEach
    void setUp() {
        tree = Tree.of(new DefaultNode<Integer, TestElement>());
    }

    /**
     * 测试1: 创建树和添加节点
     */
    @Test
    @DisplayName("应该成功创建树并添加节点")
    void testTreeCreationAndAddNodes() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, ROOT_ID),
                new TestElement(GREAT_GRANDCHILD_ID, CHILD_ID)
        );

        tree.add(elements);

        assertEquals(4, tree.size());
        assertNotNull(tree.getById(ROOT_ID));
        assertNotNull(tree.getById(GREAT_GRANDCHILD_ID));
        assertNull(tree.getById(NON_EXISTENT_ID));
    }

    /**
     * 测试2: 查找节点
     */
    @Test
    @DisplayName("应该能正确查找满足条件的节点")
    void testFindNodes() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, ROOT_ID),
                new TestElement(GREAT_GRANDCHILD_ID, CHILD_ID)
        );
        tree.add(elements);

        List<DefaultNode<Integer, TestElement>> found = tree.find(node -> {
            Integer nodeId = node.getId();
            return Objects.nonNull(nodeId) && nodeId > CHILD_ID;
        });

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(n -> Objects.equals(GRANDCHILD_ID, n.getId())));
        assertTrue(found.stream().anyMatch(n -> Objects.equals(GREAT_GRANDCHILD_ID, n.getId())));
    }

    /**
     * 测试3: 获取单个节点
     */
    @Test
    @DisplayName("应该能通过条件获取单个节点的Optional包装")
    void testGetSingleNode() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID)
        );
        tree.add(elements);

        Optional<DefaultNode<Integer, TestElement>> found = tree.get(
                node -> Objects.equals(CHILD_ID, node.getId())
        );

        assertTrue(found.isPresent());
        found.ifPresent(node -> {
            Integer nodeId = node.getId();
            if (Objects.nonNull(nodeId)) {
                assertEquals(CHILD_ID, nodeId);
            }
        });
    }

    /**
     * 测试4: 通过 ID 获取节点
     */
    @Test
    @DisplayName("应该能通过ID直接获取节点")
    void testGetNodeById() {
        List<TestElement> elements = List.of(
                new TestElement(10, null),
                new TestElement(20, 10)
        );
        tree.add(elements);

        DefaultNode<Integer, TestElement> node = tree.getById(20);

        assertNotNull(node);
        Integer nodeId = node.getId();
        Integer nodeParentId = node.getParentId();
        if (Objects.nonNull(nodeId) && Objects.nonNull(nodeParentId)) {
            assertEquals(20, nodeId.intValue());
            assertEquals(10, nodeParentId.intValue());
        }
    }

    /**
     * 测试5: 删除节点
     * <p>
     * 修复说明：
     * 之前测试执行时触发 NullPointerException：
     * "Cannot invoke Integer.intValue() because the return value of Map.get(Object) is null"
     * 原因在 UnionFind.union() 方法中，使用 rank.get(key) 在 key 不存在时返回 null。
     * 当 Tree.rebuildUnionFind() 重建并查集时，find() 的路径压缩可能改变根节点，
     * 导致新根节点在 rank map 中不存在。修复方案：将 rank.get() 改为 rank.getOrDefault(, 0)，
     * 缺失键时返回安全的默认值 0。
     * </p>
     */
    @Test
    @DisplayName("删除节点时应该同时删除其子节点")
    void testRemoveNodes() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, ROOT_ID),
                new TestElement(GREAT_GRANDCHILD_ID, CHILD_ID)
        );
        tree.add(elements);
        assertEquals(4, tree.size());

        tree.remove(node -> Objects.equals(CHILD_ID, node.getId()));

        assertEquals(2, tree.size());
        assertNull(tree.getById(CHILD_ID));
        assertNull(tree.getById(GREAT_GRANDCHILD_ID));
        assertNotNull(tree.getById(ROOT_ID));
        assertNotNull(tree.getById(GRANDCHILD_ID));
    }

    /**
     * 测试6: 更新节点
     */
    @Test
    @DisplayName("应该能成功更新节点属性")
    void testUpdateNode() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID)
        );
        tree.add(elements);

        boolean updated = tree.update(CHILD_ID, node -> {
            TestElement element = node.getElement();
            if (Objects.nonNull(element)) {
                element.setName(UPDATED_NODE);
            }
        });

        assertTrue(updated);
        DefaultNode<Integer, TestElement> node = tree.getById(CHILD_ID);
        assertNotNull(node);
        TestElement element = node.getElement();
        if (Objects.nonNull(element)) {
            assertEquals(UPDATED_NODE, element.getName());
        }
    }

    /**
     * 测试7: 清空树
     */
    @Test
    @DisplayName("应该能清空树中的所有节点")
    void testClearTree() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, ROOT_ID)
        );
        tree.add(elements);
        assertEquals(3, tree.size());

        tree.clear();

        assertEquals(0, tree.size());
        assertNull(tree.getById(ROOT_ID));
    }

    /**
     * 测试8: 父子关系验证
     */
    @Test
    @DisplayName("应该能正确维护父子关系")
    void testParentChildRelationship() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, CHILD_ID)
        );
        tree.add(elements);

        DefaultNode<Integer, TestElement> child = tree.getById(CHILD_ID);
        assertNotNull(child);

        DefaultNode<Integer, TestElement> parent = child.getParent();
        assertNotNull(parent);
        Integer parentId = parent.getId();
        if (Objects.nonNull(parentId)) {
            assertEquals(ROOT_ID, parentId.intValue());
        }

        DefaultNode<Integer, TestElement> grandchild = tree.getById(GRANDCHILD_ID);
        assertNotNull(grandchild);
        DefaultNode<Integer, TestElement> grandchildParent = grandchild.getParent();
        assertNotNull(grandchildParent);
        assertEquals(1, grandchildParent.getChildren().size());
    }

    /**
     * 测试9: 处理空元素列表
     */
    @Test
    @DisplayName("添加空列表时树应保持为空")
    void testHandleEmptyElements() {
        tree.add(new ArrayList<>());
        assertEquals(0, tree.size());
    }

    /**
     * 测试10: 节点迭代
     */
    @Test
    @DisplayName("应该能遍历树中的所有节点")
    void testNodeIteration() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, ROOT_ID)
        );
        tree.add(elements);

        List<Integer> ids = new ArrayList<>();
        tree.forEach((id, node) -> ids.add(id));

        assertEquals(3, ids.size());
        assertTrue(ids.contains(ROOT_ID));
        assertTrue(ids.contains(CHILD_ID));
        assertTrue(ids.contains(GRANDCHILD_ID));
    }

    /**
     * 测试11: 多层级树结构
     */
    @Test
    @DisplayName("应该能构建和处理多层级树结构")
    void testMultiLevelHierarchy() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, ROOT_ID),
                new TestElement(GREAT_GRANDCHILD_ID, CHILD_ID),
                new TestElement(5, CHILD_ID),
                new TestElement(6, GRANDCHILD_ID)
        );
        tree.add(elements);

        assertEquals(6, tree.size());

        DefaultNode<Integer, TestElement> root = tree.getById(ROOT_ID);
        assertNotNull(root);
        assertEquals(2, root.getChildren().size());

        DefaultNode<Integer, TestElement> level2Node = tree.getById(CHILD_ID);
        assertNotNull(level2Node);
        assertEquals(2, level2Node.getChildren().size());

        DefaultNode<Integer, TestElement> level3Node = tree.getById(GRANDCHILD_ID);
        assertNotNull(level3Node);
        assertEquals(1, level3Node.getChildren().size());

        DefaultNode<Integer, TestElement> level4Node = tree.getById(GREAT_GRANDCHILD_ID);
        assertNotNull(level4Node);
        assertEquals(0, level4Node.getChildren().size());
    }

    /**
     * 测试12: 处理重复 ID（合并策略）
     */
    @Test
    @DisplayName("重复 ID 时应该使用默认合并策略保留新节点")
    void testMergeWithDuplicateIds() {
        List<TestElement> firstBatch = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID)
        );
        tree.add(firstBatch);

        List<TestElement> secondBatch = List.of(
                new TestElement(CHILD_ID, ROOT_ID, "Updated-Name")
        );
        tree.add(secondBatch);

        assertEquals(2, tree.size());
        DefaultNode<Integer, TestElement> updated = tree.getById(CHILD_ID);
        assertNotNull(updated);
        TestElement element = updated.getElement();
        if (Objects.nonNull(element)) {
            assertEquals("Updated-Name", element.getName());
        }
    }

    /**
     * 测试13: 单个节点更新和删除
     */
    @Test
    @DisplayName("应该能正确更新和删除单个节点")
    void testSingleNodeOperations() {
        List<TestElement> elements = List.of(
                new TestElement(SINGLE_NODE_ID, null)
        );
        tree.add(elements);

        boolean updated = tree.update(SINGLE_NODE_ID, node -> {
            TestElement element = node.getElement();
            if (Objects.nonNull(element)) {
                element.setName(MODIFIED_NODE);
            }
        });
        assertTrue(updated);

        tree.remove(node -> Objects.equals(SINGLE_NODE_ID, node.getId()));
        assertNull(tree.getById(SINGLE_NODE_ID));
    }

    /**
     * 测试14: 查找不存在的节点
     */
    @Test
    @DisplayName("查找不存在的节点应返回空列表")
    void testFindNonexistentNode() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID)
        );
        tree.add(elements);

        List<DefaultNode<Integer, TestElement>> found = tree.find(
                node -> Objects.equals(NON_EXISTENT_ID, node.getId())
        );

        assertTrue(found.isEmpty());
    }

    /**
     * 测试15: 树的大小操作
     */
    @Test
    @DisplayName("树的大小应该随操作而正确变化")
    void testTreeSize() {
        assertEquals(0, tree.size());

        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, ROOT_ID)
        );
        tree.add(elements);
        assertEquals(3, tree.size());

        tree.remove(node -> Objects.equals(GRANDCHILD_ID, node.getId()));
        assertEquals(2, tree.size());

        tree.clear();
        assertEquals(0, tree.size());
    }

    /**
     * 测试16: 并发操作安全性验证
     */
    @Test
    @DisplayName("应该能安全处理并发读写操作")
    void testConcurrentOperations() throws InterruptedException {
        List<TestElement> elements = new ArrayList<>();
        for (int i = 1; i <= INITIAL_CONCURRENT_NODES; i++) {
            Integer parentId = (i == 1) ? null : ((i - 1) / 2);
            elements.add(new TestElement(i, parentId));
        }
        tree.add(elements);

        Thread readThread = new Thread(this::executeConcurrentReads);
        Thread writeThread = new Thread(this::executeConcurrentWrites);

        readThread.start();
        writeThread.start();
        readThread.join();
        writeThread.join();

        assertEquals(INITIAL_CONCURRENT_NODES + BATCH_SIZE, tree.size());
    }

    /**
     * 执行并发读操作
     */
    private void executeConcurrentReads() {
        for (int i = 0; i < CONCURRENT_ITERATIONS; i++) {
            tree.find(node -> {
                Integer nodeId = node.getId();
                return Objects.nonNull(nodeId) && nodeId > 5;
            });
            tree.getById(5);
            tree.size();
        }
    }

    /**
     * 执行并发写操作
     */
    private void executeConcurrentWrites() {
        for (int i = INITIAL_CONCURRENT_NODES + 1; i <= INITIAL_CONCURRENT_NODES + BATCH_SIZE; i++) {
            List<TestElement> newElements = List.of(
                    new TestElement(i, (i - 1) / 2)
            );
            tree.add(newElements);
        }
    }

    /**
     * 测试17: 获取可选节点
     */
    @Test
    @DisplayName("应该能正确返回存在和不存在节点的Optional")
    void testGetOptionalNode() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID)
        );
        tree.add(elements);

        Optional<DefaultNode<Integer, TestElement>> found = tree.get(
                node -> Objects.equals(ROOT_ID, node.getId())
        );
        assertTrue(found.isPresent());
        found.ifPresent(node -> {
            Integer nodeId = node.getId();
            if (Objects.nonNull(nodeId)) {
                assertEquals(ROOT_ID, nodeId);
            }
        });

        Optional<DefaultNode<Integer, TestElement>> notFound = tree.get(
                node -> Objects.equals(NON_EXISTENT_ID, node.getId())
        );
        assertFalse(notFound.isPresent());
    }

    /**
     * 测试18: 更新多个节点
     */
    @Test
    @DisplayName("应该能正确批量更新多个节点")
    void testUpdateMultipleNodes() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null),
                new TestElement(CHILD_ID, ROOT_ID),
                new TestElement(GRANDCHILD_ID, ROOT_ID)
        );
        tree.add(elements);

        long updatedCount = tree.update(List.of(ROOT_ID, CHILD_ID), node -> {
            TestElement element = node.getElement();
            if (Objects.nonNull(element)) {
                element.setName(BATCH_UPDATED);
            }
        });

        assertEquals(2L, updatedCount);
        DefaultNode<Integer, TestElement> node1 = tree.getById(ROOT_ID);
        assertNotNull(node1);
        assertNodeName(node1, BATCH_UPDATED);

        DefaultNode<Integer, TestElement> node2 = tree.getById(CHILD_ID);
        assertNotNull(node2);
        assertNodeName(node2, BATCH_UPDATED);

        DefaultNode<Integer, TestElement> node3 = tree.getById(GRANDCHILD_ID);
        assertNotNull(node3);
        assertNodeName(node3, NODE_PREFIX + GRANDCHILD_ID);
    }

    /**
     * 测试19: 错误的更新操作
     */
    @Test
    @DisplayName("更新不存在的节点应返回false")
    void testUpdateNonexistentNode() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null)
        );
        tree.add(elements);

        boolean updated = tree.update(NON_EXISTENT_ID, node -> {
            throw new AssertionError("Should not reach here");
        });

        assertFalse(updated);
    }

    /**
     * 测试20: 节点的基本属性验证
     */
    @Test
    @DisplayName("应该能正确获取和验证节点的基本属性")
    void testNodeAttributes() {
        List<TestElement> elements = List.of(
                new TestElement(ROOT_ID, null, ROOT_NODE),
                new TestElement(CHILD_ID, ROOT_ID, CHILD_NODE)
        );
        tree.add(elements);

        DefaultNode<Integer, TestElement> rootNode = tree.getById(ROOT_ID);
        assertNotNull(rootNode);
        Integer rootNodeId = rootNode.getId();
        if (Objects.nonNull(rootNodeId)) {
            assertEquals(ROOT_ID, rootNodeId.intValue());
        }
        assertNodeName(rootNode, ROOT_NODE);

        DefaultNode<Integer, TestElement> childNode = tree.getById(CHILD_ID);
        assertNotNull(childNode);
        Integer childNodeId = childNode.getId();
        Integer childNodeParentId = childNode.getParentId();
        if (Objects.nonNull(childNodeId)) {
            assertEquals(CHILD_ID, childNodeId.intValue());
        }
        if (Objects.nonNull(childNodeParentId)) {
            assertEquals(ROOT_ID, childNodeParentId.intValue());
        }
        assertNodeName(childNode, CHILD_NODE);
    }

    /**
     * 断言节点名称相等的辅助方法
     *
     * @param node     要检查的节点
     * @param expected 期望的名称
     */
    private void assertNodeName(DefaultNode<Integer, TestElement> node, String expected) {
        TestElement element = node.getElement();
        if (Objects.nonNull(element)) {
            assertEquals(expected, element.getName());
        }
    }
}