package com.xuanjiao.infrastructure.user;

import com.xuanjiao.infrastructure.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 *
 * <p>定义用户表的数据库操作方法，对应 XML Mapper 实现。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Mapper
public interface UserMapper {

    /**
     * 根据主键查询用户
     *
     * @param id 用户ID
     * @return 用户数据对象
     */
    UserDO selectById(@Param("id") Long id);

    /**
     * 根据用户名查询单个用户
     *
     * @param username 用户名
     * @return 用户数据对象
     */
    UserDO selectOneByUsername(@Param("username") String username);

    /**
     * 条件查询用户列表
     *
     * <p>使用 UserQuery 对象封装查询条件，支持动态条件组合。</p>
     *
     * @param query 查询条件
     * @return 用户数据对象列表
     */
    List<UserDO> selectList(UserQuery query);

    /**
     * 插入用户
     *
     * @param user 用户数据对象
     * @return 影响行数
     */
    int insert(UserDO user);

    /**
     * 根据主键更新用户
     *
     * @param user 用户数据对象
     * @return 影响行数
     */
    int updateById(UserDO user);

    /**
     * 根据主键删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据角色ID查询用户ID列表
     *
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 条件查询用户列表（带部门/角色名称，JOIN查询避免N+1问题）
     *
     * <p>一次性获取用户及其部门、角色信息。</p>
     *
     * @param query 查询条件
     * @return 用户详情列表（含部门名称、角色名称）
     */
    List<UserWithDetailsDO> selectListWithDetails(UserQuery query);
}
