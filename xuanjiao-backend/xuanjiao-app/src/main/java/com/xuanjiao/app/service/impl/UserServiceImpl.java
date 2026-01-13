package com.xuanjiao.app.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.xuanjiao.app.service.UserService;
import com.xuanjiao.client.dto.UserDTO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.mapper.DeptMapper;
import com.xuanjiao.infrastructure.mapper.RoleMapper;
import com.xuanjiao.infrastructure.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
        List<UserDO> list = userMapper.selectList(null);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> listByBranchDept(Long currentUserId) {
        // 获取当前用户信息
        UserDO currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null || currentUser.getRoleId() == null) {
            return list();
        }

        // 获取当前用户的角色
        RoleDO currentRole = roleMapper.selectById(currentUser.getRoleId());
        if (currentRole == null || !"BRANCH_MGMT".equals(currentRole.getRoleType())) {
            // 不是分消保管理岗，返回所有用户
            return list();
        }

        // 分消保管理岗只能看到分部门（id=102）及其子部门的用户
        List<UserDO> allUsers = userMapper.selectList(null);
        return allUsers.stream()
                .filter(user -> isUnderBranchDept(user.getDeptId()))
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
     * 检查部门是否属于分部门（id=102）及其子部门
     */
    private boolean isUnderBranchDept(Long deptId) {
        if (deptId == null) {
            return false;
        }
        // 分部门ID是102，直接匹配
        if (deptId.equals(102L)) {
            return true;
        }
        // 检查是否是分消保部（id=202）
        if (deptId.equals(202L)) {
            return true;
        }
        // 这里可以扩展：如果有更多3级部门，需要查询数据库判断是否属于分部门的子部门
        return false;
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
}
