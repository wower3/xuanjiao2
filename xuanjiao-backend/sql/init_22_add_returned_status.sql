-- ============================================================
-- 审批流程退回上一级功能 - 数据库变更
-- 日期: 2025-01-20
-- 说明: 支持退回上一级功能，新增 RETURNED 状态
-- ============================================================

-- 更新 approval_progress 表状态注释，新增 RETURNED 状态
ALTER TABLE approval_progress
MODIFY COLUMN status VARCHAR(20) DEFAULT 'PENDING'
COMMENT '状态：PENDING-待审批，APPROVED-已通过，REJECTED-已驳回，CANCELLED-已取消，RETURNED-已退回';

-- 注意：由于 MySQL 的 MODIFY COLUMN 会自动扩展 VARCHAR 长度，
-- 如果之前状态字段长度不足，可以手动调整（通常 VARCHAR(20) 已经足够）
