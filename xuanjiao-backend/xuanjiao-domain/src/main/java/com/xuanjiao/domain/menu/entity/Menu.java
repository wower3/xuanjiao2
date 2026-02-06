package com.xuanjiao.domain.menu.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜单实体
 * <p>代表系统中的菜单/权限点，用于前端页面权限控制</p>
 * <p>支持多级菜单，通过parentId实现树形结构</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.infrastructure.dataobject.MenuDO
 */
@Data
public class Menu {
    /** 菜单唯一标识，自增主键 */
    private Long id;

    /** 父菜单ID，顶级菜单为0 */
    private Long parentId;

    /** 菜单名称，显示名称 */
    private String name;

    /** 菜单类型：CATALOG-目录、MENU-菜单、BUTTON-按钮 */
    private String type;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 排序序号，数值越小越靠前 */
    private Integer sort;

    /** 菜单状态：1-正常、0-停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
