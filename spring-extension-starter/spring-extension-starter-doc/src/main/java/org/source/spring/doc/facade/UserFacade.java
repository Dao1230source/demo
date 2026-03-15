package org.source.spring.doc.facade;

import lombok.AllArgsConstructor;
import org.source.spring.doc.domain.entity.UserEntity;
import org.source.spring.doc.domain.service.UserService;
import org.source.spring.doc.facade.input.UserIn;
import org.source.spring.doc.facade.mapper.UserMapper;
import org.source.spring.doc.facade.output.UserOut;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    /**
     * 获取单个用户
     *
     * <p>根据用户ID获取用户详细信息</p>
     *
     * @param id 用户ID
     * @return 用户信息响应
     */
    @GetMapping("/{id}")
    public UserOut getUser(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        return UserMapper.INSTANCE.y2z(user);
    }

    /**
     * 获取所有用户列表
     *
     * <p>获取系统中所有用户的列表</p>
     *
     * @return 用户列表响应
     */
    @GetMapping("getAllUsers")
    public List<UserOut> getAllUsers() {
        List<UserEntity> users = userService.findAll();
        return UserMapper.INSTANCE.y2zList(users);
    }

    /**
     * 创建单个用户
     *
     * <p>创建一个新的用户账户</p>
     *
     * @param userIn 用户创建请求数据
     * @return 创建的用户信息
     */
    @PostMapping("createUser")
    public UserOut createUser(@RequestBody UserIn userIn) {
        UserEntity userEntity = UserMapper.INSTANCE.x2y(userIn);
        UserEntity createdUser = userService.add(userEntity);
        return UserMapper.INSTANCE.y2z(createdUser);
    }

    /**
     * 批量创建用户
     *
     * <p>一次性创建多个用户账户</p>
     *
     * @param userIns 用户批量创建请求数据
     */
    @PostMapping("/batch")
    public void createUsers(@RequestBody List<UserIn> userIns) {
        List<UserEntity> userEntities = UserMapper.INSTANCE.x2yList(userIns);
        userService.saveAll(userEntities);
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
    public UserOut updateUser(@RequestBody UserIn userIn) {
        UserEntity userEntity = UserMapper.INSTANCE.x2y(userIn);
        UserEntity updatedUser = userService.update(userEntity);
        return UserMapper.INSTANCE.y2z(updatedUser);
    }
}
