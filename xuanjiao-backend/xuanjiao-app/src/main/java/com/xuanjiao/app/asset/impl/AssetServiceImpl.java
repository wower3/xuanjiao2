package com.xuanjiao.app.asset.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.domain.asset.entity.Asset;
import com.xuanjiao.domain.asset.repository.AssetRepository;
import com.xuanjiao.infrastructure.dataobject.AssetTagDO;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.asset.AssetTagMapper;
import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
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

@Service
public class AssetServiceImpl implements AssetService {

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

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    @Transactional
    public AssetDTO upload(MultipartFile file, AssetUploadCmd cmd, Long userId) {
        try {
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
            Asset asset = new Asset();
            asset.setName(cmd.getName());
            asset.setType(cmd.getType());
            asset.setFilePath(filePath);
            asset.setFileSize(file.getSize());
            asset.setMd5(md5);
            asset.setCopyright(cmd.getCopyright());
            asset.setUploadUserId(userId);
            asset.setCreateTime(LocalDateTime.now());

            // New fields for material entry
            asset.setApplicationId(cmd.getApplicationId());
            asset.setCopyrightFilePath(cmd.getCopyrightFilePath());
            asset.setCopyrightText(cmd.getCopyrightText());
            asset.setDescription(cmd.getDescription());
            asset.setPublishChannel(cmd.getPublishChannel());
            asset.setTagIds(cmd.getTagIds());

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
    public PageResult<AssetDTO> query(AssetQueryCmd cmd) {
        int offset = (cmd.getPageNum() - 1) * cmd.getPageSize();
        List<Asset> list = assetRepository.findByCondition(
            cmd.getName(), cmd.getType(), cmd.getStatus(), offset, cmd.getPageSize());
        long total = assetRepository.countByCondition(cmd.getName(), cmd.getType(), cmd.getStatus());
        List<AssetDTO> dtoList = list.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, total, cmd.getPageNum(), cmd.getPageSize());
    }

    @Override
    public PageResult<AssetDTO> queryWithRoleFilter(AssetQueryCmd cmd, Long userId) {
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

        int offset = (cmd.getPageNum() - 1) * cmd.getPageSize();
        List<Asset> list;
        long total;

        // SYSTEM_ADMIN and GENERAL_MGMT can see APPROVED and PENDING
        // All other users can only see APPROVED
        // DRAFT assets are never shown in asset list
        if ("SYSTEM_ADMIN".equals(roleType) || "GENERAL_MGMT".equals(roleType)) {
            if (cmd.getStatus() != null && !cmd.getStatus().isEmpty()) {
                // User specified a status filter, use it
                list = assetRepository.findByCondition(
                    cmd.getName(), cmd.getType(), cmd.getStatus(), offset, cmd.getPageSize());
                total = assetRepository.countByCondition(cmd.getName(), cmd.getType(), cmd.getStatus());
            } else {
                // Admin default: Show APPROVED and PENDING
                List<String> statusList = Arrays.asList("APPROVED", "PENDING");
                list = assetRepository.findByStatusList(
                    cmd.getName(), cmd.getType(), statusList, offset, cmd.getPageSize());
                total = assetRepository.countByStatusList(cmd.getName(), cmd.getType(), statusList);
            }
        } else {
            // Regular users: Only APPROVED
            list = assetRepository.findByCondition(
                cmd.getName(), cmd.getType(), "APPROVED", offset, cmd.getPageSize());
            total = assetRepository.countByCondition(cmd.getName(), cmd.getType(), "APPROVED");
        }

        List<AssetDTO> dtoList = list.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, total, cmd.getPageNum(), cmd.getPageSize());
    }

    @Override
    public void delete(Long id) {
        assetRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatusByApplicationId(Long applicationId, String status) {
        LambdaQueryWrapper<com.xuanjiao.infrastructure.dataobject.AssetDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.xuanjiao.infrastructure.dataobject.AssetDO::getApplicationId, applicationId);
        List<com.xuanjiao.infrastructure.dataobject.AssetDO> assets = assetMapper.selectList(wrapper);

        for (com.xuanjiao.infrastructure.dataobject.AssetDO assetDO : assets) {
            assetDO.setStatus(status);
            assetMapper.updateById(assetDO);
        }
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
            LambdaQueryWrapper<AssetTagDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AssetTagDO::getAssetId, asset.getId());
            List<AssetTagDO> assetTags = assetTagMapper.selectList(wrapper);

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
}
