-- 审批任务表
DROP TABLE IF EXISTS approval_task;
CREATE TABLE approval_task (
    id BIGINT NOT NULL AUTO_INCREMENT,
    instance_id BIGINT NOT NULL COMMENT '实例ID',
    stage_id BIGINT NOT NULL COMMENT '阶段ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    comment VARCHAR(500) DEFAULT NULL COMMENT '审批意见',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_instance_id (instance_id),
    KEY idx_approver_id (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';
