package com.xuanjiao.client.dto.approval;

import java.time.LocalDateTime;

/**
 * 流经事项数据传输对象
 *
 * <p>用于显示用户参与过的所有工单（作为发起人或审批人），
 * 聚合展示不同业务类型的审批记录。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class FlowItemDTO {

    /**
     * 工单ID（审批实例ID）
     */
    private Long id;

    /**
     * 工单状态（PENDING-待审批、APPROVED-已通过、REJECTED-已驳回、CANCELLED-已取消）
     */
    private String status;

    /**
     * 业务类型
     * <ul>
     *   <li>MATERIAL_ENTRY - 素材录入</li>
     *   <li>ASSET_USAGE - 素材使用</li>
     *   <li>ASSET_DELETION - 素材删除</li>
     * </ul>
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 我的角色（initiator-发起人、approver-审批人）
     */
    private String myRole;

    // ==================== 业务类型相关字段 ====================

    /**
     * 素材录入申请ID（仅素材录入类型有值）
     */
    private Long applicationId;

    /**
     * 申请标题（素材录入/删除）
     */
    private String applicationTitle;

    /**
     * 素材删除申请ID（仅素材删除类型有值）
     */
    private Long deletionApplicationId;

    /**
     * 删除申请标题（仅素材删除类型有值）
     */
    private String deletionTitle;

    /**
     * 素材使用申请ID（仅素材使用类型有值）
     */
    private Long usageApplicationId;

    /**
     * 使用申请标题（仅素材使用类型有值）
     */
    private String usageTitle;

    // ==================== 便利方法 ====================

    /**
     * 获取显示用的标题
     *
     * <p>根据业务类型返回对应的标题</p>
     *
     * @return 显示标题
     */
    public String getDisplayTitle() {
        if (applicationTitle != null) {
            return applicationTitle;
        }
        if (deletionTitle != null) {
            return deletionTitle;
        }
        if (usageTitle != null) {
            return usageTitle;
        }
        return null;
    }

    // ==================== Getter/Setter ====================

    /**
     * 获取工单ID
     *
     * @return 工单ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置工单ID
     *
     * @param id 工单ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取工单状态
     *
     * @return 工单状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置工单状态
     *
     * @param status 工单状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取业务类型
     *
     * @return 业务类型
     */
    public String getBusinessType() {
        return businessType;
    }

    /**
     * 设置业务类型
     *
     * @param businessType 业务类型
     */
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    /**
     * 获取业务ID
     *
     * @return 业务ID
     */
    public Long getBusinessId() {
        return businessId;
    }

    /**
     * 设置业务ID
     *
     * @param businessId 业务ID
     */
    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取申请人ID
     *
     * @return 申请人ID
     */
    public Long getApplicantId() {
        return applicantId;
    }

    /**
     * 设置申请人ID
     *
     * @param applicantId 申请人ID
     */
    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    /**
     * 获取工作流ID
     *
     * @return 工作流ID
     */
    public Long getWorkflowId() {
        return workflowId;
    }

    /**
     * 设置工作流ID
     *
     * @param workflowId 工作流ID
     */
    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * 获取工作流名称
     *
     * @return 工作流名称
     */
    public String getWorkflowName() {
        return workflowName;
    }

    /**
     * 设置工作流名称
     *
     * @param workflowName 工作流名称
     */
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    /**
     * 获取申请人姓名
     *
     * @return 申请人姓名
     */
    public String getApplicantName() {
        return applicantName;
    }

    /**
     * 设置申请人姓名
     *
     * @param applicantName 申请人姓名
     */
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    /**
     * 获取我的角色
     *
     * @return 我的角色
     */
    public String getMyRole() {
        return myRole;
    }

    /**
     * 设置我的角色
     *
     * @param myRole 我的角色
     */
    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    /**
     * 获取素材录入申请ID
     *
     * @return 素材录入申请ID
     */
    public Long getApplicationId() {
        return applicationId;
    }

    /**
     * 设置素材录入申请ID
     *
     * @param applicationId 素材录入申请ID
     */
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * 获取申请标题
     *
     * @return 申请标题
     */
    public String getApplicationTitle() {
        return applicationTitle;
    }

    /**
     * 设置申请标题
     *
     * @param applicationTitle 申请标题
     */
    public void setApplicationTitle(String applicationTitle) {
        this.applicationTitle = applicationTitle;
    }

    /**
     * 获取素材删除申请ID
     *
     * @return 素材删除申请ID
     */
    public Long getDeletionApplicationId() {
        return deletionApplicationId;
    }

    /**
     * 设置素材删除申请ID
     *
     * @param deletionApplicationId 素材删除申请ID
     */
    public void setDeletionApplicationId(Long deletionApplicationId) {
        this.deletionApplicationId = deletionApplicationId;
    }

    /**
     * 获取删除申请标题
     *
     * @return 删除申请标题
     */
    public String getDeletionTitle() {
        return deletionTitle;
    }

    /**
     * 设置删除申请标题
     *
     * @param deletionTitle 删除申请标题
     */
    public void setDeletionTitle(String deletionTitle) {
        this.deletionTitle = deletionTitle;
    }

    /**
     * 获取素材使用申请ID
     *
     * @return 素材使用申请ID
     */
    public Long getUsageApplicationId() {
        return usageApplicationId;
    }

    /**
     * 设置素材使用申请ID
     *
     * @param usageApplicationId 素材使用申请ID
     */
    public void setUsageApplicationId(Long usageApplicationId) {
        this.usageApplicationId = usageApplicationId;
    }

    /**
     * 获取使用申请标题
     *
     * @return 使用申请标题
     */
    public String getUsageTitle() {
        return usageTitle;
    }

    /**
     * 设置使用申请标题
     *
     * @param usageTitle 使用申请标题
     */
    public void setUsageTitle(String usageTitle) {
        this.usageTitle = usageTitle;
    }
}
