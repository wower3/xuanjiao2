package com.xuanjiao.client.dto.menu;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新菜单命令对象
 */
@Data
public class MenuUpdateCmd {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    private String path;

    private String component;

    private Integer sort;

    private Long parentId;

    private String icon;

    private Integer menuType;
}
