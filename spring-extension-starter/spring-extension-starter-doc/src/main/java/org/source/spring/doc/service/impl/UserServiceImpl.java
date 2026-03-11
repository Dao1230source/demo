package org.source.spring.doc.service.impl;

import org.source.spring.doc.dto.UserDto;
import org.source.spring.doc.dto.UserVo;
import org.source.spring.doc.entity.UserEntity;
import org.source.spring.doc.repository.UserRepository;
import org.source.spring.doc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * <p>用户服务的简单实现，用于测试目的</p>
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserVo getUserById(Long id) {
        if (id == null) {
            return null;
        }
        UserEntity userEntity = userRepository.findById(id).orElse(null);
        if (userEntity == null) {
            return null;
        }
        return convertToVo(userEntity);
    }

    @Override
    public List<UserVo> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    @Override
    public UserVo createUser(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDto.getUsername());
        userEntity.setEmail(userDto.getEmail());
        // 状态默认为ACTIVE
        userEntity.setStatus(org.source.spring.doc.entity.UserStatus.ACTIVE);
        userEntity.setCreatedAt(java.time.LocalDateTime.now());
        userEntity.setUpdatedAt(java.time.LocalDateTime.now());
        
        UserEntity savedUser = userRepository.save(userEntity);
        return convertToVo(savedUser);
    }

    @Override
    public List<UserVo> createUsers(List<UserDto> userDtos) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(userDtos)) {
            return Collections.emptyList();
        }
        return userDtos.stream()
                .map(this::createUser)
                .filter(userVo -> userVo != null)
                .collect(Collectors.toList());
    }

    @Override
    public UserVo updateUser(Long id, UserDto userDto) {
        if (id == null || userDto == null) {
            return null;
        }
        UserEntity existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return null;
        }
        existingUser.setUsername(userDto.getUsername());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setUpdatedAt(java.time.LocalDateTime.now());
        
        UserEntity updatedUser = userRepository.save(existingUser);
        return convertToVo(updatedUser);
    }

    private UserVo convertToVo(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        UserVo userVo = new UserVo();
        userVo.setId(userEntity.getId());
        userVo.setUsername(userEntity.getUsername());
        userVo.setEmail(userEntity.getEmail());
        userVo.setStatusDescription(userEntity.getStatus().name());
        return userVo;
    }
}