package com.xuanjiao.app.usage.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.dto.*;
import com.xuanjiao.domain.usage.entity.UsageApply;
import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import com.xuanjiao.domain.usage.repository.UsageApplyAssetRepository;
import com.xuanjiao.domain.usage.repository.UsageApplyRepository;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
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
public class UsageApplyServiceImpl implements UsageApplyService {

    private static final Logger logger = LoggerFactory.getLogger(UsageApplyServiceImpl.class);

    @Autowired
    private UsageApplyRepository usageApplyRepository;

    @Autowired
    private UsageApplyAssetRepository usageApplyAssetRepository;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private UserMapper userMapper;

    // ========== 新API（按素材配置使用信息） ==========

    @Override
    @Transactional
    public UsageApplyDTO createDraft(UsageApplyCmd cmd, Long userId) {
        // 获取当前用户信息
        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 创建使用申请单
        UsageApply usageApply = new UsageApply();
        usageApply.setTitle(cmd.getTitle());
        usageApply.setUserId(userId);
        usageApply.setDeptId(currentUser.getDeptId());
        usageApply.setStatus("DRAFT");
        usageApply.setDraft(1);
        usageApply.setCreateTime(LocalDateTime.now());

        usageApplyRepository.save(usageApply);

        // 保存素材关联配置到中间表
        if (cmd.getAssetConfigs() != null && !cmd.getAssetConfigs().isEmpty()) {
            List<UsageApplyAsset> assets = new ArrayList<>();
            for (UsageApplyCmd.AssetUsageConfig config : cmd.getAssetConfigs()) {
                AssetDO asset = assetMapper.selectById(config.getAssetId());
                if (asset == null) {
                    throw new RuntimeException("素材不存在: " + config.getAssetId());
                }

                // 检查素材状态：只能使用APPROVED状态的素材
                // DRAFT/PENDING/DELETED状态的素材不允许使用
                // 软删除（deleted=1）的素材不允许使用
                if (asset.getDeleted() != null && asset.getDeleted() == 1) {
                    throw new RuntimeException("该素材已被删除，无法使用: " + asset.getName());
                }
                if (!"APPROVED".equals(asset.getStatus())) {
                    String statusMsg = "DRAFT".equals(asset.getStatus()) ? "草稿" :
                                     "PENDING".equals(asset.getStatus()) ? "待审批" :
                                     "DELETED".equals(asset.getStatus()) ? "已删除" : asset.getStatus();
                    throw new RuntimeException("只能使用已通过审批的素材，当前素材状态为" + statusMsg + ": " + asset.getName());
                }

                UsageApplyAsset applyAsset = new UsageApplyAsset();
                applyAsset.setUsageApplyId(usageApply.getId());
                applyAsset.setAssetId(config.getAssetId());
                applyAsset.setUsageDescription(config.getUsageDescription());
                applyAsset.setUsagePublishChannel(config.getUsagePublishChannel());
                applyAsset.setUsageIsSecondaryCreation(config.getUsageIsSecondaryCreation() != null ? config.getUsageIsSecondaryCreation() : 0);
                applyAsset.setUsageAttachmentPath(config.getUsageAttachmentPath());
                assets.add(applyAsset);
            }
            usageApplyAssetRepository.batchSave(assets);
        }

        return convert(usageApply);
    }

