package com.xuanjiao.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.domain.asset.entity.Asset;
import com.xuanjiao.domain.asset.repository.AssetRepository;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.mapper.AssetMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AssetRepositoryImpl implements AssetRepository {

    @Resource
    private AssetMapper assetMapper;

    @Override
    public Asset findById(Long id) {
        AssetDO assetDO = assetMapper.selectById(id);
        return convert(assetDO);
    }

    @Override
    public Asset findByMd5(String md5) {
        // 使用自定义SQL查询，包含已删除的记录
        AssetDO assetDO = assetMapper.selectByMd5IncludeDeleted(md5);
        return convert(assetDO);
    }

    @Override
    public List<Asset> findByCondition(String name, String type, String status, int offset, int limit) {
        LambdaQueryWrapper<AssetDO> wrapper = buildQueryWrapper(name, type, status);
        Page<AssetDO> page = new Page<>(offset / limit + 1, limit);
        Page<AssetDO> result = assetMapper.selectPage(page, wrapper);
        return result.getRecords().stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public long countByCondition(String name, String type, String status) {
        LambdaQueryWrapper<AssetDO> wrapper = buildQueryWrapper(name, type, status);
        return assetMapper.selectCount(wrapper);
    }

    @Override
    public void save(Asset asset) {
        AssetDO assetDO = new AssetDO();
        BeanUtils.copyProperties(asset, assetDO);
        assetMapper.insert(assetDO);
        asset.setId(assetDO.getId());
    }

    @Override
    public void update(Asset asset) {
        AssetDO assetDO = new AssetDO();
        BeanUtils.copyProperties(asset, assetDO);
        assetMapper.updateById(assetDO);
    }

    @Override
    public void deleteById(Long id) {
        assetMapper.deleteById(id);
    }

    private LambdaQueryWrapper<AssetDO> buildQueryWrapper(String name, String type, String status) {
        LambdaQueryWrapper<AssetDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(AssetDO::getName, name);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(AssetDO::getType, type);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AssetDO::getStatus, status);
        }
        wrapper.orderByDesc(AssetDO::getCreateTime);
        return wrapper;
    }

    private Asset convert(AssetDO assetDO) {
        if (assetDO == null) return null;
        Asset asset = new Asset();
        BeanUtils.copyProperties(assetDO, asset);
        return asset;
    }
}
