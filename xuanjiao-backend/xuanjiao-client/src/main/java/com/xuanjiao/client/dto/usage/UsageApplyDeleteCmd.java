package com.xuanjiao.client.dto.usage;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除使用申请命令对象
 */
@Data
public class UsageApplyDeleteCmd {

    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
