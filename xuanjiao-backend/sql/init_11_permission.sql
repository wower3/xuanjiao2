-- 权限资源表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL COMMENT '权限编码',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    type VARCHAR(20) NOT NULL COMMENT '类型:MENU/BUTTON/API',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限资源表';

-- 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 初始化权限数据
INSERT INTO sys_permission (code, name, type, parent_id, path, icon, sort) VALUES
('asset', '素材管理', 'MENU', 0, '/asset', 'Picture', 1),
('asset:list', '素材列表', 'BUTTON', 1, NULL, NULL, 1),
('asset:upload', '上传素材', 'BUTTON', 1, NULL, NULL, 2),
('asset:delete', '删除素材', 'BUTTON', 1, NULL, NULL, 3),

('workflow', '流程管理', 'MENU', 0, '/workflow', 'Setting', 2),
('workflow:list', '流程列表', 'BUTTON', 5, NULL, NULL, 1),
('workflow:edit', '编辑流程', 'BUTTON', 5, NULL, NULL, 2),

('approval', '审批工单', 'MENU', 0, '/approval', 'Document', 3),
('approval:list', '审批列表', 'BUTTON', 8, NULL, NULL, 1),
('approval:approve', '审批操作', 'BUTTON', 8, NULL, NULL, 2),

('system', '系统管理', 'MENU', 0, '/system', 'Tools', 4),
('system:user', '用户管理', 'MENU', 11, '/system/user', NULL, 1),
('system:role', '角色管理', 'MENU', 11, '/system/role', NULL, 2),
('system:dept', '部门管理', 'MENU', 11, '/system/dept', NULL, 3),

('log', '使用日志', 'MENU', 0, '/log', 'List', 5);

-- 为管理员角色分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;
