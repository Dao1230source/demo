package org.source.spring.doc.domain.element;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Bean 文档元素
 * <p>
 * 表示 Spring Bean 的文档信息，包含 Bean 名称、类型、
 * 依赖注入信息、事务和异步配置等。
 * </p>
 *
 * @author dao1230source
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SpringBeanElement extends DocElement {

    /**
     * Bean 名称
     */
    private String beanName;

    /**
     * Bean 类型（Controller, Service, Repository, Component, Configuration）
     */
    private String beanType;

    /**
     * 依赖注入的 Bean 列表
     */
    private List<String> dependencies;

    /**
     * 是否为异步 Bean（@Async）
     */
    private boolean isAsync;

    /**
     * 是否为事务性 Bean（@Transactional）
     */
    private boolean isTransactional;

    /**
     * 定时任务表达式（@Scheduled cron）
     */
    private String cronExpression;

    /**
     * 所属类的全限定名
     */
    private String classQualifiedName;

    /**
     * 获取元素的唯一标识
     *
     * @return Bean 的唯一标识
     */
    @Override
    public @NonNull String getId() {
        return beanName != null ? beanName : classQualifiedName;
    }

    /**
     * 获取父元素 ID
     *
     * @return 所属类的全限定名
     */
    @Override
    public String getParentId() {
        return classQualifiedName;
    }

    /**
     * 添加依赖
     *
     * @param dependency 依赖名称
     */
    public void addDependency(String dependency) {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        dependencies.add(dependency);
    }
}
