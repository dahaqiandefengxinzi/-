-- Banner表迁移脚本
-- 请在MySQL客户端中执行此脚本

USE shop_db;

-- 创建Banner表
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

-- 插入初始Banner数据
INSERT INTO `banner` (`title`, `description`, `image_url`, `link_url`, `sort`, `status`, `interval_time`) VALUES
('新品上市', '精选好物，品质保证', 'https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=1200&h=400&fit=crop', '/product', 1, 1, 5000),
('限时特惠', '全场低至5折起', 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=1200&h=400&fit=crop', '/product', 2, 1, 5000),
('品质生活', '精选好物，品质保证', 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=1200&h=400&fit=crop', '/product', 3, 1, 5000);
