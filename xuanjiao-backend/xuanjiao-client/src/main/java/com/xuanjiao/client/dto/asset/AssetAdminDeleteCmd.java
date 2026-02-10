package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理员彻底删除素材命令对象
 *
 * <p>仅管理员可用，用于彻底删除素材（软删除），
 * 需要提供删除原因。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetAdminDeleteCmd {

    /**
     * 素材ID
     */
    @NotNull(message = "素材ID不能为空")
    private Long id;

    /**
     * 删除原因
     */
    @NotBlank(message = "删除原因不能为空")
    private String reason;
}
