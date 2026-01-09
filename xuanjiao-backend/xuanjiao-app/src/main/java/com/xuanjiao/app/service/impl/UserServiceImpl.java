package com.xuanjiao.app.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.xuanjiao.app.service.UserService;
import com.xuanjiao.client.dto.UserDTO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserDTO getCurrentUser(Long userId) {
        return getById(userId);
    }

    @Override
    public List<UserDTO> list() {
        List<UserDO> list = userMapper.selectList(null);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public UserDTO getById(Long id) {
        UserDO user = userMapper.selectById(id);
        return convert(user);
    }

    @Override
    public void create(UserDTO dto) {
        UserDO user = new UserDO();
        BeanUtils.copyProperties(dto, user);
        // 默认密码123456，MD5加密
        user.setPassword(DigestUtil.md5Hex("123456"));
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public void update(UserDTO dto) {
        UserDO user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setDeptId(dto.getDeptId());
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    private UserDTO convert(UserDO entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
