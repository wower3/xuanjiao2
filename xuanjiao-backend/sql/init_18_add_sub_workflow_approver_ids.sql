-- =====================================================
-- 添加子流程审批人选择字段
-- =====================================================

-- 在 approval_task 表添加 sub_workflow_approver_ids 字段
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'xuanjiao_s' AND TABLE_NAME = 'approval_task' AND COLUMN_NAME = 'sub_workflow_approver_ids');

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE approval_task ADD COLUMN sub_workflow_approver_ids TEXT DEFAULT NULL COMMENT ''子流程第一层审批人IDs（JSON格式，key为子流程ID，value为审批人ID列表）''',
    'SELECT "Column sub_workflow_approver_ids already exists in approval_task" AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
