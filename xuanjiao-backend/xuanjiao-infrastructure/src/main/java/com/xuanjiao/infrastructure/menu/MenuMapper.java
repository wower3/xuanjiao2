package com.xuanjiao.infrastructure.menu;

import com.xuanjiao.infrastructure.dataobject.MenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单数据访问接口
 *
 * <p>定义菜单表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface MenuMapper {

    /**
     * 根据主键查询菜单
     *
     * @param id 菜单ID
     * @return 菜单数据对象
     */
    MenuDO selectById(@Param("id") Long id);

    /**
     * 条件查询菜单列表
     *
     * @param query 查询条件
     * @return 菜单数据对象列表
     */
    List<MenuDO> selectList(MenuQuery query);

    /**
     * 条件统计菜单数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(MenuQuery query);

    /**
     * 根据角色ID查询菜单ID列表
     *
     * <p>通过 sys_role_menu 中间表关联查询。</p>
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询菜单列表
     *
     * <p>通过 sys_role_menu 和 sys_user 表关联查询。</p>
     *
     * @param userId 用户ID
     * @return 菜单数据对象列表
     */
    List<MenuDO> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 插入菜单
     *
     * @param menuDO 菜单数据对象
     * @return 影响行数
     */
    int insert(MenuDO menuDO);

    /**
     * 根据主键更新菜单
     *
     * @param menuDO 菜单数据对象
     * @return 影响行数
     */
    int updateById(MenuDO menuDO);

    /**
     * 根据主键删除菜单（逻辑删除）
     *
     * @param id 菜单ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
