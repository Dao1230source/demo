package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

/**
 * REST 接口文档元素
 * <p>
 * 表示 REST API 接口的文档信息，包含 HTTP 方法类型、
 * 接口路径、所属类路径、返回类型、JavaDoc 注释内容，
 * 以及参数信息（路径变量、请求参数、请求体）。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RestDocElement extends DocElement {

    /**
     * HTTP 方法类型（GET、POST、PUT、DELETE、PATCH）
     */
    private String httpMethod;

    /**
     * 接口路径（含类级别的路径）
     */
    private String path;

    /**
     * 所属类路径（全限定名）
     */
    private String classPath;

    /**
     * 返回类型
     */
    private String returnType;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 路径变量参数列表
     */
    private String[] pathVariables;

    /**
     * 请求参数列表
     */
    private String[] requestParams;

    /**
     * 请求体类型
     */
    private String requestBody;

    /**
     * 获取元素的唯一标识
     * <p>
     * 格式：类路径#HTTP方法:接口路径
     * </p>
     *
     * @return REST 接口的唯一标识
     */
    @Override
    public @NonNull String getId() {
        return classPath + "#" + httpMethod + ":" + path;
    }

    /**
     * 获取父元素 ID
     * <p>
     * 返回所属类的路径（全限定名）
     * </p>
     *
     * @return 所属类的全限定名
     */
    @Override
    public String getParentId() {
        return classPath;
    }
}