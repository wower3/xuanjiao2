package com.xuanjiao.client.workflow;

import lombok.Data;
import java.util.List;

/**
 * 审批人配置数据传输对象
 *
 * <p>用于传输单个审批人配置信息，包括审批人类型、ID、名称
 * 和可选用户列表。</p>
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
     * 审批人类型（USER-指定用户、ROLE-指定角色、DEPT-指定部门、SUB_WORKFLOW-子流程）
     */
    private String approverType;

    /**
     * 审批人ID（用户ID/角色ID/部门ID/子流程ID）
     */
    private Long approverId;

    /**
     * 是否需要校验二级部门（0-否，1-是）
     */
    private Integer checkSecondaryDept;

    /**
     * 子流程ID（仅 SUB_WORKFLOW 类型有）
     */
    private Long subWorkflowId;

    /**
     * 审批人类型名称
     */
    private String approverTypeName;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 可选用户列表
     */
    private List<ApproverSelectionDTO> availableUsers;
}
