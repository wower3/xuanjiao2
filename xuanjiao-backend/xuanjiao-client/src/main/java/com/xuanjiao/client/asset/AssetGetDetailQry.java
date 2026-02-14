package com.xuanjiao.client.asset;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取素材详情查询对象
 *
 * <p>用于根据素材ID获取素材的详细信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetGetDetailQry {

    /**
     * 素材ID
     */
    @NotNull(message = "素材ID不能为空")
    private Long id;
}
