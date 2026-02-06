-- 流经事项优化查询SQL
-- 创建一个视图或直接在Mapper中使用JOIN查询

-- 方案1：创建视图（推荐，便于维护）
CREATE OR REPLACE VIEW v_user_flow_items AS
SELECT
    ai.id,
    ai.status,
    ai.business_type,
    ai.business_id,
    ai.create_time,
    ai.applicant_id,
    ai.workflow_id,
    w.name AS workflow_name,
    u.real_name AS applicant_name,
    CASE
        WHEN at.id IS NOT NULL THEN 'approver'
        ELSE 'initiator'
    END AS my_role,
    -- 业务类型相关字段
    ma.id AS material_application_id,
    ma.title AS application_title,
    da.id AS deletion_application_id,
    da.title AS deletion_title,
    ua.id AS usage_application_id,
    ua.title AS usage_title
FROM approval_instance ai
INNER JOIN sys_user u ON ai.applicant_id = u.id
INNER JOIN workflow w ON ai.workflow_id = w.id
LEFT JOIN approval_task at ON at.instance_id = ai.id AND at.approver_id = ?  -- 这里的?是当前用户ID
LEFT JOIN material_application ma ON ai.business_type = 'MATERIAL_ENTRY' AND ai.business_id = ma.id
LEFT JOIN asset_deletion_application da ON ai.business_type = 'ASSET_DELETION' AND ai.business_id = da.id
LEFT JOIN usage_apply ua ON ai.business_type = 'ASSET_USAGE' AND ai.business_id = ua.id
WHERE
    ai.parent_instance_id IS NULL  -- 只查主流程
    AND (
        ai.applicant_id = ?  -- 发起人的流程
        OR at.id IS NOT NULL  -- 用户作为审批人的流程
    )
ORDER BY ai.create_time DESC;
