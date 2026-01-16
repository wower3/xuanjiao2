package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.PageResult;
import java.util.Map;

public interface ApprovalService {
    PageResult<Map<String, Object>> getMyTasks(Long userId, int pageNum, int pageSize);

    /**
     * 获取我发起的审批申请（支持筛选）
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

    Map<String, Object> getTaskDetail(Long taskId);

    Map<String, Object> getInstanceDetail(Long instanceId);

    void approve(Long taskId, Long userId, String comment, boolean passed);
}
