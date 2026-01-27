-- =====================================================
-- 修复菜单中文乱码问题
-- =====================================================

USE xuanjiao_s;

-- 修复 id=29 菜单名称（审批工单）
UPDATE sys_menu SET name = '审批工单' WHERE id = 29;

-- 修复 id=31 菜单名称（审批流程）
UPDATE sys_menu SET name = '审批流程' WHERE id = 31;

-- 验证修复结果
SELECT id, parent_id, name, path, sort FROM sys_menu WHERE parent_id = 3 ORDER BY sort;
