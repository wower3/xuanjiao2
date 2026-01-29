-- 添加"审批工单"菜单
-- 在"我的任务"(id=3)下添加二级菜单"审批工单"

USE xuanjiao_s;

-- 添加二级菜单：审批工单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, sort, status) VALUES
(29, 3, '审批工单', 'MENU', '/task/material-approval', 'task/material-approval/index', 3, 1)
ON DUPLICATE KEY UPDATE name = '审批工单';

-- 验证菜单是否添加成功
SELECT id, parent_id, name, type, path, component, sort, status
FROM sys_menu
WHERE id = 29;
