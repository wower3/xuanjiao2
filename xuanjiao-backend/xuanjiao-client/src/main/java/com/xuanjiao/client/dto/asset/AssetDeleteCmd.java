package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除素材命令对象
 *
 * <p>用于发起素材删除申请，将素材标记为待删除状态。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetDeleteCmd {

    /**
     * 素材ID
     */
    @NotNull(message = "素材ID不能为空")
    private Long id;
}
