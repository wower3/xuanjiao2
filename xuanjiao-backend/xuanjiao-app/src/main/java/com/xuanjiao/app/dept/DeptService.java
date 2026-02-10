package com.xuanjiao.app.dept;

import com.xuanjiao.client.dto.DeptDTO;
import java.util.List;

/**
 * 部门服务接口
 *
 * <p>提供部门的查询、管理等功能。部门采用树形结构组织，
 * 支持无限层级嵌套。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>部门CRUD操作</li>
 *   <li>部门树形结构查询</li>
 *   <li>部门编号自动生成</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.dept.impl.DeptServiceImpl
 */
public interface DeptService {

    /**
     * 获取所有部门列表
     *
     * <p>返回系统中所有部门的平铺列表。</p>
     *
     * @return 部门DTO列表
     */
    List<DeptDTO> list();

    /**
     * 获取部门树形结构
     *
     * <p>返回部门树形结构，以顶级部门为根节点，包含所有子部门的嵌套结构。</p>
     *
     * @return 部门树形列表，每个节点的children字段包含子部门
     */
    List<DeptDTO> getTree();

    /**
     * 根据ID获取部门
     *
     * <p>返回指定部门的详细信息。</p>
     *
     * @param id 部门ID
     * @return 部门DTO，不存在返回null
     */
    DeptDTO getById(Long id);

    /**
     * 保存部门
     *
     * <p>创建新部门。需要指定父部门ID（顶级部门传null或0）。
     * 部门编号可自动生成。</p>
     *
     * @param dto 部门DTO，包含名称、父ID、负责人、电话等信息
     */
    void save(DeptDTO dto);

    /**
     * 更新部门
     *
     * <p>更新已有部门的信息。可更新名称、负责人、电话、状态等。</p>
     *
     * @param dto 部门DTO
     * @throws RuntimeException 如果部门不存在
     */
    void update(DeptDTO dto);

    /**
     * 删除部门
     *
     * <p>删除指定部门。如果部门有子部门，需要先删除子部门。
     * 如果部门下有用户，也需要先处理用户。</p>
     *
     * @param id 部门ID
     * @throws RuntimeException 如果部门有子部门或关联用户
     */
    void delete(Long id);

    /**
     * 生成部门编号
     *
     * <p>生成唯一的部门编号，格式为DEPT+时间戳。</p>
     *
     * @return 部门编号字符串
     */
    String generateCode();
}
