package com.xuanjiao.app.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.xuanjiao.app.service.AssetService;
import com.xuanjiao.app.service.WorkflowEngineService;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.domain.asset.entity.Asset;
import com.xuanjiao.domain.asset.repository.AssetRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssetServiceImpl implements AssetService {

    @Resource
    private AssetRepository assetRepository;

    @Resource
    private WorkflowEngineService workflowEngineService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    @Transactional
    public AssetDTO upload(MultipartFile file, AssetUploadCmd cmd, Long userId) {
        try {
            String md5 = DigestUtil.md5Hex(file.getInputStream());
            Asset existing = assetRepository.findByMd5(md5);
            if (existing != null) {
                throw new RuntimeException("文件已存在");
            }
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

            // 根据是否选择审批流程设置状态
            if (cmd.getWorkflowId() != null) {
                asset.setStatus("PENDING");
                assetRepository.save(asset);
                // 启动审批流程
                workflowEngineService.startProcess(
                    cmd.getWorkflowId(), "ASSET", asset.getId(), userId);
            } else {
                asset.setStatus("APPROVED");
                assetRepository.save(asset);
            }
            return convert(asset);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public AssetDTO getById(Long id) {
        Asset asset = assetRepository.findById(id);
        return convert(asset);
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
    public void delete(Long id) {
        assetRepository.deleteById(id);
    }

    private AssetDTO convert(Asset asset) {
        if (asset == null) return null;
        AssetDTO dto = new AssetDTO();
        BeanUtils.copyProperties(asset, dto);
        return dto;
    }
}
