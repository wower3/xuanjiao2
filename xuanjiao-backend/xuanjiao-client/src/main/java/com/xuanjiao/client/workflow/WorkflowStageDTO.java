package com.xuanjiao.client.workflow;

import lombok.Data;
import java.util.List;

/**
 * 工作流阶段数据传输对象
 *
 * <p>用于在前后端之间传输工作流阶段信息，包括阶段基本信息、
 * 审批类型和该阶段的审批人列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class WorkflowStageDTO {

    /**
     * 阶段ID
     */
    private Long id;

    /**
     * 所属流程ID
     */
    private Long workflowId;

    /**
     * 阶段名称
     */
    private String name;

    /**
     * 阶段顺序（从1开始）
     */
    private Integer stageOrder;

    /**
     * 审批类型（AND-会签，需要所有审批人通过；OR-或签，任一审批人通过即可）
     */
    private String approveType;

    /**
     * 该阶段的审批人列表
     */
    private List<StageApproverDTO> approvers;
}
