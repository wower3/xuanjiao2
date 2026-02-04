package com.xuanjiao.infrastructure.role;

import com.xuanjiao.infrastructure.dataobject.RoleMenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RoleMenu Mapper - 角色菜单关联表Mapper
 * 注意：此表为中间表，没有deleted字段，使用硬删除
 */
@Mapper
public interface RoleMenuMapper {

    /**
     * 根据查询条件查询角色菜单关联列表
     */
    List<RoleMenuDO> selectList(RoleMenuQuery query);

    /**
     * 根据查询条件统计数量
     */
    Long selectCount(RoleMenuQuery query);

    /**
     * 插入角色菜单关联
     */
    int insert(RoleMenuDO roleMenuDO);

    /**
     * 根据角色ID删除角色菜单关联（硬删除）
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据查询条件删除角色菜单关联（硬删除）
     */
    int delete(RoleMenuQuery query);
}
