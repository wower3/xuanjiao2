-- 素材使用申请表
CREATE TABLE IF NOT EXISTS usage_apply (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    user_id BIGINT NOT NULL COMMENT '申请用户ID',
    purpose VARCHAR(500) NOT NULL COMMENT '使用用途',
    scope VARCHAR(200) NOT NULL COMMENT '使用范围',
    workflow_id BIGINT DEFAULT NULL COMMENT '审批流程ID（为空则无需审批）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '申请状态：PENDING/APPROVED/REJECTED/CANCELLED',
    approval_instance_id BIGINT DEFAULT NULL COMMENT '关联审批实例ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_asset_user (asset_id, user_id),
    KEY idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材使用申请表';

-- 修改workflow表添加流程类型字段
ALTER TABLE workflow ADD COLUMN type VARCHAR(20) DEFAULT 'ASSET_UPLOAD' COMMENT '流程类型：ASSET_UPLOAD-素材录入, ASSET_USAGE-素材使用' AFTER status;

-- 更新现有流程数据的类型
UPDATE workflow SET type = 'ASSET_UPLOAD' WHERE type IS NULL;
