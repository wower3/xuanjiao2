package com.xuanjiao.client.dto.material;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取素材申请单详情查询对象
 */
@Data
public class MaterialApplicationGetDetailQry {

    @NotNull(message = "申请单ID不能为空")
    private Long id;
}
