package com.xuanjiao.infrastructure.workflow;

import lombok.Data;

import java.util.List;

/**
 * 阶段审批人查询条件对象
 *
 * <p>用于动态构建阶段审批人查询条件，对应 StageApproverMapper 使用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class StageApproverQuery {

    /**
     * 审批人配置ID
     */
    private Long id;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 阶段ID列表（用于批量查询）
     */
    private List<Long> stageIds;

    /**
     * 审批人类型（USER-用户、ROLE-角色、DEPT-部门）
     */
    private String approverType;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 是否校验二级部门（0-否、1-是）
     */
    private Integer checkSecondaryDept;

    /**
     * 关联的子流程ID
     */
    private Long subWorkflowId;

    /**
     * 子流程ID是否为NULL（用于 IS NULL 查询）
     */
    private Boolean subWorkflowIdNull;

    /**
     * 子流程ID是否不为NULL（用于 IS NOT NULL 查询）
     */
    private Boolean subWorkflowIdNotNull;

    /**
     * 排序字段
     */
    private String orderByField;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderByDirection;
}
