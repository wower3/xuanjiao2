-- 审批实例表
DROP TABLE IF EXISTS approval_instance;
CREATE TABLE approval_instance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workflow_id BIGINT NOT NULL COMMENT '流程ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    business_id BIGINT NOT NULL COMMENT '业务ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    current_stage_id BIGINT DEFAULT NULL COMMENT '当前阶段ID',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例表';
