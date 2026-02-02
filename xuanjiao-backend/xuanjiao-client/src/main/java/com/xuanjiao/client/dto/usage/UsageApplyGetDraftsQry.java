package com.xuanjiao.client.dto.usage;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取使用申请草稿箱查询对象
 */
@Data
public class UsageApplyGetDraftsQry {

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
