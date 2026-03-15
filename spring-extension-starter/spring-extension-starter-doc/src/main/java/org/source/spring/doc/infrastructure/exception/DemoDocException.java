package org.source.spring.doc.infrastructure.exception;

import org.source.utility.exception.BaseException;
import org.source.utility.exception.EnumProcessor;
import org.springframework.lang.Nullable;

/**
 * 自定义业务异常
 */
public class DemoDocException extends BaseException {


    public DemoDocException(EnumProcessor<?> content, @Nullable Throwable cause, @Nullable String extraMessage, @Nullable Object... objects) {
        super(content, cause, extraMessage, objects);
    }

    public DemoDocException(EnumProcessor<?> content, String extraMessage, Object... objects) {
        super(content, extraMessage, objects);
    }

    public DemoDocException(EnumProcessor<?> content, Throwable e) {
        super(content, e);
    }

    public DemoDocException(EnumProcessor<?> content) {
        super(content);
    }

}