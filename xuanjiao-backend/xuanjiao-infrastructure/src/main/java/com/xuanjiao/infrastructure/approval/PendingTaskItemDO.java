package com.xuanjiao.infrastructure.approval;

import java.time.LocalDateTime;

/**
 * 待办任务数据对象
 *
 * <p>用于接收待办任务分页JOIN查询结果，包含任务、实例、工作流和业务申请信息。</p>
 * <p>这是 infrastructure 层专门用于接收多表 JOIN 查询结果的数据对象，不要与 domain 层实体混淆。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class PendingTaskItemDO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 任务创建时间
     */
    private LocalDateTime taskCreateTime;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 子流程审批人ID列表
     */
    private String subWorkflowApproverIds;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 素材录入申请ID
     */
    private Long materialApplicationId;

    /**
     * 素材录入申请标题
     */
    private String materialApplicationTitle;

    /**
     * 素材删除申请ID
     */
    private Long deletionApplicationId;

    /**
     * 素材删除申请标题
     */
    private String deletionApplicationTitle;

    /**
     * 素材使用申请ID
     */
    private Long usageApplyId;

    /**
     * 素材使用申请标题
     */
    private String usageApplyTitle;

    /**
     * 素材数量
     */
    private Integer assetCount;

    /**
     * 素材类型
     */
    private String assetType;

    // Getter and Setter methods

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public LocalDateTime getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(LocalDateTime taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public String getSubWorkflowApproverIds() {
        return subWorkflowApproverIds;
    }

    public void setSubWorkflowApproverIds(String subWorkflowApproverIds) {
        this.subWorkflowApproverIds = subWorkflowApproverIds;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
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

    public Long getMaterialApplicationId() {
        return materialApplicationId;
    }

    public void setMaterialApplicationId(Long materialApplicationId) {
        this.materialApplicationId = materialApplicationId;
    }

    public String getMaterialApplicationTitle() {
        return materialApplicationTitle;
    }

    public void setMaterialApplicationTitle(String materialApplicationTitle) {
        this.materialApplicationTitle = materialApplicationTitle;
    }

    public Long getDeletionApplicationId() {
        return deletionApplicationId;
    }

    public void setDeletionApplicationId(Long deletionApplicationId) {
        this.deletionApplicationId = deletionApplicationId;
    }

    public String getDeletionApplicationTitle() {
        return deletionApplicationTitle;
    }

    public void setDeletionApplicationTitle(String deletionApplicationTitle) {
        this.deletionApplicationTitle = deletionApplicationTitle;
    }

    public Long getUsageApplyId() {
        return usageApplyId;
    }

    public void setUsageApplyId(Long usageApplyId) {
        this.usageApplyId = usageApplyId;
    }

    public String getUsageApplyTitle() {
        return usageApplyTitle;
    }

    public void setUsageApplyTitle(String usageApplyTitle) {
        this.usageApplyTitle = usageApplyTitle;
    }

    public Integer getAssetCount() {
        return assetCount;
    }

    public void setAssetCount(Integer assetCount) {
        this.assetCount = assetCount;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }
}
