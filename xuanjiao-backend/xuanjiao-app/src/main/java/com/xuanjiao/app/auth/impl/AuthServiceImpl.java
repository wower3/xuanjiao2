package com.xuanjiao.app.auth.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.xuanjiao.app.auth.AuthService;
import com.xuanjiao.app.user.UserService;
import com.xuanjiao.app.util.JwtUtil;
import com.xuanjiao.client.dto.auth.LoginCmd;
import com.xuanjiao.client.dto.auth.dto.LoginResultDTO;
import com.xuanjiao.client.dto.user.dto.UserDTO;
import com.xuanjiao.domain.user.entity.User;
import com.xuanjiao.domain.user.repository.UserRepository;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.role.RoleMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

 /**
 * 认证服务实现类
 * <p>实现AuthService接口，封装用户认证逻辑</p>
 * <p>核心功能：用户登录（生成JWT Token）、登出</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.auth.AuthService
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public LoginResultDTO login(LoginCmd cmd) {
        User user = userRepository.findByUsername(cmd.getUsername());
        // 统一错误提示，不区分用户不存在和密码错误
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        String encryptPwd = DigestUtil.md5Hex(cmd.getPassword());
        if (!encryptPwd.equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        // 填充角色信息
        if (user.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                userDTO.setRoleId(role.getId());
                userDTO.setRoleName(role.getName());
                userDTO.setRoleType(role.getRoleType());
            }
        }

        result.setUser(userDTO);
        return result;
    }

    @Override
    public void logout(String token) {
        // 可以将token加入黑名单
    }
}
