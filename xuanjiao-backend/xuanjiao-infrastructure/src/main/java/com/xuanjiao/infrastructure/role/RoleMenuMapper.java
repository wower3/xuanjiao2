package com.xuanjiao.infrastructure.role;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.RoleMenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
