package com.xuanjiao.client.dto.approval;

import java.time.LocalDateTime;

/**
 * 流经事项DTO
 * 用于显示用户参与过的所有工单（作为发起人或审批人）
 */
public class FlowItemDTO {

    /**
     * 工单ID（审批实例ID）
     */
    private Long id;

    /**
     * 工单状态
     */
    private String status;

    /**
     * 业务类型：MATERIAL_ENTRY(素材录入), ASSET_USAGE(素材使用), ASSET_DELETION(素材删除)
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
     * 我的角色：initiator(发起人), approver(审批人)
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
     * 获取显示用的标题（根据业务类型返回对应的标题）
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

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationTitle() {
        return applicationTitle;
    }

    public void setApplicationTitle(String applicationTitle) {
        this.applicationTitle = applicationTitle;
    }

    public Long getDeletionApplicationId() {
        return deletionApplicationId;
    }

    public void setDeletionApplicationId(Long deletionApplicationId) {
        this.deletionApplicationId = deletionApplicationId;
    }

    public String getDeletionTitle() {
        return deletionTitle;
    }

    public void setDeletionTitle(String deletionTitle) {
        this.deletionTitle = deletionTitle;
    }

    public Long getUsageApplicationId() {
        return usageApplicationId;
    }

    public void setUsageApplicationId(Long usageApplicationId) {
        this.usageApplicationId = usageApplicationId;
    }

    public String getUsageTitle() {
        return usageTitle;
    }

    public void setUsageTitle(String usageTitle) {
        this.usageTitle = usageTitle;
    }
}
