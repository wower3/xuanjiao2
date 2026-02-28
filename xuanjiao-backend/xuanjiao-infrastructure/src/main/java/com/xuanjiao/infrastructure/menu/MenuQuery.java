package com.xuanjiao.infrastructure.menu;

import lombok.Data;

/**
 * 菜单查询条件对象
 *
 * <p>用于动态构建菜单查询条件，对应 MenuMapper 使用。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class MenuQuery {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单类型
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
     */
    private Integer sort;

    /**
     * 菜单状态
     */
    private Integer status;

    /**
     * 删除标记
     */
    private Integer deleted;

    /**
     * 排序字段
     */
    private String orderByField;

    /**
     * 排序方向
     */
    private String orderByDirection;
}
