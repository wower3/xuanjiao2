package com.xuanjiao.infrastructure.deletion.repository;

import com.xuanjiao.domain.deletion.entity.AssetDeletionApplication;
import com.xuanjiao.domain.deletion.repository.AssetDeletionApplicationRepository;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO;
import com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationQuery;
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
        AssetDeletionApplicationQuery query = new AssetDeletionApplicationQuery();
        query.setApplicantId(applicantId);
        if (StringUtils.hasText(status)) {
            query.setStatus(status);
        }
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        List<AssetDeletionApplicationDO> list = assetDeletionApplicationMapper.selectList(query);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    private AssetDeletionApplication convert(AssetDeletionApplicationDO applicationDO) {
        if (applicationDO == null) return null;
        AssetDeletionApplication application = new AssetDeletionApplication();
        BeanUtils.copyProperties(applicationDO, application);
        return application;
    }
}
