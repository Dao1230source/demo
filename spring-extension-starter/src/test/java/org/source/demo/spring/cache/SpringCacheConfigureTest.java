package org.source.demo.spring.cache;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.source.demo.spring.cache.facade.SpringCacheConfigureFacade;
import org.source.demo.spring.facade.param.StudentParam;
import org.source.demo.spring.facade.view.StudentView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class SpringCacheConfigureTest {
    @Autowired
    private SpringCacheConfigureFacade configureFacade;

    @Test
    void str2str() {
        String s1 = configureFacade.str2str("Tom");
        Assertions.assertEquals("Tom", s1);
        String s2 = configureFacade.str2str("Tom");
        Assertions.assertEquals(s1, s2);
    }

    @Test
    void str2strAndCaching() throws InterruptedException {
        String s = configureFacade.str2strAndCaching("Tom");
        Assertions.assertEquals("Tom", s);
        Thread.sleep(100);
    }

    @Test
    void str2strCacheInJvm() {
        String s1 = configureFacade.str2strCacheInJvm("Tom");
        Assertions.assertEquals("Tom", s1);
        String s2 = configureFacade.str2strCacheInJvm("Tom");
        Assertions.assertEquals(s1, s2);
    }

    @Test
    void str2View() {
        StudentView view1 = configureFacade.str2View("Tom");
        Assertions.assertEquals("Tom", view1.getName());
        StudentView view2 = configureFacade.str2View("Tom");
        Assertions.assertEquals(view1, view2);
    }

    @Test
    void param2View() {
        StudentView view1 = configureFacade.param2View(StudentParam.builder().name("Tom").build());
        Assertions.assertEquals("Tom", view1.getName());
        StudentView view2 = configureFacade.param2View(StudentParam.builder().name("Tom").build());
        Assertions.assertEquals(view1, view2);
    }

    @Test
    void strings2ViewList() {
        List<StudentView> viewList1 = configureFacade.strings2ViewList(List.of("Tom", "Jim"));
        Assertions.assertEquals(2, viewList1.size());
        StudentView view = viewList1.get(0);
        Assertions.assertEquals("Tom", view.getName());
        List<StudentView> viewList2 = configureFacade.strings2ViewList(List.of("Tom", "Jim"));
        Assertions.assertEquals(viewList1, viewList2);
    }

    @Test
    void str2ViewListJvmIntegerKey() {
        List<StudentView> viewList1 = configureFacade.str2ViewListJvmIntegerKey(List.of(1, 2));
        Assertions.assertEquals(2, viewList1.size());
        StudentView view = viewList1.get(0);
        Assertions.assertEquals(1, view.getId());
        List<StudentView> viewList2 = configureFacade.str2ViewListJvmIntegerKey(List.of(1, 2));
        Assertions.assertEquals(viewList1, viewList2);
    }

    @Test
    void str2ViewMap() {
        Map<String, StudentView> nameViewMap1 = configureFacade.str2ViewMap(List.of("Tom", "Jim"));
        Assertions.assertEquals(2, nameViewMap1.size());
        StudentView view = nameViewMap1.get("Tom");
        Assertions.assertEquals("Tom", view.getName());
        Map<String, StudentView> nameViewMap2 = configureFacade.str2ViewMap(List.of("Tom", "Jim"));
        Assertions.assertEquals(nameViewMap1, nameViewMap2);
    }

    @Test
    void str2ViewList() {
        List<StudentView> viewList1 = configureFacade.str2ViewList("class_1");
        Assertions.assertEquals(2, viewList1.size());
        StudentView view = viewList1.get(0);
        Assertions.assertEquals("class_1", view.getClassName());
        List<StudentView> viewList2 = configureFacade.str2ViewList("class_1");
        Assertions.assertEquals(viewList1, viewList2);
    }

    @Test
    void str2ViewListCacheInJvm() {
        List<StudentView> viewList1 = configureFacade.str2ViewListCacheInJvm("class_1");
        Assertions.assertEquals(2, viewList1.size());
        StudentView view = viewList1.get(0);
        Assertions.assertEquals("class_1", view.getClassName());
        List<StudentView> viewList2 = configureFacade.str2ViewListCacheInJvm("class_1");
        Assertions.assertEquals(viewList1, viewList2);
    }

    @Test
    void partialCacheStrategyPartialTrust() {
        List<StudentView> viewList1 = configureFacade.partialCacheStrategyPartialTrust(List.of("Tom", "Jim"));
        Assertions.assertEquals(2, viewList1.size());
        // 使用缓存部分信任策略时，入参必须是可变的
        List<StudentView> viewList3 = configureFacade.partialCacheStrategyPartialTrust(new ArrayList<>(List.of("Tom", "Jerry")));
        Assertions.assertEquals(2, viewList3.size());
        StudentView view = viewList3.get(1);
        Assertions.assertEquals("Jerry", view.getName());
    }

    @Test
    void partialCacheStrategyTrust() {
        List<StudentView> viewList1 = configureFacade.partialCacheStrategyTrust(List.of("Tom", "Jim"));
        Assertions.assertEquals(2, viewList1.size());
        List<StudentView> viewList2 = configureFacade.partialCacheStrategyTrust(List.of("Tom", "Jerry"));
        Assertions.assertEquals(1, viewList2.size());
    }

}
