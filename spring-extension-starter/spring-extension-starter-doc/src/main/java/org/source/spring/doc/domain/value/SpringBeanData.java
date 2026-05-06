package org.source.spring.doc.domain.value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Bean 文档值对象
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpringBeanData extends DocData {

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
     * JavaDoc 注释内容
     */
    private String docContent;

    /**
     * 是否已废弃（@Deprecated）
     */
    private boolean deprecated;

    /**
     * 废弃说明
     */
    private String deprecatedReason;

    /**
     * 添加依赖
     */
    public void addDependency(String dependency) {
        if (dependencies == null) {
            dependencies = new ArrayList<>();
        }
        dependencies.add(dependency);
    }
}