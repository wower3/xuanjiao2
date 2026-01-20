package com.xuanjiao.infrastructure.usage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.domain.usage.entity.UsageApply;
import com.xuanjiao.domain.usage.repository.UsageApplyRepository;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import com.xuanjiao.infrastructure.usage.UsageApplyMapper;
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

    @Override
    public UsageApply findById(Long id) {
        UsageApplyDO usageApplyDO = usageApplyMapper.selectById(id);
        return convert(usageApplyDO);
    }

    @Override
    public UsageApply findByAssetAndUser(Long assetId, Long userId, String status) {
        UsageApplyDO usageApplyDO = usageApplyMapper.selectByAssetAndUser(assetId, userId, status);
        return convert(usageApplyDO);
    }

    @Override
    public List<UsageApply> findByCondition(Long assetId, String status, int offset, int limit) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = buildQueryWrapper(assetId, status);
        Page<UsageApplyDO> page = new Page<>(offset / limit + 1, limit);
        Page<UsageApplyDO> result = usageApplyMapper.selectPage(page, wrapper);
        return result.getRecords().stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public long countByCondition(Long assetId, String status) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = buildQueryWrapper(assetId, status);
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
        usageApplyMapper.deleteById(id);
    }

    private LambdaQueryWrapper<UsageApplyDO> buildQueryWrapper(Long assetId, String status) {
        LambdaQueryWrapper<UsageApplyDO> wrapper = new LambdaQueryWrapper<>();
        if (assetId != null) {
            wrapper.eq(UsageApplyDO::getAssetId, assetId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(UsageApplyDO::getStatus, status);
        }
        wrapper.orderByDesc(UsageApplyDO::getCreateTime);
        return wrapper;
    }

    private UsageApply convert(UsageApplyDO usageApplyDO) {
        if (usageApplyDO == null) return null;
        UsageApply usageApply = new UsageApply();
        BeanUtils.copyProperties(usageApplyDO, usageApply);
        return usageApply;
    }
}
