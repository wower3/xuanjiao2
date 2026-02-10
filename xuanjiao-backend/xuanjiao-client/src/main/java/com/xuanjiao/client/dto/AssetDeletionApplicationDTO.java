package com.xuanjiao.client.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材删除申请数据传输对象
 *
 * <p>用于在前后端之间传输素材删除申请信息，包括申请基本信息、
 * 申请人、所属部门和关联的素材列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class AssetDeletionApplicationDTO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 申请标题
     */
    private String title;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 所属部门ID
     */
    private Long deptId;

    /**
     * 所属部门名称
     */
    private String deptName;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 申请状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已驳回）
     */
    private String status;

    /**
     * 删除原因
     */
    private String deleteReason;

    /**
     * 附件路径
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
    private List<AssetDeletionAssetDTO> assets;

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
     * 获取所属部门ID
     *
     * @return 所属部门ID
     */
    public Long getDeptId() {
        return deptId;
    }

    /**
     * 设置所属部门ID
     *
     * @param deptId 所属部门ID
     */
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    /**
     * 获取所属部门名称
     *
     * @return 所属部门名称
     */
    public String getDeptName() {
        return deptName;
    }

    /**
     * 设置所属部门名称
     *
     * @param deptName 所属部门名称
     */
    public void setDeptName(String deptName) {
        this.deptName = deptName;
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
    public List<AssetDeletionAssetDTO> getAssets() {
        return assets;
    }

    /**
     * 设置关联的素材列表
     *
     * @param assets 素材列表
     */
    public void setAssets(List<AssetDeletionAssetDTO> assets) {
        this.assets = assets;
    }
}
