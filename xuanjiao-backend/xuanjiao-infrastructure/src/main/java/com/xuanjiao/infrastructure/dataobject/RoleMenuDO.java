package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-菜单关联数据对象
 *
 * <p>映射数据库 sys_role_menu 表，用于 MyBatis 数据访问。</p>
 * <p>存储角色与菜单的多对多关联关系，用于权限控制。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
@TableName("sys_role_menu")
public class RoleMenuDO {

    /**
     * 关联ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID，关联 sys_role 表
     */
    private Long roleId;

    /**
     * 菜单ID，关联 sys_menu 表
     */
    private Long menuId;
}
