package org.source.spring.doc.domain.value;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REST 接口文档元素
 * <p>
 * 表示 REST API 接口的文档信息，包含 HTTP 方法类型、
 * 接口路径、返回类型、JavaDoc 注释内容，
 * 以及参数信息（路径变量、请求参数、请求体）。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RestDocData extends DocData {

    /**
     * HTTP 方法类型（GET、POST、PUT、DELETE、PATCH）
     */
    private String httpMethod;

    /**
     * 接口路径（含类级别的路径）
     */
    private String path;

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
}