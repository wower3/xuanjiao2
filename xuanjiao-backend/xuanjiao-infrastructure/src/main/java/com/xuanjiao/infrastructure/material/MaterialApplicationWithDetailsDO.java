package com.xuanjiao.infrastructure.material;

import java.time.LocalDateTime;

/**
 * 素材申请详情数据对象
 *
 * <p>用于接收素材申请JOIN查询结果，包含申请信息及申请人、维护人、部门名称。</p>
 * <p>这是 infrastructure 层专门用于接收多表 JOIN 查询结果的数据对象，不要与 domain 层实体混淆。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class MaterialApplicationWithDetailsDO {

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
     * 申请状态
     */
    private String status;

    /**
     * 是否签署版权保证声明
     */
    private Integer guaranteeDeclaration;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 素材数量
     */
    private Integer assetCount;

    // Getter and Setter methods

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

    public Integer getAssetCount() {
        return assetCount;
    }

    public void setAssetCount(Integer assetCount) {
        this.assetCount = assetCount;
    }
}
