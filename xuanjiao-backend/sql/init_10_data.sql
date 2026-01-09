-- 初始化数据

-- 初始化角色
INSERT INTO sys_role (code, name, description) VALUES
('ADMIN', '管理员', '系统管理员'),
('APPROVER', '审批员', '审批人员'),
('USER', '普通用户', '普通用户');

-- 初始化部门
INSERT INTO sys_dept (name, parent_id) VALUES
('总公司', 0),
('技术部', 1),
('市场部', 1);

-- 初始化管理员账号 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, dept_id, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 1, 1);

-- 关联管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
