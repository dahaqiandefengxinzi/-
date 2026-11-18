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

-- 插入默认配置
INSERT INTO `captcha_config` (`enabled`, `captcha_type`, `expire_time`, `max_retry_count`, `lock_time`, `refresh_interval`, `ip_limit_count`) 
VALUES (0, 'GRAPH', 300, 5, 300, 60, 10);
