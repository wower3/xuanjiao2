package com.xuanjiao.client.menu;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单数据传输对象
 *
 * <p>用于在前后端之间传输菜单信息，支持树形结构展示。
 * 包含菜单基本信息、路由配置和子菜单列表。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MenuDTO {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 父菜单ID（顶级菜单为null）
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单类型（MENU-菜单、BUTTON-按钮）
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
     * 排序号（同级菜单排序）
     */
    private Integer sort;

    /**
     * 菜单状态（1-启用、0-禁用）
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

    /**
     * 子菜单列表（用于构建菜单树）
     */
    private List<MenuDTO> children;
}
