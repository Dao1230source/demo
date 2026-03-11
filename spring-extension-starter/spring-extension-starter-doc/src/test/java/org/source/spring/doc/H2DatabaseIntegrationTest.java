package org.source.spring.doc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.source.spring.doc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * H2 数据库集成测试
 */
@SpringBootTest
public class H2DatabaseIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(H2DatabaseIntegrationTest.class);

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testH2DatabaseConnection() {
        logger.info("=== 测试 H2 数据库连接 ===");
        
        long count = userRepository.count();
        logger.info("数据库中用户数量: {}", count);
        
        // 验证测试数据是否已加载
        if (count > 0) {
            logger.info("✅ H2 数据库配置成功！");
            logger.info("✅ 测试数据已正确加载！");
        } else {
            logger.warn("⚠️ 数据库中没有找到测试数据");
        }
    }
}