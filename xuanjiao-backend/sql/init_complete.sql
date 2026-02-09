-- ============================================================================
-- 宣传教育平台 - 完整数据库初始化脚本
-- ============================================================================
-- 数据库: MySQL 8.0
-- 数据库名: xuanjiao_s
-- 执行方式: mysql -u root -p123456 < init_complete.sql
-- 或: source /path/to/init_complete.sql
-- 说明: 本脚本基于当前数据库结构生成，包含所有自定义字段
-- ============================================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS xuanjiao_s DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE xuanjiao_s;

-- ============================================================================
-- 第一部分: 系统管理模块表
-- ============================================================================

-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    leader_id BIGINT DEFAULT NULL COMMENT '负责人ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 角色表（包含自定义字段 role_type 和 dept_scope）
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_type VARCHAR(50) DEFAULT NULL COMMENT '角色类型：SYSTEM_ADMIN-系统管理员，GENERAL_MGMT-总消保管理岗，BRANCH_MGMT-分消保管理岗，LEAF_MGMT-二分消保管理，GENERAL_USER-总消保用户，BRANCH_USER-分消保用户，LEAF_USER-二分消保用户，CUSTOM-自定义',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    dept_scope VARCHAR(500) DEFAULT NULL COMMENT '管理部门范围（逗号分隔的部门ID列表）',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表（包含直接 role_id 字段）
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    dept_id BIGINT DEFAULT NULL COMMENT '部门ID',
    role_id BIGINT DEFAULT NULL COMMENT '角色ID（直接关联）',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_dept_id (dept_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    type VARCHAR(20) DEFAULT 'MENU' COMMENT '类型: CATALOG-目录, MENU-菜单, BUTTON-按钮',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================================================
-- 第二部分: 素材管理模块表
-- ============================================================================

-- 素材表
DROP TABLE IF EXISTS asset;
CREATE TABLE asset (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT '素材名称',
    type VARCHAR(20) NOT NULL COMMENT '类型: VIDEO-视频, IMAGE-图片, DOCUMENT-文档',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    thumbnail_path VARCHAR(500) DEFAULT NULL COMMENT '缩略图路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    md5 VARCHAR(32) NOT NULL COMMENT '文件MD5值(用于去重)',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PENDING-待审批, APPROVED-已通过, REJECTED-已驳回, DELETED-已删除',
    copyright VARCHAR(500) DEFAULT NULL COMMENT '版权声明',
    upload_user_id BIGINT NOT NULL COMMENT '上传用户ID',
    deletion_approve_time DATETIME DEFAULT NULL COMMENT '删除审批通过时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除（软删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_md5 (md5),
    KEY idx_status (status),
    KEY idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材表';

-- 标签表
DROP TABLE IF EXISTS asset_tag;
CREATE TABLE asset_tag (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    category VARCHAR(50) DEFAULT NULL COMMENT '标签分类',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 素材标签关联表（多对多）
DROP TABLE IF EXISTS asset_asset_tag;
CREATE TABLE asset_asset_tag (
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (asset_id, tag_id),
    KEY idx_asset_id (asset_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材标签关联表';

-- ============================================================================
-- 第三部分: 工作流定义模块表
-- ============================================================================

-- 审批流程定义表
DROP TABLE IF EXISTS workflow;
CREATE TABLE workflow (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '流程名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '流程描述',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    bound_role_id BIGINT DEFAULT NULL COMMENT '绑定的角色ID(可选)',
    workflow_type VARCHAR(50) DEFAULT NULL COMMENT '流程类型: ASSET_UPLOAD-素材录入, ASSET_USAGE-素材使用, ASSET_DELETION-素材删除',
    type VARCHAR(20) DEFAULT 'ASSET_UPLOAD' COMMENT '业务类型: ASSET_UPLOAD-素材录入, ASSET_USAGE-素材使用, ASSET_DELETION-素材删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_bound_role_id (bound_role_id),
    KEY idx_workflow_type (workflow_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程定义表';

-- 审批流程阶段表
DROP TABLE IF EXISTS workflow_stage;
CREATE TABLE workflow_stage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workflow_id BIGINT NOT NULL COMMENT '所属流程ID',
    name VARCHAR(100) NOT NULL COMMENT '阶段名称',
    stage_order INT NOT NULL COMMENT '阶段顺序',
    approve_type VARCHAR(20) DEFAULT 'OR' COMMENT '审批方式: OR-或签(任一通过), AND-会签(全部通过)',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_workflow_id (workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程阶段表';

-- 阶段审批人配置表
DROP TABLE IF EXISTS stage_approver;
CREATE TABLE stage_approver (
    id BIGINT NOT NULL AUTO_INCREMENT,
    stage_id BIGINT NOT NULL COMMENT '阶段ID',
    approver_type VARCHAR(20) NOT NULL COMMENT '审批人类型: USER-用户, ROLE-角色, DEPT-部门',
    approver_id BIGINT DEFAULT NULL COMMENT '审批人ID(根据类型对应用户/角色/部门ID)',
    sub_workflow_id BIGINT DEFAULT NULL COMMENT '关联的子流程ID(如果是子流程类型)',
    check_secondary_dept TINYINT DEFAULT 0 COMMENT '是否检查二级部门: 0-否, 1-是',
    PRIMARY KEY (id),
    KEY idx_stage_id (stage_id),
    KEY idx_sub_workflow_id (sub_workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阶段审批人配置表';

-- ============================================================================
-- 第四部分: 审批执行模块表
-- ============================================================================

-- 审批实例表
DROP TABLE IF EXISTS approval_instance;
CREATE TABLE approval_instance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workflow_id BIGINT NOT NULL COMMENT '使用的流程ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型: MATERIAL_ENTRY-素材录入, ASSET_USAGE-素材使用, ASSET_DELETION-素材删除',
    business_id BIGINT NOT NULL COMMENT '业务数据ID(申请单ID)',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    current_stage_id BIGINT DEFAULT NULL COMMENT '当前阶段ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-审批中, APPROVED-已通过, REJECTED-已驳回, CANCELLED-已取消, MAIN_COMPLETED-主流程完成(等待子流程)',
    parent_instance_id BIGINT DEFAULT NULL COMMENT '父实例ID(用于子流程关联)',
    parent_task_id BIGINT DEFAULT NULL COMMENT '父任务ID(用于子流程,记录是哪个任务触发的)',
    root_instance_id BIGINT DEFAULT NULL COMMENT '根实例ID(用于追回主流程)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    sub_workflow_approver_ids TEXT DEFAULT NULL COMMENT '子流程选中层审批人ID(JSON格式,key为子流程ID,value为审批人ID列表)',
    PRIMARY KEY (id),
    KEY idx_parent_instance (parent_instance_id),
    KEY idx_root_instance (root_instance_id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';

-- 审批任务表
DROP TABLE IF EXISTS approval_task;
CREATE TABLE approval_task (
    id BIGINT NOT NULL AUTO_INCREMENT,
    instance_id BIGINT NOT NULL COMMENT '所属实例ID',
    stage_id BIGINT NOT NULL COMMENT '阶段ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已通过, REJECTED-已驳回, CANCELLED-已取消, RETURNED-已退回',
    comment VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    is_first_approver TINYINT DEFAULT 1 COMMENT '是否首个审批人: 0-否, 1-是(OR签时首个标记,AND签时创建时标记)',
    task_type VARCHAR(50) DEFAULT 'NORMAL' COMMENT '任务类型: NORMAL-普通审批, RESTART_SUB_WORKFLOW-重新发起子流程',
    next_stage_approver_ids TEXT DEFAULT NULL COMMENT '下一阶段审批人ID列表(JSON数组)',
    sub_workflow_approver_ids TEXT DEFAULT NULL COMMENT '子流程审批人ID(JSON对象,key为sub_workflow_id)',
    selected_by_user_id BIGINT DEFAULT NULL COMMENT '选择人ID(用于上级审批人选择的子流程审批人)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_instance_id (instance_id),
    KEY idx_approver_id (approver_id),
    KEY idx_status (status),
    KEY idx_selected_by_user_id (selected_by_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';

-- 审批进度表
DROP TABLE IF EXISTS approval_progress;
CREATE TABLE approval_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    instance_id BIGINT NOT NULL COMMENT '实例ID',
    stage_id BIGINT NOT NULL COMMENT '阶段ID',
    stage_name VARCHAR(100) NOT NULL COMMENT '阶段名称',
    approver_id BIGINT DEFAULT NULL COMMENT '审批人ID',
    approver_name VARCHAR(100) DEFAULT NULL COMMENT '审批人名称',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已通过, REJECTED-已驳回, NOT_STARTED-未开始',
    comment VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    is_sub_workflow TINYINT DEFAULT 0 COMMENT '是否子流程: 0-否, 1-是',
    parent_task_id BIGINT DEFAULT NULL COMMENT '父任务ID(如果是子流程)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_instance_id (instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批进度表';

-- ============================================================================
-- 第五部分: 业务申请模块表
-- ============================================================================

-- 素材录入申请表
DROP TABLE IF EXISTS material_application;
CREATE TABLE material_application (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '申请标题',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PENDING-审批中, APPROVED-已通过, REJECTED-已驳回, CANCELLED-已取消',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    instance_id BIGINT DEFAULT NULL COMMENT '审批实例ID',
    workflow_id BIGINT DEFAULT NULL COMMENT '使用的流程ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材录入申请表';

-- 素材录入申请-素材关联表
DROP TABLE IF EXISTS material_application_asset;
CREATE TABLE material_application_asset (
    id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL COMMENT '申请ID',
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    description VARCHAR(500) DEFAULT NULL COMMENT '素材描述',
    publish_channel VARCHAR(200) DEFAULT NULL COMMENT '发布渠道',
    is_secondary_creation TINYINT DEFAULT 0 COMMENT '是否二次创作: 0-否, 1-是',
    attachment_path VARCHAR(500) DEFAULT NULL COMMENT '版权证明附件路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_application_id (application_id),
    KEY idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材录入申请-素材关联表';

-- 素材使用申请表
DROP TABLE IF EXISTS usage_apply;
CREATE TABLE usage_apply (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '申请标题',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PENDING-审批中, APPROVED-已通过, REJECTED-已驳回, CANCELLED-已取消',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    instance_id BIGINT DEFAULT NULL COMMENT '审批实例ID',
    workflow_id BIGINT DEFAULT NULL COMMENT '使用的流程ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材使用申请表';

-- 素材使用申请-素材关联表(多对多)
DROP TABLE IF EXISTS usage_apply_asset;
CREATE TABLE usage_apply_asset (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usage_apply_id BIGINT NOT NULL COMMENT '使用申请ID',
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    usage_description VARCHAR(500) DEFAULT NULL COMMENT '该素材的使用说明',
    usage_publish_channel VARCHAR(200) DEFAULT NULL COMMENT '该素材的发布渠道',
    usage_is_secondary_creation TINYINT DEFAULT 0 COMMENT '该素材是否二次创作: 0-否, 1-是',
    usage_attachment_path VARCHAR(500) DEFAULT NULL COMMENT '该素材的附件路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_apply_asset (usage_apply_id, asset_id),
    KEY idx_asset_id (asset_id),
    KEY idx_usage_apply_id (usage_apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材使用申请-素材关联表(多对多)';

-- 素材删除申请表
DROP TABLE IF EXISTS asset_deletion_application;
CREATE TABLE asset_deletion_application (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '申请标题',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PENDING-审批中, APPROVED-已通过, REJECTED-已驳回, CANCELLED-已取消',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    instance_id BIGINT DEFAULT NULL COMMENT '审批实例ID',
    workflow_id BIGINT DEFAULT NULL COMMENT '使用的流程ID',
    deletion_reason VARCHAR(500) DEFAULT NULL COMMENT '删除原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除申请表';

-- 素材删除申请-素材关联表
DROP TABLE IF EXISTS asset_deletion_asset;
CREATE TABLE asset_deletion_asset (
    id BIGINT NOT NULL AUTO_INCREMENT,
    application_id BIGINT NOT NULL COMMENT '申请ID',
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_application_id (application_id),
    KEY idx_asset_id (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除申请-素材关联表';

-- ============================================================================
-- 第六部分: 通知与日志模块表
-- ============================================================================

-- 系统通知表
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    notification_type VARCHAR(50) NOT NULL COMMENT '通知类型: WORKFLOW_FLOW-流程流转, MENTION-知会, SYSTEM-系统通知',
    source_type VARCHAR(50) COMMENT '来源类型: MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION',
    source_id BIGINT COMMENT '来源ID(审批实例ID)',
    sender_id BIGINT COMMENT '发送人ID',
    sender_name VARCHAR(100) COMMENT '发送人名称',
    recipient_id BIGINT NOT NULL COMMENT '接收人ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    read_time DATETIME COMMENT '阅读时间',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-有效, 0-无效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_recipient_read (recipient_id, is_read),
    KEY idx_source (source_type, source_id),
    KEY idx_sender (sender_id),
    KEY idx_create_time (create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 使用日志表
DROP TABLE IF EXISTS usage_log;
CREATE TABLE usage_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    asset_id BIGINT NOT NULL COMMENT '素材ID',
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型: DOWNLOAD-下载, PREVIEW-预览, USAGE_APPLY-使用申请',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    usage_description VARCHAR(500) DEFAULT NULL COMMENT '使用说明',
    usage_publish_channel VARCHAR(200) DEFAULT NULL COMMENT '发布渠道',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_asset_id (asset_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用日志表';

-- 操作日志表
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '操作用户ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    method VARCHAR(200) NOT NULL COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================================
-- 第七部分: 初始化基础数据
-- ============================================================================

-- 初始化角色（包含 role_type）
INSERT INTO sys_role (id, name, role_type, description) VALUES
(1, '系统管理员', 'SYSTEM_ADMIN', '系统管理员'),
(4, '总消保管理岗', 'GENERAL_MGMT', '可管理总部门及分部门的用户，可配置所有角色的权限'),
(5, '分消保管理岗', 'BRANCH_MGMT', '可管理分部门的用户，可配置分消保用户的权限'),
(8, '二分消保管理', 'LEAF_MGMT', '二分部门消保管理'),
(9, '二分消保审批', 'LEAF_APPROV', '二分部门消保审批'),
(10, '分消保审批', 'BRANCH_APPROV', '分消保部门审批人员'),
(11, '总消保审批', 'GENERAL_APPROV', '总消保部门审批人员'),
(12, '分消保科负责人', 'BRANCH_SECTION', '分消保部门科负责人'),
(13, '分消保部负责人', 'BRANCH_SECRETARY', '分消保部门负责人'),
(14, '普通用户', 'COMMON_USER', '普通用户角色，无特殊管理权限');

-- 初始化部门
INSERT INTO sys_dept (id, name, parent_id) VALUES
(0, '默认根部门', 0),
(100, '总经办', 0),
(101, '总部门', 100),
(102, '分部门1', 100),
(201, '总消保部', 101),
(202, '分消保部1', 102),
(203, '技术一部', 101),
(204, '分部门2', 100),
(205, '分消保部2', 204),
(206, '二分部门1', 102),
(207, '二分部门2', 204);

-- 初始化管理员账号 (密码: 123456, MD5: e10adc3949ba59abbe56e057f20f883e)
INSERT INTO sys_user (id, username, password, real_name, dept_id, role_id, status) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 100, 1, 1);

-- ============================================================================
-- 第八部分: 初始化菜单
-- ============================================================================

-- 一级菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort) VALUES
(1, 0, '素材管理', 'MENU', '/asset', NULL, 'Picture', 1),
(2, 0, '流程管理', 'MENU', '/workflow', NULL, 'SetUp', 2),
(3, 0, '我的任务', 'MENU', '/approval', NULL, 'Task', 3),
(4, 0, '系统管理', 'MENU', '/system', NULL, 'Setting', 4);

-- 二级菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort) VALUES
(5, 4, '用户管理', 'MENU', '/system/user', 'system/user/index', 'User', 1),
(6, 4, '角色管理', 'MENU', '/system/role', 'system/role/index', 'UserFilled', 2),
(7, 4, '部门管理', 'MENU', '/system/dept', 'system/dept/index', 'OfficeBuilding', 3),
(8, 4, '菜单管理', 'MENU', '/system/menu', 'system/menu/index', 'Menu', 4),
(9, 1, '素材列表', 'MENU', '/asset', 'asset/index', 'Picture', 1),
(10, 2, '流程列表', 'MENU', '/workflow', 'workflow/index', 'SetUp', 1),
(11, 2, '流程设计', 'MENU', '/workflow/design', 'workflow/design', 'Edit', 2),
(12, 3, '待办事项', 'MENU', '/task/pending-approval', 'task/pending-approval', 'Clock', 1),
(14, 1, '素材使用', 'MENU', '/asset/usage-apply', 'asset/usage-apply', 'Share', 2),
(27, 1, '素材录入', 'MENU', '/asset/material-entry', 'asset/material-entry/index', 'Upload', 3),
(28, 3, '草稿箱', 'MENU', '/task/draft-box', 'task/draft-box', 'Document', 2),
(29, 3, '审批工单', 'MENU', '/task/material-approval', 'task/material-approval', 'Document', 3),
(30, 3, '我发起的', 'MENU', '/task/my-initiated', 'task/my-initiated', 'User', 4),
(31, 1, '素材删除', 'MENU', '/asset/deletion', 'asset/deletion/index', 'Delete', 4),
(32, 3, '流经事项', 'MENU', '/task/flow-items', 'task/flow-items', 'Share', 5),
(33, 3, '知会事项', 'MENU', '/task/notifications', 'task/notifications', 'Bell', 6);

-- 按钮权限
INSERT INTO sys_menu (id, parent_id, name, type, path, component, sort) VALUES
(34, 3, '知会', 'BUTTON', NULL, NULL, 0),
(101, 9, '上传素材', 'BUTTON', NULL, NULL, 1),
(102, 9, '删除素材', 'BUTTON', NULL, NULL, 2),
(103, 9, '申请使用', 'BUTTON', NULL, NULL, 3),
(104, 9, '申请删除', 'BUTTON', NULL, NULL, 4),
(201, 10, '创建流程', 'BUTTON', NULL, NULL, 1),
(202, 10, '编辑流程', 'BUTTON', NULL, NULL, 2),
(203, 10, '删除流程', 'BUTTON', NULL, NULL, 3),
(501, 5, '新增用户', 'BUTTON', NULL, NULL, 1),
(502, 5, '编辑用户', 'BUTTON', NULL, NULL, 2),
(503, 5, '删除用户', 'BUTTON', NULL, NULL, 3),
(601, 6, '新增角色', 'BUTTON', NULL, NULL, 1),
(602, 6, '编辑角色', 'BUTTON', NULL, NULL, 2),
(603, 6, '删除角色', 'BUTTON', NULL, NULL, 3),
(604, 6, '分配权限', 'BUTTON', NULL, NULL, 4);

-- 为管理员角色分配所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- ============================================================================
-- 第九部分: 初始化审批流程模板
-- ============================================================================

-- 模板1: 单级审批
INSERT INTO workflow (name, description, version, status, type) VALUES
('单级审批模板', '仅需部门主管审批即可通过', 1, 1, 'ASSET_UPLOAD');
SET @wf1 = LAST_INSERT_ID();
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type) VALUES
(@wf1, '部门主管审批', 1, 'OR');

-- 模板2: 两级审批
INSERT INTO workflow (name, description, version, status, type) VALUES
('两级审批模板', '需部门主管和总监依次审批', 1, 1, 'ASSET_UPLOAD');
SET @wf2 = LAST_INSERT_ID();
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type) VALUES
(@wf2, '部门主管审批', 1, 'OR'),
(@wf2, '总监审批', 2, 'OR');

-- 模板3: 会签审批
INSERT INTO workflow (name, description, version, status, type) VALUES
('会签审批模板', '需要所有审批人全部通过', 1, 1, 'ASSET_UPLOAD');
SET @wf3 = LAST_INSERT_ID();
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type) VALUES
(@wf3, '会签审批', 1, 'AND');

-- ============================================================================
-- 验证脚本执行结果
-- ============================================================================
SELECT '数据库初始化完成!' AS Result;
SELECT COUNT(*) AS 部门数量 FROM sys_dept;
SELECT COUNT(*) AS 角色数量 FROM sys_role;
SELECT COUNT(*) AS 用户数量 FROM sys_user;
SELECT COUNT(*) AS 菜单数量 FROM sys_menu;
SELECT COUNT(*) AS 流程模板数量 FROM workflow;
