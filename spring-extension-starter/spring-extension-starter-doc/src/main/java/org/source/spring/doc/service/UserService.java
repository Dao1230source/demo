package org.source.spring.doc.service;

import org.source.spring.doc.dto.UserDto;
import org.source.spring.doc.dto.UserVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务接口
 * 
 * <p>提供用户相关的业务逻辑处理</p>
 */
@Service
public interface UserService {

    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return 用户VO对象
     */
    UserVo getUserById(Long id);

    /**
     * 获取所有用户列表
     * 
     * @return 用户VO列表
     */
    List<UserVo> getAllUsers();

    /**
     * 创建新用户
     * 
     * @param userDto 用户DTO
     * @return 创建的用户VO
     */
    UserVo createUser(UserDto userDto);

    /**
     * 批量创建用户
     * 
     * @param userDtos 用户DTO列表
     * @return 创建的用户VO列表
     */
    List<UserVo> createUsers(List<UserDto> userDtos);

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param userDto 用户DTO
     * @return 更新后的用户VO
     */
    UserVo updateUser(Long id, UserDto userDto);
}