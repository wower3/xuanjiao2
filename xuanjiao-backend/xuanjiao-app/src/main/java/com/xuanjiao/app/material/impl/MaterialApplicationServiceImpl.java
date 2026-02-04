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
import com.xuanjiao.infrastructure.asset.AssetTagQuery;
import com.xuanjiao.infrastructure.asset.AssetQuery;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaterialApplicationServiceImpl implements MaterialApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialApplicationServiceImpl.class);

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
        application.setDeleted(0);

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

        // 只有草稿或已驳回状态可以提交，且只能提交自己的申请单
        if (!"DRAFT".equals(application.getStatus()) && !"REJECTED".equals(application.getStatus())) {
            throw new RuntimeException("只有草稿或已驳回状态可以提交");
        }
        if (!application.getApplicantId().equals(userId)) {
            throw new RuntimeException("只能提交自己的申请单");
        }

        // 检查是否有至少一个文件
        AssetQuery query = new AssetQuery();
        query.setApplicationId(id);
        Long count = assetMapper.selectCount(query);
        if (count == 0) {
            throw new RuntimeException("请至少上传一个素材文件");
        }

        // 更新素材状态：将所有关联素材的状态统一更新为PENDING
        // 注意：可能存在部分素材已经是PENDING状态的情况（如复制后添加新素材）
        assetService.updateStatusByApplicationId(id, "PENDING");

        // 再次验证所有素材状态是否正确
        AssetQuery verifyQuery = new AssetQuery();
        verifyQuery.setApplicationId(id);
        List<com.xuanjiao.infrastructure.dataobject.AssetDO> allAssets = assetMapper.selectList(verifyQuery);
        boolean allPending = allAssets.stream().allMatch(a -> "PENDING".equals(a.getStatus()));
        if (!allPending) {
            logger.warn("申请单{}存在非PENDING状态的素材，强制更新", id);
            assetService.updateStatusByApplicationId(id, "PENDING");
        }

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
        AssetQuery assetQuery = new AssetQuery();
        assetQuery.setApplicationId(id);
        List<AssetDO> assets = assetMapper.selectList(assetQuery);

        // Delete asset-tag associations first (foreign key constraints)
        for (AssetDO asset : assets) {
            AssetTagQuery tagQuery = new AssetTagQuery();
            tagQuery.setAssetId(asset.getId());
            assetTagMapper.delete(tagQuery);

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
    public PageResult<MaterialApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title) {
        List<MaterialApplication> list = materialApplicationRepository.findByApplicant(userId, (pageNum - 1) * pageSize, pageSize);
        // 过滤出草稿状态
        list = list.stream().filter(app -> "DRAFT".equals(app.getStatus())).collect(Collectors.toList());

        // 按标题筛选
        List<MaterialApplication> filteredList = list;
        if (title != null && !title.isEmpty()) {
            final String titleFilter = title;
            filteredList = list.stream()
                .filter(app -> app.getTitle() != null && app.getTitle().contains(titleFilter))
                .collect(Collectors.toList());
        }

        long total = materialApplicationRepository.countByApplicant(userId);

        List<MaterialApplicationDTO> dtoList = filteredList.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, (long) filteredList.size(), pageNum, pageSize);
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

        logger.info("MaterialApplication.convert - applicationId: {}, title: {}", application.getId(), application.getTitle());

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
            AssetQuery wrapper = new AssetQuery();
            wrapper.setApplicationId(application.getId());
            logger.info("MaterialApplication.convert - 查询assets，applicationId: {}", application.getId());
            List<AssetDO> assets = assetMapper.selectList(wrapper);
            logger.info("MaterialApplication.convert - 查询到 {} 条assets记录", assets.size());
            for (AssetDO assetDO : assets) {
                logger.info("MaterialApplication.convert - asset: id={}, name={}, applicationId={}",
                    assetDO.getId(), assetDO.getName(), assetDO.getApplicationId());
            }
            List<AssetDTO> assetDTOs = assets.stream().map(this::convertAsset).collect(Collectors.toList());
            dto.setAssets(assetDTOs);
            logger.info("MaterialApplication.convert - 设置后 dto.assets 数量: {}", dto.getAssets() != null ? dto.getAssets().size() : 0);
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
        AssetTagQuery tagQuery = new AssetTagQuery();
        tagQuery.setAssetId(assetDO.getId());
        List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> assetTags = assetTagMapper.selectList(tagQuery);

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

    @Override
    @Transactional
    public Long copyApplication(Long id, Long userId) {
        // 1. 获取原申请单
        MaterialApplication original = materialApplicationRepository.findById(id);
        if (original == null) {
            throw new RuntimeException("原申请单不存在");
        }

        // 2. 创建新申请单（草稿状态）
        MaterialApplication newApplication = new MaterialApplication();
        newApplication.setTitle(original.getTitle() + " - 副本");
        newApplication.setApplicantId(userId);
        newApplication.setMaintainerId(userId);
        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser != null) {
            newApplication.setDeptId(currentUser.getDeptId());
        }
        newApplication.setGuaranteeDeclaration(original.getGuaranteeDeclaration());
        newApplication.setStatus("DRAFT");
        newApplication.setCreateTime(LocalDateTime.now());
        newApplication.setDeleted(0);

        MaterialApplication saved = materialApplicationRepository.save(newApplication);

        // 3. 复制素材文件
        AssetQuery wrapper = new AssetQuery();
        wrapper.setApplicationId(id);
        List<AssetDO> originalAssets = assetMapper.selectList(wrapper);

        for (AssetDO originalAsset : originalAssets) {
            AssetDO newAsset = new AssetDO();
            BeanUtils.copyProperties(originalAsset, newAsset);
            newAsset.setId(null);
            newAsset.setApplicationId(saved.getId());
            newAsset.setStatus("DRAFT");
            newAsset.setCreateTime(LocalDateTime.now());
            newAsset.setUpdateTime(LocalDateTime.now());
            newAsset.setDeleted(0);

            // 复制文件到新路径
            if (originalAsset.getFilePath() != null) {
                try {
                    java.nio.file.Path sourcePath = java.nio.file.Paths.get(originalAsset.getFilePath());
                    if (java.nio.file.Files.exists(sourcePath)) {
                        // 生成新的文件名（添加时间戳避免冲突）
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String newFileName = timestamp + "_" + sourcePath.getFileName().toString();
                        java.nio.file.Path targetPath = sourcePath.resolveSibling(newFileName);
                        java.nio.file.Files.copy(sourcePath, targetPath);
                        newAsset.setFilePath(targetPath.toString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("复制文件失败: " + originalAsset.getName(), e);
                }
            }

            // 复制缩略图
            if (originalAsset.getThumbnailPath() != null) {
                try {
                    java.nio.file.Path sourceThumb = java.nio.file.Paths.get(originalAsset.getThumbnailPath());
                    if (java.nio.file.Files.exists(sourceThumb)) {
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String newThumbName = timestamp + "_" + sourceThumb.getFileName().toString();
                        java.nio.file.Path targetThumb = sourceThumb.resolveSibling(newThumbName);
                        java.nio.file.Files.copy(sourceThumb, targetThumb);
                        newAsset.setThumbnailPath(targetThumb.toString());
                    }
                } catch (Exception e) {
                    // 缩略图复制失败不影响主流程
                    newAsset.setThumbnailPath(null);
                }
            }

            // 复制版权文件
            if (originalAsset.getCopyrightFilePath() != null) {
                try {
                    java.nio.file.Path sourceCopyright = java.nio.file.Paths.get(originalAsset.getCopyrightFilePath());
                    if (java.nio.file.Files.exists(sourceCopyright)) {
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String newCopyrightName = timestamp + "_" + sourceCopyright.getFileName().toString();
                        java.nio.file.Path targetCopyright = sourceCopyright.resolveSibling(newCopyrightName);
                        java.nio.file.Files.copy(sourceCopyright, targetCopyright);
                        newAsset.setCopyrightFilePath(targetCopyright.toString());
                    }
                } catch (Exception e) {
                    // 版权文件复制失败不影响主流程
                    newAsset.setCopyrightFilePath(null);
                }
            }

            assetMapper.insert(newAsset);

            // 复制标签关联
            AssetTagQuery tagQuery = new AssetTagQuery();
            tagQuery.setAssetId(originalAsset.getId());
            List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> assetTags = assetTagMapper.selectList(tagQuery);
            for (com.xuanjiao.infrastructure.dataobject.AssetTagDO assetTag : assetTags) {
                com.xuanjiao.infrastructure.dataobject.AssetTagDO newAssetTag = new com.xuanjiao.infrastructure.dataobject.AssetTagDO();
                newAssetTag.setAssetId(newAsset.getId());
                newAssetTag.setTagId(assetTag.getTagId());
                assetTagMapper.insert(newAssetTag);
            }
        }

        // 复制完成后，确保所有复制的素材状态都是 DRAFT（草稿状态）
        AssetQuery verifyQuery = new AssetQuery();
        verifyQuery.setApplicationId(saved.getId());
        List<com.xuanjiao.infrastructure.dataobject.AssetDO> copiedAssets = assetMapper.selectList(verifyQuery);
        for (com.xuanjiao.infrastructure.dataobject.AssetDO asset : copiedAssets) {
            if (!"DRAFT".equals(asset.getStatus())) {
                asset.setStatus("DRAFT");
                assetMapper.updateById(asset);
            }
        }

        return saved.getId();
    }
}
