package com.xuanjiao.domain.deletion.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材删除申请实体
 * <p>代表用户提交的删除素材申请，用于申请删除系统中的已有素材</p>
 * <p>删除申请需要经过审批流程，审批通过后素材进入删除待清理状态</p>
 * <p>支持一个申请包含多个素材（通过AssetDeletionAsset关联）</p>
 * <p>审批通过后7天，素材会被软删除（设置deleted=1）</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO
 */
public class AssetDeletionApplication {
    /** 申请唯一标识，自增主键 */
    private Long id;

    /** 申请标题，简要说明删除原因 */
    private String title;

    /** 申请人ID，关联sys_user表 */
    private Long applicantId;

    /** 部门ID，关联sys_dept表 */
    private Long deptId;

    /** 关联的工作流定义ID，用于审批该申请 */
    private Long workflowId;

    /** 申请状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝 */
    private String status;

    /** 删除原因，详细说明删除素材的原因 */
    private String deleteReason;

    /** 附件路径，相关证明文件的存储路径 */
    private String attachmentPath;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 关联的素材列表 */
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
