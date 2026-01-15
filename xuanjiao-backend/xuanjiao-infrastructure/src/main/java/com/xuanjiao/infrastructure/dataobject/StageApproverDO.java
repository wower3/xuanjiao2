package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("stage_approver")
public class StageApproverDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long stageId;
    private String approverType;
    private Long approverId;
    private Integer checkSecondaryDept; // 是否校验二级部门（仅当approver_type=ROLE时有效）：0-否，1-是
    private Long subWorkflowId; // 关联的子流程ID（如果该审批人是子流程）
}
