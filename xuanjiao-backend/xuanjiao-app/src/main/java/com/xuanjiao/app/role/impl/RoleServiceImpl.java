package com.xuanjiao.app.role.impl;

import com.xuanjiao.app.menu.MenuService;
import com.xuanjiao.app.role.RoleService;
import com.xuanjiao.client.role.RoleDTO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.role.RoleQuery;
import com.xuanjiao.common.ConvertUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * <p>实现RoleService接口，封装角色业务逻辑</p>
 * <p>核心功能：角色CRUD、菜单权限分配</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.role.RoleService
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private MenuService menuService;

    @Override
    public List<RoleDTO> list() {
        RoleQuery query = new RoleQuery();
        query.setOrderByField("id");
        query.setOrderByDirection("DESC");
        List<RoleDO> list = roleMapper.selectList(query);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public RoleDTO getById(Long id) {
        RoleDO role = roleMapper.selectById(id);
        RoleDTO dto = convert(role);
        if (dto != null) {
            dto.setMenuIds(menuService.getMenuIdsByRoleId(id));
        }
        return dto;
    }

    @Override
    public void create(RoleDTO dto) {
        // 校验 roleType 格式和唯一性
        validateRoleType(dto.getRoleType(), null);

        RoleDO role = new RoleDO();
        ConvertUtils.copyProperties(dto, role);
        role.setStatus(1);
        roleMapper.insert(role);
        // 分配菜单权限
        if (dto.getMenuIds() != null) {
            menuService.assignMenusToRole(role.getId(), dto.getMenuIds());
        }
    }

    @Override
    public void update(RoleDTO dto) {
        // 校验 roleType 格式和唯一性（排除当前角色自身）
        validateRoleType(dto.getRoleType(), dto.getId());
        RoleDO role = new RoleDO();
        ConvertUtils.copyProperties(dto, role);
        roleMapper.updateById(role);
        // 更新菜单权限
        if (dto.getMenuIds() != null) {
            menuService.assignMenusToRole(dto.getId(), dto.getMenuIds());
        }
    }

    @Override
    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    @Override
    public void assignMenus(Long roleId, List<Long> menuIds) {
        menuService.assignMenusToRole(roleId, menuIds);
    }

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return menuService.getMenuIdsByRoleId(roleId);
    }

    private RoleDTO convert(RoleDO entity) {
        return ConvertUtils.copyProperties(entity, RoleDTO.class);
    }

    /**
     * 校验 roleType 格式和唯一性
     * @param roleType 角色类型
     * @param excludeId 排除的角色ID（更新时使用，null表示不排除）
     */
    private void validateRoleType(String roleType, Long excludeId) {
        if (roleType == null || roleType.trim().isEmpty()) {
            throw new RuntimeException("角色类型不能为空");
        }

        // 校验格式：只允许大写字母、数字和下划线
        Pattern pattern = Pattern.compile("^[A-Z0-9_]+$");
        if (!pattern.matcher(roleType).matches()) {
            throw new RuntimeException("角色类型只能包含大写字母、数字和下划线");
        }

        // 校验唯一性
        RoleQuery query = new RoleQuery();
        query.setRoleType(roleType);
        if (excludeId != null) {
            query.setExcludeId(excludeId);
        }
        Long count = roleMapper.selectCount(query);
        if (count > 0) {
            throw new RuntimeException("角色类型已存在：" + roleType);
        }
    }
}
