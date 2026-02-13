package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.domain.usage.entity.UsageApply;
import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import com.xuanjiao.domain.usage.repository.UsageApplyRepository;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import com.xuanjiao.infrastructure.dataobject.UsageApplyWithUserDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
    public List<UsageApply> findByUserId(Long userId, int offset, int limit) {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");

        // 使用 MyBatis-Plus 分页
        Page<UsageApplyWithUserDO> page = new Page<>(offset / limit + 1, limit);
        IPage<UsageApplyWithUserDO> pageResult = usageApplyMapper.selectPageWithUser(page, query);
        return pageResult.getRecords().stream()
                .map(this::convertFromDetailDO)
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

        // 使用 MyBatis-Plus 分页
        Page<UsageApplyWithUserDO> page = new Page<>(offset / limit + 1, limit);
        IPage<UsageApplyWithUserDO> pageResult = usageApplyMapper.selectPageWithUser(page, query);
        return pageResult.getRecords().stream()
                .map(this::convertFromDetailDO)
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
        BeanUtils.copyProperties(usageApply, usageApplyDO);
        // 显式设置deleted字段，确保不为NULL
        usageApplyDO.setDeleted(0);
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

    /**
     * 将 UsageApplyWithUserDO 转换为 UsageApply
     * 用于 JOIN 查询结果，避免 N+1 问题
     *
     * @param detailDO 使用申请详情数据对象
     * @return 使用申请实体
     */
    private UsageApply convertFromDetailDO(UsageApplyWithUserDO detailDO) {
        if (detailDO == null) return null;
        UsageApply usageApply = new UsageApply();
        usageApply.setId(detailDO.getId());
        usageApply.setTitle(detailDO.getTitle());
        usageApply.setUserId(detailDO.getUserId());
        usageApply.setDeptId(detailDO.getDeptId());
        usageApply.setWorkflowId(detailDO.getWorkflowId());
        usageApply.setStatus(detailDO.getStatus());
        usageApply.setDraft(detailDO.getDraft());
        usageApply.setCreateTime(detailDO.getCreateTime());
        usageApply.setUpdateTime(detailDO.getUpdateTime());

        // 设置用户名称（从 JOIN 结果中获取）
        if (detailDO.getRealName() != null) {
            usageApply.setUsername(detailDO.getRealName());
        }

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
