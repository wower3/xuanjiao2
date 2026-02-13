package com.xuanjiao.client.dto.material.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材录入申请数据传输对象
 *
 * <p>用于在前后端之间传输素材录入申请信息，包括申请基本信息、
 * 申请人、所属部门、保障声明和关联的素材列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class MaterialApplicationDTO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 事项标题
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
     * 维护人ID
     */
    private Long maintainerId;

    /**
     * 维护人姓名
     */
    private String maintainerName;

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
     * 是否签署版权保证声明（0-否，1-是）
     */
    private Integer guaranteeDeclaration;

    /**
     * 关联的素材列表
     */
    private List<com.xuanjiao.client.dto.asset.dto.AssetDTO> assets;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public Long getMaintainerId() {
        return maintainerId;
    }

    public void setMaintainerId(Long maintainerId) {
        this.maintainerId = maintainerId;
    }

    public String getMaintainerName() {
        return maintainerName;
    }

    public void setMaintainerName(String maintainerName) {
        this.maintainerName = maintainerName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
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

    public Integer getGuaranteeDeclaration() {
        return guaranteeDeclaration;
    }

    public void setGuaranteeDeclaration(Integer guaranteeDeclaration) {
        this.guaranteeDeclaration = guaranteeDeclaration;
    }

    public List<com.xuanjiao.client.dto.asset.dto.AssetDTO> getAssets() {
        return assets;
    }

    public void setAssets(List<com.xuanjiao.client.dto.asset.dto.AssetDTO> assets) {
        this.assets = assets;
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
}
