package org.source.demo.spring.i18n.enums;

import lombok.Getter;
import org.source.spring.i18n.annotation.I18nDict;
import org.source.spring.i18n.annotation.I18nRef;
import org.source.spring.i18n.enums.I18nRefTypeEnum;

@I18nDict(value = @I18nRef(type = I18nRefTypeEnum.FIELD, value = "code"))
@Getter
public enum TestI18nOneEnum {
    TEST1("001", "test1"),
    TEST2("002", "test2");
    private final String code;
    private final String desc;

    TestI18nOneEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
