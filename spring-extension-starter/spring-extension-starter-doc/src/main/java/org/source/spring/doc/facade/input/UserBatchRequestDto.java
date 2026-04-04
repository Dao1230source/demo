package org.source.spring.doc.facade.input;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 用户批量操作请求数据传输对象
 *
 * <p>用于批量创建或更新用户时的请求数据封装。</p>
 * <p>包含用户列表，支持一次请求处理多个用户数据。</p>
 *
 * @author System Admin
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBatchRequestDto {

    /**
     * 用户列表
     *
     * <p>包含一个或多个用户输入数据，用于批量操作。</p>
     */
    private List<UserIn> users;
}