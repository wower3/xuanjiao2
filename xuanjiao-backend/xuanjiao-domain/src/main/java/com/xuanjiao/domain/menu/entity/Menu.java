package com.xuanjiao.domain.menu.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜单实体
 *
 * <p>代表系统中的菜单/权限点，用于前端页面权限控制。</p>
 * <p>支持多级菜单，通过parentId实现树形结构。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class Menu {

    /**
     * 菜单唯一标识
     *
     * <p>自增主键。</p>
     */
    private Long id;

    /**
     * 父菜单ID
     *
     * <p>顶级菜单为0。</p>
     */
    private Long parentId;

    /**
     * 菜单名称
     *
     * <p>显示名称。</p>
     */
    private String name;

    /**
     * 菜单类型
     *
     * <p>CATALOG-目录、MENU-菜单、BUTTON-按钮。</p>
     */
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
     * 排序序号
     *
     * <p>数值越小越靠前。</p>
     */
    private Integer sort;

    /**
     * 菜单状态
     *
     * <p>1-正常、0-停用。</p>
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
