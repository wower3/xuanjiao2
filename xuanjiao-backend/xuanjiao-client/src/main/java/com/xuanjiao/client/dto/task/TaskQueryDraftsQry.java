package com.xuanjiao.client.dto.task;

import com.xuanjiao.client.dto.common.BasePageQry;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询草稿箱查询对象
 *
 * <p>用于查询当前用户保存的草稿列表，支持按草稿类型和标题筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQueryDraftsQry extends BasePageQry {

    /**
     * 草稿类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String draftType;

    /**
     * 标题（模糊查询）
     */
    private String title;
}
