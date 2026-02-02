-- ========================================
-- 修复历史子流程数据
-- ⚠️ 警告：执行前请先备份数据库！
-- ⚠️ 建议先执行 check_invalid_subworkflows.sql 确认问题数据
-- ========================================

-- 使用说明：
-- 1. 先执行 check_invalid_subworkflows.sql 查看问题数据
-- 2. 确认后，取消下面的注释并执行对应的修复语句
-- 3. 建议分批执行，每次只处理特定的实例

-- ========================================
-- 方案1：取消特定主流程的所有未完成子流程
-- ========================================
-- 示例：取消主流程实例 ID = 100 的所有未完成子流程
-- 将 100 替换为实际的主流程实例ID

/*
-- 取消子流程实例
UPDATE approval_instance
SET status = 'CANCELLED'
WHERE parent_instance_id = 100
  AND status IN ('PENDING', 'APPROVED', 'MAIN_COMPLETED');

-- 取消子流程的所有任务
UPDATE approval_task
SET status = 'CANCELLED'
WHERE instance_id IN (
    SELECT id FROM approval_instance
    WHERE parent_instance_id = 100
);

-- 更新子流程的进度记录
UPDATE approval_progress
SET status = 'CANCELLED'
WHERE instance_id IN (
    SELECT id FROM approval_instance
    WHERE parent_instance_id = 100
);
*/

-- ========================================
-- 方案2：批量取消所有可能有问题的子流程
-- ========================================
-- ⚠️ 这个脚本会取消所有 PENDING 状态主流程的 APPROVED/PENDING 子流程
-- ⚠️ 执行前务必确认！

/*
-- 取消所有可能有问题的子流程实例
UPDATE approval_instance sub
INNER JOIN approval_instance main ON sub.parent_instance_id = main.id
SET sub.status = 'CANCELLED'
WHERE main.status = 'PENDING'
  AND sub.status IN ('PENDING', 'APPROVED', 'MAIN_COMPLETED');

-- 取消对应的子流程任务
UPDATE approval_task
SET status = 'CANCELLED'
WHERE instance_id IN (
    SELECT sub.id FROM approval_instance sub
    INNER JOIN approval_instance main ON sub.parent_instance_id = main.id
    WHERE main.status = 'PENDING'
      AND sub.status = 'CANCELLED'
);

-- 更新子流程的进度记录
UPDATE approval_progress
SET status = 'CANCELLED'
WHERE instance_id IN (
    SELECT sub.id FROM approval_instance sub
    WHERE sub.status = 'CANCELLED'
);
*/

-- ========================================
-- 验证修复结果
-- ========================================
-- 执行修复后，运行此查询验证结果
SELECT
    main.id as main_instance_id,
    main.status as main_status,
    COUNT(DISTINCT sub.id) as total_sub_workflows,
    SUM(CASE WHEN sub.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled,
    SUM(CASE WHEN sub.status = 'APPROVED' THEN 1 ELSE 0 END) as approved,
    SUM(CASE WHEN sub.status = 'PENDING' THEN 1 ELSE 0 END) as pending
FROM approval_instance main
LEFT JOIN approval_instance sub ON sub.parent_instance_id = main.id
WHERE main.status = 'PENDING'
  AND main.id = 100  -- 替换为验证的主流程实例ID
GROUP BY main.id;
