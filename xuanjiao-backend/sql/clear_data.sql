-- 清除图片素材和审批流程数据
-- 执行前请确保已备份数据库
-- 此脚本仅删除数据，不删除表结构，不影响后续新增操作

USE xuanjiao_s;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 审批流程相关数据清除 ====================

-- 清空审批任务表
TRUNCATE TABLE approval_task;

-- 清空审批实例表
TRUNCATE TABLE approval_instance;

-- 清空阶段审批人配置表
TRUNCATE TABLE stage_approver;

-- 清空审批流程阶段表
TRUNCATE TABLE workflow_stage;

-- 清空审批流程定义表
TRUNCATE TABLE workflow;

-- ==================== 图片/素材相关数据清除 ====================

-- 清空使用日志表
TRUNCATE TABLE usage_log;

-- 清空素材使用申请表（如果存在）
-- TRUNCATE TABLE usage_apply;

-- 清空素材申请表
TRUNCATE TABLE material_application;

-- 清空素材表
TRUNCATE TABLE asset;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 显示清除结果
SELECT '数据清除完成' AS status;
SELECT COUNT(*) AS remaining_workflows FROM workflow;
SELECT COUNT(*) AS remaining_assets FROM asset;
SELECT COUNT(*) AS remaining_instances FROM approval_instance;
