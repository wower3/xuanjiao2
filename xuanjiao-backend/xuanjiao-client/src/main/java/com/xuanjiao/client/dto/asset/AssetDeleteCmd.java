package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除素材命令对象
 */
@Data
public class AssetDeleteCmd {

    @NotNull(message = "素材ID不能为空")
    private Long id;
}
