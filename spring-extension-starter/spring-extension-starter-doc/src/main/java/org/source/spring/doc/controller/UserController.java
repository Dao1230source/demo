package org.source.spring.doc.controller;

import lombok.AllArgsConstructor;
import org.source.spring.doc.facade.UserFacade;
import org.source.spring.doc.facade.input.UserIn;
import org.source.spring.doc.facade.output.UserOut;
import org.source.spring.io.Output;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * <p>
 * 提供用户相关的REST API接口，负责接收HTTP请求并返回响应。
 * 作为系统的入口点，处理请求的路由和参数绑定。
 * </p>
 *
 * <p>
 * 提供的接口：
 * <ul>
 *   <li>GET /users/{id} - 获取单个用户</li>
 *   <li>GET /users - 获取所有用户列表</li>
 *   <li>POST /users - 创建单个用户</li>
 *   <li>POST /users/batch - 批量创建用户</li>
 *   <li>PUT /users/updateUser - 更新用户信息</li>
 * </ul>
 * </p>
 *
 * @author source
 * @since 1.0.0
 */
@AllArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    /**
     * 用户门面服务
     */
    private final UserFacade userFacade;

    /**
     * 获取单个用户
     *
     * <p>根据用户ID获取用户详细信息</p>
     *
     * @param id 用户ID
     * @return 用户信息响应
     */
    @GetMapping("/{id}")
    public Output<UserOut> getUser(@PathVariable Long id) {
        return Output.success(userFacade.getUser(id));
    }

    /**
     * 获取所有用户列表
     *
     * <p>获取系统中所有用户的列表</p>
     *
     * @return 用户列表响应
     */
    @GetMapping
    public Output<List<UserOut>> getAllUsers() {
        return Output.success(userFacade.getAllUsers());
    }

    /**
     * 创建单个用户
     *
     * <p>创建一个新的用户账户</p>
     *
     * @param userIn 用户创建请求数据
     * @return 创建的用户信息
     */
    @PostMapping
    public Output<UserOut> createUser(@RequestBody UserIn userIn) {
        return Output.success(userFacade.createUser(userIn));
    }

    /**
     * 批量创建用户
     *
     * <p>一次性创建多个用户账户</p>
     *
     * @param userIns 用户批量创建请求数据
     * @return 创建的用户列表
     */
    @PostMapping("/batch")
    public Output<Void> createUsers(@RequestBody List<UserIn> userIns) {
        userFacade.createUsers(userIns);
        return Output.success();
    }

    /**
     * 更新用户信息
     *
     * <p>更新指定用户的详细信息</p>
     *
     * @param userIn 用户更新请求数据
     * @return 更新后的用户信息
     */
    @PutMapping("/updateUser")
    public Output<UserOut> updateUser(@RequestBody UserIn userIn) {
        return Output.success(userFacade.updateUser(userIn));
    }
}