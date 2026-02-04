package com.xuanjiao.infrastructure.menu;

import com.xuanjiao.infrastructure.dataobject.MenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Menu Mapper Interface
 * Refactored from BaseMapper to XML Mapper approach
 */
@Mapper
public interface MenuMapper {

    /**
     * Select menu by ID
     */
    MenuDO selectById(@Param("id") Long id);

    /**
     * Select menus with dynamic query conditions
     */
    List<MenuDO> selectList(MenuQuery query);

    /**
     * Count menus with dynamic query conditions
     */
    Long selectCount(MenuQuery query);

    /**
     * Select menu IDs by role ID (from sys_role_menu intermediate table)
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * Select menus by user ID (with JOINs to sys_role_menu and sys_user)
     */
    List<MenuDO> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * Insert new menu
     */
    int insert(MenuDO menuDO);

    /**
     * Update menu by ID
     */
    int updateById(MenuDO menuDO);

    /**
     * Delete menu by ID (soft delete)
     */
    int deleteById(@Param("id") Long id);
}
