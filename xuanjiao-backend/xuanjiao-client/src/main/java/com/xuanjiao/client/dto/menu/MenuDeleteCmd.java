package com.xuanjiao.client.dto.menu;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除菜单命令对象
 */
@Data
public class MenuDeleteCmd {

    @NotNull(message = "ID不能为空")
    private Long id;
}
