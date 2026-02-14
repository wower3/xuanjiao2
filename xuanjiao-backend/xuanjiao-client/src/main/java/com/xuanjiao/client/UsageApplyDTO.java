package com.xuanjiao.client;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 素材使用申请数据传输对象
 *
 * <p>用于在前后端之间传输素材使用申请信息，包括申请基本信息、
 * 关联的素材列表和每个素材的使用配置。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyDTO {

    /**
     * 申请ID
     */
    private Long id;

    /**
     * 申请人ID
     */
    private Long userId;

    /**
     * 申请人用户名
     */
    private String username;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 申请说明
     */
    private String purpose;

    /**
     * 使用范围
     */
    private String scope;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 申请状态（DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已驳回）
     */
    private String status;

    /**
     * 审批实例ID
     */
    private Long approvalInstanceId;

    /**
     * 附件路径
     */
    private String attachmentPath;

    /**
     * 是否二次创作（0-否，1-是）
     */
    private Integer isSecondaryCreation;

    /**
     * 发布渠道
     */
    private String publishChannel;

    /**
     * 申请部门ID
     */
    private Long deptId;

    /**
     * 是否草稿（0-否，1-是）
     */
    private Integer draft;

    /**
     * 申请单标题
     */
    private String title;

    /**
     * 关联的素材列表（包含每个素材的使用配置）
     */
    private List<AssetUsageConfigDTO> assets;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 素材使用配置数据传输对象
     *
     * <p>内部类，用于传输单个素材的使用配置信息</p>
     *
     * @author xuanjiao
     * @since 1.0.0
     */
    @Data
    public static class AssetUsageConfigDTO {

        /**
         * 素材ID
         */
        private Long assetId;

        /**
         * 素材名称
         */
        private String assetName;

        /**
         * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
         */
        private String assetType;

        /**
         * 素材文件路径
         */
        private String assetFilePath;

        /**
         * 素材缩略图路径
         */
        private String assetThumbnailPath;

        /**
         * 素材状态
         */
        private String assetStatus;

        /**
         * 使用说明
         */
        private String usageDescription;

        /**
         * 使用发布渠道
         */
        private String usagePublishChannel;

        /**
         * 是否二次创作（0-否，1-是）
         */
        private Integer usageIsSecondaryCreation;

        /**
         * 使用附件路径
         */
        private String usageAttachmentPath;
    }
}
