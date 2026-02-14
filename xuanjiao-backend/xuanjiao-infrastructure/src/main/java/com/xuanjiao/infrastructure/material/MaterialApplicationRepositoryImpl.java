package com.xuanjiao.infrastructure.material;

import com.xuanjiao.domain.material.entity.MaterialApplication;
import com.xuanjiao.domain.material.repository.MaterialApplicationRepository;
import com.xuanjiao.infrastructure.dataobject.MaterialApplicationDO;
import com.xuanjiao.common.ConvertUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class MaterialApplicationRepositoryImpl implements MaterialApplicationRepository {

    @Resource
    private MaterialApplicationMapper materialApplicationMapper;

    @Override
    public MaterialApplication findById(Long id) {
        MaterialApplicationDO applicationDO = materialApplicationMapper.selectById(id);
        return convert(applicationDO);
    }

    @Override
    public MaterialApplication save(MaterialApplication application) {
        MaterialApplicationDO applicationDO = new MaterialApplicationDO();
        ConvertUtils.copyProperties(application, applicationDO);
        materialApplicationMapper.insert(applicationDO);
        application.setId(applicationDO.getId());
        return application;
    }

    @Override
    public MaterialApplication update(MaterialApplication application) {
        MaterialApplicationDO applicationDO = new MaterialApplicationDO();
        ConvertUtils.copyProperties(application, applicationDO);
        materialApplicationMapper.updateById(applicationDO);
        return application;
    }

    @Override
    public void deleteById(Long id) {
        materialApplicationMapper.deleteById(id);
    }

    @Override
    public List<MaterialApplication> findByApplicantAndStatus(Long applicantId, String status) {
        MaterialApplicationQuery query = new MaterialApplicationQuery();
        query.setApplicantId(applicantId);
        if (StringUtils.hasText(status)) {
            query.setStatus(status);
        }
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        List<MaterialApplicationDO> list = materialApplicationMapper.selectList(query);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<MaterialApplication> findByApplicant(Long applicantId, int offset, int limit) {
        MaterialApplicationQuery query = new MaterialApplicationQuery();
        query.setApplicantId(applicantId);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset(offset);
        query.setLimit(limit);
        List<MaterialApplicationDO> list = materialApplicationMapper.selectList(query);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public long countByApplicant(Long applicantId) {
        MaterialApplicationQuery query = new MaterialApplicationQuery();
        query.setApplicantId(applicantId);
        return materialApplicationMapper.selectCount(query);
    }

    private MaterialApplication convert(MaterialApplicationDO applicationDO) {
        if (applicationDO == null) return null;
        MaterialApplication application = new MaterialApplication();
        ConvertUtils.copyProperties(applicationDO, application);
        return application;
    }
}
