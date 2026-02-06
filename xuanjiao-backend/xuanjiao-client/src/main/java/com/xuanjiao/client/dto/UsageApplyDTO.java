package com.xuanjiao.client.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UsageApplyDTO {
    private Long id;

    private Long userId;
    private String username;
    private String deptName;      // 部门名称
    private String purpose;       // 申请说明
    private String scope;         // 使用范围
    private Long workflowId;
    private String workflowName;
    private String status;
    private Long approvalInstanceId;

    // 新增字段
    private String attachmentPath;       // 附件路径
    private Integer isSecondaryCreation; // 是否二次创作
    private String publishChannel;       // 发布渠道
    private Long deptId;                 // 申请部门ID
    private Integer draft;               // 是否草稿
    private String title;                // 申请单标题

    // 关联的素材列表（包含每个素材的使用配置）
    private List<AssetUsageConfigDTO> assets;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 素材使用配置DTO
     */
    @Data
    public static class AssetUsageConfigDTO {
        private Long assetId;
        private String assetName;
        private String assetType;
        private String assetFilePath;
        private String assetThumbnailPath;
        private String assetStatus;

        // 使用配置信息
        private String usageDescription;
        private String usagePublishChannel;
        private Integer usageIsSecondaryCreation;
        private String usageAttachmentPath;
    }
}
