package org.source.spring.doc.test.fixture;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

/**
 * 测试 REST 控制器
 * <p>
 * 用于测试 REST 注解解析器的功能
 * </p>
 *
 * @author test-author
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Controller
public class TestRestController {

    /**
     * 获取所有用户列表
     * <p>
     * GET 方法，返回所有用户
     * </p>
     *
     * @return 用户列表
     */
    @GetMapping
    public List<TestService> getAllUsers() {
        return null;
    }

    /**
     * 根据 ID 获取单个用户
     *
     * @param userId 用户 ID（路径变量）
     * @return 用户实体
     */
    @GetMapping("/{userId}")
    public TestService getUserById(@PathVariable("userId") Long userId) {
        return null;
    }

    /**
     * 根据名称搜索用户
     *
     * @param name 用户名称（请求参数）
     * @param page 页码（请求参数）
     * @return 用户列表
     */
    @GetMapping("/search")
    public List<TestService> searchUsers(
            @RequestParam("name") String name,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        return null;
    }

    /**
     * 创建新用户
     *
     * @param user 用户数据（请求体）
     * @return 创建的用户
     */
    @PostMapping
    public TestService createUser(@RequestBody TestService user) {
        return null;
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户 ID（路径变量）
     * @param user   用户数据（请求体）
     * @return 更新后的用户
     */
    @PutMapping("/{userId}")
    public TestService updateUser(
            @PathVariable("userId") Long userId,
            @RequestBody TestService user) {
        return null;
    }

    /**
     * 删除用户
     *
     * @param userId 用户 ID（路径变量）
     */
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
    }

    /**
     * 批量更新状态
     *
     * @param request 批量请求
     * @return 更新结果
     */
    @PatchMapping("/batch/status")
    public Map<String, Object> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        return null;
    }
}