package com.xuanjiao.client.task;

import lombok.Data;

/**
 * 草稿项目数据传输对象
 *
 * <p>用于返回草稿箱中的草稿项目，包含业务类型和业务数据。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class DraftItemDTO {

    /**
     * 业务类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String type;

    /**
     * 业务数据（根据 type 使用具体 DTO 类型）
     * - MATERIAL_ENTRY: MaterialApplicationDTO
     * - ASSET_USAGE: UsageApplyDTO
     * - ASSET_DELETION: AssetDeletionApplicationDTO
     */
    private Object data;
}
