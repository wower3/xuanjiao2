package com.xuanjiao.client.dto.asset;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 查询用户已录入的素材（APPROVED状态）查询对象
 */
@Data
public class AssetGetMyApprovedQry {
    private String name;
    private String type;

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
