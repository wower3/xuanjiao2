package com.xuanjiao.app.material.impl;

import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.client.approval.ApprovalProgressDTO;
import com.xuanjiao.client.asset.AssetDTO;
import com.xuanjiao.client.material.MaterialApplicationCmd;
import com.xuanjiao.client.material.MaterialApplicationDTO;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.asset.TagDTO;
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
import com.xuanjiao.infrastructure.material.MaterialApplicationWithDetailsDO;
import com.xuanjiao.infrastructure.material.MaterialApplicationMapper;
import com.xuanjiao.infrastructure.material.MaterialApplicationQuery;
import com.xuanjiao.common.ConvertUtils;
import com.xuanjiao.common.exception.BusinessException;
import com.xuanjiao.common.exception.NotFoundException;
import com.xuanjiao.common.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 素材录入申请服务实现类
 * <p>实现MaterialApplicationService接口，封装素材录入申请业务逻辑</p>
 * <p>核心功能：申请单CRUD、草稿管理、提交审批</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.material.MaterialApplicationService
 */
@Service
public class MaterialApplicationServiceImpl implements MaterialApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialApplicationServiceImpl.class);

    /** 消息常量 */
    private static final String MSG_USER_NOT_FOUND = "用户不存在";
    private static final String MSG_APPLICATION_NOT_FOUND = "申请单不存在";
    private static final String MSG_ONLY_DRAFT_CAN_MODIFY = "只有草稿状态可以修改";

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

    @Autowired
    private MaterialApplicationMapper materialApplicationMapper;

    @Override
    @Transactional
    public MaterialApplicationDTO create(MaterialApplicationCmd cmd, Long userId) {
        // 获取当前用户信息作为默认维护人
        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new NotFoundException(MSG_USER_NOT_FOUND);
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
            throw new NotFoundException(MSG_APPLICATION_NOT_FOUND);
        }

        // 只有草稿状态可以修改，且只能修改自己的申请单
        if (!"DRAFT".equals(application.getStatus())) {
            throw new BusinessException(MSG_ONLY_DRAFT_CAN_MODIFY);
        }
        if (!application.getApplicantId().equals(userId)) {
            throw new BusinessException("只能修改自己的申请单");
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
            throw new NotFoundException(MSG_APPLICATION_NOT_FOUND);
        }

        // 只有草稿或已驳回状态可以提交，且只能提交自己的申请单
        if (!"DRAFT".equals(application.getStatus()) && !"REJECTED".equals(application.getStatus())) {
            throw new BusinessException("只有草稿或已驳回状态可以提交");
        }
        if (!application.getApplicantId().equals(userId)) {
            throw new BusinessException("只能提交自己的申请单");
        }

        // 检查是否有至少一个文件
        AssetQuery query = new AssetQuery();
        query.setApplicationId(id);
        Long count = assetMapper.selectCount(query);
        if (count == 0) {
            throw new BusinessException("请至少上传一个素材文件");
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
            throw new NotFoundException(MSG_APPLICATION_NOT_FOUND);
        }

        // 只有草稿状态可以删除，且只能删除自己的申请单
        if (!"DRAFT".equals(application.getStatus())) {
            throw new BusinessException("只有草稿状态可以删除");
        }
        if (!application.getApplicantId().equals(userId)) {
            throw new BusinessException("只能删除自己的申请单");
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
        // 使用JOIN查询一次性获取申请及关联的用户/部门信息，避免N+1问题
        MaterialApplicationQuery query = new MaterialApplicationQuery();
        query.setApplicantId(userId);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset((pageNum - 1) * pageSize);
        query.setLimit(pageSize);
        List<MaterialApplicationWithDetailsDO> list = materialApplicationMapper.selectListWithDetails(query);
        long total = materialApplicationMapper.selectCount(query);

        // 过滤出草稿状态
        List<MaterialApplicationDTO> dtoList = list.stream()
            .filter(app -> "DRAFT".equals(app.getStatus()))
            .map(this::convertWithDetails)
            .collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<MaterialApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title) {
        // 使用JOIN查询一次性获取申请及关联的用户/部门信息，避免N+1问题
        MaterialApplicationQuery query = new MaterialApplicationQuery();
        query.setApplicantId(userId);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset((pageNum - 1) * pageSize);
        query.setLimit(pageSize);
        List<MaterialApplicationWithDetailsDO> list = materialApplicationMapper.selectListWithDetails(query);
        long total = materialApplicationMapper.selectCount(query);

        // 过滤出草稿状态
        List<MaterialApplicationWithDetailsDO> filteredList = list.stream()
            .filter(app -> "DRAFT".equals(app.getStatus()))
            .collect(Collectors.toList());

        // 按标题筛选
        if (title != null && !title.isEmpty()) {
            final String titleFilter = title;
            filteredList = filteredList.stream()
                .filter(app -> app.getTitle() != null && app.getTitle().contains(titleFilter))
                .collect(Collectors.toList());
        }

        List<MaterialApplicationDTO> dtoList = filteredList.stream()
            .map(this::convertWithDetails)
            .collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<MaterialApplicationDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize) {
        // 使用JOIN查询一次性获取申请及关联的用户/部门信息，避免N+1问题
        MaterialApplicationQuery query = new MaterialApplicationQuery();
        query.setApplicantId(userId);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset((pageNum - 1) * pageSize);
        query.setLimit(pageSize);
        List<MaterialApplicationWithDetailsDO> list = materialApplicationMapper.selectListWithDetails(query);
        long total = materialApplicationMapper.selectCount(query);

        // 转换为DTO - 使用优化的convert方法
        List<MaterialApplicationDTO> dtoList = list.stream().map(this::convertWithDetails).collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    /**
     * 将带详情的DO转换为DTO（优化版，使用JOIN查询结果）
     */
    private MaterialApplicationDTO convertWithDetails(MaterialApplicationWithDetailsDO details) {
        if (details == null) return null;
        MaterialApplicationDTO dto = new MaterialApplicationDTO();
        dto.setId(details.getId());
        dto.setTitle(details.getTitle());
        dto.setApplicantId(details.getApplicantId());
        dto.setApplicantName(details.getApplicantName());
        dto.setMaintainerId(details.getMaintainerId());
        dto.setMaintainerName(details.getMaintainerName());
        dto.setDeptId(details.getDeptId());
        dto.setDeptName(details.getDeptName());
        dto.setWorkflowId(details.getWorkflowId());
        dto.setStatus(details.getStatus());
        dto.setGuaranteeDeclaration(details.getGuaranteeDeclaration());
        dto.setCreateTime(details.getCreateTime());
        dto.setUpdateTime(details.getUpdateTime());

        // 查询关联的素材文件
        if (details.getId() != null) {
            AssetQuery wrapper = new AssetQuery();
            wrapper.setApplicationId(details.getId());
            List<AssetDO> assets = assetMapper.selectList(wrapper);

            if (!assets.isEmpty()) {
                // 批量查询所有素材的标签（优化N+1问题）
                List<Long> assetIds = assets.stream().map(AssetDO::getId).collect(Collectors.toList());
                Map<Long, List<TagDTO>> tagsMap = new java.util.HashMap<>();

                if (!assetIds.isEmpty()) {
                    List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> allAssetTags =
                        assetTagMapper.selectByAssetIds(assetIds);

                    if (!allAssetTags.isEmpty()) {
                        List<Long> tagIds = allAssetTags.stream()
                            .map(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getTagId)
                            .distinct()
                            .collect(Collectors.toList());

                        List<TagDO> allTags = tagMapper.selectBatchIds(tagIds);
                        Map<Long, TagDO> tagIdToTag = new java.util.HashMap<>();
                        for (TagDO tag : allTags) {
                            tagIdToTag.put(tag.getId(), tag);
                        }

                        Map<Long, List<com.xuanjiao.infrastructure.dataobject.AssetTagDO>> assetTagsGrouped = allAssetTags.stream()
                            .collect(Collectors.groupingBy(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getAssetId));

                        for (Map.Entry<Long, List<com.xuanjiao.infrastructure.dataobject.AssetTagDO>> entry : assetTagsGrouped.entrySet()) {
                            List<TagDTO> tagsForAsset = new ArrayList<>();
                            for (com.xuanjiao.infrastructure.dataobject.AssetTagDO assetTag : entry.getValue()) {
                                TagDO tag = tagIdToTag.get(assetTag.getTagId());
                                if (tag != null) {
                                    TagDTO tagDTO = new TagDTO();
                                    tagDTO.setId(tag.getId());
                                    tagDTO.setName(tag.getName());
                                    tagDTO.setCategory(tag.getCategory());
                                    tagDTO.setCreateTime(tag.getCreateTime());
                                    tagsForAsset.add(tagDTO);
                                }
                            }
                            tagsMap.put(entry.getKey(), tagsForAsset);
                        }
                    }
                }

                // 转换为 DTO
                List<AssetDTO> assetDTOs = new ArrayList<>();
                for (AssetDO asset : assets) {
                    AssetDTO assetDTO = new AssetDTO();
                    ConvertUtils.copyProperties(asset, assetDTO);
                    assetDTO.setTags(tagsMap.get(asset.getId()));
                    assetDTOs.add(assetDTO);
                }
                dto.setAssets(assetDTOs);
            }
        }

        return dto;
    }

    private MaterialApplicationDTO convert(MaterialApplication application) {
        if (application == null) return null;
        MaterialApplicationDTO dto = new MaterialApplicationDTO();
        ConvertUtils.copyProperties(application, dto);

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

            // 批量查询所有素材的标签（优化N+1问题）
            // 1. 获取所有素材ID
            List<Long> assetIds = assets.stream().map(AssetDO::getId).collect(Collectors.toList());

            // 2. 批量查询素材-标签关联
            Map<Long, List<TagDO>> tagsMap = new java.util.HashMap<>();
            if (!assetIds.isEmpty()) {
                List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> allAssetTags =
                    assetTagMapper.selectByAssetIds(assetIds);

                if (!allAssetTags.isEmpty()) {
                    // 3. 收集所有标签ID
                    List<Long> tagIds = allAssetTags.stream()
                        .map(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getTagId)
                        .distinct()
                        .collect(Collectors.toList());

                    // 4. 批量查询所有标签
                    List<TagDO> allTags = tagMapper.selectBatchIds(tagIds);

                    // 5. 建立标签ID到标签对象的映射
                    Map<Long, TagDO> tagIdToTag = new java.util.HashMap<>();
                    for (TagDO tag : allTags) {
                        tagIdToTag.put(tag.getId(), tag);
                    }

                    // 6. 按素材ID分组，建立素材ID到标签列表的映射
                    Map<Long, List<com.xuanjiao.infrastructure.dataobject.AssetTagDO>> assetTagsGrouped = allAssetTags.stream()
                        .collect(Collectors.groupingBy(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getAssetId));

                    for (Map.Entry<Long, List<com.xuanjiao.infrastructure.dataobject.AssetTagDO>> entry : assetTagsGrouped.entrySet()) {
                        List<TagDO> tagsForAsset = new ArrayList<>();
                        for (com.xuanjiao.infrastructure.dataobject.AssetTagDO assetTag : entry.getValue()) {
                            TagDO tag = tagIdToTag.get(assetTag.getTagId());
                            if (tag != null) {
                                tagsForAsset.add(tag);
                            }
                        }
                        tagsMap.put(entry.getKey(), tagsForAsset);
                    }
                }
            }

            // 6. 转换素材为DTO，使用预加载的标签
            List<AssetDTO> assetDTOs = assets.stream()
                .map(asset -> convertAssetWithPreloadedTags(asset, tagsMap))
                .collect(Collectors.toList());
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
            throw new NotFoundException(MSG_APPLICATION_NOT_FOUND);
        }
        application.setStatus(status);
        materialApplicationRepository.update(application);
    }

    /**
     * 使用预加载的标签转换素材为DTO（优化N+1问题）
     *
     * @param assetDO 素材数据对象
     * @param tagsMap 素材ID到标签列表的映射（预加载）
     * @return 素材DTO
     */
    private AssetDTO convertAssetWithPreloadedTags(AssetDO assetDO, Map<Long, List<TagDO>> tagsMap) {
        if (assetDO == null) return null;
        AssetDTO dto = new AssetDTO();
        ConvertUtils.copyProperties(assetDO, dto);

        // 使用预加载的标签
        if (tagsMap != null && tagsMap.containsKey(assetDO.getId())) {
            List<TagDO> tags = tagsMap.get(assetDO.getId());
            if (tags != null && !tags.isEmpty()) {
                dto.setTags(tags.stream().map(tag -> {
                    com.xuanjiao.client.asset.TagDTO tagDTO = new com.xuanjiao.client.asset.TagDTO();
                    ConvertUtils.copyProperties(tag, tagDTO);
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
            throw new NotFoundException("原申请单不存在");
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
            ConvertUtils.copyProperties(originalAsset, newAsset);
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
                    throw new SystemException("复制文件失败: " + originalAsset.getName(), e);
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
