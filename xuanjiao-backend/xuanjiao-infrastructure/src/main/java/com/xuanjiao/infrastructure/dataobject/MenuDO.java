package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜单数据对象
 * <p>对应数据库表 sys_menu，存储菜单的持久化数据</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.menu.entity.Menu
 */
@Data
@TableName("sys_menu")
public class MenuDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单ID，顶级菜单为0 */
    private Long parentId;

    /** 菜单名称 */
    private String name;

    /** 菜单类型：CATALOG-目录、MENU-菜单、BUTTON-按钮 */
    private String type;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 排序序号 */
    private Integer sort;

    /** 菜单状态：1-正常、0-停用 */
    private Integer status;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间，自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标识 */
    @TableLogic
    private Integer deleted;
}
