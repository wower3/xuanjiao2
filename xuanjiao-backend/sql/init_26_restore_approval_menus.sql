-- =====================================================
-- 恢复审批相关菜单
-- =====================================================

USE xuanjiao_s;

-- 1. 恢复"审批工单"菜单（查看所有审批工单，包括其他人发起的）
-- 在"我的任务"(id=3)下添加二级菜单"审批工单"
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status)
VALUES (29, 3, '审批工单', 'MENU', '/task/material-approval', 'task/material-approval', 'Document', 4, 1)
ON DUPLICATE KEY UPDATE
  name = '审批工单',
  parent_id = 3,
  path = '/task/material-approval',
  component = 'task/material-approval',
  sort = 4,
  status = 1;

-- 2. 确保"审批流程"菜单存在（流经事项中）
-- id=12 已被更新为"待我审批"，需要确认"审批流程"菜单
-- 检查是否有"审批流程"或"流经事项中"菜单
-- 如果 workflow-in-progress.vue 对应的菜单不存在，添加它
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status)
VALUES (31, 3, '审批流程', 'MENU', '/task/in-progress', 'task/workflow-in-progress', 'Clock', 5, 1);

-- 3. 给管理员角色分配菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, 29;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, 31;

-- 4. 验证菜单结构
SELECT id, parent_id, name, path, sort FROM sys_menu WHERE parent_id = 3 ORDER BY sort;
