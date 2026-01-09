-- 使用日志表
DROP TABLE IF EXISTS usage_log;
CREATE TABLE usage_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_asset_id (asset_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用日志表';
