-- ============================================================================
-- 宣传教育平台 - 完整数据库初始化脚本（修复版）
-- ============================================================================
-- 数据库: MySQL 8.0
-- 数据库名: xuanjiao_s
-- 执行方式: mysql -u root -p123456 < init_complete_fixed.sql
-- 或: source /path/to/init_complete_fixed.sql
-- 说明: 基于 Mapper XML 文件修复的完整数据库初始化脚本
-- 修复日期: 2025-02-09
-- ============================================================================
-- 修复内容:
-- 1. approval_progress 表 - 修复为阶段级记录结构
--    - 移除: approver_id, approver_name, comment
--    - 添加: stage_order, approvers (JSON), parent_instance_id, update_time
-- 2. operation_log 表 - 字段重命名
--    - 移除: user_id, username, operation, method, params
--    - 添加: operator_id, operator_name, operation_type, target_type, target_id,
--           target_name, operation_detail, ip_address
-- 3. usage_log 表 - 添加 dept_name 字段
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
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) DEFAULT NULL COMMENT '部门编码（数字字符串）',
    `level` INT DEFAULT 1 COMMENT '部门层级：1-一级，2-二级，3-三级',
    full_code VARCHAR(200) DEFAULT NULL COMMENT '完整部门编码（如：1,101,102）',
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    leader_id BIGINT DEFAULT NULL COMMENT '负责人ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 角色表（包含自定义字段 role_type 和 dept_scope）
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_type VARCHAR(50) DEFAULT NULL COMMENT '角色类型：SYSTEM-系统管理员，GENERAL_MGMT-总消保管理岗，BRANCH_MGMT-分消保管理岗，GENERAL_USER-总消保用户，BRANCH_USER-分消保用户，CUSTOM-自定义',
    description VARCHAR(200) DEFAULT NULL,
    dept_scope VARCHAR(500) DEFAULT NULL COMMENT '管理部门范围（逗号分隔的部门ID列表）',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户表（包含直接 role_id 字段）
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(100) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    dept_id BIGINT DEFAULT NULL COMMENT '部门ID',
    role_id BIGINT DEFAULT NULL COMMENT '角色ID',
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID（表示顶级菜单）',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    type VARCHAR(20) DEFAULT 'MENU' COMMENT '类型：MENU-菜单，BUTTON-按钮',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1-可用，0-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    role_id BIGINT(20) NOT NULL COMMENT '角色ID',
    menu_id BIGINT(20) NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 系统通知表
