package com.xuanjiao.client.dto.menu;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 获取菜单详情查询对象
 */
@Data
public class MenuGetDetailQry {

    @NotNull(message = "ID不能为空")
    private Long id;
}
