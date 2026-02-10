package com.xuanjiao.infrastructure.approval;

import java.time.LocalDateTime;

/**
 * 我发起的工单数据对象
 *
 * <p>用于优化"我发起的"列表查询，通过JOIN一次性获取所有需要的数据，
 * 避免N+1查询问题。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class MyAppliedDO {

    /**
     * 审批实例ID
     */
    private Long id;

    /**
     * 审批状态（PENDING/APPROVED/REJECTED等）
     */
    private String status;

    /**
     * 业务类型（MATERIAL_ENTRY/ASSET_USAGE/ASSET_DELETION）
     */
    private String businessType;

    /**
     * 业务ID（申请单ID）
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
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 当前阶段ID
     */
    private Long currentStageId;

    /**
     * 当前阶段名称
     */
    private String currentStageName;

    /**
     * 素材录入申请标题
     */
    private String applicationTitle;

    /**
     * 删除申请标题
     */
    private String deletionTitle;

    /**
     * 使用申请标题
     */
    private String usageTitle;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public Long getCurrentStageId() {
        return currentStageId;
    }

    public void setCurrentStageId(Long currentStageId) {
        this.currentStageId = currentStageId;
    }

    public String getCurrentStageName() {
        return currentStageName;
    }

    public void setCurrentStageName(String currentStageName) {
        this.currentStageName = currentStageName;
    }

    public String getApplicationTitle() {
        return applicationTitle;
    }

    public void setApplicationTitle(String applicationTitle) {
        this.applicationTitle = applicationTitle;
    }

    public String getDeletionTitle() {
        return deletionTitle;
    }

    public void setDeletionTitle(String deletionTitle) {
        this.deletionTitle = deletionTitle;
    }

    public String getUsageTitle() {
        return usageTitle;
    }

    public void setUsageTitle(String usageTitle) {
        this.usageTitle = usageTitle;
    }

    /**
     * 获取统一的业务标题
     *
     * <p>根据业务类型返回对应的申请单标题。</p>
     *
     * @return 业务标题
     */
    public String getTitle() {
        if ("MATERIAL_ENTRY".equals(businessType)) {
            return applicationTitle;
        }
        if ("ASSET_DELETION".equals(businessType)) {
            return deletionTitle;
        }
        if ("ASSET_USAGE".equals(businessType)) {
            return usageTitle;
        }
        return null;
    }
}
