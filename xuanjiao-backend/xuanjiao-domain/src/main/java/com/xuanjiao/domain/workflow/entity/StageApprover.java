package com.xuanjiao.domain.workflow.entity;

import lombok.Data;

/**
 * 阶段审批人实体
 * <p>定义工作流阶段中的审批人配置，支持用户、角色、部门三种类型</p>
 * <p>审批人可以是具体用户，也可以是角色或部门（运行时解析为具体用户）</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.StageApproverDO
 */
@Data
public class StageApprover {
    /** 阶段审批人唯一标识，自增主键 */
    private Long id;

    /** 关联的工作流阶段ID，指向WorkflowStage */
    private Long stageId;

    /** 审批人类型：USER-用户、ROLE-角色、DEPT-部门 */
    private String approverType;

    /** 审批人ID，当类型为USER时指向sys_user表，为ROLE时指向sys_role表，为DEPT时指向sys_dept表 */
    private Long approverId;
}
