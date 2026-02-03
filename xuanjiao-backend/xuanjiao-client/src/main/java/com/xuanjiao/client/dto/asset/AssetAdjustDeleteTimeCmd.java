package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理员调整素材删除时间（测试功能）命令对象
 */
@Data
public class AssetAdjustDeleteTimeCmd {

    @NotNull(message = "素材ID不能为空")
    private Long id;
}
