-- 修复素材使用申请表结构，支持多素材申请
-- 原因：新架构中一个使用申请可关联多个素材，通过 asset.usage_apply_id 反向关联
-- 所以 usage_apply 表的 asset_id 字段应改为可空

USE xuanjiao_s;

-- 1. 修改 asset_id 字段为可空
ALTER TABLE usage_apply
MODIFY COLUMN asset_id BIGINT DEFAULT NULL COMMENT '素材ID（保留用于兼容旧数据，新架构使用asset.usage_apply_id关联）';

-- 2. 添加新字段支持多素材申请
ALTER TABLE usage_apply
ADD COLUMN title VARCHAR(200) DEFAULT NULL COMMENT '申请标题' AFTER user_id,
ADD COLUMN attachment_path VARCHAR(500) DEFAULT NULL COMMENT '附件路径' AFTER workflow_id,
ADD COLUMN is_secondary_creation TINYINT DEFAULT 0 COMMENT '是否二次创作:0-否,1-是' AFTER attachment_path,
ADD COLUMN publish_channel VARCHAR(200) DEFAULT NULL COMMENT '发布渠道' AFTER is_secondary_creation,
ADD COLUMN dept_id BIGINT DEFAULT NULL COMMENT '申请部门ID' AFTER publish_channel,
ADD COLUMN draft TINYINT DEFAULT 0 COMMENT '是否草稿:0-已提交,1-草稿' AFTER dept_id;

-- 3. 修改 purpose 为可空（草稿时可不填）
ALTER TABLE usage_apply
MODIFY COLUMN purpose VARCHAR(500) DEFAULT NULL COMMENT '使用用途';

-- 4. 修改 scope 为可空（草稿时可不填）
ALTER TABLE usage_apply
MODIFY COLUMN scope VARCHAR(200) DEFAULT NULL COMMENT '使用范围';
