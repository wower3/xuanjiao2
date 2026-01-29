-- =====================================================
-- 修复版：部门表重构和菜单权限功能 SQL 脚本
-- 执行前请备份数据库！
-- =====================================================

USE xuanjiao_s;

-- ----------------------------
-- 1. 修改部门表结构
-- ----------------------------
-- 添加部门编号和层级字段（如果不存在）
SET @exist_code = (SELECT COUNT(*) FROM information_schema.columns
                  WHERE table_schema = 'xuanjiao_s' AND table_name = 'sys_dept' AND column_name = 'code');
SET @sql = IF(@exist_code = 0, 'ALTER TABLE sys_dept ADD COLUMN code VARCHAR(50) DEFAULT NULL COMMENT ''部门编号（6位数字字母混合）'' AFTER id', 'SELECT ''code already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_level = (SELECT COUNT(*) FROM information_schema.columns
                   WHERE table_schema = 'xuanjiao_s' AND table_name = 'sys_dept' AND column_name = 'level');
SET @sql = IF(@exist_level = 0, 'ALTER TABLE sys_dept ADD COLUMN level INT DEFAULT 1 COMMENT ''部门层级：1-一级，2-二级，3-三级'' AFTER code', 'SELECT ''level already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist_full_code = (SELECT COUNT(*) FROM information_schema.columns
                       WHERE table_schema = 'xuanjiao_s' AND table_name = 'sys_dept' AND column_name = 'full_code');
SET @sql = IF(@exist_full_code = 0, 'ALTER TABLE sys_dept ADD COLUMN full_code VARCHAR(200) DEFAULT NULL COMMENT ''完整部门编号（如：99H999-98H999-485D76）'' AFTER level', 'SELECT ''full_code already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 为现有数据生成部门编号（如果编号为空）
UPDATE sys_dept SET code = 'COMPAN', level = 1, full_code = 'COMPAN' WHERE id = 100 AND (code IS NULL OR code = '');

-- ----------------------------
-- 2. 创建菜单表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0表示根菜单',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    type VARCHAR(20) DEFAULT 'MENU' COMMENT '类型：MENU-菜单，BUTTON-按钮',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ----------------------------
-- 3. 创建菜单角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------
-- 4. 修改角色表，添加部门范围字段（如果不存在）
-- ----------------------------
SET @exist_dept_scope = (SELECT COUNT(*) FROM information_schema.columns
                        WHERE table_schema = 'xuanjiao_s' AND table_name = 'sys_role' AND column_name = 'dept_scope');
SET @sql = IF(@exist_dept_scope = 0, 'ALTER TABLE sys_role ADD COLUMN dept_scope VARCHAR(500) DEFAULT NULL COMMENT ''可管理部门范围（逗号分隔的部门ID列表）'' AFTER description', 'SELECT ''dept_scope already exists''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 5. 插入初始菜单数据
-- ----------------------------
-- 一级菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, icon, sort) VALUES
(1, 0, '素材管理', 'MENU', '/asset', 'Document', 1),
(2, 0, '流程管理', 'MENU', '/workflow', 'Operation', 2),
(3, 0, '审批管理', 'MENU', '/approval', 'Odometer', 3),
(4, 0, '系统管理', 'MENU', '/system', 'Setting', 4),
(5, 4, '用户管理', 'MENU', '/system/user', 'User', 1),
(6, 4, '角色管理', 'MENU', '/system/role', 'UserFilled', 2),
(7, 4, '部门管理', 'MENU', '/system/dept', 'OfficeBuilding', 3),
(8, 4, '菜单管理', 'MENU', '/system/menu', 'Menu', 4)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 二级菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, sort) VALUES
(9, 1, '素材列表', 'MENU', '/asset', 'asset/index', 1),
(10, 2, '流程列表', 'MENU', '/workflow', 'workflow/index', 1),
(11, 2, '流程设计', 'MENU', '/workflow/design', 'workflow/design', 2),
(12, 3, '待我审批', 'MENU', '/approval', 'approval/index', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 按钮权限
INSERT INTO sys_menu (id, parent_id, name, type, sort) VALUES
(101, 9, '上传素材', 'BUTTON', 1),
(102, 9, '删除素材', 'BUTTON', 2),
(103, 9, '申请使用', 'BUTTON', 3),
(104, 9, '下载素材', 'BUTTON', 4),
(201, 10, '新增流程', 'BUTTON', 1),
(202, 10, '编辑流程', 'BUTTON', 2),
(203, 10, '删除流程', 'BUTTON', 3),
(501, 5, '新增用户', 'BUTTON', 1),
(502, 5, '编辑用户', 'BUTTON', 2),
(503, 5, '删除用户', 'BUTTON', 4),
(601, 6, '新增角色', 'BUTTON', 1),
(602, 6, '编辑角色', 'BUTTON', 2),
(603, 6, '删除角色', 'BUTTON', 3),
(604, 6, '配置权限', 'BUTTON', 4)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ----------------------------
-- 6. 插入新角色类型（如果不存在）
-- ----------------------------
INSERT IGNORE INTO sys_role (name, description, role_type, status) VALUES
('系统管理员', '拥有系统所有权限', 'SYSTEM_ADMIN', 1),
('总消保管理岗', '可管理总部门及分部门的用户，可配置所有角色的权限', 'GENERAL_MGMT', 1),
('分消保管理岗', '可管理分部门的用户，可配置分消保用户的权限', 'BRANCH_MGMT', 1),
('总消保用户', '总消保部普通用户', 'GENERAL_USER', 1),
('分消保用户', '分消保部普通用户', 'BRANCH_USER', 1);

-- ----------------------------
-- 7. 系统管理员默认拥有所有菜单权限
-- ----------------------------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- ----------------------------
-- 8. 创建新的部门结构（如果不存在）
-- ----------------------------
INSERT IGNORE INTO sys_dept (id, name, parent_id, code, level, full_code, sort, status) VALUES
(100, '总公司', 0, 'COMPAN', 1, 'COMPAN', 1, 1);

INSERT IGNORE INTO sys_dept (id, name, parent_id, code, level, full_code, sort, status) VALUES
(101, '总部门', 100, 'GENDEP', 2, 'COMPAN-GENDEP', 1, 1),
(102, '分部门', 100, 'BRDEPA', 2, 'COMPAN-BRDEPA', 2, 1);

INSERT IGNORE INTO sys_dept (id, name, parent_id, code, level, full_code, sort, status) VALUES
(201, '总消保部', 101, 'GENCON', 3, 'COMPAN-GENDEP-GENCON', 1, 1),
(202, '分消保部', 102, 'BRCONB', 3, 'COMPAN-BRDEPA-BRCONB', 1, 1);
