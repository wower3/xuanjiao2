package com.xuanjiao.domain.approval.repository;

import com.xuanjiao.domain.approval.entity.ApprovalInstance;
import java.util.List;

/**
 * 审批实例仓储接口
 *
 * <p>定义审批实例的持久化操作，包括审批实例的查询、保存和更新。</p>
 * <p>审批实例代表一次完整的审批流程，支持主流程和子流程。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface ApprovalInstanceRepository {

    /**
     * 根据ID查找审批实例
     *
     * @param id 审批实例ID
     * @return 审批实例实体，如果不存在返回 null
     */
    ApprovalInstance findById(Long id);

    /**
     * 保存审批实例
     *
     * <p>将新审批实例持久化到数据库，返回带有生成ID的实例。</p>
     *
     * @param instance 审批实例实体
     * @return 保存后的审批实例
     */
    ApprovalInstance save(ApprovalInstance instance);

    /**
     * 更新审批实例
     *
     * <p>更新已存在的审批实例信息，如状态、当前阶段等。</p>
     *
     * @param instance 审批实例实体
     */
    void update(ApprovalInstance instance);

    /**
     * 根据申请人ID查找审批实例列表
     *
     * @param applicantId 申请人ID
     * @return 申请人的审批实例列表
     */
    List<ApprovalInstance> findByApplicantId(Long applicantId);

    /**
     * 根据业务类型和业务ID查找审批实例
     *
     * <p>用于查询特定业务记录的审批实例，如某个素材录入申请的审批流程。</p>
     *
     * @param businessType 业务类型（MATERIAL_ENTRY、ASSET_USAGE、ASSET_DELETION）
     * @param businessId 业务记录ID
     * @return 匹配的审批实例列表
     */
    List<ApprovalInstance> findByBusinessTypeAndBusinessId(String businessType, Long businessId);
}
