package com.xuanjiao.infrastructure.deletion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AssetDeletionApplicationMapper测试实现类
 * 用于集成测试中直接注入Mapper
 */
@Component
public class AssetDeletionApplicationMapperImpl implements AssetDeletionApplicationMapper {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private AssetDeletionApplicationMapper assetDeletionApplicationMapper;

    private SqlSession getSession() {
        return sqlSessionFactory.openSession();
    }

    @Override
    public AssetDeletionApplicationDO selectById(Long id) {
        return assetDeletionApplicationMapper.selectById(id);
    }

    @Override
    public AssetDeletionApplicationDO selectOne(AssetDeletionApplicationQuery query) {
        return assetDeletionApplicationMapper.selectOne(query);
    }

    @Override
    public List<AssetDeletionApplicationDO> selectList(AssetDeletionApplicationQuery query) {
        return assetDeletionApplicationMapper.selectList(query);
    }

    @Override
    public Long selectCount(AssetDeletionApplicationQuery query) {
        return assetDeletionApplicationMapper.selectCount(query);
    }

    @Override
    public IPage<AssetDeletionApplicationDO> selectPage(Page<AssetDeletionApplicationDO> page, AssetDeletionApplicationQuery query) {
        return assetDeletionApplicationMapper.selectPage(page, query);
    }

    @Override
    public int insert(AssetDeletionApplicationDO assetDeletionApplicationDO) {
        try (SqlSession session = getSession()) {
            int result = session.insert("com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper.insert", assetDeletionApplicationDO);
            session.commit();
            return result;
        }
    }

    @Override
    public int updateById(AssetDeletionApplicationDO assetDeletionApplicationDO) {
        try (SqlSession session = getSession()) {
            int result = session.update("com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper.updateById", assetDeletionApplicationDO);
            session.commit();
            return result;
        }
    }

    @Override
    public int deleteById(Long id) {
        try (SqlSession session = getSession()) {
            int result = session.delete("com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper.deleteById", id);
            session.commit();
            return result;
        }
    }

    @Override
    public int delete(AssetDeletionApplicationQuery query) {
        try (SqlSession session = getSession()) {
            int result = session.delete("com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper.delete", query);
            session.commit();
            return result;
        }
    }
}
