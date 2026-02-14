package com.xuanjiao.client.menu;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 删除菜单命令对象
 *
 * <p>用于删除指定的菜单，删除前会检查是否存在子菜单。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MenuDeleteCmd {

    /**
     * 菜单ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;
}
