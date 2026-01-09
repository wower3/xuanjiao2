package com.xuanjiao.app.service.impl;

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

    @Override
    public List<RoleDTO> list() {
        List<RoleDO> list = roleMapper.selectList(null);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public void create(RoleDTO dto) {
        RoleDO role = new RoleDO();
        BeanUtils.copyProperties(dto, role);
        role.setStatus(1);
        roleMapper.insert(role);
    }

    @Override
    public void update(RoleDTO dto) {
        RoleDO role = roleMapper.selectById(dto.getId());
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus());
        roleMapper.updateById(role);
    }

    @Override
    public void delete(Long id) {
        roleMapper.deleteById(id);
    }

    private RoleDTO convert(RoleDO entity) {
        if (entity == null) return null;
        RoleDTO dto = new RoleDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
