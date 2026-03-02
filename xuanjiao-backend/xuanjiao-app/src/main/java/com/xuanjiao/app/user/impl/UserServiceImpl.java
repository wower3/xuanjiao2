package com.xuanjiao.app.user.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjiao.app.user.UserService;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.user.UserDTO;
import com.xuanjiao.client.user.UserGetListWithFilterQry;
import com.xuanjiao.common.ConvertUtils;
import com.xuanjiao.common.exception.NotFoundException;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.dept.DeptQuery;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

 /**
 * 用户服务实现类
 * <p>实现UserService接口，封装用户业务逻辑</p>
 * <p>核心功能：用户CRUD、权限过滤、用户搜索</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.user.UserService
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private DeptMapper deptMapper;

    @Override
    public UserDTO getCurrentUser(Long userId) {
        return getById(userId);
    }

    @Override
    public List<UserDTO> list() {
        List<UserDO> list = userMapper.selectList(new UserQuery());
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> listByBranchDept(Long currentUserId) {
        // 获取当前用户所属的二级机构，返回该机构及其子部门的用户
        Long secondaryDeptId = getSecondaryDeptId(currentUserId);
        if (secondaryDeptId == null) {
            return list();
        }

        Set<Long> deptIds = getAllSubDeptIds(secondaryDeptId);
        deptIds.add(secondaryDeptId);

        List<UserDO> allUsers = userMapper.selectList(new UserQuery());
        return allUsers.stream()
                .filter(user -> user.getDeptId() != null && deptIds.contains(user.getDeptId()))
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> listWithFilter(Long userId, List<Long> roleIds, Long deptId, Boolean includeSubDept) {
        // 获取当前用户信息和角色
        UserDTO currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return list();
        }

        RoleDO currentRole = null;
        if (currentUser.getRoleId() != null) {
            currentRole = roleMapper.selectById(currentUser.getRoleId());
        }

        // 确定可查询的部门范围
        Set<Long> allowedDeptIds = getAllowedDeptIds(currentUser, currentRole);

        // 计算最终的筛选部门集合（使用final变量供lambda使用）
        final Set<Long> filterDeptIds;
        if (deptId != null) {
            if (Boolean.TRUE.equals(includeSubDept)) {
                Set<Long> subDeptIds = getAllSubDeptIds(deptId);
                subDeptIds.add(deptId);
                filterDeptIds = allowedDeptIds.stream()
                        .filter(subDeptIds::contains)
                        .collect(Collectors.toSet());
            } else {
                filterDeptIds = allowedDeptIds.stream()
                        .filter(id -> id.equals(deptId))
                        .collect(Collectors.toSet());
            }
        } else {
            filterDeptIds = allowedDeptIds;
        }

        // 获取所有用户并筛选
        List<UserDO> allUsers = userMapper.selectList(new UserQuery());
        return allUsers.stream()
                .filter(user -> {
                    // 部门筛选
                    if (user.getDeptId() == null || !filterDeptIds.contains(user.getDeptId())) {
                        return false;
                    }
                    // 角色筛选
                    if (roleIds != null && !roleIds.isEmpty()) {
                        if (user.getRoleId() == null || !roleIds.contains(user.getRoleId())) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getById(Long id) {
        UserDO user = userMapper.selectById(id);
        return convert(user);
    }

    @Override
    public void create(UserDTO dto) {
        UserDO user = new UserDO();
        ConvertUtils.copyProperties(dto, user);
        // 默认密码123456，MD5加密
        user.setPassword(DigestUtil.md5Hex("123456"));
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public void update(UserDTO dto) {
        UserDO user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setRoleId(dto.getRoleId());
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    /**
     * 获取用户所属的二级机构（level=2的部门ID）
     * @param userId 用户ID
     * @return 二级机构ID，如果找不到返回null
     */
    public Long getSecondaryDeptId(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null) {
            return null;
        }

        DeptDO dept = deptMapper.selectById(user.getDeptId());
        if (dept == null) {
            return null;
        }

        // 如果当前部门就是二级机构（level=2），直接返回
        if (dept.getLevel() == 2) {
            return dept.getId();
        }

        // 否则向上查找，直到找到二级机构
        DeptDO currentDept = dept;
        while (currentDept != null && currentDept.getLevel() > 2) {
            currentDept = deptMapper.selectById(currentDept.getParentId());
            if (currentDept != null && currentDept.getLevel() == 2) {
                return currentDept.getId();
            }
        }

        return null;
    }

    /**
     * 获取用户允许查询的部门ID集合
     * @param currentUser 当前用户
     * @param currentRole 当前用户角色
     * @return 可查询的部门ID集合
     */
    public Set<Long> getAllowedDeptIds(UserDTO currentUser, RoleDO currentRole) {
        // 系统管理员和总消保管理岗可以查询所有部门
        if (currentRole != null) {
            if ("SYSTEM_ADMIN".equals(currentRole.getRoleType()) ||
                "GENERAL_MGMT".equals(currentRole.getRoleType())) {
                List<DeptDO> allDepts = deptMapper.selectList(new DeptQuery());
                return allDepts.stream()
                        .map(DeptDO::getId)
                        .collect(Collectors.toSet());
            }
        }

        // 分消保管理岗只能查询其二级机构及子部门
        if (currentRole != null && "BRANCH_MGMT".equals(currentRole.getRoleType())) {
            Long secondaryDeptId = getSecondaryDeptId(currentUser.getId());
            if (secondaryDeptId != null) {
                Set<Long> deptIds = getAllSubDeptIds(secondaryDeptId);
                deptIds.add(secondaryDeptId);
                return deptIds;
            }
        }

        // 其他情况返回空集合（无权限）
        return new HashSet<>();
    }

    /**
     * 获取指定部门的所有子部门ID（递归）
     * @param deptId 部门ID
     * @return 所有子部门ID集合
     */
    private Set<Long> getAllSubDeptIds(Long deptId) {
        Set<Long> result = new HashSet<>();
        List<DeptDO> children = deptMapper.selectByParentId(deptId);
        for (DeptDO child : children) {
            result.add(child.getId());
            result.addAll(getAllSubDeptIds(child.getId()));
        }
        return result;
    }

    private UserDTO convert(UserDO entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        ConvertUtils.copyProperties(entity, dto);

        // 填充角色信息
        if (entity.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(entity.getRoleId());
            if (role != null) {
                dto.setRoleId(role.getId());
                dto.setRoleName(role.getName());
                dto.setRoleType(role.getRoleType());
            }
        }

        // 填充部门信息
        if (entity.getDeptId() != null) {
            DeptDO dept = deptMapper.selectById(entity.getDeptId());
            if (dept != null) {
                dto.setDeptId(dept.getId());
                dto.setDeptName(dept.getName());
            }
        }

        return dto;
    }

    @Override
    public PageResult<UserDTO> searchUsers(Long userId, UserGetListWithFilterQry qry) {
        UserDTO currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return PageResult.of(new ArrayList<>(), 0L, qry.getPageNum(), qry.getPageSize());
        }

        RoleDO currentRole = getCurrentRole(currentUser);
        Set<Long> allowedDeptIds = getAllowedDeptIds(currentUser, currentRole);
        Set<Long> filterDeptIds = calculateFilterDeptIds(qry, allowedDeptIds);

        List<UserDTO> filteredUsers = filterUsers(qry, filterDeptIds);
        return paginateUsers(filteredUsers, qry.getPageNum(), qry.getPageSize());
    }

    /**
     * 获取当前用户角色
     */
    private RoleDO getCurrentRole(UserDTO currentUser) {
        if (currentUser.getRoleId() == null) {
            return null;
        }
        return roleMapper.selectById(currentUser.getRoleId());
    }

    /**
     * 计算筛选部门集合
     */
    private Set<Long> calculateFilterDeptIds(UserGetListWithFilterQry qry, Set<Long> allowedDeptIds) {
        if (qry.getDeptId() == null) {
            return allowedDeptIds;
        }

        if (Boolean.TRUE.equals(qry.getIncludeSubDept())) {
            return calculateFilterDeptIdsWithSubDept(qry.getDeptId(), allowedDeptIds);
        } else {
            return calculateFilterDeptIdsWithoutSubDept(qry.getDeptId(), allowedDeptIds);
        }
    }

    /**
     * 计算筛选部门集合（包含子部门）
     */
    private Set<Long> calculateFilterDeptIdsWithSubDept(Long deptId, Set<Long> allowedDeptIds) {
        Set<Long> subDeptIds = getAllSubDeptIds(deptId);
        subDeptIds.add(deptId);
        return allowedDeptIds.stream()
                .filter(subDeptIds::contains)
                .collect(Collectors.toSet());
    }

    /**
     * 计算筛选部门集合（不包含子部门）
     */
    private Set<Long> calculateFilterDeptIdsWithoutSubDept(Long deptId, Set<Long> allowedDeptIds) {
        return allowedDeptIds.stream()
                .filter(id -> id.equals(deptId))
                .collect(Collectors.toSet());
    }

    /**
     * 筛选用户
     */
    private List<UserDTO> filterUsers(UserGetListWithFilterQry qry, Set<Long> filterDeptIds) {
        List<UserDO> allUsers = userMapper.selectList(new UserQuery());
        return allUsers.stream()
                .filter(user -> matchesFilterCriteria(user, qry, filterDeptIds))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否匹配筛选条件
     */
    private boolean matchesFilterCriteria(UserDO user, UserGetListWithFilterQry qry, Set<Long> filterDeptIds) {
        if (!matchesDepartmentFilter(user, filterDeptIds)) {
            return false;
        }
        if (!matchesRoleFilter(user, qry)) {
            return false;
        }
        if (!matchesKeywordFilter(user, qry)) {
            return false;
        }
        return true;
    }

    /**
     * 检查部门筛选条件
     */
    private boolean matchesDepartmentFilter(UserDO user, Set<Long> filterDeptIds) {
        return user.getDeptId() != null && filterDeptIds.contains(user.getDeptId());
    }

    /**
     * 检查角色筛选条件
     */
    private boolean matchesRoleFilter(UserDO user, UserGetListWithFilterQry qry) {
        if (qry.getRoleIds() == null || qry.getRoleIds().isEmpty()) {
            return true;
        }
        return user.getRoleId() != null && qry.getRoleIds().contains(user.getRoleId());
    }

    /**
     * 检查关键词筛选条件
     */
    private boolean matchesKeywordFilter(UserDO user, UserGetListWithFilterQry qry) {
        if (!StringUtils.hasText(qry.getKeyword())) {
            return true;
        }

        String keyword = qry.getKeyword().toLowerCase();
        boolean matchName = user.getRealName() != null &&
                user.getRealName().toLowerCase().contains(keyword);
        boolean matchUsername = user.getUsername() != null &&
                user.getUsername().toLowerCase().contains(keyword);

        return matchName || matchUsername;
    }

    /**
     * 用户列表分页
     */
    private PageResult<UserDTO> paginateUsers(List<UserDTO> users, Integer pageNum, Integer pageSize) {
        int total = users.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<UserDTO> pagedUsers = start < total ?
                users.subList(start, end) : new ArrayList<>();

        return PageResult.of(pagedUsers, (long) total, pageNum, pageSize);
    }

    /**
     * 将UserDO转换为UserDTO（包含角色和部门信息）
     */
    private UserDTO convertToDTO(UserDO entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setRealName(entity.getRealName());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setRoleId(entity.getRoleId());
        dto.setDeptId(entity.getDeptId());
        dto.setStatus(entity.getStatus());

        // 填充角色信息
        if (entity.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(entity.getRoleId());
            if (role != null) {
                dto.setRoleName(role.getName());
                dto.setRoleType(role.getRoleType());
            }
        }

        // 填充部门信息
        if (entity.getDeptId() != null) {
            DeptDO dept = deptMapper.selectById(entity.getDeptId());
            if (dept != null) {
                dto.setDeptName(dept.getName());
            }
        }

        return dto;
    }
}
