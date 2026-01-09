-- 内置审批流程模板数据
SET NAMES utf8mb4;
USE xuanjiao_s;

-- 模板1: 单级审批（部门主管审批）
INSERT INTO workflow (name, description, version, status) VALUES
('单级审批模板', '仅需部门主管审批即可通过', 1, 1);
SET @wf1 = LAST_INSERT_ID();
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type) VALUES
(@wf1, '部门主管审批', 1, 'OR');

-- 模板2: 两级审批（部门主管+总监）
INSERT INTO workflow (name, description, version, status) VALUES
('两级审批模板', '需部门主管和总监依次审批', 1, 1);
SET @wf2 = LAST_INSERT_ID();
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type) VALUES
(@wf2, '部门主管审批', 1, 'OR'),
(@wf2, '总监审批', 2, 'OR');

-- 模板3: 会签审批（多人同时审批）
INSERT INTO workflow (name, description, version, status) VALUES
('会签审批模板', '需要所有审批人全部通过', 1, 1);
SET @wf3 = LAST_INSERT_ID();
INSERT INTO workflow_stage (workflow_id, name, stage_order, approve_type) VALUES
(@wf3, '会签审批', 1, 'AND');
