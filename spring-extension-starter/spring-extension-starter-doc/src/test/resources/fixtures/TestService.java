package org.source.spring.doc.test.fixture;

import jakarta.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测试服务类
 * <p>
 * 用于测试 Doc 解析器的完整功能，包含：
 * - 类级别 JavaDoc
 * - 方法级别 JavaDoc
 * - 字段级别 JavaDoc
 * - 重载方法
 * - 构造函数
 * - 内部类
 * - JPA 注解
 * - Spring 注解
 * </p>
 *
 * @author test-author
 * @since 1.0.0
 */
@Service
@Entity
@Table(name = "test_entity")
public class TestService {

    /**
     * 主键 ID
     */
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 用户名称
     */
    @Column(name = "user_name", length = 100)
    private String userName;

    /**
     * 状态标识
     */
    private boolean active;

    /**
     * 默认构造函数
     */
    public TestService() {
        this.id = 0L;
    }

    /**
     * 带 ID 的构造函数
     *
     * @param id 主键 ID
     */
    public TestService(Long id) {
        this.id = id;
    }

    /**
     * 全参数构造函数
     *
     * @param id       主键 ID
     * @param userName 用户名称
     */
    public TestService(Long id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    /**
     * 根据 ID 获取用户
     * <p>
     * 查询指定 ID 的用户信息
     * </p>
     *
     * @param id 用户 ID
     * @return 用户实体
     */
    @Transactional
    public TestService getById(Long id) {
        return null;
    }

    /**
     * 根据名称获取用户
     *
     * @param name 用户名称
     * @return 用户实体列表
     */
    public List<TestService> getByByName(String name) {
        return null;
    }

    /**
     * 重载方法：根据 ID 和名称获取用户
     *
     * @param id   用户 ID
     * @param name 用户名称
     * @return 用户实体
     */
    @Transactional
    public TestService getById(Long id, String name) {
        return null;
    }

    /**
     * 定时任务方法
     * <p>
     * 每分钟执行一次的清理任务
     * </p>
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scheduledTask() {
        // 定时任务逻辑
    }

    /**
     * 异步方法
     *
     * @param data 处理数据
     */
    @org.springframework.scheduling.annotation.Async
    public void asyncProcess(String data) {
        // 异步处理逻辑
    }

    /**
     * 内部枚举类
     * <p>
     * 定义用户状态枚举
     * </p>
     */
    public enum Status {
        /**
         * 活跃状态
         */
        ACTIVE,
        /**
         * 禁用状态
         */
        DISABLED,
        /**
         * 已删除状态
         */
        DELETED
    }

    /**
     * 内部静态类
     * <p>
     * 定义用户统计信息
     * </p>
     */
    public static class Statistics {
        /**
         * 总数
         */
        private int total;
        /**
         * 活跃数
         */
        private int activeCount;
    }

    /**
     * 内部接口
     */
    public interface Handler {
        /**
         * 处理方法
         *
         * @param data 数据
         */
        void handle(String data);
    }
}