package com.xuanjiao.domain.deletion.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材删除申请实体
 *
 * <p>代表用户提交的删除素材申请，用于申请删除系统中的已有素材。</p>
 * <p>删除申请需要经过审批流程，审批通过后素材进入删除待清理状态。</p>
 * <p>支持一个申请包含多个素材（通过AssetDeletionAsset关联）。</p>
 * <p>审批通过后7天，素材会被软删除（设置deleted=1）。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class AssetDeletionApplication {

    /**
     * 申请唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 申请标题
     *
     * <p>简要说明删除原因。</p>
     */
    private String title;

    /**
     * 申请人ID
     *
     * <p>关联sys_user表。</p>
     */
    private Long applicantId;

    /**
     * 部门ID
     *
     * <p>关联sys_dept表。</p>
     */
    private Long deptId;

    /**
     * 关联的工作流定义ID
     *
     * <p>用于审批该申请。</p>
     */
    private Long workflowId;

    /**
     * 申请状态
     *
     * <p>DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝。</p>
     */
    private String status;

    /**
     * 删除原因
     *
     * <p>详细说明删除素材的原因。</p>
     */
    private String deleteReason;

    /**
     * 附件路径
     *
     * <p>相关证明文件的存储路径。</p>
     */
    private String attachmentPath;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 关联的素材列表
     */
    private List<AssetDeletionAsset> assets;

    /**
     * 获取申请ID
     *
     * @return 申请ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置申请ID
     *
     * @param id 申请ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取申请标题
     *
     * @return 申请标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置申请标题
     *
     * @param title 申请标题
     */
    public void setTitle(String title) {
        this.title = title;
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
     * 获取部门ID
     *
     * @return 部门ID
     */
    public Long getDeptId() {
        return deptId;
    }

    /**
     * 设置部门ID
     *
     * @param deptId 部门ID
     */
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
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
     * 获取申请状态
     *
     * @return 申请状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置申请状态
     *
     * @param status 申请状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取删除原因
     *
     * @return 删除原因
     */
    public String getDeleteReason() {
        return deleteReason;
    }

    /**
     * 设置删除原因
     *
     * @param deleteReason 删除原因
     */
    public void setDeleteReason(String deleteReason) {
        this.deleteReason = deleteReason;
    }

    /**
     * 获取附件路径
     *
     * @return 附件路径
     */
    public String getAttachmentPath() {
        return attachmentPath;
    }

    /**
     * 设置附件路径
     *
     * @param attachmentPath 附件路径
     */
    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
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
     * 获取更新时间
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取关联的素材列表
     *
     * @return 素材列表
     */
    public List<AssetDeletionAsset> getAssets() {
        return assets;
    }

    /**
     * 设置关联的素材列表
     *
     * @param assets 素材列表
     */
    public void setAssets(List<AssetDeletionAsset> assets) {
        this.assets = assets;
    }
}
