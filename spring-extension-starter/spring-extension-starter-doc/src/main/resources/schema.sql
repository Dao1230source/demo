-- 用户表
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 插入测试数据
INSERT INTO users (username, email, status, created_at, updated_at) VALUES
('alice', 'alice@example.com', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('bob', 'bob@example.com', 'ACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('charlie', 'charlie@example.com', 'INACTIVE', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());