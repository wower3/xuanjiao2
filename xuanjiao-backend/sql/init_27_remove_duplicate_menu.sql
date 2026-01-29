-- =====================================================
-- 删除重复的"审批流程"菜单（与"我发起的"功能重复）
-- =====================================================

USE xuanjiao_s;

-- 删除"审批流程"菜单(id=31)
DELETE FROM sys_menu WHERE id = 31;
DELETE FROM sys_role_menu WHERE menu_id = 31;

-- 验证删除结果
SELECT id, parent_id, name, path, sort FROM sys_menu WHERE parent_id = 3 ORDER BY sort;
