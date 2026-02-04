package com.xuanjiao.infrastructure.dept;

import com.xuanjiao.infrastructure.dataobject.DeptDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门 Mapper
 * 重构为 XML Mapper 方式，移除 BaseMapper 继承
 */
@Mapper
public interface DeptMapper {

    /**
     * 根据主键查询
     */
    DeptDO selectById(@Param("id") Long id);

    /**
     * 根据编码查询
     */
    DeptDO selectByCode(@Param("code") String code);

    /**
     * 根据父部门ID查询子部门
     */
    List<DeptDO> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有部门（按级别和排序）
     */
    List<DeptDO> selectAll();

    /**
     * 动态条件查询
     */
    List<DeptDO> selectList(DeptQuery query);

    /**
     * 插入
     */
    int insert(DeptDO dept);

    /**
     * 更新
     */
    int updateById(DeptDO dept);

    /**
     * 删除（逻辑删除）
     */
    int deleteById(@Param("id") Long id);
}

