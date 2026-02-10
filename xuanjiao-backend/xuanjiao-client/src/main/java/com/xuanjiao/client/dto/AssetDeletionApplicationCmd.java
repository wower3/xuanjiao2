package com.xuanjiao.client.dto;

import java.util.List;

/**
 * 素材删除申请命令
 *
 * <p>封装创建素材删除申请所需的参数信息，包括申请标题、
 * 删除原因、附件和待删除的素材列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public class AssetDeletionApplicationCmd {

    /**
     * 申请标题
     */
    private String title;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 删除原因
     */
    private String deleteReason;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 待删除的素材ID列表
     */
    private List<Long> assetIds;

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
     * 获取待删除的素材ID列表
     *
     * @return 素材ID列表
     */
    public List<Long> getAssetIds() {
        return assetIds;
    }

    /**
     * 设置待删除的素材ID列表
     *
     * @param assetIds 素材ID列表
     */
    public void setAssetIds(List<Long> assetIds) {
        this.assetIds = assetIds;
    }
}
