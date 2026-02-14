package com.xuanjiao.app.workflow;

import com.xuanjiao.client.ApproverSelectionDTO;
import com.xuanjiao.client.ApprovalProgressDTO;
import com.xuanjiao.client.WorkflowDTO;

import java.util.List;
import java.util.Map;

/**
 * 审批人选择服务接口
 *
 * <p>定义审批流程中审批人选择的核心业务操作。支持主流程和子流程的审批人选择，
 * 包括第一层审批人选择、下一层审批人选择、审批进度查询等功能。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>获取可选审批人列表（支持搜索）</li>
 *   <li>选择审批人（主流程和子流程）</li>
 *   <li>查询审批进度</li>
 *   <li>角色与流程绑定查询</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface ApproverSelectionService {

    /**
     * 获取下一层可选审批人
     *
     * <p>根据阶段配置获取下一阶段可选的审批人列表，支持按用户名或姓名进行模糊搜索。</p>
     *
     * @param stageId 下一阶段ID
     * @param instanceId 审批实例ID
     * @param applicantId 申请人ID
     * @param keyword 搜索关键词（用户名或姓名，支持中英文模糊查询），可为null表示不搜索
     * @return 可选审批人列表，包含用户基本信息和部门信息
     */
    List<ApproverSelectionDTO> getNextStageApprovers(Long stageId, Long instanceId, Long applicantId, String keyword);

    /**
     * 选择下一层审批人（兼容旧接口）
     *
     * <p>为当前审批任务选择下一阶段的审批人，不包含子流程审批人选择。</p>
     *
     * @param taskId 当前任务ID
     * @param approverIds 选择的审批人ID列表，不能为空
     */
    void selectNextStageApprovers(Long taskId, List<Long> approverIds);

    /**
     * 选择下一层审批人和子流程审批人
     *
     * <p>为当前审批任务选择下一阶段的审批人，同时可以指定各子流程的第一层审批人。</p>
     *
     * @param taskId 当前任务ID
     * @param approverIds 选择的下一层审批人ID列表，不能为空
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表），可为null
     */
    void selectNextStageApprovers(Long taskId, List<Long> approverIds, Map<Long, List<Long>> subWorkflowApproverIds);

    /**
     * 获取第一层可选审批人
     *
     * <p>获取流程第一阶段可选的审批人配置信息，用于申请提交时选择审批人。</p>
     *
     * @param workflowId 流程ID
     * @param applicantId 申请人ID
     * @param keyword 搜索关键词（用户名或姓名，支持中英文模糊查询），可为null表示不搜索
     * @return 包含流程信息和审批人配置的Map，含 workflowId, workflowName, stageId, stageName, approveType, approverConfigs, approverCount
     */
    Map<String, Object> getFirstStageApprovers(Long workflowId, Long applicantId, String keyword);

    /**
     * 选择第一层审批人（兼容旧接口）
     *
     * <p>为审批实例选择第一阶段的审批人，不包含子流程审批人选择。</p>
     *
     * @param instanceId 审批实例ID
     * @param approverIds 选择的审批人ID列表，不能为空
     */
    void selectFirstStageApprovers(Long instanceId, List<Long> approverIds);

    /**
     * 选择第一层审批人（包括主流程审批人和子流程第一层审批人）
     *
     * <p>为审批实例选择第一阶段的审批人，同时可以指定各子流程的第一层审批人。</p>
     *
     * @param instanceId 审批实例ID
     * @param approverIds 主流程第一层审批人ID列表，不能为空
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表），可为null
     */
    void selectFirstStageApproversWithSubWorkflows(Long instanceId, List<Long> approverIds, Map<Long, List<Long>> subWorkflowApproverIds);

    /**
     * 获取审批实例进度
     *
     * <p>获取审批实例的完整审批进度，包含主流程和所有子流程的进度信息。</p>
     *
     * @param instanceId 实例ID
     * @return 进度列表（包含子流程），每个元素代表一个阶段的审批进度
     */
    List<ApprovalProgressDTO> getApprovalProgress(Long instanceId);

    /**
     * 检查角色是否绑定了审批流程
     *
     * <p>根据角色ID和流程类型查询该角色绑定的审批流程配置。</p>
     *
     * @param roleId 角色ID
     * @param workflowType 流程类型（如：MATERIAL_ENTRY, ASSET_USAGE, ASSET_DELETION）
     * @return 绑定的流程信息，如果未绑定返回null
     */
    WorkflowDTO getWorkflowByRole(Long roleId, String workflowType);

    /**
     * 获取子流程第一层可选审批人
     *
     * <p>获取子流程第一阶段可选的审批人配置信息，用于在主流程审批时选择子流程审批人。</p>
     *
     * @param subWorkflowId 子流程ID
     * @param applicantId 申请人ID
     * @param keyword 搜索关键词（用户名或姓名，支持中英文模糊查询），可为null表示不搜索
     * @return 包含流程信息和审批人配置的Map，含 workflowId, workflowName, approverConfigs, approverCount
     */
    Map<String, Object> getSubWorkflowFirstStageApprovers(Long subWorkflowId, Long applicantId, String keyword);

    /**
     * 选择子流程第一层审批人
     *
     * <p>为子流程实例选择第一阶段的审批人。</p>
     *
     * @param subInstanceId 子流程实例ID
     * @param approverIds 选择的审批人ID列表，不能为空
     */
    void selectSubWorkflowFirstStageApprovers(Long subInstanceId, List<Long> approverIds);
}
