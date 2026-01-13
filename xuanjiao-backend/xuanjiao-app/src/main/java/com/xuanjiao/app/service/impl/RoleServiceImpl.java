package com.xuanjiao.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.service.MenuService;
import com.xuanjiao.app.service.RoleService;
import com.xuanjiao.client.dto.RoleDTO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.mapper.RoleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private MenuService menuService;

    @Override
    public List<RoleDTO> list() {
        List<RoleDO> list = roleMapper.selectList(new LambdaQueryWrapper<RoleDO>()
                .orderByDesc(RoleDO::getId));
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
        RoleDO role = new RoleDO();
        BeanUtils.copyProperties(dto, role);
        role.setStatus(1);
        roleMapper.insert(role);
        // 分配菜单权限
        if (dto.getMenuIds() != null) {
            menuService.assignMenusToRole(role.getId(), dto.getMenuIds());
        }
    }

    @Override
    public void update(RoleDTO dto) {
        RoleDO role = new RoleDO();
        BeanUtils.copyProperties(dto, role);
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
        if (entity == null) return null;
        RoleDTO dto = new RoleDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
