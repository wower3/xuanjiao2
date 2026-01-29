-- 扩展 asset 表，支持每个素材单独配置使用信息
USE xuanjiao_s;

-- 添加使用相关字段到 asset 表
ALTER TABLE asset
ADD COLUMN usage_description VARCHAR(500) DEFAULT NULL COMMENT '使用申请说明' AFTER copyright,
ADD COLUMN usage_publish_channel VARCHAR(200) DEFAULT NULL COMMENT '使用发布渠道',
ADD COLUMN usage_is_secondary_creation TINYINT DEFAULT 0 COMMENT '使用时是否二次创作:0-否,1-是',
ADD COLUMN usage_attachment_path VARCHAR(500) DEFAULT NULL COMMENT '使用附件路径',
ADD COLUMN usage_apply_id BIGINT DEFAULT NULL COMMENT '关联的使用申请ID',
ADD INDEX idx_usage_apply_id (usage_apply_id);

-- 清理之前创建的中间表（不再需要）
DROP TABLE IF EXISTS usage_apply_asset;

-- 从 usage_apply 表中移除不再需要的字段
-- 注意：保留 title, dept_id 等基本信息
