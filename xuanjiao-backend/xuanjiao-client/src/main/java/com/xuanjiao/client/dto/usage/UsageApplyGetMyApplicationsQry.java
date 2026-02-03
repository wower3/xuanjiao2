package com.xuanjiao.client.dto.usage;

import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 获取我的使用申请查询对象
 */
@Data
public class UsageApplyGetMyApplicationsQry {

    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
