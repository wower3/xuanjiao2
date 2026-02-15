package com.xuanjiao.client.workflow;

import com.xuanjiao.client.user.UserDTO;
import lombok.Data;

import java.util.List;

/**
 * 阶段审批人数据传输对象
 *
 * <p>用于在前后端之间传输工作流阶段中的审批人配置信息，
 * 包括审批人类型、关联的子流程等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class StageApproverDTO {

    /**
     * 审批人配置ID
     */
    private Long id;

    /**
     * 所属阶段ID
     */
    private Long stageId;

    /**
     * 审批人类型（USER-用户、DEPT-部门、ROLE-角色）
     */
    private String approverType;

    /**
     * 审批人ID（用户ID、部门ID或角色ID）
     */
    private Long approverId;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 审批人类型名称（如：指定用户、指定角色、指定部门）
     */
    private String approverTypeName;

    /**
     * 是否校验二级部门
     *
     * <p>仅当approver_type=ROLE时有效：0-否，1-是</p>
     */
    private Integer checkSecondaryDept;

    /**
     * 关联的子流程ID
     *
     * <p>如果该审批人配置了子流程，则此字段为子流程的ID</p>
     */
    private Long subWorkflowId;

    /**
     * 子流程名称（前端显示用）
     */
    private String subWorkflowName;

    /**
     * 可选用户列表（仅在任务详情接口中使用）
     */
    private List<UserDTO> availableUsers;
}
