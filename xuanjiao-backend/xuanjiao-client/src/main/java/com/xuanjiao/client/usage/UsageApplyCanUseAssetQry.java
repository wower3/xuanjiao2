package com.xuanjiao.client.usage;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 检查是否有权限使用素材查询对象
 *
 * <p>用于检查当前用户是否有权限使用（下载）指定素材。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyCanUseAssetQry {

    /**
     * 素材ID
     */
    @NotNull(message = "素材ID不能为空")
    private Long assetId;
}
