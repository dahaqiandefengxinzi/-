-- 创建数据库
CREATE DATABASE IF NOT EXISTS shop_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE shop_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别（0-未知，1-男，2-女）',
    `birthday` DATE COMMENT '生日',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    `role` TINYINT DEFAULT 0 COMMENT '角色（0-普通用户，1-管理员）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
    `level` TINYINT NOT NULL COMMENT '分类级别',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `icon` VARCHAR(255) COMMENT '分类图标',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `subtitle` VARCHAR(255) COMMENT '商品副标题',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `stock` INT NOT NULL COMMENT '商品库存',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `brand_id` BIGINT COMMENT '品牌ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-下架，1-上架）',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `main_image` VARCHAR(255) COMMENT '主图',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品详情表
CREATE TABLE IF NOT EXISTS `product_detail` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '详情ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `description` TEXT COMMENT '商品描述',
    `specs` JSON COMMENT '商品规格',
    `images` JSON COMMENT '商品图片'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品详情表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL COMMENT '商品数量',
    `selected` TINYINT DEFAULT 1 COMMENT '是否选中（0-未选中，1-选中）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_sn` VARCHAR(32) UNIQUE COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态（0-待付款，1-待发货，2-待收货，3-已完成，4-已取消，5-退款中）',
    `payment_type` TINYINT COMMENT '支付方式（1-支付宝，2-微信支付）',
    `shipping_address` JSON COMMENT '收货地址',
    `shipping_company` VARCHAR(50) COMMENT '物流公司',
    `shipping_number` VARCHAR(50) COMMENT '物流单号',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `pay_time` DATETIME COMMENT '支付时间',
    `delivery_time` DATETIME COMMENT '发货时间',
    `receive_time` DATETIME COMMENT '收货时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单商品表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单商品ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `product_price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `quantity` INT NOT NULL COMMENT '商品数量',
    `product_image` VARCHAR(255) COMMENT '商品图片'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品表';

-- 地址表
CREATE TABLE IF NOT EXISTS `address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址（0-否，1-是）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址表';

-- Banner表
CREATE TABLE IF NOT EXISTS `banner` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Banner ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `description` VARCHAR(255) COMMENT '描述',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `link_url` VARCHAR(500) COMMENT '跳转链接',
    `sort` INT DEFAULT 0 COMMENT '排序（数字越小越靠前）',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    `interval_time` INT DEFAULT 5000 COMMENT '轮播间隔时间（毫秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner轮播图表';

-- 插入测试数据

-- 插入管理员用户（密码：admin123，使用BCrypt加密）
-- BCrypt加密后的密码
INSERT INTO `user` (`username`, `password`, `nickname`, `email`, `phone`, `status`, `role`, `create_time`, `update_time`) VALUES
('admin', '$2a$10$AB96rYAB42GqbW9M2ehSne7hPzPEJvsNcySNheuno.hh2748uAz1m', '管理员', 'admin@shop.com', '13800138000', 1, 1, NOW(), NOW());

-- 插入分类数据
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort`, `status`) VALUES
('数码产品', 0, 1, 1, 1),
('服装鞋帽', 0, 1, 2, 1),
('家居用品', 0, 1, 3, 1),
('手机', 1, 2, 1, 1),
('电脑', 1, 2, 2, 1),
('男装', 2, 2, 1, 1),
('女装', 2, 2, 2, 1);

-- 插入商品数据
INSERT INTO `product` (`name`, `subtitle`, `price`, `stock`, `category_id`, `status`, `sales`, `main_image`) VALUES
('iPhone 15 Pro', '全新钛金属设计', 7999.00, 100, 4, 1, 50, 'https://via.placeholder.com/300'),
('MacBook Pro 14', 'M3 Pro芯片', 14999.00, 50, 5, 1, 30, 'https://via.placeholder.com/300'),
('男士休闲外套', '春季新款', 299.00, 200, 6, 1, 100, 'https://via.placeholder.com/300'),
('女士连衣裙', '夏季新款', 199.00, 150, 7, 1, 80, 'https://via.placeholder.com/300'),
('智能手表', '健康监测', 1299.00, 80, 4, 1, 40, 'https://via.placeholder.com/300');

-- 插入商品详情
INSERT INTO `product_detail` (`product_id`, `description`, `images`, `specs`) VALUES
(1, 'iPhone 15 Pro 采用全新钛金属设计，配备 A17 Pro 芯片，性能强劲。', '["https://via.placeholder.com/300", "https://via.placeholder.com/300"]', '{"color": "深空黑", "storage": "256GB"}'),
(2, 'MacBook Pro 14 英寸，M3 Pro 芯片，性能与续航的完美平衡。', '["https://via.placeholder.com/300", "https://via.placeholder.com/300"]', '{"color": "深空灰", "memory": "18GB", "storage": "512GB"}'),
(3, '男士休闲外套，春季新款，舒适透气。', '["https://via.placeholder.com/300"]', '{"color": "黑色", "size": "L"}'),
(4, '女士连衣裙，夏季新款，优雅时尚。', '["https://via.placeholder.com/300"]', '{"color": "白色", "size": "M"}'),
(5, '智能手表，支持健康监测，运动追踪。', '["https://via.placeholder.com/300"]', '{"color": "银色", "size": "45mm"}');

-- 验证码配置表
CREATE TABLE IF NOT EXISTS `captcha_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `enabled` TINYINT DEFAULT 0 COMMENT '是否启用验证码（0-关闭，1-开启）',
    `captcha_type` VARCHAR(20) DEFAULT 'GRAPH' COMMENT '验证码类型：GRAPH-图形，SLIDE-滑动拼图，CLICK_TEXT-文字点选，CALC-计算题',
    `expire_time` INT DEFAULT 300 COMMENT '验证码有效期（秒）',
    `max_retry_count` INT DEFAULT 5 COMMENT '最大错误次数',
    `lock_time` INT DEFAULT 300 COMMENT '锁定时间（秒）',
    `refresh_interval` INT DEFAULT 60 COMMENT '刷新间隔（秒）',
    `ip_limit_count` INT DEFAULT 10 COMMENT 'IP每小时最大请求次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码配置表';

-- 验证码记录表
CREATE TABLE IF NOT EXISTS `captcha_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `captcha_id` VARCHAR(64) NOT NULL COMMENT '验证码唯一标识',
    `captcha_type` VARCHAR(20) NOT NULL COMMENT '验证码类型',
    `captcha_key` VARCHAR(255) NOT NULL COMMENT '验证码答案（加密存储）',
    `user_id` BIGINT COMMENT '用户ID',
    `ip` VARCHAR(50) COMMENT '请求IP',
    `retry_count` INT DEFAULT 0 COMMENT '重试次数',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-未验证，1-成功，2-失败',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码记录表';

-- 插入默认验证码配置
INSERT INTO `captcha_config` (`enabled`, `captcha_type`, `expire_time`, `max_retry_count`, `lock_time`, `refresh_interval`, `ip_limit_count`) 
VALUES (0, 'GRAPH', 300, 5, 300, 60, 10);

-- 插入Banner数据
INSERT INTO `banner` (`title`, `description`, `image_url`, `link_url`, `sort`, `status`, `interval_time`) VALUES
('新品上市', '精选好物，品质保证', 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=1200&h=400&fit=crop', '/product', 1, 1, 5000),
('限时特惠', '全场低至5折起', 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=1200&h=400&fit=crop', '/product', 2, 1, 5000),
('品质生活', '精选好物，品质保证', 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200&h=400&fit=crop', '/product', 3, 1, 5000);
