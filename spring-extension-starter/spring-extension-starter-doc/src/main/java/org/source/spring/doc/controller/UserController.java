package org.source.spring.doc.controller;

import org.source.spring.doc.dto.UserDto;
import org.source.spring.doc.dto.UserVo;
import org.source.spring.doc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 
 * <p>提供用户相关的REST API接口</p>
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取单个用户
     * 
     * <p>根据用户ID获取用户详细信息</p>
     * 
     * @param id 用户ID
     * @return 用户信息响应
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserVo> getUser(@PathVariable Long id) {
        UserVo user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * 获取所有用户列表
     * 
     * <p>获取系统中所有用户的列表</p>
     * 
     * @return 用户列表响应
     */
    @GetMapping
    public ResponseEntity<List<UserVo>> getAllUsers() {
        List<UserVo> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 创建单个用户
     * 
     * <p>创建一个新的用户账户</p>
     * 
     * @param userDto 用户创建请求数据
     * @return 创建的用户信息
     */
    @PostMapping
    public ResponseEntity<UserVo> createUser(@RequestBody UserDto userDto) {
        UserVo createdUser = userService.createUser(userDto);
        return ResponseEntity.ok(createdUser);
    }

    /**
     * 批量创建用户
     * 
     * <p>一次性创建多个用户账户</p>
     * 
     * @param userDtos 用户批量创建请求数据
     * @return 创建的用户列表
     */
    @PostMapping("/batch")
    public ResponseEntity<List<UserVo>> createUsers(@RequestBody List<UserDto> userDtos) {
        List<UserVo> createdUsers = userService.createUsers(userDtos);
        return ResponseEntity.ok(createdUsers);
    }

    /**
     * 更新用户信息
     * 
     * <p>更新指定用户的详细信息</p>
     * 
     * @param id 用户ID
     * @param userDto 用户更新请求数据
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserVo> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        UserVo updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }
}