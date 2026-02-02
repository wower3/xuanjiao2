package com.xuanjiao.app.workflow.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.app.user.UserService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import org.springframework.context.annotation.Lazy;
import com.xuanjiao.client.dto.ApproverSelectionDTO;
import com.xuanjiao.client.dto.ApprovalProgressDTO;
import com.xuanjiao.client.dto.WorkflowDTO;
import com.xuanjiao.client.dto.WorkflowStageDTO;
import com.xuanjiao.client.dto.StageApproverDTO;
import com.xuanjiao.infrastructure.dataobject.*;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.approval.ApprovalTaskMapper;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.approval.ApprovalProgressMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 审批人选择服务实现
 */
@Slf4j
@Service
public class ApproverSelectionServiceImpl implements ApproverSelectionService {

    @Resource
    private WorkflowStageMapper workflowStageMapper;

    @Resource
    private StageApproverMapper stageApproverMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private ApprovalTaskMapper approvalTaskMapper;

    @Resource
    private ApprovalInstanceMapper approvalInstanceMapper;

    @Resource
    private ApprovalProgressMapper approvalProgressMapper;

    @Resource
    private WorkflowMapper workflowMapper;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private WorkflowEngineService workflowEngineService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ApproverSelectionDTO> getNextStageApprovers(Long stageId, Long instanceId, Long applicantId, String keyword) {
        // 获取下一阶段配置
        WorkflowStageDO stageDO = workflowStageMapper.selectById(stageId);
        if (stageDO == null) {
            return new ArrayList<>();
        }

        // 获取该阶段的审批人配置
        LambdaQueryWrapper<StageApproverDO> approverWrapper = new LambdaQueryWrapper<>();
        approverWrapper.eq(StageApproverDO::getStageId, stageId);
        List<StageApproverDO> approvers = stageApproverMapper.selectList(approverWrapper);

        // 获取申请人的二级部门ID（用于校验）
        Long applicantSecondaryDeptId = getSecondaryDeptId(applicantId);

        Set<Long> userIdSet = new HashSet<>();
        Set<Long> roleIdSet = new HashSet<>();
        Set<Long> deptIdSet = new HashSet<>();

        // 收集所有需要查询的ID
        for (StageApproverDO approver : approvers) {
            if ("USER".equals(approver.getApproverType())) {
                userIdSet.add(approver.getApproverId());
            } else if ("ROLE".equals(approver.getApproverType())) {
                roleIdSet.add(approver.getApproverId());
            } else if ("DEPT".equals(approver.getApproverType())) {
                deptIdSet.add(approver.getApproverId());
            }
        }

        List<ApproverSelectionDTO> result = new ArrayList<>();

        // 处理指定用户
        if (!userIdSet.isEmpty()) {
            LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(UserDO::getId, userIdSet);
            if (keyword != null && !keyword.trim().isEmpty()) {
                userWrapper.and(wrapper -> wrapper
                    .like(UserDO::getUsername, keyword)
                    .or()
                    .like(UserDO::getRealName, keyword)
                );
            }
            List<UserDO> users = userMapper.selectList(userWrapper);
            for (UserDO user : users) {
                ApproverSelectionDTO dto = convertToSelectionDTO(user);
                result.add(dto);
            }
        }

        // 处理指定角色
        if (!roleIdSet.isEmpty()) {
            for (Long roleId : roleIdSet) {
                // 检查是否需要校验二级部门
                StageApproverDO approver = approvers.stream()
                    .filter(a -> "ROLE".equals(a.getApproverType()) && a.getApproverId().equals(roleId))
                    .findFirst()
                    .orElse(null);

                boolean checkSecondary = approver != null && approver.getCheckSecondaryDept() != null && approver.getCheckSecondaryDept() == 1;

                LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(UserDO::getRoleId, roleId);

                // 如果需要校验二级部门
                if (checkSecondary && applicantSecondaryDeptId != null) {
                    // 查询该二级部门下的所有用户
                    List<Long> deptIds = getAllSubDeptIds(applicantSecondaryDeptId);
                    deptIds.add(applicantSecondaryDeptId);
                    userWrapper.in(UserDO::getDeptId, deptIds);
                }

                if (keyword != null && !keyword.trim().isEmpty()) {
                    userWrapper.and(wrapper -> wrapper
                        .like(UserDO::getUsername, keyword)
                        .or()
                        .like(UserDO::getRealName, keyword)
                    );
                }

                List<UserDO> users = userMapper.selectList(userWrapper);
                for (UserDO user : users) {
                    // 避免重复
                    if (result.stream().noneMatch(dto -> dto.getId().equals(user.getId()))) {
                        ApproverSelectionDTO dto = convertToSelectionDTO(user);
                        result.add(dto);
                    }
                }
            }
        }

        // 处理指定部门
        if (!deptIdSet.isEmpty()) {
            for (Long deptId : deptIdSet) {
                LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.eq(UserDO::getDeptId, deptId);

                if (keyword != null && !keyword.trim().isEmpty()) {
                    userWrapper.and(wrapper -> wrapper
                        .like(UserDO::getUsername, keyword)
                        .or()
                        .like(UserDO::getRealName, keyword)
                    );
                }

                List<UserDO> users = userMapper.selectList(userWrapper);
                for (UserDO user : users) {
                    if (result.stream().noneMatch(dto -> dto.getId().equals(user.getId()))) {
                        ApproverSelectionDTO dto = convertToSelectionDTO(user);
                        result.add(dto);
                    }
                }
            }
        }

        return result;
    }

