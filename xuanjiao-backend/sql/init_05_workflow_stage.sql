-- 审批流程阶段表
DROP TABLE IF EXISTS workflow_stage;
CREATE TABLE workflow_stage (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '阶段ID',
    workflow_id BIGINT NOT NULL COMMENT '流程ID',
    name VARCHAR(100) NOT NULL COMMENT '阶段名称',
    stage_order INT NOT NULL COMMENT '阶段顺序',
    approve_type VARCHAR(20) DEFAULT 'OR' COMMENT '审批类型：OR-或签，AND-会签',
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_workflow_id (workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流程阶段表';
