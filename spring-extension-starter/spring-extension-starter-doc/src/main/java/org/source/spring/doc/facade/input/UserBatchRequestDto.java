package org.source.spring.doc.facade.input;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 用户批量操作请求DTO
 * 
 * <p>用于批量创建或更新用户</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBatchRequestDto {

    /**
     * 用户列表
     */
    private List<UserIn> users;
}