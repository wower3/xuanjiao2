-- =====================================================
-- 子流程重构：将子流程从阶段级别移到审批人级别
-- =====================================================

-- 1. 在 stage_approver 表添加 sub_workflow_id 字段
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'xuanjiao_s' AND TABLE_NAME = 'stage_approver' AND COLUMN_NAME = 'sub_workflow_id');

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE stage_approver ADD COLUMN sub_workflow_id BIGINT DEFAULT NULL COMMENT ''关联的子流程ID（如果该审批人是子流程）''',
    'SELECT "Column sub_workflow_id already exists in stage_approver" AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 添加索引
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = 'xuanjiao_s' AND TABLE_NAME = 'stage_approver' AND INDEX_NAME = 'idx_sub_workflow_id');

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE stage_approver ADD INDEX idx_sub_workflow_id (sub_workflow_id)',
    'SELECT "Index idx_sub_workflow_id already exists in stage_approver" AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 删除 workflow_stage 表的子流程相关字段（如果存在的话）
-- 注意：先检查字段是否存在，存在才删除
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'xuanjiao_s' AND TABLE_NAME = 'workflow_stage' AND COLUMN_NAME = 'is_sub_workflow');

SET @sql = IF(@column_exists > 0,
    'ALTER TABLE workflow_stage DROP COLUMN is_sub_workflow',
    'SELECT "Column is_sub_workflow does not exist, skipping" AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'xuanjiao_s' AND TABLE_NAME = 'workflow_stage' AND COLUMN_NAME = 'sub_workflow_id');

SET @sql = IF(@column_exists > 0,
    'ALTER TABLE workflow_stage DROP COLUMN sub_workflow_id',
    'SELECT "Column sub_workflow_id does not exist, skipping" AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- 数据迁移：将现有的子流程配置迁移到 stage_approver
-- =====================================================

-- 迁移 approval_progress 表的 parent_stage_id 改为 parent_task_id
-- 这样可以更准确地追踪是哪个任务触发了子流程
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'xuanjiao_s' AND TABLE_NAME = 'approval_progress' AND COLUMN_NAME = 'parent_stage_id');

SET @sql = IF(@column_exists > 0,
    'ALTER TABLE approval_progress CHANGE COLUMN parent_stage_id parent_task_id BIGINT COMMENT "父任务ID（如果是子流程的话）"',
    'SELECT "Column parent_stage_id does not exist, skipping rename" AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================
-- 说明
-- =====================================================
-- 1. stage_approver 表现在支持两种类型的审批人：
--    - 普通审批人：sub_workflow_id IS NULL
--    - 子流程审批人：sub_workflow_id IS NOT NULL
--
-- 2. workflow_stage 表不再有 is_sub_workflow 和 sub_workflow_id 字段
--
-- 3. 一个阶段可以有：
--    - 多个普通审批人（USER/ROLE/DEPT 类型）
--    - 多个子流程（sub_workflow_id 不为空）
--    - 混合配置
--
-- 4. 至少需要一个普通审批人来保证主流程正常运转
-- =====================================================

-- 5. 将 approval_instance 表的 parent_stage_id 改为 parent_task_id
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'xuanjiao_s' AND TABLE_NAME = 'approval_instance' AND COLUMN_NAME = 'parent_stage_id');

SET @sql = IF(@column_exists > 0,
    'ALTER TABLE approval_instance CHANGE COLUMN parent_stage_id parent_task_id BIGINT COMMENT "父任务ID（用于子流程，记录是哪个任务触发的）"',
    'SELECT "Column parent_stage_id does not exist in approval_instance, checking if parent_task_id exists" AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果 parent_task_id 不存在，则添加它
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'xuanjiao_s' AND TABLE_NAME = 'approval_instance' AND COLUMN_NAME = 'parent_task_id');

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE approval_instance ADD COLUMN parent_task_id BIGINT DEFAULT NULL COMMENT "父任务ID（用于子流程，记录是哪个任务触发的）" AFTER parent_instance_id',
    'SELECT "Column parent_task_id already exists in approval_instance" AS Info');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
