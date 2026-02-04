package com.xuanjiao.app.approval.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.app.approval.ApprovalService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.dto.ApprovalProgressDTO;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.infrastructure.dataobject.*;
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
import com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalServiceImpl implements ApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalServiceImpl.class);

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

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public PageResult<Map<String, Object>> getMyTasks(Long userId, int pageNum, int pageSize) {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setApproverId(userId);
        query.setStatus("PENDING");
        IPage<ApprovalTaskDO> page = taskMapper.selectPage(new Page<>(pageNum, pageSize), query);
        List<Map<String, Object>> list = page.getRecords().stream()
            .map(this::buildTaskInfo).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<Map<String, Object>> getMyApplied(Long userId, int pageNum, int pageSize,
                                                         String businessType, boolean forAllUsers,
                                                         Long applicantId, Long deptId, String roleType,
                                                         String status) {
        ApprovalInstanceQuery query = new ApprovalInstanceQuery();

        // 业务类型筛选（如果指定）
        if (businessType != null && !businessType.isEmpty()) {
            query.setBusinessType(businessType);
        }

        // 审批状态筛选（如果指定）
        if (status != null && !status.isEmpty()) {
            query.setStatus(status);
        }

        // 只查询主流程，排除子流程
        query.setParentInstanceIdIsNull(true);

        if (forAllUsers) {
            // 查询所有用户的工单，支持筛选

            // 筛选条件：发起人
            if (applicantId != null) {
                query.setApplicantId(applicantId);
            }

            // 筛选条件：发起人所属部门或角色类型
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

            // 筛选条件：发起人角色类型
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

            // 应用发起人筛选（如果有部门或角色筛选，且没有指定具体的发起人）
            if (!applicantIds.isEmpty() && applicantId == null) {
                query.setApplicantIds(new ArrayList<>(applicantIds));
            }
        } else {
            // 仅查询当前用户的工单
            query.setApplicantId(userId);
        }

        Page<ApprovalInstanceDO> page = instanceMapper.selectPage(new Page<>(pageNum, pageSize), query);
        List<Map<String, Object>> list = page.getRecords().stream()
            .map(this::buildInstanceInfo).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getInstanceDetail(Long instanceId) {
        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("审批实例不存在");
        }
        Map<String, Object> result = buildInstanceInfo(instance);
        // 添加日志检查返回数据
        if (result.containsKey("approvalProgress")) {
            Object progress = result.get("approvalProgress");
            logger.info("getInstanceDetail返回: instanceId={}, approvalProgress={}", instanceId, progress);
        }
        return result;
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
                throw new RuntimeException("任务不存在: " + taskId);
            }

            ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());
            if (instance == null) {
                logger.error("审批实例不存在: instanceId={}", task.getInstanceId());
                throw new RuntimeException("审批实例不存在: " + task.getInstanceId());
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
            throw new RuntimeException("审批处理失败: " + e.getMessage(), e);
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
            throw new RuntimeException("退回处理失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildTaskInfo(ApprovalTaskDO task) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("status", task.getStatus());
        map.put("createTime", task.getCreateTime());
        // 添加任务类型和子流程信息，用于前端区分不同类型的任务
        map.put("taskType", task.getTaskType());
        map.put("approverId", task.getApproverId());
        map.put("stageId", task.getStageId());
        if (task.getSubWorkflowApproverIds() != null) {
            map.put("subWorkflowApproverIds", task.getSubWorkflowApproverIds());
        }

        // 获取实例信息
        ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());
        if (instance != null) {
            map.put("instanceId", instance.getId());
            map.put("businessType", instance.getBusinessType());
            map.put("businessId", instance.getBusinessId());

            // 获取流程名称
            WorkflowDO workflow = workflowMapper.selectById(instance.getWorkflowId());
            if (workflow != null) {
                map.put("workflowName", workflow.getName());
            }

            // 获取业务名称（素材名称或使用申请）
            if ("MATERIAL_ENTRY".equals(instance.getBusinessType())) {
                // 素材录入申请：获取申请单信息
                MaterialApplicationDO application = materialApplicationMapper.selectById(instance.getBusinessId());
                if (application != null) {
                    map.put("applicationId", application.getId());
                    map.put("applicationTitle", application.getTitle());
                    map.put("businessName", application.getTitle());

                    // 获取关联的素材数量和类型
                    AssetQuery assetQuery = new AssetQuery();
                    assetQuery.setApplicationId(application.getId());
                    List<AssetDO> assets = assetMapper.selectList(assetQuery);
                    if (assets != null && !assets.isEmpty()) {
                        map.put("assetType", assets.get(0).getType());
                        map.put("assetCount", assets.size());
                    }
                }
            } else if ("ASSET".equals(instance.getBusinessType())) {
                AssetDO asset = assetMapper.selectById(instance.getBusinessId());
                if (asset != null) {
                    map.put("businessName", asset.getName());
                }
            } else if ("ASSET_USAGE".equals(instance.getBusinessType())) {
                // 通过中间表查询关联的素材
                List<UsageApplyAssetDO> applyAssets = usageApplyAssetMapper.findByUsageApplyIdWithAsset(instance.getBusinessId());
                if (applyAssets != null && !applyAssets.isEmpty()) {
                    // 取第一个素材名称作为业务名称
                    String businessName = "使用申请：" + applyAssets.get(0).getAssetName();
                    if (applyAssets.size() > 1) {
                        businessName += " 等" + applyAssets.size() + "个素材";
                    }
                    map.put("businessName", businessName);
                }
            } else if ("ASSET_DELETION".equals(instance.getBusinessType())) {
                // 素材删除申请：获取申请单信息
                com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO deletionApplication =
                    assetDeletionApplicationMapper.selectById(instance.getBusinessId());
                if (deletionApplication != null) {
                    map.put("applicationId", deletionApplication.getId());
                    map.put("applicationTitle", deletionApplication.getTitle());
                    map.put("businessName", deletionApplication.getTitle());
                }
            }

            // 获取申请人信息
            UserDO applicant = userMapper.selectById(instance.getApplicantId());
            if (applicant != null) {
                map.put("applicantName", applicant.getRealName());
            }
        }
        return map;
    }

    private Map<String, Object> buildInstanceInfo(ApprovalInstanceDO instance) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", instance.getId());
        map.put("status", instance.getStatus());
        map.put("businessType", instance.getBusinessType());
        map.put("businessId", instance.getBusinessId());
        map.put("createTime", instance.getCreateTime());

        // 获取流程信息
        WorkflowDO workflow = workflowMapper.selectById(instance.getWorkflowId());
        if (workflow != null) {
            map.put("workflowName", workflow.getName());
            map.put("workflowId", workflow.getId());
        }

        // 获取业务名称和详情
        if ("MATERIAL_ENTRY".equals(instance.getBusinessType())) {
            // 获取申请单信息
            MaterialApplicationDO application = materialApplicationMapper.selectById(instance.getBusinessId());
            if (application != null) {
                // 申请单ID和标题
                map.put("applicationId", application.getId());
                map.put("applicationTitle", application.getTitle());
                map.put("businessName", application.getTitle()); // 兼容旧字段

                // 获取关联的素材文件列表（一个申请单可能有多个素材）
                AssetQuery assetQuery = new AssetQuery();
                assetQuery.setApplicationId(application.getId());
                List<AssetDO> assets = assetMapper.selectList(assetQuery);
                if (assets != null && !assets.isEmpty()) {
                    // 取第一个素材作为主要信息
                    AssetDO firstAsset = assets.get(0);
                    map.put("assetType", firstAsset.getType());
                    map.put("assetStatus", firstAsset.getStatus());
                    map.put("assetCount", assets.size()); // 素材数量

                    // 构建素材列表（包含完整信息）
                    List<Map<String, Object>> assetList = new ArrayList<>();
                    for (AssetDO asset : assets) {
                        Map<String, Object> assetInfo = new HashMap<>();
                        assetInfo.put("id", asset.getId());
                        assetInfo.put("name", asset.getName());
                        assetInfo.put("type", asset.getType());
                        assetInfo.put("status", asset.getStatus());
                        // 文件路径（用于预览和下载）
                        assetInfo.put("filePath", asset.getFilePath());
                        assetInfo.put("thumbnailPath", asset.getThumbnailPath());
                        assetInfo.put("fileSize", asset.getFileSize());
                        // 申请单填写信息
                        assetInfo.put("description", asset.getDescription());
                        assetInfo.put("publishChannel", asset.getPublishChannel());
                        // 附件文件路径
                        assetInfo.put("copyrightFilePath", asset.getCopyrightFilePath());
                        assetList.add(assetInfo);
                    }
                    map.put("assetList", assetList);
                }
            }
        } else if ("ASSET".equals(instance.getBusinessType())) {
            AssetDO asset = assetMapper.selectById(instance.getBusinessId());
            if (asset != null) {
                map.put("businessName", asset.getName());
                // 业务详情
                map.put("assetType", asset.getType());
                map.put("assetStatus", asset.getStatus());
                map.put("filePath", asset.getFilePath());
                map.put("thumbnailPath", asset.getThumbnailPath());
                map.put("fileSize", asset.getFileSize());
            }
        } else if ("ASSET_USAGE".equals(instance.getBusinessType())) {
            // 通过中间表查询关联的素材
            List<UsageApplyAssetDO> applyAssets = usageApplyAssetMapper.findByUsageApplyIdWithAsset(instance.getBusinessId());
            if (applyAssets != null && !applyAssets.isEmpty()) {
                UsageApplyAssetDO firstAsset = applyAssets.get(0);
                map.put("businessName", "使用申请：" + firstAsset.getAssetName());
                // 业务详情
                map.put("assetType", firstAsset.getAssetType());
                map.put("assetId", firstAsset.getAssetId());
                map.put("assetCount", applyAssets.size());
            }
        } else if ("ASSET_DELETION".equals(instance.getBusinessType())) {
            // 素材删除申请：获取申请单信息
            com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO deletionApplication =
                assetDeletionApplicationMapper.selectById(instance.getBusinessId());
            if (deletionApplication != null) {
                map.put("applicationId", deletionApplication.getId());
                map.put("applicationTitle", deletionApplication.getTitle());
                map.put("businessName", deletionApplication.getTitle());
                map.put("deleteReason", deletionApplication.getDeleteReason());

                // 获取关联的素材ID列表
                AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
                query.setDeletionApplicationId(instance.getBusinessId());
                List<com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO> deletionAssets =
                    assetDeletionAssetMapper.selectList(query);

                if (deletionAssets != null && !deletionAssets.isEmpty()) {
                    map.put("assetCount", deletionAssets.size());

                    // 查询每个素材的详细信息
                    List<Map<String, Object>> assetList = new ArrayList<>();
                    for (com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO deletionAsset : deletionAssets) {
                        AssetDO asset = assetMapper.selectById(deletionAsset.getAssetId());
                        if (asset != null) {
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
                            assetList.add(assetInfo);
                        }
                    }
                    map.put("assetList", assetList);
                }
            }
        }

        // 获取申请人信息
        UserDO applicant = userMapper.selectById(instance.getApplicantId());
        if (applicant != null) {
            map.put("applicantId", applicant.getId());
            map.put("applicantName", applicant.getRealName());
        }

        // 获取当前阶段信息
        if (instance.getCurrentStageId() != null) {
            WorkflowStageDO currentStage = workflowStageMapper.selectById(instance.getCurrentStageId());
            if (currentStage != null) {
                map.put("currentStageId", currentStage.getId());
                map.put("currentStageName", currentStage.getName());
                map.put("approveType", currentStage.getApproveType());
            }
        }

        // 获取当前阶段的待审批任务
        ApprovalTaskQuery pendingTaskQuery = new ApprovalTaskQuery();
        pendingTaskQuery.setInstanceId(instance.getId());
        pendingTaskQuery.setStatus("PENDING");
        List<ApprovalTaskDO> pendingTasks = taskMapper.selectList(pendingTaskQuery);
        List<Map<String, Object>> pendingApprovers = new ArrayList<>();
        for (ApprovalTaskDO task : pendingTasks) {
            UserDO approver = userMapper.selectById(task.getApproverId());
            if (approver != null) {
                Map<String, Object> approverInfo = new HashMap<>();
                approverInfo.put("id", approver.getId());
                approverInfo.put("name", approver.getRealName() != null ? approver.getRealName() : approver.getUsername());
                pendingApprovers.add(approverInfo);
            }
        }
        map.put("pendingApprovers", pendingApprovers);

        // 获取审批进度
        List<ApprovalProgressDTO> progress = approverSelectionService.getApprovalProgress(instance.getId());
        map.put("approvalProgress", progress);

        return map;
    }

    @Override
    public Map<String, Object> getTaskDetail(Long taskId) {
        ApprovalTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("status", task.getStatus());
        result.put("taskType", task.getTaskType());
        result.put("isFirstApprover", task.getIsFirstApprover());
        result.put("nextStageApproverIds", task.getNextStageApproverIds());
        result.put("selectedByUserId", task.getSelectedByUserId());
        result.put("approverId", task.getApproverId());
        if (task.getSubWorkflowApproverIds() != null) {
            result.put("subWorkflowApproverIds", task.getSubWorkflowApproverIds());
        }

        // 获取实例信息
        ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());
        if (instance != null) {
            result.put("instanceId", instance.getId());
            result.put("businessType", instance.getBusinessType());
            result.put("businessId", instance.getBusinessId());
            result.put("workflowId", instance.getWorkflowId());
            result.put("currentStageId", instance.getCurrentStageId());

            // 获取流程名称
            WorkflowDO workflow = workflowMapper.selectById(instance.getWorkflowId());
            if (workflow != null) {
                result.put("workflowName", workflow.getName());
            }

            // 获取当前阶段信息
            WorkflowStageDO currentStage = workflowStageMapper.selectById(task.getStageId());
            String approveType = null;
            if (currentStage != null) {
                approveType = currentStage.getApproveType();
                result.put("stageId", currentStage.getId());
                result.put("stageName", currentStage.getName());
                result.put("approveType", approveType);

                // 查找下一阶段
                WorkflowStageQuery nextStageQuery = new WorkflowStageQuery();
                nextStageQuery.setWorkflowId(instance.getWorkflowId());
                nextStageQuery.setOrderByField("stage_order");
                nextStageQuery.setOrderByDirection("ASC");
                List<WorkflowStageDO> allNextStages = workflowStageMapper.selectList(nextStageQuery);
                WorkflowStageDO nextStage = null;
                for (WorkflowStageDO stage : allNextStages) {
                    if (stage.getStageOrder() > currentStage.getStageOrder()) {
                        nextStage = stage;
                        break;
                    }
                }
                if (nextStage != null) {
                    result.put("nextStageId", nextStage.getId());
                    result.put("nextStageName", nextStage.getName());
                    result.put("nextStageApproveType", nextStage.getApproveType());

                    // 获取下一阶段配置的审批人列表（按配置顺序，排除子流程）
                    StageApproverQuery approverConfigQuery = new StageApproverQuery();
                    approverConfigQuery.setStageId(nextStage.getId());
                    approverConfigQuery.setSubWorkflowIdNull(true);
                    approverConfigQuery.setOrderByField("id");
                    approverConfigQuery.setOrderByDirection("ASC");
                    List<StageApproverDO> approverConfigs = stageApproverMapper.selectList(approverConfigQuery);

                    // 构建下一层审批人配置列表（每个配置项包含类型、名称、可选用户）
                    List<Map<String, Object>> nextStageApproverConfigs = new ArrayList<>();
                    for (StageApproverDO config : approverConfigs) {
                        Map<String, Object> configInfo = new HashMap<>();
                        configInfo.put("configId", config.getId());
                        configInfo.put("approverType", config.getApproverType());
                        configInfo.put("approverId", config.getApproverId());
                        configInfo.put("checkSecondaryDept", config.getCheckSecondaryDept());

                        // 设置审批人类型名称
                        String approverTypeName = "";
                        String approverName = "";
                        if ("USER".equals(config.getApproverType())) {
                            UserDO user = userMapper.selectById(config.getApproverId());
                            if (user != null) {
                                approverTypeName = "指定用户";
                                approverName = user.getRealName() != null ? user.getRealName() : user.getUsername();
                            }
                        } else if ("ROLE".equals(config.getApproverType())) {
                            RoleDO role = roleMapper.selectById(config.getApproverId());
                            if (role != null) {
                                approverTypeName = "指定角色";
                                approverName = role.getName();
                            }
                        } else if ("DEPT".equals(config.getApproverType())) {
                            DeptDO dept = deptMapper.selectById(config.getApproverId());
                            if (dept != null) {
                                approverTypeName = "指定部门";
                                approverName = dept.getName();
                            }
                        }
                        configInfo.put("approverTypeName", approverTypeName);
                        configInfo.put("approverName", approverName);

                        // 获取该配置项的可选用户列表
                        List<Map<String, Object>> availableUsers = getAvailableUsersForConfig(config, instance.getApplicantId());
                        configInfo.put("availableUsers", availableUsers);

                        nextStageApproverConfigs.add(configInfo);
                    }
                    result.put("nextStageApproverConfigs", nextStageApproverConfigs);
                    result.put("nextStageApproverCount", nextStageApproverConfigs.size());

                    // 获取下一阶段配置的子流程列表（当前层的审批人选择下一层的子流程审批人）
                    StageApproverQuery subWorkflowQuery = new StageApproverQuery();
                    subWorkflowQuery.setStageId(nextStage.getId());
                    subWorkflowQuery.setSubWorkflowIdNotNull(true);
                    List<StageApproverDO> subWorkflowApprovers = stageApproverMapper.selectList(subWorkflowQuery);

                    // 构建子流程信息列表（包含第一层审批人配置）
                    List<Map<String, Object>> subWorkflows = new ArrayList<>();
                    for (StageApproverDO sw : subWorkflowApprovers) {
                        WorkflowDO subWorkflow = workflowMapper.selectById(sw.getSubWorkflowId());
                        if (subWorkflow != null) {
                            Map<String, Object> subWorkflowInfo = new HashMap<>();
                            subWorkflowInfo.put("id", subWorkflow.getId());
                            subWorkflowInfo.put("name", subWorkflow.getName());
                            subWorkflowInfo.put("workflowType", subWorkflow.getWorkflowType());

                            // 获取子流程的第一层阶段
                            WorkflowStageDO subFirstStage = getFirstStageOfWorkflow(subWorkflow.getId());
                            logger.info("子流程获取第一阶段: subWorkflowId={}, subFirstStage={}", subWorkflow.getId(), subFirstStage != null ? subFirstStage.getId() : null);

                            if (subFirstStage != null) {
                                // 设置子流程第一层的审批类型
                                subWorkflowInfo.put("approveType", subFirstStage.getApproveType());

                                // 获取第一层的审批人配置（排除子流程）
                                StageApproverQuery subApproverConfigQuery = new StageApproverQuery();
                                subApproverConfigQuery.setStageId(subFirstStage.getId());
                                subApproverConfigQuery.setSubWorkflowIdNull(true);
                                subApproverConfigQuery.setOrderByField("id");
                                subApproverConfigQuery.setOrderByDirection("ASC");
                                List<StageApproverDO> subApproverConfigs = stageApproverMapper.selectList(subApproverConfigQuery);

                                logger.info("子流程第一层审批人配置: subWorkflowId={}, stageId={}, approveType={}, configCount={}",
                                    subWorkflow.getId(), subFirstStage.getId(), subFirstStage.getApproveType(), subApproverConfigs.size());

                                // 构建子流程第一层审批人配置列表
                                List<Map<String, Object>> subApproverConfigsList = new ArrayList<>();
                                for (StageApproverDO subConfig : subApproverConfigs) {
                                    Map<String, Object> subConfigInfo = new HashMap<>();
                                    subConfigInfo.put("configId", subConfig.getId());
                                    subConfigInfo.put("approverType", subConfig.getApproverType());
                                    subConfigInfo.put("approverId", subConfig.getApproverId());
                                    subConfigInfo.put("checkSecondaryDept", subConfig.getCheckSecondaryDept());

                                    // 设置审批人类型名称
                                    String subApproverTypeName = "";
                                    String subApproverName = "";
                                    if ("USER".equals(subConfig.getApproverType())) {
                                        UserDO user = userMapper.selectById(subConfig.getApproverId());
                                        if (user != null) {
                                            subApproverTypeName = "指定用户";
                                            subApproverName = user.getRealName() != null ? user.getRealName() : user.getUsername();
                                        }
                                    } else if ("ROLE".equals(subConfig.getApproverType())) {
                                        RoleDO role = roleMapper.selectById(subConfig.getApproverId());
                                        if (role != null) {
                                            subApproverTypeName = "指定角色";
                                            subApproverName = role.getName();
                                        }
                                    } else if ("DEPT".equals(subConfig.getApproverType())) {
                                        DeptDO dept = deptMapper.selectById(subConfig.getApproverId());
                                        if (dept != null) {
                                            subApproverTypeName = "指定部门";
                                            subApproverName = dept.getName();
                                        }
                                    }
                                    subConfigInfo.put("approverTypeName", subApproverTypeName);
                                    subConfigInfo.put("approverName", subApproverName);

                                    // 获取该配置项的可选用户列表
                                    List<Map<String, Object>> subAvailableUsers = getAvailableUsersForConfig(subConfig, instance.getApplicantId());
                                    subConfigInfo.put("availableUsers", subAvailableUsers);

                                    logger.info("子流程配置: configId={}, type={}, name={}, availableUsersCount={}",
                                        subConfig.getId(), subConfig.getApproverType(), subApproverName, subAvailableUsers.size());

                                    subApproverConfigsList.add(subConfigInfo);
                                }
                                subWorkflowInfo.put("approverConfigs", subApproverConfigsList);
                                subWorkflowInfo.put("approverCount", subApproverConfigsList.size());
                            } else {
                                logger.warn("子流程没有找到第一阶段: subWorkflowId={}, subWorkflowName={}",
                                    subWorkflow.getId(), subWorkflow.getName());
                                // 设置空的配置列表
                                subWorkflowInfo.put("approverConfigs", new ArrayList<>());
                                subWorkflowInfo.put("approverCount", 0);
                            }

                            subWorkflows.add(subWorkflowInfo);
                        }
                    }
                    result.put("subWorkflows", subWorkflows);
                    result.put("hasSubWorkflows", !subWorkflows.isEmpty());
                } else {
                    // 没有下一阶段，说明是最后一层
                    result.put("nextStageId", null);
                    result.put("isLastStage", true);
                    result.put("nextStageApproverConfigs", new ArrayList<>());
                    result.put("nextStageApproverCount", 0);
                    result.put("subWorkflows", new ArrayList<>());
                    result.put("hasSubWorkflows", false);
                }
            }

            // 获取业务名称和申请人信息
            if ("ASSET".equals(instance.getBusinessType())) {
                AssetDO asset = assetMapper.selectById(instance.getBusinessId());
                if (asset != null) {
                    result.put("businessName", asset.getName());
                }
            } else if ("ASSET_USAGE".equals(instance.getBusinessType())) {
                // 通过中间表查询关联的素材
                List<UsageApplyAssetDO> applyAssets = usageApplyAssetMapper.findByUsageApplyIdWithAsset(instance.getBusinessId());
                if (applyAssets != null && !applyAssets.isEmpty()) {
                    String businessName = "使用申请：" + applyAssets.get(0).getAssetName();
                    if (applyAssets.size() > 1) {
                        businessName += " 等" + applyAssets.size() + "个素材";
                    }
                    result.put("businessName", businessName);
                }
            } else if ("ASSET_DELETION".equals(instance.getBusinessType())) {
                // 素材删除申请：获取申请单信息
                com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO deletionApplication =
                    assetDeletionApplicationMapper.selectById(instance.getBusinessId());
                if (deletionApplication != null) {
                    result.put("applicationId", deletionApplication.getId());
                    result.put("applicationTitle", deletionApplication.getTitle());
                    result.put("businessName", deletionApplication.getTitle());
                    result.put("deleteReason", deletionApplication.getDeleteReason());
                }
            }

            UserDO applicant = userMapper.selectById(instance.getApplicantId());
            if (applicant != null) {
                result.put("applicantId", applicant.getId());
                result.put("applicantName", applicant.getRealName());
            }

            // 获取审批进度
            List<ApprovalProgressDTO> progress = approverSelectionService.getApprovalProgress(instance.getId());
            result.put("approvalProgress", progress);
        }

        // 判断当前用户是否可以选择下一层审批人
        // 条件：存在下一阶段 且 下一层审批人尚未选择
        boolean hasNextStage = result.containsKey("nextStageId") && result.get("nextStageId") != null;
        boolean nextStageNotSelected = task.getNextStageApproverIds() == null || task.getNextStageApproverIds().isEmpty();

        boolean canSelectNextApprovers = false;

        if (hasNextStage && nextStageNotSelected) {
            // 或签和会签都使用相同的动态判断逻辑
            // 第一个完成的审批人可以选择下一层审批人
            // 检查当前阶段是否已有已完成的任务
            ApprovalTaskQuery completedTaskQuery = new ApprovalTaskQuery();
            completedTaskQuery.setInstanceId(task.getInstanceId());
            completedTaskQuery.setStageId(task.getStageId());
            completedTaskQuery.setStatus("APPROVED");
            List<ApprovalTaskDO> completedTasks = taskMapper.selectList(completedTaskQuery);

            // 如果没有已完成的任务，当前任务作为第一个完成的任务，可以选择下一层审批人
            // 无论或签还是会签，第一个完成的审批人都可以选择
            canSelectNextApprovers = completedTasks.isEmpty();
        }

        result.put("canSelectNextApprovers", canSelectNextApprovers);

        // 解析已选择的下一层审批人
        // 只有当不能选择下一层审批人时，才返回已选择的审批人信息（用于只读显示）
        // 如果可以选择下一层审批人，清空已选择的审批人信息，允许重新选择
        if (!canSelectNextApprovers && task.getNextStageApproverIds() != null && !task.getNextStageApproverIds().isEmpty()) {
            try {
                List<Long> selectedApproverIds = objectMapper.readValue(
                    task.getNextStageApproverIds(),
                    new TypeReference<List<Long>>() {}
                );
                List<Map<String, Object>> selectedApprovers = new ArrayList<>();
                for (Long approverId : selectedApproverIds) {
                    UserDO user = userMapper.selectById(approverId);
                    if (user != null) {
                        Map<String, Object> approverInfo = new HashMap<>();
                        approverInfo.put("id", user.getId());
                        approverInfo.put("name", user.getRealName() != null ? user.getRealName() : user.getUsername());
                        approverInfo.put("username", user.getUsername());
                        selectedApprovers.add(approverInfo);
                    }
                }
                result.put("selectedNextApprovers", selectedApprovers);
            } catch (Exception e) {
                // 忽略解析错误
            }
        } else {
            // 如果可以选择下一层审批人，清空已选择的审批人信息
            result.put("selectedNextApprovers", new ArrayList<>());
        }

        // 解析已选择的子流程审批人
        if (task.getSubWorkflowApproverIds() != null && !task.getSubWorkflowApproverIds().isEmpty()) {
            try {
                Map<Long, List<Long>> subWorkflowApproverIdsMap = objectMapper.readValue(
                    task.getSubWorkflowApproverIds(),
                    new TypeReference<Map<Long, List<Long>>>() {}
                );

                // 构建子流程审批人信息：子流程ID -> 审批人列表
                Map<Long, List<Map<String, Object>>> selectedSubWorkflowApprovers = new HashMap<>();
                for (Map.Entry<Long, List<Long>> entry : subWorkflowApproverIdsMap.entrySet()) {
                    Long subWorkflowId = entry.getKey();
                    List<Long> approverIds = entry.getValue();

                    List<Map<String, Object>> approvers = new ArrayList<>();
                    for (Long approverId : approverIds) {
                        UserDO user = userMapper.selectById(approverId);
                        if (user != null) {
                            Map<String, Object> approverInfo = new HashMap<>();
                            approverInfo.put("id", user.getId());
                            approverInfo.put("name", user.getRealName() != null ? user.getRealName() : user.getUsername());
                            approverInfo.put("username", user.getUsername());
                            approvers.add(approverInfo);
                        }
                    }
                    selectedSubWorkflowApprovers.put(subWorkflowId, approvers);
                }
                result.put("selectedSubWorkflowApprovers", selectedSubWorkflowApprovers);
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        // 获取同阶段的其他审批人（用于判断是或签还是会签）
        // 只查询待审批状态的任务，过滤掉已退回、已取消、已完成等无效任务
        ApprovalTaskQuery taskQuery = new ApprovalTaskQuery();
        taskQuery.setInstanceId(task.getInstanceId());
        taskQuery.setStageId(task.getStageId());
        taskQuery.setIdNotEqual(task.getId());
        taskQuery.setStatus("PENDING");  // 只查询待审批状态的任务
        List<ApprovalTaskDO> otherTasks = taskMapper.selectList(taskQuery);

        // 使用Set对审批人去重（同一审批人可能有多条任务记录，但只显示一次）
        Set<Long> approverIds = new HashSet<>();
        List<Map<String, Object>> otherApprovers = new ArrayList<>();
        for (ApprovalTaskDO otherTask : otherTasks) {
            // 去重：如果该审批人已经添加过，跳过
            if (approverIds.contains(otherTask.getApproverId())) {
                continue;
            }

            UserDO user = userMapper.selectById(otherTask.getApproverId());
            if (user != null) {
                Map<String, Object> approverInfo = new HashMap<>();
                approverInfo.put("id", user.getId());
                approverInfo.put("name", user.getRealName() != null ? user.getRealName() : user.getUsername());
                approverInfo.put("status", otherTask.getStatus());
                otherApprovers.add(approverInfo);
                approverIds.add(otherTask.getApproverId());
            }
        }
        result.put("otherApprovers", otherApprovers);

        return result;
    }

    /**
     * 获取指定审批人配置的可选用户列表
     * @param config 审批人配置
     * @param applicantId 申请人ID（用于二级部门校验）
     * @return 可选用户列表
     */
    private List<Map<String, Object>> getAvailableUsersForConfig(StageApproverDO config, Long applicantId) {
        List<Map<String, Object>> result = new ArrayList<>();
        String approverType = config.getApproverType();
        Long approverId = config.getApproverId();

        if ("USER".equals(approverType)) {
            // 指定用户：返回该用户
            UserDO user = userMapper.selectById(approverId);
            if (user != null && user.getStatus() == 1) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("realName", user.getRealName());
                // 获取部门和角色信息
                if (user.getDeptId() != null) {
                    DeptDO dept = deptMapper.selectById(user.getDeptId());
                    if (dept != null) {
                        userInfo.put("deptName", dept.getName());
                    }
                }
                if (user.getRoleId() != null) {
                    RoleDO role = roleMapper.selectById(user.getRoleId());
                    if (role != null) {
                        userInfo.put("roleName", role.getName());
                    }
                }
                result.add(userInfo);
            }
        } else if ("ROLE".equals(approverType)) {
            // 指定角色：根据是否校验二级部门返回用户列表
            boolean checkSecondary = config.getCheckSecondaryDept() != null && config.getCheckSecondaryDept() == 1;

            UserQuery userQuery = new UserQuery();
            userQuery.setRoleId(approverId);
            userQuery.setStatus(1);

            if (checkSecondary && applicantId != null) {
                // 获取申请人的二级部门
                Long applicantSecondaryDeptId = getSecondaryDeptId(applicantId);
                if (applicantSecondaryDeptId != null) {
                    List<Long> deptIds = new ArrayList<>();
                    deptIds.add(applicantSecondaryDeptId);
                    deptIds.addAll(getAllSubDeptIds(applicantSecondaryDeptId));
                    userQuery.setDeptIds(deptIds);
                }
            }

            List<UserDO> users = userMapper.selectList(userQuery);
            for (UserDO user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("realName", user.getRealName());
                // 获取部门和角色信息
                if (user.getDeptId() != null) {
                    DeptDO dept = deptMapper.selectById(user.getDeptId());
                    if (dept != null) {
                        userInfo.put("deptName", dept.getName());
                    }
                }
                if (user.getRoleId() != null) {
                    RoleDO role = roleMapper.selectById(user.getRoleId());
                    if (role != null) {
                        userInfo.put("roleName", role.getName());
                    }
                }
                result.add(userInfo);
            }
        } else if ("DEPT".equals(approverType)) {
            // 指定部门：返回该部门的所有用户
            UserQuery userQuery = new UserQuery();
            userQuery.setDeptId(approverId);
            userQuery.setStatus(1);
            List<UserDO> users = userMapper.selectList(userQuery);
            for (UserDO user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("realName", user.getRealName());
                // 获取部门和角色信息
                if (user.getDeptId() != null) {
                    DeptDO dept = deptMapper.selectById(user.getDeptId());
                    if (dept != null) {
                        userInfo.put("deptName", dept.getName());
                    }
                }
                if (user.getRoleId() != null) {
                    RoleDO role = roleMapper.selectById(user.getRoleId());
                    if (role != null) {
                        userInfo.put("roleName", role.getName());
                    }
                }
                result.add(userInfo);
            }
        }

        return result;
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
}
