-- 回滚错误设计，恢复正确的中间表架构
-- 日期: 2024-01-23
-- 原因: 一个素材可以被多个申请单使用，需要多对多关系

USE xuanjiao_s;

-- ============================================
-- 第一步：修改usage_apply表结构
-- ============================================
-- 删除asset_id字段（新架构通过中间表关联）
ALTER TABLE usage_apply
DROP COLUMN asset_id;

-- 修改 purpose 和 scope 为可空（草稿时可不填）
ALTER TABLE usage_apply
MODIFY COLUMN purpose VARCHAR(500) DEFAULT NULL COMMENT '使用用途',
MODIFY COLUMN scope VARCHAR(200) DEFAULT NULL COMMENT '使用范围';

-- ============================================
-- 第二步：创建usage_apply_asset中间表（多对多）
-- ============================================
DROP TABLE IF EXISTS usage_apply_asset;

CREATE TABLE usage_apply_asset (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_apply_id BIGINT NOT NULL COMMENT '使用申请ID',
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    usage_description VARCHAR(500) DEFAULT NULL COMMENT '该素材的使用说明',
    usage_publish_channel VARCHAR(200) DEFAULT NULL COMMENT '该素材的发布渠道',
    usage_is_secondary_creation TINYINT DEFAULT 0 COMMENT '该素材是否二次创作:0-否,1-是',
    usage_attachment_path VARCHAR(500) DEFAULT NULL COMMENT '该素材的附件路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_apply_asset (usage_apply_id, asset_id),
    KEY idx_asset_id (asset_id),
    KEY idx_usage_apply_id (usage_apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材使用申请-素材关联表（多对多）';

-- ============================================
-- 说明
-- ============================================
-- 1. 一个申请单可以包含多个素材 (usage_apply_id -> 多个 asset_id)
-- 2. 一个素材可以被多个申请单使用 (asset_id -> 多个 usage_apply_id)
-- 3. 每个申请单对每个素材有独立的配置说明
-- 4. usage_log 表的扩展字段保留 (init_23)，用于记录下载时的使用信息
