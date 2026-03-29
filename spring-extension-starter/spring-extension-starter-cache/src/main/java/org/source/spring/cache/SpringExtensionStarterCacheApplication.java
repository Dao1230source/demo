package org.source.spring.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableExtendedCache
@SpringBootApplication
public class SpringExtensionStarterCacheApplication {

    /**
     * 应用入口：启动 Spring Boot 应用并启用扩展缓存功能。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringExtensionStarterCacheApplication.class, args);
    }

}
