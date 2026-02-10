package com.xuanjiao.infrastructure.role;

import com.xuanjiao.infrastructure.dataobject.RoleMenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-菜单关联数据访问接口
 *
 * <p>定义角色菜单关联的数据库操作方法，对应 XML Mapper 实现。</p>
 * <p>注意：此表为中间表，没有 deleted 字段，使用硬删除。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface RoleMenuMapper {

    /**
     * 根据查询条件查询角色菜单关联列表
     *
     * @param query 查询条件
     * @return 角色菜单关联列表
     */
    List<RoleMenuDO> selectList(RoleMenuQuery query);

    /**
     * 根据查询条件统计数量
     *
     * @param query 查询条件
     * @return 数量
     */
    Long selectCount(RoleMenuQuery query);

    /**
     * 插入角色菜单关联
     *
     * @param roleMenuDO 角色菜单关联数据对象
     * @return 影响行数
     */
    int insert(RoleMenuDO roleMenuDO);

    /**
     * 根据角色ID删除角色菜单关联（硬删除）
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据查询条件删除角色菜单关联（硬删除）
     *
     * @param query 查询条件
     * @return 影响行数
     */
    int delete(RoleMenuQuery query);
}
