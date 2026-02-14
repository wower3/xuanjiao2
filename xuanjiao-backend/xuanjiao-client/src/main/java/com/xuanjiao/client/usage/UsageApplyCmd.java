package com.xuanjiao.client.usage;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 素材使用申请命令
 *
 * <p>封装创建素材使用申请所需的参数信息，支持多素材批量申请，
 * 每个素材可单独配置使用说明、发布渠道等信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyCmd {

    /**
     * 申请标题
     */
    @NotBlank(message = "申请标题不能为空")
    private String title;

    /**
     * 素材配置列表（每个素材单独配置）
     */
    @Valid
    private List<AssetUsageConfig> assetConfigs;

    /**
     * 素材使用配置
     *
     * <p>内部类，用于配置单个素材的使用信息</p>
     *
     * @author xuanjiao
     * @since 1.0.0
     */
    @Data
    public static class AssetUsageConfig {

        /**
         * 素材ID
         */
        @NotNull(message = "素材ID不能为空")
        private Long assetId;

        /**
         * 使用申请说明
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
