package com.xuanjiao.infrastructure.role;

import com.xuanjiao.infrastructure.dataobject.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据主键查询角色
     */
    RoleDO selectById(@Param("id") Long id);

    /**
     * 条件查询角色列表
     */
    List<RoleDO> selectList(RoleQuery query);

    /**
     * 查询角色数量（用于唯一性检查等）
     */
    Long selectCount(RoleQuery query);

    /**
     * 插入角色
     */
    int insert(RoleDO role);

    /**
     * 根据主键更新角色
     */
    int updateById(RoleDO role);

    /**
     * 根据主键删除角色（逻辑删除）
     */
    int deleteById(@Param("id") Long id);
}
