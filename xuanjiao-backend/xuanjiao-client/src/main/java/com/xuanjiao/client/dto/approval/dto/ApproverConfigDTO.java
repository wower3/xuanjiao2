package com.xuanjiao.client.dto.approval.dto;

import lombok.Data;
import java.util.List;

/**
 * 审批人配置数据传输对象
 *
 * <p>用于在审批任务详情中展示下一阶段的审批人配置信息，
 * 包括配置类型、审批人ID、可选用户列表等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class ApproverConfigDTO {

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 审批人类型（USER-用户、DEPT-部门、ROLE-角色）
     */
    private String approverType;

    /**
     * 审批人ID（当 approverType=USER 时为用户ID，其他为角色ID）
     */
    private Long approverId;

    /**
     * 是否校验二级部门（仅当 approverType=ROLE 时有效）
     */
    private Integer checkSecondaryDept;

    /**
     * 子流程ID（如果有配置子流程）
     */
    private Long subWorkflowId;

    /**
     * 子流程名称
     */
    private String subWorkflowName;

    /**
     * 审批人类型名称（中文显示）
     */
    private String approverTypeName;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 该配置的可选用户列表（用于前端下拉选择）
     */
    private List<ApproverSelectionDTO> availableUsers;
}
