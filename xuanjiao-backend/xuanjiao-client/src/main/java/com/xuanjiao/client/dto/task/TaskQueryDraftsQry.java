package com.xuanjiao.client.dto.task;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 查询草稿箱查询对象
 *
 * <p>用于查询当前用户保存的草稿列表，支持按草稿类型和标题筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class TaskQueryDraftsQry {

    /**
     * 当前页码（从1开始，默认为1）
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页记录数（默认为10）
     */
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;

    /**
     * 草稿类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String draftType;

    /**
     * 标题（模糊查询）
     */
    private String title;
}
