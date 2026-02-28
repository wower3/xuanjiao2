package com.xuanjiao.infrastructure.usage;

import java.time.LocalDateTime;

/**
 * 素材使用申请详情数据对象
 *
 * <p>用于接收素材使用申请JOIN查询结果，包含申请信息及申请人信息。</p>
 * <p>这是 infrastructure 层专门用于接收多表 JOIN 查询结果的数据对象，不要与 domain 层实体混淆。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class UsageApplyWithDetailsDO {

    private Long id;
    private Long userId;
    private String title;
    private String purpose;
    private String scope;
    private Long workflowId;
    private String status;
    private Long approvalInstanceId;
    private String attachmentPath;
    private Integer isSecondaryCreation;
    private String publishChannel;
    private Long deptId;
    private Integer draft;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;

    // 申请人姓名
    private String applicantName;

    // 部门名称
    private String deptName;

    // Getter and Setter methods

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getApprovalInstanceId() {
        return approvalInstanceId;
    }

    public void setApprovalInstanceId(Long approvalInstanceId) {
        this.approvalInstanceId = approvalInstanceId;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public Integer getIsSecondaryCreation() {
        return isSecondaryCreation;
    }

    public void setIsSecondaryCreation(Integer isSecondaryCreation) {
        this.isSecondaryCreation = isSecondaryCreation;
    }

    public String getPublishChannel() {
        return publishChannel;
    }

    public void setPublishChannel(String publishChannel) {
        this.publishChannel = publishChannel;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getDraft() {
        return draft;
    }

    public void setDraft(Integer draft) {
        this.draft = draft;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}
