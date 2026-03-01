package com.xuanjiao.app.usage.impl;

import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetQuery;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.usage.UsageApplyCmd;
import com.xuanjiao.client.usage.UsageApplyDTO;
import com.xuanjiao.client.usage.UsageApplyQueryCmd;
import com.xuanjiao.domain.usage.entity.UsageApply;
import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import com.xuanjiao.domain.usage.repository.UsageApplyAssetRepository;
import com.xuanjiao.domain.usage.repository.UsageApplyRepository;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyQuery;
import com.xuanjiao.infrastructure.usage.UsageApplyWithDetailsDO;
import com.xuanjiao.common.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.xuanjiao.common.exception.BusinessException;
import com.xuanjiao.common.exception.NotFoundException;

/**
 * 素材使用申请服务实现类
 * <p>实现UsageApplyService接口，封装素材使用申请业务逻辑</p>
 * <p>核心功能：使用申请CRUD、草稿管理、提交审批、多素材支持</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.usage.UsageApplyService
 */
@Service
public class UsageApplyServiceImpl implements UsageApplyService {

    private static final Logger logger = LoggerFactory.getLogger(UsageApplyServiceImpl.class);

    @Autowired
    private UsageApplyRepository usageApplyRepository;

    @Autowired
    private UsageApplyAssetRepository usageApplyAssetRepository;

    @Autowired
    private com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper usageApplyAssetMapper;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UsageApplyMapper usageApplyMapper;

    // ========== 新API（按素材配置使用信息） ==========

    @Override
    @Transactional
    public UsageApplyDTO createDraft(UsageApplyCmd cmd, Long userId) {
        // 获取当前用户信息
        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new NotFoundException("用户不存在");
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
                    throw new NotFoundException("素材不存在: " + config.getAssetId());
                }

                // 检查素材状态：只能使用APPROVED状态的素材
                // DRAFT/PENDING/DELETED状态的素材不允许使用
                // 软删除（deleted=1）的素材不允许使用
                if (asset.getDeleted() != null && asset.getDeleted() == 1) {
                    throw new BusinessException("该素材已被删除，无法使用: " + asset.getName());
                }
                if (!"APPROVED".equals(asset.getStatus())) {
                    String statusMsg = "DRAFT".equals(asset.getStatus()) ? "草稿" :
                                     "PENDING".equals(asset.getStatus()) ? "待审批" :
                                     "DELETED".equals(asset.getStatus()) ? "已删除" : asset.getStatus();
                    throw new BusinessException("只能使用已通过审批的素材，当前素材状态为" + statusMsg + ": " + asset.getName());
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
            throw new NotFoundException("申请单不存在");
        }

        // 只有草稿状态可以修改
        if (usageApply.getDraft() != 1) {
            throw new BusinessException("只有草稿状态可以修改");
        }
        if (!usageApply.getUserId().equals(userId)) {
            throw new BusinessException("只能修改自己的申请单");
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
                    throw new NotFoundException("素材不存在: " + config.getAssetId());
                }

