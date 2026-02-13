package com.xuanjiao.client.dto.menu;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 菜单命令对象
 *
 * <p>封装创建或更新菜单所需的参数信息。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MenuCmd {

    /**
     * 菜单ID（更新时必填）
     */
    private Long id;

    /**
     * 父菜单ID（顶级菜单为null）
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /**
     * 菜单类型（MENU-菜单、BUTTON-按钮）
     */
    @NotBlank(message = "菜单类型不能为空")
    private String type;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序号（同级菜单排序）
     */
    private Integer sort;

    /**
     * 菜单状态（1-启用、0-禁用）
     */
    private Integer status;
}
