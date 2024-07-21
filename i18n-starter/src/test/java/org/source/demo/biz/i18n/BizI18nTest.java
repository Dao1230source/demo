package org.source.demo.biz.i18n;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.source.demo.biz.i18n.enums.TestI18nMultiEnum;
import org.source.demo.biz.i18n.enums.TestI18nOneEnum;
import org.source.i18n.I18nWrapper;
import org.source.i18n.facade.data.Dict;
import org.source.i18n.facade.param.Dict2Param;
import org.source.i18n.facade.param.Dict3Param;
import org.source.utility.utils.Jsons;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class BizI18nTest {
    private final String oneEnumName = TestI18nOneEnum.class.getSimpleName();
    private final String multiEnumName = TestI18nMultiEnum.class.getSimpleName();

    @Test
    void findByKey() {
        // 默认只有一组，group = I18nDict.group(name)
        // @I18nDict(value = @I18nRef(type = I18nRefTypeEnum.FIELD, value = "code")) key 默认是枚举名称
        Dict dict1 = I18nWrapper.findByKey(new Dict3Param("zh-Hans-CN", oneEnumName, "TEST1"));
        log.info("dict1:{}", Jsons.str(dict1));
        Assertions.assertNotNull(dict1);
        Assertions.assertEquals("001", dict1.getValue());
        //  多组 group = I18nDict.group + [I18nDict.key(name)] + [I18nDict.value(name)]
        // 比如 @I18nRef(type = I18nRefTypeEnum.FIELD, value = "code") name 即字段名称 code
        Dict dict2 = I18nWrapper.findByKey(new Dict3Param("zh-Hans-CN", multiEnumName + ":code", "TEST1"));
        log.info("dict2:{}", Jsons.str(dict2));
        Assertions.assertNotNull(dict2);
        Assertions.assertEquals("001", dict2.getValue());
        List<Dict> dictList = I18nWrapper.findByKeys(List.of(new Dict3Param("zh-Hans-CN", oneEnumName, "TEST2"),
                new Dict3Param("zh-Hans-CN", multiEnumName + ":code", "TEST2")));
        log.info("findByKeys:{}", Jsons.str(dictList));
        Assertions.assertEquals(2, dictList.size(), "findByKeys error");
    }

    @Test
    void findByGroup() {
        List<Dict> dictList = I18nWrapper.findByGroup(new Dict2Param("zh-Hans-CN", oneEnumName));
        log.info("findByGroup:{}", Jsons.str(dictList));
        Assertions.assertFalse(dictList.isEmpty(), "findByGroup error");
        Map<Dict2Param, List<Dict>> dict2ParamListMap = I18nWrapper.findByGroups(List.of(new Dict2Param("zh-Hans-CN", oneEnumName),
                new Dict2Param("zh-Hans-CN", multiEnumName + ":desc")));
        log.info("findByGroups:{}", Jsons.str(dict2ParamListMap));
    }
}
