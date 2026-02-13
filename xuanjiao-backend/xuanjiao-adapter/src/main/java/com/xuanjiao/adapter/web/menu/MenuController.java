package com.xuanjiao.adapter.web.menu;

import com.xuanjiao.app.menu.MenuService;
import com.xuanjiao.client.dto.menu.MenuAssignCmd;
import com.xuanjiao.client.dto.menu.MenuCmd;
import com.xuanjiao.client.dto.menu.dto.MenuDTO;
import com.xuanjiao.client.dto.menu.MenuCreateCmd;
import com.xuanjiao.client.dto.menu.MenuDeleteCmd;
import com.xuanjiao.client.dto.menu.MenuGetCurrentQry;
import com.xuanjiao.client.dto.menu.MenuGetDetailQry;
import com.xuanjiao.client.dto.menu.MenuGetRoleMenusQry;
import com.xuanjiao.client.dto.menu.MenuGetTreeQry;
import com.xuanjiao.client.dto.menu.MenuUpdateCmd;
import com.xuanjiao.client.dto.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 菜单管理控制器
 *
 * <p>提供系统菜单的增删改查功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>菜单树：查询当前用户的菜单树（基于角色权限）</li>
 *   <li>菜单列表：查询所有菜单</li>
 *   <li>菜单详情：查询单个菜单的详细信息</li>
 *   <li>新增菜单：创建新的菜单项</li>
 *   <li>更新菜单：修改菜单信息</li>
 *   <li>删除菜单：删除指定菜单</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "菜单管理")
@RestController
@RequestMapping("/menu")
public class MenuController {

    /**
     * 菜单服务
     *
     * <p>处理菜单的增删改查和权限分配业务逻辑。</p>
     */
    @Resource
    private MenuService menuService;

    /**
     * 获取菜单树
     *
     * <p>查询系统中所有菜单的树形结构，用于后台管理界面展示菜单配置。</p>
     *
     * @param qry 查询条件（当前无过滤参数）
     * @return 菜单树形结构
     */
    @ApiOperation("获取菜单树")
    @PostMapping("/getTree")
    public Result<List<MenuDTO>> getTree(@Valid @RequestBody MenuGetTreeQry qry) {
        return Result.success(menuService.getTree());
    }

    /**
     * 获取当前用户菜单
     *
     * <p>根据当前登录用户的角色权限，查询其可访问的菜单树。
     * 用于前端动态生成导航菜单。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件（当前无过滤参数）
     * @return 当前用户可访问的菜单树
     */
    @ApiOperation("获取当前用户菜单")
    @PostMapping("/getCurrent")
    public Result<List<MenuDTO>> getCurrentMenus(@RequestAttribute("userId") Long userId, @Valid @RequestBody MenuGetCurrentQry qry) {
        return Result.success(menuService.getMenusByUserId(userId));
    }

    /**
     * 获取菜单详情
     *
     * <p>根据菜单ID查询菜单的详细信息，包括菜单名称、路径、图标、排序等。</p>
     *
     * @param qry 查询条件，包含菜单ID
     * @return 菜单详情信息
     */
    @ApiOperation("获取菜单详情")
    @PostMapping("/getDetail")
    public Result<MenuDTO> getById(@Valid @RequestBody MenuGetDetailQry qry) {
        return Result.success(menuService.getById(qry.getId()));
    }

    /**
     * 新增菜单
     *
     * <p>创建新的菜单项，需要提供菜单名称、路径、组件、图标等信息。</p>
     *
     * @param cmd 创建命令，包含菜单信息
     * @return 操作结果
     */
    @ApiOperation("新增菜单")
    @PostMapping("/create")
    public Result<Void> save(@Valid @RequestBody MenuCreateCmd cmd) {
        menuService.save(convertToMenuCmd(cmd));
        return Result.success();
    }

    /**
     * 更新菜单
     *
     * <p>修改指定菜单的信息，包括菜单名称、路径、图标、排序等。</p>
     *
     * @param cmd 更新命令，包含菜单ID和要更新的字段
     * @return 操作结果
     */
    @ApiOperation("更新菜单")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody MenuUpdateCmd cmd) {
        menuService.update(convertToMenuCmd(cmd));
        return Result.success();
    }

    /**
     * 删除菜单
     *
     * <p>删除指定的菜单。如果菜单有子菜单，则需要先删除子菜单。</p>
     *
     * @param cmd 删除命令，包含要删除的菜单ID
     * @return 操作结果
     */
    @ApiOperation("删除菜单")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody MenuDeleteCmd cmd) {
        menuService.delete(cmd.getId());
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
    @PostMapping("/assign")
    public Result<Void> assignMenus(@Valid @RequestBody MenuAssignCmd cmd) {
        menuService.assignMenusToRole(cmd.getRoleId(), cmd.getMenuIds());
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
    public Result<List<Long>> getRoleMenus(@Valid @RequestBody MenuGetRoleMenusQry qry) {
        return Result.success(menuService.getMenuIdsByRoleId(qry.getRoleId()));
    }

    /**
     * 将创建命令转换为MenuCmd对象
     *
     * <p>将 MenuCreateCmd 转换为 MenuCmd，用于服务层处理。</p>
     *
     * @param cmd 创建命令
     * @return 菜单命令对象
     */
    private MenuCmd convertToMenuCmd(MenuCreateCmd cmd) {
        MenuCmd menuCmd = new MenuCmd();
        menuCmd.setName(cmd.getName());
        menuCmd.setPath(cmd.getPath());
        menuCmd.setComponent(cmd.getComponent());
        menuCmd.setSort(cmd.getSort());
        menuCmd.setParentId(cmd.getParentId());
        menuCmd.setIcon(cmd.getIcon());
        menuCmd.setType(cmd.getMenuType() != null ? String.valueOf(cmd.getMenuType()) : null);
        return menuCmd;
    }

    /**
     * 将更新命令转换为MenuCmd对象
     *
     * <p>将 MenuUpdateCmd 转换为 MenuCmd，用于服务层处理。</p>
     *
     * @param cmd 更新命令
     * @return 菜单命令对象
     */
    private MenuCmd convertToMenuCmd(MenuUpdateCmd cmd) {
        MenuCmd menuCmd = new MenuCmd();
        menuCmd.setId(cmd.getId());
        menuCmd.setName(cmd.getName());
        menuCmd.setPath(cmd.getPath());
        menuCmd.setComponent(cmd.getComponent());
        menuCmd.setSort(cmd.getSort());
        menuCmd.setParentId(cmd.getParentId());
        menuCmd.setIcon(cmd.getIcon());
        menuCmd.setType(cmd.getMenuType() != null ? String.valueOf(cmd.getMenuType()) : null);
        return menuCmd;
    }
}
