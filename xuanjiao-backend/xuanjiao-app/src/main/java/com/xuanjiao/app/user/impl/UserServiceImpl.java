package com.xuanjiao.app.user.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjiao.app.user.UserService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.UserDTO;
import com.xuanjiao.client.dto.user.UserGetListWithFilterQry;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.dept.DeptQuery;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        BeanUtils.copyProperties(dto, user);
        // 默认密码123456，MD5加密
        user.setPassword(DigestUtil.md5Hex("123456"));
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public void update(UserDTO dto) {
        UserDO user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
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
        BeanUtils.copyProperties(entity, dto);

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
    public PageResult<Map<String, Object>> searchUsers(Long userId, UserGetListWithFilterQry qry) {
        // 获取当前用户信息和角色
        UserDTO currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return PageResult.of(new ArrayList<>(), 0L, qry.getPageNum(), qry.getPageSize());
        }

        RoleDO currentRole = null;
        if (currentUser.getRoleId() != null) {
            currentRole = roleMapper.selectById(currentUser.getRoleId());
        }

        // 确定可查询的部门范围
        Set<Long> allowedDeptIds = getAllowedDeptIds(currentUser, currentRole);

        // 计算最终的筛选部门集合
        final Set<Long> filterDeptIds;
        if (qry.getDeptId() != null) {
            if (Boolean.TRUE.equals(qry.getIncludeSubDept())) {
                Set<Long> subDeptIds = getAllSubDeptIds(qry.getDeptId());
                subDeptIds.add(qry.getDeptId());
                filterDeptIds = allowedDeptIds.stream()
                        .filter(subDeptIds::contains)
                        .collect(Collectors.toSet());
            } else {
                filterDeptIds = allowedDeptIds.stream()
                        .filter(id -> id.equals(qry.getDeptId()))
                        .collect(Collectors.toSet());
            }
        } else {
            filterDeptIds = allowedDeptIds;
        }

        // 获取所有用户并筛选
        List<UserDO> allUsers = userMapper.selectList(new UserQuery());
        List<Map<String, Object>> filteredUsers = allUsers.stream()
                .filter(user -> {
                    // 部门筛选
                    if (user.getDeptId() == null || !filterDeptIds.contains(user.getDeptId())) {
                        return false;
                    }
                    // 角色筛选
                    if (qry.getRoleIds() != null && !qry.getRoleIds().isEmpty()) {
                        if (user.getRoleId() == null || !qry.getRoleIds().contains(user.getRoleId())) {
                            return false;
                        }
                    }
                    // 关键词筛选（匹配姓名或用户名）
                    if (StringUtils.hasText(qry.getKeyword())) {
                        String keyword = qry.getKeyword().toLowerCase();
                        boolean matchName = user.getRealName() != null &&
                                user.getRealName().toLowerCase().contains(keyword);
                        boolean matchUsername = user.getUsername() != null &&
                                user.getUsername().toLowerCase().contains(keyword);
                        if (!matchName && !matchUsername) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(this::convertToMap)
                .collect(Collectors.toList());

        // 分页处理
        int total = filteredUsers.size();
        int start = (qry.getPageNum() - 1) * qry.getPageSize();
        int end = Math.min(start + qry.getPageSize(), total);

        List<Map<String, Object>> pagedUsers = start < total ?
                filteredUsers.subList(start, end) : new ArrayList<>();

        return PageResult.of(pagedUsers, (long) total, qry.getPageNum(), qry.getPageSize());
    }

    /**
     * 将UserDO转换为Map（包含角色和部门信息）
     */
    private Map<String, Object> convertToMap(UserDO entity) {
        if (entity == null) return null;
        Map<String, Object> map = new HashMap<>();
        map.put("id", entity.getId());
        map.put("username", entity.getUsername());
        map.put("realName", entity.getRealName());
        map.put("phone", entity.getPhone());
        map.put("email", entity.getEmail());
        map.put("roleId", entity.getRoleId());
        map.put("deptId", entity.getDeptId());
        map.put("status", entity.getStatus());

        // 填充角色信息
        if (entity.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(entity.getRoleId());
            if (role != null) {
                map.put("roleName", role.getName());
                map.put("roleType", role.getRoleType());
            }
        }

        // 填充部门信息
        if (entity.getDeptId() != null) {
            DeptDO dept = deptMapper.selectById(entity.getDeptId());
            if (dept != null) {
                map.put("deptName", dept.getName());
            }
        }

        return map;
    }
}
