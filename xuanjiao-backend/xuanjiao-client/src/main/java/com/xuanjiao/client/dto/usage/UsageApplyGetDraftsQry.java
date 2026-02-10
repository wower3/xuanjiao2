package com.xuanjiao.client.dto.usage;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取使用申请草稿箱查询对象
 *
 * <p>用于查询当前用户保存的素材使用草稿列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class UsageApplyGetDraftsQry {

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