    @Override
    @Transactional
    public void selectNextStageApprovers(Long taskId, List<Long> approverIds) {
        selectNextStageApprovers(taskId, approverIds, null);
    }

    @Override
    @Transactional
    public void selectNextStageApprovers(Long taskId, List<Long> approverIds, Map<Long, List<Long>> subWorkflowApproverIds) {
        ApprovalTaskDO task = approvalTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        try {
            // 保存选中的审批人ID列表（JSON格式）
            String approverIdsJson = objectMapper.writeValueAsString(approverIds);
            task.setNextStageApproverIds(approverIdsJson);
            task.setSelectedByUserId(task.getApproverId());

            // 保存子流程审批人选择
            if (subWorkflowApproverIds != null && !subWorkflowApproverIds.isEmpty()) {
                String subWorkflowApproverIdsJson = objectMapper.writeValueAsString(subWorkflowApproverIds);
                task.setSubWorkflowApproverIds(subWorkflowApproverIdsJson);
            }

            approvalTaskMapper.updateById(task);
        } catch (Exception e) {
            throw new RuntimeException("保存审批人选择失败", e);
        }
    }

    @Override
    @Transactional
    public void selectFirstStageApproversWithSubWorkflows(Long instanceId, List<Long> approverIds, Map<Long, List<Long>> subWorkflowApproverIds) {
        ApprovalInstanceDO instance = approvalInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("审批实例不存在");
        }

