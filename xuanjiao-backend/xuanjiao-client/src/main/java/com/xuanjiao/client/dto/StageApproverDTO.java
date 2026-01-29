package com.xuanjiao.client.dto;

import lombok.Data;

@Data
public class StageApproverDTO {
    private Long id;
    private Long stageId; // 阶段ID
    private String approverType;
    private Long approverId;
    private String approverName;
    private Integer checkSecondaryDept; // 是否校验二级部门（仅当approver_type=ROLE时有效）：0-否，1-是
    private Long subWorkflowId; // 关联的子流程ID（如果该审批人是子流程）
    private String subWorkflowName; // 子流程名称（前端显示用）
}
