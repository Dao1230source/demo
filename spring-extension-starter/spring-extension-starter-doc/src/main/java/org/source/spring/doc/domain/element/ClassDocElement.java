package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * 类文档元素
 * <p>
 * 表示 Java 类的文档信息，包含类名、全限定名、修饰符、
 * JavaDoc 注释内容以及 JPA 相关信息（是否为实体、表名等）。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClassDocElement extends DocElement {

    /**
     * 类名（不含包名）
     */
    private String className;

    /**
     * 类的全限定名（含包名）
     */
    private String classQualifiedName;

    /**
     * 修饰符（如 public、abstract、final 等）
     */
    private String modifiers;

    /**
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 是否为 JPA 实体类
     */
    private boolean isEntity;

    /**
     * JPA 表名（仅当 isEntity 为 true 时有效）
     */
    private String tableName;

    /**
     * 所属模块名称（模块路径）
     * <p>
     * 用于建立 class -> module 的层级关系
     * </p>
     */
    private String moduleName;

    /**
     * 是否为接口
     */
    private Boolean isInterface;

    /**
     * 是否为枚举
     */
    private Boolean isEnum;

    /**
     * 是否已废弃（@Deprecated）
     */
    private Boolean deprecated;

    /**
     * Spring 组件注解信息
     * <p>
     * 包含 @Service、@Component、@Configuration 等注解信息
     * </p>
     */
    private Map<String, Object> springAnnotations;

    /**
     * Feign 客户端信息
     * <p>
     * 包含 @FeignClient 注解解析结果
     * </p>
     */
    private Map<String, Object> feignInfo;

    /**
     * MyBatis Mapper 信息
     * <p>
     * 包含 @Mapper 及 SQL 注解解析结果
     * </p>
     */
    private Map<String, Object> myBatisInfo;

    /**
     * 获取元素的唯一标识
     * <p>
     * 使用类的全限定名作为 ID
     * </p>
     *
     * @return 类的全限定名
     */
    @Override
    public @NonNull String getId() {
        return classQualifiedName;
    }

    /**
     * 获取父元素 ID
     * <p>
     * 返回所属模块的路径（modulePath）
     * </p>
     *
     * @return 模块路径
     */
    @Override
    public String getParentId() {
        return moduleName;
    }
}