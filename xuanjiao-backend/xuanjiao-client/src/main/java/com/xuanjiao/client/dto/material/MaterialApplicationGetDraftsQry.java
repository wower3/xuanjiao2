package com.xuanjiao.client.dto.material;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取素材申请单草稿箱查询对象
 *
 * <p>用于查询当前用户保存的素材录入草稿列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationGetDraftsQry {

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
