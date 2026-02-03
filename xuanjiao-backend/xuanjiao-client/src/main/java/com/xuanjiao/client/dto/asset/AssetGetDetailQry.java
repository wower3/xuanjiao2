package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取素材详情查询对象
 */
@Data
public class AssetGetDetailQry {

    @NotNull(message = "素材ID不能为空")
    private Long id;
}
