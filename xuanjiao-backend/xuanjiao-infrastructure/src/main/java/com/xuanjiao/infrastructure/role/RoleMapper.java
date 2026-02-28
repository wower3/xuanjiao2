package com.xuanjiao.infrastructure.role;

import com.xuanjiao.infrastructure.dataobject.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据访问接口
 *
 * <p>定义角色表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据主键查询角色
     *
     * @param id 角色ID
     * @return 角色数据对象
     */
    RoleDO selectById(@Param("id") Long id);

    /**
     * 条件查询角色列表
     *
     * @param query 查询条件
     * @return 角色数据对象列表
     */
    List<RoleDO> selectList(RoleQuery query);

    /**
     * 查询角色数量（用于唯一性检查等）
     *
     * @param query 查询条件
     * @return 角色数量
     */
    Long selectCount(RoleQuery query);

    /**
     * 插入角色
     *
     * @param role 角色数据对象
     * @return 影响行数
     */
    int insert(RoleDO role);

    /**
     * 根据主键更新角色
     *
     * @param role 角色数据对象
     * @return 影响行数
     */
    int updateById(RoleDO role);

    /**
     * 根据主键删除角色（逻辑删除）
     *
     * @param id 角色ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}
