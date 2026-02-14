package com.xuanjiao.client.usage;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * 更新使用申请命令对象
 *
 * <p>封装更新素材使用申请所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyUpdateCmd {

    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long id;

    /**
     * 申请标题
     */
    @NotBlank(message = "申请标题不能为空")
    private String title;

    /**
     * 素材配置列表
     */
    @Valid
    private List<com.xuanjiao.client.UsageApplyCmd.AssetUsageConfig> assetConfigs;
}
