-- 扩展 usage_log 表，添加使用申请相关字段
ALTER TABLE usage_log
ADD COLUMN dept_name VARCHAR(100) DEFAULT NULL COMMENT '部门名称' AFTER ip,
ADD COLUMN usage_description VARCHAR(500) DEFAULT NULL COMMENT '使用申请说明',
ADD COLUMN usage_publish_channel VARCHAR(200) DEFAULT NULL COMMENT '使用发布渠道';
