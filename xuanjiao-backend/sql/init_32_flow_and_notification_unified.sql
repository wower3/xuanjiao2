-- ============================================================================
-- 流经事项和知会事项功能 - 统一迁移脚本
-- 此脚本替代 init_29, init_30, init_31 三个冲突脚本
-- 执行: mysql -u root -p123456 < init_32_flow_and_notification_unified.sql
-- ============================================================================

USE xuanjiao_s;

-- ============================================================================
-- 第一部分：创建通知表（如果不存在）
-- ============================================================================
DROP TABLE IF EXISTS sys_notification;

CREATE TABLE IF NOT EXISTS `sys_notification` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `title` varchar(200) NOT NULL COMMENT '通知标题',
    `content` text COMMENT '通知内容',
    `notification_type` varchar(50) NOT NULL COMMENT '通知类型: WORKFLOW_FLOW-流程流转, MENTION-知会, SYSTEM-系统通知',
    `source_type` varchar(50) COMMENT '来源类型: MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION',
    `source_id` bigint COMMENT '来源ID（审批实例ID）',
    `sender_id` bigint COMMENT '发送人ID',
    `sender_name` varchar(100) COMMENT '发送人名称',
    `recipient_id` bigint NOT NULL COMMENT '接收人ID',
    `is_read` tinyint DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    `read_time` datetime COMMENT '阅读时间',
    `status` tinyint DEFAULT 1 COMMENT '状态: 1-有效, 0-无效',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_recipient_read` (`recipient_id`, `is_read`),
    KEY `idx_source` (`source_type`, `source_id`),
    KEY `idx_sender` (`sender_id`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- ============================================================================
-- 第二部分：添加/更新菜单（使用ON DUPLICATE KEY UPDATE避免冲突）
-- ============================================================================

-- 流经事项菜单 (ID=32, 父菜单"我的任务"ID=3, sort=5)
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `icon`, `sort`, `status`)
VALUES (32, 3, '流经事项', 'MENU', '/task/flow-items', 'task/flow-items', 'Share', 5, 1)
ON DUPLICATE KEY UPDATE
    `name` = '流经事项',
    `path` = '/task/flow-items',
    `component` = 'task/flow-items',
    `icon` = 'Share',
    `sort` = 5;

-- 知会事项菜单 (ID=33, 父菜单"我的任务"ID=3, sort=6)
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `icon`, `sort`, `status`)
VALUES (33, 3, '知会事项', 'MENU', '/task/notifications', 'task/notifications', 'Bell', 6, 1)
ON DUPLICATE KEY UPDATE
    `name` = '知会事项',
    `path` = '/task/notifications',
    `component` = 'task/notifications',
    `icon` = 'Bell',
    `sort` = 6;

-- ============================================================================
-- 第三部分：为所有角色添加菜单权限
-- ============================================================================
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, 32 FROM `sys_role` r WHERE r.deleted = 0;

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, 33 FROM `sys_role` r WHERE r.deleted = 0;

-- ============================================================================
-- 验证结果
-- ============================================================================
SELECT '验证菜单创建:' AS '';
SELECT id, parent_id, name, type, path, component, icon, sort, status
FROM `sys_menu`
WHERE id IN (32, 33)
ORDER BY id;

SELECT '验证角色菜单关联:' AS '';
SELECT rm.role_id, r.name AS role_name, rm.menu_id, m.name AS menu_name
FROM `sys_role_menu` rm
JOIN `sys_role` r ON rm.role_id = r.id
JOIN `sys_menu` m ON rm.menu_id = m.id
WHERE rm.menu_id IN (32, 33)
ORDER BY rm.menu_id, rm.role_id;
