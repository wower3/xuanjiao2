package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 菜单数据对象
 *
 * <p>映射数据库 sys_menu 表，用于 MyBatis 数据访问。</p>
 * <p>存储系统菜单配置，支持多级菜单和按钮权限。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("sys_menu")
public class MenuDO {

    /**
     * 菜单ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单ID，顶级菜单为0
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String name;

    /**
     * 菜单类型：CATALOG-目录、MENU-菜单、BUTTON-按钮
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
     * 菜单状态：1-正常、0-停用
     */
    private Integer status;

    /**
     * 创建时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除、1-已删除
     */
    @TableLogic
    private Integer deleted;
}
