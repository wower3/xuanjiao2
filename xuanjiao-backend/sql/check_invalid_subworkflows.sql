-- ========================================
-- 查询可能有问题的子流程数据
-- 用途：找出主流程已退回，但子流程状态仍为 APPROVED/PENDING 的记录
-- ========================================

-- 查询1：查看所有子流程实例及其父实例的状态
SELECT
    sub.id as sub_instance_id,
    sub.business_id,
    sub.status as sub_status,
    sub.parent_instance_id,
    sub.parent_task_id,
    sub.create_time as sub_create_time,
    main.id as main_instance_id,
    main.status as main_status,
    main.current_stage_id,
    main.create_time as main_create_time
FROM approval_instance sub
INNER JOIN approval_instance main ON sub.parent_instance_id = main.id
WHERE sub.status IN ('APPROVED', 'PENDING', 'MAIN_COMPLETED')
  AND main.status = 'PENDING'
ORDER BY sub.id DESC
LIMIT 100;

-- 查询2：查看特定的子流程任务状态
SELECT
    t.id as task_id,
    t.instance_id,
    t.stage_id,
    s.stage_order,
    s.name as stage_name,
    t.status as task_status,
    t.create_time
FROM approval_task t
JOIN workflow_stage s ON t.stage_id = s.id
WHERE t.instance_id IN (
    SELECT sub.id
    FROM approval_instance sub
    INNER JOIN approval_instance main ON sub.parent_instance_id = main.id
    WHERE sub.status IN ('APPROVED', 'PENDING', 'MAIN_COMPLETED')
      AND main.status = 'PENDING'
)
ORDER BY t.instance_id, t.id;

-- 查询3：统计每个主流程有多少子流程
SELECT
    main.id as main_instance_id,
    main.business_id,
    main.status as main_status,
    COUNT(DISTINCT sub.id) as sub_workflow_count,
    SUM(CASE WHEN sub.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_count,
    SUM(CASE WHEN sub.status = 'APPROVED' THEN 1 ELSE 0 END) as approved_count,
    SUM(CASE WHEN sub.status = 'PENDING' THEN 1 ELSE 0 END) as pending_count
FROM approval_instance main
LEFT JOIN approval_instance sub ON sub.parent_instance_id = main.id
WHERE main.status = 'PENDING'
GROUP BY main.id
HAVING sub_workflow_count > 0
ORDER BY main.id DESC;
