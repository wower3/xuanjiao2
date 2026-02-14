package com.xuanjiao.adapter.web.user;

import com.xuanjiao.app.user.UserService;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.Result;
import com.xuanjiao.client.user.UserDTO;
import com.xuanjiao.client.user.UserCreateCmd;
import com.xuanjiao.client.user.UserDeleteCmd;
import com.xuanjiao.client.user.UserGetDefaultFilterDeptQry;
import com.xuanjiao.client.user.UserGetListQry;
import com.xuanjiao.client.user.UserGetListWithFilterQry;
import com.xuanjiao.client.user.UserUpdateCmd;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户管理控制器
 *
 * <p>提供用户的增删改查功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>用户列表：分页查询用户列表</li>
 *   <li>用户详情：查询单个用户的详细信息</li>
 *   <li>新增用户：创建新的用户</li>
 *   <li>更新用户：修改用户信息</li>
 *   <li>删除用户：删除指定用户</li>
 *   <li>重置密码：为用户重置密码</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 用户服务
     *
     * <p>处理用户的增删改查业务逻辑。</p>
     */
    @Resource
    private UserService userService;

    /**
     * 角色数据访问对象
     *
     * <p>用于查询角色信息，实现权限控制。</p>
     */
    @Resource
    private RoleMapper roleMapper;

    /**
     * 用户数据访问对象
     *
     * <p>用于查询用户信息。</p>
     */
    @Resource
    private UserMapper userMapper;

    /**
     * 获取当前登录用户信息
     *
     * <p>根据用户ID查询当前登录用户的详细信息，包括用户名、姓名、部门、角色等。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 当前用户信息
     */
    @ApiOperation("获取当前用户")
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getCurrentUser(userId));
    }

    /**
     * 查询用户列表
     *
     * <p>根据当前用户角色权限查询用户列表：
     * 系统管理员和总消保管理岗可查看所有用户，
     * 分消保管理岗只能查看其所属二级机构的用户。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件（当前无过滤参数）
     * @return 用户列表
     */
    @ApiOperation("用户列表")
    @PostMapping("/getList")
    public Result<List<UserDTO>> list(@RequestAttribute(value = "userId", required = false) Long userId, @Valid @RequestBody UserGetListQry qry) {
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

    /**
     * 查询用户列表（带筛选条件）
     *
     * <p>根据角色ID、部门ID等条件筛选用户列表，
     * 支持包含子部门的递归查询。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件，包含角色ID列表、部门ID、是否包含子部门
     * @return 筛选后的用户列表
     */
    @ApiOperation("用户列表（带筛选条件）")
    @PostMapping("/getListWithFilter")
    public Result<List<UserDTO>> listWithFilter(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @Valid @RequestBody UserGetListWithFilterQry qry) {
        return Result.success(userService.listWithFilter(userId, qry.getRoleIds(), qry.getDeptId(), qry.getIncludeSubDept()));
    }

    /**
     * 搜索用户（支持角色/部门/姓名筛选，带分页）
     *
     * <p>分页查询用户列表，支持按角色、部门、姓名等条件进行筛选，
     * 适用于审批人选择器等场景。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件，包含分页参数和筛选条件
     * @return 分页的用户列表
     */
    @ApiOperation("搜索用户（支持角色/部门/姓名筛选，带分页）")
    @PostMapping("/search")
    public Result<PageResult<Map<String, Object>>> searchUsers(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @Valid @RequestBody UserGetListWithFilterQry qry) {
        return Result.success(userService.searchUsers(userId, qry));
    }

    /**
     * 获取当前用户的默认筛选部门
     *
     * <p>根据当前用户角色返回默认的部门筛选条件：
     * 分消保管理岗需要默认筛选其所属二级机构，
     * 且部门选择受限。其他角色无默认筛选。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件（当前无过滤参数）
     * @return 默认筛选部门配置
     */
    @ApiOperation("获取当前用户的默认筛选部门")
    @PostMapping("/getDefaultFilterDept")
    public Result<DefaultFilterDeptDTO> getDefaultFilterDept(@RequestAttribute("userId") Long userId, @Valid @RequestBody UserGetDefaultFilterDeptQry qry) {
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

    /**
     * 新增用户
     *
     * <p>创建新的用户账号，需要提供用户名、姓名、邮箱、电话、部门、角色等信息。
     * 系统管理员和总消保管理岗可创建所有部门的用户，
     * 分消保管理岗只能创建其允许管理的部门的用户。</p>
     *
     * @param currentUserId 当前登录用户ID，由拦截器注入
     * @param cmd 创建命令，包含用户信息
     * @return 操作结果
     */
    @ApiOperation("新增用户")
    @PostMapping("/create")
    public Result<Void> create(@RequestAttribute("userId") Long currentUserId, @Valid @RequestBody UserCreateCmd cmd) {
        // 部门权限检查：只能创建允许查询的部门的用户
        checkCreateUpdatePermission(currentUserId, cmd.getDeptId());
        // 角色分配权限检查
        checkRoleAssignmentPermission(currentUserId, cmd.getRoleId());
        userService.create(convertToDto(cmd));
        return Result.success();
    }

    /**
     * 更新用户
     *
     * <p>修改指定用户的信息，包括用户名、姓名、邮箱、电话、部门、角色等。
     * 权限控制与创建用户相同。</p>
     *
     * @param currentUserId 当前登录用户ID，由拦截器注入
     * @param cmd 更新命令，包含用户ID和要更新的字段
     * @return 操作结果
     */
    @ApiOperation("更新用户")
    @PostMapping("/update")
    public Result<Void> update(@RequestAttribute("userId") Long currentUserId, @Valid @RequestBody UserUpdateCmd cmd) {
        UserDTO targetUser = userService.getById(cmd.getId());
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        // 部门权限检查：只能更新允许查询的部门的用户
        checkCreateUpdatePermission(currentUserId, targetUser.getDeptId());
        // 角色分配权限检查
        checkRoleAssignmentPermission(currentUserId, cmd.getRoleId());
        userService.update(convertToDto(cmd));
        return Result.success();
    }

    /**
     * 删除用户
     *
     * <p>删除指定的用户账号。权限控制与创建用户相同。</p>
     *
     * @param currentUserId 当前登录用户ID，由拦截器注入
     * @param cmd 删除命令，包含要删除的用户ID
     * @return 操作结果
     */
    @ApiOperation("删除用户")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestAttribute("userId") Long currentUserId, @Valid @RequestBody UserDeleteCmd cmd) {
        UserDTO targetUser = userService.getById(cmd.getId());
        if (targetUser == null) {
            return Result.error("用户不存在");
        }
        // 部门权限检查：只能删除允许查询的部门的用户
        checkCreateUpdatePermission(currentUserId, targetUser.getDeptId());
        userService.delete(cmd.getId());
        return Result.success();
    }

    /**
     * 将创建命令转换为DTO对象
     *
     * <p>将 UserCreateCmd 转换为 UserDTO，用于服务层处理。</p>
     *
     * @param cmd 创建命令
     * @return 用户DTO对象
     */
    private UserDTO convertToDto(UserCreateCmd cmd) {
        UserDTO dto = new UserDTO();
        dto.setUsername(cmd.getUsername());
        dto.setRealName(cmd.getRealName());
        dto.setEmail(cmd.getEmail());
        dto.setPhone(cmd.getPhone());
        dto.setDeptId(cmd.getDeptId());
        dto.setRoleId(cmd.getRoleId());
        return dto;
    }

    /**
     * 将更新命令转换为DTO对象
     *
     * <p>将 UserUpdateCmd 转换为 UserDTO，用于服务层处理。</p>
     *
     * @param cmd 更新命令
     * @return 用户DTO对象
     */
    private UserDTO convertToDto(UserUpdateCmd cmd) {
        UserDTO dto = new UserDTO();
        dto.setId(cmd.getId());
        dto.setUsername(cmd.getUsername());
        dto.setRealName(cmd.getRealName());
        dto.setEmail(cmd.getEmail());
        dto.setPhone(cmd.getPhone());
        dto.setDeptId(cmd.getDeptId());
        dto.setRoleId(cmd.getRoleId());
        return dto;
    }

    /**
     * 检查创建/更新权限
     *
     * <p>系统管理员和总消保管理岗可以操作所有用户，
     * 分消保管理岗只能操作其允许查询的部门的用户。</p>
     *
     * @param currentUserId 当前登录用户ID
     * @param targetDeptId 目标用户所属部门ID
     * @throws RuntimeException 当用户无权操作时抛出异常
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
     *
     * <p>系统管理员和总消保管理岗可以分配所有角色，
     * 分消保管理岗只能分配除系统管理员(1)和总消保管理岗(4)外的其他角色。</p>
     *
     * @param currentUserId 当前登录用户ID
     * @param targetRoleId 要分配的目标角色ID
     * @throws RuntimeException 当用户无权分配该角色时抛出异常
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
     *
     * <p>用于返回当前用户的默认部门筛选配置，
     * 主要用于分消保管理岗的部门权限控制。</p>
     *
     * @author xuanjiao
     * @since 1.0.0
     */
    public static class DefaultFilterDeptDTO {

        /**
         * 是否有默认筛选
         *
         * <p>true 表示需要应用默认筛选，false 表示不限制。</p>
         */
        private Boolean hasFilter;

        /**
         * 默认筛选的部门ID
         */
        private Long deptId;

        /**
         * 是否包含子部门
         */
        private Boolean includeSubDept;

        /**
         * 是否可以分配所有角色
         *
         * <p>分消保管理岗不能分配系统管理员和总消保管理岗角色。</p>
         */
        private Boolean canAssignAllRoles;

        /**
         * 允许选择的部门ID集合
         *
         * <p>用于前端限制部门选择器的可选范围。</p>
         */
        private Set<Long> allowedDeptIds;

        /**
         * 根部门ID
         *
         * <p>分消保管理岗的根部门ID（二级机构），用于前端构建部门树。</p>
         */
        private Long rootDeptId;

        /**
         * 获取是否有默认筛选
         *
         * @return 是否有默认筛选
         */
        public Boolean getHasFilter() {
            return hasFilter;
        }

        /**
         * 设置是否有默认筛选
         *
         * @param hasFilter 是否有默认筛选
         */
        public void setHasFilter(Boolean hasFilter) {
            this.hasFilter = hasFilter;
        }

        /**
         * 获取默认筛选的部门ID
         *
         * @return 部门ID
         */
        public Long getDeptId() {
            return deptId;
        }

        /**
         * 设置默认筛选的部门ID
         *
         * @param deptId 部门ID
         */
        public void setDeptId(Long deptId) {
            this.deptId = deptId;
        }

        /**
         * 获取是否包含子部门
         *
         * @return 是否包含子部门
         */
        public Boolean getIncludeSubDept() {
            return includeSubDept;
        }

        /**
         * 设置是否包含子部门
         *
         * @param includeSubDept 是否包含子部门
         */
        public void setIncludeSubDept(Boolean includeSubDept) {
            this.includeSubDept = includeSubDept;
        }

        /**
         * 获取是否可以分配所有角色
         *
         * @return 是否可以分配所有角色
         */
        public Boolean getCanAssignAllRoles() {
            return canAssignAllRoles;
        }

        /**
         * 设置是否可以分配所有角色
         *
         * @param canAssignAllRoles 是否可以分配所有角色
         */
        public void setCanAssignAllRoles(Boolean canAssignAllRoles) {
            this.canAssignAllRoles = canAssignAllRoles;
        }

        /**
         * 获取允许选择的部门ID集合
         *
         * @return 部门ID集合
         */
        public Set<Long> getAllowedDeptIds() {
            return allowedDeptIds;
        }

        /**
         * 设置允许选择的部门ID集合
         *
         * @param allowedDeptIds 部门ID集合
         */
        public void setAllowedDeptIds(Set<Long> allowedDeptIds) {
            this.allowedDeptIds = allowedDeptIds;
        }

        /**
         * 获取根部门ID
         *
         * @return 根部门ID
         */
        public Long getRootDeptId() {
            return rootDeptId;
        }

        /**
         * 设置根部门ID
         *
         * @param rootDeptId 根部门ID
         */
        public void setRootDeptId(Long rootDeptId) {
            this.rootDeptId = rootDeptId;
        }
    }
}
