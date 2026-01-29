package com.xuanjiao.client.dto;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 素材使用申请命令
 */
@Data
public class UsageApplyCmd {
    @NotBlank(message = "申请标题不能为空")
    private String title;

    // 素材配置列表（每个素材单独配置）
    @Valid
    private List<AssetUsageConfig> assetConfigs;

    /**
     * 素材使用配置
     */
    @Data
    public static class AssetUsageConfig {
        @NotNull(message = "素材ID不能为空")
        private Long assetId;

        private String usageDescription;        // 使用申请说明
        private String usagePublishChannel;      // 使用发布渠道
        private Integer usageIsSecondaryCreation; // 是否二次创作:0-否,1-是
        private String usageAttachmentPath;      // 使用附件路径
    }
}
