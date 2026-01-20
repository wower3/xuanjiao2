package com.xuanjiao.adapter.web.user;

import com.xuanjiao.app.user.UserService;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.client.dto.UserDTO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserMapper userMapper;

    @ApiOperation("获取当前用户")
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }

    @ApiOperation("用户列表")
    @GetMapping("/list")
    public Result<List<UserDTO>> list(@RequestAttribute(value = "userId", required = false) Long userId) {
        // 获取当前用户角色，如果是分消保管理岗，只显示其二级机构的用户
        if (userId != null) {
            UserDTO currentUser = userService.getCurrentUser(userId);
            if (currentUser != null) {
                RoleDO currentRole = roleMapper.selectById(currentUser.getRoleId());
                if (currentRole != null && "BRANCH_MGMT".equals(currentRole.getRoleType())) {
                    return Result.success(userService.listByBranchDept(userId));
                }
            }
        }
        return Result.success(userService.list());
    }

    @ApiOperation("用户列表（带筛选条件）")
    @GetMapping("/listWithFilter")
    public Result<List<UserDTO>> listWithFilter(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(value = "roleIds", required = false) String roleIdsStr,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "includeSubDept", required = false, defaultValue = "true") Boolean includeSubDept) {
        // 将逗号分隔的字符串转换为 List<Long>
        List<Long> roleIds = null;
        if (roleIdsStr != null && !roleIdsStr.trim().isEmpty()) {
            roleIds = new java.util.ArrayList<>();
            for (String id : roleIdsStr.split(",")) {
                try {
                    roleIds.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效的ID
                }
            }
        }
        return Result.success(userService.listWithFilter(userId, roleIds, deptId, includeSubDept));
    }

    @ApiOperation("获取当前用户的默认筛选部门")
    @GetMapping("/defaultFilterDept")
    public Result<DefaultFilterDeptDTO> getDefaultFilterDept(@RequestAttribute("userId") Long userId) {
        UserDTO currentUser = userService.getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }

        RoleDO currentRole = roleMapper.selectById(currentUser.getRoleId());
        if (currentRole == null) {
            return Result.error("用户角色不存在");
        }

        DefaultFilterDeptDTO result = new DefaultFilterDeptDTO();
        result.setHasFilter(false);
        result.setAllowedDeptIds(null); // 默认为null表示不限制
        result.setRootDeptId(null);

        // 分消保管理岗需要默认筛选其二级机构，且部门选择受限
        if ("BRANCH_MGMT".equals(currentRole.getRoleType())) {
            Long secondaryDeptId = userService.getSecondaryDeptId(userId);
            if (secondaryDeptId != null) {
                Set<Long> allowedDeptIds = userService.getAllowedDeptIds(currentUser, currentRole);
                result.setHasFilter(true);
                result.setDeptId(secondaryDeptId);
                result.setIncludeSubDept(true);
                result.setCanAssignAllRoles(false); // 分消保管理岗不能分配所有角色
                result.setAllowedDeptIds(allowedDeptIds); // 设置可选的部门ID列表
                result.setRootDeptId(secondaryDeptId); // 设置根部门ID（用于前端构建部门树）
            }
        }

        return Result.success(result);
    }

    @ApiOperation("新增用户")
    @PostMapping
    public Result<Void> create(@RequestAttribute("userId") Long currentUserId, @RequestBody UserDTO userDTO) {
        // 部门权限检查：只能创建允许查询的部门的用户
        checkCreateUpdatePermission(currentUserId, userDTO.getDeptId());
        // 角色分配权限检查
        checkRoleAssignmentPermission(currentUserId, userDTO.getRoleId());
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
        // 部门权限检查：只能更新允许查询的部门的用户
        checkCreateUpdatePermission(currentUserId, targetUser.getDeptId());
        // 角色分配权限检查
        checkRoleAssignmentPermission(currentUserId, userDTO.getRoleId());
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
        // 部门权限检查：只能删除允许查询的部门的用户
        checkCreateUpdatePermission(currentUserId, targetUser.getDeptId());
        userService.delete(id);
        return Result.success();
    }

    /**
     * 检查创建/更新权限
     * 系统管理员和总消保管理岗可以操作所有用户
     * 分消保管理岗只能操作其允许查询的部门的用户
     */
    private void checkCreateUpdatePermission(Long currentUserId, Long targetDeptId) {
        UserDTO currentUser = userService.getCurrentUser(currentUserId);
        if (currentUser == null || currentUser.getRoleId() == null) {
            throw new RuntimeException("无权操作");
        }

        RoleDO currentRole = roleMapper.selectById(currentUser.getRoleId());
        if (currentRole == null) {
            throw new RuntimeException("无权操作");
        }

        // 系统管理员和总消保管理岗可以操作所有用户
        if ("SYSTEM_ADMIN".equals(currentRole.getRoleType()) ||
            "GENERAL_MGMT".equals(currentRole.getRoleType())) {
            return;
        }

        // 分消保管理岗只能操作其允许查询的部门的用户
        if ("BRANCH_MGMT".equals(currentRole.getRoleType())) {
            Set<Long> allowedDeptIds = userService.getAllowedDeptIds(currentUser, currentRole);
            if (targetDeptId == null || !allowedDeptIds.contains(targetDeptId)) {
                throw new RuntimeException("分消保管理岗只能管理其所属二级机构的用户");
            }
            return;
        }

        // 其他角色无权操作
        throw new RuntimeException("无权操作");
    }

    /**
     * 检查角色分配权限
     * 系统管理员和总消保管理岗可以分配所有角色
     * 分消保管理岗只能分配除系统管理员(1)和总消保管理岗(4)外的其他角色
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
        if ("SYSTEM_ADMIN".equals(currentRole.getRoleType()) ||
            "GENERAL_MGMT".equals(currentRole.getRoleType())) {
            return;
        }

        // 分消保管理岗不能分配系统管理员(1)和总消保管理岗(4)
        if ("BRANCH_MGMT".equals(currentRole.getRoleType())) {
            if (targetRoleId != null) {
                if (targetRoleId.equals(1L) || targetRoleId.equals(4L)) {
                    throw new RuntimeException("分消保管理岗不能分配系统管理员和总消保管理岗角色");
                }
            }
            return;
        }

        // 其他角色无权分配角色
        throw new RuntimeException("无权分配角色");
    }

    /**
     * 默认筛选部门返回对象
     */
    public static class DefaultFilterDeptDTO {
        private Boolean hasFilter;
        private Long deptId;
        private Boolean includeSubDept;
        private Boolean canAssignAllRoles;
        private Set<Long> allowedDeptIds;
        private Long rootDeptId; // 分消保管理岗的根部门ID（二级机构）

        public Boolean getHasFilter() {
            return hasFilter;
        }

        public void setHasFilter(Boolean hasFilter) {
            this.hasFilter = hasFilter;
        }

        public Long getDeptId() {
            return deptId;
        }

        public void setDeptId(Long deptId) {
            this.deptId = deptId;
        }

        public Boolean getIncludeSubDept() {
            return includeSubDept;
        }

        public void setIncludeSubDept(Boolean includeSubDept) {
            this.includeSubDept = includeSubDept;
        }

        public Boolean getCanAssignAllRoles() {
            return canAssignAllRoles;
        }

        public void setCanAssignAllRoles(Boolean canAssignAllRoles) {
            this.canAssignAllRoles = canAssignAllRoles;
        }

        public Set<Long> getAllowedDeptIds() {
            return allowedDeptIds;
        }

        public void setAllowedDeptIds(Set<Long> allowedDeptIds) {
            this.allowedDeptIds = allowedDeptIds;
        }

        public Long getRootDeptId() {
            return rootDeptId;
        }

        public void setRootDeptId(Long rootDeptId) {
            this.rootDeptId = rootDeptId;
        }
    }
}
