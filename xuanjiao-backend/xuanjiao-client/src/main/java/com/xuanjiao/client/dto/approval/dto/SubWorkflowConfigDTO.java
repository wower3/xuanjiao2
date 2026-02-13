package com.xuanjiao.client.dto.approval.dto;

import lombok.Data;

import java.util.List;

/**
 * 子流程配置数据传输对象
 *
 * <p>用于在审批任务详情中展示子流程的配置信息，
 * 包括子流程基本信息、第一阶段审批人配置等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class SubWorkflowConfigDTO {

    /**
     * 子流程ID
     */
    private Long id;

    /**
     * 子流程名称
     */
    private String name;

    /**
     * 子流程类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String workflowType;

    /**
     * 审批类型（AND-会签，需要所有审批人通过；OR-或签，任一审批人通过即可）
     */
    private String approveType;

    /**
     * 第一阶段审批人配置列表
     */
    private List<ApproverConfigDTO> approverConfigs;

    /**
     * 审批人数量
     */
    private Integer approverCount;
}
