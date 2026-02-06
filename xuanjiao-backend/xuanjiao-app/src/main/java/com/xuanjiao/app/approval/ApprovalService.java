package com.xuanjiao.app.approval;

import com.xuanjiao.client.dto.PageResult;
import java.util.Map;

/**
 * 审批服务接口
 * <p>提供审批任务管理、审批操作、流程查询等功能</p>
 * <p>支持按业务类型筛选：素材录入、素材使用、素材删除</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.approval.impl.ApprovalServiceImpl
 */
public interface ApprovalService {

    /**
     * 获取待办任务列表
     * <p>支持按业务类型筛选，返回分页结果</p>
     * <p>只返回状态为PENDING的任务</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param businessType 业务类型筛选（可选，如 MATERIAL_ENTRY、ASSET_USAGE、ASSET_DELETION）
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getMyTasks(Long userId, int pageNum, int pageSize, String businessType);

    /**
     * 获取待办任务数量
     * <p>用于徽章显示</p>
     *
     * @param userId 用户ID
     * @return 待办任务数量（status=PENDING的任务数）
     */
    Long getMyTasksCount(Long userId);

    /**
     * 获取我发起的审批申请
     * <p>支持多条件筛选，返回分页结果</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param businessType 业务类型筛选（可选，如 MATERIAL_ENTRY、ASSET_USAGE 等）
     * @param forAllUsers 是否查询所有用户的工单（true=可查看所有用户，false=仅查询当前用户）
     * @param applicantId 发起人ID筛选（可选，仅在 forAllUsers=true 时有效）
     * @param deptId 部门ID筛选（可选，仅在 forAllUsers=true 时有效）
     * @param roleType 角色类型筛选（可选，仅在 forAllUsers=true 时有效）
     * @param status 审批状态筛选（可选，如 PENDING、APPROVED、REJECTED、CANCELLED）
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getMyApplied(Long userId, int pageNum, int pageSize,
                                                     String businessType, boolean forAllUsers,
                                                     Long applicantId, Long deptId, String roleType,
                                                     String status);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    Map<String, Object> getTaskDetail(Long taskId);

    /**
     * 获取审批实例详情
     *
     * @param instanceId 审批实例ID
     * @return 审批实例详情
     */
    Map<String, Object> getInstanceDetail(Long instanceId);

    /**
     * 审批通过或拒绝
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @param comment 审批意见
     * @param passed 是否通过
     */
    void approve(Long taskId, Long userId, String comment, boolean passed);

    /**
     * 退回上一级
     * <p>将任务退回给上一阶段的审批人</p>
     *
     * @param taskId 任务ID
     * @param userId 当前用户ID
     * @param comment 退回原因（可选）
     */
    void returnTask(Long taskId, Long userId, String comment);

    /**
     * 获取流经事项列表
     * <p>获取用户发起或审批过的所有工单</p>
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param businessType 业务类型筛选（可选）
     * @param status 审批状态筛选（可选）
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getMyFlowItems(Long userId, int pageNum, int pageSize,
                                                     String businessType, String status);
}
