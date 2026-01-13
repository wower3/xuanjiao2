package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.RoleDTO;
import java.util.List;

public interface RoleService {
    List<RoleDTO> list();
    RoleDTO getById(Long id);
    void create(RoleDTO dto);
    void update(RoleDTO dto);
    void delete(Long id);
    void assignMenus(Long roleId, List<Long> menuIds);
    List<Long> getMenuIdsByRoleId(Long roleId);
}
