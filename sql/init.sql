-- ============================================================
-- Java Review Demo 数据库初始化脚本
-- 适用于 MySQL 8.0
-- ============================================================

-- 强制指定客户端连接字符集
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS java_review
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE java_review;


DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS orders;

-- ==================== 商品表 ====================
CREATE TABLE IF NOT EXISTS product (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(200)   NOT NULL COMMENT '商品名称',
    description VARCHAR(1000)  DEFAULT '' COMMENT '商品描述',
    price       DECIMAL(10, 2) NOT NULL COMMENT '价格',
    stock       INT            NOT NULL DEFAULT 0 COMMENT '库存',
    create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ==================== 订单表 ====================
CREATE TABLE IF NOT EXISTS orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no     VARCHAR(64)    NOT NULL COMMENT '业务订单号（幂等键）',
    product_id   BIGINT         NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200)   NOT NULL COMMENT '商品名称（冗余存储，避免跨服务查询）',
    price        DECIMAL(10, 2) NOT NULL COMMENT '下单时单价（快照）',
    quantity     INT            NOT NULL COMMENT '购买数量',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    status       TINYINT        NOT NULL DEFAULT 0 COMMENT '状态：0-待支付 1-已支付 2-已取消',
    create_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT        NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-正常 1-已删除',
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ==================== 测试数据 ====================
INSERT INTO product (name, description, price, stock) VALUES
    ('Java 编程思想', '经典 Java 书籍，深入理解面向对象', 108.00, 100),
    ('Spring Boot 实战', 'Spring Boot 从入门到精通', 79.00, 200),
    ('深入理解 JVM 虚拟机', '周志明著，JVM 必读', 99.00, 150);