DROP TABLE IF EXISTS sys_notification;
CREATE TABLE sys_notification (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    notification_type VARCHAR(50) NOT NULL COMMENT '通知类型: WORKFLOW_FLOW-流程流转, MENTION-知会, SYSTEM-系统通知',
    source_type VARCHAR(50) COMMENT '来源类型: MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION',
    source_id BIGINT COMMENT '来源ID(审批实例ID)',
    sender_id BIGINT COMMENT '发送人ID',
    sender_name VARCHAR(100) COMMENT '发送人名称',
    recipient_id BIGINT(20) NOT NULL COMMENT '接收人ID',
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

-- 操作日志表（修复版）
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    operator_id BIGINT(20) NOT NULL COMMENT '操作用户ID',
    operator_name VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(50) DEFAULT NULL COMMENT '目标类型',
    target_id BIGINT(20) DEFAULT NULL COMMENT '目标ID',
    target_name VARCHAR(200) DEFAULT NULL COMMENT '目标名称',
    operation_detail TEXT DEFAULT NULL COMMENT '操作详情',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_operator_id (operator_id),
    KEY idx_target (target_type, target_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================================================
-- 第二部分: 素材管理模块表
-- ============================================================================

-- 素材表（完整字段）
DROP TABLE IF EXISTS asset;
CREATE TABLE asset (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT '素材名称',
    type VARCHAR(20) NOT NULL COMMENT '类型:VIDEO-视频, IMAGE-图片, DOCUMENT-文档',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    thumbnail_path VARCHAR(500) DEFAULT NULL,
    file_size BIGINT(20) DEFAULT '0' COMMENT '文件大小(字节)',
    md5 VARCHAR(32) NOT NULL COMMENT '文件MD5值(用于去重)',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态:DRAFT-草稿, PENDING-待审批, APPROVED-已通过, REJECTED-已驳回, DELETED-已删除',
    copyright VARCHAR(500) DEFAULT NULL,
    upload_user_id BIGINT(20) NOT NULL COMMENT '上传用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT '0' COMMENT '删除标记:0-未删除,1-已删除(软删除)',
    application_id BIGINT(20) DEFAULT NULL COMMENT '关联申请单ID',
    copyright_file_path VARCHAR(500) DEFAULT NULL COMMENT '版权附件路径',
    copyright_text TEXT COMMENT '版权声明文本',
    description TEXT COMMENT '申请说明',
    publish_channel VARCHAR(200) DEFAULT NULL COMMENT '发布渠道',
    deletion_application_id BIGINT(20) DEFAULT NULL COMMENT '删除申请ID',
    deletion_approve_time DATETIME DEFAULT NULL COMMENT '删除审批通过时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材表';

-- 标签表
DROP TABLE IF EXISTS tag;
CREATE TABLE tag (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类（如：图片、视频、文档）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT '0',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='素材标签表';

-- 素材标签关联表（多对多）
DROP TABLE IF EXISTS asset_tag;
CREATE TABLE asset_tag (
    asset_id BIGINT(20) NOT NULL COMMENT '素材ID',
    tag_id BIGINT(20) NOT NULL COMMENT '标签ID',
    PRIMARY KEY (asset_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='素材标签关联表';

-- 使用日志表（修复版 - 添加 dept_name 字段）
DROP TABLE IF EXISTS usage_log;
CREATE TABLE usage_log (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    asset_id BIGINT(20) NOT NULL COMMENT '素材ID',
    user_id BIGINT(20) NOT NULL COMMENT '操作用户ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型:DOWNLOAD-下载, PREVIEW-预览, USAGE_APPLY-使用申请',
    ip VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    dept_name VARCHAR(100) DEFAULT NULL COMMENT '部门名称',
    usage_description VARCHAR(500) DEFAULT NULL COMMENT '使用说明',
    usage_publish_channel VARCHAR(200) DEFAULT NULL COMMENT '发布渠道',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_asset_id (asset_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用日志表';

-- ============================================================================
-- 第三部分: 工作流定义模块表
-- ============================================================================

-- 审批流程定义表
DROP TABLE IF EXISTS workflow;
CREATE TABLE workflow (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '流程名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '流程描述',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态:1-启用, 0-禁用',
    bound_role_id BIGINT DEFAULT NULL COMMENT '绑定的角色ID(可选)',
    workflow_type VARCHAR(50) DEFAULT NULL COMMENT '流程类型:ASSET_UPLOAD-素材录入, ASSET_USAGE-素材使用, ASSET_DELETION-素材删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT '0' COMMENT '删除标记:0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_bound_role_id (bound_role_id),
    KEY idx_workflow_type (workflow_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程定义表';

-- 审批流程阶段表
DROP TABLE IF EXISTS workflow_stage;
CREATE TABLE workflow_stage (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    workflow_id BIGINT(20) NOT NULL COMMENT '所属流程ID',
    name VARCHAR(100) NOT NULL COMMENT '阶段名称',
    stage_order INT NOT NULL COMMENT '阶段顺序',
    approve_type VARCHAR(20) DEFAULT 'OR' COMMENT '审批方式:OR-或签(任一通过), AND-会签(全部通过)',
    deleted TINYINT DEFAULT '0' COMMENT '删除标记:0-未删除, 1-已删除',
    PRIMARY KEY (id),
    KEY idx_workflow_id (workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程阶段表';

-- 阶段审批人配置表
DROP TABLE IF EXISTS stage_approver;
CREATE TABLE stage_approver (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    stage_id BIGINT(20) NOT NULL COMMENT '阶段ID',
    approver_type VARCHAR(20) NOT NULL COMMENT '审批人类型:USER-用户, ROLE-角色, DEPT-部门',
    approver_id BIGINT DEFAULT NULL COMMENT '审批人ID(根据类型对应用户/角色/部门ID)',
    sub_workflow_id BIGINT DEFAULT NULL COMMENT '关联的子流程ID(如果是子流程类型)',
    check_secondary_dept TINYINT DEFAULT '0' COMMENT '是否检查二级部门:0-否, 1-是',
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
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    workflow_id BIGINT(20) NOT NULL COMMENT '使用的流程ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型:MATERIAL_ENTRY-素材录入, ASSET_USAGE-素材使用, ASSET_DELETION-素材删除',
    business_id BIGINT(20) NOT NULL COMMENT '业务数据ID(申请单ID)',
    applicant_id BIGINT(20) NOT NULL COMMENT '申请人ID',
    current_stage_id BIGINT DEFAULT NULL COMMENT '当前阶段ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态:PENDING-审批中, APPROVED-已通过, REJECTED-已驳回, CANCELLED-已取消, MAIN_COMPLETED-主流程完成(等待子流程)',
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
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    instance_id BIGINT(20) NOT NULL COMMENT '所属实例ID',
    stage_id BIGINT(20) NOT NULL COMMENT '阶段ID',
    approver_id BIGINT(20) NOT NULL COMMENT '审批人ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态:PENDING-待审批, APPROVED-已通过, REJECTED-已驳回, CANCELLED-已取消, RETURNED-已退回',
    comment VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    is_first_approver TINYINT DEFAULT '1' COMMENT '是否首个审批人:0-否, 1-是(OR签时首个标记,AND签时创建时标记)',
    task_type VARCHAR(50) DEFAULT 'NORMAL' COMMENT '任务类型:NORMAL-普通审批, RESTART_SUB_WORKFLOW-重新发起子流程',
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

-- 审批进度表（修复版 - 阶段级记录结构）
DROP TABLE IF EXISTS approval_progress;
CREATE TABLE approval_progress (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    instance_id BIGINT(20) NOT NULL COMMENT '实例ID',
    stage_id BIGINT(20) NOT NULL COMMENT '阶段ID',
    stage_name VARCHAR(100) NOT NULL COMMENT '阶段名称',
    stage_order INT DEFAULT NULL COMMENT '阶段顺序',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态:PENDING-待审批, APPROVED-已通过, REJECTED-已驳回, NOT_STARTED-未开始',
    approvers TEXT DEFAULT NULL COMMENT '审批人列表，JSON格式：[{id, name, status, comment, approveTime}]',
    is_sub_workflow TINYINT DEFAULT '0' COMMENT '是否子流程:0-否, 1-是',
    parent_instance_id BIGINT DEFAULT NULL COMMENT '父实例ID（用于子流程）',
    parent_task_id BIGINT DEFAULT NULL COMMENT '父任务ID',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_instance_id (instance_id),
    KEY idx_parent_instance (parent_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批进度表（阶段级记录）';

-- ============================================================================
-- 第五部分: 业务申请模块表
-- ============================================================================

-- 素材录入申请表
DROP TABLE IF EXISTS material_application;
CREATE TABLE material_application (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '事项标题',
    applicant_id BIGINT(20) NOT NULL COMMENT '申请人ID',
    maintainer_id BIGINT(20) NOT NULL COMMENT '维护人ID',
    dept_id BIGINT(20) NOT NULL COMMENT '归属部门ID',
    workflow_id BIGINT DEFAULT NULL COMMENT '审批流程ID（提交时选择）',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PENDING/APPROVED/REJECTED',
    guarantee_declaration TINYINT DEFAULT '0' COMMENT '保证声明(0未勾选，1已勾选）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT '0',
    PRIMARY KEY (id),
    KEY idx_applicant (applicant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='素材录入申请单主表';

-- 素材使用申请表
DROP TABLE IF EXISTS usage_apply;
CREATE TABLE usage_apply (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    user_id BIGINT(20) NOT NULL COMMENT '申请用户ID',
    title VARCHAR(200) DEFAULT NULL COMMENT '申请单标题',
    purpose VARCHAR(500) DEFAULT NULL COMMENT '使用用途',
    scope VARCHAR(200) DEFAULT NULL COMMENT '使用范围',
    workflow_id BIGINT DEFAULT NULL COMMENT '审批流程ID（为空则无需审批）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '申请状态：PENDING/APPROVED/REJECTED/CANCELLED',
    attachment_path VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
    approval_instance_id BIGINT DEFAULT NULL COMMENT '关联审批实例ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT '0',
    is_secondary_creation TINYINT DEFAULT '0' COMMENT '是否二次创作:0-否,1-是',
    publish_channel VARCHAR(200) DEFAULT NULL COMMENT '发布渠道',
    dept_id BIGINT DEFAULT NULL COMMENT '申请部门ID',
    draft TINYINT DEFAULT '1' COMMENT '是否草稿:0-已提交,1-草稿',
    PRIMARY KEY (id),
    KEY idx_asset_user (user_id),
    KEY idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材使用申请表';

-- 素材使用申请-素材关联表(多对多)
DROP TABLE IF EXISTS usage_apply_asset;
CREATE TABLE usage_apply_asset (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    usage_apply_id BIGINT(20) NOT NULL COMMENT '使用申请ID',
    asset_id BIGINT(20) NOT NULL COMMENT '素材ID',
    usage_description VARCHAR(500) DEFAULT NULL COMMENT '该素材的使用说明',
    usage_publish_channel VARCHAR(200) DEFAULT NULL COMMENT '该素材的发布渠道',
    usage_is_secondary_creation TINYINT DEFAULT '0' COMMENT '该素材是否二次创作:0-否,1-是',
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
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '删除申请ID',
    title VARCHAR(200) NOT NULL COMMENT '删除申请标题',
    applicant_id BIGINT(20) NOT NULL COMMENT '申请人ID',
    dept_id BIGINT DEFAULT NULL COMMENT '部门ID',
    workflow_id BIGINT DEFAULT NULL COMMENT '审批流程ID',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PENDING/APPROVED/REJECTED',
    delete_reason TEXT NOT NULL COMMENT '删除理由（必填）',
    attachment_path VARCHAR(500) DEFAULT NULL COMMENT '附件路径',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT '0' COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    KEY idx_applicant (applicant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除申请表';

-- 素材删除申请-素材关联表
DROP TABLE IF EXISTS asset_deletion_asset;
CREATE TABLE asset_deletion_asset (
    id BIGINT(20) NOT NULL AUTO_INCREMENT,
    deletion_application_id BIGINT(20) NOT NULL COMMENT '删除申请ID',
    asset_id BIGINT(20) NOT NULL COMMENT '素材ID',
    asset_name VARCHAR(200) DEFAULT NULL COMMENT '素材名称（快照）',
    asset_type VARCHAR(20) DEFAULT NULL COMMENT '素材类型（快照）',
    delete_time DATETIME DEFAULT NULL COMMENT '删除时间（审批通过后设置）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_deletion_app (deletion_application_id),
    KEY idx_asset (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材删除关联表';

-- ============================================================================
-- 第六部分: 初始化基础数据
-- ============================================================================

-- 初始化角色（包含 role_type）
INSERT INTO sys_role (id, name, role_type, description) VALUES
(1, '系统管理员', 'SYSTEM_ADMIN', '系统管理员'),
(2, '部门经理', 'CUSTOM', '审批人员'),
(3, '普通用户', 'CUSTOM', '普通用户'),
(4, '总消保管理岗', 'GENERAL_MGMT', '可管理总部门及分部门的用户，可配置所有角色的权限'),
(5, '分消保管理岗', 'BRANCH_MGMT', '可管理分部门的用户，可配置分消保用户的权限'),
(6, '总消保用户', 'GENERAL_USER', '总消保部普通用户'),
(7, '分消保用户', 'BRANCH_USER', '分消保部普通用户'),
(8, '二分消保管理', 'LEAF_MGMT', '二分部门消保管理'),
(9, '二分消保审批', 'LEAF_APPROV', '二分部门消保审批'),
(10, '分消保审批', 'BRANCH_APPROV', '分消保部门审批人员'),
(11, '总消保审批', 'GENERAL_APPROV', '总消保部门审批人员'),
(12, '分消保科负责人', 'BRANCH_SECTION', '分消保部门科负责人'),
(13, '分消保部负责人', 'BRANCH_SECRETARY', '分消保部门负责人'),
(14, '普通用户', 'COMMON_USER', '普通用户角色，无特殊管理权限');

-- 初始化部门（包含 code, level, full_code 字段）
INSERT INTO sys_dept (id, code, `level`, full_code, name, parent_id, leader_id, sort, status) VALUES
(100, 'COMPAN', 1, 'COMPAN', '总公司', 0, NULL, 1, 1),
(101, 'GENDEP', 2, 'COMPAN,GENDEP', '总部门', 100, NULL, 1, 1),
(102, 'BRDEPA', 2, 'COMPAN,BRDEPA', '分部门1', 100, NULL, 2, 1),
(1, 'DEP001', 3, 'COMPAN,GENDEP,DEP001', '总经办', 101, NULL, 2, 1),
(2, 'DEP002', 3, 'COMPAN,GENDEP,DEP002', '技术部', 101, NULL, 3, 1),
(3, 'DEP003', 3, 'COMPAN,GENDEP,DEP003', '市场部', 101, NULL, 4, 1),
(4, 'DEP004', 3, 'COMPAN,GENDEP,DEP004', '财务部', 101, NULL, 5, 1),
(5, 'DEP005', 3, 'COMPAN,GENDEP,DEP005', '人事部', 101, NULL, 6, 1),
(201, 'GENCON', 3, 'COMPAN,GENDEP,GENCON', '总消保部', 101, NULL, 1, 1),
(202, 'BRCONB', 3, 'COMPAN,BRDEPA,BRCONB', '分消保部1', 102, NULL, 1, 1),
(203, 'FKFNU6', 3, 'COMPAN,GENDEP,2,FKFNU6', '技术一部', 2, NULL, 0, 1),
(204, '9SQR7L', 2, 'COMPAN,9SQR7L', '分部门2', 100, NULL, 0, 1),
(205, '22C3LZ', 3, 'COMPAN,9SQR7L,22C3LZ', '分消保部2', 204, NULL, 0, 1),
(206, 'LZ8ZDW', 3, 'COMPAN,BRDEPA,LZ8ZDW', '二分部门1', 102, NULL, 0, 1),
(207, '4TGV7Y', 3, 'COMPAN,9SQR7L,4TGV7Y', '二分部门2', 204, NULL, 0, 1);

-- 初始化用户数据（密码统一为: 123456, MD5: e10adc3949ba59abbe56e057f20f883e）
-- 管理员账号
INSERT INTO sys_user (id, username, password, real_name, dept_id, role_id, status) VALUES
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 100, 1, 1);

-- 总消保部用户
INSERT INTO sys_user (id, username, password, real_name, dept_id, role_id) VALUES
(18, 'zong_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '总消保审批1', 201, 11),
(19, 'zong_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '总消保审批2', 201, 11),
(20, 'zong_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '总消保管理岗1', 201, 4),
(21, 'zong_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '总消保管理岗2', 201, 4);

-- 分消保部1用户
INSERT INTO sys_user (id, username, password, real_name, dept_id, role_id) VALUES
(22, 'fen1_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1管理岗1', 202, 5),
(23, 'fen1_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1管理岗2', 202, 5),
(24, 'fen1_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1审批1', 202, 10),
(25, 'fen1_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1审批2', 202, 10),
(26, 'fen1_xb_section_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1科负责人1', 202, 12),
(27, 'fen1_xb_section_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1科负责人2', 202, 12),
(28, 'fen1_xb_secretary_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1负责人1', 202, 13),
(29, 'fen1_xb_secretary_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部1负责人2', 202, 13);

-- 分消保部2用户
INSERT INTO sys_user (id, username, password, real_name, dept_id, role_id) VALUES
(30, 'fen2_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2管理岗1', 205, 5),
(31, 'fen2_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2管理岗2', 205, 5),
(32, 'fen2_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2审批1', 205, 10),
(33, 'fen2_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2审批2', 205, 10),
(34, 'fen2_xb_section_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2科负责人1', 205, 12),
(35, 'fen2_xb_section_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2科负责人2', 205, 12),
(36, 'fen2_xb_secretary_1', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2负责人1', 205, 13),
(37, 'fen2_xb_secretary_2', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2负责人2', 205, 13);

-- 二分部门1用户
INSERT INTO sys_user (id, username, password, real_name, dept_id, role_id) VALUES
(38, 'erfen1_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1消保管理1', 206, 8),
(39, 'erfen1_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1消保管理2', 206, 8),
(40, 'erfen1_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1消保审批1', 206, 9),
(41, 'erfen1_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1消保审批2', 206, 9);

-- 二分部门2用户
INSERT INTO sys_user (id, username, password, real_name, dept_id, role_id) VALUES
(42, 'erfen2_xb_manager_1', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2消保管理1', 207, 8),
(43, 'erfen2_xb_manager_2', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2消保管理2', 207, 8),
(44, 'erfen2_xb_approver_1', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2消保审批1', 207, 9),
(45, 'erfen2_xb_approver_2', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2消保审批2', 207, 9);

-- 各部门普通用户
INSERT INTO sys_user (id, username, password, real_name, dept_id, role_id) VALUES
(46, 'dept_100_user', 'e10adc3949ba59abbe56e057f20f883e', '总公司普通用户', 100, 11),
(47, 'dept_101_user', 'e10adc3949ba59abbe56e057f20f883e', '总部门普通用户', 101, 14),
(48, 'dept_102_user', 'e10adc3949ba59abbe56e057f20f883e', '分部门1普通用户', 102, 14),
(49, 'dept_1_user', 'e10adc3949ba59abbe56e057f20f883e', '总经办普通用户', 1, 14),
(50, 'dept_2_user', 'e10adc3949ba59abbe56e057f20f883e', '技术部普通用户', 2, 14),
(51, 'dept_3_user', 'e10adc3949ba59abbe56e057f20f883e', '市场部普通用户', 3, 14),
(52, 'dept_4_user', 'e10adc3949ba59abbe56e057f20f883e', '财务部普通用户', 4, 14),
(53, 'dept_5_user', 'e10adc3949ba59abbe56e057f20f883e', '人事部普通用户', 5, 14),
(54, 'dept_201_user', 'e10adc3949ba59abbe56e057f20f883e', '总消保部普通用户', 201, 14),
(55, 'dept_202_user', 'e10adc3949ba59abbe56e057f20f883e', '分消保部普通用户', 202, 14),
(56, 'dept_203_user', 'e10adc3949ba59abbe56e057f20f883e', '技术一部普通用户', 203, 14),
(57, 'dept_204_user', 'e10adc3949ba59abbe56e057f20f883e', '分部门2普通用户', 204, 14),
(58, 'dept_205_user', 'e10adc3949ba59abbe56e057f20f883e', '分消保部2普通用户', 205, 14),
(59, 'dept_206_user', 'e10adc3949ba59abbe56e057f20f883e', '二分部门1普通用户', 206, 14),
(60, 'dept_207_user', 'e10adc3949ba59abbe56e057f20f883e', '二分部门2普通用户', 207, 14);

-- ============================================================================
-- 第七部分: 初始化菜单
-- ============================================================================

-- 一级菜单
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status) VALUES
(1, 0, '素材管理', 'MENU', '/asset', NULL, 'Document', 1, 1),
(2, 0, '流程管理', 'MENU', '/workflow', NULL, 'Operation', 2, 1),
(3, 0, '我的任务', 'MENU', '/approval', NULL, 'Odometer', 3, 1),
(4, 0, '系统管理', 'MENU', '/system', NULL, 'Setting', 4, 1);

-- 二级菜单 - 素材管理
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status) VALUES
(9, 1, '素材列表', 'MENU', '/asset', 'asset/index', NULL, 1, 1),
(27, 1, '素材录入', 'MENU', '/asset/material-entry', 'asset/material-entry/index', NULL, 2, 1),
(14, 1, '素材使用', 'MENU', '/asset/usage-apply', 'asset/usage-apply', 'Download', 3, 1),
(31, 1, '素材删除', 'MENU', '/asset/deletion', 'asset/deletion/index', NULL, 4, 1);

-- 二级菜单 - 流程管理
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status) VALUES
(10, 2, '流程列表', 'MENU', '/workflow', 'workflow/index', NULL, 1, 1),
(11, 2, '流程设计', 'MENU', '/workflow/design', 'workflow/design', NULL, 2, 1);

-- 二级菜单 - 我的任务
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status) VALUES
(12, 3, '待办事项', 'MENU', '/task/pending-approval', 'task/pending-approval', NULL, 1, 1),
(28, 3, '草稿箱', 'MENU', '/task/draft-box', 'task/draft-box', NULL, 2, 1),
(30, 3, '我发起的', 'MENU', '/task/my-initiated', 'task/my-initiated', 'Document', 3, 1),
(29, 3, '审批工单', 'MENU', '/task/material-approval', 'task/material-approval', 'Document', 4, 1),
(32, 3, '流经事项', 'MENU', '/task/flow-items', 'task/flow-items', NULL, 3, 1),
(33, 3, '知会事项', 'MENU', '/task/notifications', 'task/notifications', NULL, 4, 1);

-- 二级菜单 - 系统管理
INSERT INTO sys_menu (id, parent_id, name, type, path, component, icon, sort, status) VALUES
(5, 4, '用户管理', 'MENU', '/system/user', 'system/user/index', 'User', 1, 1),
(6, 4, '角色管理', 'MENU', '/system/role', 'system/role/index', 'UserFilled', 2, 1),
(7, 4, '部门管理', 'MENU', '/system/dept', 'system/dept/index', 'OfficeBuilding', 3, 1),
(8, 4, '菜单管理', 'MENU', '/system/menu', 'system/menu/index', 'Menu', 4, 1);

-- 按钮权限 - 素材管理
INSERT INTO sys_menu (id, parent_id, name, type, path, component, sort, status) VALUES
(101, 9, '上传素材', 'BUTTON', NULL, NULL, 1, 1),
(102, 9, '删除素材', 'BUTTON', NULL, NULL, 2, 1),
(103, 9, '申请使用', 'BUTTON', NULL, NULL, 3, 1),
(104, 9, '下载素材', 'BUTTON', NULL, NULL, 4, 1);

-- 按钮权限 - 流程管理
INSERT INTO sys_menu (id, parent_id, name, type, path, component, sort, status) VALUES
(201, 10, '新增流程', 'BUTTON', NULL, NULL, 1, 1),
(202, 10, '编辑流程', 'BUTTON', NULL, NULL, 2, 1),
(203, 10, '删除流程', 'BUTTON', NULL, NULL, 3, 1);

-- 按钮权限 - 我的任务
INSERT INTO sys_menu (id, parent_id, name, type, path, component, sort, status) VALUES
(34, 3, 'notify', 'BUTTON', NULL, NULL, 5, 1);

-- 按钮权限 - 用户管理
INSERT INTO sys_menu (id, parent_id, name, type, path, component, sort, status) VALUES
(501, 5, '新增用户', 'BUTTON', NULL, NULL, 1, 1),
(502, 5, '编辑用户', 'BUTTON', NULL, NULL, 2, 1),
(503, 5, '删除用户', 'BUTTON', NULL, NULL, 4, 1);

-- 按钮权限 - 角色管理
INSERT INTO sys_menu (id, parent_id, name, type, path, component, sort, status) VALUES
(601, 6, '新增角色', 'BUTTON', NULL, NULL, 1, 1),
(602, 6, '编辑角色', 'BUTTON', NULL, NULL, 2, 1),
(603, 6, '删除角色', 'BUTTON', NULL, NULL, 3, 1),
(604, 6, '配置权限', 'BUTTON', NULL, NULL, 4, 1);

-- 为管理员角色分配菜单权限（仅分配主要菜单，不包含按钮权限）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12), (1, 14), (1, 27), (1, 28),
(1, 29), (1, 30), (1, 31), (1, 32), (1, 33);

-- ============================================================================
-- 第八部分: 初始化审批流程模板
-- ============================================================================

-- 模板1: 单级审批
INSERT INTO workflow (name, description, version, status, workflow_type) VALUES
('单级审批模板', '仅需部门主管审批即可通过', 1, 1, 'ASSET_UPLOAD');
SET @wf1 = LAST_INSERT_ID();
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type) VALUES
(@wf1, '部门主管审批', 1, 'OR');

-- 模板2: 两级审批
INSERT INTO workflow (name, description, version, status, workflow_type) VALUES
('两级审批模板', '需部门主管和总监依次审批', 1, 1, 'ASSET_UPLOAD');
SET @wf2 = LAST_INSERT_ID();
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type) VALUES
(@wf2, '部门主管审批', 1, 'OR'),
(@wf2, '总监审批', 2, 'OR');

-- 模板3: 会签审批
INSERT INTO workflow (name, description, version, status, workflow_type) VALUES
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
SELECT COUNT(*) AS 菜单权限数量 FROM sys_role_menu WHERE role_id = 1;
SELECT COUNT(*) AS 流程模板数量 FROM workflow;
SELECT COUNT(*) AS 流程阶段数量 FROM workflow_stage;
