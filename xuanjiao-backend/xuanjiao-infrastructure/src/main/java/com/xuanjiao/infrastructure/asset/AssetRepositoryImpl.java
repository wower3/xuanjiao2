package com.xuanjiao.infrastructure.asset;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.domain.asset.entity.Asset;
import com.xuanjiao.domain.asset.repository.AssetRepository;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.asset.AssetMapper;
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
        AssetQuery query = new AssetQuery();
        query.setName(name);
        query.setType(type);
        query.setStatus(status);
        query.setOffset(offset);
        query.setLimit(limit);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");

        List<AssetDO> list = assetMapper.selectList(query);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<Asset> findByApplicationId(Long applicationId) {
        List<AssetDO> list = assetMapper.selectByApplicationId(applicationId);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<Asset> findByStatusList(String name, String type, List<String> statusList, int offset, int limit) {
        AssetQuery query = new AssetQuery();
        query.setName(name);
        query.setType(type);
        query.setStatusList(statusList);
        query.setOffset(offset);
        query.setLimit(limit);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");

        List<AssetDO> list = assetMapper.selectList(query);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public long countByStatusList(String name, String type, List<String> statusList) {
        AssetQuery query = new AssetQuery();
        query.setName(name);
        query.setType(type);
        query.setStatusList(statusList);

        return assetMapper.selectCount(query);
    }

    @Override
    public long countByCondition(String name, String type, String status) {
        AssetQuery query = new AssetQuery();
        query.setName(name);
        query.setType(type);
        query.setStatus(status);

        return assetMapper.selectCount(query);
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

    private Asset convert(AssetDO assetDO) {
        if (assetDO == null) return null;
        Asset asset = new Asset();
        BeanUtils.copyProperties(assetDO, asset);
        return asset;
    }
}
