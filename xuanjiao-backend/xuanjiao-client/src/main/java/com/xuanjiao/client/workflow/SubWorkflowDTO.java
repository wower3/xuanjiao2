package com.xuanjiao.client.workflow;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 子流程数据传输对象
 *
 * <p>继承 WorkflowDTO，包含子流程特有的审批人配置信息。
 * 用于在任务详情接口中返回子流程的完整信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubWorkflowDTO extends WorkflowDTO {

    /**
     * 子流程第一阶段审批类型（OR-或签、AND-会签）
     */
    private String approveType;

    /**
     * 审批人配置列表
     */
    private List<StageApproverDTO> approverConfigs;

    /**
     * 审批人数量
     */
    private Integer approverCount;
}
