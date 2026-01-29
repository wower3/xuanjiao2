package com.xuanjiao.app.workflow;

import com.xuanjiao.client.dto.ApproverSelectionDTO;
import com.xuanjiao.client.dto.ApprovalProgressDTO;
import com.xuanjiao.client.dto.WorkflowDTO;

import java.util.List;
import java.util.Map;

/**
 * 审批人选择服务
 */
public interface ApproverSelectionService {

    /**
     * 获取下一层可选审批人
     * @param stageId 下一阶段ID
     * @param instanceId 审批实例ID
     * @param applicantId 申请人ID
     * @param keyword 搜索关键词（用户名或姓名，支持中英文模糊查询）
     * @return 可选审批人列表
     */
    List<ApproverSelectionDTO> getNextStageApprovers(Long stageId, Long instanceId, Long applicantId, String keyword);

    /**
     * 选择下一层审批人（兼容旧接口）
     * @param taskId 当前任务ID
     * @param approverIds 选择的审批人ID列表
     */
    void selectNextStageApprovers(Long taskId, List<Long> approverIds);

    /**
     * 选择下一层审批人和子流程审批人
     * @param taskId 当前任务ID
     * @param approverIds 选择的下一层审批人ID列表
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表）
     */
    void selectNextStageApprovers(Long taskId, List<Long> approverIds, Map<Long, List<Long>> subWorkflowApproverIds);

    /**
     * 获取第一层可选审批人
     * @param workflowId 流程ID
     * @param applicantId 申请人ID
     * @param keyword 搜索关键词（用户名或姓名，支持中英文模糊查询）
     * @return 包含流程信息和审批人配置的Map，含 workflowId, workflowName, stageId, stageName, approveType, approverConfigs, approverCount
     */
    Map<String, Object> getFirstStageApprovers(Long workflowId, Long applicantId, String keyword);

    /**
     * 选择第一层审批人（兼容旧接口）
     * @param instanceId 审批实例ID
     * @param approverIds 选择的审批人ID列表
     */
    void selectFirstStageApprovers(Long instanceId, List<Long> approverIds);

    /**
     * 选择第一层审批人（包括主流程审批人和子流程第一层审批人）
     * @param instanceId 审批实例ID
     * @param approverIds 主流程第一层审批人ID列表
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表）
     */
    void selectFirstStageApproversWithSubWorkflows(Long instanceId, List<Long> approverIds, Map<Long, List<Long>> subWorkflowApproverIds);

    /**
     * 获取审批实例进度
     * @param instanceId 实例ID
     * @return 进度列表（包含子流程）
     */
    List<ApprovalProgressDTO> getApprovalProgress(Long instanceId);

    /**
     * 检查角色是否绑定了审批流程
     * @param roleId 角色ID
     * @param workflowType 流程类型
     * @return 绑定的流程，如果未绑定返回null
     */
    WorkflowDTO getWorkflowByRole(Long roleId, String workflowType);

    /**
     * 获取子流程第一层可选审批人
     * @param subWorkflowId 子流程ID
     * @param applicantId 申请人ID
     * @param keyword 搜索关键词（用户名或姓名，支持中英文模糊查询）
     * @return 包含流程信息和审批人配置的Map，含 workflowId, workflowName, approverConfigs, approverCount
     */
    Map<String, Object> getSubWorkflowFirstStageApprovers(Long subWorkflowId, Long applicantId, String keyword);

    /**
     * 选择子流程第一层审批人
     * @param subInstanceId 子流程实例ID
     * @param approverIds 选择的审批人ID列表
     */
    void selectSubWorkflowFirstStageApprovers(Long subInstanceId, List<Long> approverIds);
}
