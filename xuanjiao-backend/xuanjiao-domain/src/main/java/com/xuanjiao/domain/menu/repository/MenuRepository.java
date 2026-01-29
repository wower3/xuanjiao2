package com.xuanjiao.domain.menu.repository;

import com.xuanjiao.domain.menu.entity.Menu;
import java.util.List;

public interface MenuRepository {
    Menu findById(Long id);
    List<Menu> findAll();
    List<Menu> findByUserId(Long userId);
    void save(Menu menu);
    void update(Menu menu);
    void deleteById(Long id);
}
