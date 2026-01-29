package com.xuanjiao.domain.deletion.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材删除申请实体
 */
public class AssetDeletionApplication {
    private Long id;
    private String title;
    private Long applicantId;
    private Long deptId;
    private Long workflowId;
    private String status; // DRAFT/PENDING/APPROVED/REJECTED
    private String deleteReason;
    private String attachmentPath;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AssetDeletionAsset> assets;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
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

    public String getDeleteReason() {
        return deleteReason;
    }

    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
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

    public List<AssetDeletionAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetDeletionAsset> assets) {
        this.assets = assets;
    }
}
