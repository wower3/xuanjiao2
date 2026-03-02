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

    /** 状态常量 */
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";

    /** 业务类型常量 */
    private static final String BUSINESS_TYPE_MATERIAL_ENTRY = "MATERIAL_ENTRY";

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
        application.setStatus(STATUS_DRAFT);
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
        if (!STATUS_DRAFT.equals(application.getStatus())) {
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
        if (!STATUS_DRAFT.equals(application.getStatus()) && !STATUS_REJECTED.equals(application.getStatus())) {
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
        assetService.updateStatusByApplicationId(id, STATUS_PENDING);

        // 再次验证所有素材状态是否正确
        AssetQuery verifyQuery = new AssetQuery();
        verifyQuery.setApplicationId(id);
        List<com.xuanjiao.infrastructure.dataobject.AssetDO> allAssets = assetMapper.selectList(verifyQuery);
        boolean allPending = allAssets.stream().allMatch(a -> STATUS_PENDING.equals(a.getStatus()));
        if (!allPending) {
            logger.warn("申请单{}存在非PENDING状态的素材，强制更新", id);
            assetService.updateStatusByApplicationId(id, STATUS_PENDING);
        }

        application.setWorkflowId(workflowId);
        application.setStatus(STATUS_PENDING);
        materialApplicationRepository.update(application);

        // 启动审批流程
        Long instanceId = workflowEngineService.startProcess(workflowId, BUSINESS_TYPE_MATERIAL_ENTRY, id, userId);
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
        if (!STATUS_DRAFT.equals(application.getStatus())) {
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
            .filter(app -> STATUS_DRAFT.equals(app.getStatus()))
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
            .filter(app -> STATUS_DRAFT.equals(app.getStatus()))
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

        MaterialApplicationDTO dto = createBaseDTO(details);

        // 填充素材信息
        List<AssetDO> assets = getAssetsForApplication(details.getId());
        if (!assets.isEmpty()) {
            Map<Long, List<TagDTO>> tagsMap = loadTagsForAssets(assets);
            dto.setAssets(convertAssetsWithTags(assets, tagsMap));
        }

        return dto;
    }

    /**
     * 创建基础 DTO
     */
    private MaterialApplicationDTO createBaseDTO(MaterialApplicationWithDetailsDO details) {
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
        return dto;
    }

    /**
     * 获取申请关联的素材列表
     */
    private List<AssetDO> getAssetsForApplication(Long applicationId) {
        if (applicationId == null) {
            return new ArrayList<>();
        }
        AssetQuery wrapper = new AssetQuery();
        wrapper.setApplicationId(applicationId);
        return assetMapper.selectList(wrapper);
    }

    /**
     * 为素材列表加载标签（优化版）
     */
    private Map<Long, List<TagDTO>> loadTagsForAssets(List<AssetDO> assets) {
        Map<Long, List<TagDTO>> tagsMap = new java.util.HashMap<>();

        List<Long> assetIds = assets.stream().map(AssetDO::getId).collect(Collectors.toList());
        if (assetIds.isEmpty()) {
            return tagsMap;
        }

        List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> allAssetTags = assetTagMapper.selectByAssetIds(assetIds);
        if (allAssetTags.isEmpty()) {
            return tagsMap;
        }

        List<Long> tagIds = allAssetTags.stream()
            .map(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getTagId)
            .distinct()
            .collect(Collectors.toList());

        List<TagDO> allTags = tagMapper.selectBatchIds(tagIds);
        Map<Long, TagDO> tagIdToTag = allTags.stream()
            .collect(Collectors.toMap(TagDO::getId, t -> t));

        Map<Long, List<com.xuanjiao.infrastructure.dataobject.AssetTagDO>> assetTagsGrouped = allAssetTags.stream()
            .collect(Collectors.groupingBy(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getAssetId));

        for (Map.Entry<Long, List<com.xuanjiao.infrastructure.dataobject.AssetTagDO>> entry : assetTagsGrouped.entrySet()) {
            List<TagDTO> tagsForAsset = convertTagsToDTO(entry.getValue(), tagIdToTag);
            tagsMap.put(entry.getKey(), tagsForAsset);
        }

        return tagsMap;
    }

    /**
     * 将标签实体转换为 DTO 列表
     */
    private List<TagDTO> convertTagsToDTO(List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> assetTags, Map<Long, TagDO> tagIdToTag) {
        List<TagDTO> tagsForAsset = new ArrayList<>();
        for (com.xuanjiao.infrastructure.dataobject.AssetTagDO assetTag : assetTags) {
            TagDO tag = tagIdToTag.get(assetTag.getTagId());
            if (tag != null) {
                tagsForAsset.add(convertTagToDTO(tag));
            }
        }
        return tagsForAsset;
    }

    /**
     * 将标签实体转换为 DTO
     */
    private TagDTO convertTagToDTO(TagDO tag) {
        TagDTO tagDTO = new TagDTO();
        tagDTO.setId(tag.getId());
        tagDTO.setName(tag.getName());
        tagDTO.setCategory(tag.getCategory());
        tagDTO.setCreateTime(tag.getCreateTime());
        return tagDTO;
    }

    /**
     * 将素材列表转换为 DTO 列表
     */
    private List<AssetDTO> convertAssetsWithTags(List<AssetDO> assets, Map<Long, List<TagDTO>> tagsMap) {
        List<AssetDTO> assetDTOs = new ArrayList<>();
        for (AssetDO asset : assets) {
            AssetDTO assetDTO = new AssetDTO();
            ConvertUtils.copyProperties(asset, assetDTO);
            assetDTO.setTags(tagsMap.get(asset.getId()));
            assetDTOs.add(assetDTO);
        }
        return assetDTOs;
    }

    private MaterialApplicationDTO convert(MaterialApplication application) {
        if (application == null) return null;

        MaterialApplicationDTO dto = new MaterialApplicationDTO();
        ConvertUtils.copyProperties(application, dto);

        logger.info("MaterialApplication.convert - applicationId: {}, title: {}", application.getId(), application.getTitle());

        // 填充申请人、维护人和部门名称
        populateUserInfo(dto, application);

        // 填充关联的素材文件
        if (application.getId() != null) {
            List<AssetDTO> assetDTOs = convertAssetsWithTags(application.getId());
            dto.setAssets(assetDTOs);
        }

        return dto;
    }

    /**
     * 填充用户信息（申请人、维护人、部门）
     */
    private void populateUserInfo(MaterialApplicationDTO dto, MaterialApplication application) {
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
    }

    /**
     * 转换素材列表（带标签）
     */
    private List<AssetDTO> convertAssetsWithTags(Long applicationId) {
        AssetQuery wrapper = new AssetQuery();
        wrapper.setApplicationId(applicationId);
        logger.info("MaterialApplication.convert - 查询assets，applicationId: {}", applicationId);
        List<AssetDO> assets = assetMapper.selectList(wrapper);
        logger.info("MaterialApplication.convert - 查询到 {} 条assets记录", assets.size());

        for (AssetDO assetDO : assets) {
            logger.info("MaterialApplication.convert - asset: id={}, name={}, applicationId={}",
                assetDO.getId(), assetDO.getName(), assetDO.getApplicationId());
        }

        // 预加载标签并转换
        Map<Long, List<TagDO>> tagsMap = loadTagDOsForAssets(assets);
        List<AssetDTO> assetDTOs = assets.stream()
            .map(asset -> convertAssetWithPreloadedTags(asset, tagsMap))
            .collect(Collectors.toList());

        logger.info("MaterialApplication.convert - 设置后 dto.assets 数量: {}", assetDTOs.size());
        return assetDTOs;
    }

    /**
     * 批量加载素材的标签（优化N+1问题，返回 TagDO）
     */
    private Map<Long, List<TagDO>> loadTagDOsForAssets(List<AssetDO> assets) {
        Map<Long, List<TagDO>> tagsMap = new java.util.HashMap<>();

        List<Long> assetIds = assets.stream().map(AssetDO::getId).collect(Collectors.toList());
        if (assetIds.isEmpty()) {
            return tagsMap;
        }

        List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> allAssetTags =
            assetTagMapper.selectByAssetIds(assetIds);

        if (allAssetTags.isEmpty()) {
            return tagsMap;
        }

        // 收集所有标签ID并批量查询
        List<Long> tagIds = allAssetTags.stream()
            .map(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getTagId)
            .distinct()
            .collect(Collectors.toList());

        List<TagDO> allTags = tagMapper.selectBatchIds(tagIds);

        // 建立标签ID到标签对象的映射
        Map<Long, TagDO> tagIdToTag = new java.util.HashMap<>();
        for (TagDO tag : allTags) {
            tagIdToTag.put(tag.getId(), tag);
        }

        // 按素材ID分组
        Map<Long, List<com.xuanjiao.infrastructure.dataobject.AssetTagDO>> assetTagsGrouped = allAssetTags.stream()
            .collect(Collectors.groupingBy(com.xuanjiao.infrastructure.dataobject.AssetTagDO::getAssetId));

        // 构建素材ID到标签列表的映射
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

        return tagsMap;
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
        MaterialApplication original = materialApplicationRepository.findById(id);
        if (original == null) {
            throw new NotFoundException("原申请单不存在");
        }

        MaterialApplication saved = createDraftCopyApplication(original, userId);
        copyAssetsFromOriginal(id, saved.getId());
        verifyDraftStatus(saved.getId());

        return saved.getId();
    }

    /**
     * 创建草稿副本申请单
     */
    private MaterialApplication createDraftCopyApplication(MaterialApplication original, Long userId) {
        MaterialApplication newApplication = new MaterialApplication();
        newApplication.setTitle(original.getTitle() + " - 副本");
        newApplication.setApplicantId(userId);
        newApplication.setMaintainerId(userId);

        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser != null) {
            newApplication.setDeptId(currentUser.getDeptId());
        }
        newApplication.setGuaranteeDeclaration(original.getGuaranteeDeclaration());
        newApplication.setStatus(STATUS_DRAFT);
        newApplication.setCreateTime(LocalDateTime.now());
        newApplication.setDeleted(0);

        return materialApplicationRepository.save(newApplication);
    }

    /**
     * 复制素材文件
     */
    private void copyAssetsFromOriginal(Long originalApplicationId, Long newApplicationId) {
        AssetQuery wrapper = new AssetQuery();
        wrapper.setApplicationId(originalApplicationId);
        List<AssetDO> originalAssets = assetMapper.selectList(wrapper);

        for (AssetDO originalAsset : originalAssets) {
            AssetDO newAsset = createAssetCopy(originalAsset, newApplicationId);
            copyAssetFiles(originalAsset, newAsset);
            assetMapper.insert(newAsset);
            copyAssetTags(originalAsset.getId(), newAsset.getId());
        }
    }

    /**
     * 创建素材副本对象
     */
    private AssetDO createAssetCopy(AssetDO originalAsset, Long newApplicationId) {
        AssetDO newAsset = new AssetDO();
        ConvertUtils.copyProperties(originalAsset, newAsset);
        newAsset.setId(null);
        newAsset.setApplicationId(newApplicationId);
        newAsset.setStatus(STATUS_DRAFT);
        newAsset.setCreateTime(LocalDateTime.now());
        newAsset.setUpdateTime(LocalDateTime.now());
        newAsset.setDeleted(0);
        return newAsset;
    }

    /**
     * 复制素材相关文件
     */
    private void copyAssetFiles(AssetDO originalAsset, AssetDO newAsset) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        // 复制主文件（关键操作，失败抛出异常）
        if (originalAsset.getFilePath() != null) {
            newAsset.setFilePath(copyFileWithTimestamp(originalAsset.getFilePath(), timestamp, originalAsset.getName()));
        }

        // 复制缩略图（非关键操作，失败时设为null）
        if (originalAsset.getThumbnailPath() != null) {
            newAsset.setThumbnailPath(copyFileSafely(originalAsset.getThumbnailPath(), timestamp));
        }

        // 复制版权文件（非关键操作，失败时设为null）
        if (originalAsset.getCopyrightFilePath() != null) {
            newAsset.setCopyrightFilePath(copyFileSafely(originalAsset.getCopyrightFilePath(), timestamp));
        }
    }

    /**
     * 复制文件（关键操作）
     */
    private String copyFileWithTimestamp(String originalPath, String timestamp, String assetName) {
        try {
            java.nio.file.Path sourcePath = java.nio.file.Paths.get(originalPath);
            if (!java.nio.file.Files.exists(sourcePath)) {
                return originalPath;
            }
            String newFileName = timestamp + "_" + sourcePath.getFileName().toString();
            java.nio.file.Path targetPath = sourcePath.resolveSibling(newFileName);
            java.nio.file.Files.copy(sourcePath, targetPath);
            return targetPath.toString();
        } catch (Exception e) {
            throw new SystemException("复制文件失败: " + assetName, e);
        }
    }

    /**
     * 复制文件（安全操作，失败返回null）
     */
    private String copyFileSafely(String originalPath, String timestamp) {
        try {
            java.nio.file.Path sourcePath = java.nio.file.Paths.get(originalPath);
            if (!java.nio.file.Files.exists(sourcePath)) {
                return null;
            }
            String newFileName = timestamp + "_" + sourcePath.getFileName().toString();
            java.nio.file.Path targetPath = sourcePath.resolveSibling(newFileName);
            java.nio.file.Files.copy(sourcePath, targetPath);
            return targetPath.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 复制素材标签关联
     */
    private void copyAssetTags(Long originalAssetId, Long newAssetId) {
        AssetTagQuery tagQuery = new AssetTagQuery();
        tagQuery.setAssetId(originalAssetId);
        List<com.xuanjiao.infrastructure.dataobject.AssetTagDO> assetTags = assetTagMapper.selectList(tagQuery);

        for (com.xuanjiao.infrastructure.dataobject.AssetTagDO assetTag : assetTags) {
            com.xuanjiao.infrastructure.dataobject.AssetTagDO newAssetTag = new com.xuanjiao.infrastructure.dataobject.AssetTagDO();
            newAssetTag.setAssetId(newAssetId);
            newAssetTag.setTagId(assetTag.getTagId());
            assetTagMapper.insert(newAssetTag);
        }
    }

    /**
     * 验证并确保所有复制的素材状态都是 DRAFT
     */
    private void verifyDraftStatus(Long applicationId) {
        AssetQuery verifyQuery = new AssetQuery();
        verifyQuery.setApplicationId(applicationId);
        List<com.xuanjiao.infrastructure.dataobject.AssetDO> copiedAssets = assetMapper.selectList(verifyQuery);

        for (com.xuanjiao.infrastructure.dataobject.AssetDO asset : copiedAssets) {
            if (!STATUS_DRAFT.equals(asset.getStatus())) {
                asset.setStatus(STATUS_DRAFT);
                assetMapper.updateById(asset);
            }
        }
    }
}
