package com.xuanjiao.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.domain.user.entity.User;
import com.xuanjiao.domain.user.repository.UserRepository;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDO::getUsername, username);
        UserDO userDO = userMapper.selectOne(wrapper);
        return convert(userDO);
    }

    @Override
    public User findById(Long id) {
        UserDO userDO = userMapper.selectById(id);
        return convert(userDO);
    }

    @Override
    public void save(User user) {
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(user, userDO);
        userMapper.insert(userDO);
        user.setId(userDO.getId());
    }

    @Override
    public void update(User user) {
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(user, userDO);
        userMapper.updateById(userDO);
    }

    private User convert(UserDO userDO) {
        if (userDO == null) return null;
        User user = new User();
        BeanUtils.copyProperties(userDO, user);
        return user;
    }
}
