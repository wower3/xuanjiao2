package com.xuanjiao.client.dto.usage;

import lombok.Data;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import java.util.List;

/**
 * 创建使用申请草稿命令对象
 */
@Data
public class UsageApplyCreateDraftCmd {

    @NotBlank(message = "申请标题不能为空")
    private String title;

    @Valid
    private List<com.xuanjiao.client.dto.UsageApplyCmd.AssetUsageConfig> assetConfigs;
}
