-- 审批流程阶段表
CREATE TABLE IF NOT EXISTS workflow_stage (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workflow_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    stage_order INT NOT NULL,
    approve_type VARCHAR(20) DEFAULT 'OR',
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_workflow_id (workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 阶段审批人配置表
CREATE TABLE IF NOT EXISTS stage_approver (
    id BIGINT NOT NULL AUTO_INCREMENT,
    stage_id BIGINT NOT NULL,
    approver_type VARCHAR(20) NOT NULL,
    approver_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_stage_id (stage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
