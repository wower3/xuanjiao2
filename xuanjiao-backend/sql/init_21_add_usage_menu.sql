-- 素材使用菜单添加
USE xuanjiao_s;

-- 添加素材管理下的二级菜单
-- 素材列表 (id=9) 已存在，添加素材录入和素材使用

-- 先调整现有素材列表的 component 路径
UPDATE sys_menu SET component = 'asset/index' WHERE id = 9;

-- 添加素材录入菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status)
VALUES (13, 1, '素材录入', 'MENU', '/asset/material-entry', 'asset/material-entry', 'DocumentAdd', 2, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 添加素材使用菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status)
VALUES (14, 1, '素材使用', 'MENU', '/asset/usage-apply', 'asset/usage-apply', 'Download', 3, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 添加素材使用申请列表菜单（作为素材使用的子菜单，或者独立菜单）
-- 这里作为独立菜单放在素材管理下
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status)
VALUES (15, 1, '使用申请列表', 'MENU', '/asset/usage-list', 'asset/usage-list', 'List', 4, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 给系统管理员角色分配新菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id IN (13, 14, 15);

-- 更新素材列表的排序
UPDATE sys_menu SET sort = 1 WHERE id = 9;
