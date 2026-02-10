package com.xuanjiao.domain.dept.repository;

import com.xuanjiao.domain.dept.entity.Dept;
import java.util.List;

/**
 * 部门仓储接口
 *
 * <p>定义部门数据的持久化操作，包括部门的查询、保存、更新和删除。</p>
 * <p>部门支持多级树形结构，通过parentId关联父部门。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface DeptRepository {

    /**
     * 根据ID查找部门
     *
     * @param id 部门ID
     * @return 部门实体，如果不存在返回 null
     */
    Dept findById(Long id);

    /**
     * 根据父部门ID查找子部门列表
     *
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<Dept> findByParentId(Long parentId);

    /**
     * 查找所有部门
     *
     * @return 所有部门列表
     */
    List<Dept> findAll();

    /**
     * 保存部门
     *
     * @param dept 部门实体
     */
    void save(Dept dept);

    /**
     * 更新部门
     *
     * @param dept 部门实体
     */
    void update(Dept dept);

    /**
     * 根据ID删除部门
     *
     * @param id 部门ID
     */
    void deleteById(Long id);
}