    @Override
    @Transactional
    public UsageApplyDTO updateDraft(Long id, UsageApplyCmd cmd, Long userId) {
        UsageApply usageApply = usageApplyRepository.findById(id);
        if (usageApply == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿状态可以修改
        if (usageApply.getDraft() != 1) {
            throw new RuntimeException("只有草稿状态可以修改");
        }
        if (!usageApply.getUserId().equals(userId)) {
            throw new RuntimeException("只能修改自己的申请单");
        }

        // 更新标题
        usageApply.setTitle(cmd.getTitle());
        usageApplyRepository.update(usageApply);

        // 先清除之前关联的素材配置
        usageApplyAssetRepository.deleteByUsageApplyId(id);

        // 保存新的素材关联配置到中间表
        if (cmd.getAssetConfigs() != null && !cmd.getAssetConfigs().isEmpty()) {
            List<UsageApplyAsset> assets = new ArrayList<>();
            for (UsageApplyCmd.AssetUsageConfig config : cmd.getAssetConfigs()) {
                AssetDO asset = assetMapper.selectById(config.getAssetId());
                if (asset == null) {
                    throw new RuntimeException("素材不存在: " + config.getAssetId());
                }

                // 检查素材状态：只能使用APPROVED状态的素材
                if (asset.getDeleted() != null && asset.getDeleted() == 1) {
                    throw new RuntimeException("该素材已被删除，无法使用: " + asset.getName());
                }
                if (!"APPROVED".equals(asset.getStatus())) {
                    String statusMsg = "DRAFT".equals(asset.getStatus()) ? "草稿" :
                                     "PENDING".equals(asset.getStatus()) ? "待审批" :
                                     "DELETED".equals(asset.getStatus()) ? "已删除" : asset.getStatus();
                    throw new RuntimeException("只能使用已通过审批的素材，当前素材状态为" + statusMsg + ": " + asset.getName());
                }

                UsageApplyAsset applyAsset = new UsageApplyAsset();
                applyAsset.setUsageApplyId(id);
                applyAsset.setAssetId(config.getAssetId());
                applyAsset.setUsageDescription(config.getUsageDescription());
                applyAsset.setUsagePublishChannel(config.getUsagePublishChannel());
                applyAsset.setUsageIsSecondaryCreation(config.getUsageIsSecondaryCreation() != null ? config.getUsageIsSecondaryCreation() : 0);
                applyAsset.setUsageAttachmentPath(config.getUsageAttachmentPath());
                assets.add(applyAsset);
            }
            usageApplyAssetRepository.batchSave(assets);
        }

        return convert(usageApply);
    }

    @Override
    @Transactional
    public Long submit(Long id, Long workflowId, Long userId) {
        UsageApply usageApply = usageApplyRepository.findById(id);
        if (usageApply == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿或已驳回状态可以提交
        if (usageApply.getDraft() != 1 && !"REJECTED".equals(usageApply.getStatus())) {
            throw new RuntimeException("只有草稿或已驳回状态可以提交");
        }
        if (!usageApply.getUserId().equals(userId)) {
            throw new RuntimeException("只能提交自己的申请单");
        }

        // 检查是否有至少一个素材
        List<UsageApplyAsset> assets = usageApplyAssetRepository.findByUsageApplyId(id);
        if (assets.isEmpty()) {
            throw new RuntimeException("请至少选择一个素材并配置使用信息");
        }

        usageApply.setWorkflowId(workflowId);
        usageApply.setStatus("PENDING");
        usageApply.setDraft(0);
        usageApplyRepository.update(usageApply);

        // 启动审批流程
        Long instanceId = workflowEngineService.startProcess(workflowId, "ASSET_USAGE", id, userId);
        return instanceId;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        UsageApply usageApply = usageApplyRepository.findById(id);
        if (usageApply == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿状态可以删除
        if (usageApply.getDraft() != 1) {
            throw new RuntimeException("只有草稿状态可以删除");
        }
        if (!usageApply.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的申请单");
        }

        // 删除申请单（Repository会自动清理中间表关联）
        usageApplyRepository.deleteById(id);
    }

    @Override
    public UsageApplyDTO getById(Long id) {
        UsageApply usageApply = usageApplyRepository.findById(id);
        return convert(usageApply);
    }

    @Override
    public PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize) {
        List<UsageApply> list = usageApplyRepository.findDraftsByUserId(userId, (pageNum - 1) * pageSize, pageSize);
        long total = usageApplyRepository.countDraftsByUserId(userId);

        List<UsageApplyDTO> dtoList = list.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title) {
        List<UsageApply> list = usageApplyRepository.findDraftsByUserId(userId, (pageNum - 1) * pageSize, pageSize);
        long total = usageApplyRepository.countDraftsByUserId(userId);

        // 按标题筛选
        List<UsageApply> filteredList = list;
        if (title != null && !title.isEmpty()) {
            final String titleFilter = title;
            filteredList = list.stream()
                .filter(apply -> apply.getTitle() != null && apply.getTitle().contains(titleFilter))
                .collect(Collectors.toList());
        }

        List<UsageApplyDTO> dtoList = filteredList.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, (long) filteredList.size(), pageNum, pageSize);
    }

    @Override
    public PageResult<UsageApplyDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize) {
        List<UsageApply> list = usageApplyRepository.findByUserId(userId, (pageNum - 1) * pageSize, pageSize);
        long total = usageApplyRepository.countByUserId(userId);

        List<UsageApplyDTO> dtoList = list.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    // ========== 旧API（保持兼容） ==========

    @Override
    @Transactional
    public UsageApplyDTO apply(UsageApplyCmd cmd, Long userId) {
        // 旧API适配
        return createDraft(cmd, userId);
    }

    @Override
    public PageResult<UsageApplyDTO> queryMyApplications(UsageApplyQueryCmd cmd, Long userId) {
        int offset = (cmd.getPageNum() - 1) * cmd.getPageSize();
        List<UsageApply> list = usageApplyRepository.findByCondition(cmd.getStatus(), offset, cmd.getPageSize());
        list = list.stream().filter(apply -> apply.getUserId().equals(userId)).collect(Collectors.toList());
        long total = usageApplyRepository.countByCondition(cmd.getStatus());

        List<UsageApplyDTO> dtoList = list.stream().map(this::convert).collect(Collectors.toList());
        return PageResult.of(dtoList, total, cmd.getPageNum(), cmd.getPageSize());
    }

    @Override
    public boolean canUseAsset(Long assetId, Long userId) {
        logger.info("检查用户下载权限 - assetId: {}, userId: {}", assetId, userId);

        // 首先检查素材状态：DELETED状态或软删除的素材不可使用
        AssetDO asset = assetMapper.selectById(assetId);
        if (asset == null) {
            logger.info("素材不存在 - assetId: {}", assetId);
            return false;
        }
        if (asset.getDeleted() != null && asset.getDeleted() == 1) {
            logger.info("素材已被软删除，不可使用 - assetId: {}", assetId);
            return false;
        }
        if ("DELETED".equals(asset.getStatus())) {
            logger.info("素材状态为DELETED，不可使用 - assetId: {}", assetId);
            return false;
        }

        // 通过中间表检查是否有已通过的申请
        List<UsageApplyAsset> applyAssets = usageApplyAssetRepository.findByAssetId(assetId);
        logger.info("查询到 {} 条使用申请记录", applyAssets.size());

        for (UsageApplyAsset applyAsset : applyAssets) {
            logger.info("检查使用申请 - usageApplyId: {}, assetId: {}", applyAsset.getUsageApplyId(), applyAsset.getAssetId());
            UsageApply apply = usageApplyRepository.findById(applyAsset.getUsageApplyId());
            if (apply != null) {
                logger.info("使用申请详情 - userId: {}, status: {}, 申请用户匹配: {}, 状态匹配: {}",
                    apply.getUserId(), apply.getStatus(),
                    apply.getUserId().equals(userId),
                    "APPROVED".equals(apply.getStatus()));
            }
            if (apply != null && apply.getUserId().equals(userId) && "APPROVED".equals(apply.getStatus())) {
                logger.info("用户有下载权限");
                return true;
            }
        }

        logger.info("用户无下载权限");
        return false;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        UsageApply usageApply = usageApplyRepository.findById(id);
        if (usageApply == null) {
            throw new RuntimeException("申请单不存在");
        }
        usageApply.setStatus(status);
        usageApplyRepository.update(usageApply);
    }

    private UsageApplyDTO convert(UsageApply usageApply) {
        if (usageApply == null) return null;
        UsageApplyDTO dto = new UsageApplyDTO();
        BeanUtils.copyProperties(usageApply, dto);

        // 填充用户名称
        if (usageApply.getUserId() != null) {
            UserDO user = userMapper.selectById(usageApply.getUserId());
            if (user != null) {
                dto.setUserName(user.getRealName());
            }
        }

        // 填充关联的素材（带使用配置信息）
        if (usageApply.getAssets() != null) {
            List<UsageApplyDTO.AssetUsageConfigDTO> assetDTOs = usageApply.getAssets().stream().map(asset -> {
                UsageApplyDTO.AssetUsageConfigDTO assetDTO = new UsageApplyDTO.AssetUsageConfigDTO();
                assetDTO.setAssetId(asset.getAssetId());
                assetDTO.setAssetName(asset.getAssetName());
                assetDTO.setAssetType(asset.getAssetType());
                assetDTO.setAssetFilePath(asset.getAssetFilePath());
                assetDTO.setAssetThumbnailPath(asset.getAssetThumbnailPath());
                assetDTO.setAssetStatus(asset.getAssetStatus());
                assetDTO.setUsageDescription(asset.getUsageDescription());
                assetDTO.setUsagePublishChannel(asset.getUsagePublishChannel());
                assetDTO.setUsageIsSecondaryCreation(asset.getUsageIsSecondaryCreation());
                assetDTO.setUsageAttachmentPath(asset.getUsageAttachmentPath());
                return assetDTO;
            }).collect(Collectors.toList());
            dto.setAssets(assetDTOs);
        }

        return dto;
    }
}
