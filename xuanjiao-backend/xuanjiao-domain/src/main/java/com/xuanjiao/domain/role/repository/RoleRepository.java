package com.xuanjiao.domain.role.repository;

import com.xuanjiao.domain.role.entity.Role;
import java.util.List;

public interface RoleRepository {
    Role findById(Long id);
    List<Role> findAll();
    void save(Role role);
    void update(Role role);
    void deleteById(Long id);
}
