package com.xuanjiao.infrastructure.user;

import com.xuanjiao.infrastructure.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper
 * 重构说明：不再继承BaseMapper，所有SQL在UserMapper.xml中定义
 */
@Mapper
public interface UserMapper {

    /**
     * 根据主键查询用户
     */
    UserDO selectById(@Param("id") Long id);

    /**
     * 根据用户名查询单个用户
     */
    UserDO selectOneByUsername(@Param("username") String username);

    /**
     * 条件查询用户列表
     * 使用UserQuery对象封装查询条件，支持动态条件组合
     */
    List<UserDO> selectList(UserQuery query);

    /**
     * 插入用户
     */
    int insert(UserDO user);

    /**
     * 根据主键更新用户
     */
    int updateById(UserDO user);

    /**
     * 根据主键删除用户（逻辑删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据角色ID查询用户ID列表
     */
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}
