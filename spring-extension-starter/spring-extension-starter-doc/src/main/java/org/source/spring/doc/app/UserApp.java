package org.source.spring.doc.app;

import lombok.AllArgsConstructor;
import org.source.spring.doc.domain.service.UserService;
import org.springframework.stereotype.Component;

/**
 * 逻辑过于简单 省略 app 这一步
 */
@AllArgsConstructor
@Component
public class UserApp {
    private final UserService userService;
}
