SET NAMES utf8mb4;

-- 添加"素材列表"菜单（parent_id=1 是"素材管理"）
INSERT INTO sys_menu (name, parent_id, path, sort, create_time, deleted)
VALUES ('素材列表', 1, 'material-list', 2, NOW(), 0);

-- 获取刚插入的素材列表菜单ID
SET @material_list_id = LAST_INSERT_ID();

-- 添加"流经事项中"菜单（parent_id=3 是"我的任务"）
INSERT INTO sys_menu (name, parent_id, path, sort, create_time, deleted)
VALUES ('流经事项中', 3, 'in-progress', 2, NOW(), 0);

-- 获取刚插入的流经事项中菜单ID
SET @in_progress_id = LAST_INSERT_ID();

-- 为角色添加权限（SYSTEM_ADMIN role_id=1, GENERAL_MGMT role_id=4）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, @material_list_id),
(4, @material_list_id),
(1, @in_progress_id),
(4, @in_progress_id);

-- 查询确认
SELECT id, parent_id, name, path FROM sys_menu WHERE parent_id IN (1, 3) ORDER BY parent_id, sort;
