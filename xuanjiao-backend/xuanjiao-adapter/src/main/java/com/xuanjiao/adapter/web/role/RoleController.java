package com.xuanjiao.adapter.web.role;

import com.xuanjiao.app.role.RoleService;
import com.xuanjiao.client.dto.common.Result;
import com.xuanjiao.client.dto.role.dto.RoleDTO;
import com.xuanjiao.client.dto.role.RoleCreateCmd;
import com.xuanjiao.client.dto.role.RoleDeleteCmd;
import com.xuanjiao.client.dto.role.RoleGetDetailQry;
import com.xuanjiao.client.dto.role.RoleGetListQry;
import com.xuanjiao.client.dto.role.RoleGetRoleMenusQry;
import com.xuanjiao.client.dto.role.RoleUpdateCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 角色管理控制器
 *
 * <p>提供角色的增删改查功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>角色列表：查询所有角色</li>
 *   <li>角色详情：查询单个角色的详细信息</li>
 *   <li>新增角色：创建新的角色</li>
 *   <li>更新角色：修改角色信息</li>
 *   <li>删除角色：删除指定角色</li>
 *   <li>分配权限：为角色分配菜单权限</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/role")
public class RoleController {

    /**
     * 角色服务
     *
     * <p>处理角色的增删改查和权限分配业务逻辑。</p>
     */
    @Resource
    private RoleService roleService;

    /**
     * 查询角色列表
     *
     * <p>查询系统中所有角色的列表信息。</p>
     *
     * @param qry 查询条件（当前无过滤参数）
     * @return 角色列表
     */
    @ApiOperation("角色列表")
    @PostMapping("/getList")
    public Result<List<RoleDTO>> list(@Valid @RequestBody RoleGetListQry qry) {
        return Result.success(roleService.list());
    }

    /**
     * 获取角色详情
     *
     * <p>根据角色ID查询角色的详细信息，包括角色名称、描述、类型等。</p>
     *
     * @param qry 查询条件，包含角色ID
     * @return 角色详情信息
     */
    @ApiOperation("获取角色详情")
    @PostMapping("/getDetail")
    public Result<RoleDTO> getById(@Valid @RequestBody RoleGetDetailQry qry) {
        return Result.success(roleService.getById(qry.getId()));
    }

    /**
     * 新增角色
     *
     * <p>创建新的角色，需要提供角色名称、描述、类型等信息。</p>
     *
     * @param cmd 创建命令，包含角色信息
     * @return 操作结果
     */
    @ApiOperation("新增角色")
    @PostMapping("/create")
    public Result<Void> create(@Valid @RequestBody RoleCreateCmd cmd) {
        roleService.create(convertToDto(cmd));
        return Result.success();
    }

    /**
     * 更新角色
     *
     * <p>修改指定角色的信息，包括角色名称、描述、类型等。</p>
     *
     * @param cmd 更新命令，包含角色ID和要更新的字段
     * @return 操作结果
     */
    @ApiOperation("更新角色")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody RoleUpdateCmd cmd) {
        roleService.update(convertToDto(cmd));
        return Result.success();
    }

    /**
     * 删除角色
     *
     * <p>删除指定的角色。如果角色下有用户，则需要先处理用户关联。</p>
     *
     * @param cmd 删除命令，包含要删除的角色ID
     * @return 操作结果
     */
    @ApiOperation("删除角色")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody RoleDeleteCmd cmd) {
        roleService.delete(cmd.getId());
        return Result.success();
    }

    /**
     * 分配角色菜单权限
     *
     * <p>为指定角色分配菜单访问权限，角色下的用户将能够访问这些菜单。</p>
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     * @return 操作结果
     */
    @ApiOperation("分配角色菜单权限")
    @PostMapping("/{roleId}/menus")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }

    /**
     * 获取角色菜单权限
     *
     * <p>查询指定角色已分配的菜单ID列表。</p>
     *
     * @param qry 查询条件，包含角色ID
     * @return 角色已分配的菜单ID列表
     */
    @ApiOperation("获取角色菜单权限")
    @PostMapping("/getRoleMenus")
    public Result<List<Long>> getRoleMenus(@Valid @RequestBody RoleGetRoleMenusQry qry) {
        return Result.success(roleService.getMenuIdsByRoleId(qry.getRoleId()));
    }

    /**
     * 将创建命令转换为DTO对象
     *
     * <p>将 RoleCreateCmd 转换为 RoleDTO，用于服务层处理。</p>
     *
     * @param cmd 创建命令
     * @return 角色DTO对象
     */
    private RoleDTO convertToDto(RoleCreateCmd cmd) {
        RoleDTO dto = new RoleDTO();
        dto.setName(cmd.getName());
        dto.setDescription(cmd.getDescription());
        dto.setRoleType(cmd.getRoleType() != null ? String.valueOf(cmd.getRoleType()) : null);
        return dto;
    }

    /**
     * 将更新命令转换为DTO对象
     *
     * <p>将 RoleUpdateCmd 转换为 RoleDTO，用于服务层处理。</p>
     *
     * @param cmd 更新命令
     * @return 角色DTO对象
     */
    private RoleDTO convertToDto(RoleUpdateCmd cmd) {
        RoleDTO dto = new RoleDTO();
        dto.setId(cmd.getId());
        dto.setName(cmd.getName());
        dto.setDescription(cmd.getDescription());
        dto.setRoleType(cmd.getRoleType() != null ? String.valueOf(cmd.getRoleType()) : null);
        return dto;
    }
}
