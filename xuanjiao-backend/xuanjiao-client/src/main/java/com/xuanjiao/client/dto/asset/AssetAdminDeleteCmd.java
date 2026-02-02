package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理员彻底删除素材命令对象
 */
@Data
public class AssetAdminDeleteCmd {

    @NotNull(message = "素材ID不能为空")
    private Long id;

    @NotBlank(message = "删除原因不能为空")
    private String reason;
}
