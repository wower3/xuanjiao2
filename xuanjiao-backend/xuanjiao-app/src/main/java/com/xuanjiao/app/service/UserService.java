package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.UserDTO;
import java.util.List;

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
}
