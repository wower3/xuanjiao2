package com.xuanjiao.app.approval.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.material.MaterialApplicationMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PageResult<Map<String, Object>> getMyTasks(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<ApprovalTaskDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTaskDO::getApproverId, userId)
               .eq(ApprovalTaskDO::getStatus, "PENDING")
               .orderByDesc(ApprovalTaskDO::getCreateTime);
        Page<ApprovalTaskDO> page = taskMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Map<String, Object>> list = page.getRecords().stream()
            .map(this::buildTaskInfo).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public PageResult<Map<String, Object>> getMyApplied(Long userId, int pageNum, int pageSize,
                                                         String businessType, boolean forAllUsers,
                                                         Long applicantId, Long deptId, String roleType,
                                                         String status) {
        LambdaQueryWrapper<ApprovalInstanceDO> wrapper = new LambdaQueryWrapper<>();

        // 业务类型筛选（如果指定）
        if (businessType != null && !businessType.isEmpty()) {
            wrapper.eq(ApprovalInstanceDO::getBusinessType, businessType);
        }

        // 审批状态筛选（如果指定）
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ApprovalInstanceDO::getStatus, status);
        }

        // 只查询主流程，排除子流程
        wrapper.isNull(ApprovalInstanceDO::getParentInstanceId);

        if (forAllUsers) {
            // 查询所有用户的工单，支持筛选

            // 筛选条件：发起人
            if (applicantId != null) {
                wrapper.eq(ApprovalInstanceDO::getApplicantId, applicantId);
            }

            // 筛选条件：发起人所属部门或角色类型
            Set<Long> applicantIds = new HashSet<>();
            if (deptId != null) {
                // 查询该部门下的所有用户
                LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(UserDO::getDeptId, deptId).eq(UserDO::getStatus, 1);
                List<UserDO> usersInDept = userMapper.selectList(userWrapper);
                for (UserDO user : usersInDept) {
                    applicantIds.add(user.getId());
                }
            }

            // 筛选条件：发起人角色类型
            if (roleType != null && !roleType.isEmpty()) {
                // 根据角色类型查询角色
                LambdaQueryWrapper<RoleDO> roleWrapper = new LambdaQueryWrapper<>();
                roleWrapper.eq(RoleDO::getRoleType, roleType).eq(RoleDO::getStatus, 1);
                List<RoleDO> roles = roleMapper.selectList(roleWrapper);

                if (!roles.isEmpty()) {
                    List<Long> roleIds = roles.stream().map(RoleDO::getId).collect(Collectors.toList());
                    // 查询拥有这些角色的用户
                    LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
                    userWrapper.in(UserDO::getRoleId, roleIds).eq(UserDO::getStatus, 1);
                    List<UserDO> usersWithRole = userMapper.selectList(userWrapper);
                    for (UserDO user : usersWithRole) {
                        applicantIds.add(user.getId());
                    }
                }
            }

            // 应用发起人筛选（如果有部门或角色筛选，且没有指定具体的发起人）
            if (!applicantIds.isEmpty() && applicantId == null) {
                wrapper.in(ApprovalInstanceDO::getApplicantId, applicantIds);
            }
        } else {
            // 仅查询当前用户的工单
            wrapper.eq(ApprovalInstanceDO::getApplicantId, userId);
        }

        wrapper.orderByDesc(ApprovalInstanceDO::getCreateTime);
        Page<ApprovalInstanceDO> page = instanceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
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
            // 调用工作流引擎完成任务
            workflowEngineService.completeTask(taskId, userId, passed, comment);

            // 获取任务和实例信息
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

            logger.info("审批完成，更新业务状态: instanceId={}, businessType={}, businessId={}, instanceStatus={}",
                instance.getId(), instance.getBusinessType(), instance.getBusinessId(), instance.getStatus());

            // 根据业务类型更新对应业务状态
            if ("MATERIAL_ENTRY".equals(instance.getBusinessType())) {
                // 素材录入审批：由工作流引擎的 handleWorkflowCompletion 处理
                logger.info("素材录入审批，业务状态由工作流引擎处理: applicationId={}", instance.getBusinessId());
            } else if ("ASSET".equals(instance.getBusinessType())) {
                // 素材录入审批：更新素材状态
                AssetDO asset = assetMapper.selectById(instance.getBusinessId());
                if (asset != null) {
                    if ("APPROVED".equals(instance.getStatus())) {
                        asset.setStatus("APPROVED");
                        assetMapper.updateById(asset);
                        logger.info("素材状态已更新为APPROVED: assetId={}", asset.getId());
                    } else if ("REJECTED".equals(instance.getStatus())) {
                        asset.setStatus("REJECTED");
                        assetMapper.updateById(asset);
                        logger.info("素材状态已更新为REJECTED: assetId={}", asset.getId());
                    }
                } else {
                    logger.warn("素材不存在: assetId={}", instance.getBusinessId());
                }
            } else if ("ASSET_USAGE".equals(instance.getBusinessType())) {
                // 素材使用申请审批：更新申请状态
                UsageApplyDO usageApply = usageApplyMapper.selectById(instance.getBusinessId());
                if (usageApply != null) {
                    if ("APPROVED".equals(instance.getStatus())) {
                        usageApply.setStatus("APPROVED");
                        usageApplyMapper.updateById(usageApply);
                        logger.info("使用申请状态已更新为APPROVED: usageApplyId={}", usageApply.getId());
                    } else if ("REJECTED".equals(instance.getStatus())) {
                        usageApply.setStatus("REJECTED");
                        usageApplyMapper.updateById(usageApply);
                        logger.info("使用申请状态已更新为REJECTED: usageApplyId={}", usageApply.getId());
                    }
                } else {
                    logger.warn("使用申请不存在: usageApplyId={}", instance.getBusinessId());
                }
            } else {
                logger.warn("未知的业务类型: businessType={}, instanceId={}", instance.getBusinessType(), instance.getId());
            }

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
                    LambdaQueryWrapper<AssetDO> assetWrapper = new LambdaQueryWrapper<>();
                    assetWrapper.eq(AssetDO::getApplicationId, application.getId());
                    List<AssetDO> assets = assetMapper.selectList(assetWrapper);
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
                LambdaQueryWrapper<AssetDO> assetWrapper = new LambdaQueryWrapper<>();
                assetWrapper.eq(AssetDO::getApplicationId, application.getId());
                List<AssetDO> assets = assetMapper.selectList(assetWrapper);
                if (assets != null && !assets.isEmpty()) {
                    // 取第一个素材作为主要信息
                    AssetDO firstAsset = assets.get(0);
                    map.put("assetType", firstAsset.getType());
                    map.put("assetStatus", firstAsset.getStatus());
                    map.put("assetCount", assets.size()); // 素材数量

                    // 构建素材列表（包含ID和名称）
                    List<Map<String, Object>> assetList = new ArrayList<>();
                    for (AssetDO asset : assets) {
                        Map<String, Object> assetInfo = new HashMap<>();
                        assetInfo.put("id", asset.getId());
                        assetInfo.put("name", asset.getName());
                        assetInfo.put("type", asset.getType());
                        assetInfo.put("status", asset.getStatus());
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
        LambdaQueryWrapper<ApprovalTaskDO> pendingTaskWrapper = new LambdaQueryWrapper<>();
        pendingTaskWrapper.eq(ApprovalTaskDO::getInstanceId, instance.getId())
                .eq(ApprovalTaskDO::getStatus, "PENDING");
        List<ApprovalTaskDO> pendingTasks = taskMapper.selectList(pendingTaskWrapper);
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
        result.put("isFirstApprover", task.getIsFirstApprover());
        result.put("nextStageApproverIds", task.getNextStageApproverIds());
        result.put("selectedByUserId", task.getSelectedByUserId());

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
                LambdaQueryWrapper<WorkflowStageDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WorkflowStageDO::getWorkflowId, instance.getWorkflowId())
                       .gt(WorkflowStageDO::getStageOrder, currentStage.getStageOrder())
                       .orderByAsc(WorkflowStageDO::getStageOrder)
                       .last("LIMIT 1");
                WorkflowStageDO nextStage = workflowStageMapper.selectOne(wrapper);
                if (nextStage != null) {
                    result.put("nextStageId", nextStage.getId());
                    result.put("nextStageName", nextStage.getName());
                    result.put("nextStageApproveType", nextStage.getApproveType());

                    // 获取下一阶段配置的审批人列表（按配置顺序，排除子流程）
                    LambdaQueryWrapper<StageApproverDO> approverConfigWrapper = new LambdaQueryWrapper<>();
                    approverConfigWrapper.eq(StageApproverDO::getStageId, nextStage.getId())
                            .isNull(StageApproverDO::getSubWorkflowId)
                            .orderByAsc(StageApproverDO::getId);
                    List<StageApproverDO> approverConfigs = stageApproverMapper.selectList(approverConfigWrapper);

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
                    LambdaQueryWrapper<StageApproverDO> subWorkflowWrapper = new LambdaQueryWrapper<>();
                    subWorkflowWrapper.eq(StageApproverDO::getStageId, nextStage.getId())
                            .isNotNull(StageApproverDO::getSubWorkflowId);
                    List<StageApproverDO> subWorkflowApprovers = stageApproverMapper.selectList(subWorkflowWrapper);

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
                                LambdaQueryWrapper<StageApproverDO> subApproverConfigWrapper = new LambdaQueryWrapper<>();
                                subApproverConfigWrapper.eq(StageApproverDO::getStageId, subFirstStage.getId())
                                        .isNull(StageApproverDO::getSubWorkflowId)
                                        .orderByAsc(StageApproverDO::getId);
                                List<StageApproverDO> subApproverConfigs = stageApproverMapper.selectList(subApproverConfigWrapper);

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
            LambdaQueryWrapper<ApprovalTaskDO> completedTaskWrapper = new LambdaQueryWrapper<>();
            completedTaskWrapper.eq(ApprovalTaskDO::getInstanceId, task.getInstanceId())
                    .eq(ApprovalTaskDO::getStageId, task.getStageId())
                    .eq(ApprovalTaskDO::getStatus, "APPROVED");
            List<ApprovalTaskDO> completedTasks = taskMapper.selectList(completedTaskWrapper);

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
        LambdaQueryWrapper<ApprovalTaskDO> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(ApprovalTaskDO::getInstanceId, task.getInstanceId())
                   .eq(ApprovalTaskDO::getStageId, task.getStageId())
                   .ne(ApprovalTaskDO::getId, task.getId());
        List<ApprovalTaskDO> otherTasks = taskMapper.selectList(taskWrapper);

        List<Map<String, Object>> otherApprovers = new ArrayList<>();
        for (ApprovalTaskDO otherTask : otherTasks) {
            UserDO user = userMapper.selectById(otherTask.getApproverId());
            if (user != null) {
                Map<String, Object> approverInfo = new HashMap<>();
                approverInfo.put("id", user.getId());
                approverInfo.put("name", user.getRealName() != null ? user.getRealName() : user.getUsername());
                approverInfo.put("status", otherTask.getStatus());
                otherApprovers.add(approverInfo);
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

            LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(UserDO::getRoleId, approverId).eq(UserDO::getStatus, 1);

            if (checkSecondary && applicantId != null) {
                // 获取申请人的二级部门
                Long applicantSecondaryDeptId = getSecondaryDeptId(applicantId);
                if (applicantSecondaryDeptId != null) {
                    List<Long> deptIds = new ArrayList<>();
                    deptIds.add(applicantSecondaryDeptId);
                    deptIds.addAll(getAllSubDeptIds(applicantSecondaryDeptId));
                    userWrapper.in(UserDO::getDeptId, deptIds);
                }
            }

            List<UserDO> users = userMapper.selectList(userWrapper);
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
            LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(UserDO::getDeptId, approverId).eq(UserDO::getStatus, 1);
            List<UserDO> users = userMapper.selectList(userWrapper);
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
        LambdaQueryWrapper<WorkflowStageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageDO::getWorkflowId, workflowId)
               .orderByAsc(WorkflowStageDO::getStageOrder)
               .last("LIMIT 1");
        return workflowStageMapper.selectOne(wrapper);
    }
}
