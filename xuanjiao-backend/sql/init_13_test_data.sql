-- 测试数据：部门、角色、用户
SET NAMES utf8mb4;
USE xuanjiao_s;

-- 清理旧的测试数据（保留admin）
DELETE FROM sys_user_role WHERE user_id > 1;
DELETE FROM sys_user WHERE id > 1;

-- 插入部门
INSERT INTO sys_dept (id, name, parent_id, sort, status) VALUES
(1, '总经办', 0, 1, 1),
(2, '技术部', 0, 2, 1),
(3, '市场部', 0, 3, 1),
(4, '财务部', 0, 4, 1),
(5, '人事部', 0, 5, 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 插入角色
INSERT INTO sys_role (id, code, name, description, status) VALUES
(1, 'admin', '系统管理员', '拥有所有权限', 1),
(2, 'manager', '部门经理', '部门管理权限', 1),
(3, 'approver', '审批人', '审批权限', 1),
(4, 'user', '普通用户', '基础权限', 1)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 插入用户（密码都是123456的MD5）
-- 总经办
INSERT INTO sys_user (id, username, password, real_name, phone, dept_id, status) VALUES
(2, 'ceo', 'e10adc3949ba59abbe56e057f20f883e', '张总', '13800000001', 1, 1),
(3, 'vp', 'e10adc3949ba59abbe56e057f20f883e', '李副总', '13800000002', 1, 1);

-- 技术部
INSERT INTO sys_user (id, username, password, real_name, phone, dept_id, status) VALUES
(4, 'tech_mgr', 'e10adc3949ba59abbe56e057f20f883e', '王技术经理', '13800000003', 2, 1),
(5, 'dev1', 'e10adc3949ba59abbe56e057f20f883e', '赵开发', '13800000004', 2, 1),
(6, 'dev2', 'e10adc3949ba59abbe56e057f20f883e', '钱开发', '13800000005', 2, 1);

-- 市场部
INSERT INTO sys_user (id, username, password, real_name, phone, dept_id, status) VALUES
(7, 'mkt_mgr', 'e10adc3949ba59abbe56e057f20f883e', '孙市场经理', '13800000006', 3, 1),
(8, 'market1', 'e10adc3949ba59abbe56e057f20f883e', '周市场', '13800000007', 3, 1);

-- 财务部
INSERT INTO sys_user (id, username, password, real_name, phone, dept_id, status) VALUES
(9, 'fin_mgr', 'e10adc3949ba59abbe56e057f20f883e', '吴财务经理', '13800000008', 4, 1),
(10, 'finance1', 'e10adc3949ba59abbe56e057f20f883e', '郑财务', '13800000009', 4, 1);

-- 人事部
INSERT INTO sys_user (id, username, password, real_name, phone, dept_id, status) VALUES
(11, 'hr_mgr', 'e10adc3949ba59abbe56e057f20f883e', '冯人事经理', '13800000010', 5, 1),
(12, 'hr1', 'e10adc3949ba59abbe56e057f20f883e', '陈人事', '13800000011', 5, 1);

-- 用户角色关联
DELETE FROM sys_user_role;
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),   -- admin -> 系统管理员
(2, 2),   -- ceo -> 部门经理
(3, 2),   -- vp -> 部门经理
(4, 2),   -- tech_mgr -> 部门经理
(5, 4),   -- dev1 -> 普通用户
(6, 4),   -- dev2 -> 普通用户
(7, 2),   -- mkt_mgr -> 部门经理
(8, 4),   -- market1 -> 普通用户
(9, 2),   -- fin_mgr -> 部门经理
(9, 3),   -- fin_mgr -> 审批人
(10, 4),  -- finance1 -> 普通用户
(11, 2),  -- hr_mgr -> 部门经理
(12, 4);  -- hr1 -> 普通用户

-- 为管理员分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE id NOT IN (SELECT permission_id FROM sys_role_permission WHERE role_id = 1);
