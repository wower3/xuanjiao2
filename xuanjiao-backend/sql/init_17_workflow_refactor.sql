-- ============================================================
-- 审批流程重构 - 数据库表结构变更
-- 日期: 2025-01-14
-- 说明: 支持二级部门校验、角色绑定、子流程、审批人选择等新功能
-- ============================================================

-- 1. 修改workflow表 - 增加角色绑定和流程类型
ALTER TABLE workflow
    ADD COLUMN bound_role_id BIGINT DEFAULT NULL COMMENT '绑定的角色ID（一个流程对应一个角色）' AFTER status,
    ADD COLUMN workflow_type VARCHAR(50) DEFAULT NULL COMMENT '流程类型：ASSET_UPLOAD-素材录入审批，ASSET_USAGE-素材使用审批' AFTER bound_role_id,
    ADD INDEX idx_bound_role_id (bound_role_id);

-- 2. 修改workflow_stage表 - 增加子流程支持
ALTER TABLE workflow_stage
    ADD COLUMN sub_workflow_id BIGINT DEFAULT NULL COMMENT '子流程ID（如果该阶段嵌入子流程）' AFTER approve_type,
    ADD COLUMN is_sub_workflow TINYINT DEFAULT 0 COMMENT '是否是子流程：0-否，1-是' AFTER sub_workflow_id,
    ADD INDEX idx_sub_workflow_id (sub_workflow_id);

-- 3. 修改stage_approver表 - 增加二级部门校验
ALTER TABLE stage_approver
    ADD COLUMN check_secondary_dept TINYINT DEFAULT 0 COMMENT '是否校验二级部门（仅当approver_type=ROLE时有效）：0-否，1-是' AFTER approver_id;

-- 4. 修改approval_instance表 - 增加子流程关联
ALTER TABLE approval_instance
    ADD COLUMN parent_instance_id BIGINT DEFAULT NULL COMMENT '父实例ID（用于子流程关联）' AFTER status,
    ADD COLUMN parent_stage_id BIGINT DEFAULT NULL COMMENT '父阶段ID（用于子流程关联）' AFTER parent_instance_id,
    ADD COLUMN root_instance_id BIGINT DEFAULT NULL COMMENT '根实例ID（用于追溯主流程）' AFTER parent_stage_id,
    ADD INDEX idx_parent_instance (parent_instance_id),
    ADD INDEX idx_root_instance (root_instance_id);

-- 5. 修改approval_task表 - 增加下一层审批人选择相关字段
ALTER TABLE approval_task
    ADD COLUMN next_stage_approver_ids TEXT DEFAULT NULL COMMENT '下一层审批人IDs（JSON格式）' AFTER comment,
    ADD COLUMN selected_by_user_id BIGINT DEFAULT NULL COMMENT '选择下一层审批人的用户ID' AFTER next_stage_approver_ids,
    ADD COLUMN is_first_approver TINYINT DEFAULT 1 COMMENT '是否是该阶段第一个审批人：0-否，1-是' AFTER selected_by_user_id,
    ADD INDEX idx_selected_by (selected_by_user_id);

-- 6. 创建审批进度表 - 用于记录和显示审批进度
DROP TABLE IF EXISTS approval_progress;
CREATE TABLE approval_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    instance_id BIGINT NOT NULL COMMENT '审批实例ID',
    stage_id BIGINT NOT NULL COMMENT '阶段ID',
    stage_name VARCHAR(100) NOT NULL COMMENT '阶段名称',
    stage_order INT NOT NULL COMMENT '阶段顺序',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审批，APPROVED-已通过，REJECTED-已驳回，SKIPPED-已跳过',
    approvers TEXT DEFAULT NULL COMMENT '审批人列表（JSON格式：[{id, name, status, approveTime}]）',
    is_sub_workflow TINYINT DEFAULT 0 COMMENT '是否是子流程：0-否，1-是',
    parent_instance_id BIGINT DEFAULT NULL COMMENT '父实例ID（用于子流程）',
    parent_stage_id BIGINT DEFAULT NULL COMMENT '父阶段ID（用于子流程）',
    approve_time DATETIME DEFAULT NULL COMMENT '审批通过时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_instance_id (instance_id),
    INDEX idx_parent_instance (parent_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批进度表';

-- 7. 为角色绑定创建索引（如果不存在）
-- 注意：sys_role表应该在用户管理相关脚本中创建

-- 8. 更新现有数据（如果需要）
-- 将现有的素材录入审批流程绑定到合适的角色（需要根据实际情况调整）
-- UPDATE workflow SET bound_role_id = 3, workflow_type = 'ASSET_UPLOAD' WHERE id = 1;
