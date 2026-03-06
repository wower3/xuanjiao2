package com.xuanjiao.app.workflow.impl;

import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.app.user.UserService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.common.exception.NotFoundException;
import com.xuanjiao.common.exception.SystemException;
import org.springframework.context.annotation.Lazy;
import com.xuanjiao.client.workflow.ApproverSelectionDTO;
import com.xuanjiao.client.workflow.FirstStageApproversDTO;
import com.xuanjiao.client.workflow.ApproverConfigDTO;
import com.xuanjiao.client.approval.ApprovalProgressDTO;
import com.xuanjiao.client.workflow.WorkflowDTO;
import com.xuanjiao.client.workflow.WorkflowStageDTO;
import com.xuanjiao.client.workflow.StageApproverDTO;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import com.xuanjiao.infrastructure.dataobject.ApprovalProgressDO;
import com.xuanjiao.infrastructure.dataobject.ApprovalTaskDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowQuery;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverWithDetailsDO;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import com.xuanjiao.infrastructure.user.UserWithDetailsDO;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.approval.ApprovalTaskMapper;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceQuery;
import com.xuanjiao.infrastructure.approval.ApprovalProgressMapper;
import com.xuanjiao.infrastructure.approval.ApprovalProgressQuery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 审批人选择服务实现类
 * <p>实现ApproverSelectionService接口，封装审批人选择逻辑</p>
 * <p>核心功能：获取可选审批人、选择审批人、审批进度查询</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.workflow.ApproverSelectionService
 */
@Slf4j
@Service
public class ApproverSelectionServiceImpl implements ApproverSelectionService {

    /** 消息常量 */
    private static final String MSG_INSTANCE_NOT_FOUND = "审批实例不存在";
    private static final String MSG_TASK_NOT_FOUND = "任务不存在";

    /** 审批状态常量 */
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String STATUS_CANCELLED = "CANCELLED";

    /** 审批人类型常量 */
    private static final String APPROVER_TYPE_USER = "USER";
    private static final String APPROVER_TYPE_ROLE = "ROLE";
    private static final String APPROVER_TYPE_DEPT = "DEPT";
    private static final String APPROVER_TYPE_SUB_WORKFLOW = "SUB_WORKFLOW";

    /** 数据库列名常量 */
    private static final String COLUMN_STAGE_ORDER = "stage_order";

    /** 排序方向常量 */
    private static final String ORDER_ASC = "ASC";
    private static final String ORDER_DESC = "DESC";

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
        WorkflowStageDO stageDO = workflowStageMapper.selectById(stageId);
        if (stageDO == null) {
            return new ArrayList<>();
        }

        List<StageApproverDO> approvers = getApproversForStage(stageId);
        ApproverTypeIds ids = collectApproverTypeIds(approvers);
        Long applicantSecondaryDeptId = getSecondaryDeptId(applicantId);

