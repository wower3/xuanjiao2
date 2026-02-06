package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 阶段审批人数据对象
 * <p>对应数据库表 stage_approver，存储阶段审批人配置的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.workflow.entity.StageApprover
 */
@Data
@TableName("stage_approver")
public class StageApproverDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的工作流阶段ID */
    private Long stageId;

    /** 审批人类型：USER-用户、ROLE-角色、DEPT-部门 */
    private String approverType;

    /** 审批人ID */
    private Long approverId;

    /** 是否校验二级部门（仅当approver_type=ROLE时有效）：0-否，1-是 */
    private Integer checkSecondaryDept;

    /** 关联的子流程ID（如果该审批人是子流程入口） */
    private Long subWorkflowId;
}
