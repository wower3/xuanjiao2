package com.xuanjiao.app.deletion.impl;

import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.AssetDeletionApplicationCmd;
import com.xuanjiao.client.AssetDeletionApplicationDTO;
import com.xuanjiao.client.AssetDeletionAssetDTO;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.domain.asset.entity.Asset;
import com.xuanjiao.domain.asset.repository.AssetRepository;
import com.xuanjiao.domain.deletion.entity.AssetDeletionApplication;
import com.xuanjiao.domain.deletion.entity.AssetDeletionAsset;
import com.xuanjiao.domain.deletion.repository.AssetDeletionApplicationRepository;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetQuery;
import com.xuanjiao.infrastructure.user.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 素材删除申请服务实现类
 * <p>实现AssetDeletionApplicationService接口，封装素材删除申请业务逻辑</p>
 * <p>核心功能：删除申请CRUD、提交审批、审批通过后设置素材删除时间</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.deletion.AssetDeletionApplicationService
 */
@Service
public class AssetDeletionApplicationServiceImpl implements AssetDeletionApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(AssetDeletionApplicationServiceImpl.class);

    @Autowired
    private AssetDeletionApplicationRepository deletionApplicationRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private AssetDeletionAssetMapper deletionAssetMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DeptMapper deptMapper;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Override
    @Transactional
    public AssetDeletionApplicationDTO create(AssetDeletionApplicationCmd cmd, Long userId) {
        // 获取当前用户信息
        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 创建删除申请单
        AssetDeletionApplication application = new AssetDeletionApplication();
        application.setTitle(cmd.getTitle());
        application.setApplicantId(userId);
        application.setDeptId(currentUser.getDeptId());
        application.setWorkflowId(cmd.getWorkflowId());
        application.setStatus("DRAFT");
        application.setDeleteReason(cmd.getDeleteReason());
        application.setAttachmentPath(cmd.getAttachmentPath());
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        deletionApplicationRepository.save(application);

        // 保存素材关联
        if (cmd.getAssetIds() != null && !cmd.getAssetIds().isEmpty()) {
            List<AssetDeletionAsset> assets = new ArrayList<>();
            for (Long assetId : cmd.getAssetIds()) {
                AssetDO asset = assetMapper.selectById(assetId);
                if (asset == null) {
                    throw new RuntimeException("素材不存在: " + assetId);
                }

                // 只能删除已通过审批的素材
                if (!"APPROVED".equals(asset.getStatus())) {
                    throw new RuntimeException("只能删除已通过审批的素材: " + asset.getName());
                }

                AssetDeletionAsset deletionAsset = new AssetDeletionAsset();
                deletionAsset.setDeletionApplicationId(application.getId());
                deletionAsset.setAssetId(assetId);
                deletionAsset.setAssetName(asset.getName());
                deletionAsset.setAssetType(asset.getType());
                assets.add(deletionAsset);
            }
            batchSaveDeletionAssets(assets);
        }

        return convertToDTO(application);
    }

    @Override
    @Transactional
    public AssetDeletionApplicationDTO update(Long id, AssetDeletionApplicationCmd cmd) {
        AssetDeletionApplication application = deletionApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿状态可以修改
        if (!"DRAFT".equals(application.getStatus())) {
            throw new RuntimeException("只有草稿状态可以修改");
        }

        // 更新基本信息
        application.setTitle(cmd.getTitle());
        application.setWorkflowId(cmd.getWorkflowId());
        application.setDeleteReason(cmd.getDeleteReason());
        application.setAttachmentPath(cmd.getAttachmentPath());
        application.setUpdateTime(LocalDateTime.now());

        deletionApplicationRepository.update(application);

        // 先清除之前关联的素材
        deleteDeletionAssetsByApplicationId(id);

        // 保存新的素材关联
        if (cmd.getAssetIds() != null && !cmd.getAssetIds().isEmpty()) {
            List<AssetDeletionAsset> assets = new ArrayList<>();
            for (Long assetId : cmd.getAssetIds()) {
                AssetDO asset = assetMapper.selectById(assetId);
                if (asset == null) {
                    throw new RuntimeException("素材不存在: " + assetId);
                }

                // 只能删除已通过审批的素材
                if (!"APPROVED".equals(asset.getStatus())) {
                    throw new RuntimeException("只能删除已通过审批的素材: " + asset.getName());
                }

                AssetDeletionAsset deletionAsset = new AssetDeletionAsset();
                deletionAsset.setDeletionApplicationId(id);
                deletionAsset.setAssetId(assetId);
                deletionAsset.setAssetName(asset.getName());
                deletionAsset.setAssetType(asset.getType());
                assets.add(deletionAsset);
            }
            batchSaveDeletionAssets(assets);
        }

        return convertToDTO(application);
    }

    @Override
    public AssetDeletionApplicationDTO getById(Long id) {
        AssetDeletionApplication application = deletionApplicationRepository.findById(id);
        if (application == null) {
            return null;
        }
        return convertToDTO(application);
    }

    @Override
    public PageResult<AssetDeletionApplicationDTO> getMyApplications(String title, String status,
                                                                     Integer pageNum, Integer pageSize,
                                                                     Long userId) {
        // 分页查询
        int offset = (pageNum - 1) * pageSize;
        List<AssetDeletionApplication> applications = deletionApplicationRepository.findByApplicant(userId, offset, pageSize);
        long total = deletionApplicationRepository.countByApplicant(userId);

        List<AssetDeletionApplicationDTO> dtoList = applications.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, total, pageNum, pageSize);
    }

    @Override
    public PageResult<AssetDeletionApplicationDTO> queryDrafts(Long userId, Integer pageNum, Integer pageSize, String title) {
        // 查询草稿状态的申请（状态为DRAFT）
        List<AssetDeletionApplication> applications = deletionApplicationRepository.findByApplicant(userId, (pageNum - 1) * pageSize, pageSize);
        List<AssetDeletionApplication> filteredList = applications.stream()
            .filter(app -> "DRAFT".equals(app.getStatus()))
            .collect(Collectors.toList());

        // 按标题筛选
        if (StringUtils.hasText(title)) {
            filteredList = filteredList.stream()
                .filter(app -> app.getTitle() != null && app.getTitle().contains(title))
                .collect(Collectors.toList());
        }

        long total = deletionApplicationRepository.countByApplicant(userId);

        List<AssetDeletionApplicationDTO> dtoList = filteredList.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, (long) filteredList.size(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AssetDeletionApplication application = deletionApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿或已驳回状态可以删除
        if (!"DRAFT".equals(application.getStatus()) && !"REJECTED".equals(application.getStatus())) {
            throw new RuntimeException("只有草稿或已驳回状态可以删除");
        }

        // 先删除关联的素材
        deleteDeletionAssetsByApplicationId(id);

        // 删除申请单
        deletionApplicationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Long submitApproval(Long id, Long workflowId, Long userId) {
        AssetDeletionApplication application = deletionApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 只有草稿或已驳回状态可以提交
        if (!"DRAFT".equals(application.getStatus()) && !"REJECTED".equals(application.getStatus())) {
            throw new RuntimeException("只有草稿或已驳回状态可以提交");
        }

        if (!application.getApplicantId().equals(userId)) {
            throw new RuntimeException("只能提交自己的申请单");
        }

        // 更新状态为待审批
        application.setStatus("PENDING");
        application.setWorkflowId(workflowId);
        application.setUpdateTime(LocalDateTime.now());
        deletionApplicationRepository.update(application);

        // 创建审批实例
        Long instanceId = workflowEngineService.startProcess(workflowId, "ASSET_DELETION", id, userId);
        return instanceId;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        AssetDeletionApplication application = deletionApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }

        application.setStatus(status);
        application.setUpdateTime(LocalDateTime.now());
        deletionApplicationRepository.update(application);
    }

    @Override
    @Transactional
    public void approveDeletion(Long id) {
        logger.info("开始处理素材删除审批通过: applicationId={}", id);

        AssetDeletionApplication application = deletionApplicationRepository.findById(id);
        if (application == null) {
            throw new RuntimeException("申请单不存在");
        }

        // 获取关联的所有素材
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        query.setDeletionApplicationId(id);
        List<com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO> deletionAssetDOs =
            deletionAssetMapper.selectList(query);

        if (deletionAssetDOs == null || deletionAssetDOs.isEmpty()) {
            logger.warn("申请单没有关联任何素材: applicationId={}", id);
            return;
        }

        // 更新所有关联素材的状态为DELETED，并设置删除审批时间
        for (com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO deletionAssetDO : deletionAssetDOs) {
            Long assetId = deletionAssetDO.getAssetId();
            Asset asset = assetRepository.findById(assetId);

            if (asset != null) {
                logger.info("标记素材为已删除: assetId={}, assetName={}", assetId, asset.getName());
                asset.setStatus("DELETED");

                // 需要通过AssetMapper直接更新deletionApproveTime字段
                AssetDO assetDO = new AssetDO();
                assetDO.setId(assetId);
                assetDO.setStatus("DELETED");
                assetDO.setDeletionApproveTime(LocalDateTime.now());
                assetMapper.updateById(assetDO);

                assetRepository.update(asset);
            } else {
                logger.warn("素材不存在，跳过: assetId={}", assetId);
            }
        }

        // 更新申请单状态
        application.setStatus("APPROVED");
        application.setUpdateTime(LocalDateTime.now());
        deletionApplicationRepository.update(application);

        logger.info("素材删除审批通过处理完成: applicationId={}, updatedAssetCount={}",
            id, deletionAssetDOs.size());
    }

    @Override
    @Transactional
    public Long copyApplication(Long id, Long userId) {
        // 1. 获取原申请单
        AssetDeletionApplication original = deletionApplicationRepository.findById(id);
        if (original == null) {
            throw new RuntimeException("原申请单不存在");
        }

        // 2. 创建新申请单（草稿状态）
        AssetDeletionApplication newApplication = new AssetDeletionApplication();
        newApplication.setTitle(original.getTitle() + " - 副本");
        newApplication.setApplicantId(userId);
        UserDO currentUser = userMapper.selectById(userId);
        if (currentUser != null) {
            newApplication.setDeptId(currentUser.getDeptId());
        }
        newApplication.setStatus("DRAFT");
        newApplication.setDeleteReason(original.getDeleteReason());
        newApplication.setAttachmentPath(original.getAttachmentPath());
        newApplication.setCreateTime(LocalDateTime.now());
        newApplication.setUpdateTime(LocalDateTime.now());

        AssetDeletionApplication saved = deletionApplicationRepository.save(newApplication);

        // 3. 复制素材关联（只复制引用，不复制文件）
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        query.setDeletionApplicationId(id);
        List<com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO> originalAssets =
            deletionAssetMapper.selectList(query);

        for (com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO originalAsset : originalAssets) {
            com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO newAsset =
                new com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO();
            newAsset.setDeletionApplicationId(saved.getId());
            newAsset.setAssetId(originalAsset.getAssetId());
            newAsset.setAssetName(originalAsset.getAssetName());
            newAsset.setAssetType(originalAsset.getAssetType());
            deletionAssetMapper.insert(newAsset);
        }

        return saved.getId();
    }

    /**
     * 批量保存删除申请素材关联
     */
    private void batchSaveDeletionAssets(List<AssetDeletionAsset> assets) {
        for (AssetDeletionAsset asset : assets) {
            com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO assetDO =
                new com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO();
            BeanUtils.copyProperties(asset, assetDO);
            deletionAssetMapper.insert(assetDO);
        }
    }

    /**
     * 删除申请单的所有素材关联
     */
    private void deleteDeletionAssetsByApplicationId(Long applicationId) {
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        query.setDeletionApplicationId(applicationId);
        deletionAssetMapper.delete(query);
    }

    /**
     * 转换为DTO
     */
    private AssetDeletionApplicationDTO convertToDTO(AssetDeletionApplication application) {
        AssetDeletionApplicationDTO dto = new AssetDeletionApplicationDTO();
        BeanUtils.copyProperties(application, dto);

        // 查询申请人信息
        UserDO applicant = userMapper.selectById(application.getApplicantId());
        if (applicant != null) {
            dto.setApplicantName(applicant.getRealName());
        }

        // 查询部门信息
        if (application.getDeptId() != null) {
            DeptDO dept = deptMapper.selectById(application.getDeptId());
            if (dept != null) {
                dto.setDeptName(dept.getName());
            }
        }

        // 查询关联的素材
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        query.setDeletionApplicationId(application.getId());
        List<com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO> deletionAssetDOs =
            deletionAssetMapper.selectList(query);

        List<AssetDeletionAssetDTO> assetDTOs = new ArrayList<>();
        for (com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO deletionAssetDO : deletionAssetDOs) {
            AssetDeletionAssetDTO assetDTO = new AssetDeletionAssetDTO();
            assetDTO.setId(deletionAssetDO.getId());
            assetDTO.setDeletionApplicationId(deletionAssetDO.getDeletionApplicationId());
            assetDTO.setAssetId(deletionAssetDO.getAssetId());
            assetDTO.setAssetName(deletionAssetDO.getAssetName() != null ? deletionAssetDO.getAssetName() : "");
            assetDTO.setAssetType(deletionAssetDO.getAssetType() != null ? deletionAssetDO.getAssetType() : "");

            // 查询素材详细信息
            AssetDO asset = assetMapper.selectById(deletionAssetDO.getAssetId());
            if (asset != null) {
                assetDTO.setFilePath(asset.getFilePath());
                assetDTO.setThumbnailPath(asset.getThumbnailPath());
            }

            assetDTOs.add(assetDTO);
        }
        dto.setAssets(assetDTOs);

        return dto;
    }
}
