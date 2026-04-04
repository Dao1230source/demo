package org.source.spring.doc.infrastructure.exception;

import org.source.utility.exception.EnumProcessor;

/**
 * 文档模块异常枚举
 * 
 * <p>该枚举类定义了文档模块中所有业务异常的错误码和消息，实现了{@link EnumProcessor}接口，
 * 用于配合{@link DemoDocException}进行统一的异常处理。</p>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>抛出文档模块相关的业务异常</li>
 *   <li>定义文档处理过程中的错误类型</li>
 *   <li>提供统一的错误码和错误消息管理</li>
 * </ul>
 * 
 * <p>示例：</p>
 * <pre>{@code
 * throw new DemoDocException(DemoDocExceptionEnum.NOT_EXISTS_DELETE_COLUMN);
 * }</pre>
 *
 * @author dao1230source
 * @since 1.0.0
 * @see DemoDocException
 * @see EnumProcessor
 */
public enum DemoDocExceptionEnum implements EnumProcessor<DemoDocException> {

    /**
     * 标记删除状态列不存在异常
     * 
     * <p>当尝试对不存在的删除标记列进行操作时抛出此异常。</p>
     */
    NOT_EXISTS_DELETE_COLUMN("mark deleted state column not exists");

    /**
     * 异常消息内容
     * 
     * <p>描述异常的详细信息，用于异常抛出时显示给调用方。</p>
     */
    private final String message;

    /**
     * 构造函数
     *
     * @param message 异常消息内容
     */
    DemoDocExceptionEnum(String message) {
        this.message = message;
    }

    /**
     * 获取异常错误码
     * 
     * <p>返回枚举实例的名称作为错误码，保证错误码的唯一性和可读性。</p>
     *
     * @return 异常错误码，格式为枚举常量名称
     */
    @Override
    public String getCode() {
        return this.name();
    }

    /**
     * 获取异常消息内容
     * 
     * <p>返回异常的详细描述信息。</p>
     *
     * @return 异常消息内容
     */
    @Override
    public String getMessage() {
        return this.message;
    }

}
