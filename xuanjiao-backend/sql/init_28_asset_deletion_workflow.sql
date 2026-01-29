-- ============================================================
-- 素材删除审批流程配置
-- 日期: 2025-01-28
-- 说明: 为素材删除功能配置审批流程
-- ============================================================

SET NAMES utf8mb4;
USE xuanjiao_s;

-- 1. 创建素材删除审批流程（单级审批 - 部门主管）
INSERT INTO workflow (name, description, version, status, bound_role_id, workflow_type)
VALUES ('素材删除审批流程', '素材删除申请需要部门主管审批', 1, 1, 1, 'ASSET_DELETION');

SET @deletion_workflow = LAST_INSERT_ID();

-- 2. 添加审批阶段
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type)
VALUES (@deletion_workflow, '部门主管审批', 1, 'OR');

SET @deletion_stage = LAST_INSERT_ID();

-- 3. 添加审批人配置（系统管理员角色）
-- approver_type: ROLE-角色, DEPT-部门, USER-用户
INSERT INTO stage_approver (stage_id, approver_type, approver_id, check_secondary_dept)
VALUES (@deletion_stage, 'ROLE', 1, 0);

-- 4. 为所有需要删除权限的角色添加菜单权限
-- 确保素材删除菜单对相关角色可见
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.deleted = 0
  AND m.path = '/asset/deletion'
  AND r.id IN (1, 4, 5, 7, 8);  -- 系统管理员、总消保管理岗、分消保管理岗等

-- 验证配置
SELECT '=== 素材删除工作流配置 ===' AS '';
SELECT
    wf.id AS workflow_id,
    wf.name AS workflow_name,
    wf.bound_role_id,
    wf.workflow_type,
    ws.id AS stage_id,
    ws.name AS stage_name,
    ws.approve_type,
    sa.approver_type,
    sa.approver_id
FROM workflow wf
LEFT JOIN workflow_stage ws ON ws.workflow_id = wf.id
LEFT JOIN stage_approver sa ON sa.stage_id = ws.id
WHERE wf.workflow_type = 'ASSET_DELETION';
