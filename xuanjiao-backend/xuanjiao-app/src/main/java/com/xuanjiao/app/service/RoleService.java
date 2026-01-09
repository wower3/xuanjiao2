package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.RoleDTO;
import java.util.List;

public interface RoleService {
    List<RoleDTO> list();
    void create(RoleDTO dto);
    void update(RoleDTO dto);
    void delete(Long id);
}
