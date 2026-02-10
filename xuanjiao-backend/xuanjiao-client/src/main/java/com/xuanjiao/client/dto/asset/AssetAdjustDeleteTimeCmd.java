package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理员调整素材删除时间命令对象
 *
 * <p>仅管理员可用，用于测试定时清理功能。
 * 将素材的删除审批时间调整为7天前，以便测试定时任务。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetAdjustDeleteTimeCmd {

    /**
     * 素材ID
     */
    @NotNull(message = "素材ID不能为空")
    private Long id;
}
