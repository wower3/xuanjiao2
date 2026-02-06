package com.xuanjiao.app.user;

import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.UserDTO;
import com.xuanjiao.client.dto.user.UserGetListWithFilterQry;
import com.xuanjiao.infrastructure.dataobject.RoleDO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户服务接口
 * <p>提供用户的查询、管理等功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.user.impl.UserServiceImpl
 */
public interface UserService {

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID
     * @return 用户DTO
     */
    UserDTO getCurrentUser(Long userId);

    /**
     * 获取所有用户列表
     *
     * @return 用户DTO列表
     */
    List<UserDTO> list();

    /**
     * 获取当前用户所在分部的用户列表
     *
     * @param currentUserId 当前用户ID
     * @return 用户DTO列表
     */
    List<UserDTO> listByBranchDept(Long currentUserId);

    /**
     * 带筛选条件的用户列表查询
     *
     * @param userId 当前用户ID（用于权限控制）
     * @param roleIds 角色ID列表（可选，null表示不筛选）
     * @param deptId 部门ID（可选，null表示不筛选）
     * @param includeSubDept 是否包含子部门
     * @return 用户列表
     */
    List<UserDTO> listWithFilter(Long userId, List<Long> roleIds, Long deptId, Boolean includeSubDept);

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户DTO
     */
    UserDTO getById(Long id);

    /**
     * 创建用户
     *
     * @param userDTO 用户DTO
     */
    void create(UserDTO userDTO);

    /**
     * 更新用户
     *
     * @param userDTO 用户DTO
     */
    void update(UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void delete(Long id);

    /**
     * 获取用户所属的二级机构
     * <p>二级机构是指level=2的部门</p>
     *
     * @param userId 用户ID
     * @return 二级机构ID，如果找不到返回null
     */
    Long getSecondaryDeptId(Long userId);

    /**
     * 获取用户允许查询的部门ID集合
     *
     * @param currentUser 当前用户
     * @param currentRole 当前用户角色
     * @return 可查询的部门ID集合
     */
    Set<Long> getAllowedDeptIds(UserDTO currentUser, RoleDO currentRole);

    /**
     * 搜索用户
     * <p>支持角色/部门/姓名筛选，带分页</p>
     *
     * @param userId 当前用户ID（用于权限控制）
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResult<Map<String, Object>> searchUsers(Long userId, UserGetListWithFilterQry qry);
}
