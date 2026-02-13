package com.xuanjiao.infrastructure.material;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.domain.material.entity.MaterialApplication;
import com.xuanjiao.domain.material.repository.MaterialApplicationRepository;
import com.xuanjiao.infrastructure.dataobject.MaterialApplicationDO;
import com.xuanjiao.infrastructure.dataobject.MaterialApplicationWithDetailsDO;
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

        // 验证 ID 是否正确生成
        Long generatedId = applicationDO.getId();
        if (generatedId == null) {
            throw new RuntimeException("插入申请单失败：未获取到生成的ID");
        }
        application.setId(generatedId);
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
        MaterialApplicationQuery query = new MaterialApplicationQuery();
        query.setApplicantId(applicantId);
        if (StringUtils.hasText(status)) {
            query.setStatus(status);
        }
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        // 使用 MyBatis-Plus 分页查询全部
        Page<MaterialApplicationDO> page = new Page<>(1, Integer.MAX_VALUE);
        IPage<MaterialApplicationDO> pageResult = materialApplicationMapper.selectPage(page, query);
        return pageResult.getRecords().stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<MaterialApplication> findByApplicant(Long applicantId, int offset, int limit) {
        MaterialApplicationQuery query = new MaterialApplicationQuery();
        query.setApplicantId(applicantId);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        // 使用 MyBatis-Plus 分页
        Page<MaterialApplicationWithDetailsDO> page = new Page<>(offset / limit + 1, limit);
        IPage<MaterialApplicationWithDetailsDO> pageResult = materialApplicationMapper.selectPageWithDetails(page, query);
        return pageResult.getRecords().stream().map(this::convertFromDetailDO).collect(Collectors.toList());
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
        BeanUtils.copyProperties(applicationDO, application);
        return application;
    }

    /**
     * 将 MaterialApplicationWithDetailsDO 转换为 MaterialApplication
     * 用于 JOIN 查询结果，避免 N+1 问题
     *
     * @param detailDO 素材录入申请详情数据对象
     * @return 素材录入申请实体
     */
    private MaterialApplication convertFromDetailDO(MaterialApplicationWithDetailsDO detailDO) {
        if (detailDO == null) return null;
        MaterialApplication application = new MaterialApplication();
        application.setId(detailDO.getId());
        application.setTitle(detailDO.getTitle());
        application.setApplicantId(detailDO.getApplicantId());
        application.setMaintainerId(detailDO.getMaintainerId());
        application.setDeptId(detailDO.getDeptId());
        application.setWorkflowId(detailDO.getWorkflowId());
        application.setStatus(detailDO.getStatus());
        application.setGuaranteeDeclaration(detailDO.getGuaranteeDeclaration());
        application.setCreateTime(detailDO.getCreateTime());
        application.setUpdateTime(detailDO.getUpdateTime());

        // 设置用户名称（从 JOIN 结果中获取）
        if (detailDO.getApplicantName() != null) {
            application.setApplicantName(detailDO.getApplicantName());
        }
        if (detailDO.getMaintainerName() != null) {
            application.setMaintainerName(detailDO.getMaintainerName());
        }
        if (detailDO.getDeptName() != null) {
            application.setDeptName(detailDO.getDeptName());
        }

        return application;
    }
}
