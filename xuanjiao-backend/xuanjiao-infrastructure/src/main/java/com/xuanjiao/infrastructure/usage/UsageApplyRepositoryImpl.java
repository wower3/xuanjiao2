package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.domain.usage.entity.UsageApply;
import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import com.xuanjiao.domain.usage.repository.UsageApplyRepository;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UsageApplyRepositoryImpl implements UsageApplyRepository {

    @Resource
    private UsageApplyMapper usageApplyMapper;

    @Resource
    private UsageApplyAssetMapper usageApplyAssetMapper;

    @Override
    public UsageApply findById(Long id) {
        UsageApplyDO usageApplyDO = usageApplyMapper.selectById(id);
        UsageApply usageApply = convert(usageApplyDO);
        if (usageApply != null) {
            loadAssets(usageApply);
        }
        return usageApply;
    }

    @Override
    public List<UsageApply> findByCondition(String status, int offset, int limit) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(UsageApplyDO::getStatus, status);
        }
        wrapper.orderByDesc(UsageApplyDO::getCreateTime);
        Page<UsageApplyDO> page = new Page<>(offset / limit + 1, limit);
        Page<UsageApplyDO> result = usageApplyMapper.selectPage(page, wrapper);
        return result.getRecords().stream()
                .map(this::convert)
                .peek(this::loadAssets)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(String status) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(UsageApplyDO::getStatus, status);
        }
        return usageApplyMapper.selectCount(wrapper);
    }

    @Override
    public List<UsageApply> findByUserId(Long userId, int offset, int limit) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageApplyDO::getUserId, userId)
                .orderByDesc(UsageApplyDO::getCreateTime);
        Page<UsageApplyDO> page = new Page<>(offset / limit + 1, limit);
        Page<UsageApplyDO> result = usageApplyMapper.selectPage(page, wrapper);
        return result.getRecords().stream()
                .map(this::convert)
                .peek(this::loadAssets)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsageApply> findDraftsByUserId(Long userId, int offset, int limit) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageApplyDO::getUserId, userId)
                .eq(UsageApplyDO::getDraft, 1)
                .orderByDesc(UsageApplyDO::getCreateTime);
        Page<UsageApplyDO> page = new Page<>(offset / limit + 1, limit);
        Page<UsageApplyDO> result = usageApplyMapper.selectPage(page, wrapper);
        return result.getRecords().stream()
                .map(this::convert)
                .peek(this::loadAssets)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUserId(Long userId) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageApplyDO::getUserId, userId);
        return usageApplyMapper.selectCount(wrapper);
    }

    @Override
    public long countDraftsByUserId(Long userId) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageApplyDO::getUserId, userId)
                .eq(UsageApplyDO::getDraft, 1);
        return usageApplyMapper.selectCount(wrapper);
    }

    @Override
    public void save(UsageApply usageApply) {
        UsageApplyDO usageApplyDO = new UsageApplyDO();
        BeanUtils.copyProperties(usageApply, usageApplyDO);
        usageApplyMapper.insert(usageApplyDO);
        usageApply.setId(usageApplyDO.getId());
    }

    @Override
    public void update(UsageApply usageApply) {
        UsageApplyDO usageApplyDO = new UsageApplyDO();
        BeanUtils.copyProperties(usageApply, usageApplyDO);
        usageApplyMapper.updateById(usageApplyDO);
    }

    @Override
    public void deleteById(Long id) {
        // 删除申请单时，先删除中间表的关联数据
        usageApplyAssetMapper.deleteByUsageApplyId(id);
        usageApplyMapper.deleteById(id);
    }

    private UsageApply convert(UsageApplyDO usageApplyDO) {
        if (usageApplyDO == null) return null;
        UsageApply usageApply = new UsageApply();
        BeanUtils.copyProperties(usageApplyDO, usageApply);
        return usageApply;
    }

    private void loadAssets(UsageApply usageApply) {
        if (usageApply == null || usageApply.getId() == null) return;

        // 新架构：通过中间表查询关联的素材
        List<UsageApplyAssetDO> assetDOs = usageApplyAssetMapper.findByUsageApplyIdWithAsset(usageApply.getId());

        List<UsageApplyAsset> assets = assetDOs.stream().map(assetDO -> {
            UsageApplyAsset asset = new UsageApplyAsset();
            asset.setId(assetDO.getId());
            asset.setUsageApplyId(assetDO.getUsageApplyId());
            asset.setAssetId(assetDO.getAssetId());
            asset.setUsageDescription(assetDO.getUsageDescription());
            asset.setUsagePublishChannel(assetDO.getUsagePublishChannel());
            asset.setUsageIsSecondaryCreation(assetDO.getUsageIsSecondaryCreation());
            asset.setUsageAttachmentPath(assetDO.getUsageAttachmentPath());

            // 设置关联的素材信息
            asset.setAssetName(assetDO.getAssetName());
            asset.setAssetType(assetDO.getAssetType());
            asset.setAssetFilePath(assetDO.getAssetFilePath());
            asset.setAssetThumbnailPath(assetDO.getAssetThumbnailPath());
            asset.setAssetStatus(assetDO.getAssetStatus());
            return asset;
        }).collect(Collectors.toList());
        usageApply.setAssets(assets);
    }
}
