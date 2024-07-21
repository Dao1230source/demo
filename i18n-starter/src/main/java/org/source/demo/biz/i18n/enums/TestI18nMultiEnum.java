package org.source.demo.biz.i18n.enums;

import lombok.Getter;
import org.source.i18n.annotation.I18nDict;
import org.source.i18n.annotation.I18nRef;
import org.source.i18n.enums.I18nRefTypeEnum;

@I18nDict(value = @I18nRef(type = I18nRefTypeEnum.FIELD, value = "code"))
@I18nDict(value = @I18nRef(type = I18nRefTypeEnum.FIELD, value = "desc"))
@Getter
public enum TestI18nMultiEnum {
    TEST1("001", "test1"),
    TEST2("002", "test2");
    private final String code;
    private final String desc;

    TestI18nMultiEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
