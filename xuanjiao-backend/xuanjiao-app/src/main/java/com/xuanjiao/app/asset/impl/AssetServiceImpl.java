package com.xuanjiao.app.asset.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xuanjiao.infrastructure.asset.AssetTagQuery;
import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.app.schedule.AssetDeletionCleanupTask;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.dto.asset.dto.AssetDTO;
import com.xuanjiao.client.dto.asset.AssetQry;
import com.xuanjiao.client.dto.asset.AssetUploadCmd;
import com.xuanjiao.client.dto.common.PageResult;
import com.xuanjiao.client.dto.tag.dto.TagDTO;
import com.xuanjiao.domain.asset.entity.Asset;
import com.xuanjiao.domain.asset.repository.AssetRepository;
import com.xuanjiao.infrastructure.dataobject.AssetTagDO;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.asset.AssetQuery;
import com.xuanjiao.infrastructure.asset.AssetTagMapper;
import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyMapper;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import com.xuanjiao.app.log.OperationLogService;
import com.xuanjiao.app.usage.UsageApplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 素材服务实现类
 * <p>实现AssetService接口，封装素材业务逻辑</p>
 * <p>核心功能：文件上传（MD5去重）、分页查询、标签管理、删除管理</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.asset.AssetService
 */
@Service
public class AssetServiceImpl implements AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetServiceImpl.class);

    @Resource
    private AssetRepository assetRepository;

    @Resource
    private WorkflowEngineService workflowEngineService;

    @Resource
    private AssetTagMapper assetTagMapper;

    @Resource
    private AssetMapper assetMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UsageApplyMapper usageApplyMapper;

    @Resource
    private UsageApplyAssetMapper usageApplyAssetMapper;

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private AssetDeletionCleanupTask assetDeletionCleanupTask;

    @Resource
    private UsageApplyService usageApplyService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    @Transactional
    public AssetDTO upload(MultipartFile file, MultipartFile thumbnailFile, AssetUploadCmd cmd, Long userId) {
        // 文件格式校验
        validateFileFormat(file, cmd.getType());

        try {
            logger.info("Asset.upload - 开始上传，applicationId: {}, tagIds: {}", cmd.getApplicationId(), cmd.getTagIds());

            String md5 = DigestUtil.md5Hex(file.getInputStream());
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + ext;
            String filePath = uploadPath + cmd.getType() + "/" + fileName;
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);

            // 保存视频缩略图
            String thumbnailPath = null;
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                String thumbFileName = UUID.randomUUID().toString() + ".jpg";
                thumbnailPath = uploadPath + "thumbnail/" + thumbFileName;
                File thumbDest = new File(thumbnailPath);
                if (!thumbDest.getParentFile().exists()) {
                    thumbDest.getParentFile().mkdirs();
                }
                thumbnailFile.transferTo(thumbDest);
                logger.info("视频缩略图已保存: {}", thumbnailPath);
            }

            Asset asset = new Asset();
            asset.setName(cmd.getName());
            asset.setType(cmd.getType());
            asset.setFilePath(filePath);
            asset.setThumbnailPath(thumbnailPath);
            asset.setFileSize(file.getSize());
            asset.setMd5(md5);
            asset.setCopyright(cmd.getCopyright());
            asset.setUploadUserId(userId);
            asset.setCreateTime(LocalDateTime.now());
            asset.setUpdateTime(LocalDateTime.now());
            asset.setDeleted(0);

            // New fields for material entry
            asset.setApplicationId(cmd.getApplicationId());
            asset.setCopyrightFilePath(cmd.getCopyrightFilePath());
            asset.setCopyrightText(cmd.getCopyrightText());
            asset.setDescription(cmd.getDescription());
            asset.setPublishChannel(cmd.getPublishChannel());
            asset.setTagIds(cmd.getTagIds());

            logger.info("Asset.upload - asset设置完成，applicationId: {}, tagIds: {}", asset.getApplicationId(), asset.getTagIds());

            // For draft applications, status is DRAFT, otherwise follow workflow logic
            if (cmd.getApplicationId() != null) {
                // Check if the application is still in draft status
                // If yes, set asset status to DRAFT
                // If no (submitted), set to PENDING
                asset.setStatus("DRAFT");
                assetRepository.save(asset);
            } else if (cmd.getWorkflowId() != null) {
                asset.setStatus("PENDING");
                assetRepository.save(asset);
                // 启动审批流程
                workflowEngineService.startProcess(
                    cmd.getWorkflowId(), "ASSET", asset.getId(), userId);
            } else {
                asset.setStatus("APPROVED");
                assetRepository.save(asset);
            }

            // Save tag associations
            if (cmd.getTagIds() != null && !cmd.getTagIds().isEmpty() && asset.getId() != null) {
                for (Long tagId : cmd.getTagIds()) {
                    AssetTagDO assetTag = new AssetTagDO();
                    assetTag.setAssetId(asset.getId());
                    assetTag.setTagId(tagId);
                    assetTagMapper.insert(assetTag);
                }
            }

            return convert(asset);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public AssetDTO getById(Long id) {
        Asset asset = assetRepository.findById(id);
        return convertWithTags(asset);
    }

    @Override
    public PageResult<AssetDTO> query(AssetQry cmd) {
        // 使用 MyBatis-Plus 分页
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.xuanjiao.infrastructure.dataobject.AssetDO> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(cmd.getPageNum(), cmd.getPageSize());

        // 构建 AssetQuery 条件
        AssetQuery query = new AssetQuery();
        query.setName(cmd.getName());
        query.setType(cmd.getType());
        query.setStatus(cmd.getStatus());

        IPage<com.xuanjiao.infrastructure.dataobject.AssetDO> pageResult = assetMapper.selectPage(page, query);

        List<AssetDTO> dtoList = pageResult.getRecords().stream()
            .map(this::convertDOToDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, pageResult.getTotal(), cmd.getPageNum(), cmd.getPageSize());
    }

    @Override
    public PageResult<AssetDTO> queryWithRoleFilter(AssetQry cmd, Long userId) {
        // Get user's role to determine filtering rules
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        RoleDO role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            throw new RuntimeException("用户角色不存在");
        }

        // Determine allowed statuses based on role
        String roleType = role.getRoleType();

        // 使用 MyBatis-Plus 分页
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.xuanjiao.infrastructure.dataobject.AssetDO> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(cmd.getPageNum(), cmd.getPageSize());

        // 构建 AssetQuery 条件
        AssetQuery query = new AssetQuery();
        query.setName(cmd.getName());
        query.setType(cmd.getType());

        IPage<com.xuanjiao.infrastructure.dataobject.AssetDO> pageResult;

        // SYSTEM_ADMIN and GENERAL_MGMT can see APPROVED, PENDING, and DELETED
        // All other users can only see APPROVED and DELETED
        // DRAFT assets are never shown in asset list
        // Soft deleted assets (deleted=1) are excluded from all queries
        if ("SYSTEM_ADMIN".equals(roleType) || "GENERAL_MGMT".equals(roleType)) {
            if (cmd.getStatus() != null && !cmd.getStatus().isEmpty()) {
                // User specified a status filter, use it
                query.setStatus(cmd.getStatus());
            } else {
                // Admin default: Show APPROVED, PENDING, and DELETED
                query.setStatusList(Arrays.asList("APPROVED", "PENDING", "DELETED"));
            }
        } else {
            // Regular users: APPROVED and DELETED
            if (cmd.getStatus() != null && !cmd.getStatus().isEmpty()) {
                // User specified a status filter, use it
                query.setStatus(cmd.getStatus());
            } else {
                // Regular user default: Show APPROVED and DELETED
                query.setStatusList(Arrays.asList("APPROVED", "DELETED"));
            }
        }

        pageResult = assetMapper.selectPage(page, query);

        // 转换为DTO并填充下载权限
        List<AssetDTO> dtoList = pageResult.getRecords().stream().map(assetDO -> {
            AssetDTO dto = convertDOToDTO(assetDO);
            if (dto != null && "APPROVED".equals(assetDO.getStatus())) {
                // 只有 APPROVED 状态的素材需要检查下载权限
                dto.setCanDownload(usageApplyService.canUseAsset(assetDO.getId(), userId));
            } else {
                dto.setCanDownload(false);
            }
            return dto;
        }).collect(Collectors.toList());

        return PageResult.of(dtoList, pageResult.getTotal(), cmd.getPageNum(), cmd.getPageSize());
    }

    @Override
    public void delete(Long id) {
        assetRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatusByApplicationId(Long applicationId, String status) {
        // 使用 AssetMapper 的批量更新方法
        assetMapper.updateStatusByApplicationId(applicationId, status);
    }

    private AssetDTO convert(Asset asset) {
        if (asset == null) return null;
        AssetDTO dto = new AssetDTO();
        BeanUtils.copyProperties(asset, dto);
        return dto;
    }

    private AssetDTO convertWithTags(Asset asset) {
        if (asset == null) return null;
        AssetDTO dto = new AssetDTO();
        BeanUtils.copyProperties(asset, dto);

        // Load tags
        if (asset.getId() != null) {
            AssetTagQuery query = new AssetTagQuery();
            query.setAssetId(asset.getId());
            List<AssetTagDO> assetTags = assetTagMapper.selectList(query);

            if (!assetTags.isEmpty()) {
                List<Long> tagIds = assetTags.stream()
                        .map(AssetTagDO::getTagId)
                        .collect(Collectors.toList());
                if (!tagIds.isEmpty()) {
                    List<TagDO> tags = tagMapper.selectBatchIds(tagIds);
                    dto.setTags(tags.stream().map(tag -> {
                        TagDTO tagDTO = new TagDTO();
                        BeanUtils.copyProperties(tag, tagDTO);
                        return tagDTO;
                    }).collect(Collectors.toList()));
                }
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public void adminDelete(Long assetId, String reason, Long userId, Boolean isAdmin) {
        // 验证管理员权限
        if (!isAdmin) {
            throw new RuntimeException("只有管理员才能执行此操作");
        }

        // 获取素材信息
        com.xuanjiao.infrastructure.dataobject.AssetDO asset = assetMapper.selectById(assetId);
        if (asset == null) {
            throw new RuntimeException("素材不存在");
        }

        logger.info("管理员彻底删除素材 - id={}, name={}, status={}, deleted={}",
            asset.getId(), asset.getName(), asset.getStatus(), asset.getDeleted());

        // 只能删除已通过审批的素材或已删除状态的素材
        if (!"APPROVED".equals(asset.getStatus()) && !"DELETED".equals(asset.getStatus())) {
            throw new RuntimeException("只能删除已通过审批或已删除状态的素材");
        }

        // 获取用户信息
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 使用 AssetMapper 的方法直接更新 deleted 字段为 1
        int updateResult = assetMapper.updateDeletedById(assetId);
        logger.info("软删除更新结果：影响行数={}", updateResult);

        // 记录操作日志
        operationLogService.log(
            userId,
            user.getRealName(),
            "ADMIN_DELETE",
            "ASSET",
            assetId,
            asset.getName(),
            reason,
            null // IP地址由拦截器提供
        );
    }

    @Override
    @Transactional
    public void adjustDeleteTime(Long assetId, Boolean isAdmin) {
        // 验证管理员权限
        if (!isAdmin) {
            throw new RuntimeException("只有管理员才能执行此操作");
        }

        // 获取素材信息
        com.xuanjiao.infrastructure.dataobject.AssetDO asset = assetMapper.selectById(assetId);
        if (asset == null) {
            throw new RuntimeException("素材不存在");
        }

        // 只能对DELETED状态的素材执行模拟时间操作
        if (!"DELETED".equals(asset.getStatus())) {
            throw new RuntimeException("只能对已删除状态的素材执行此操作");
        }

        // 将删除审批通过时间设置为一周前（测试用）
        // 然后可以通过手动触发定时任务来执行软删除
        asset.setDeletionApproveTime(LocalDateTime.now().minusWeeks(1));
        assetMapper.updateById(asset);
    }

    @Override
    public int triggerCleanupTask(Boolean isAdmin) {
        // 验证管理员权限
        if (!isAdmin) {
            throw new RuntimeException("只有管理员才能执行此操作");
        }

        // 手动触发定时任务，执行素材软删除
        return assetDeletionCleanupTask.cleanupDeletedAssetsManually();
    }

    @Override
    public PageResult<AssetDTO> getMyApprovedAssets(String name, String type, Integer pageNum, Integer pageSize, Long userId) {
        // 查询当前用户申请录入的素材，状态为APPROVED
        AssetQuery query = new AssetQuery();
        query.setUploadUserId(userId);
        query.setStatus("APPROVED");
        query.setName(name);
        query.setType(type);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");

        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.xuanjiao.infrastructure.dataobject.AssetDO> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        assetMapper.selectPage(page, query);

        List<AssetDTO> dtoList = page.getRecords().stream()
            .map(this::convertDOToDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, page.getTotal(), pageNum, pageSize);
    }

    private AssetDTO convertDOToDTO(com.xuanjiao.infrastructure.dataobject.AssetDO assetDO) {
        if (assetDO == null) return null;
        AssetDTO dto = new AssetDTO();
        BeanUtils.copyProperties(assetDO, dto);
        return dto;
    }

    /**
     * 校验文件格式
     */
    private void validateFileFormat(MultipartFile file, String type) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String lowerName = fileName.toLowerCase();
        String ext = lowerName.substring(lowerName.lastIndexOf("."));

        // 校验MIME类型
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("无法识别文件类型");
        }

        if ("IMAGE".equals(type)) {
            // 图片允许格式
            java.util.Set<String> imageFormats = new java.util.HashSet<>(java.util.Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));
            if (!imageFormats.contains(ext)) {
                throw new IllegalArgumentException("图片格式不支持，请选择 jpg, jpeg, png, gif, webp 格式");
            }
            // 额外校验MIME类型
            if (!contentType.startsWith("image/")) {
                throw new IllegalArgumentException("文件类型不匹配，请上传图片文件");
            }
        } else if ("VIDEO".equals(type)) {
            // 视频允许格式
            java.util.Set<String> videoFormats = new java.util.HashSet<>(java.util.Arrays.asList(".mp4", ".webm", ".ogg", ".mov", ".avi", ".mkv", ".mpg", ".mpeg", ".3gp"));
            if (!videoFormats.contains(ext)) {
                throw new IllegalArgumentException("视频格式不支持，请选择 mp4, webm, ogg, mov, avi, mkv, mpg, mpeg, 3gp 格式");
            }
            // 额外校验MIME类型
            if (!contentType.startsWith("video/")) {
                throw new IllegalArgumentException("文件类型不匹配，请上传视频文件");
            }
        }
    }
}
