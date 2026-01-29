-- 素材删除申请表
CREATE TABLE IF NOT EXISTS asset_deletion_application (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '申请标题',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    dept_id BIGINT COMMENT '部门ID',
    workflow_id BIGINT COMMENT '工作流ID',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PENDING/APPROVED/REJECTED',
    delete_reason TEXT NOT NULL COMMENT '删除理由',
    attachment_path VARCHAR(500) COMMENT '附件路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    INDEX idx_applicant (applicant_id),
    INDEX idx_status (status),
    INDEX idx_workflow (workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除申请表';

-- 素材删除申请-素材关联表
CREATE TABLE IF NOT EXISTS asset_deletion_asset (
    id BIGINT NOT NULL AUTO_INCREMENT,
    deletion_application_id BIGINT NOT NULL COMMENT '删除申请ID',
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    INDEX idx_deletion_application (deletion_application_id),
    INDEX idx_asset (asset_id),
    UNIQUE KEY uk_asset_application (deletion_application_id, asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除申请-素材关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) NOT NULL COMMENT '操作人姓名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    target_name VARCHAR(200) COMMENT '目标名称',
    operation_detail TEXT COMMENT '操作详情',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_operator (operator_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_operation_type (operation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 添加菜单
-- Note: 使用 sys_menu 表，parent_id=1 是素材管理菜单
INSERT INTO sys_menu (parent_id, name, type, path, icon, sort, status, create_time, update_time, deleted)
VALUES (
    1,
    '素材删除',
    'MENU',
    '/asset/deletion',
    'Delete',
    4,
    1,
    NOW(),
    NOW(),
    0
) ON DUPLICATE KEY UPDATE
    parent_id=1,
    name='素材删除',
    type='MENU',
    path='/asset/deletion',
    icon='Delete',
    sort=4;

-- 为菜单添加角色权限关联（系统管理员、总消保管理岗、分消保管理岗等）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM sys_role r, sys_menu m
WHERE r.deleted = 0
  AND m.id = (SELECT id FROM sys_menu WHERE name = '素材删除' LIMIT 1)
  AND r.id IN (1, 4, 5, 7, 8);
