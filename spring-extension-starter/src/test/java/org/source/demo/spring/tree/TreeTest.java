package org.source.demo.spring.tree;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.source.utility.tree.*;
import org.source.utility.utils.Jsons;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
class TreeTest {

    /**
     * DefaultNode
     */
    @Test
    void defaultNode() {
        List<Ele> es = new ArrayList<>();
        es.add(new Ele("1", "p1"));
        es.add(new Ele("2", "p1"));
        es.add(new Ele("3", "p1"));
        es.add(new Ele("p1", null));
        es.add(new Ele("p2", null));
        Tree<String, Ele, DefaultNode<String, Ele>> defaultNodeTree = Tree.of(new DefaultNode<>());
        defaultNodeTree.add(es);
        System.out.println(Jsons.str(defaultNodeTree));
    }

    @Test
    void deepNode() {
        List<Ele> es = new ArrayList<>();
        es.add(new Ele("1", "p1"));
        es.add(new Ele("2", "p1"));
        es.add(new Ele("3", "p1"));
        es.add(new Ele("p1", null));
        es.add(new Ele("p2", null));
        Tree<String, Ele, DeepNode<String, Ele>> deepNodeTree = Tree.of(new DeepNode<>(true));
        deepNodeTree.add(es);
        System.out.println(Jsons.str(deepNodeTree));
    }

    @Test
    void flatNode() {
        List<Ele> es = new ArrayList<>();
        es.add(new Ele("1", "p1"));
        es.add(new Ele("2", "p1"));
        es.add(new Ele("3", "p1"));
        es.add(new Ele("p1", null));
        es.add(new Ele("p2", null));
        Tree<String, Ele, FlatNode<String, Ele>> flatNodeTree = Tree.of(new FlatNode<>(List.of(Ele::getId, Ele::getParentId)));
        flatNodeTree.add(es);
        System.out.println(Jsons.str(flatNodeTree));
    }

    @Test
    void defaultEnhanceNode() {
        List<Ele2> es = new ArrayList<>();
        es.add(new Ele2("1", "p1", "1"));
        es.add(new Ele2("2", "p1", "2"));
        es.add(new Ele2("3", "p1", "0"));
        es.add(new Ele2("p1", null, "2"));
        es.add(new Ele2("p2", null, "2"));
        es.add(new Ele2("1", "p2", "1"));
        EnhanceTree<String, Ele2, DefaultEnhanceNode<String, Ele2>> enhanceTree = EnhanceTree.of(new DefaultEnhanceNode<String, Ele2>());
        enhanceTree.add(es);
        System.out.println(Jsons.str(enhanceTree));
    }

}
