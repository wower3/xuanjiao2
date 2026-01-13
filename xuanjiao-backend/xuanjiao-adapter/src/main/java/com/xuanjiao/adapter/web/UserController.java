package com.xuanjiao.adapter.web;

import com.xuanjiao.app.service.UserService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.UserDTO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.mapper.RoleMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RoleMapper roleMapper;

    @ApiOperation("获取当前用户")
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }

    @ApiOperation("用户列表")
    @GetMapping("/list")
    public Result<List<UserDTO>> list(@RequestAttribute(value = "userId", required = false) Long userId) {
        // 获取当前用户角色，如果是分消保管理岗，只显示分部门的用户
        if (userId != null) {
            UserDTO currentUser = userService.getCurrentUser(userId);
            if (currentUser != null && "BRANCH_MGMT".equals(currentUser.getRoleType())) {
                return Result.success(userService.listByBranchDept(userId));
            }
        }
        return Result.success(userService.list());
    }

    @ApiOperation("新增用户")
    @PostMapping
    public Result<Void> create(@RequestAttribute("userId") Long currentUserId, @RequestBody UserDTO userDTO) {
        // 角色分配权限检查
        checkRoleAssignmentPermission(currentUserId, userDTO.getRoleId());
        // 部门权限检查：分消保管理岗只能创建分部门的用户
        checkPermission(currentUserId, userDTO.getDeptId(), "create");
        userService.create(userDTO);
        return Result.success();
    }

    @ApiOperation("更新用户")
    @PutMapping
    public Result<Void> update(@RequestAttribute("userId") Long currentUserId, @RequestBody UserDTO userDTO) {
        UserDTO targetUser = userService.getById(userDTO.getId());
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        // 角色分配权限检查
        checkRoleAssignmentPermission(currentUserId, userDTO.getRoleId());
        // 部门权限检查：分消保管理岗只能更新分部门的用户
        checkPermission(currentUserId, targetUser.getDeptId(), "update");
        userService.update(userDTO);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestAttribute("userId") Long currentUserId, @PathVariable Long id) {
        UserDTO targetUser = userService.getById(id);
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        checkPermission(currentUserId, targetUser.getDeptId(), "delete");
        userService.delete(id);
        return Result.success();
    }

    /**
     * 角色分配权限检查
     * 系统管理员和总消保管理岗可以分配所有角色
     * 分消保管理岗只能分配分消保用户角色（id=7）
     */
    private void checkRoleAssignmentPermission(Long currentUserId, Long targetRoleId) {
        UserDTO currentUser = userService.getCurrentUser(currentUserId);
        if (currentUser == null || currentUser.getRoleId() == null) {
            throw new RuntimeException("无权操作");
        }

        RoleDO currentRole = roleMapper.selectById(currentUser.getRoleId());
        if (currentRole == null) {
            throw new RuntimeException("无权操作");
        }

        // 系统管理员和总消保管理岗可以分配所有角色
        if ("SYSTEM_ADMIN".equals(currentRole.getRoleType()) || "GENERAL_MGMT".equals(currentRole.getRoleType())) {
            return;
        }

        // 分消保管理岗只能分配分消保用户角色（id=7, role_type=BRANCH_USER）
        if ("BRANCH_MGMT".equals(currentRole.getRoleType())) {
            if (targetRoleId == null || !targetRoleId.equals(7L)) {
                throw new RuntimeException("分消保管理岗只能分配分消保用户角色");
            }
            return;
        }

        // 其他角色无权分配角色
        throw new RuntimeException("无权分配角色");
    }

    /**
     * 部门权限检查
     * 系统管理员和总消保管理岗可以操作所有用户
     * 分消保管理岗只能操作分部门（id=102）及其子部门的用户
     */
    private void checkPermission(Long currentUserId, Long targetDeptId, String operation) {
        UserDTO currentUser = userService.getCurrentUser(currentUserId);
        if (currentUser == null || currentUser.getRoleId() == null) {
            throw new RuntimeException("无权操作");
        }

        RoleDO currentRole = roleMapper.selectById(currentUser.getRoleId());
        if (currentRole == null) {
            throw new RuntimeException("无权操作");
        }

        // 系统管理员和总消保管理岗可以操作所有用户
        if ("SYSTEM_ADMIN".equals(currentRole.getRoleType()) || "GENERAL_MGMT".equals(currentRole.getRoleType())) {
            return;
        }

        // 分消保管理岗只能操作分部门的用户
        if ("BRANCH_MGMT".equals(currentRole.getRoleType())) {
            if (!isUnderBranchDept(targetDeptId)) {
                throw new RuntimeException("分消保管理岗只能管理分部门的用户");
            }
            return;
        }

        // 其他角色无权操作
        throw new RuntimeException("无权操作");
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
        return false;
    }
}
