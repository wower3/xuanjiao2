-- ============================================================
-- 审批流程任务类型扩展 - 数据库变更
-- 日期: 2025-01-20
-- 说明: 支持子流程重新发起功能，新增 task_type 字段
-- ============================================================

-- 添加 task_type 字段到 approval_task 表
ALTER TABLE approval_task
ADD COLUMN task_type VARCHAR(20) DEFAULT 'NORMAL'
COMMENT '任务类型：NORMAL-普通审批任务，RESTART_SUB_WORKFLOW-重新发起子流程';

-- 为现有数据设置默认值（可选，现有数据已经是 NORMAL）
UPDATE approval_task SET task_type = 'NORMAL' WHERE task_type IS NULL;
