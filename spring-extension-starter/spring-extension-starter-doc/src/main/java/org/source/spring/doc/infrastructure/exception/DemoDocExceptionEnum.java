package org.source.spring.doc.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.source.utility.exception.EnumProcessor;

@Getter
@AllArgsConstructor
public enum DemoDocExceptionEnum implements EnumProcessor<DemoDocException> {

    NOT_EXISTS_DELETE_COLUMN("mark deleted state column not exists");

    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }

}
