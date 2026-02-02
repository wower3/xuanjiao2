package com.xuanjiao.client.dto.deletion;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取删除申请详情查询对象
 */
@Data
public class DeletionGetDetailQry {

    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
