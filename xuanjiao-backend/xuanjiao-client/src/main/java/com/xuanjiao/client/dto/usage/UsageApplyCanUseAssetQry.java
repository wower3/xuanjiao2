package com.xuanjiao.client.dto.usage;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 检查是否有权限使用素材查询对象
 */
@Data
public class UsageApplyCanUseAssetQry {

    @NotNull(message = "素材ID不能为空")
    private Long assetId;
}
