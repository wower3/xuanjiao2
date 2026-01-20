package com.xuanjiao.app.material.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.client.dto.AssetDTO;
import com.xuanjiao.client.dto.MaterialApplicationCmd;
import com.xuanjiao.client.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.domain.material.entity.MaterialApplication;
import com.xuanjiao.domain.material.repository.MaterialApplicationRepository;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.asset.AssetTagMapper;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaterialApplicationServiceImpl implements MaterialApplicationService {

    @Autowired
    private MaterialApplicationRepository materialApplicationRepository;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private AssetTagMapper assetTagMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DeptMapper deptMapper;

    @Override
    @Transactional
    public MaterialApplicationDTO create(MaterialApplicationCmd cmd, Long userId) {
        // 获取当前用户信息作为默认维护人
        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        MaterialApplication application = new MaterialApplication();
        application.setTitle(cmd.getTitle());
        application.setApplicantId(userId);
        application.setMaintainerId(cmd.getMaintainerId() != null ? cmd.getMaintainerId() : userId);
        application.setDeptId(cmd.getDeptId() != null ? cmd.getDeptId() : currentUser.getDeptId());
        application.setGuaranteeDeclaration(cmd.getGuaranteeDeclaration() != null ? cmd.getGuaranteeDeclaration() : 0);
        application.setStatus("DRAFT");
        application.setCreateTime(LocalDateTime.now());

        MaterialApplication saved = materialApplicationRepository.save(application);
        return convert(saved);
    }

    @Override
    @Transactional
    public MaterialApplicationDTO update(Long id, MaterialApplicationCmd cmd, Long userId) {
        MaterialApplication application = materialApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿状态可以修改，且只能修改自己的申请单
        if (!"DRAFT".equals(application.getStatus())) {
            throw new RuntimeException("只有草稿状态可以修改");
        }
        if (!application.getApplicantId().equals(userId)) {
            throw new RuntimeException("只能修改自己的申请单");
        }

        application.setTitle(cmd.getTitle());
        if (cmd.getMaintainerId() != null) {
            application.setMaintainerId(cmd.getMaintainerId());
        }
        if (cmd.getDeptId() != null) {
            application.setDeptId(cmd.getDeptId());
        }
        if (cmd.getGuaranteeDeclaration() != null) {
            application.setGuaranteeDeclaration(cmd.getGuaranteeDeclaration());
        }

        MaterialApplication updated = materialApplicationRepository.update(application);
        return convert(updated);
    }

    @Override
    @Transactional
    public Long submit(Long id, Long workflowId, Long userId) {
        MaterialApplication application = materialApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿状态可以提交，且只能提交自己的申请单
        if (!"DRAFT".equals(application.getStatus())) {
            throw new RuntimeException("只有草稿状态可以提交");
        }
        if (!application.getApplicantId().equals(userId)) {
            throw new RuntimeException("只能提交自己的申请单");
        }

        // 检查是否有至少一个文件
        LambdaQueryWrapper<com.xuanjiao.infrastructure.dataobject.AssetDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.xuanjiao.infrastructure.dataobject.AssetDO::getApplicationId, id);
        Long count = assetMapper.selectCount(wrapper);
        if (count == 0) {
            throw new RuntimeException("请至少上传一个素材文件");
        }

        // Update asset status from DRAFT to PENDING
        assetService.updateStatusByApplicationId(id, "PENDING");

        application.setWorkflowId(workflowId);
        application.setStatus("PENDING");
        materialApplicationRepository.update(application);

        // 启动审批流程
        Long instanceId = workflowEngineService.startProcess(workflowId, "MATERIAL_ENTRY", id, userId);
        return instanceId;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        MaterialApplication application = materialApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿状态可以删除，且只能删除自己的申请单
        if (!"DRAFT".equals(application.getStatus())) {
            throw new RuntimeException("只有草稿状态可以删除");
        }
        if (!application.getApplicantId().equals(userId)) {
            throw new RuntimeException("只能删除自己的申请单");
        }

        // Cascade delete: First delete all associated assets
        LambdaQueryWrapper<AssetDO> assetWrapper = new LambdaQueryWrapper<>();
        assetWrapper.eq(AssetDO::getApplicationId, id);
        List<AssetDO> assets = assetMapper.selectList(assetWrapper);

        // Delete asset-tag associations first (foreign key constraints)
        for (AssetDO asset : assets) {
            LambdaQueryWrapper<com.xuanjiao.infrastructure.dataobject.AssetTagDO> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getAssetId, asset.getId());
            assetTagMapper.delete(tagWrapper);

            // Delete the asset file and database record
            assetMapper.deleteById(asset.getId());
        }

        // Finally delete the application
        materialApplicationRepository.deleteById(id);
    }

    @Override
    public MaterialApplicationDTO getById(Long id) {
        MaterialApplication application = materialApplicationRepository.findById(id);
        return convert(application);
    }

    @Override
    public PageResult<MaterialApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize) {
        List<MaterialApplication> list = materialApplicationRepository.findByApplicant(userId, (pageNum - 1) * pageSize, pageSize);
        // 过滤出草稿状态
        list = list.stream().filter(app -> "DRAFT".equals(app.getStatus())).collect(Collectors.toList());

        long total = materialApplicationRepository.countByApplicant(userId);

        List<MaterialApplicationDTO> dtoList = list.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<MaterialApplicationDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize) {
        List<MaterialApplication> list = materialApplicationRepository.findByApplicant(userId, (pageNum - 1) * pageSize, pageSize);
        long total = materialApplicationRepository.countByApplicant(userId);

        List<MaterialApplicationDTO> dtoList = list.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    private MaterialApplicationDTO convert(MaterialApplication application) {
        if (application == null) return null;
        MaterialApplicationDTO dto = new MaterialApplicationDTO();
        BeanUtils.copyProperties(application, dto);

        // 填充申请人名称
        if (application.getApplicantId() != null) {
            UserDO user = userMapper.selectById(application.getApplicantId());
            if (user != null) {
                dto.setApplicantName(user.getRealName());
            }
        }

        // 填充维护人名称
        if (application.getMaintainerId() != null) {
            UserDO maintainer = userMapper.selectById(application.getMaintainerId());
            if (maintainer != null) {
                dto.setMaintainerName(maintainer.getRealName());
            }
        }

        // 填充部门名称
        if (application.getDeptId() != null) {
            DeptDO dept = deptMapper.selectById(application.getDeptId());
            if (dept != null) {
                dto.setDeptName(dept.getName());
            }
        }

        // 填充关联的素材文件
        if (application.getId() != null) {
            LambdaQueryWrapper<AssetDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AssetDO::getApplicationId, application.getId());
            List<AssetDO> assets = assetMapper.selectList(wrapper);
            List<AssetDTO> assetDTOs = assets.stream().map(this::convertAsset).collect(Collectors.toList());
            dto.setAssets(assetDTOs);
        }

        return dto;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        MaterialApplication application = materialApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }
        application.setStatus(status);
        materialApplicationRepository.update(application);
    }

    private AssetDTO convertAsset(AssetDO assetDO) {
        if (assetDO == null) return null;
        AssetDTO dto = new AssetDTO();
        BeanUtils.copyProperties(assetDO, dto);

        // 填充标签
        LambdaQueryWrapper<com.xuanjiao.infrastructure.dataobject.AssetTagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getAssetId, assetDO.getId());
        List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> assetTags = assetTagMapper.selectList(wrapper);

        if (!assetTags.isEmpty()) {
            List<Long> tagIds = assetTags.stream()
                    .map(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getTagId)
                    .collect(Collectors.toList());
            if (!tagIds.isEmpty()) {
                List<TagDO> tags = tagMapper.selectBatchIds(tagIds);
                dto.setTags(tags.stream().map(tag -> {
                    com.xuanjiao.client.dto.TagDTO tagDTO = new com.xuanjiao.client.dto.TagDTO();
                    BeanUtils.copyProperties(tag, tagDTO);
                    return tagDTO;
                }).collect(Collectors.toList()));
            }
        }

        return dto;
    }
}
