-- Fix Asset Deletion Menu Issues
-- Date: 2025-01-28
-- Description: Fix parent_id and add role associations for the deletion menu

-- 1. Update the deletion menu to have correct parent_id (Asset Management instead of Workflow)
UPDATE sys_menu
SET parent_id = 1,
    sort = 4,
    status = 1
WHERE id = 31 AND name = '素材删除';

-- 2. Add role_menu associations for the deletion menu
-- Grant access to admin and management roles (similar to material entry and usage permissions)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
VALUES
    (1, 31),  -- 系统管理员
    (4, 31),  -- 总消保管理岗
    (5, 31),  -- 分消保管理岗
    (7, 31),  -- 二分消保管理岗
    (8, 31);  -- 二分消保审批

-- 3. Verify the fix
SELECT
    m.id,
    m.parent_id,
    p.name as parent_name,
    m.name,
    m.type,
    m.path,
    m.sort,
    m.status,
    CASE WHEN COUNT(rm.role_id) > 0 THEN 'YES' ELSE 'NO' END as has_roles,
    GROUP_CONCAT(r.name) as assigned_roles
FROM sys_menu m
LEFT JOIN sys_menu p ON m.parent_id = p.id
LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id
LEFT JOIN sys_role r ON rm.role_id = r.id AND r.deleted = 0
WHERE m.id = 31
GROUP BY m.id, m.parent_id, p.name, m.name, m.type, m.path, m.sort, m.status;

-- 4. Show all Asset Management child menus for comparison
SELECT
    m.id,
    m.parent_id,
    m.name,
    m.type,
    m.path,
    m.sort,
    m.status,
    COUNT(rm.role_id) as role_count
FROM sys_menu m
LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id
WHERE m.parent_id = 1 AND m.deleted = 0
GROUP BY m.id, m.parent_id, m.name, m.type, m.path, m.sort, m.status
ORDER BY m.sort;
