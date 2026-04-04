package org.source.spring.doc.app;

import lombok.AllArgsConstructor;
import org.source.spring.doc.domain.service.UserService;
import org.springframework.stereotype.Component;

/**
 * 用户应用层
 *
 * <p>
 * 应用层负责协调多个领域服务，处理跨领域的业务逻辑。
 * 在简单场景下，逻辑过于简单时可以省略 app 这一步，直接在 Facade 层调用 Service。
 * </p>
 *
 * <p>
 * 使用场景：当业务逻辑涉及多个领域服务的协调、事务管理、
 * 或需要组合多个领域对象的复杂操作时使用。
 * </p>
 *
 * @author source
 * @since 1.0.0
 */
@AllArgsConstructor
@Component
public class UserApp {

    /**
     * 用户领域服务
     */
    private final UserService userService;
}
