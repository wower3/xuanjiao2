package com.xuanjiao.domain.menu.repository;

import com.xuanjiao.domain.menu.entity.Menu;
import java.util.List;

/**
 * 菜单仓储接口
 *
 * <p>定义菜单数据的持久化操作，包括菜单的查询、保存、更新和删除。</p>
 * <p>菜单支持多级树形结构，用于前端页面权限控制。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface MenuRepository {

    /**
     * 根据ID查找菜单
     *
     * @param id 菜单ID
     * @return 菜单实体，如果不存在返回 null
     */
    Menu findById(Long id);

    /**
     * 查找所有菜单
     *
     * @return 所有菜单列表
     */
    List<Menu> findAll();

    /**
     * 根据用户ID查找该用户有权访问的菜单列表
     *
     * <p>根据用户角色关联查询用户可访问的菜单。</p>
     *
     * @param userId 用户ID
     * @return 用户可访问的菜单列表
     */
    List<Menu> findByUserId(Long userId);

    /**
     * 保存菜单
     *
     * @param menu 菜单实体
     */
    void save(Menu menu);

    /**
     * 更新菜单
     *
     * @param menu 菜单实体
     */
    void update(Menu menu);

    /**
     * 根据ID删除菜单
     *
     * @param id 菜单ID
     */
    void deleteById(Long id);
}