        List<ApproverSelectionDTO> result = new ArrayList<>();
        result.addAll(getUsersByUserIds(ids.userIds, keyword));
        result.addAll(getUsersByRoleIds(ids.roleIds, approvers, applicantSecondaryDeptId, keyword, result));
        result.addAll(getUsersByDeptIds(ids.deptIds, keyword, result));
        return result;
    }

    /**
     * 获取阶段的所有审批人配置
     */
    private List<StageApproverDO> getApproversForStage(Long stageId) {
        StageApproverQuery approverQuery = new StageApproverQuery();
        approverQuery.setStageId(stageId);
        return stageApproverMapper.selectList(approverQuery);
    }

    /**
     * 收集按类型分组的审批人ID
     */
    private ApproverTypeIds collectApproverTypeIds(List<StageApproverDO> approvers) {
        Set<Long> userIdSet = new HashSet<>();
        Set<Long> roleIdSet = new HashSet<>();
        Set<Long> deptIdSet = new HashSet<>();

        for (StageApproverDO approver : approvers) {
            switch (approver.getApproverType()) {
                case APPROVER_TYPE_USER:
                    userIdSet.add(approver.getApproverId());
                    break;
                case APPROVER_TYPE_ROLE:
                    roleIdSet.add(approver.getApproverId());
                    break;
                case APPROVER_TYPE_DEPT:
                    deptIdSet.add(approver.getApproverId());
                    break;
            }
        }
        return new ApproverTypeIds(userIdSet, roleIdSet, deptIdSet);
    }

    /**
     * 通过用户ID列表获取用户
     */
    private List<ApproverSelectionDTO> getUsersByUserIds(Set<Long> userIds, String keyword) {
        if (userIds.isEmpty()) {
            return new ArrayList<>();
        }
        UserQuery userQuery = new UserQuery();
        userQuery.setUserIds(new ArrayList<>(userIds));
        addKeywordFilter(userQuery, keyword);
        return userMapper.selectList(userQuery).stream()
            .map(this::convertToSelectionDTO)
            .collect(Collectors.toList());
    }

    /**
     * 通过角色ID列表获取用户
     */
    private List<ApproverSelectionDTO> getUsersByRoleIds(
            Set<Long> roleIds,
            List<StageApproverDO> approvers,
            Long applicantSecondaryDeptId,
            String keyword,
            List<ApproverSelectionDTO> existingResults) {

        List<ApproverSelectionDTO> result = new ArrayList<>();
        Set<Long> existingUserIds = existingResults.stream()
            .map(ApproverSelectionDTO::getId)
            .collect(Collectors.toSet());

        for (Long roleId : roleIds) {
            StageApproverDO approverConfig = findApproverConfig(approvers, roleId, APPROVER_TYPE_ROLE);
            List<UserDO> users = queryUsersByRole(roleId, approverConfig, applicantSecondaryDeptId, keyword);

            for (UserDO user : users) {
                if (!existingUserIds.contains(user.getId())) {
                    result.add(convertToSelectionDTO(user));
                    existingUserIds.add(user.getId());
                }
            }
        }
        return result;
    }

    /**
     * 查询指定角色的用户
     */
    private List<UserDO> queryUsersByRole(Long roleId, StageApproverDO approverConfig, Long applicantSecondaryDeptId, String keyword) {
        UserQuery userQuery = new UserQuery();
        userQuery.setRoleId(roleId);

        if (shouldCheckSecondaryDept(approverConfig) && applicantSecondaryDeptId != null) {
            List<Long> deptIds = new ArrayList<>();
            deptIds.add(applicantSecondaryDeptId);
            deptIds.addAll(getAllSubDeptIds(applicantSecondaryDeptId));
            userQuery.setDeptIds(deptIds);
        }

        addKeywordFilter(userQuery, keyword);
        return userMapper.selectList(userQuery);
    }

    /**
     * 通过部门ID列表获取用户
     */
    private List<ApproverSelectionDTO> getUsersByDeptIds(
            Set<Long> deptIds,
            String keyword,
            List<ApproverSelectionDTO> existingResults) {

        Set<Long> existingUserIds = existingResults.stream()
            .map(ApproverSelectionDTO::getId)
            .collect(Collectors.toSet());

        List<ApproverSelectionDTO> result = new ArrayList<>();
        for (Long deptId : deptIds) {
            List<UserDO> users = queryUsersByDept(deptId, keyword);
            for (UserDO user : users) {
                if (!existingUserIds.contains(user.getId())) {
                    result.add(convertToSelectionDTO(user));
                    existingUserIds.add(user.getId());
                }
            }
        }
        return result;
    }

    /**
     * 查询指定部门的用户
     */
    private List<UserDO> queryUsersByDept(Long deptId, String keyword) {
        UserQuery userQuery = new UserQuery();
        userQuery.setDeptId(deptId);
        addKeywordFilter(userQuery, keyword);
        return userMapper.selectList(userQuery);
    }

    /**
     * 添加关键词过滤条件
     */
    private void addKeywordFilter(UserQuery query, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setKeyword(keyword.trim());
        }
    }

    /**
     * 查找指定类型和ID的审批人配置
     */
    private StageApproverDO findApproverConfig(List<StageApproverDO> approvers, Long id, String type) {
        return approvers.stream()
            .filter(a -> type.equals(a.getApproverType()) && id.equals(a.getApproverId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 判断是否需要校验二级部门
     */
    private boolean shouldCheckSecondaryDept(StageApproverDO approver) {
        return approver != null
            && approver.getCheckSecondaryDept() != null
            && approver.getCheckSecondaryDept() == 1;
    }

    /**
     * 审批人类型ID集合
     */
    private static class ApproverTypeIds {
        Set<Long> userIds;
        Set<Long> roleIds;
        Set<Long> deptIds;

        ApproverTypeIds(Set<Long> userIds, Set<Long> roleIds, Set<Long> deptIds) {
            this.userIds = userIds;
            this.roleIds = roleIds;
            this.deptIds = deptIds;
        }
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
            throw new NotFoundException(MSG_TASK_NOT_FOUND);
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
            throw new SystemException("保存审批人选择失败", e);
        }
    }

    @Override
    @Transactional
    public void selectFirstStageApproversWithSubWorkflows(Long instanceId, List<Long> approverIds, Map<Long, List<Long>> subWorkflowApproverIds) {
        ApprovalInstanceDO instance = approvalInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new NotFoundException(MSG_INSTANCE_NOT_FOUND);
        }

        // 获取第一阶段
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(instance.getWorkflowId());
        stageQuery.setOrderByField(COLUMN_STAGE_ORDER);
        stageQuery.setOrderByDirection(ORDER_ASC);
        List<WorkflowStageDO> stages = workflowStageMapper.selectList(stageQuery);
        WorkflowStageDO firstStage = stages.isEmpty() ? null : stages.get(0);
        if (firstStage == null) {
            throw new NotFoundException("未找到第一阶段");
        }

        // 保存子流程审批人选择到实例
        if (subWorkflowApproverIds != null && !subWorkflowApproverIds.isEmpty()) {
            try {
                String subWorkflowApproverIdsJson = objectMapper.writeValueAsString(subWorkflowApproverIds);
                instance.setSubWorkflowApproverIds(subWorkflowApproverIdsJson);
                approvalInstanceMapper.updateById(instance);
            } catch (Exception e) {
                throw new SystemException("保存子流程审批人选择失败", e);
            }
        }

        // 为选中的主流程审批人创建任务
        for (Long approverId : approverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(instanceId);
            task.setStageId(firstStage.getId());
            task.setApproverId(approverId);
            task.setStatus(STATUS_PENDING);
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
        ApprovalProgressQuery mainQuery = new ApprovalProgressQuery();
        mainQuery.setInstanceId(instanceId);
        mainQuery.setParentInstanceIdIsNull(true);
        List<ApprovalProgressDO> mainProgress = approvalProgressMapper.selectList(mainQuery);

        // 获取工作流的所有阶段
        List<WorkflowStageDO> allStages = getAllStagesForWorkflow(instance.getWorkflowId());

        // 为还没有到达的阶段创建"未开始"状态的进度记录
        List<ApprovalProgressDO> notStartedProgress = createNotStartedProgress(
            instanceId, allStages, mainProgress, 0, null, null
        );

        List<ApprovalProgressDO> allProgress = new ArrayList<>(mainProgress);
        allProgress.addAll(notStartedProgress);
        allProgress.addAll(getSubProgressForMainWorkflow(instanceId));

        return convertAndSortProgress(allProgress);
    }

    /**
     * 为未到达的阶段创建"未开始"状态的进度记录
     */
    private List<ApprovalProgressDO> createNotStartedProgress(
            Long instanceId,
            List<WorkflowStageDO> allStages,
            List<ApprovalProgressDO> existingProgress,
            int isSubWorkflow,
            Long parentInstanceId,
            Long parentTaskId) {

        Set<Long> existingStageIds = existingProgress.stream()
            .map(ApprovalProgressDO::getStageId)
            .collect(Collectors.toSet());

        List<ApprovalProgressDO> notStartedList = new ArrayList<>();
        for (WorkflowStageDO stage : allStages) {
            if (!existingStageIds.contains(stage.getId())) {
                notStartedList.add(createNotStartedProgressRecord(
                    instanceId, stage, isSubWorkflow, parentInstanceId, parentTaskId
                ));
            }
        }
        return notStartedList;
    }

    /**
     * 创建单个"未开始"状态的进度记录
     */
    private ApprovalProgressDO createNotStartedProgressRecord(
            Long instanceId,
            WorkflowStageDO stage,
            int isSubWorkflow,
            Long parentInstanceId,
            Long parentTaskId) {

        ApprovalProgressDO progress = new ApprovalProgressDO();
        progress.setId(null);
        progress.setInstanceId(instanceId);
        progress.setStageId(stage.getId());
        progress.setStageName(stage.getName());
        progress.setStageOrder(stage.getStageOrder());
        progress.setStatus(STATUS_NOT_STARTED);
        progress.setIsSubWorkflow(isSubWorkflow);
        progress.setParentInstanceId(parentInstanceId);
        progress.setParentTaskId(parentTaskId);
        progress.setApprovers(null);
        progress.setApproveTime(null);
        return progress;
    }

    /**
     * 获取子流程的进度（只返回子流程自己的进度）
     */
    private List<ApprovalProgressDTO> getSubWorkflowProgress(ApprovalInstanceDO subInstance) {
        Long instanceId = subInstance.getId();

        // 获取子流程已有的进度记录
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(instanceId);
        List<ApprovalProgressDO> existingProgress = approvalProgressMapper.selectList(query);

        // 获取子流程的所有阶段
        List<WorkflowStageDO> allStages = getAllStagesForWorkflow(subInstance.getWorkflowId());

        // 为还没有到达的阶段创建"未开始"状态的进度记录
        List<ApprovalProgressDO> notStartedProgress = createNotStartedProgress(
            instanceId, allStages, existingProgress, 1,
            subInstance.getParentInstanceId(),
            subInstance.getParentTaskId()
        );

        List<ApprovalProgressDO> allProgress = new ArrayList<>(existingProgress);
        allProgress.addAll(notStartedProgress);

        return convertAndSortProgress(allProgress);
    }

    @Override
    public WorkflowDTO getWorkflowByRole(Long roleId, String workflowType) {
        // Use WorkflowQuery to replace LambdaQueryWrapper
        WorkflowQuery query = new WorkflowQuery();
        query.setBoundRoleId(roleId);
        query.setWorkflowType(workflowType);
        query.setStatus(1);
        query.setDeleted(0);
        List<WorkflowDO> workflows = workflowMapper.selectList(query);
        WorkflowDO workflow = workflows.isEmpty() ? null : workflows.get(0);
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
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(workflowId);
        stageQuery.setOrderByField(COLUMN_STAGE_ORDER);
        stageQuery.setOrderByDirection(ORDER_ASC);
        List<WorkflowStageDO> stages = workflowStageMapper.selectList(stageQuery);

        List<WorkflowStageDTO> stageDTOs = new ArrayList<>();
        for (WorkflowStageDO stage : stages) {
            WorkflowStageDTO stageDTO = new WorkflowStageDTO();
            stageDTO.setId(stage.getId());
            stageDTO.setWorkflowId(stage.getWorkflowId());
            stageDTO.setName(stage.getName());
            stageDTO.setStageOrder(stage.getStageOrder());
            stageDTO.setApproveType(stage.getApproveType());

            // 加载该阶段的审批人
            StageApproverQuery approverQuery = new StageApproverQuery();
            approverQuery.setStageId(stage.getId());
            List<StageApproverDO> approvers = stageApproverMapper.selectList(approverQuery);

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
        if (APPROVER_TYPE_USER.equals(type)) {
            UserDO user = userMapper.selectById(id);
            return user != null ? "[用户] " + (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "[用户] 未知";
        } else if (APPROVER_TYPE_ROLE.equals(type)) {
            RoleDO role = roleMapper.selectById(id);
            return role != null ? "[角色] " + role.getName() : "[角色] 未知";
        } else if (APPROVER_TYPE_DEPT.equals(type)) {
            DeptDO dept = deptMapper.selectById(id);
            return dept != null ? "[部门] " + dept.getName() : "[部门] 未知";
        }
        return "未知";
    }

    @Override
    public FirstStageApproversDTO getFirstStageApprovers(Long workflowId, Long applicantId, String keyword) {
        WorkflowDO workflow = workflowMapper.selectById(workflowId);
        if (workflow == null) {
            return createEmptyFirstStageApproversDTO(workflowId);
        }

        FirstStageApproversDTO result = new FirstStageApproversDTO();
        result.setWorkflowId(workflow.getId());
        result.setWorkflowName(workflow.getName());

        WorkflowStageDO firstStage = getFirstStageOfWorkflow(workflowId);
        if (firstStage == null) {
            return createEmptyFirstStageApproversDTO(result);
        }

        populateFirstStageInfo(result, firstStage);
        populateApproverConfigs(result, firstStage.getId(), applicantId, keyword);

        return result;
    }

    /**
     * 创建空的FirstStageApproversDTO（流程不存在时）
     */
    private FirstStageApproversDTO createEmptyFirstStageApproversDTO(Long workflowId) {
        FirstStageApproversDTO result = new FirstStageApproversDTO();
        result.setWorkflowId(workflowId);
        result.setWorkflowName(null);
        result.setApproveType(null);
        result.setApproverConfigs(new ArrayList<>());
        result.setApproverCount(0);
        return result;
    }

    /**
     * 创建空的FirstStageApproversDTO（阶段不存在时，保留流程信息）
     */
    private FirstStageApproversDTO createEmptyFirstStageApproversDTO(FirstStageApproversDTO existing) {
        existing.setStageId(null);
        existing.setStageName(null);
        existing.setApproveType(null);
        existing.setApproverConfigs(new ArrayList<>());
        existing.setApproverCount(0);
        return existing;
    }

    /**
     * 获取流程的第一阶段
     */
    private WorkflowStageDO getFirstStageOfWorkflow(Long workflowId) {
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(workflowId);
        stageQuery.setOrderByField(COLUMN_STAGE_ORDER);
        stageQuery.setOrderByDirection(ORDER_ASC);
        List<WorkflowStageDO> stages = workflowStageMapper.selectList(stageQuery);
        return stages.isEmpty() ? null : stages.get(0);
    }

    /**
     * 填充第一阶段信息
     */
    private void populateFirstStageInfo(FirstStageApproversDTO result, WorkflowStageDO firstStage) {
        result.setStageId(firstStage.getId());
        result.setStageName(firstStage.getName());
        result.setApproveType(firstStage.getApproveType());
    }

    /**
     * 填充审批人配置
     */
    private void populateApproverConfigs(FirstStageApproversDTO result, Long stageId, Long applicantId, String keyword) {
        StageApproverQuery approverQuery = new StageApproverQuery();
        approverQuery.setStageId(stageId);
        approverQuery.setSubWorkflowIdNull(true);
        List<StageApproverWithDetailsDO> approverConfigs = stageApproverMapper.selectWithDetails(approverQuery);

        List<ApproverConfigDTO> configs = new ArrayList<>();
        for (StageApproverWithDetailsDO config : approverConfigs) {
            ApproverConfigDTO configDTO = buildApproverConfigDTO(config, applicantId, keyword);
            configs.add(configDTO);
        }

        result.setApproverConfigs(configs);
        result.setApproverCount(configs.size());
    }

    /**
     * 构建审批人配置DTO
     */
    private ApproverConfigDTO buildApproverConfigDTO(StageApproverWithDetailsDO config, Long applicantId, String keyword) {
        ApproverConfigDTO configDTO = new ApproverConfigDTO();
        configDTO.setConfigId(config.getId());
        configDTO.setApproverType(config.getApproverType());
        configDTO.setApproverId(config.getApproverId());
        configDTO.setCheckSecondaryDept(config.getCheckSecondaryDept());

        ApproverTypeInfo approverInfo = getApproverTypeInfo(config);
        configDTO.setApproverTypeName(approverInfo.typeName);
        configDTO.setApproverName(approverInfo.name);
        configDTO.setSubWorkflowId(approverInfo.subWorkflowId);

        List<ApproverSelectionDTO> availableUsers = getAvailableUsersForConfigOptimizedDTO(config, applicantId, keyword);
        configDTO.setAvailableUsers(availableUsers);

        return configDTO;
    }

    /**
     * 获取审批人类型信息（类型名称和名称）
     */
    private ApproverTypeInfo getApproverTypeInfo(StageApproverWithDetailsDO config) {
        ApproverTypeInfo info = new ApproverTypeInfo();
        info.subWorkflowId = null;

        switch (config.getApproverType()) {
            case APPROVER_TYPE_USER:
                info.typeName = "指定用户";
                info.name = config.getRealName() != null ? config.getRealName() : config.getUsername();
                break;
            case APPROVER_TYPE_ROLE:
                info.typeName = "指定角色";
                info.name = config.getRoleName();
                break;
            case APPROVER_TYPE_DEPT:
                info.typeName = "指定部门";
                info.name = config.getDeptName();
                break;
            case APPROVER_TYPE_SUB_WORKFLOW:
                info.typeName = "子流程";
                info.name = config.getSubWorkflowName() != null ? config.getSubWorkflowName() : "子流程";
                info.subWorkflowId = config.getSubWorkflowId();
                break;
            default:
                info.typeName = "";
                info.name = "";
        }

        return info;
    }

    /**
     * 审批人类型信息
     */
    private static class ApproverTypeInfo {
        String typeName;
        String name;
        Long subWorkflowId;
    }

    @Override
    @Transactional
    public void selectFirstStageApprovers(Long instanceId, List<Long> approverIds) {
        ApprovalInstanceDO instance = approvalInstanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new NotFoundException(MSG_INSTANCE_NOT_FOUND);
        }

        // 获取第一阶段
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(instance.getWorkflowId());
        stageQuery.setOrderByField(COLUMN_STAGE_ORDER);
        stageQuery.setOrderByDirection(ORDER_ASC);
        List<WorkflowStageDO> stages = workflowStageMapper.selectList(stageQuery);
        WorkflowStageDO firstStage = stages.isEmpty() ? null : stages.get(0);
        if (firstStage == null) {
            throw new NotFoundException("未找到第一阶段");
        }

        // 为选中的审批人创建任务
        for (Long approverId : approverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(instanceId);
            task.setStageId(firstStage.getId());
            task.setApproverId(approverId);
            task.setStatus(STATUS_PENDING);
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
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stageId);
        ApprovalProgressDO progress = approvalProgressMapper.selectOne(query);

        if (progress != null) {
            try {
                List<Map<String, Object>> approvers = new ArrayList<>();
                for (Long approverId : approverIds) {
                    UserDO user = userMapper.selectById(approverId);
                    if (user != null) {
                        Map<String, Object> approverInfo = new HashMap<>();
                        approverInfo.put("id", user.getId());
                        approverInfo.put("name", user.getRealName() != null ? user.getRealName() : user.getUsername());
                        approverInfo.put("status", STATUS_PENDING);
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

    /**
     * 转换并排序审批进度列表
     * 按阶段顺序排序，主流程阶段优先于子流程阶段
     */
    private List<ApprovalProgressDTO> convertAndSortProgress(List<ApprovalProgressDO> progressList) {
        return progressList.stream()
                .map(this::convertToProgressDTO)
                .sorted(Comparator
                        .comparingInt(ApprovalProgressDTO::getStageOrder)
                        .thenComparingInt(ApprovalProgressDTO::getIsSubWorkflow))
                .collect(Collectors.toList());
    }

    @Override
    public FirstStageApproversDTO getSubWorkflowFirstStageApprovers(Long subWorkflowId, Long applicantId, String keyword) {
        WorkflowDO workflow = workflowMapper.selectById(subWorkflowId);
        if (workflow == null) {
            return createEmptySubWorkflowApproversDTO(subWorkflowId);
        }

        FirstStageApproversDTO result = new FirstStageApproversDTO();
        result.setWorkflowId(workflow.getId());
        result.setWorkflowName(workflow.getName());

        WorkflowStageDO firstStage = getFirstStageOfWorkflow(subWorkflowId);
        if (firstStage == null) {
            return createEmptySubWorkflowApproversDTO(result);
        }

        result.setApproveType(firstStage.getApproveType());
        populateSubWorkflowApproverConfigs(result, firstStage.getId(), applicantId, keyword);

        return result;
    }

    /**
     * 创建空的子流程审批人DTO
     */
    private FirstStageApproversDTO createEmptySubWorkflowApproversDTO(Long workflowId) {
        FirstStageApproversDTO result = new FirstStageApproversDTO();
        result.setWorkflowId(workflowId);
        result.setWorkflowName(null);
        result.setApproverConfigs(new ArrayList<>());
        result.setApproverCount(0);
        return result;
    }

    /**
     * 创建空的子流程审批人DTO（保留流程信息）
     */
    private FirstStageApproversDTO createEmptySubWorkflowApproversDTO(FirstStageApproversDTO existing) {
        existing.setApproveType(null);
        existing.setApproverConfigs(new ArrayList<>());
        existing.setApproverCount(0);
        return existing;
    }

    /**
     * 填充子流程审批人配置
     */
    private void populateSubWorkflowApproverConfigs(FirstStageApproversDTO result, Long stageId, Long applicantId, String keyword) {
        StageApproverQuery approverQuery = new StageApproverQuery();
        approverQuery.setStageId(stageId);
        approverQuery.setSubWorkflowIdNull(true);
        List<StageApproverDO> approverConfigs = stageApproverMapper.selectList(approverQuery);

        List<ApproverConfigDTO> configs = new ArrayList<>();
        for (StageApproverDO config : approverConfigs) {
            ApproverConfigDTO configDTO = buildSubWorkflowApproverConfigDTO(config, applicantId, keyword);
            configs.add(configDTO);
        }

        result.setApproverConfigs(configs);
        result.setApproverCount(configs.size());
    }

    /**
     * 构建子流程审批人配置DTO
     */
    private ApproverConfigDTO buildSubWorkflowApproverConfigDTO(StageApproverDO config, Long applicantId, String keyword) {
        ApproverConfigDTO configDTO = new ApproverConfigDTO();
        configDTO.setConfigId(config.getId());
        configDTO.setApproverType(config.getApproverType());
        configDTO.setApproverId(config.getApproverId());
        configDTO.setCheckSecondaryDept(config.getCheckSecondaryDept());

        ApproverTypeBasicInfo info = getApproverTypeBasicInfo(config);
        configDTO.setApproverTypeName(info.typeName);
        configDTO.setApproverName(info.name);

        List<ApproverSelectionDTO> availableUsers = getAvailableUsersForConfigDTO(config, applicantId, keyword);
        configDTO.setAvailableUsers(availableUsers);

        return configDTO;
    }

    /**
     * 获取审批人类型基本信息（从StageApproverDO查询）
     */
    private ApproverTypeBasicInfo getApproverTypeBasicInfo(StageApproverDO config) {
        ApproverTypeBasicInfo info = new ApproverTypeBasicInfo();

        switch (config.getApproverType()) {
            case APPROVER_TYPE_USER:
                info.typeName = "指定用户";
                UserDO user = userMapper.selectById(config.getApproverId());
                info.name = user != null ? (user.getRealName() != null ? user.getRealName() : user.getUsername()) : "";
                break;
            case APPROVER_TYPE_ROLE:
                info.typeName = "指定角色";
                RoleDO role = roleMapper.selectById(config.getApproverId());
                info.name = role != null ? role.getName() : "";
                break;
            case APPROVER_TYPE_DEPT:
                info.typeName = "指定部门";
                DeptDO dept = deptMapper.selectById(config.getApproverId());
                info.name = dept != null ? dept.getName() : "";
                break;
            default:
                info.typeName = "";
                info.name = "";
        }

        return info;
    }

    /**
     * 审批人类型基本信息
     */
    private static class ApproverTypeBasicInfo {
        String typeName;
        String name;
    }

    /**
     * 获取指定配置的可用用户列表（返回 DTO 列表）
     */
    /**
     * 获取指定配置的可用用户列表（返回 DTO 列表）
     */
    private List<ApproverSelectionDTO> getAvailableUsersForConfigDTO(StageApproverDO config, Long applicantId, String keyword) {
        // 转换为优化版需要的StageApproverWithDetailsDO
        StageApproverWithDetailsDO detailsDO = new StageApproverWithDetailsDO();
        detailsDO.setApproverType(config.getApproverType());
        detailsDO.setApproverId(config.getApproverId());
        detailsDO.setCheckSecondaryDept(config.getCheckSecondaryDept());
        return getAvailableUsersForConfigOptimizedDTO(detailsDO, applicantId, keyword);
    }

    /**
     * 获取指定配置的可用用户列表（优化版，返回 DTO 列表）
     */
    private List<ApproverSelectionDTO> getAvailableUsersForConfigOptimizedDTO(StageApproverWithDetailsDO config, Long applicantId, String keyword) {
        switch (config.getApproverType()) {
            case APPROVER_TYPE_USER:
                return getUsersForUserType(config, keyword);
            case APPROVER_TYPE_ROLE:
                return getUsersForRoleType(config, applicantId, keyword);
            case APPROVER_TYPE_DEPT:
                return getUsersForDeptType(config, keyword);
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 获取指定类型的用户
     */
    private List<ApproverSelectionDTO> getUsersForUserType(StageApproverWithDetailsDO config, String keyword) {
        UserDO user = userMapper.selectById(config.getApproverId());
        if (user == null) {
            return new ArrayList<>();
        }

        if (!matchesKeyword(user, keyword)) {
            return new ArrayList<>();
        }

        return Collections.singletonList(convertUserToSelectionDTO(user));
    }

    /**
     * 获取角色类型的用户
     */
    private List<ApproverSelectionDTO> getUsersForRoleType(StageApproverWithDetailsDO config, Long applicantId, String keyword) {
        UserQuery userQuery = new UserQuery();
        userQuery.setRoleId(config.getApproverId());
        userQuery.setStatus(1);

        if (shouldCheckSecondaryDept(config)) {
            applySecondaryDeptFilter(userQuery, applicantId);
        }

        addKeywordFilterIfNeeded(userQuery, keyword);

        List<UserWithDetailsDO> roleUsers = userMapper.selectListWithDetails(userQuery);
        return convertUsersWithDetailsToSelectionDTO(roleUsers);
    }

    /**
     * 获取部门类型的用户
     */
    private List<ApproverSelectionDTO> getUsersForDeptType(StageApproverWithDetailsDO config, String keyword) {
        UserQuery userQuery = new UserQuery();
        userQuery.setDeptId(config.getApproverId());
        userQuery.setStatus(1);

        addKeywordFilterIfNeeded(userQuery, keyword);

        List<UserWithDetailsDO> deptUsers = userMapper.selectListWithDetails(userQuery);
        return convertUsersWithDetailsToSelectionDTO(deptUsers);
    }

    /**
     * 检查用户是否匹配关键词
     */
    private boolean matchesKeyword(UserDO user, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }

        String lowerKeyword = keyword.toLowerCase();
        return (user.getUsername() != null && user.getUsername().toLowerCase().contains(lowerKeyword)) ||
               (user.getRealName() != null && user.getRealName().toLowerCase().contains(lowerKeyword));
    }

    /**
     * 检查是否需要校验二级部门
     */
    private boolean shouldCheckSecondaryDept(StageApproverWithDetailsDO config) {
        return config.getCheckSecondaryDept() != null && config.getCheckSecondaryDept() == 1;
    }

    /**
     * 应用二级部门过滤条件
     */
    private void applySecondaryDeptFilter(UserQuery userQuery, Long applicantId) {
        Long applicantSecondaryDeptId = getSecondaryDeptId(applicantId);
        if (applicantSecondaryDeptId != null) {
            List<Long> deptIds = new ArrayList<>();
            deptIds.add(applicantSecondaryDeptId);
            deptIds.addAll(getAllSubDeptIds(applicantSecondaryDeptId));
            userQuery.setDeptIds(deptIds);
        }
    }

    /**
     * 添加关键词过滤条件（如果需要）
     */
    private void addKeywordFilterIfNeeded(UserQuery query, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setKeyword(keyword.trim());
        }
    }

    /**
     * 将 UserWithDetailsDO 列表转换为 ApproverSelectionDTO 列表
     */
    private List<ApproverSelectionDTO> convertUsersWithDetailsToSelectionDTO(List<UserWithDetailsDO> users) {
        List<ApproverSelectionDTO> result = new ArrayList<>();
        for (UserWithDetailsDO user : users) {
            result.add(convertUserWithDetailsToSelectionDTO(user));
        }
        return result;
    }

    /**
     * 将用户DO转换为ApproverSelectionDTO
     */
    private ApproverSelectionDTO convertUserToSelectionDTO(UserDO user) {
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
     * 将用户详情DO转换为ApproverSelectionDTO（直接从JOIN结果获取，无需额外查询）
     */
    private ApproverSelectionDTO convertUserWithDetailsToSelectionDTO(UserWithDetailsDO user) {
        ApproverSelectionDTO dto = new ApproverSelectionDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setDeptId(user.getDeptId());
        dto.setRoleId(user.getRoleId());
        // 直接从JOIN结果获取部门名称和角色名称，无需额外查询
        dto.setDeptName(user.getDeptName());
        dto.setRoleName(user.getRoleName());
        return dto;
    }

    /**
     * 获取指定配置的可用用户列表（优化版，使用JOIN查询，返回 Map）
     */
    private List<Map<String, Object>> getAvailableUsersForConfigOptimized(StageApproverWithDetailsDO config, Long applicantId, String keyword) {
        switch (config.getApproverType()) {
            case APPROVER_TYPE_USER:
                return getUsersMapForUserType(config, keyword);
            case APPROVER_TYPE_ROLE:
                return getUsersMapForRoleType(config, applicantId, keyword);
            case APPROVER_TYPE_DEPT:
                return getUsersMapForDeptType(config, keyword);
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 获取指定用户类型的用户列表（返回Map格式）
     */
    private List<Map<String, Object>> getUsersMapForUserType(StageApproverWithDetailsDO config, String keyword) {
        List<Map<String, Object>> users = new ArrayList<>();
        UserDO user = userMapper.selectById(config.getApproverId());
        if (user != null && matchesKeyword(user, keyword)) {
            users.add(convertUserToMapWithDetails(user));
        }
        return users;
    }

    /**
     * 获取指定角色类型的用户列表（返回Map格式）
     */
    private List<Map<String, Object>> getUsersMapForRoleType(StageApproverWithDetailsDO config, Long applicantId, String keyword) {
        UserQuery userQuery = new UserQuery();
        userQuery.setRoleId(config.getApproverId());
        userQuery.setStatus(1);

        if (shouldCheckSecondaryDept(config)) {
            applySecondaryDeptFilter(userQuery, applicantId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            userQuery.setKeyword(keyword.trim());
        }

        List<UserWithDetailsDO> roleUsers = userMapper.selectListWithDetails(userQuery);
        return convertUserWithDetailsListToMap(roleUsers);
    }

    /**
     * 获取指定部门类型的用户列表（返回Map格式）
     */
    private List<Map<String, Object>> getUsersMapForDeptType(StageApproverWithDetailsDO config, String keyword) {
        UserQuery userQuery = new UserQuery();
        userQuery.setDeptId(config.getApproverId());
        userQuery.setStatus(1);

        if (keyword != null && !keyword.trim().isEmpty()) {
            userQuery.setKeyword(keyword.trim());
        }

        List<UserWithDetailsDO> deptUsers = userMapper.selectListWithDetails(userQuery);
        return convertUserWithDetailsListToMap(deptUsers);
    }

    /**
     * 将UserWithDetailsDO列表转换为Map列表
     */
    private List<Map<String, Object>> convertUserWithDetailsListToMap(List<UserWithDetailsDO> users) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserWithDetailsDO user : users) {
            result.add(convertUserWithDetailsToMap(user));
        }
        return result;
    }

    /**
     * 将用户DO转换为Map（使用JOIN查询结果，无需额外查询）
     */
    private Map<String, Object> convertUserToMapWithDetails(UserDO user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("deptId", user.getDeptId());
        map.put("roleId", user.getRoleId());

        // 使用JOIN查询结果或单独查询
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

    /**
     * 将用户详情DO转换为Map（直接从JOIN结果获取，无需额外查询）
     */
    private Map<String, Object> convertUserWithDetailsToMap(UserWithDetailsDO user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("realName", user.getRealName());
        map.put("deptId", user.getDeptId());
        map.put("roleId", user.getRoleId());
        // 直接从JOIN结果获取部门名称和角色名称，无需额外查询
        map.put("deptName", user.getDeptName());
        map.put("roleName", user.getRoleName());
        return map;
    }

    @Override
    @Transactional
    public void selectSubWorkflowFirstStageApprovers(Long subInstanceId, List<Long> approverIds) {
        ApprovalInstanceDO subInstance = approvalInstanceMapper.selectById(subInstanceId);
        if (subInstance == null) {
            throw new NotFoundException("子流程实例不存在");
        }

        // 获取子流程第一阶段
        WorkflowStageQuery firstStageQuery = new WorkflowStageQuery();
        firstStageQuery.setWorkflowId(subInstance.getWorkflowId());
        firstStageQuery.setOrderByField(COLUMN_STAGE_ORDER);
        firstStageQuery.setOrderByDirection(ORDER_ASC);
        List<WorkflowStageDO> firstStageList = workflowStageMapper.selectList(firstStageQuery);
        WorkflowStageDO firstStage = firstStageList.isEmpty() ? null : firstStageList.get(0);
        if (firstStage == null) {
            throw new NotFoundException("未找到子流程第一阶段");
        }

        // 为选中的审批人创建任务
        for (Long approverId : approverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(subInstanceId);
            task.setStageId(firstStage.getId());
            task.setApproverId(approverId);
            task.setStatus(STATUS_PENDING);
            task.setIsFirstApprover(0);
            task.setSelectedByUserId(subInstance.getApplicantId()); // 由发起人（或上一层审批人）选择
            approvalTaskMapper.insert(task);
        }

        // 更新进度记录，添加审批人信息
        updateProgressRecordWithApprovers(subInstanceId, firstStage.getId(), approverIds);
    }

    /**
     * 获取工作流的所有阶段
     */
    private List<WorkflowStageDO> getAllStagesForWorkflow(Long workflowId) {
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(workflowId);
        stageQuery.setOrderByField(COLUMN_STAGE_ORDER);
        stageQuery.setOrderByDirection(ORDER_ASC);
        return workflowStageMapper.selectList(stageQuery);
    }

    /**
     * 获取主流程的所有子流程进度
     */
    private List<ApprovalProgressDO> getSubProgressForMainWorkflow(Long instanceId) {
        // 获取所有子流程实例
        ApprovalInstanceQuery subInstanceQuery = new ApprovalInstanceQuery();
        subInstanceQuery.setParentInstanceId(instanceId);
        List<ApprovalInstanceDO> subInstances = approvalInstanceMapper.selectList(subInstanceQuery);

        if (subInstances.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有子流程的进度记录
        List<Long> subInstanceIds = subInstances.stream()
                .map(ApprovalInstanceDO::getId)
                .collect(Collectors.toList());

        ApprovalProgressQuery subProgressQuery = new ApprovalProgressQuery();
        subProgressQuery.setInstanceIds(subInstanceIds);
        return approvalProgressMapper.selectList(subProgressQuery);
    }
}
