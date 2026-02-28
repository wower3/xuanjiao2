package com.xuanjiao.domain.user.repository;

import com.xuanjiao.domain.user.entity.User;

/**
 * 用户仓储接口
 *
 * <p>定义用户数据的持久化操作，包括用户的查询、保存和更新。</p>
 * <p>用户是系统的基础实体，与角色和部门关联，用于权限控制。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface UserRepository {

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户实体，如果不存在返回 null
     */
    User findByUsername(String username);

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户实体，如果不存在返回 null
     */
    User findById(Long id);

    /**
     * 保存用户
     *
     * <p>将新用户持久化到数据库。</p>
     *
     * @param user 用户实体
     */
    void save(User user);

    /**
     * 更新用户
     *
     * <p>更新已存在的用户信息。</p>
     *
     * @param user 用户实体
     */
    void update(User user);
}
