package com.xuanjiao.infrastructure.user;

import com.xuanjiao.domain.user.entity.User;
import com.xuanjiao.domain.user.repository.UserRepository;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.common.ConvertUtils;
import org.springframework.stereotype.Repository;
import javax.annotation.Resource;

/**
 * 用户仓储实现类
 *
 * <p>实现用户数据的持久化操作，基于 MyBatis Mapper。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    /**
     * 用户 Mapper
     */
    @Resource
    private UserMapper userMapper;

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Override
    public User findByUsername(String username) {
        UserDO userDO = userMapper.selectOneByUsername(username);
        return convert(userDO);
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户实体
     */
    @Override
    public User findById(Long id) {
        UserDO userDO = userMapper.selectById(id);
        return convert(userDO);
    }

    /**
     * 保存用户
     *
     * @param user 用户实体
     */
    @Override
    public void save(User user) {
        UserDO userDO = new UserDO();
        ConvertUtils.copyProperties(user, userDO);
        userMapper.insert(userDO);
        user.setId(userDO.getId());
    }

    /**
     * 更新用户
     *
     * @param user 用户实体
     */
    @Override
    public void update(User user) {
        UserDO userDO = new UserDO();
        ConvertUtils.copyProperties(user, userDO);
        userMapper.updateById(userDO);
    }

    /**
     * 将 DO 转换为实体
     *
     * @param userDO 用户数据对象
     * @return 用户实体
     */
    private User convert(UserDO userDO) {
        if (userDO == null) {
            return null;
        }
        User user = new User();
        ConvertUtils.copyProperties(userDO, user);
        return user;
    }
}
