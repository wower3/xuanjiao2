package com.xuanjiao.infrastructure.dept;

import com.xuanjiao.infrastructure.dataobject.DeptDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门数据访问接口
 *
 * <p>定义部门表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface DeptMapper {

    /**
     * 根据主键查询部门
     *
     * @param id 部门ID
     * @return 部门数据对象
     */
    DeptDO selectById(@Param("id") Long id);

    /**
     * 根据编码查询部门
     *
     * @param code 部门编码
     * @return 部门数据对象
     */
    DeptDO selectByCode(@Param("code") String code);

    /**
     * 根据父部门ID查询子部门
     *
     * @param parentId 父部门ID
     * @return 部门数据对象列表
     */
    List<DeptDO> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有部门（按级别和排序）
     *
     * @return 部门数据对象列表
     */
    List<DeptDO> selectAll();

    /**
     * 动态条件查询部门列表
     *
     * @param query 查询条件
     * @return 部门数据对象列表
     */
    List<DeptDO> selectList(DeptQuery query);

    /**
     * 插入部门
     *
     * @param dept 部门数据对象
     * @return 影响行数
     */
    int insert(DeptDO dept);

    /**
     * 更新部门
     *
     * @param dept 部门数据对象
     * @return 影响行数
     */
    int updateById(DeptDO dept);

    /**
     * 删除部门（逻辑删除）
     *
     * @param id 部门ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
