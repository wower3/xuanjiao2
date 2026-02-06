package com.xuanjiao.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 角色-菜单关联数据对象
 * <p>对应数据库表 sys_role_menu，存储角色与菜单的关联关系</p>
 * <p>多对多关系中间表</p>
 *
 * @author system
 * @version 1.0
 */
@Data
@TableName("sys_role_menu")
public class RoleMenuDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID，关联sys_role表 */
    private Long roleId;

    /** 菜单ID，关联sys_menu表 */
    private Long menuId;
}
