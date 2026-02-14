package com.xuanjiao.client.menu;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 更新菜单命令对象
 *
 * <p>封装更新菜单所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MenuUpdateCmd {

    /**
     * 菜单ID
     */
    @NotNull(message = "ID不能为空")
    private Long id;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 菜单类型（1-菜单、2-按钮）
     */
    private Integer menuType;
}
