package com.xuanjiao.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.domain.material.entity.MaterialApplication;
import com.xuanjiao.domain.material.repository.MaterialApplicationRepository;
import com.xuanjiao.infrastructure.dataobject.MaterialApplicationDO;
import com.xuanjiao.infrastructure.mapper.MaterialApplicationMapper;
import org.springframework.beans.BeanUtils;
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
        BeanUtils.copyProperties(application, applicationDO);
        materialApplicationMapper.insert(applicationDO);
        application.setId(applicationDO.getId());
        return application;
    }

    @Override
    public MaterialApplication update(MaterialApplication application) {
        MaterialApplicationDO applicationDO = new MaterialApplicationDO();
        BeanUtils.copyProperties(application, applicationDO);
        materialApplicationMapper.updateById(applicationDO);
        return application;
    }

    @Override
    public void deleteById(Long id) {
        materialApplicationMapper.deleteById(id);
    }

    @Override
    public List<MaterialApplication> findByApplicantAndStatus(Long applicantId, String status) {
        LambdaQueryWrapper<MaterialApplicationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialApplicationDO::getApplicantId, applicantId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(MaterialApplicationDO::getStatus, status);
        }
        wrapper.orderByDesc(MaterialApplicationDO::getCreateTime);
        List<MaterialApplicationDO> list = materialApplicationMapper.selectList(wrapper);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<MaterialApplication> findByApplicant(Long applicantId, int offset, int limit) {
        LambdaQueryWrapper<MaterialApplicationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialApplicationDO::getApplicantId, applicantId);
        wrapper.orderByDesc(MaterialApplicationDO::getCreateTime);
        wrapper.last("LIMIT " + offset + ", " + limit);
        List<MaterialApplicationDO> list = materialApplicationMapper.selectList(wrapper);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public long countByApplicant(Long applicantId) {
        LambdaQueryWrapper<MaterialApplicationDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MaterialApplicationDO::getApplicantId, applicantId);
        return materialApplicationMapper.selectCount(wrapper);
    }

    private MaterialApplication convert(MaterialApplicationDO applicationDO) {
        if (applicationDO == null) return null;
        MaterialApplication application = new MaterialApplication();
        BeanUtils.copyProperties(applicationDO, application);
        return application;
    }
}
