package com.xuanjiao.client.dto.workflow.dto;

import com.xuanjiao.client.dto.approval.dto.ApproverSelectionDTO;
import com.xuanjiao.client.dto.approval.dto.ApproverConfigDTO;
import lombok.Data;

import java.util.List;

/**
 * 首阶段审批人选择结果数据传输对象
 *
 * <p>用于返回首阶段审批人配置的可选用户列表，
 * 供前端在创建申请时选择审批人。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class FirstStageApproversDTO {

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 工作流类型
     */
    private String workflowType;

    /**
     * 首阶段ID
     */
    private Long firstStageId;

    /**
     * 首阶段名称
     */
    private String firstStageName;

    /**
     * 审批类型（AND-会签，OR-或签）
     */
    private String approveType;

    /**
     * 审批人配置列表（每个配置包含类型、名称、可选用户）
     */
    private List<ApproverConfigDTO> approverConfigs;

    /**
     * 审批人配置数量（用于前端显示"已选择 X / Y 位审批人"）
     */
    private Integer approverCount;
}
