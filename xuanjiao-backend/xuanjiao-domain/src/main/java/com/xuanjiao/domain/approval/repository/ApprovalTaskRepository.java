package com.xuanjiao.domain.approval.repository;

import com.xuanjiao.domain.approval.entity.ApprovalTask;
import java.util.List;

/**
 * 审批任务仓储接口
 *
 * <p>定义审批任务的持久化操作，包括任务的查询、保存、更新和删除。</p>
 * <p>审批任务代表审批流程中分配给具体审批人的任务项。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface ApprovalTaskRepository {

    /**
     * 根据ID查找审批任务
     *
     * @param id 审批任务ID
     * @return 审批任务实体，如果不存在返回 null
     */
    ApprovalTask findById(Long id);

    /**
     * 保存审批任务
     *
     * <p>将新审批任务持久化到数据库。</p>
     *
     * @param task 审批任务实体
     * @return 保存后的审批任务
     */
    ApprovalTask save(ApprovalTask task);

    /**
     * 更新审批任务
     *
     * <p>更新已存在的审批任务信息，如状态、审批意见等。</p>
     *
     * @param task 审批任务实体
     */
    void update(ApprovalTask task);

    /**
     * 根据审批实例ID查找任务列表
     *
     * <p>获取某个审批实例下的所有审批任务。</p>
     *
     * @param instanceId 审批实例ID
     * @return 该实例下的所有审批任务列表
     */
    List<ApprovalTask> findByInstanceId(Long instanceId);

    /**
     * 根据审批人ID和状态查找任务列表
     *
     * <p>获取某个审批人待处理或已处理的任务。</p>
     *
     * @param approverId 审批人ID
     * @param status 任务状态（PENDING、APPROVED、REJECTED等）
     * @return 匹配的审批任务列表
     */
    List<ApprovalTask> findByApproverIdAndStatus(Long approverId, String status);

    /**
     * 根据ID删除审批任务
     *
     * @param id 审批任务ID
     */
    void deleteById(Long id);
}
