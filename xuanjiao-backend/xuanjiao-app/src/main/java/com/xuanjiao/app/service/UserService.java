package com.xuanjiao.app.service;

import com.xuanjiao.client.dto.UserDTO;
import java.util.List;

public interface UserService {
    UserDTO getCurrentUser(Long userId);
    List<UserDTO> list();
    UserDTO getById(Long id);
    void create(UserDTO userDTO);
    void update(UserDTO userDTO);
    void delete(Long id);
}
