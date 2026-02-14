package com.xuanjiao.client.asset;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 查询用户已录入的素材查询对象
 *
 * <p>用于查询当前用户已通过审批的素材列表（APPROVED状态），
 * 支持按名称和类型筛选。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class AssetGetMyApprovedQry {

    /**
     * 素材名称（模糊查询）
     */
    private String name;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String type;

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
}
