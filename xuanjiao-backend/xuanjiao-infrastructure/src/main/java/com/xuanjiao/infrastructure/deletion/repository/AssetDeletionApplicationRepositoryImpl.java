package com.xuanjiao.infrastructure.deletion.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuanjiao.domain.deletion.entity.AssetDeletionApplication;
import com.xuanjiao.domain.deletion.repository.AssetDeletionApplicationRepository;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO;
import com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 素材删除申请Repository实现
 */
@Repository
public class AssetDeletionApplicationRepositoryImpl implements AssetDeletionApplicationRepository {

    @Resource
    private AssetDeletionApplicationMapper assetDeletionApplicationMapper;

    @Override
    public AssetDeletionApplication findById(Long id) {
        AssetDeletionApplicationDO applicationDO = assetDeletionApplicationMapper.selectById(id);
        return convert(applicationDO);
    }

    @Override
    public AssetDeletionApplication save(AssetDeletionApplication application) {
        AssetDeletionApplicationDO applicationDO = new AssetDeletionApplicationDO();
        BeanUtils.copyProperties(application, applicationDO);
        assetDeletionApplicationMapper.insert(applicationDO);
        application.setId(applicationDO.getId());
        return application;
    }

    @Override
    public AssetDeletionApplication update(AssetDeletionApplication application) {
        AssetDeletionApplicationDO applicationDO = new AssetDeletionApplicationDO();
        BeanUtils.copyProperties(application, applicationDO);
        assetDeletionApplicationMapper.updateById(applicationDO);
        return application;
    }

    @Override
    public void deleteById(Long id) {
        assetDeletionApplicationMapper.deleteById(id);
    }

    @Override
    public List<AssetDeletionApplication> findByApplicantAndStatus(Long applicantId, String status) {
        QueryWrapper<AssetDeletionApplicationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("applicant_id", applicantId);
        if (StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        List<AssetDeletionApplicationDO> list = assetDeletionApplicationMapper.selectList(wrapper);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<AssetDeletionApplication> findByApplicant(Long applicantId, int offset, int limit) {
        QueryWrapper<AssetDeletionApplicationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("applicant_id", applicantId);
        wrapper.orderByDesc("create_time");
        wrapper.last("LIMIT " + offset + ", " + limit);
        List<AssetDeletionApplicationDO> list = assetDeletionApplicationMapper.selectList(wrapper);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public long countByApplicant(Long applicantId) {
        QueryWrapper<AssetDeletionApplicationDO> wrapper = new QueryWrapper<>();
        wrapper.eq("applicant_id", applicantId);
        return assetDeletionApplicationMapper.selectCount(wrapper);
    }

    private AssetDeletionApplication convert(AssetDeletionApplicationDO applicationDO) {
        if (applicationDO == null) return null;
        AssetDeletionApplication application = new AssetDeletionApplication();
        BeanUtils.copyProperties(applicationDO, application);
        return application;
    }
}
