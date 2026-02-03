package com.xuanjiao.client.dto.usage;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取使用申请详情查询对象
 */
@Data
public class UsageApplyGetDetailQry {

    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
