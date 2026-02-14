package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.domain.usage.entity.UsageApply;
import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import com.xuanjiao.domain.usage.repository.UsageApplyRepository;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.xuanjiao.common.ConvertUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UsageApplyRepositoryImpl implements UsageApplyRepository {

    private static final Logger logger = LoggerFactory.getLogger(UsageApplyRepositoryImpl.class);

    @Resource
    private UsageApplyMapper usageApplyMapper;

    @Resource
    private UsageApplyAssetMapper usageApplyAssetMapper;

    @Override
    public UsageApply findById(Long id) {
        logger.info("UsageApplyRepository.findById - 查询id: {}", id);
        UsageApplyDO usageApplyDO = usageApplyMapper.selectById(id);
        logger.info("UsageApplyRepository.findById - 查询结果: {}", usageApplyDO != null ? "id=" + usageApplyDO.getId() + ", title=" + usageApplyDO.getTitle() + ", deleted=" + usageApplyDO.getDeleted() : "null");
        UsageApply usageApply = convert(usageApplyDO);
        if (usageApply != null) {
            loadAssets(usageApply);
        }
        return usageApply;
    }

    @Override
    public List<UsageApply> findByCondition(String status, int offset, int limit) {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setStatus(status);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset(offset);
        query.setLimit(limit);

        List<UsageApplyDO> list = usageApplyMapper.selectList(query);
        return list.stream()
                .map(this::convert)
                .peek(this::loadAssets)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(String status) {
        UsageApplyQuery query = new UsageApplyQuery();
        if (StringUtils.hasText(status)) {
            query.setStatus(status);
        }
        return usageApplyMapper.selectCount(query);
    }

    @Override
    public List<UsageApply> findByUserId(Long userId, int offset, int limit) {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset(offset);
        query.setLimit(limit);

        List<UsageApplyDO> list = usageApplyMapper.selectList(query);
        return list.stream()
                .map(this::convert)
                .peek(this::loadAssets)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsageApply> findDraftsByUserId(Long userId, int offset, int limit) {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        query.setDraft(1);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset(offset);
        query.setLimit(limit);

        List<UsageApplyDO> list = usageApplyMapper.selectList(query);
        return list.stream()
                .map(this::convert)
                .peek(this::loadAssets)
                .collect(Collectors.toList());
    }

    @Override
    public long countByUserId(Long userId) {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        return usageApplyMapper.selectCount(query);
    }

    @Override
    public long countDraftsByUserId(Long userId) {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        query.setDraft(1);
        return usageApplyMapper.selectCount(query);
    }

    @Override
    public void save(UsageApply usageApply) {
        UsageApplyDO usageApplyDO = new UsageApplyDO();
        ConvertUtils.copyProperties(usageApply, usageApplyDO);
        // 显式设置deleted字段，确保不为NULL
        usageApplyDO.setDeleted(0);
        usageApplyMapper.insert(usageApplyDO);
        usageApply.setId(usageApplyDO.getId());
    }

    @Override
    public void update(UsageApply usageApply) {
        UsageApplyDO usageApplyDO = new UsageApplyDO();
        ConvertUtils.copyProperties(usageApply, usageApplyDO);
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
        ConvertUtils.copyProperties(usageApplyDO, usageApply);
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
