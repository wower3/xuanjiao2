package com.xuanjiao.client.workflow;

import lombok.Data;
import java.util.List;

/**
 * 第一层审批人配置数据传输对象
 *
 * <p>用于返回审批流程第一层可选审批人配置信息，包括流程信息、
 * 阶段信息和审批人配置列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class FirstStageApproversDTO {

    /**
     * 流程ID
     */
    private Long workflowId;

    /**
     * 流程名称
     */
    private String workflowName;

    /**
     * 阶段ID（主流程有，子流程无）
     */
    private Long stageId;

    /**
     * 阶段名称（主流程有，子流程无）
     */
    private String stageName;

    /**
     * 审批类型（OR_SIGN-或签、AND_SIGN-会签）
     */
    private String approveType;

    /**
     * 审批人配置列表
     */
    private List<ApproverConfigDTO> approverConfigs;

    /**
     * 审批人配置数量
     */
    private Integer approverCount;
}
