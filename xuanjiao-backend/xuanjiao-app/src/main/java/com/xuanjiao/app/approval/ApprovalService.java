package com.xuanjiao.app.approval;

import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.approval.FlowItemDTO;
import com.xuanjiao.client.approval.InstanceDetailDTO;
import com.xuanjiao.client.approval.MyAppliedDTO;
import com.xuanjiao.client.approval.PendingTaskDTO;
import com.xuanjiao.client.approval.TaskDetailDTO;

/**
 * 审批服务接口
 *
 * <p>提供审批任务管理、审批操作、流程查询等功能。
 * 支持按业务类型筛选：素材录入、素材使用、素材删除。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>待办任务查询与统计</li>
 *   <li>我发起的申请查询</li>
 *   <li>流经事项（历史审批记录）查询</li>
 *   <li>审批操作（通过、拒绝、退回）</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.approval.impl.ApprovalServiceImpl
 */
public interface ApprovalService {

    /**
     * 获取待办任务列表
     *
     * <p>返回当前用户需要处理的审批任务。支持按业务类型筛选，只返回状态为PENDING的任务。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param businessType 业务类型筛选（可选），可选值：MATERIAL_ENTRY、ASSET_USAGE、ASSET_DELETION，为null时查询全部
     * @return 分页结果，包含任务基本信息、申请信息、流程信息等
     */
    PageResult<PendingTaskDTO> getMyTasks(Long userId, int pageNum, int pageSize, String businessType);

    /**
     * 获取待办任务数量
     *
     * <p>返回当前用户待处理的任务总数，用于前端徽章显示。</p>
     *
     * @param userId 用户ID
     * @return 待办任务数量（status=PENDING的任务数）
     */
    Long getMyTasksCount(Long userId);

    /**
     * 获取我发起的审批申请
     *
     * <p>返回当前用户发起的所有审批申请。管理员可以查看所有用户的申请。</p>
     *
     * @param userId 当前用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param businessType 业务类型筛选（可选），可选值：MATERIAL_ENTRY、ASSET_USAGE、ASSET_DELETION
     * @param forAllUsers 是否查询所有用户的工单（true=管理员查看所有，false=仅查看当前用户）
     * @param applicantId 发起人ID筛选（可选，仅在 forAllUsers=true 时有效）
     * @param deptId 部门ID筛选（可选，仅在 forAllUsers=true 时有效）
     * @param roleType 角色类型筛选（可选，仅在 forAllUsers=true 时有效）
     * @param status 审批状态筛选（可选），可选值：PENDING、APPROVED、REJECTED、CANCELLED
     * @return 分页结果，包含申请基本信息、审批状态、流程进度等
     */
    PageResult<MyAppliedDTO> getMyApplied(Long userId, int pageNum, int pageSize,
                                          String businessType, boolean forAllUsers,
                                          Long applicantId, Long deptId, String roleType,
                                          String status);

    /**
     * 获取任务详情
     *
     * <p>返回审批任务的完整详情，包含任务信息、申请信息、审批进度、可选审批人等。</p>
     *
     * @param taskId 任务ID
     * @return 任务详情DTO
     */
    TaskDetailDTO getTaskDetail(Long taskId);

    /**
     * 获取审批实例详情
     *
     * <p>返回审批实例的完整详情，包含实例信息、申请信息、所有阶段进度等。</p>
     *
     * @param instanceId 审批实例ID
     * @return 审批实例详情DTO
     */
    InstanceDetailDTO getInstanceDetail(Long instanceId);

    /**
     * 审批通过或拒绝
     *
     * <p>处理审批任务，更新任务状态并推进流程。如果是最后一个审批人，
     * 将触发流程完成回调处理业务逻辑。</p>
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @param comment 审批意见，可为null
     * @param passed 是否通过（true-通过，false-拒绝）
     */
    void approve(Long taskId, Long userId, String comment, boolean passed);

    /**
     * 退回上一级
     *
     * <p>将任务退回给上一阶段的审批人。退回后，上一阶段会重新生成待办任务，
     * 当前任务状态变为RETURNED。</p>
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @param comment 退回原因，可为null
     */
    void returnTask(Long taskId, Long userId, String comment);

    /**
     * 获取流经事项列表
     *
     * <p>返回用户发起或审批过的所有工单记录，用于历史查看。</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param businessType 业务类型筛选（可选），可选值：MATERIAL_ENTRY、ASSET_USAGE、ASSET_DELETION
     * @param status 审批状态筛选（可选），可选值：PENDING、APPROVED、REJECTED、CANCELLED
     * @return 分页结果，包含工单基本信息和审批状态
     */
    PageResult<FlowItemDTO> getMyFlowItems(Long userId, int pageNum, int pageSize,
                                           String businessType, String status);
}
