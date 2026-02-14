package com.xuanjiao.client.material;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取我的素材申请单查询对象
 *
 * <p>用于查询当前用户发起的素材录入申请列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MaterialApplicationGetMyApplicationsQry {

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
