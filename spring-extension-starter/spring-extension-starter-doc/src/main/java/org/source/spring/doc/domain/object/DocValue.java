package org.source.spring.doc.domain.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.source.spring.object.handler.ObjectBodyValueHandlerDefiner;

/**
 * 文档值对象
 * <p>
 * 实现 {@link ObjectBodyValueHandlerDefiner} 接口，用于表示文档元素的值。
 * 包含文档元素的基本信息，如元素类型、文档内容等。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DocValue implements ObjectBodyValueHandlerDefiner {

    /**
     * 名称
     */
    private String name;

    /**
     * 对象ID，唯一
     */
    private String objectId;

    /**
     * 排序字段
     */
    private String sorted;

    /**
     * 关联关系类型
     */
    private Integer relationType;

    /**
     * 元素类型
     * <p>
     * 取值见 {@link DocObjectTypeEnum}
     * </p>
     */
    private Integer elementType;

    /**
     * 文档内容（JavaDoc 注释）
     */
    private String docContent;

    /**
     * 元素的唯一标识
     * <p>
     * 格式根据元素类型不同而不同：
     * <ul>
     *     <li>类：类全限定名</li>
     *     <li>方法：类全限定名#方法名</li>
     *     <li>成员变量：类全限定名#变量名</li>
     *     <li>参数：方法ID#参数名</li>
     *     <li>共用变量：类型全限定名#变量名</li>
     * </ul>
     * </p>
     */
    private String elementId;

    /**
     * 父元素ID
     */
    private String parentElementId;

    /**
     * 变量名（仅变量类型元素使用）
     */
    private String variableName;

    /**
     * 变量类型（仅变量类型元素使用）
     */
    private String variableType;

    /**
     * 变量类型全限定名（仅变量类型元素使用）
     */
    private String variableTypeQualifiedName;

    /**
     * 是否原始类型（仅变量类型元素使用）
     */
    private Boolean primitive;

    /**
     * 方法名（仅方法类型元素使用）
     */
    private String methodName;

    /**
     * 返回类型（仅方法类型元素使用）
     */
    private String returnType;

    /**
     * 返回类型全限定名（仅方法类型元素使用）
     */
    private String returnTypeQualifiedName;

    /**
     * 类名（仅类类型元素使用）
     */
    private String className;

    /**
     * 类全限定名
     */
    private String classQualifiedName;

    /**
     * JPA 表名（仅 JPA 实体类使用）
     */
    private String tableName;

    /**
     * 是否为 JPA 实体
     */
    private Boolean isEntity;

    /**
     * JPA 列名（仅 JPA 列变量使用）
     */
    private String columnName;

    /**
     * 是否为主键（仅 JPA 列变量使用）
     */
    private Boolean isPrimaryKey;

    /**
     * HTTP 方法（仅 REST 接口使用）
     */
    private String httpMethod;

    /**
     * 请求路径（仅 REST 接口使用）
     */
    private String path;

    /**
     * 参数顺序（仅方法入参使用）
     */
    private Integer parameterOrder;

    /**
     * 模块路径
     */
    private String modulePath;

    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 是否为 SpringBoot 服务
     */
    private Boolean isSpringBoot;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态
     * <p>
     * DRAFT: 草稿
     * PUBLISHED: 已发布
     * MERGED: 已合并
     * </p>
     */
    private String status;
}