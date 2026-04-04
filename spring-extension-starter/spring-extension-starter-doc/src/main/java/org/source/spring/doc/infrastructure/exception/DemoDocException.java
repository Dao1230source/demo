package org.source.spring.doc.infrastructure.exception;

import org.source.utility.exception.BaseException;
import org.source.utility.exception.EnumProcessor;
import org.springframework.lang.Nullable;

/**
 * 文档模块自定义业务异常
 * 
 * <p>该异常类继承自{@link BaseException}，用于文档模块中的业务异常处理。
 * 所有文档相关的异常都应使用此异常类或其子类进行封装抛出。</p>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>文档生成过程中的异常情况</li>
 *   <li>文档配置校验失败时抛出</li>
 *   <li>文档数据处理异常时抛出</li>
 * </ul>
 * 
 * <p>示例：</p>
 * <pre>{@code
 * throw new DemoDocException(DemoDocExceptionEnum.NOT_EXISTS_DELETE_COLUMN);
 * }</pre>
 *
 * @author dao1230source
 * @since 1.0.0
 * @see BaseException
 * @see DemoDocExceptionEnum
 */
public class DemoDocException extends BaseException {

    /**
     * 构造完整的业务异常实例
     * 
     * <p>包含异常枚举、原始异常、额外消息和格式化参数，适用于需要完整异常信息的场景。</p>
     *
     * @param content      异常枚举，包含异常码和消息模板，不能为null
     * @param cause        原始异常，可为null
     * @param extraMessage 额外的异常消息，可为null
     * @param objects      消息格式化参数，用于替换消息模板中的占位符
     */
    public DemoDocException(EnumProcessor<?> content, @Nullable Throwable cause, @Nullable String extraMessage, @Nullable Object... objects) {
        super(content, cause, extraMessage, objects);
    }

    /**
     * 构造包含额外消息和格式化参数的业务异常实例
     * 
     * <p>适用于需要附加自定义消息的场景，消息会追加到枚举定义的默认消息之后。</p>
     *
     * @param content      异常枚举，包含异常码和消息模板，不能为null
     * @param extraMessage 额外的异常消息，用于补充说明异常详情
     * @param objects      消息格式化参数，用于替换消息模板中的占位符
     */
    public DemoDocException(EnumProcessor<?> content, String extraMessage, Object... objects) {
        super(content, extraMessage, objects);
    }

    /**
     * 构造包含原始异常的业务异常实例
     * 
     * <p>适用于捕获其他异常后进行封装的场景，保留原始异常堆栈信息。</p>
     *
     * @param content 异常枚举，包含异常码和消息模板，不能为null
     * @param e       原始异常，用于异常链追踪
     */
    public DemoDocException(EnumProcessor<?> content, Throwable e) {
        super(content, e);
    }

    /**
     * 构造基础的业务异常实例
     * 
     * <p>仅使用异常枚举中的信息，适用于不需要额外信息的简单异常场景。</p>
     *
     * @param content 异常枚举，包含异常码和消息模板，不能为null
     */
    public DemoDocException(EnumProcessor<?> content) {
        super(content);
    }

}