                // 检查素材状态：只能使用APPROVED状态的素材
                if (asset.getDeleted() != null && asset.getDeleted() == 1) {
                    throw new BusinessException("该素材已被删除，无法使用: " + asset.getName());
                }
                if (!"APPROVED".equals(asset.getStatus())) {
                    String statusMsg = "DRAFT".equals(asset.getStatus()) ? "草稿" :
                                     "PENDING".equals(asset.getStatus()) ? "待审批" :
                                     "DELETED".equals(asset.getStatus()) ? "已删除" : asset.getStatus();
                    throw new BusinessException("只能使用已通过审批的素材，当前素材状态为" + statusMsg + ": " + asset.getName());
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
        logger.info("UsageApply.submit - 开始提交，id: {}, workflowId: {}, userId: {}", id, workflowId, userId);

        UsageApply usageApply = usageApplyRepository.findById(id);

        logger.info("UsageApply.submit - 查询结果: {}", usageApply != null ? usageApply.getId() : "null");

        if (usageApply == null) {
            logger.error("UsageApply.submit - 申请单不存在，id: {}", id);
            throw new NotFoundException("申请单不存在");
        }

        // 只有草稿或已驳回状态可以提交
        if (usageApply.getDraft() != 1 && !"REJECTED".equals(usageApply.getStatus())) {
            throw new BusinessException("只有草稿或已驳回状态可以提交");
        }
        if (!usageApply.getUserId().equals(userId)) {
            throw new BusinessException("只能提交自己的申请单");
        }

        // 检查是否有至少一个素材
        List<UsageApplyAsset> assets = usageApplyAssetRepository.findByUsageApplyId(id);
        logger.info("UsageApply.submit - 关联素材数量: {}", assets.size());

        if (assets.isEmpty()) {
            throw new BusinessException("请至少选择一个素材并配置使用信息");
        }

        usageApply.setWorkflowId(workflowId);
        usageApply.setStatus("PENDING");
        usageApply.setDraft(0);
        usageApplyRepository.update(usageApply);

        // 启动审批流程
        Long instanceId = workflowEngineService.startProcess(workflowId, "ASSET_USAGE", id, userId);
        logger.info("UsageApply.submit - 提交成功，instanceId: {}", instanceId);
        return instanceId;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        UsageApply usageApply = usageApplyRepository.findById(id);
        if (usageApply == null) {
            throw new NotFoundException("申请单不存在");
        }

        // 只有草稿状态可以删除
        if (usageApply.getDraft() != 1) {
            throw new BusinessException("只有草稿状态可以删除");
        }
        if (!usageApply.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的申请单");
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
        // 使用JOIN查询一次性获取申请及关联的申请人信息，避免N+1问题
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        query.setDraft(1);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset((pageNum - 1) * pageSize);
        query.setLimit(pageSize);

        List<UsageApplyWithDetailsDO> list = usageApplyMapper.selectListWithDetails(query);
        long total = usageApplyMapper.selectCount(query);

        List<UsageApplyDTO> dtoList = list.stream().map(this::convertWithDetails).collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<UsageApplyDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title) {
        // 使用JOIN查询一次性获取申请及关联的申请人信息，避免N+1问题
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        query.setDraft(1);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset((pageNum - 1) * pageSize);
        query.setLimit(pageSize);
        if (title != null && !title.isEmpty()) {
            query.setTitleKeyword(title);
        }

        List<UsageApplyWithDetailsDO> list = usageApplyMapper.selectListWithDetails(query);
        long total = usageApplyMapper.selectCount(query);

        List<UsageApplyDTO> dtoList = list.stream().map(this::convertWithDetails).collect(Collectors.toList());
        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<UsageApplyDTO> queryMyApplications(Long userId, Integer pageNum, Integer pageSize) {
        // 使用JOIN查询一次性获取申请及关联的申请人信息，避免N+1问题
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset((pageNum - 1) * pageSize);
        query.setLimit(pageSize);

        List<UsageApplyWithDetailsDO> list = usageApplyMapper.selectListWithDetails(query);
        long total = usageApplyMapper.selectCount(query);

        List<UsageApplyDTO> dtoList = list.stream().map(this::convertWithDetails).collect(Collectors.toList());
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
        // 使用JOIN查询一次性获取申请及关联的申请人信息，避免N+1问题
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(userId);
        query.setStatus(cmd.getStatus());
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        query.setOffset((cmd.getPageNum() - 1) * cmd.getPageSize());
        query.setLimit(cmd.getPageSize());

        List<UsageApplyWithDetailsDO> list = usageApplyMapper.selectListWithDetails(query);
        long total = usageApplyMapper.selectCount(query);

        List<UsageApplyDTO> dtoList = list.stream().map(this::convertWithDetails).collect(Collectors.toList());
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
            throw new NotFoundException("申请单不存在");
        }
        usageApply.setStatus(status);
        usageApplyRepository.update(usageApply);
    }

    @Override
    @Transactional
    public Long copyApplication(Long id, Long userId) {
        // 1. 获取原申请单
        UsageApply original = usageApplyRepository.findById(id);
        if (original == null) {
            throw new NotFoundException("原申请单不存在");
        }

        // 2. 创建新申请单（草稿状态）
        UsageApply newApplication = new UsageApply();
        newApplication.setTitle(original.getTitle() + " - 副本");
        newApplication.setUserId(userId);
        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser != null) {
            newApplication.setDeptId(currentUser.getDeptId());
        }
        newApplication.setStatus("DRAFT");
        newApplication.setDraft(1);
        newApplication.setCreateTime(LocalDateTime.now());

        usageApplyRepository.save(newApplication);

        // 3. 复制素材关联配置（只复制引用，不复制文件）
        UsageApplyAssetQuery query = new UsageApplyAssetQuery();
        query.setUsageApplyId(id);
        List<com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO> originalAssets =
            usageApplyAssetMapper.selectList(query);

        for (com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO originalAsset : originalAssets) {
            com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO newAsset =
                new com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO();
            newAsset.setUsageApplyId(newApplication.getId());
            newAsset.setAssetId(originalAsset.getAssetId());
            newAsset.setUsageDescription(originalAsset.getUsageDescription());
            newAsset.setUsagePublishChannel(originalAsset.getUsagePublishChannel());
            newAsset.setUsageIsSecondaryCreation(originalAsset.getUsageIsSecondaryCreation());
            newAsset.setUsageAttachmentPath(originalAsset.getUsageAttachmentPath());
            usageApplyAssetMapper.insert(newAsset);
        }

        return newApplication.getId();
    }

    private UsageApplyDTO convert(UsageApply usageApply) {
        if (usageApply == null) return null;
        UsageApplyDTO dto = new UsageApplyDTO();
        ConvertUtils.copyProperties(usageApply, dto);

        // 填充用户名称
        if (usageApply.getUserId() != null) {
            UserDO user = userMapper.selectById(usageApply.getUserId());
            if (user != null) {
                dto.setUsername(user.getRealName());
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

    /**
     * 将带详情的DO转换为DTO（优化版，使用JOIN查询结果）
     */
    private UsageApplyDTO convertWithDetails(UsageApplyWithDetailsDO details) {
        if (details == null) return null;
        UsageApplyDTO dto = new UsageApplyDTO();
        ConvertUtils.copyProperties(details, dto);

        // 直接从JOIN结果获取申请人姓名，无需额外查询
        dto.setUsername(details.getApplicantName());

        // 直接从JOIN结果获取部门名称
        dto.setDeptName(details.getDeptName());

        // 查询关联的素材（带使用配置信息）
        List<UsageApplyAsset> assets = usageApplyAssetRepository.findByUsageApplyId(details.getId());
        if (assets != null && !assets.isEmpty()) {
            List<UsageApplyDTO.AssetUsageConfigDTO> assetDTOs = assets.stream().map(asset -> {
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
