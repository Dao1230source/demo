package org.source.spring.doc.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档模块配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "doc")
public class DocConfig {

    /**
     * 项目根路径
     * <p>
     * 当 scanPackages 使用包名格式时，需要此配置来确定项目位置。
     * 若未配置，默认使用当前工作目录 System.getProperty("user.dir")
     * </p>
     */
    private String projectRootPath;

    /**
     * 扫描路径列表
     * <p>
     * 支持两种格式：
     * <ul>
     *     <li>包名格式：org.source.spring.doc.controller（转换为 src/main/java 下的路径）</li>
     *     <li>绝对路径：/Users/project/src/main/java/org/source（直接扫描）</li>
     * </ul>
     * 若为空，则扫描项目根目录下所有模块的 src/main/java
     * </p>
     */
    private List<String> scanPackages = new ArrayList<>();

    /**
     * 排除包/路径列表
     */
    private List<String> excludePackages = new ArrayList<>();

    /**
     * 文件扩展名列表，默认只扫描 .java 文件
     */
    private List<String> fileExtensions = List.of(".java");

    /**
     * 是否启用并行解析
     */
    private boolean enableParallel = true;

    /**
     * 并行解析线程数，默认使用 CPU 核数
     */
    private int parallelThreads = Runtime.getRuntime().availableProcessors();

    /**
     * 是否生成解析报告
     */
    private boolean generateReport = true;

    /**
     * 是否解析 Validation 注解（@NotNull, @NotBlank 等）
     */
    private boolean parseValidationAnnotations = true;

    /**
     * 是否解析 Spring 注解
     */
    private boolean parseSpringAnnotations = true;

    /**
     * 是否解析 Feign 注解
     */
    private boolean parseFeignAnnotations = false;

    /**
     * 是否解析 MyBatis 注解
     */
    private boolean parseMyBatisAnnotations = false;

    /**
     * 是否解析内部类
     */
    private boolean includeInnerClasses = true;

    /**
     * 是否解析私有成员（方法和字段）
     */
    private boolean includePrivateMembers = false;

    /**
     * 是否扫描测试类（src/test/java）
     */
    private boolean includeTestClasses = false;

    /**
     * 解析失败时的行为
     * <ul>
     *     <li>true: 遇到错误时抛出异常中止解析</li>
     *     <li>false: 记录失败文件，继续解析其他文件</li>
     * </ul>
     */
    private boolean failOnError = false;
}