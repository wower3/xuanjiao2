package com.xuanjiao.client.dto.usage;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * 更新使用申请命令对象
 */
@Data
public class UsageApplyUpdateCmd {

    @NotNull(message = "申请单ID不能为空")
    private Long id;

    @NotBlank(message = "申请标题不能为空")
    private String title;

    @Valid
    private List<com.xuanjiao.client.dto.UsageApplyCmd.AssetUsageConfig> assetConfigs;
}
