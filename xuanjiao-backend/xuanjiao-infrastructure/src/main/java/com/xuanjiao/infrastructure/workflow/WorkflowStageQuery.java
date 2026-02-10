package com.xuanjiao.infrastructure.workflow;

import lombok.Data;

/**
 * 工作流阶段查询条件对象
 *
 * <p>用于动态构建工作流阶段查询条件，对应 WorkflowStageMapper 使用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowStageQuery {

    /**
     * 阶段ID
     */
    private Long id;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 阶段名称
     */
    private String name;

    /**
     * 阶段顺序
     */
    private Integer stageOrder;

    /**
     * 审批类型（AND-会签、OR-或签）
     */
    private String approveType;

    /**
     * 删除标记
     */
    private Integer deleted;

    /**
     * 排序字段
     */
    private String orderByField;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderByDirection;
}
