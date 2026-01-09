-- 阶段审批人配置表
DROP TABLE IF EXISTS stage_approver;
CREATE TABLE stage_approver (
    id BIGINT NOT NULL AUTO_INCREMENT,
    stage_id BIGINT NOT NULL COMMENT '阶段ID',
    approver_type VARCHAR(20) NOT NULL COMMENT '审批人类型：USER/ROLE/DEPT/SELF',
    approver_id BIGINT DEFAULT NULL COMMENT '审批人/角色/部门ID',
    PRIMARY KEY (id),
    KEY idx_stage_id (stage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阶段审批人配置表';
