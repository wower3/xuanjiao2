package com.xuanjiao.infrastructure.usage;

import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import com.xuanjiao.domain.usage.repository.UsageApplyAssetRepository;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 素材使用申请-素材关联Repository实现
 */
@Repository
public class UsageApplyAssetRepositoryImpl implements UsageApplyAssetRepository {

    @Resource
    private UsageApplyAssetMapper usageApplyAssetMapper;

    @Override
    public List<UsageApplyAsset> findByUsageApplyId(Long usageApplyId) {
        List<UsageApplyAssetDO> doList = usageApplyAssetMapper.findByUsageApplyIdWithAsset(usageApplyId);
        return doList.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<UsageApplyAsset> findByAssetId(Long assetId) {
        List<UsageApplyAssetDO> doList = usageApplyAssetMapper.findByAssetId(assetId);
        return doList.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public UsageApplyAsset findByUsageApplyIdAndAssetId(Long usageApplyId, Long assetId) {
        UsageApplyAssetDO usageApplyAssetDO = usageApplyAssetMapper.findByUsageApplyIdAndAssetId(usageApplyId, assetId);
        return convert(usageApplyAssetDO);
    }

    @Override
    public void batchSave(List<UsageApplyAsset> usageApplyAssets) {
        if (CollectionUtils.isEmpty(usageApplyAssets)) {
            return;
        }
        for (UsageApplyAsset usageApplyAsset : usageApplyAssets) {
            UsageApplyAssetDO usageApplyAssetDO = convertToDO(usageApplyAsset);
            usageApplyAssetMapper.insert(usageApplyAssetDO);
            usageApplyAsset.setId(usageApplyAssetDO.getId());
        }
    }

    @Override
    public void deleteByUsageApplyId(Long usageApplyId) {
        UsageApplyAssetQuery query = new UsageApplyAssetQuery();
        query.setUsageApplyId(usageApplyId);
        usageApplyAssetMapper.delete(query);
    }

    @Override
    public void deleteById(Long id) {
        usageApplyAssetMapper.deleteById(id);
    }

    @Override
    public boolean existsByAssetIdAndUsageApplyId(Long assetId, Long usageApplyId) {
        UsageApplyAssetDO result = usageApplyAssetMapper.findByUsageApplyIdAndAssetId(usageApplyId, assetId);
        return result != null;
    }

    private UsageApplyAsset convert(UsageApplyAssetDO usageApplyAssetDO) {
        if (usageApplyAssetDO == null) return null;
        UsageApplyAsset usageApplyAsset = new UsageApplyAsset();
        BeanUtils.copyProperties(usageApplyAssetDO, usageApplyAsset);
        return usageApplyAsset;
    }

    private UsageApplyAssetDO convertToDO(UsageApplyAsset usageApplyAsset) {
        UsageApplyAssetDO usageApplyAssetDO = new UsageApplyAssetDO();
        BeanUtils.copyProperties(usageApplyAsset, usageApplyAssetDO);
        return usageApplyAssetDO;
    }
}
