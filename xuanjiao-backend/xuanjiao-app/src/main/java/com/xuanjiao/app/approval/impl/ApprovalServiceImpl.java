package com.xuanjiao.app.approval.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.app.approval.ApprovalService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.approval.ApprovalProgressDTO;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.approval.FlowItemDTO;
import com.xuanjiao.client.approval.InstanceDetailDTO;
import com.xuanjiao.client.approval.MyAppliedDTO;
import com.xuanjiao.client.approval.PendingTaskDTO;
import com.xuanjiao.client.approval.TaskDetailDTO;
import com.xuanjiao.client.asset.AssetDTO;
import com.xuanjiao.client.user.UserDTO;
import com.xuanjiao.client.workflow.StageApproverDTO;
import com.xuanjiao.client.workflow.SubWorkflowDTO;
import com.xuanjiao.infrastructure.approval.FlowItemDO;
import com.xuanjiao.infrastructure.approval.MyAppliedDO;
import com.xuanjiao.infrastructure.approval.PendingTaskItemDO;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import com.xuanjiao.infrastructure.dataobject.ApprovalProgressDO;
import com.xuanjiao.infrastructure.dataobject.ApprovalTaskDO;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.AssetTagDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.asset.AssetTagMapper;
import com.xuanjiao.infrastructure.asset.AssetTagQuery;
import com.xuanjiao.infrastructure.dataobject.MaterialApplicationDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import com.xuanjiao.infrastructure.approval.ApprovalTaskMapper;
import com.xuanjiao.infrastructure.approval.ApprovalTaskQuery;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceQuery;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverQuery;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.asset.AssetQuery;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.role.RoleQuery;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.material.MaterialApplicationMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper;
import com.xuanjiao.common.exception.NotFoundException;
import com.xuanjiao.common.exception.SystemException;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO;
import com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审批服务实现类
 * <p>实现ApprovalService接口，封装审批业务逻辑</p>
 * <p>核心功能：待办任务查询、审批操作、任务详情、实例详情</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.approval.ApprovalService
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    /** 消息常量 */
    private static final String MSG_INSTANCE_NOT_FOUND = "审批实例不存在";
    private static final String MSG_TASK_NOT_FOUND = "任务不存在";

    /** 审批状态常量 */
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    /** 业务类型常量 */
    private static final String BUSINESS_TYPE_MATERIAL_ENTRY = "MATERIAL_ENTRY";
    private static final String BUSINESS_TYPE_ASSET_USAGE = "ASSET_USAGE";
    private static final String BUSINESS_TYPE_ASSET_DELETION = "ASSET_DELETION";

    @Resource
    private ApprovalTaskMapper taskMapper;
    @Resource
    private ApprovalInstanceMapper instanceMapper;
    @Resource
    private WorkflowMapper workflowMapper;
    @Resource
    private AssetMapper assetMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private WorkflowEngineService workflowEngineService;
    @Resource
    private UsageApplyMapper usageApplyMapper;
    @Resource
    private UsageApplyAssetMapper usageApplyAssetMapper;
    @Resource
    private ApproverSelectionService approverSelectionService;
    @Resource
    private WorkflowStageMapper workflowStageMapper;
    @Resource
    private StageApproverMapper stageApproverMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private MaterialApplicationMapper materialApplicationMapper;
    @Resource
    private AssetDeletionApplicationMapper assetDeletionApplicationMapper;
    @Resource
    private AssetDeletionAssetMapper assetDeletionAssetMapper;

    @Resource
    private AssetTagMapper assetTagMapper;

    @Resource
    private TagMapper tagMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<PendingTaskDTO> getMyTasks(Long userId, int pageNum, int pageSize, String businessType) {
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        // 使用JOIN查询一次性获取所有关联数据，避免N+1问题
        List<PendingTaskItemDO> items = taskMapper.selectPendingTaskPage(userId, businessType, offset, pageSize);
        Long total = taskMapper.selectPendingTaskCount(userId, businessType);
        // 转换为DTO
        List<PendingTaskDTO> list = items.stream()
            .map(this::convertToPendingTaskDTO).collect(Collectors.toList());
        return PageResult.of(list, total, pageNum, pageSize);
    }

    @Override
    public Long getMyTasksCount(Long userId) {
        return taskMapper.selectPendingTaskCount(userId, null);
    }

    @Override
    public PageResult<MyAppliedDTO> getMyApplied(Long userId, int pageNum, int pageSize,
                                                  String businessType, boolean forAllUsers,
                                                  Long applicantId, Long deptId, String roleType,
                                                  String status) {
        // 构建筛选条件
        Long queryApplicantId = null;
        List<Long> queryApplicantIds = null;

        if (forAllUsers) {
            // 查询所有用户的工单，支持筛选
            if (applicantId != null) {
                // 指定了具体的发起人
                queryApplicantId = applicantId;
            } else if (deptId != null || (roleType != null && !roleType.isEmpty())) {
                // 按部门或角色筛选
                queryApplicantIds = getApplicantIdsByFilters(deptId, roleType);
            }
            // 如果都没有指定，则查询所有用户的工单（queryApplicantId 和 queryApplicantIds 都为 null）
        } else {
            // 仅查询当前用户的工单
            queryApplicantId = userId;
        }

        // 使用优化的 JOIN 查询（避免 N+1 问题）
        List<MyAppliedDO> allRecords = instanceMapper.selectMyAppliedList(
            queryApplicantId, queryApplicantIds, businessType, status
        );

        // 内存分页（因为需要先获取所有数据再排序和分页）
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allRecords.size());
        List<MyAppliedDO> pagedRecords = start < allRecords.size()
            ? allRecords.subList(start, end)
            : new ArrayList<>();

        // 转换为 DTO
        List<MyAppliedDTO> result = pagedRecords.stream()
            .map(this::convertMyAppliedToDTO)
            .collect(Collectors.toList());

        return PageResult.of(result, (long) allRecords.size(), pageNum, pageSize);
    }

    /**
     * 根据部门和角色筛选条件获取申请人ID列表
     *
     * @param deptId 部门ID
     * @param roleType 角色类型
     * @return 申请人ID列表
     */
    private List<Long> getApplicantIdsByFilters(Long deptId, String roleType) {
        Set<Long> applicantIds = new HashSet<>();

        if (deptId != null) {
            // 查询该部门下的所有用户
            UserQuery userQuery = new UserQuery();
            userQuery.setDeptId(deptId);
            userQuery.setStatus(1);
            List<UserDO> usersInDept = userMapper.selectList(userQuery);
            for (UserDO user : usersInDept) {
                applicantIds.add(user.getId());
            }
        }

        if (roleType != null && !roleType.isEmpty()) {
            // 根据角色类型查询角色
            RoleQuery roleQuery = new RoleQuery();
            roleQuery.setRoleType(roleType);
            roleQuery.setStatus(1);
            List<RoleDO> roles = roleMapper.selectList(roleQuery);

            if (!roles.isEmpty()) {
                List<Long> roleIds = roles.stream().map(RoleDO::getId).collect(Collectors.toList());
                // 查询拥有这些角色的用户
                UserQuery userQuery = new UserQuery();
                userQuery.setRoleIds(roleIds);
                userQuery.setStatus(1);
                List<UserDO> usersWithRole = userMapper.selectList(userQuery);
                for (UserDO user : usersWithRole) {
                    applicantIds.add(user.getId());
                }
            }
        }

        return new ArrayList<>(applicantIds);
    }

    /**
     * 将 MyAppliedDO 转换为 MyAppliedDTO
     *
     * <p>将数据库查询对象转换为API返回的DTO对象。</p>
     *
     * @param item 我发起的工单数据对象
     * @return 我发起的工单DTO
     */
    private MyAppliedDTO convertMyAppliedToDTO(MyAppliedDO item) {
        MyAppliedDTO dto = new MyAppliedDTO();
        dto.setId(item.getId());
        dto.setStatus(item.getStatus());
        dto.setBusinessType(item.getBusinessType());
        dto.setBusinessId(item.getBusinessId());
        dto.setApplicationId(item.getBusinessId()); // 与businessId相同，用于前端兼容
        dto.setCreateTime(item.getCreateTime());
        dto.setApplicantId(item.getApplicantId());
        dto.setApplicantName(item.getApplicantName());
        dto.setWorkflowId(item.getWorkflowId());
        dto.setWorkflowName(item.getWorkflowName());
        dto.setCurrentStageId(item.getCurrentStageId());
        dto.setCurrentStageName(item.getCurrentStageName());
        dto.setBusinessName(item.getTitle());

        // 转换待审批人信息（逗号分隔字符串 -> List<Map>）
        if (item.getPendingApproverNames() != null && !item.getPendingApproverNames().isEmpty()) {
            String[] names = item.getPendingApproverNames().split(",");
            String[] ids = item.getPendingApproverIds() != null
                ? item.getPendingApproverIds().split(",")
                : new String[0];

            List<Map<String, Object>> pendingApprovers = new ArrayList<>();
            for (int i = 0; i < names.length; i++) {
                Map<String, Object> approver = new HashMap<>();
                approver.put("id", i < ids.length ? Long.valueOf(ids[i].trim()) : null);
                approver.put("name", names[i].trim());
                pendingApprovers.add(approver);
            }
            dto.setPendingApprovers(pendingApprovers);
        }

        return dto;
    }

    @Override
    public InstanceDetailDTO getInstanceDetail(Long instanceId) {
        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new NotFoundException(MSG_INSTANCE_NOT_FOUND);
        }
        Map<String, Object> map = buildInstanceInfo(instance);
        InstanceDetailDTO result = convertToInstanceDetailDTO(map);
        // 添加日志检查返回数据
        if (result.getApprovalProgress() != null) {
            logger.info("getInstanceDetail返回: instanceId={}, approvalProgress={}", instanceId, result.getApprovalProgress());
        }
        return result;
    }

    /**
     * 将 Map 转换为 InstanceDetailDTO
     */
    private InstanceDetailDTO convertToInstanceDetailDTO(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        InstanceDetailDTO dto = new InstanceDetailDTO();
        dto.setId(getLongValue(map, "id"));
        dto.setStatus((String) map.get("status"));
        dto.setBusinessType((String) map.get("businessType"));
        dto.setBusinessId(getLongValue(map, "businessId"));
        dto.setCreateTime(getLocalDateTimeValue(map, "createTime"));
        dto.setWorkflowId(getLongValue(map, "workflowId"));
        dto.setWorkflowName((String) map.get("workflowName"));
        dto.setApplicationId(getLongValue(map, "applicationId"));
        dto.setApplicationTitle((String) map.get("applicationTitle"));
        dto.setBusinessName((String) map.get("businessName"));
        dto.setAssetType((String) map.get("assetType"));
        dto.setAssetStatus((String) map.get("assetStatus"));
        dto.setAssetStatus((String) map.get("assetStatus"));
        dto.setAssetCount((Integer) map.get("assetCount"));
        dto.setCurrentStageId(getLongValue(map, "currentStageId"));
        dto.setCurrentStageName((String) map.get("currentStageName"));

        // 申请人信息
        UserDTO applicant = new UserDTO();
        applicant.setId(getLongValue(map, "applicantId"));
        applicant.setRealName((String) map.get("applicantName"));
        dto.setApplicant(applicant);

        // 审批进度
        dto.setApprovalProgress((List<ApprovalProgressDTO>) map.get("approvalProgress"));

        // 素材列表
        dto.setAssets((List<AssetDTO>) map.get("assets"));

        return dto;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
    }

    @Override
    @Transactional
    public void approve(Long taskId, Long userId, String comment, boolean passed) {
        logger.info("开始审批: taskId={}, userId={}, passed={}, comment={}", taskId, userId, passed, comment);

        try {
            // 获取任务和实例信息（在调用工作流引擎前获取，因为调用后实例状态可能变化）
            ApprovalTaskDO task = taskMapper.selectById(taskId);
            if (task == null) {
                logger.error("任务不存在: taskId={}", taskId);
                throw new NotFoundException("任务不存在: " + taskId);
            }

            ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());
            if (instance == null) {
                logger.error("审批实例不存在: instanceId={}", task.getInstanceId());
                throw new NotFoundException("审批实例不存在: " + task.getInstanceId());
            }

            String businessType = instance.getBusinessType();
            Long businessId = instance.getBusinessId();
            logger.info("审批前信息: instanceId={}, businessType={}, businessId={}",
                instance.getId(), businessType, businessId);

            // 调用工作流引擎完成任务
            workflowEngineService.completeTask(taskId, userId, passed, comment);
            // Note: Business status updates (strategy pattern) are handled by WorkflowEngineServiceImpl when the entire workflow completes


            logger.info("审批处理完成: taskId={}, userId={}", taskId, userId);
        } catch (Exception e) {
            logger.error("审批处理失败: taskId={}, userId={}, error={}", taskId, userId, e.getMessage(), e);
            throw new SystemException("审批处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnTask(Long taskId, Long userId, String comment) {
        logger.info("开始退回上一级: taskId={}, userId={}, comment={}", taskId, userId, comment);

        try {
            // 调用工作流引擎执行退回
            workflowEngineService.returnTask(taskId, userId, comment);

            logger.info("退回处理完成: taskId={}, userId={}", taskId, userId);
        } catch (Exception e) {
            logger.error("退回处理失败: taskId={}, userId={}, error={}", taskId, userId, e.getMessage(), e);
            throw new SystemException("退回处理失败: " + e.getMessage(), e);
        }
    }

    /**
    /**
     * 将待办任务数据对象转换为DTO（优化版，使用JOIN查询结果）
     *
     * <p>将PendingTaskItemDO（JOIN查询结果）转换为PendingTaskDTO。</p>
     * <p>此方法替代buildTaskInfo，避免在循环中进行数据库查询。</p>
     *
     * @param item 待办任务数据对象
     * @return 待办任务DTO
     */
    private PendingTaskDTO convertToPendingTaskDTO(PendingTaskItemDO item) {
        PendingTaskDTO dto = new PendingTaskDTO();
        dto.setId(item.getTaskId());
        dto.setStatus(item.getTaskStatus());
        dto.setCreateTime(item.getTaskCreateTime());
        dto.setTaskType(item.getTaskType());
        dto.setApproverId(item.getApproverId());
        dto.setStageId(item.getStageId());
        dto.setSubWorkflowApproverIds(item.getSubWorkflowApproverIds());
        dto.setInstanceId(item.getInstanceId());
        dto.setBusinessType(item.getBusinessType());
        dto.setBusinessId(item.getBusinessId());
        dto.setWorkflowId(item.getWorkflowId());
        dto.setWorkflowName(item.getWorkflowName());
        dto.setApplicantId(item.getApplicantId());
        dto.setApplicantName(item.getApplicantName());

        // 根据业务类型设置业务名称和申请单信息
        if (BUSINESS_TYPE_MATERIAL_ENTRY.equals(item.getBusinessType())) {
            dto.setApplicationId(item.getMaterialApplicationId());
            dto.setApplicationTitle(item.getMaterialApplicationTitle());
            dto.setBusinessName(item.getMaterialApplicationTitle());
            dto.setAssetCount(item.getAssetCount());
            dto.setAssetType(item.getAssetType());
        } else if (BUSINESS_TYPE_ASSET_USAGE.equals(item.getBusinessType())) {
            dto.setApplicationId(item.getUsageApplyId());
            dto.setApplicationTitle(item.getUsageApplyTitle());
            dto.setBusinessName(item.getUsageApplyTitle());
        } else if (BUSINESS_TYPE_ASSET_DELETION.equals(item.getBusinessType())) {
            dto.setApplicationId(item.getDeletionApplicationId());
            dto.setApplicationTitle(item.getDeletionApplicationTitle());
            dto.setBusinessName(item.getDeletionApplicationTitle());
        }

        return dto;
    }

    private Map<String, Object> buildInstanceInfo(ApprovalInstanceDO instance) {
        Map<String, Object> map = new HashMap<>();

        // 基础信息
        map.put("id", instance.getId());
        map.put("status", instance.getStatus());
        map.put("businessType", instance.getBusinessType());
        map.put("businessId", instance.getBusinessId());
        map.put("createTime", instance.getCreateTime());

        // 流程信息
        populateWorkflowInfo(map, instance);

        // 业务详情
        populateBusinessInfo(map, instance);

        // 申请人信息
        populateApplicantInfo(map, instance);

        // 当前阶段信息
        populateCurrentStageInfo(map, instance);

        // 待审批审批人
        populatePendingApprovers(map, instance);

        // 审批进度
        List<ApprovalProgressDTO> progress = approverSelectionService.getApprovalProgress(instance.getId());
        map.put("approvalProgress", progress);

        return map;
    }

    /**
     * 填充流程信息
     */
    private void populateWorkflowInfo(Map<String, Object> map, ApprovalInstanceDO instance) {
        WorkflowDO workflow = workflowMapper.selectById(instance.getWorkflowId());
        if (workflow != null) {
            map.put("workflowName", workflow.getName());
            map.put("workflowId", workflow.getId());
        }
    }

    /**
     * 填充业务详情
     */
    private void populateBusinessInfo(Map<String, Object> map, ApprovalInstanceDO instance) {
        String businessType = instance.getBusinessType();
        if (businessType == null) {
            return;
        }

        switch (businessType) {
            case BUSINESS_TYPE_MATERIAL_ENTRY:
                populateMaterialEntryInfo(map, instance.getBusinessId());
                break;
            case BUSINESS_TYPE_ASSET_USAGE:
                populateAssetUsageInfo(map, instance.getBusinessId());
                break;
            case BUSINESS_TYPE_ASSET_DELETION:
                populateAssetDeletionInfo(map, instance.getBusinessId());
                break;
        }
    }

    /**
     * 填充素材录入业务详情
     */
    private void populateMaterialEntryInfo(Map<String, Object> map, Long businessId) {
        MaterialApplicationDO application = materialApplicationMapper.selectById(businessId);
        if (application == null) {
            return;
        }

        map.put("applicationId", application.getId());
        map.put("applicationTitle", application.getTitle());
        map.put("businessName", application.getTitle());

        AssetQuery assetQuery = new AssetQuery();
        assetQuery.setApplicationId(application.getId());
        List<AssetDO> assets = assetMapper.selectList(assetQuery);

        if (assets != null && !assets.isEmpty()) {
            AssetDO firstAsset = assets.get(0);
            map.put("assetType", firstAsset.getType());
            map.put("assetStatus", firstAsset.getStatus());
            map.put("assetCount", assets.size());

            List<Map<String, Object>> assetList = buildAssetListWithTags(assets);
            map.put("assets", assetList);
        }
    }

    /**
     * 构建素材列表（带标签）
     */
    private List<Map<String, Object>> buildAssetListWithTags(List<AssetDO> assets) {
        List<Long> assetIds = assets.stream().map(AssetDO::getId).collect(Collectors.toList());
        List<AssetTagDO> assetTags = assetTagMapper.selectByAssetIds(assetIds);

        List<Long> tagIds = assetTags.stream()
            .map(AssetTagDO::getTagId)
            .distinct()
            .collect(Collectors.toList());
        List<TagDO> tags = tagIds.isEmpty() ? new ArrayList<>() : tagMapper.selectBatchIds(tagIds);

        Map<Long, TagDO> tagMap = tags.stream()
            .collect(Collectors.toMap(TagDO::getId, t -> t));

        Map<Long, List<Map<String, Object>>> tagsMap = assetTags.stream()
            .collect(Collectors.groupingBy(AssetTagDO::getAssetId,
                Collectors.mapping(tag -> {
                    Map<String, Object> tagInfo = new HashMap<>();
                    TagDO tagDetail = tagMap.get(tag.getTagId());
                    tagInfo.put("id", tagDetail.getId());
                    tagInfo.put("name", tagDetail.getName());
                    return tagInfo;
                },
                Collectors.toList())));

        List<Map<String, Object>> assetList = new ArrayList<>();
        for (AssetDO asset : assets) {
            Map<String, Object> assetInfo = new HashMap<>();
            assetInfo.put("id", asset.getId());
            assetInfo.put("name", asset.getName());
            assetInfo.put("type", asset.getType());
            assetInfo.put("status", asset.getStatus());
            assetInfo.put("filePath", asset.getFilePath());
            assetInfo.put("thumbnailPath", asset.getThumbnailPath());
            assetInfo.put("fileSize", asset.getFileSize());
            assetInfo.put("description", asset.getDescription());
            assetInfo.put("publishChannel", asset.getPublishChannel());
            assetInfo.put("tags", tagsMap.getOrDefault(asset.getId(), new ArrayList<>()));
            assetInfo.put("copyrightFilePath", asset.getCopyrightFilePath());
            assetList.add(assetInfo);
        }
        return assetList;
    }

    /**
     * 填充素材使用业务详情
     */
    private void populateAssetUsageInfo(Map<String, Object> map, Long businessId) {
        List<UsageApplyAssetDO> applyAssets = usageApplyAssetMapper.findByUsageApplyIdWithAsset(businessId);
        if (applyAssets != null && !applyAssets.isEmpty()) {
            UsageApplyAssetDO firstAsset = applyAssets.get(0);
            map.put("businessName", "使用申请：" + firstAsset.getAssetName());
            map.put("assetType", firstAsset.getAssetType());
            map.put("assetId", firstAsset.getAssetId());
            map.put("assetCount", applyAssets.size());
        }
    }

    /**
     * 填充素材删除业务详情
     */
    private void populateAssetDeletionInfo(Map<String, Object> map, Long businessId) {
        AssetDeletionApplicationDO deletionApplication = assetDeletionApplicationMapper.selectById(businessId);
        if (deletionApplication == null) {
            return;
        }

        populateDeletionApplicationInfo(map, deletionApplication);
        populateDeletionAssetsInfo(map, businessId);
    }

    /**
     * 填充删除申请信息
     */
    private void populateDeletionApplicationInfo(Map<String, Object> map, AssetDeletionApplicationDO deletionApplication) {
        map.put("applicationId", deletionApplication.getId());
        map.put("applicationTitle", deletionApplication.getTitle());
        map.put("businessName", deletionApplication.getTitle());
        map.put("deleteReason", deletionApplication.getDeleteReason());
    }

    /**
     * 填充删除的素材信息
     */
    private void populateDeletionAssetsInfo(Map<String, Object> map, Long businessId) {
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        query.setDeletionApplicationId(businessId);
        List<AssetDeletionAssetDO> deletionAssets = assetDeletionAssetMapper.selectList(query);

        if (deletionAssets == null || deletionAssets.isEmpty()) {
            return;
        }

        map.put("assetCount", deletionAssets.size());
        List<Map<String, Object>> assetList = buildDeletionAssetList(deletionAssets);
        map.put("assets", assetList);
    }

    /**
     * 构建删除素材列表
     */
    private List<Map<String, Object>> buildDeletionAssetList(List<AssetDeletionAssetDO> deletionAssets) {
        List<Long> assetIds = deletionAssets.stream()
            .map(AssetDeletionAssetDO::getAssetId)
            .collect(Collectors.toList());
        List<AssetDO> assetDOList = assetMapper.selectByIds(assetIds);

        Map<Long, AssetDO> assetMap = assetDOList.stream()
            .collect(Collectors.toMap(AssetDO::getId, a -> a));

        List<Map<String, Object>> assetList = new ArrayList<>();
        for (AssetDeletionAssetDO deletionAsset : deletionAssets) {
            AssetDO asset = assetMap.get(deletionAsset.getAssetId());
            if (asset != null) {
                assetList.add(buildAssetInfoMap(asset));
            }
        }
        return assetList;
    }

    /**
     * 构建素材信息 Map
     */
    private Map<String, Object> buildAssetInfoMap(AssetDO asset) {
        Map<String, Object> assetInfo = new HashMap<>();
        assetInfo.put("id", asset.getId());
        assetInfo.put("name", asset.getName());
        assetInfo.put("type", asset.getType());
        assetInfo.put("status", asset.getStatus());
        assetInfo.put("filePath", asset.getFilePath());
        assetInfo.put("thumbnailPath", asset.getThumbnailPath());
        assetInfo.put("fileSize", asset.getFileSize());
        assetInfo.put("description", asset.getDescription());
        assetInfo.put("publishChannel", asset.getPublishChannel());
        return assetInfo;
    }

    /**
     * 填充申请人信息
     */
    private void populateApplicantInfo(Map<String, Object> map, ApprovalInstanceDO instance) {
        UserDO applicant = userMapper.selectById(instance.getApplicantId());
        if (applicant != null) {
            map.put("applicantId", applicant.getId());
            map.put("applicantName", applicant.getRealName());
        }
    }

    /**
     * 填充当前阶段信息
     */
    private void populateCurrentStageInfo(Map<String, Object> map, ApprovalInstanceDO instance) {
        if (instance.getCurrentStageId() == null) {
            return;
        }
        WorkflowStageDO currentStage = workflowStageMapper.selectById(instance.getCurrentStageId());
        if (currentStage != null) {
            map.put("currentStageId", currentStage.getId());
            map.put("currentStageName", currentStage.getName());
            map.put("approveType", currentStage.getApproveType());
        }
    }

    /**
     * 填充待审批审批人信息
     */
    private void populatePendingApprovers(Map<String, Object> map, ApprovalInstanceDO instance) {
        ApprovalTaskQuery pendingTaskQuery = new ApprovalTaskQuery();
        pendingTaskQuery.setInstanceId(instance.getId());
        pendingTaskQuery.setStatus(STATUS_PENDING);
        List<ApprovalTaskDO> pendingTasks = taskMapper.selectList(pendingTaskQuery);

        List<Map<String, Object>> pendingApprovers = new ArrayList<>();
        if (!pendingTasks.isEmpty()) {
            List<Long> approverIds = pendingTasks.stream()
                .map(ApprovalTaskDO::getApproverId)
                .distinct()
                .collect(Collectors.toList());

            UserQuery userQuery = new UserQuery();
            userQuery.setUserIds(approverIds);
            List<UserDO> approverUsers = userMapper.selectList(userQuery);

            Map<Long, UserDO> userMap = approverUsers.stream()
                .collect(Collectors.toMap(UserDO::getId, u -> u));

            for (ApprovalTaskDO task : pendingTasks) {
                UserDO approver = userMap.get(task.getApproverId());
                if (approver != null) {
                    Map<String, Object> approverInfo = new HashMap<>();
                    approverInfo.put("id", approver.getId());
                    approverInfo.put("name", approver.getRealName() != null ? approver.getRealName() : approver.getUsername());
                    pendingApprovers.add(approverInfo);
                }
            }
        }
        map.put("pendingApprovers", pendingApprovers);
    }

    @Override
    public TaskDetailDTO getTaskDetail(Long taskId) {
        // 1. 加载并验证任务
        ApprovalTaskDO task = loadAndValidateTask(taskId);

        // 2. 创建并设置基本任务信息
        TaskDetailDTO result = createTaskDetailDTO(task);

        // 3. 加载实例相关信息
        loadInstanceRelatedInfo(result, task);

        // 4. 判断是否可以选择下一层审批人
        determineCanSelectNextApprovers(result, task);

        // 5. 解析已选择的审批人
        parseSelectedApprovers(result, task);

        // 6. 加载其他审批人
        loadOtherApprovers(result, task);

        return result;
    }

    /**
     * 加载并验证任务
     */
    private ApprovalTaskDO loadAndValidateTask(Long taskId) {
        ApprovalTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new NotFoundException(MSG_TASK_NOT_FOUND);
        }
        return task;
    }

    /**
     * 创建任务详情 DTO 并设置基本信息
     */
    private TaskDetailDTO createTaskDetailDTO(ApprovalTaskDO task) {
        TaskDetailDTO result = new TaskDetailDTO();
        result.setId(task.getId());
        result.setStatus(task.getStatus());
        result.setTaskType(task.getTaskType());
        result.setIsFirstApprover(task.getIsFirstApprover());
        result.setNextStageApproverIds(task.getNextStageApproverIds());
        result.setSelectedByUserId(task.getSelectedByUserId());
        result.setApproverId(task.getApproverId());
        result.setCreateTime(task.getCreateTime());
        if (task.getSubWorkflowApproverIds() != null) {
            result.setSubWorkflowApproverIds(task.getSubWorkflowApproverIds());
        }
        return result;
    }

    /**
     * 加载实例相关信息（流程、阶段、业务数据等）
     */
    private void loadInstanceRelatedInfo(TaskDetailDTO result, ApprovalTaskDO task) {
        ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());
        if (instance == null) {
            return;
        }

        // 设置实例基本信息
        setInstanceBasicInfo(result, instance);

        // 加载流程信息
        loadWorkflowInfo(result, instance);

        // 加载阶段和下一阶段信息
        loadStageAndNextStageInfo(result, instance, task);

        // 加载业务信息
        loadBusinessInfo(result, instance);

        // 加载申请人信息
        loadApplicantInfo(result, instance);

        // 加载审批进度
        loadApprovalProgress(result, instance);
    }

    /**
     * 设置实例基本信息
     */
    private void setInstanceBasicInfo(TaskDetailDTO result, ApprovalInstanceDO instance) {
        result.setInstanceId(instance.getId());
        result.setBusinessType(instance.getBusinessType());
        result.setBusinessId(instance.getBusinessId());
        result.setWorkflowId(instance.getWorkflowId());
        result.setCurrentStageId(instance.getCurrentStageId());
    }

    /**
     * 加载流程信息
     */
    private void loadWorkflowInfo(TaskDetailDTO result, ApprovalInstanceDO instance) {
        WorkflowDO workflow = workflowMapper.selectById(instance.getWorkflowId());
        if (workflow != null) {
            result.setWorkflowName(workflow.getName());
        }
    }

    /**
     * 加载阶段和下一阶段信息
     */
    private void loadStageAndNextStageInfo(TaskDetailDTO result, ApprovalInstanceDO instance, ApprovalTaskDO task) {
        WorkflowStageDO currentStage = workflowStageMapper.selectById(task.getStageId());
        if (currentStage == null) {
            return;
        }

        result.setStageId(currentStage.getId());
        result.setStageName(currentStage.getName());
        result.setApproveType(currentStage.getApproveType());

        // 查找并加载下一阶段信息
        WorkflowStageDO nextStage = findNextStage(instance, currentStage);
        if (nextStage != null) {
            loadNextStageInfo(result, instance, nextStage);
        } else {
            setEmptyNextStageInfo(result);
        }
    }

    /**
     * 查找下一阶段
     */
    private WorkflowStageDO findNextStage(ApprovalInstanceDO instance, WorkflowStageDO currentStage) {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setWorkflowId(instance.getWorkflowId());
        query.setOrderByField("stage_order");
        query.setOrderByDirection("ASC");
        List<WorkflowStageDO> allStages = workflowStageMapper.selectList(query);

        for (WorkflowStageDO stage : allStages) {
            if (stage.getStageOrder() > currentStage.getStageOrder()) {
                return stage;
            }
        }
        return null;
    }

    /**
     * 加载下一阶段信息
     */
    private void loadNextStageInfo(TaskDetailDTO result, ApprovalInstanceDO instance, WorkflowStageDO nextStage) {
        result.setNextStageId(nextStage.getId());
        result.setNextStageName(nextStage.getName());
        result.setNextStageApproveType(nextStage.getApproveType());

        // 加载下一阶段审批人配置
        loadNextStageApproverConfigs(result, instance, nextStage);

        // 加载子流程信息
        loadSubWorkflows(result, instance, nextStage);
    }

    /**
     * 设置空下一阶段信息
     */
    private void setEmptyNextStageInfo(TaskDetailDTO result) {
        result.setNextStageId(null);
        result.setNextStageApproverConfigs(new ArrayList<>());
        result.setNextStageApproverCount(0);
        result.setSubWorkflows(new ArrayList<>());
        result.setHasSubWorkflows(false);
    }

    /**
     * 加载下一阶段审批人配置
     */
    private void loadNextStageApproverConfigs(TaskDetailDTO result, ApprovalInstanceDO instance, WorkflowStageDO nextStage) {
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(nextStage.getId());
        query.setSubWorkflowIdNull(true);
        query.setOrderByField("id");
        query.setOrderByDirection("ASC");
        List<StageApproverDO> approverConfigs = stageApproverMapper.selectList(query);

        List<StageApproverDTO> configs = new ArrayList<>();
        for (StageApproverDO config : approverConfigs) {
            StageApproverDTO configInfo = buildStageApproverDTO(config, instance.getApplicantId());
            configs.add(configInfo);
        }

        result.setNextStageApproverConfigs(configs);
        result.setNextStageApproverCount(configs.size());
    }

    /**
     * 加载子流程信息
     */
    private void loadSubWorkflows(TaskDetailDTO result, ApprovalInstanceDO instance, WorkflowStageDO nextStage) {
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(nextStage.getId());
        query.setSubWorkflowIdNotNull(true);
        List<StageApproverDO> subWorkflowApprovers = stageApproverMapper.selectList(query);

        List<SubWorkflowDTO> subWorkflows = new ArrayList<>();
        for (StageApproverDO sw : subWorkflowApprovers) {
            WorkflowDO subWorkflow = workflowMapper.selectById(sw.getSubWorkflowId());
            if (subWorkflow != null) {
                SubWorkflowDTO dto = buildSubWorkflowDTO(subWorkflow, instance.getApplicantId());
                subWorkflows.add(dto);
            }
        }

        result.setSubWorkflows(subWorkflows);
        result.setHasSubWorkflows(!subWorkflows.isEmpty());
    }

    /**
     * 加载业务信息
     */
    private void loadBusinessInfo(TaskDetailDTO result, ApprovalInstanceDO instance) {
        String businessType = instance.getBusinessType();

        if (BUSINESS_TYPE_MATERIAL_ENTRY.equals(businessType)) {
            loadMaterialEntryInfo(result, instance.getBusinessId());
        } else if (BUSINESS_TYPE_ASSET_USAGE.equals(businessType)) {
            loadAssetUsageInfo(result, instance.getBusinessId());
        } else if (BUSINESS_TYPE_ASSET_DELETION.equals(businessType)) {
            loadAssetDeletionInfo(result, instance.getBusinessId());
        }
    }

    /**
     * 加载素材录入信息
     */
    private void loadMaterialEntryInfo(TaskDetailDTO result, Long businessId) {
        MaterialApplicationDO application = materialApplicationMapper.selectById(businessId);
        if (application != null) {
            result.setApplicationId(application.getId());
            result.setApplicationTitle(application.getTitle());
            result.setBusinessName(application.getTitle());
        }
    }

    /**
     * 加载素材使用信息
     */
    private void loadAssetUsageInfo(TaskDetailDTO result, Long businessId) {
        List<UsageApplyAssetDO> applyAssets = usageApplyAssetMapper.findByUsageApplyIdWithAsset(businessId);
        if (applyAssets != null && !applyAssets.isEmpty()) {
            String businessName = "使用申请：" + applyAssets.get(0).getAssetName();
            if (applyAssets.size() > 1) {
                businessName += " 等" + applyAssets.size() + "个素材";
            }
            result.setBusinessName(businessName);
            result.setApplicationId(businessId);
            result.setApplicationTitle(businessName);
        }
    }

    /**
     * 加载素材删除信息
     */
    private void loadAssetDeletionInfo(TaskDetailDTO result, Long businessId) {
        AssetDeletionApplicationDO deletionApplication = assetDeletionApplicationMapper.selectById(businessId);
        if (deletionApplication != null) {
            result.setApplicationId(deletionApplication.getId());
            result.setApplicationTitle(deletionApplication.getTitle());
            result.setBusinessName(deletionApplication.getTitle());
            result.setDeleteReason(deletionApplication.getDeleteReason());
        }
    }

    /**
     * 加载申请人信息
     */
    private void loadApplicantInfo(TaskDetailDTO result, ApprovalInstanceDO instance) {
        UserDO applicant = userMapper.selectById(instance.getApplicantId());
        if (applicant != null) {
            result.setApplicantId(applicant.getId());
            result.setApplicantName(applicant.getRealName());
        }
    }

    /**
     * 加载审批进度
     */
    private void loadApprovalProgress(TaskDetailDTO result, ApprovalInstanceDO instance) {
        List<ApprovalProgressDTO> progress = approverSelectionService.getApprovalProgress(instance.getId());
        result.setApprovalProgress(progress);
    }

    /**
     * 判断是否可以选择下一层审批人
     */
    private void determineCanSelectNextApprovers(TaskDetailDTO result, ApprovalTaskDO task) {
        boolean hasNextStage = result.getNextStageId() != null;
        boolean nextStageNotSelected = task.getNextStageApproverIds() == null || task.getNextStageApproverIds().isEmpty();

        result.setHasNextStage(hasNextStage);

        boolean canSelect = false;
        if (hasNextStage && nextStageNotSelected) {
            canSelect = isFirstApproverToComplete(task);
        }

        result.setCanSelectNextApprovers(canSelect);
    }

    /**
     * 判断是否是第一个完成的审批人
     */
    private boolean isFirstApproverToComplete(ApprovalTaskDO task) {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(task.getInstanceId());
        query.setStageId(task.getStageId());
        query.setStatus(STATUS_APPROVED);
        List<ApprovalTaskDO> completedTasks = taskMapper.selectList(query);
        return completedTasks.isEmpty();
    }

    /**
     * 解析已选择的审批人
     */
    private void parseSelectedApprovers(TaskDetailDTO result, ApprovalTaskDO task) {
        Boolean canSelect = result.getCanSelectNextApprovers();
        if (canSelect != null && canSelect) {
            result.setSelectedNextApprovers(new ArrayList<>());
            result.setSelectedSubWorkflowApprovers(new HashMap<>());
            return;
        }

        // 解析已选择的下一层审批人
        if (task.getNextStageApproverIds() != null && !task.getNextStageApproverIds().isEmpty()) {
            parseSelectedNextApprovers(result, task.getNextStageApproverIds());
        }

        // 解析已选择的子流程审批人
        if (task.getSubWorkflowApproverIds() != null && !task.getSubWorkflowApproverIds().isEmpty()) {
            parseSelectedSubWorkflowApprovers(result, task.getSubWorkflowApproverIds());
        }
    }

    /**
     * 解析已选择的下一层审批人
     */
    private void parseSelectedNextApprovers(TaskDetailDTO result, String approverIdsJson) {
        try {
            List<Long> selectedApproverIds = objectMapper.readValue(approverIdsJson, new TypeReference<List<Long>>() {});
            List<UserDTO> selectedApprovers = new ArrayList<>();
            for (Long approverId : selectedApproverIds) {
                UserDO user = userMapper.selectById(approverId);
                if (user != null) {
                    selectedApprovers.add(convertToUserDTO(user));
                }
            }
            result.setSelectedNextApprovers(selectedApprovers);
        } catch (Exception e) {
            // 忽略解析错误
        }
    }

    /**
     * 解析已选择的子流程审批人
     */
    private void parseSelectedSubWorkflowApprovers(TaskDetailDTO result, String subWorkflowApproverIdsJson) {
        try {
            Map<Long, List<Long>> subWorkflowApproverIdsMap = objectMapper.readValue(
                subWorkflowApproverIdsJson, new TypeReference<Map<Long, List<Long>>>() {});

            Map<Long, List<UserDTO>> selectedSubWorkflowApprovers = new HashMap<>();
            for (Map.Entry<Long, List<Long>> entry : subWorkflowApproverIdsMap.entrySet()) {
                List<UserDTO> approvers = new ArrayList<>();
                for (Long approverId : entry.getValue()) {
                    UserDO user = userMapper.selectById(approverId);
                    if (user != null) {
                        approvers.add(convertToUserDTO(user));
                    }
                }
                selectedSubWorkflowApprovers.put(entry.getKey(), approvers);
            }
            result.setSelectedSubWorkflowApprovers(selectedSubWorkflowApprovers);
        } catch (Exception e) {
            // 忽略解析错误
        }
    }

    /**
     * 加载其他审批人
     */
    private void loadOtherApprovers(TaskDetailDTO result, ApprovalTaskDO task) {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(task.getInstanceId());
        query.setStageId(task.getStageId());
        query.setIdNotEqual(task.getId());
        query.setStatus(STATUS_PENDING);
        List<ApprovalTaskDO> otherTasks = taskMapper.selectList(query);

        Set<Long> approverIds = new HashSet<>();
        List<UserDTO> otherApprovers = new ArrayList<>();

        for (ApprovalTaskDO otherTask : otherTasks) {
            if (approverIds.contains(otherTask.getApproverId())) {
                continue;
            }

            UserDO user = userMapper.selectById(otherTask.getApproverId());
            if (user != null) {
                UserDTO approverInfo = new UserDTO();
                approverInfo.setId(user.getId());
                approverInfo.setRealName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                approverInfo.setUsername(user.getUsername());
                approverInfo.setStatus(user.getStatus());
                otherApprovers.add(approverInfo);
                approverIds.add(otherTask.getApproverId());
            }
        }

        result.setOtherApprovers(otherApprovers);
    }

    /**
     * 转换为 UserDTO
     */
    private UserDTO convertToUserDTO(UserDO user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setRealName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        dto.setUsername(user.getUsername());
        return dto;
    }

    /**
     * 获取指定审批人配置的可选用户列表
     * @param config 审批人配置
     * @param applicantId 申请人ID（用于二级部门校验）
     * @return 可选用户列表
     */
    private List<Map<String, Object>> getAvailableUsersForConfig(StageApproverDO config, Long applicantId) {
        String approverType = config.getApproverType();
        Long approverId = config.getApproverId();

        switch (approverType) {
            case "USER":
                return getSingleUser(approverId);
            case "ROLE":
                return getRoleUsers(approverId, config, applicantId);
            case "DEPT":
                return getDeptUsers(approverId);
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 获取单个用户
     */
    private List<Map<String, Object>> getSingleUser(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        UserDO user = userMapper.selectById(userId);
        if (user != null && user.getStatus() == 1) {
            result.add(buildUserInfoMap(user));
        }
        return result;
    }

    /**
     * 获取角色用户列表（支持二级部门校验）
     */
    private List<Map<String, Object>> getRoleUsers(Long roleId, StageApproverDO config, Long applicantId) {
        UserQuery userQuery = new UserQuery();
        userQuery.setRoleId(roleId);
        userQuery.setStatus(1);

        boolean checkSecondary = config.getCheckSecondaryDept() != null && config.getCheckSecondaryDept() == 1;
        if (checkSecondary && applicantId != null) {
            userQuery.setDeptIds(getDeptIdsForSecondaryCheck(applicantId));
        }

        List<UserDO> users = userMapper.selectList(userQuery);
        return buildUserInfoList(users);
    }

    /**
     * 获取部门用户列表
     */
    private List<Map<String, Object>> getDeptUsers(Long deptId) {
        UserQuery userQuery = new UserQuery();
        userQuery.setDeptId(deptId);
        userQuery.setStatus(1);
        List<UserDO> users = userMapper.selectList(userQuery);
        return buildUserInfoList(users);
    }

    /**
     * 获取用于二级部门校验的部门 ID 列表
     */
    private List<Long> getDeptIdsForSecondaryCheck(Long applicantId) {
        List<Long> deptIds = new ArrayList<>();
        Long applicantSecondaryDeptId = getSecondaryDeptId(applicantId);
        if (applicantSecondaryDeptId != null) {
            deptIds.add(applicantSecondaryDeptId);
            deptIds.addAll(getAllSubDeptIds(applicantSecondaryDeptId));
        }
        return deptIds;
    }

    /**
     * 从用户列表构建用户信息 Map 列表
     */
    private List<Map<String, Object>> buildUserInfoList(List<UserDO> users) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserDO user : users) {
            result.add(buildUserInfoMap(user));
        }
        return result;
    }

    /**
     * 从单个用户构建用户信息 Map
     */
    private Map<String, Object> buildUserInfoMap(UserDO user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        addDeptInfo(userInfo, user);
        addRoleInfo(userInfo, user);
        return userInfo;
    }

    /**
     * 添加部门信息到用户 Map
     */
    private void addDeptInfo(Map<String, Object> userInfo, UserDO user) {
        if (user.getDeptId() != null) {
            DeptDO dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                userInfo.put("deptName", dept.getName());
            }
        }
    }

    /**
     * 添加角色信息到用户 Map
     */
    private void addRoleInfo(Map<String, Object> userInfo, UserDO user) {
        if (user.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                userInfo.put("roleName", role.getName());
            }
        }
    }

    /**
     * 将 Map 列表转换为 UserDTO 列表
     */
    private List<UserDTO> convertToUserDTOList(List<Map<String, Object>> mapList) {
        if (mapList == null) {
            return new ArrayList<>();
        }
        List<UserDTO> result = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            if (map == null) {
                continue;
            }
            UserDTO dto = new UserDTO();
            dto.setId(getLongValue(map, "id"));
            dto.setUsername((String) map.get("username"));
            dto.setRealName((String) map.get("realName"));
            dto.setDeptName((String) map.get("deptName"));
            dto.setRoleName((String) map.get("roleName"));
            result.add(dto);
        }
        return result;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * 获取用户的二级部门ID
     */
    private Long getSecondaryDeptId(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null || user.getDeptId() == null) {
            return null;
        }

        DeptDO dept = deptMapper.selectById(user.getDeptId());
        if (dept == null) {
            return null;
        }

        if (dept.getLevel() == 2) {
            return dept.getId();
        }

        DeptDO currentDept = dept;
        while (currentDept != null && currentDept.getLevel() > 2) {
            currentDept = deptMapper.selectById(currentDept.getParentId());
            if (currentDept != null && currentDept.getLevel() == 2) {
                return currentDept.getId();
            }
        }

        return null;
    }

    /**
     * 获取指定部门的所有子部门ID
     */
    private List<Long> getAllSubDeptIds(Long deptId) {
        List<Long> result = new ArrayList<>();
        List<DeptDO> children = deptMapper.selectByParentId(deptId);
        for (DeptDO child : children) {
            result.add(child.getId());
            result.addAll(getAllSubDeptIds(child.getId()));
        }
        return result;
    }

    /**
     * 获取工作流的第一阶段
     * @param workflowId 工作流ID
     * @return 第一阶段，如果没有则返回null
     */
    private WorkflowStageDO getFirstStageOfWorkflow(Long workflowId) {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setWorkflowId(workflowId);
        query.setOrderByField("stage_order");
        query.setOrderByDirection("ASC");
        List<WorkflowStageDO> stages = workflowStageMapper.selectList(query);
        return stages.isEmpty() ? null : stages.get(0);
    }

    /**
     * 构建审批人配置 DTO
     */
    private StageApproverDTO buildStageApproverDTO(StageApproverDO config, Long applicantId) {
        StageApproverDTO dto = new StageApproverDTO();
        dto.setId(config.getId());
        dto.setApproverType(config.getApproverType());
        dto.setApproverId(config.getApproverId());
        dto.setCheckSecondaryDept(config.getCheckSecondaryDept());

        // 设置审批人类型名称和审批人名称
        ApproverInfo info = getApproverInfo(config.getApproverType(), config.getApproverId());
        dto.setApproverTypeName(info.typeName);
        dto.setApproverName(info.name);

        // 获取可选用户列表
        List<UserDTO> availableUsers = convertToUserDTOList(getAvailableUsersForConfig(config, applicantId));
        dto.setAvailableUsers(availableUsers);

        return dto;
    }

    /**
     * 构建子流程 DTO
     */
    private SubWorkflowDTO buildSubWorkflowDTO(WorkflowDO subWorkflow, Long applicantId) {
        SubWorkflowDTO dto = new SubWorkflowDTO();
        dto.setId(subWorkflow.getId());
        dto.setName(subWorkflow.getName());
        dto.setWorkflowType(subWorkflow.getWorkflowType());

        // 获取第一层阶段
        WorkflowStageDO firstStage = getFirstStageOfWorkflow(subWorkflow.getId());
        if (firstStage != null) {
            dto.setApproveType(firstStage.getApproveType());
            dto.setApproverConfigs(buildSubWorkflowApproverConfigs(firstStage, applicantId));
            dto.setApproverCount(dto.getApproverConfigs() != null ? dto.getApproverConfigs().size() : 0);
        } else {
            dto.setApproverConfigs(new ArrayList<>());
            dto.setApproverCount(0);
            logger.warn("子流程没有找到第一阶段: subWorkflowId={}, subWorkflowName={}",
                subWorkflow.getId(), subWorkflow.getName());
        }

        return dto;
    }

    /**
     * 构建子流程审批人配置列表
     */
    private List<StageApproverDTO> buildSubWorkflowApproverConfigs(WorkflowStageDO firstStage, Long applicantId) {
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(firstStage.getId());
        query.setSubWorkflowIdNull(true);
        query.setOrderByField("id");
        query.setOrderByDirection("ASC");
        List<StageApproverDO> configs = stageApproverMapper.selectList(query);

        List<StageApproverDTO> dtoList = new ArrayList<>();
        for (StageApproverDO config : configs) {
            dtoList.add(buildStageApproverDTO(config, applicantId));
        }
        return dtoList;
    }

    /**
     * 获取审批人信息
     */
    private ApproverInfo getApproverInfo(String approverType, Long approverId) {
        ApproverInfo info = new ApproverInfo();

        if ("USER".equals(approverType)) {
            UserDO user = userMapper.selectById(approverId);
            if (user != null) {
                info.typeName = "指定用户";
                info.name = user.getRealName() != null ? user.getRealName() : user.getUsername();
            }
        } else if ("ROLE".equals(approverType)) {
            RoleDO role = roleMapper.selectById(approverId);
            if (role != null) {
                info.typeName = "指定角色";
                info.name = role.getName();
            }
        } else if ("DEPT".equals(approverType)) {
            DeptDO dept = deptMapper.selectById(approverId);
            if (dept != null) {
                info.typeName = "指定部门";
                info.name = dept.getName();
            }
        } else {
            info.typeName = "";
            info.name = "";
        }

        return info;
    }

    /**
     * 审批人信息内部类
     */
    private static class ApproverInfo {
        String typeName;
        String name;
    }

    @Override
    public PageResult<FlowItemDTO> getMyFlowItems(Long userId, int pageNum, int pageSize,
                                                   String businessType, String status) {
        // 使用优化的JOIN查询（避免N+1问题，从数百次查询减少到1次）
        List<FlowItemDO> flowItems = taskMapper.selectFlowItemsByUser(userId, businessType, status);

        // 去重（同一个实例可能既是发起人又是审批人）
        Map<Long, FlowItemDO> uniqueFlows = new LinkedHashMap<>();
        for (FlowItemDO flow : flowItems) {
            if (!uniqueFlows.containsKey(flow.getId())) {
                uniqueFlows.put(flow.getId(), flow);
            }
        }

        // 排序（按创建时间倒序）
        List<FlowItemDO> sortedFlows = new ArrayList<>(uniqueFlows.values());
        sortedFlows.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        // 分页（在内存中进行，因为需要去重）
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, sortedFlows.size());
        List<FlowItemDO> pagedFlows = start < sortedFlows.size() ?
                sortedFlows.subList(start, end) : new ArrayList<>();

        // 转换为 FlowItemDTO
        List<FlowItemDTO> result = new ArrayList<>();
        for (FlowItemDO item : pagedFlows) {
            FlowItemDTO dto = new FlowItemDTO();
            dto.setId(item.getId());
            dto.setStatus(item.getStatus());
            dto.setBusinessType(item.getBusinessType());
            dto.setBusinessId(item.getBusinessId());
            dto.setCreateTime(item.getCreateTime());
            dto.setApplicantId(item.getApplicantId());
            dto.setWorkflowId(item.getWorkflowId());
            dto.setWorkflowName(item.getWorkflowName());
            dto.setApplicantName(item.getApplicantName());
            dto.setMyRole(item.getMyRole());
            dto.setApplicationId(item.getApplicationId());
            dto.setApplicationTitle(item.getApplicationTitle());
            dto.setDeletionApplicationId(item.getDeletionApplicationId());
            dto.setDeletionTitle(item.getDeletionTitle());
            dto.setUsageApplicationId(item.getUsageApplicationId());
            dto.setUsageTitle(item.getUsageTitle());
            result.add(dto);
        }

        return PageResult.of(result, (long) sortedFlows.size(), pageNum, pageSize);
    }
}
