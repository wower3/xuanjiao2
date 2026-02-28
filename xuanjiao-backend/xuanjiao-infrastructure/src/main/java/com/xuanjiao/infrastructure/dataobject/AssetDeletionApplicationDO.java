package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 素材删除申请数据对象
 *
 * <p>映射数据库 asset_deletion_application 表，用于 MyBatis 数据访问。</p>
 * <p>存储素材删除申请信息，包括删除原因、关联素材等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@TableName("asset_deletion_application")
public class AssetDeletionApplicationDO {

    /**
     * 申请ID（主键）
     */
    @TableId(type = IdType.AUTO)
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
     * 部门ID
     */
    private Long deptId;

    /**
     * 关联的工作流定义ID
     */
    private Long workflowId;

    /**
     * 申请状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝
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
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除、1-已删除
     */
    @TableLogic
    private Integer deleted;

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
     * 获取工作流定义ID
     *
     * @return 工作流定义ID
     */
    public Long getWorkflowId() {
        return workflowId;
    }

    /**
     * 设置工作流定义ID
     *
     * @param workflowId 工作流定义ID
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
     * 获取逻辑删除标识
     *
     * @return 逻辑删除标识
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 设置逻辑删除标识
     *
     * @param deleted 逻辑删除标识
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
