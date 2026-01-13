package com.xuanjiao.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeptMapper extends BaseMapper<DeptDO> {

    @Select("SELECT * FROM sys_dept WHERE deleted = 0 ORDER BY level, sort")
    List<DeptDO> selectAll();

    @Select("SELECT * FROM sys_dept WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort")
    List<DeptDO> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM sys_dept WHERE code = #{code} AND deleted = 0 LIMIT 1")
    DeptDO selectByCode(@Param("code") String code);
}

