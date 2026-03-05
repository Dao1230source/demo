package org.source.demo.tree.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.source.demo.tree.model.AreaDataLoader;
import org.source.demo.tree.model.AreaElement;
import org.source.utility.tree.DefaultNode;
import org.source.utility.tree.Tree;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
* Tree组件JMH性能测试
* 基于中国行政区划数据（area_code_2024.csv.gz）
* <p>
* 测试场景：
* 1. 树的创建和数据添加（小、中、大规模）
* 2. ID查询性能（getById）
* 3. 条件查询性能（find）
* 4. 遍历性能（forEach）
* <p>
*/
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xmx2g", "-Xms2g"})
@Warmup(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
public class TreeBenchmark {

    // 数据集
    private List<AreaElement> smallDataset;    // 100条
    private List<AreaElement> mediumDataset;   // 1000条
    private List<AreaElement> largeDataset;    // 全量数据

    // 创建好的树
    private Tree<String, AreaElement, DefaultNode<String, AreaElement>> smallTree;
    private Tree<String, AreaElement, DefaultNode<String, AreaElement>> mediumTree;
    private Tree<String, AreaElement, DefaultNode<String, AreaElement>> largeTree;

    @Setup(Level.Trial)
    public void setup() {
        // 加载数据集
        smallDataset = AreaDataLoader.loadAreas(100);
        List<AreaElement> allAreas = AreaDataLoader.loadAllAreas();
        mediumDataset = allAreas.stream().limit(1000).toList();
        largeDataset = allAreas;

        // 初始化树
        smallTree = initTree(smallDataset);
        mediumTree = initTree(mediumDataset);
        largeTree = initTree(largeDataset);
    }

    private Tree<String, AreaElement, DefaultNode<String, AreaElement>> initTree(List<AreaElement> data) {
        Tree<String, AreaElement, DefaultNode<String, AreaElement>> tree =
                Tree.of(new DefaultNode<String, AreaElement>());
        tree.add(data);
        return tree;
    }

    // ==================== 添加操作基准测试 ====================

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public Tree<String, AreaElement, DefaultNode<String, AreaElement>> testAddSmall() {
        Tree<String, AreaElement, DefaultNode<String, AreaElement>> tree =
                Tree.of(new DefaultNode<String, AreaElement>());
        tree.add(smallDataset);
        return tree;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public Tree<String, AreaElement, DefaultNode<String, AreaElement>> testAddMedium() {
        Tree<String, AreaElement, DefaultNode<String, AreaElement>> tree =
                Tree.of(new DefaultNode<String, AreaElement>());
        tree.add(mediumDataset);
        return tree;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public Tree<String, AreaElement, DefaultNode<String, AreaElement>> testAddLarge() {
        Tree<String, AreaElement, DefaultNode<String, AreaElement>> tree =
                Tree.of(new DefaultNode<String, AreaElement>());
        tree.add(largeDataset);
        return tree;
    }

    // ==================== 查询操作基准测试 ====================

    @Benchmark
    public int testGetByIdSmall() {
        int count = 0;
        for (AreaElement area : smallDataset) {
            if (smallTree.getById(area.getId()) != null) {
                count++;
            }
        }
        return count;
    }

    @Benchmark
    public int testGetByIdMedium() {
        int count = 0;
        for (AreaElement area : mediumDataset) {
            if (mediumTree.getById(area.getId()) != null) {
                count++;
            }
        }
        return count;
    }

    @Benchmark
    public int testGetByIdLarge() {
        int count = 0;
        for (int i = 0; i < Math.min(1000, largeDataset.size()); i++) {
            AreaElement area = largeDataset.get(i);
            if (largeTree.getById(area.getId()) != null) {
                count++;
            }
        }
        return count;
    }

    // ==================== 条件查询基准测试 ====================

    @Benchmark
    public int testFindSmall() {
        return smallTree.find(n -> Objects.nonNull(n.getElement()) && n.getElement().getName().contains("区")).size();
    }

    @Benchmark
    public int testFindMedium() {
        return mediumTree.find(n -> Objects.nonNull(n.getElement()) && n.getElement().getName().contains("区")).size();
    }

    @Benchmark
    public int testFindLarge() {
        return largeTree.find(n -> Objects.nonNull(n.getElement()) && n.getElement().getName().contains("区")).size();
    }

    // ==================== 遍历操作基准测试 ====================

    @Benchmark
    public int testForEachSmall() {
        final int[] count = {0};
        smallTree.forEach((id, node) -> count[0]++);
        return count[0];
    }

    @Benchmark
    public int testForEachMedium() {
        final int[] count = {0};
        mediumTree.forEach((id, node) -> count[0]++);
        return count[0];
    }

    @Benchmark
    public int testForEachLarge() {
        final int[] count = {0};
        largeTree.forEach((id, node) -> count[0]++);
        return count[0];
    }

    // ==================== 主函数 - 执行性能测试 ====================

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TreeBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .result("tree-benchmark-result.json")
                .build();

        new Runner(opt).run();
    }
}