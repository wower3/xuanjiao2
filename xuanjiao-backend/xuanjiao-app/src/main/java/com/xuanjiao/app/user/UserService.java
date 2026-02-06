package com.xuanjiao.app.user;

import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.UserDTO;
import com.xuanjiao.client.dto.user.UserGetListWithFilterQry;
import com.xuanjiao.infrastructure.dataobject.RoleDO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {
    UserDTO getCurrentUser(Long userId);
    List<UserDTO> list();
    List<UserDTO> listByBranchDept(Long currentUserId);

    /**
     * 带筛选条件的用户列表查询
     * @param userId 当前用户ID（用于权限控制）
     * @param roleIds 角色ID列表（可选，null表示不筛选）
     * @param deptId 部门ID（可选，null表示不筛选）
     * @param includeSubDept 是否包含子部门
     * @return 用户列表
     */
    List<UserDTO> listWithFilter(Long userId, List<Long> roleIds, Long deptId, Boolean includeSubDept);

    UserDTO getById(Long id);
    void create(UserDTO userDTO);
    void update(UserDTO userDTO);
    void delete(Long id);

    /**
     * 获取用户所属的二级机构（level=2的部门ID）
     * @param userId 用户ID
     * @return 二级机构ID，如果找不到返回null
     */
    Long getSecondaryDeptId(Long userId);

    /**
     * 获取用户允许查询的部门ID集合
     * @param currentUser 当前用户
     * @param currentRole 当前用户角色
     * @return 可查询的部门ID集合
     */
    Set<Long> getAllowedDeptIds(UserDTO currentUser, RoleDO currentRole);

    /**
     * 搜索用户（支持角色/部门/姓名筛选，带分页）
     * @param userId 当前用户ID（用于权限控制）
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResult<Map<String, Object>> searchUsers(Long userId, UserGetListWithFilterQry qry);
}
