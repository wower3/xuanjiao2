package com.xuanjiao.app.dept;

import com.xuanjiao.client.dto.DeptDTO;
import java.util.List;

/**
 * 部门服务接口
 * <p>提供部门的查询、管理等功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.dept.impl.DeptServiceImpl
 */
public interface DeptService {

    /**
     * 获取所有部门列表
     *
     * @return 部门DTO列表
     */
    List<DeptDTO> list();

    /**
     * 获取部门树形结构
     *
     * @return 部门树形列表
     */
    List<DeptDTO> getTree();

    /**
     * 根据ID获取部门
     *
     * @param id 部门ID
     * @return 部门DTO
     */
    DeptDTO getById(Long id);

    /**
     * 保存部门
     *
     * @param dto 部门DTO
     */
    void save(DeptDTO dto);

    /**
     * 更新部门
     *
     * @param dto 部门DTO
     */
    void update(DeptDTO dto);

    /**
     * 删除部门
     *
     * @param id 部门ID
     */
    void delete(Long id);

    /**
     * 生成部门编号
     *
     * @return 部门编号
     */
    String generateCode();
}
