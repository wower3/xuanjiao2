-- =====================================================
-- 审批管理功能重构：菜单调整与草稿箱整合
-- =====================================================

USE xuanjiao_s;

-- 1. 删除"使用申请列表"菜单(id=15)
DELETE FROM sys_menu WHERE id = 15;
DELETE FROM sys_role_menu WHERE menu_id = 15;

-- 2. 在"我的任务"(id=3)下添加"我发起的"菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status)
VALUES (30, 3, '我发起的', 'MENU', '/task/my-initiated', 'task/my-initiated', 'Document', 3, 1)
ON DUPLICATE KEY UPDATE name = '我发起的';

-- 3. 给管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, 30;

-- 4. 更新"审批工单"(id=12)名称和排序
UPDATE sys_menu SET name = '待我审批', sort = 1 WHERE id = 12;

-- 5. 更新"草稿箱"(id=28)排序
UPDATE sys_menu SET sort = 2 WHERE id = 28;

-- 6. 删除重复的"审批工单"菜单(id=29，如果存在)
DELETE FROM sys_menu WHERE id = 29;
DELETE FROM sys_role_menu WHERE menu_id = 29;
