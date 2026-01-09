package com.xuanjiao.domain.user.repository;

import com.xuanjiao.domain.user.entity.User;

public interface UserRepository {
    User findByUsername(String username);
    User findById(Long id);
    void save(User user);
    void update(User user);
}
