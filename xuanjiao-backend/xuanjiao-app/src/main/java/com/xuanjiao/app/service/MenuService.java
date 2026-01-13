package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.MenuDTO;
import com.xuanjiao.client.dto.MenuCmd;

import java.util.List;

public interface MenuService {
    List<MenuDTO> getTree();
    MenuDTO getById(Long id);
    void save(MenuCmd cmd);
    void update(MenuCmd cmd);
    void delete(Long id);
    void assignMenusToRole(Long roleId, List<Long> menuIds);
    List<Long> getMenuIdsByRoleId(Long roleId);
    List<MenuDTO> getMenusByUserId(Long userId);
}
