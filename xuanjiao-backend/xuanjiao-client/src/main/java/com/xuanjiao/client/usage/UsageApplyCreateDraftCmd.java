package com.xuanjiao.client.usage;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import java.util.List;

/**
 * 创建使用申请草稿命令对象
 *
 * <p>封装创建素材使用申请草稿所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyCreateDraftCmd {

    /**
     * 申请标题
     */
    @NotBlank(message = "申请标题不能为空")
    private String title;

    /**
     * 素材配置列表
     */
    @Valid
    private List<com.xuanjiao.client.usage.UsageApplyCmd.AssetUsageConfig> assetConfigs;
}