        // 获取第一阶段
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, instance.getWorkflowId())
                    .orderByAsc(WorkflowStageDO::getStageOrder)
                    .last("LIMIT 1");
        WorkflowStageDO firstStage = workflowStageMapper.selectOne(stageWrapper);
        if (firstStage == null) {
            throw new RuntimeException("未找到第一阶段");
        }

        // 保存子流程审批人选择到实例
        if (subWorkflowApproverIds != null && !subWorkflowApproverIds.isEmpty()) {
            try {
                String subWorkflowApproverIdsJson = objectMapper.writeValueAsString(subWorkflowApproverIds);
                instance.setSubWorkflowApproverIds(subWorkflowApproverIdsJson);
                approvalInstanceMapper.updateById(instance);
            } catch (Exception e) {
                throw new RuntimeException("保存子流程审批人选择失败", e);
            }
        }

        // 为选中的主流程审批人创建任务
        for (Long approverId : approverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(instanceId);
            task.setStageId(firstStage.getId());
            task.setApproverId(approverId);
            task.setStatus("PENDING");
            task.setIsFirstApprover(0);
            task.setSelectedByUserId(instance.getApplicantId());
            approvalTaskMapper.insert(task);
        }

        // 更新进度记录
        updateProgressRecordWithApprovers(instanceId, firstStage.getId(), approverIds);

        // 立即启动第一阶段的所有子流程（子流程独立运行，不阻塞主流程）
        // parentTaskId 为 null，因为还没有人完成审批
        if (subWorkflowApproverIds != null && !subWorkflowApproverIds.isEmpty()) {
            workflowEngineService.startSubProcessesForStage(instanceId, firstStage.getId(), null, subWorkflowApproverIds);
        }
    }

    @Override
    public List<ApprovalProgressDTO> getApprovalProgress(Long instanceId) {
        // 获取主实例信息，以便获取工作流ID
        ApprovalInstanceDO instance = approvalInstanceMapper.selectById(instanceId);
        if (instance == null) {
            return new ArrayList<>();
        }

        // 判断是否是子流程
        boolean isSubWorkflow = instance.getParentInstanceId() != null;

        if (isSubWorkflow) {
            // 子流程：只返回子流程自己的进度
            return getSubWorkflowProgress(instance);
        } else {
            // 主流程：返回主流程进度 + 所有子流程进度
            return getMainWorkflowProgress(instance);
        }
    }

    /**
     * 获取主流程的进度（包含所有子流程进度）
     */
    private List<ApprovalProgressDTO> getMainWorkflowProgress(ApprovalInstanceDO instance) {
        Long instanceId = instance.getId();

        // 获取主实例进度（主流程的进度）
        LambdaQueryWrapper<ApprovalProgressDO> mainWrapper = new LambdaQueryWrapper<>();
        mainWrapper.eq(ApprovalProgressDO::getInstanceId, instanceId)
                   .isNull(ApprovalProgressDO::getParentInstanceId); // 只获取主流程进度，不包含子流程
        List<ApprovalProgressDO> mainProgress = approvalProgressMapper.selectList(mainWrapper);

        // 获取工作流的所有阶段（主流程）
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, instance.getWorkflowId())
                    .orderByAsc(WorkflowStageDO::getStageOrder);
        List<WorkflowStageDO> allStages = workflowStageMapper.selectList(stageWrapper);

        // 创建已有进度的stageId集合，便于查找
        Set<Long> existingStageIds = mainProgress.stream()
            .map(ApprovalProgressDO::getStageId)
            .collect(Collectors.toSet());

        // 为还没有到达的阶段创建"未开始"状态的进度记录
        List<ApprovalProgressDO> allProgress = new ArrayList<>(mainProgress);
        for (WorkflowStageDO stage : allStages) {
            if (!existingStageIds.contains(stage.getId())) {
                // 这个阶段还没有进度记录，创建一个"未开始"的记录
                ApprovalProgressDO notStartedProgress = new ApprovalProgressDO();
                notStartedProgress.setId(null); // 新记录没有ID
                notStartedProgress.setInstanceId(instanceId);
                notStartedProgress.setStageId(stage.getId());
                notStartedProgress.setStageName(stage.getName());
                notStartedProgress.setStageOrder(stage.getStageOrder());
                notStartedProgress.setStatus("NOT_STARTED"); // 未开始状态
                notStartedProgress.setIsSubWorkflow(0);
                notStartedProgress.setParentInstanceId(null);
                notStartedProgress.setParentTaskId(null);
                notStartedProgress.setApprovers(null); // 未到达的阶段没有人员信息
                notStartedProgress.setApproveTime(null);
                allProgress.add(notStartedProgress);
            }
        }

        // 获取所有子流程进度（parentInstanceId指向主实例的记录）
        // 只获取非 CANCELLED 状态的子流程实例的进度
        LambdaQueryWrapper<ApprovalInstanceDO> subInstanceWrapper = new LambdaQueryWrapper<>();
        subInstanceWrapper.eq(ApprovalInstanceDO::getParentInstanceId, instanceId)
                       .ne(ApprovalInstanceDO::getStatus, "CANCELLED");
        List<ApprovalInstanceDO> activeSubInstances = approvalInstanceMapper.selectList(subInstanceWrapper);

        List<ApprovalProgressDO> subProgress = new ArrayList<>();
        if (!activeSubInstances.isEmpty()) {
            List<Long> activeSubInstanceIds = activeSubInstances.stream()
                .map(ApprovalInstanceDO::getId)
                .collect(Collectors.toList());

            LambdaQueryWrapper<ApprovalProgressDO> subProgressWrapper = new LambdaQueryWrapper<>();
            subProgressWrapper.in(ApprovalProgressDO::getInstanceId, activeSubInstanceIds);
            subProgress = approvalProgressMapper.selectList(subProgressWrapper);
        }

        // 合并主流程和子流程进度
        allProgress.addAll(subProgress);

        return allProgress.stream()
            .map(this::convertToProgressDTO)
            .sorted(Comparator.comparing(ApprovalProgressDTO::getStageOrder))
            .collect(Collectors.toList());
    }

    /**
     * 获取子流程的进度（只返回子流程自己的进度）
     */
    private List<ApprovalProgressDTO> getSubWorkflowProgress(ApprovalInstanceDO subInstance) {
        Long instanceId = subInstance.getId();

        // 获取子流程已有的进度记录
        LambdaQueryWrapper<ApprovalProgressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProgressDO::getInstanceId, instanceId);
        List<ApprovalProgressDO> existingProgress = approvalProgressMapper.selectList(wrapper);

        // 获取子流程的所有阶段
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, subInstance.getWorkflowId())
                    .orderByAsc(WorkflowStageDO::getStageOrder);
        List<WorkflowStageDO> allStages = workflowStageMapper.selectList(stageWrapper);

        // 创建已有进度的stageId集合
        Set<Long> existingStageIds = existingProgress.stream()
            .map(ApprovalProgressDO::getStageId)
            .collect(Collectors.toSet());

        // 为还没有到达的阶段创建"未开始"状态的进度记录
        List<ApprovalProgressDO> allProgress = new ArrayList<>(existingProgress);
        for (WorkflowStageDO stage : allStages) {
            if (!existingStageIds.contains(stage.getId())) {
                ApprovalProgressDO notStartedProgress = new ApprovalProgressDO();
                notStartedProgress.setId(null);
                notStartedProgress.setInstanceId(instanceId);
                notStartedProgress.setStageId(stage.getId());
                notStartedProgress.setStageName(stage.getName());
                notStartedProgress.setStageOrder(stage.getStageOrder());
                notStartedProgress.setStatus("NOT_STARTED");
                notStartedProgress.setIsSubWorkflow(1); // 子流程
                notStartedProgress.setParentInstanceId(subInstance.getParentInstanceId());
                notStartedProgress.setParentTaskId(subInstance.getParentTaskId());
                notStartedProgress.setApprovers(null);
                notStartedProgress.setApproveTime(null);
                allProgress.add(notStartedProgress);
            }
        }

        return allProgress.stream()
            .map(this::convertToProgressDTO)
            .sorted(Comparator.comparing(ApprovalProgressDTO::getStageOrder))
            .collect(Collectors.toList());
    }

    @Override
    public WorkflowDTO getWorkflowByRole(Long roleId, String workflowType) {
        LambdaQueryWrapper<WorkflowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowDO::getBoundRoleId, roleId)
               .eq(WorkflowDO::getWorkflowType, workflowType)
               .eq(WorkflowDO::getStatus, 1)
               .eq(WorkflowDO::getDeleted, 0);

        WorkflowDO workflow = workflowMapper.selectOne(wrapper);
        if (workflow == null) {
            return null;
        }

        // 使用 WorkflowServiceImpl 来获取完整的工作流数据（包括 stages）
        return getWorkflowDetails(workflow.getId());
    }

    /**
     * 获取工作流详细信息（包括阶段和审批人）
     */
    private WorkflowDTO getWorkflowDetails(Long workflowId) {
        WorkflowDO workflow = workflowMapper.selectById(workflowId);
        if (workflow == null) {
            return null;
        }

        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(workflow.getId());
        dto.setName(workflow.getName());
        dto.setWorkflowType(workflow.getWorkflowType());
        dto.setBoundRoleId(workflow.getBoundRoleId());

        // 加载绑定的角色名称
        if (workflow.getBoundRoleId() != null) {
            RoleDO role = roleMapper.selectById(workflow.getBoundRoleId());
            if (role != null) {
                dto.setRoleName(role.getName());
            }
        }

        // 加载阶段和审批人
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, workflowId).orderByAsc(WorkflowStageDO::getStageOrder);
        List<WorkflowStageDO> stages = workflowStageMapper.selectList(stageWrapper);

        List<WorkflowStageDTO> stageDTOs = new ArrayList<>();
        for (WorkflowStageDO stage : stages) {
            WorkflowStageDTO stageDTO = new WorkflowStageDTO();
            stageDTO.setId(stage.getId());
            stageDTO.setWorkflowId(stage.getWorkflowId());
            stageDTO.setName(stage.getName());
            stageDTO.setStageOrder(stage.getStageOrder());
            stageDTO.setApproveType(stage.getApproveType());

            // 加载该阶段的审批人
            LambdaQueryWrapper<StageApproverDO> approverWrapper = new LambdaQueryWrapper<>();
            approverWrapper.eq(StageApproverDO::getStageId, stage.getId());
            List<StageApproverDO> approvers = stageApproverMapper.selectList(approverWrapper);

            List<StageApproverDTO> approverDTOs = new ArrayList<>();
            for (StageApproverDO approver : approvers) {
                StageApproverDTO approverDTO = new StageApproverDTO();
                approverDTO.setId(approver.getId());
                approverDTO.setStageId(approver.getStageId());
                approverDTO.setApproverType(approver.getApproverType());
                approverDTO.setApproverId(approver.getApproverId());
                approverDTO.setCheckSecondaryDept(approver.getCheckSecondaryDept());
                approverDTO.setSubWorkflowId(approver.getSubWorkflowId());

                // 设置审批人名称
                String approverName = getApproverName(approver.getApproverType(), approver.getApproverId());
                approverDTO.setApproverName(approverName);

                // 如果是子流程，加载子流程名称
                if (approver.getSubWorkflowId() != null) {
                    WorkflowDO subWorkflow = workflowMapper.selectById(approver.getSubWorkflowId());
                    if (subWorkflow != null) {
                        approverDTO.setSubWorkflowName(subWorkflow.getName());
                    }
                }

                approverDTOs.add(approverDTO);
            }

            stageDTO.setApprovers(approverDTOs);
            stageDTOs.add(stageDTO);
        }

        dto.setStages(stageDTOs);
        return dto;
    }

    /**
     * 获取审批人名称
     */
    private String getApproverName(String type, Long id) {
        if ("USER".equals(type)) {
            UserDO user = userMapper.selectById(id);
            return user != null ? "[用户] " + (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "[用户] 未知";
        } else if ("ROLE".equals(type)) {
            RoleDO role = roleMapper.selectById(id);
            return role != null ? "[角色] " + role.getName() : "[角色] 未知";
        } else if ("DEPT".equals(type)) {
            DeptDO dept = deptMapper.selectById(id);
            return dept != null ? "[部门] " + dept.getName() : "[部门] 未知";
        }
        return "未知";
    }

    @Override
    public Map<String, Object> getFirstStageApprovers(Long workflowId, Long applicantId, String keyword) {
        Map<String, Object> result = new HashMap<>();

        // 获取流程信息
        WorkflowDO workflow = workflowMapper.selectById(workflowId);
        if (workflow == null) {
            result.put("workflowId", workflowId);
            result.put("workflowName", null);
            result.put("approveType", null);
            result.put("approverConfigs", new ArrayList<>());
            result.put("approverCount", 0);
            return result;
        }
        result.put("workflowId", workflow.getId());
        result.put("workflowName", workflow.getName());

        // 获取第一阶段的配置
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, workflowId)
                    .orderByAsc(WorkflowStageDO::getStageOrder)
                    .last("LIMIT 1");
        WorkflowStageDO firstStage = workflowStageMapper.selectOne(stageWrapper);
        if (firstStage == null) {
            result.put("stageId", null);
            result.put("stageName", null);
            result.put("approveType", null);
            result.put("approverConfigs", new ArrayList<>());
            result.put("approverCount", 0);
            return result;
        }
        result.put("stageId", firstStage.getId());
        result.put("stageName", firstStage.getName());
        result.put("approveType", firstStage.getApproveType());

        // 获取该阶段的审批人配置（排除子流程）
        LambdaQueryWrapper<StageApproverDO> approverWrapper = new LambdaQueryWrapper<>();
        approverWrapper.eq(StageApproverDO::getStageId, firstStage.getId())
                .isNull(StageApproverDO::getSubWorkflowId)
                .orderByAsc(StageApproverDO::getId);
        List<StageApproverDO> approverConfigs = stageApproverMapper.selectList(approverWrapper);

        // 为每个配置获取可用用户
        List<Map<String, Object>> configs = new ArrayList<>();
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

            // 获取该配置的可用用户
            List<Map<String, Object>> availableUsers = getAvailableUsersForConfig(config, applicantId, keyword);
            configInfo.put("availableUsers", availableUsers);

            configs.add(configInfo);
        }

        result.put("approverConfigs", configs);
        result.put("approverCount", configs.size());

        return result;
    }

    @Override
    @Transactional
    public void selectFirstStageApprovers(Long instanceId, List<Long> approverIds) {
        ApprovalInstanceDO instance = approvalInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("审批实例不存在");
        }

        // 获取第一阶段
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, instance.getWorkflowId())
                    .orderByAsc(WorkflowStageDO::getStageOrder)
                    .last("LIMIT 1");
        WorkflowStageDO firstStage = workflowStageMapper.selectOne(stageWrapper);
        if (firstStage == null) {
            throw new RuntimeException("未找到第一阶段");
        }

        // 为选中的审批人创建任务
        for (Long approverId : approverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(instanceId);
            task.setStageId(firstStage.getId());
            task.setApproverId(approverId);
            task.setStatus("PENDING");
            task.setIsFirstApprover(0); // 第一个审批的审批人标记
            task.setSelectedByUserId(instance.getApplicantId()); // 由发起人选择
            approvalTaskMapper.insert(task);
        }

        // 更新进度记录，添加审批人信息
        updateProgressRecordWithApprovers(instanceId, firstStage.getId(), approverIds);
    }

    /**
     * 更新进度记录，添加审批人信息
     */
    private void updateProgressRecordWithApprovers(Long instanceId, Long stageId, List<Long> approverIds) {
        LambdaQueryWrapper<ApprovalProgressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProgressDO::getInstanceId, instanceId)
               .eq(ApprovalProgressDO::getStageId, stageId);
        ApprovalProgressDO progress = approvalProgressMapper.selectOne(wrapper);

        if (progress != null) {
            try {
                List<Map<String, Object>> approvers = new ArrayList<>();
                for (Long approverId : approverIds) {
                    UserDO user = userMapper.selectById(approverId);
                    if (user != null) {
                        Map<String, Object> approverInfo = new HashMap<>();
                        approverInfo.put("id", user.getId());
                        approverInfo.put("name", user.getRealName() != null ? user.getRealName() : user.getUsername());
                        approverInfo.put("status", "PENDING");
                        approverInfo.put("approveTime", null);
                        approverInfo.put("comment", null);
                        approvers.add(approverInfo);
                    }
                }
                progress.setApprovers(objectMapper.writeValueAsString(approvers));
                approvalProgressMapper.updateById(progress);
            } catch (Exception e) {
                // 忽略序列化错误
            }
        }
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

        // 如果当前部门就是二级机构（level=2），直接返回
        if (dept.getLevel() == 2) {
            return dept.getId();
        }

        // 否则向上查找，直到找到二级机构
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
     * 获取指定部门的所有子部门ID（递归）
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
     * 将UserDO转换为ApproverSelectionDTO
     */
    private ApproverSelectionDTO convertToSelectionDTO(UserDO user) {
        ApproverSelectionDTO dto = new ApproverSelectionDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setDeptId(user.getDeptId());
        dto.setRoleId(user.getRoleId());

        // 加载部门名称
        if (user.getDeptId() != null) {
            DeptDO dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                dto.setDeptName(dept.getName());
            }
        }

        // 加载角色名称
        if (user.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(user.getRoleId());
            if (role != null) {
                dto.setRoleName(role.getName());
            }
        }

        return dto;
    }

    /**
     * 将ApprovalProgressDO转换为ApprovalProgressDTO
     */
    private ApprovalProgressDTO convertToProgressDTO(ApprovalProgressDO progressDO) {
        ApprovalProgressDTO dto = new ApprovalProgressDTO();
        dto.setId(progressDO.getId());
        dto.setInstanceId(progressDO.getInstanceId());
        dto.setStageId(progressDO.getStageId());
        dto.setStageName(progressDO.getStageName());
        dto.setStageOrder(progressDO.getStageOrder());
        dto.setStatus(progressDO.getStatus());
        dto.setIsSubWorkflow(progressDO.getIsSubWorkflow());
        dto.setParentInstanceId(progressDO.getParentInstanceId());
        dto.setParentTaskId(progressDO.getParentTaskId());
        dto.setApproveTime(progressDO.getApproveTime());
        dto.setCreateTime(progressDO.getCreateTime());
        dto.setUpdateTime(progressDO.getUpdateTime());

        // 解析审批人列表
        if (progressDO.getApprovers() != null && !progressDO.getApprovers().isEmpty()) {
            try {
                List<ApprovalProgressDTO.ApproverInfo> approvers = objectMapper.readValue(
                    progressDO.getApprovers(),
                    new TypeReference<List<ApprovalProgressDTO.ApproverInfo>>() {}
                );
                dto.setApprovers(approvers);
            } catch (Exception e) {
                // 忽略解析错误
            }
        }

        return dto;
    }

    @Override
    public Map<String, Object> getSubWorkflowFirstStageApprovers(Long subWorkflowId, Long applicantId, String keyword) {
        Map<String, Object> result = new HashMap<>();

        // 获取子流程信息
        WorkflowDO workflow = workflowMapper.selectById(subWorkflowId);
        if (workflow == null) {
            result.put("workflowId", subWorkflowId);
            result.put("workflowName", null);
            result.put("approverConfigs", new ArrayList<>());
            result.put("approverCount", 0);
            return result;
        }
        result.put("workflowId", workflow.getId());
        result.put("workflowName", workflow.getName());

        // 获取子流程第一阶段的配置
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, subWorkflowId)
                    .orderByAsc(WorkflowStageDO::getStageOrder)
                    .last("LIMIT 1");
        WorkflowStageDO firstStage = workflowStageMapper.selectOne(stageWrapper);
        if (firstStage == null) {
            result.put("approveType", null);
            result.put("approverConfigs", new ArrayList<>());
            result.put("approverCount", 0);
            return result;
        }
        result.put("approveType", firstStage.getApproveType());

        // 获取该阶段的审批人配置（排除子流程）
        LambdaQueryWrapper<StageApproverDO> approverWrapper = new LambdaQueryWrapper<>();
        approverWrapper.eq(StageApproverDO::getStageId, firstStage.getId());
        List<StageApproverDO> approverConfigs = stageApproverMapper.selectList(approverWrapper);

        // 为每个配置获取可用用户
        List<Map<String, Object>> configs = new ArrayList<>();
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

            // 获取该配置的可用用户
            List<Map<String, Object>> availableUsers = getAvailableUsersForConfig(config, applicantId, keyword);
            configInfo.put("availableUsers", availableUsers);

            configs.add(configInfo);
        }

        result.put("approverConfigs", configs);
        result.put("approverCount", configs.size());

        return result;
    }

    /**
     * 获取指定配置的可用用户列表
     */
    private List<Map<String, Object>> getAvailableUsersForConfig(StageApproverDO config, Long applicantId, String keyword) {
        List<Map<String, Object>> users = new ArrayList<>();

        if ("USER".equals(config.getApproverType())) {
            // 指定用户：直接返回该用户
            UserDO user = userMapper.selectById(config.getApproverId());
            if (user != null) {
                if (keyword == null || keyword.trim().isEmpty()) {
                    users.add(convertUserToMap(user));
                } else {
                    // 支持模糊搜索
                    if ((user.getUsername() != null && user.getUsername().toLowerCase().contains(keyword.toLowerCase())) ||
                        (user.getRealName() != null && user.getRealName().toLowerCase().contains(keyword.toLowerCase()))) {
                        users.add(convertUserToMap(user));
                    }
                }
            }
        } else if ("ROLE".equals(config.getApproverType())) {
            // 指定角色：返回该角色下的所有用户
            LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(UserDO::getRoleId, config.getApproverId());

            // 如果需要校验二级部门
            if (config.getCheckSecondaryDept() != null && config.getCheckSecondaryDept() == 1) {
                Long applicantSecondaryDeptId = getSecondaryDeptId(applicantId);
                if (applicantSecondaryDeptId != null) {
                    List<Long> deptIds = getAllSubDeptIds(applicantSecondaryDeptId);
                    deptIds.add(applicantSecondaryDeptId);
                    userWrapper.in(UserDO::getDeptId, deptIds);
                }
            }

            // 支持模糊搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                userWrapper.and(wrapper -> wrapper
                    .like(UserDO::getUsername, keyword)
                    .or()
                    .like(UserDO::getRealName, keyword)
                );
            }

            List<UserDO> roleUsers = userMapper.selectList(userWrapper);
            for (UserDO user : roleUsers) {
                users.add(convertUserToMap(user));
            }
        } else if ("DEPT".equals(config.getApproverType())) {
            // 指定部门：返回该部门下的所有用户
            LambdaQueryWrapper<UserDO> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(UserDO::getDeptId, config.getApproverId());

            // 支持模糊搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                userWrapper.and(wrapper -> wrapper
                    .like(UserDO::getUsername, keyword)
                    .or()
                    .like(UserDO::getRealName, keyword)
                );
            }

            List<UserDO> deptUsers = userMapper.selectList(userWrapper);
            for (UserDO user : deptUsers) {
                users.add(convertUserToMap(user));
            }
        }

        return users;
    }

    /**
     * 将用户DO转换为Map
     */
    private Map<String, Object> convertUserToMap(UserDO user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("deptId", user.getDeptId());
        map.put("roleId", user.getRoleId());

        // 获取部门名称
        if (user.getDeptId() != null) {
            DeptDO dept = deptMapper.selectById(user.getDeptId());
            map.put("deptName", dept != null ? dept.getName() : null);
        }

        // 获取角色名称
        if (user.getRoleId() != null) {
            RoleDO role = roleMapper.selectById(user.getRoleId());
            map.put("roleName", role != null ? role.getName() : null);
        }

        return map;
    }

    @Override
    @Transactional
    public void selectSubWorkflowFirstStageApprovers(Long subInstanceId, List<Long> approverIds) {
        ApprovalInstanceDO subInstance = approvalInstanceMapper.selectById(subInstanceId);
        if (subInstance == null) {
            throw new RuntimeException("子流程实例不存在");
        }

        // 获取子流程第一阶段
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, subInstance.getWorkflowId())
                    .orderByAsc(WorkflowStageDO::getStageOrder)
                    .last("LIMIT 1");
        WorkflowStageDO firstStage = workflowStageMapper.selectOne(stageWrapper);
        if (firstStage == null) {
            throw new RuntimeException("未找到子流程第一阶段");
        }

        // 为选中的审批人创建任务
        for (Long approverId : approverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(subInstanceId);
            task.setStageId(firstStage.getId());
            task.setApproverId(approverId);
            task.setStatus("PENDING");
            task.setIsFirstApprover(0);
            task.setSelectedByUserId(subInstance.getApplicantId()); // 由发起人（或上一层审批人）选择
            approvalTaskMapper.insert(task);
        }

        // 更新进度记录，添加审批人信息
        updateProgressRecordWithApprovers(subInstanceId, firstStage.getId(), approverIds);
    }
}
