package com.xuanjiao.client.dto.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 草稿箱列表项数据传输对象
 *
 * <p>用于在草稿箱中展示不同类型的草稿申请，
 * 包括素材录入、素材使用、素材删除三种类型。</p>
 *
 * <p>使用组合设计模式，通过 type 字段区分不同类型的草稿。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DraftItemDTO {

    /**
     * 草稿类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String type;

    /**
     * 草稿ID
     */
    private Long id;

    /**
     * 草稿标题
     */
    private String title;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 素材录入申请数据（仅当 type = MATERIAL_ENTRY 时有值）
     */
    private MaterialEntryDraftData materialEntry;

    /**
     * 素材使用申请数据（仅当 type = ASSET_USAGE 时有值）
     */
    private UsageDraftData assetUsage;

    /**
     * 素材删除申请数据（仅当 type = ASSET_DELETION 时有值）
     */
    private DeletionDraftData assetDeletion;

    /**
     * 素材录入草稿数据
     */
    @Data
    public static class MaterialEntryDraftData {
        private Long applicantId;
        private String applicantName;
        private Long maintainerId;
        private String maintainerName;
        private Long deptId;
        private String deptName;
        private Long workflowId;
        private String status;
        private Integer guaranteeDeclaration;
        private Object assets; // 使用 Object 避免 Jackson 循环依赖，实际是 List<AssetDTO>
    }

    /**
     * 素材使用草稿数据
     */
    @Data
    public static class UsageDraftData {
        private Long userId;
        private String username;
        private Long deptId;
        private String deptName;
        private Long workflowId;
        private String status;
        private String attachmentPath;
        private Integer isSecondaryCreation;
        private String publishChannel;
        private Integer draft;
        private Object assets; // 使用 Object 避免 Jackson 循环依赖，实际是 List<AssetUsageConfigDTO>
    }

    /**
     * 素材删除草稿数据
     */
    @Data
    public static class DeletionDraftData {
        private Long applicantId;
        private String applicantName;
        private Long deptId;
        private String deptName;
        private Long workflowId;
        private String status;
        private String deleteReason;
        private String attachmentPath;
        private Object assets; // 使用 Object 避免 Jackson 循环依赖，实际是 List<AssetDeletionAssetDTO>
    }
}
