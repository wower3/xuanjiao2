package com.xuanjiao.app.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.xuanjiao.app.service.AuthService;
import com.xuanjiao.app.util.JwtUtil;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.domain.user.entity.User;
import com.xuanjiao.domain.user.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public LoginResultDTO login(LoginCmd cmd) {
        User user = userRepository.findByUsername(cmd.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String encryptPwd = DigestUtil.md5Hex(cmd.getPassword());
        if (!encryptPwd.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        result.setUser(userDTO);
        return result;
    }

    @Override
    public void logout(String token) {
        // 可以将token加入黑名单
    }
}
