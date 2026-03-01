package com.xuanjiao.app.workflow.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.app.workflow.handler.WorkflowCompletionHandler;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import com.xuanjiao.infrastructure.dataobject.ApprovalProgressDO;
import com.xuanjiao.infrastructure.dataobject.ApprovalTaskDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverQuery;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceQuery;
import com.xuanjiao.infrastructure.approval.ApprovalTaskMapper;
import com.xuanjiao.infrastructure.approval.ApprovalTaskQuery;
import com.xuanjiao.infrastructure.approval.ApprovalProgressMapper;
import com.xuanjiao.infrastructure.approval.ApprovalProgressQuery;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.common.exception.BusinessException;
import com.xuanjiao.common.exception.NotFoundException;
import com.xuanjiao.common.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作流引擎服务实现类
 * <p>实现WorkflowEngineService接口，封装工作流核心引擎逻辑</p>
 * <p>核心功能：流程启动、任务完成、子流程管理、流程推进</p>
 * <p>设计特点：支持每层通过后选择下一层审批人、支持子流程</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.workflow.WorkflowEngineService
 */
@Service
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineServiceImpl.class);

    /** 消息常量 */
    private static final String MSG_INSTANCE_NOT_FOUND = "审批实例不存在";
    private static final String MSG_TASK_NOT_FOUND = "任务不存在";

    @Resource
    private WorkflowMapper workflowMapper;
    @Resource
    private WorkflowStageMapper stageMapper;
    @Resource
    private StageApproverMapper approverMapper;
    @Resource
    private ApprovalInstanceMapper instanceMapper;
    @Resource
    private ApprovalTaskMapper taskMapper;
    @Resource
    private ApprovalProgressMapper progressMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private ApproverSelectionService approverSelectionService;
    @Resource
    private List<WorkflowCompletionHandler> completionHandlers;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 业务类型到处理器的映射表
    private Map<String, WorkflowCompletionHandler> handlerMap;

    /**
     * 初始化处理器映射表
     * Spring 自动注入所有 WorkflowCompletionHandler 实现类
     */
    @PostConstruct
    public void initHandlers() {
        handlerMap = completionHandlers.stream()
            .collect(Collectors.toMap(
                WorkflowCompletionHandler::getSupportedBusinessType,
                handler -> handler
            ));

        logger.info("已注册 {} 个审批完成处理器: {}",
            handlerMap.size(),
            handlerMap.keySet());
    }

    @Override
    @Transactional
    public Long startProcess(Long workflowId, String businessType, Long businessId, Long applicantId) {
        // 检查流程是否存在且已启用
        WorkflowDO workflow = workflowMapper.selectById(workflowId);
        if (workflow == null) {
            throw new NotFoundException("流程不存在，ID: " + workflowId);
        }
        if (workflow.getStatus() == null || workflow.getStatus() != 1) {
            throw new BusinessException("流程已禁用，无法创建审批实例。流程名称：《" + workflow.getName() + "》");
        }

        // 创建审批实例
        ApprovalInstanceDO instance = new ApprovalInstanceDO();
        instance.setWorkflowId(workflowId);
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setApplicantId(applicantId);
        instance.setStatus("PENDING");
        instance.setRootInstanceId(null); // 主流程的rootInstanceId为null
        instance.setParentInstanceId(null);
        instance.setParentTaskId(null);
        instanceMapper.insert(instance);

        // 获取第一个阶段
        WorkflowStageDO firstStage = getFirstStage(workflowId);
        if (firstStage != null) {
            instance.setCurrentStageId(firstStage.getId());
            instanceMapper.updateById(instance);

            // 创建进度记录
            createProgressRecord(instance.getId(), firstStage, null, "PENDING");

            // 注意：按照新需求，发起人需要选择第一层审批人
            // 不在这里创建任务，等待发起人选择
        }

        return instance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId, Long userId, boolean approved, String comment) {
        logger.info("开始完成任务: taskId={}, userId={}, approved={}, comment={}", taskId, userId, approved, comment);

        ApprovalTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            logger.error("任务不存在: taskId={}", taskId);
            throw new NotFoundException(MSG_TASK_NOT_FOUND);
        }
        if (!task.getApproverId().equals(userId)) {
            logger.error("无权操作: taskId={}, taskApproverId={}, userId={}", taskId, task.getApproverId(), userId);
            throw new BusinessException("无权操作此任务");
        }
        // 检查任务状态：只有 PENDING 状态的任务才能被审批
        if (!"PENDING".equals(task.getStatus())) {
            logger.error("任务状态不允许审批: taskId={}, status={}", taskId, task.getStatus());
            throw new BusinessException("任务状态不允许审批，当前状态：" + task.getStatus());
        }

        // 更新任务状态
        task.setStatus(approved ? "APPROVED" : "REJECTED");
        task.setComment(comment);
        task.setApproveTime(LocalDateTime.now());
        taskMapper.updateById(task);
        logger.info("任务状态已更新: taskId={}, status={}", taskId, task.getStatus());

        if (!approved) {
            // 驳回：更新实例状态
            ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());
            if (instance != null) {
                instance.setStatus("REJECTED");
                instanceMapper.updateById(instance);
                logger.info("实例状态已更新为REJECTED: instanceId={}", instance.getId());

                // 获取根实例ID：主流程的rootInstanceId为null，使用当前实例ID；子流程使用rootInstanceId
                Long rootInstanceId = instance.getRootInstanceId() != null ? instance.getRootInstanceId() : instance.getId();

                // 如果是子流程驳回，需要同时将主流程（父实例）的状态也设置为REJECTED
                if (instance.getRootInstanceId() != null) {
                    ApprovalInstanceDO parentInstance = instanceMapper.selectById(instance.getRootInstanceId());
                    if (parentInstance != null && !"REJECTED".equals(parentInstance.getStatus())) {
                        parentInstance.setStatus("REJECTED");
                        instanceMapper.updateById(parentInstance);
                        logger.info("子流程驳回，主流程状态已更新为REJECTED: parentInstanceId={}", parentInstance.getId());
                    }
                }

                // 取消所有待办任务（包括主流程和所有子流程）
                cancelAllPendingTasksForInstance(rootInstanceId);

                // 更新进度记录
                updateProgressRecord(task.getInstanceId(), task.getStageId(), "REJECTED", userId, comment);

                // 更新业务数据状态（使用主流程的业务数据）
                ApprovalInstanceDO businessInstance = instance.getRootInstanceId() != null
                    ? instanceMapper.selectById(instance.getRootInstanceId())
                    : instance;
                if (businessInstance != null) {
                    handleWorkflowRejection(businessInstance.getBusinessType(), businessInstance.getBusinessId());
                }
            }
            return;
        }

        // 审批通过：先更新当前审批人的状态（会签时，每个人完成后都要更新）
        updateApproverStatusInProgress(task.getInstanceId(), task.getStageId(), userId, "APPROVED", comment);

        // AND签：标记第一个审批通过的人为 is_first_approver=1
        markFirstApproverInAndSign(task);

        // 检查当前阶段是否完成
        logger.info("检查阶段是否完成: instanceId={}, stageId={}", task.getInstanceId(), task.getStageId());
        checkAndMoveToNextStage(task.getInstanceId(), task.getStageId());
        logger.info("完成任务处理完成: taskId={}", taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnTask(Long taskId, Long userId, String comment) {
        logger.info("开始退回上一级: taskId={}, userId={}, comment={}", taskId, userId, comment);

        // 1. 获取任务信息
        ApprovalTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            logger.error("任务不存在: taskId={}", taskId);
            throw new NotFoundException(MSG_TASK_NOT_FOUND);
        }
        if (!task.getApproverId().equals(userId)) {
            logger.error("无权操作: taskId={}, taskApproverId={}, userId={}", taskId, task.getApproverId(), userId);
            throw new BusinessException("无权操作此任务");
        }
        if (!"PENDING".equals(task.getStatus())) {
            logger.error("任务状态不允许退回: taskId={}, status={}", taskId, task.getStatus());
            throw new BusinessException("只有待审批任务才能退回");
        }

        // 2. 获取实例和阶段信息
        ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());
        if (instance == null) {
            throw new NotFoundException(MSG_INSTANCE_NOT_FOUND);
        }

        // 检查是否为子流程任务
        if (instance.getParentInstanceId() != null) {
            // 子流程退回：子流程第一层退回到主流程上一层
            logger.info("子流程退回: instanceId={}, parentInstanceId={}", instance.getId(), instance.getParentInstanceId());
            handleSubWorkflowReturn(task, instance, comment);
            return;
        }

        // 主流程退回
        WorkflowStageDO currentStage = stageMapper.selectById(task.getStageId());
        if (currentStage == null) {
            throw new NotFoundException("当前阶段不存在");
        }

        // 3. 判断是否为第一层
        if (currentStage.getStageOrder() == 1) {
            // 第一层退回 → 退给发起人，复用驳回逻辑
            logger.info("第一层退回，执行驳回逻辑: taskId={}", taskId);
            completeTask(taskId, userId, false, comment);
            return;
        }

        // 4. 非第一层退回上一层
        handleReturnToPreviousStage(task, instance, currentStage, comment);
    }

    /**
     * 处理主流程退回到上一层
     */
    private void handleReturnToPreviousStage(ApprovalTaskDO task, ApprovalInstanceDO instance,
                                              WorkflowStageDO currentStage, String comment) {
        logger.info("处理主流程退回上一层: instanceId={}, currentStageOrder={}",
            instance.getId(), currentStage.getStageOrder());

        // 4.1 标记当前任务为 CANCELLED
        task.setStatus("CANCELLED");
        task.setComment(comment);
        taskMapper.updateById(task);

        // 4.2 取消当前层的其他待办任务（同层其他审批人）
        cancelPendingTasks(instance.getId(), task.getStageId());

        // 4.2.1 取消当前层（被退回层）的所有已通过任务
        // 这样确保上层重新选择时，不会跳过被退回的层
        cancelApprovedTasksInStage(instance.getId(), task.getStageId());

        // 4.3 取消之后所有层的待办任务
        cancelAllSubsequentTasks(instance.getId(), currentStage.getStageOrder());

        // 4.4 取消当前层的所有子流程待办任务
        cancelSubWorkflowTasksForStage(instance.getId(), task.getStageId());

        // 4.5 查找上一层（stage_order - 1）
        WorkflowStageQuery prevStageQuery = new WorkflowStageQuery();
        prevStageQuery.setWorkflowId(instance.getWorkflowId());
        prevStageQuery.setStageOrder(currentStage.getStageOrder() - 1);
        List<WorkflowStageDO> prevStageList = stageMapper.selectList(prevStageQuery);
        WorkflowStageDO previousStage = prevStageList.isEmpty() ? null : prevStageList.get(0);

        if (previousStage == null) {
            throw new NotFoundException("上一层不存在，无法退回");
        }

        logger.info("找到上一层: previousStageId={}, previousStageOrder={}, previousStageName={}",
            previousStage.getId(), previousStage.getStageOrder(), previousStage.getName());

        // 4.6 更新实例的 currentStageId 为上一层
        instance.setCurrentStageId(previousStage.getId());
        instanceMapper.updateById(instance);

        // 4.7 更新审批进度：当前层 → RETURNED，上一层 → 重置为 PENDING
        updateProgressRecord(instance.getId(), currentStage.getId(), "RETURNED", task.getApproverId(), comment);
        resetProgressRecordForStage(instance.getId(), previousStage.getId());

        // 4.8 重置上一层的所有任务状态为 PENDING（恢复到刚接到任务时的状态）
        resetPreviousStageTasks(instance.getId(), previousStage.getId());

        // 4.9 重新创建上一层的子流程（因为退回时已取消）
        recreateSubWorkflowForStage(instance.getId(), previousStage, instance);

        logger.info("主流程退回上一层完成: instanceId={}, previousStageId={}", instance.getId(), previousStage.getId());
    }

    /**
     * 重新创建指定层的子流程（退回时调用）
     */
    private void recreateSubWorkflowForStage(Long instanceId, WorkflowStageDO stage, ApprovalInstanceDO instance) {
        Map<Long, List<Long>> subWorkflowApproverIds = null;

        if (stage.getStageOrder() == 1) {
            // 第一层：从实例获取子流程审批人选择
            // parentTaskId 为 null，因为发起时启动子流程还没有审批人完成
            if (instance.getSubWorkflowApproverIds() != null) {
                try {
                    subWorkflowApproverIds = objectMapper.readValue(
                        instance.getSubWorkflowApproverIds(),
                        new TypeReference<Map<Long, List<Long>>>() {}
                    );
                } catch (Exception e) {
                    logger.error("解析子流程审批人选择失败: {}", e.getMessage(), e);
                }
            }
        } else {
            // 非第一层：从该层的任务获取子流程审批人选择
            ApprovalTaskQuery taskQuery = new ApprovalTaskQuery();
            taskQuery.setInstanceId(instanceId);
            taskQuery.setStageId(stage.getId());
            taskQuery.setSubWorkflowApproverIdsNotNull(true);
            ApprovalTaskDO taskWithSubWorkflow = taskMapper.selectOne(taskQuery);

            if (taskWithSubWorkflow != null && taskWithSubWorkflow.getSubWorkflowApproverIds() != null) {
                try {
                    subWorkflowApproverIds = objectMapper.readValue(
                        taskWithSubWorkflow.getSubWorkflowApproverIds(),
                        new TypeReference<Map<Long, List<Long>>>() {}
                    );
                } catch (Exception e) {
                    logger.error("解析子流程审批人选择失败: {}", e.getMessage(), e);
                }
            }
        }

        // 创建子流程
        if (subWorkflowApproverIds != null && !subWorkflowApproverIds.isEmpty()) {
            logger.info("退回到第{}层，重新创建子流程: instanceId={}, subWorkflowApproverIds={}",
                stage.getStageOrder(), instanceId, subWorkflowApproverIds);
            startSubProcessesForStage(instanceId, stage.getId(), null, subWorkflowApproverIds);
        }
    }

    /**
     * 处理子流程退回
     * 第一层退回：创建"重新发起子流程"任务
     * 其他层退回：触发主流程退回上一层
     */
    private void handleSubWorkflowReturn(ApprovalTaskDO task, ApprovalInstanceDO subInstance, String comment) {
        logger.info("处理子流程退回: subInstanceId={}, taskId={}", subInstance.getId(), task.getId());

        // 判断是否是第一层退回
        WorkflowStageDO currentStage = stageMapper.selectById(task.getStageId());
        if (currentStage == null) {
            throw new NotFoundException("当前阶段不存在，无法处理退回");
        }

        if (currentStage.getStageOrder() == 1) {
            // 第一层退回：创建"重新发起子流程"任务
            handleFirstLayerSubWorkflowReturn(task, subInstance, comment);
        } else {
            // 其他层退回：触发主流程退回上一层
            handleSubWorkflowMiddleLayerReturn(task, subInstance, currentStage, comment);
        }
    }

    /**
     * 处理子流程第一层退回：创建"重新发起子流程"任务
     * 根据 parentTaskId 判断子流程属于哪一层，从而决定给谁创建重新发起任务
     */
    private void handleFirstLayerSubWorkflowReturn(ApprovalTaskDO task, ApprovalInstanceDO subInstance, String comment) {
        logger.info("子流程第一层退回，创建重新发起任务: subInstanceId={}, parentTaskId={}",
            subInstance.getId(), subInstance.getParentTaskId());

        // 1. 子流程实例状态 → CANCELLED（废弃）
        subInstance.setStatus("CANCELLED");
        instanceMapper.updateById(subInstance);

        // 2. 取消子流程所有待办任务
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(subInstance.getId());
        query.setStatus("PENDING");
        List<ApprovalTaskDO> pendingTasks = taskMapper.selectList(query);
        for (ApprovalTaskDO t : pendingTasks) {
            t.setStatus("CANCELLED");
            t.setComment(comment);
            taskMapper.updateById(t);
        }
        logger.info("已取消子流程待办任务: count={}", pendingTasks.size());

        // 3. 获取主流程实例
        Long parentInstanceId = subInstance.getRootInstanceId();
        ApprovalInstanceDO parentInstance = instanceMapper.selectById(parentInstanceId);
        if (parentInstance == null) {
            throw new NotFoundException("主流程实例不存在，无法创建重新发起任务");
        }

        Long parentTaskId = subInstance.getParentTaskId();

        // 4. 根据 parentTaskId 判断给谁创建重新发起任务
        if (parentTaskId == null) {
            // 子流程属于主流程第一层（发起时启动）：给发起人创建任务
            logger.info("子流程属于主流程第一层，给发起人创建重新发起任务");
            createRestartSubWorkflowTaskForApplicant(parentInstance, subInstance.getWorkflowId(), comment);
        } else {
            // 子流程属于主流程非第一层（上一层审批通过后启动）：给上一层第一个审批人创建任务
            logger.info("子流程属于主流程非第一层，给上一层审批人创建重新发起任务");
            ApprovalTaskDO parentTask = taskMapper.selectById(parentTaskId);
            if (parentTask == null) {
                throw new NotFoundException("父任务不存在，无法创建重新发起任务");
            }
            createRestartSubWorkflowTask(parentInstance, parentTask, subInstance.getWorkflowId(), comment);
        }

        logger.info("已创建重新发起子流程任务: parentInstanceId={}, parentTaskId={}, subWorkflowId={}",
            parentInstance.getId(), parentTaskId, subInstance.getWorkflowId());
    }

    /**
     * 创建"重新发起子流程"任务
     */
    private void createRestartSubWorkflowTask(ApprovalInstanceDO parentInstance, ApprovalTaskDO parentTask,
                                                Long subWorkflowId, String comment) {
        // 查找选择子流程的审批人（parentTask 的 approverId）
        Long approverId = parentTask.getSelectedByUserId();
        if (approverId == null) {
            // 如果没有记录，使用 parentTask 的 approverId
            approverId = parentTask.getApproverId();
        }

        // 创建"重新发起子流程"任务
        ApprovalTaskDO restartTask = new ApprovalTaskDO();
        restartTask.setInstanceId(parentInstance.getId());
        // RESTART_SUB_WORKFLOW 任务不属于任何阶段，设为 0
        restartTask.setStageId(0L);
        restartTask.setApproverId(approverId);
        restartTask.setStatus("PENDING");
        restartTask.setTaskType("RESTART_SUB_WORKFLOW");
        restartTask.setIsFirstApprover(0);
        // 保存子流程ID，用于前端显示哪个子流程需要重新发起
        try {
            Map<String, Long> subWorkflowInfo = new HashMap<>();
            subWorkflowInfo.put("subWorkflowId", subWorkflowId);
            subWorkflowInfo.put("originalParentTaskId", parentTask.getId());
            restartTask.setSubWorkflowApproverIds(objectMapper.writeValueAsString(subWorkflowInfo));
        } catch (Exception e) {
            logger.warn("保存子流程信息失败: {}", e.getMessage());
        }
        taskMapper.insert(restartTask);

        logger.info("已创建重新发起子流程任务: taskId={}, approverId={}, subWorkflowId={}",
            restartTask.getId(), approverId, subWorkflowId);
    }

    /**
     * 创建"重新发起子流程"任务（给发起人）
     * 用于子流程属于主流程第一层时退回的场景
     */
    private void createRestartSubWorkflowTaskForApplicant(ApprovalInstanceDO parentInstance, Long subWorkflowId, String comment) {
        // 给发起人创建"重新发起子流程"任务
        Long applicantId = parentInstance.getApplicantId();

        // 创建"重新发起子流程"任务，分配给发起人
        ApprovalTaskDO restartTask = new ApprovalTaskDO();
        restartTask.setInstanceId(parentInstance.getId());
        // RESTART_SUB_WORKFLOW 任务不属于任何阶段，设为 0
        restartTask.setStageId(0L);
        restartTask.setApproverId(applicantId);
        restartTask.setStatus("PENDING");
        restartTask.setTaskType("RESTART_SUB_WORKFLOW");
        restartTask.setIsFirstApprover(0);
        restartTask.setSelectedByUserId(applicantId);
        // 保存子流程ID，用于前端显示哪个子流程需要重新发起
        try {
            Map<String, Long> subWorkflowInfo = new HashMap<>();
            subWorkflowInfo.put("subWorkflowId", subWorkflowId);
            subWorkflowInfo.put("isForApplicant", 1L); // 标记这是给发起人的任务
            restartTask.setSubWorkflowApproverIds(objectMapper.writeValueAsString(subWorkflowInfo));
        } catch (Exception e) {
            logger.warn("保存子流程信息失败: {}", e.getMessage());
        }
        taskMapper.insert(restartTask);

        logger.info("已创建重新发起子流程任务（给发起人）: taskId={}, applicantId={}, subWorkflowId={}",
            restartTask.getId(), applicantId, subWorkflowId);
    }

    /**
     * 处理子流程中间层退回：在子流程内部退回到上一层，不影响主流程
     * 复用主流程的退回逻辑，保持与主流程退回一致的行为
     */
    private void handleSubWorkflowMiddleLayerReturn(ApprovalTaskDO task, ApprovalInstanceDO subInstance,
                                                      WorkflowStageDO currentStage, String comment) {
        logger.info("子流程中间层退回，在子流程内部退回到上一层: subInstanceId={}, currentStageOrder={}",
            subInstance.getId(), currentStage.getStageOrder());

        // 直接在子流程内部退回到上一层，复用主流程的退回逻辑
        // 子流程独立处理，不影响主流程的进度
        handleReturnToPreviousStage(task, subInstance, currentStage, comment);

        logger.info("子流程退回上一层完成: subInstanceId={}", subInstance.getId());
    }

    /**
     * 取消指定层之后的所有待办任务
     */
    private void cancelAllSubsequentTasks(Long instanceId, int currentStageOrder) {
        // 查询当前层之后所有阶段的待办任务
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(instanceId);
        query.setStatus("PENDING");
        List<ApprovalTaskDO> allPendingTasks = taskMapper.selectList(query);
        for (ApprovalTaskDO t : allPendingTasks) {
            WorkflowStageDO stage = stageMapper.selectById(t.getStageId());
            if (stage != null && stage.getStageOrder() > currentStageOrder) {
                t.setStatus("CANCELLED");
                taskMapper.updateById(t);
            }
        }
        logger.info("已取消后续层待办任务: instanceId={}, currentStageOrder={}, count={}",
            instanceId, currentStageOrder, allPendingTasks.stream()
                .filter(t -> {
                    WorkflowStageDO s = stageMapper.selectById(t.getStageId());
                    return s != null && s.getStageOrder() > currentStageOrder;
                }).count());
    }

    /**
     * 取消指定层及前一层的所有子流程（包括已流转到其他层或已完成的）
     * N层退回时，需要取消N层和N-1层的子流程，因为会重新创建
     * @param instanceId 主流程实例ID
     * @param currentStageId 当前被退回的层ID
     */
    private void cancelSubWorkflowTasksForStage(Long instanceId, Long currentStageId) {
        // 获取当前层和前一层的所有任务ID
        List<Long> taskIdsToCancel = new ArrayList<>();

        // 1. 获取当前层的所有任务
        ApprovalTaskQuery currentTasksQuery = new ApprovalTaskQuery();
        currentTasksQuery.setInstanceId(instanceId);
        currentTasksQuery.setStageId(currentStageId);
        List<ApprovalTaskDO> currentTasks = taskMapper.selectList(currentTasksQuery);
        List<Long> currentTaskIds = currentTasks.stream()
            .map(ApprovalTaskDO::getId)
            .collect(Collectors.toList());
        taskIdsToCancel.addAll(currentTaskIds);

        // 2. 获取前一层的所有任务
        WorkflowStageDO currentStage = stageMapper.selectById(currentStageId);
        if (currentStage != null && currentStage.getStageOrder() > 1) {
            // 查询主流程实例以获取workflow_id
            ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
            if (instance != null) {
                WorkflowStageQuery prevStageQuery = new WorkflowStageQuery();
                prevStageQuery.setWorkflowId(instance.getWorkflowId());
                prevStageQuery.setStageOrder(currentStage.getStageOrder() - 1);
                List<WorkflowStageDO> prevStageList = stageMapper.selectList(prevStageQuery);
                WorkflowStageDO previousStage = prevStageList.isEmpty() ? null : prevStageList.get(0);

                if (previousStage != null) {
                    ApprovalTaskQuery prevTasksQuery = new ApprovalTaskQuery();
                    prevTasksQuery.setInstanceId(instanceId);
                    prevTasksQuery.setStageId(previousStage.getId());
                    List<ApprovalTaskDO> prevTasks = taskMapper.selectList(prevTasksQuery);
                    List<Long> prevTaskIds = prevTasks.stream()
                        .map(ApprovalTaskDO::getId)
                        .collect(Collectors.toList());
                    taskIdsToCancel.addAll(prevTaskIds);
                }
            }
        }

        if (taskIdsToCancel.isEmpty()) {
            logger.info("没有找到需要取消子流程的任务: instanceId={}, currentStageId={}", instanceId, currentStageId);
            return;
        }

        logger.info("退回时取消子流程: instanceId={}, currentStageId={}, 取消任务IDs={}",
            instanceId, currentStageId, taskIdsToCancel);

        // 3. 查询由这些任务触发的所有子流程实例（通过 parentTaskId 匹配）
        // 只有退回到第一层时，才取消 parent_task_id IS NULL 的子流程（发起时创建的）
        boolean includeNullParentTask = (currentStage != null && currentStage.getStageOrder() == 2);
        List<ApprovalInstanceDO> subInstances = instanceMapper.selectSubInstancesToCancel(
            instanceId, taskIdsToCancel, includeNullParentTask);

        logger.info("找到需要取消的子流程实例数量: {}", subInstances.size());

        // 4. 取消所有子流程实例及其所有任务（无论状态如何）
        for (ApprovalInstanceDO subInstance : subInstances) {
            // 子流程实例设置为CANCELLED
            subInstance.setStatus("CANCELLED");
            instanceMapper.updateById(subInstance);

            // 取消子流程的所有任务（所有状态）
            ApprovalTaskQuery allSubTaskQuery = new ApprovalTaskQuery();
            allSubTaskQuery.setInstanceId(subInstance.getId());
            List<ApprovalTaskDO> allSubTasks = taskMapper.selectList(allSubTaskQuery);
            int cancelledCount = 0;
            for (ApprovalTaskDO t : allSubTasks) {
                if ("PENDING".equals(t.getStatus()) || "APPROVED".equals(t.getStatus())) {
                    t.setStatus("CANCELLED");
                    taskMapper.updateById(t);
                    cancelledCount++;
                }
                // REJECTED/CANCELLED状态的任务保持不变
            }
            logger.info("已取消子流程实例及其任务: subInstanceId={}, parentTaskId={}, 总任务数={}, 取消任务数={}",
                subInstance.getId(), subInstance.getParentTaskId(), allSubTasks.size(), cancelledCount);
        }

        logger.info("取消子流程完成: instanceId={}, currentStageId={}, cancelledSubInstanceCount={}",
            instanceId, currentStageId, subInstances.size());
    }

    /**
     * 重置指定阶段的进度记录为 PENDING（清空审批人信息）
     */
    private void resetProgressRecordForStage(Long instanceId, Long stageId) {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stageId);
        ApprovalProgressDO progress = progressMapper.selectOne(query);

        if (progress != null) {
            // 使用 XML Mapper 显式更新，将字段设置为 null
            progressMapper.resetForResubmit(progress.getId());
            logger.info("已重置进度记录: instanceId={}, stageId={}", instanceId, stageId);
        }
    }

    /**
     * 发起人选择第一层审批人（包括主流程审批人和子流程第一层审批人）
     * @param instanceId 实例ID
     * @param approverIds 主流程第一层审批人ID列表
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表）
     */
    @Transactional
    public void selectFirstStageApprovers(Long instanceId, List<Long> approverIds, Map<Long, List<Long>> subWorkflowApproverIds) {
        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new NotFoundException(MSG_INSTANCE_NOT_FOUND);
        }

        WorkflowStageDO firstStage = getFirstStage(instance.getWorkflowId());
        if (firstStage == null) {
            throw new NotFoundException("未找到第一阶段");
        }

        // 保存子流程审批人选择到实例
        if (subWorkflowApproverIds != null && !subWorkflowApproverIds.isEmpty()) {
            try {
                String subWorkflowApproverIdsJson = objectMapper.writeValueAsString(subWorkflowApproverIds);
                instance.setSubWorkflowApproverIds(subWorkflowApproverIdsJson);
                instanceMapper.updateById(instance);
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
            task.setStatus("PENDING");
            task.setIsFirstApprover(0); // 第一个审批的审批人标记
            task.setSelectedByUserId(instance.getApplicantId()); // 由发起人选择
            taskMapper.insert(task);
        }

        // 更新进度记录，添加审批人信息
        updateProgressRecordWithApprovers(instanceId, firstStage.getId(), approverIds);
    }

    /**
     * 发起人选择第一层审批人（兼容旧接口）
     */
    @Transactional
    public void selectFirstStageApprovers(Long instanceId, List<Long> approverIds) {
        selectFirstStageApprovers(instanceId, approverIds, null);
    }

    /**
     * 检查当前阶段是否完成，并移动到下一阶段
     */
    private void checkAndMoveToNextStage(Long instanceId, Long currentStageId) {
        logger.info("检查并移动到下一阶段: instanceId={}, currentStageId={}", instanceId, currentStageId);

        WorkflowStageDO stage = stageMapper.selectById(currentStageId);
        if (stage == null) {
            logger.error("阶段不存在: stageId={}", currentStageId);
            throw new NotFoundException("阶段不存在: " + currentStageId);
        }

        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            logger.error("审批实例不存在: instanceId={}", instanceId);
            throw new NotFoundException("审批实例不存在: " + instanceId);
        }

        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(instanceId);
        query.setStageId(currentStageId);
        List<ApprovalTaskDO> tasks = taskMapper.selectList(query);
        logger.info("当前阶段任务数: instanceId={}, stageId={}, taskCount={}", instanceId, currentStageId, tasks.size());

        boolean stageCompleted = false;
        ApprovalTaskDO firstCompletedTask = null;

        if ("OR".equals(stage.getApproveType())) {
            // 或签：任一通过即可
            // 优先使用 is_first_approver=1 的任务，如果没有则使用第一个APPROVED的任务
            firstCompletedTask = tasks.stream()
                .filter(t -> "APPROVED".equals(t.getStatus()))
                .filter(t -> t.getIsFirstApprover() != null && t.getIsFirstApprover() == 1)
                .findFirst()
                .orElseGet(() -> tasks.stream()
                    .filter(t -> "APPROVED".equals(t.getStatus()))
                    .findFirst()
                    .orElse(null));
            stageCompleted = firstCompletedTask != null;
            logger.info("或签检查: stageCompleted={}, firstCompletedTaskId={}", stageCompleted,
                firstCompletedTask != null ? firstCompletedTask.getId() : null);

            // 如果或签完成，取消该层其他待办任务
            if (stageCompleted) {
                cancelPendingTasks(instanceId, currentStageId);
                logger.info("已取消同层其他待办任务: instanceId={}, stageId={}", instanceId, currentStageId);
            }
        } else {
            // 会签：全部通过（CANCELLED的任务不阻塞完成）
            stageCompleted = tasks.stream()
                .noneMatch(t -> "PENDING".equals(t.getStatus()) || "RETURNED".equals(t.getStatus()));
            if (stageCompleted) {
                // 优先使用 is_first_approver=1 的任务，如果没有则使用第一个APPROVED的任务
                firstCompletedTask = tasks.stream()
                    .filter(t -> "APPROVED".equals(t.getStatus()))
                    .filter(t -> t.getIsFirstApprover() != null && t.getIsFirstApprover() == 1)
                    .findFirst()
                    .orElseGet(() -> tasks.stream()
                        .filter(t -> "APPROVED".equals(t.getStatus()))
                        .findFirst()
                        .orElse(null));
            }
            logger.info("会签检查: stageCompleted={}, taskCount={}, approvedCount={}, cancelledCount={}",
                stageCompleted,
                tasks.size(),
                tasks.stream().filter(t -> "APPROVED".equals(t.getStatus())).count(),
                tasks.stream().filter(t -> "CANCELLED".equals(t.getStatus())).count());
        }

        if (stageCompleted) {
            logger.info("阶段已完成，开始处理下一阶段: instanceId={}, stageId={}", instanceId, currentStageId);

            // 更新当前阶段进度
            updateProgressRecord(instanceId, currentStageId, "APPROVED",
                firstCompletedTask != null ? firstCompletedTask.getApproverId() : null,
                firstCompletedTask != null ? firstCompletedTask.getComment() : null);

            // 解析下一层的子流程审批人选择（从第一个完成的任务获取）
            Map<Long, List<Long>> nextSubWorkflowApproverIds = null;
            if (firstCompletedTask != null && firstCompletedTask.getSubWorkflowApproverIds() != null) {
                try {
                    nextSubWorkflowApproverIds = objectMapper.readValue(
                        firstCompletedTask.getSubWorkflowApproverIds(),
                        new TypeReference<Map<Long, List<Long>>>() {}
                    );
                    logger.info("从任务解析下一层子流程审批人选择: nextSubWorkflowApproverIds={}", nextSubWorkflowApproverIds);
                } catch (Exception e) {
                    logger.warn("解析子流程审批人选择失败: {}", e.getMessage());
                }
            }

            // 检查是否有下一层审批人已选择，并移动到下一阶段
            if (firstCompletedTask != null && firstCompletedTask.getNextStageApproverIds() != null) {
                // 第一个审批人已选择了下一层审批人，直接创建下一层任务
                logger.info("使用已选择的下一层审批人移动到下一阶段");
                moveToNextStage(instanceId, currentStageId, firstCompletedTask.getNextStageApproverIds(), nextSubWorkflowApproverIds, firstCompletedTask.getId());
            } else {
                // 需要等待第一个审批人选择下一层审批人
                // 在新流程中，审批人在审批时选择下一层审批人
                logger.info("未选择下一层审批人，移动到下一阶段");
                moveToNextStage(instanceId, currentStageId, null, null, null);
            }
        } else {
            logger.info("阶段未完成，无需移动到下一阶段: instanceId={}, stageId={}", instanceId, currentStageId);
        }
    }

    /**
     * 移动到下一阶段
     * @param parentTaskId 触发阶段转换的父任务ID（用于关联子流程）
     */
    private void moveToNextStage(Long instanceId, Long currentStageId, String nextStageApproverIds, Map<Long, List<Long>> subWorkflowApproverIds, Long parentTaskId) {
        logger.info("移动到下一阶段: instanceId={}, currentStageId={}, nextStageApproverIds={}",
            instanceId, currentStageId, nextStageApproverIds);

        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            logger.error("审批实例不存在: instanceId={}", instanceId);
            throw new NotFoundException("审批实例不存在: " + instanceId);
        }

        WorkflowStageDO currentStage = stageMapper.selectById(currentStageId);
        if (currentStage == null) {
            logger.error("当前阶段不存在: stageId={}", currentStageId);
            throw new NotFoundException("当前阶段不存在: " + currentStageId);
        }

        // 查找下一阶段
        WorkflowStageQuery nextStageQuery = new WorkflowStageQuery();
        nextStageQuery.setWorkflowId(instance.getWorkflowId());
        nextStageQuery.setOrderByField("stage_order");
        nextStageQuery.setOrderByDirection("ASC");
        List<WorkflowStageDO> allNextStages = stageMapper.selectList(nextStageQuery);
        WorkflowStageDO nextStage = null;
        for (WorkflowStageDO stage : allNextStages) {
            if (stage.getStageOrder() > currentStage.getStageOrder()) {
                nextStage = stage;
                break;
            }
        }

        logger.info("查找下一阶段: currentStageOrder={}, nextStage={}",
            currentStage.getStageOrder(), nextStage != null ? nextStage.getName() : "null");

        if (nextStage != null) {
            // 普通阶段
            instance.setCurrentStageId(nextStage.getId());
            instanceMapper.updateById(instance);
            logger.info("已更新当前阶段: instanceId={}, newStageId={}, newStageName={}",
                instance.getId(), nextStage.getId(), nextStage.getName());

            // 创建进度记录
            createProgressRecord(instance.getId(), nextStage, null, "PENDING");

            // 如果已选择了下一层审批人，创建任务
            if (nextStageApproverIds != null) {
                try {
                    List<Long> approverIds = objectMapper.readValue(nextStageApproverIds, new TypeReference<List<Long>>() {});
                    createTasksForStageWithApprovers(instance.getId(), nextStage.getId(), approverIds, instance.getApplicantId());
                    logger.info("已创建下一层任务: approverIds={}", approverIds);
                } catch (Exception e) {
                    logger.error("解析下一层审批人失败: {}", e.getMessage(), e);
                }
            } else {
                logger.info("未选择下一层审批人，等待后续选择");
            }

            // 启动下一层的所有子流程（子流程独立运行，不阻塞主流程）
            if (subWorkflowApproverIds != null && !subWorkflowApproverIds.isEmpty()) {
                logger.info("启动下一层的子流程: instanceId={}, nextStageId={}, parentTaskId={}", instanceId, nextStage.getId(), parentTaskId);
                startSubProcessesForStage(instanceId, nextStage.getId(), parentTaskId, subWorkflowApproverIds);
            }
        } else {
            // 没有下一阶段，流程结束
            logger.info("没有下一阶段，流程即将结束: instanceId={}", instanceId);
            if (instance.getParentInstanceId() != null) {
                // 这是子流程
                instance.setStatus("APPROVED");
                instanceMapper.updateById(instance);
                logger.info("子流程已完成: instanceId={}", instanceId);

                // 检查父实例：主流程+所有子流程是否都已完成
                checkParentCompletion(instance.getParentInstanceId());
            } else {
                // 这是主流程
                // 检查所有子流程是否也都完成
                boolean allSubWorkflowsComplete = areAllSubWorkflowsComplete(instanceId);
                logger.info("主流程检查子流程完成状态: allSubWorkflowsComplete={}", allSubWorkflowsComplete);

                if (allSubWorkflowsComplete) {
                    // 主流程和所有子流程都已完成，标记整个审批为完成
                    instance.setStatus("APPROVED");
                    instanceMapper.updateById(instance);
                    logger.info("整个审批已完成: instanceId={}", instanceId);
                    handleWorkflowCompletion(instance.getBusinessType(), instance.getBusinessId());
                } else {
                    // 主流程完成但还有子流程未完成，标记主流程状态但等待子流程
                    instance.setStatus("MAIN_COMPLETED");
                    instanceMapper.updateById(instance);
                    logger.info("主流程已完成，等待子流程: instanceId={}", instanceId);
                }
            }
        }
    }

    /**
     * 检查父实例是否所有流程（主流程+子流程）都已完成
     * 如果都已完成，将父实例标记为 APPROVED
     * @param parentInstanceId 父实例ID
     */
    private void checkParentCompletion(Long parentInstanceId) {
        ApprovalInstanceDO parentInstance = instanceMapper.selectById(parentInstanceId);
        if (parentInstance == null) {
            return;
        }

        // 检查所有子流程是否都已完成
        if (!areAllSubWorkflowsComplete(parentInstanceId)) {
            return; // 还有子流程未完成，不需要处理
        }

        // 检查主流程是否已完成
        boolean mainCompleted = "APPROVED".equals(parentInstance.getStatus()) ||
                               "MAIN_COMPLETED".equals(parentInstance.getStatus());

        if (mainCompleted) {
            // 主流程和所有子流程都已完成
            parentInstance.setStatus("APPROVED");
            instanceMapper.updateById(parentInstance);
            handleWorkflowCompletion(parentInstance.getBusinessType(), parentInstance.getBusinessId());
        }
    }

    /**
     * 检查指定实例的所有子流程是否都已完成
     * 只看APPROVED状态的子流程，REJECTED和CANCELED的子流程不参与判断
     * @param parentInstanceId 父实例ID
     * @return true-所有有效的子流程都已完成，false-还有子流程未完成或不存在子流程
     */
    private boolean areAllSubWorkflowsComplete(Long parentInstanceId) {
        // 查找所有子流程实例
        ApprovalInstanceQuery query = new ApprovalInstanceQuery();
        query.setParentInstanceId(parentInstanceId);
        List<ApprovalInstanceDO> subInstances = instanceMapper.selectList(query);

        // 如果没有子流程，返回true（没有子流程需要等待）
        if (subInstances.isEmpty()) {
            return true;
        }

        // 统计有效的子流程（排除REJECTED和CANCELED）
        // 包括：PENDING、MAIN_COMPLETED、APPROVED等状态
        List<ApprovalInstanceDO> activeSubInstances = new ArrayList<>();
        for (ApprovalInstanceDO subInstance : subInstances) {
            String status = subInstance.getStatus();
            if (!"REJECTED".equals(status) && !"CANCELLED".equals(status)) {
                activeSubInstances.add(subInstance);
            }
        }

        // 如果没有有效的子流程，返回true
        if (activeSubInstances.isEmpty()) {
            logger.info("所有子流程都已被取消或拒绝，无需等待: parentInstanceId={}, 总子流程数={}, APPROVED数={}, REJECTED数={}, CANCELLED数={}",
                parentInstanceId,
                subInstances.size(),
                subInstances.stream().filter(s -> "APPROVED".equals(s.getStatus())).count(),
                subInstances.stream().filter(s -> "REJECTED".equals(s.getStatus())).count(),
                subInstances.stream().filter(s -> "CANCELLED".equals(s.getStatus())).count());
            return true;
        }

        // 检查所有有效的子流程是否都已完成
        for (ApprovalInstanceDO subInstance : activeSubInstances) {
            // 子流程可能已经流转到其他层，需要检查其内部是否有PENDING任务
            ApprovalTaskQuery taskQuery = new ApprovalTaskQuery();
            taskQuery.setInstanceId(subInstance.getId());
            taskQuery.setStatus("PENDING");
            long pendingTaskCount = taskMapper.selectCount(taskQuery);

            if (pendingTaskCount > 0) {
                logger.info("子流程还有待办任务未完成: subInstanceId={}, pendingTaskCount={}",
                    subInstance.getId(), pendingTaskCount);
                return false; // 还有子流程未完成
            }
        }

        logger.info("子流程完成检查通过: parentInstanceId={}, 有效子流程数={}",
            parentInstanceId, activeSubInstances.size());
        return true; // 所有有效子流程都已完成
    }

    /**
     * 为指定阶段启动所有子流程
     * @param parentInstanceId 父实例ID
     * @param parentStageId 父阶段ID
     * @param parentTaskId 触发子流程的父任务ID（用于记录关联关系）
     * @param subWorkflowApproverIds 子流程第一层审批人ID映射（子流程ID -> 审批人ID列表）
     */
    public void startSubProcessesForStage(Long parentInstanceId, Long parentStageId, Long parentTaskId, Map<Long, List<Long>> subWorkflowApproverIds) {
        // 获取该阶段的所有审批人配置，找出所有子流程
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(parentStageId);
        query.setSubWorkflowIdNotNull(true);
        List<StageApproverDO> subWorkflowApprovers = approverMapper.selectList(query);

        if (subWorkflowApprovers.isEmpty()) {
            return; // 没有子流程需要启动
        }

        ApprovalInstanceDO parentInstance = instanceMapper.selectById(parentInstanceId);
        if (parentInstance == null) {
            throw new NotFoundException("父实例不存在，无法启动子流程");
        }

        // 为每个子流程创建独立的审批实例
        for (StageApproverDO subWorkflowApprover : subWorkflowApprovers) {
            Long subWorkflowId = subWorkflowApprover.getSubWorkflowId();
            if (subWorkflowId == null) continue;

            // 验证子流程存在
            WorkflowDO subWorkflow = workflowMapper.selectById(subWorkflowId);
            if (subWorkflow == null) {
                logger.warn("子流程不存在，跳过: subWorkflowId={}, parentInstanceId={}, parentStageId={}",
                    subWorkflowId, parentInstanceId, parentStageId);
                continue;
            }

            // 获取子流程的第一个阶段
            WorkflowStageDO firstStage = getFirstStage(subWorkflowId);
            if (firstStage == null) {
                logger.warn("子流程没有配置阶段，跳过: subWorkflowId={}, subWorkflowName={}",
                    subWorkflowId, subWorkflow.getName());
                continue;
            }

            try {
                // 创建子流程实例
                ApprovalInstanceDO subInstance = new ApprovalInstanceDO();
                subInstance.setWorkflowId(subWorkflowId);
                subInstance.setBusinessType(parentInstance.getBusinessType());
                subInstance.setBusinessId(parentInstance.getBusinessId());
                subInstance.setApplicantId(parentInstance.getApplicantId());
                subInstance.setStatus("PENDING");
                subInstance.setRootInstanceId(parentInstanceId); // 记录根实例ID
                subInstance.setParentInstanceId(parentInstanceId);
                subInstance.setParentTaskId(parentTaskId); // 记录触发的父任务ID
                subInstance.setCurrentStageId(firstStage.getId());
                instanceMapper.insert(subInstance);

                // 创建进度记录
                createProgressRecordForSubWorkflow(subInstance.getId(), firstStage, parentInstanceId, parentTaskId, "PENDING");

                // 如果已选择子流程第一层审批人，创建任务
                List<Long> approverIds = subWorkflowApproverIds != null ? subWorkflowApproverIds.get(subWorkflowId) : null;
                if (approverIds != null && !approverIds.isEmpty()) {
                    createTasksForStageWithApprovers(subInstance.getId(), firstStage.getId(), approverIds, parentInstance.getApplicantId());
                    updateProgressRecordWithApprovers(subInstance.getId(), firstStage.getId(), approverIds);
                }

                logger.info("成功启动子流程: subWorkflowId={}, subInstanceId={}, parentInstanceId={}",
                    subWorkflowId, subInstance.getId(), parentInstanceId);
            } catch (Exception e) {
                logger.error("启动子流程失败: subWorkflowId={}, parentInstanceId={}, error={}",
                    subWorkflowId, parentInstanceId, e.getMessage(), e);
                // 继续处理其他子流程，不中断整个流程
            }
        }
    }

    /**
     * 为阶段创建任务（根据审批人配置创建任务，跳过子流程配置）
     * @param instanceId 实例ID
     * @param stageId 阶段ID
     * @param applicantId 申请人ID（用于二级部门校验）
     */
    private void createTasksForStage(Long instanceId, Long stageId, Long applicantId) {
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(stageId);
        List<StageApproverDO> approvers = approverMapper.selectList(query);

        // 收集所有实际审批人ID，避免重复
        Set<Long> actualApproverIds = new HashSet<>();
        for (StageApproverDO approver : approvers) {
            // 跳过子流程配置（sub_workflow_id 不为空的）
            if (approver.getSubWorkflowId() != null) {
                continue;
            }
            List<Long> userIds = getActualApproverIds(approver, applicantId);
            actualApproverIds.addAll(userIds);
        }

        // 为每个实际审批人创建任务
        boolean isFirst = true;
        for (Long approverId : actualApproverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(instanceId);
            task.setStageId(stageId);
            task.setApproverId(approverId);
            task.setStatus("PENDING");
            task.setIsFirstApprover(isFirst ? 1 : 0); // 标记第一个审批人
            taskMapper.insert(task);
            isFirst = false;
        }
    }

    /**
     * 为子流程创建进度记录（带唯一性检查）
     */
    private void createProgressRecordForSubWorkflow(Long instanceId, WorkflowStageDO stage, Long parentInstanceId, Long parentTaskId, String status) {
        // 检查是否已存在该实例该阶段的进度记录
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stage.getId());
        ApprovalProgressDO existing = progressMapper.selectOne(query);

        if (existing != null) {
            // 已存在记录，更新状态
            existing.setStatus(status);
            existing.setIsSubWorkflow(1); // 确保标记为子流程
            existing.setParentInstanceId(parentInstanceId);
            existing.setParentTaskId(parentTaskId);
            if (status.equals("APPROVED") || status.equals("REJECTED")) {
                existing.setApproveTime(LocalDateTime.now());
            }
            progressMapper.updateById(existing);
        } else {
            // 不存在记录，创建新记录
            ApprovalProgressDO progress = new ApprovalProgressDO();
            progress.setInstanceId(instanceId);
            progress.setStageId(stage.getId());
            progress.setStageName(stage.getName());
            progress.setStageOrder(stage.getStageOrder());
            progress.setStatus(status);
            progress.setIsSubWorkflow(1); // 标记为子流程
            progress.setParentInstanceId(parentInstanceId);
            progress.setParentTaskId(parentTaskId);
            progressMapper.insert(progress);
        }
    }

    /**
     * 创建进度记录
     */
    private void createProgressRecord(Long instanceId, WorkflowStageDO stage, Long parentInstanceId, String status) {
        createProgressRecord(instanceId, stage, parentInstanceId, null, status);
    }

    private void createProgressRecord(Long instanceId, WorkflowStageDO stage, Long parentInstanceId, Long parentStageId, String status) {
        // 检查是否已存在该实例该阶段的进度记录
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stage.getId());
        ApprovalProgressDO existing = progressMapper.selectOne(query);

        if (existing != null) {
            // 已存在记录，更新状态
            existing.setStatus(status);
            existing.setIsSubWorkflow(parentInstanceId != null ? 1 : 0);
            existing.setParentInstanceId(parentInstanceId);
            existing.setParentTaskId(parentStageId);
            if (status.equals("APPROVED") || status.equals("REJECTED")) {
                existing.setApproveTime(LocalDateTime.now());
            }
            progressMapper.updateById(existing);
        } else {
            // 不存在记录，创建新记录
            ApprovalProgressDO progress = new ApprovalProgressDO();
            progress.setInstanceId(instanceId);
            progress.setStageId(stage.getId());
            progress.setStageName(stage.getName());
            progress.setStageOrder(stage.getStageOrder());
            progress.setStatus(status);
            progress.setIsSubWorkflow(parentInstanceId != null ? 1 : 0);
            progress.setParentInstanceId(parentInstanceId);
            progress.setParentTaskId(parentStageId);
            progressMapper.insert(progress);
        }
    }

    /**
     * 发起子流程（供外部调用的方法）
     */
    @Transactional
    public void startSubProcess(Long parentInstanceId, Long parentTaskId, Long subWorkflowId) {
        ApprovalInstanceDO parentInstance = instanceMapper.selectById(parentInstanceId);
        if (parentInstance == null) {
            throw new NotFoundException("父实例不存在");
        }

        // 创建子流程实例
        ApprovalInstanceDO subInstance = new ApprovalInstanceDO();
        subInstance.setWorkflowId(subWorkflowId);
        subInstance.setBusinessType(parentInstance.getBusinessType());
        subInstance.setBusinessId(parentInstance.getBusinessId());
        subInstance.setApplicantId(parentInstance.getApplicantId());
        subInstance.setStatus("PENDING");
        subInstance.setRootInstanceId(parentInstanceId); // 主流程的根实例ID就是父实例ID
        subInstance.setParentInstanceId(parentInstanceId);
        subInstance.setParentTaskId(parentTaskId);
        instanceMapper.insert(subInstance);

        // 获取子流程的第一个阶段
        WorkflowStageDO firstStage = getFirstStage(subWorkflowId);
        if (firstStage != null) {
            subInstance.setCurrentStageId(firstStage.getId());
            instanceMapper.updateById(subInstance);

            // 创建进度记录
            createProgressRecordForSubWorkflow(subInstance.getId(), firstStage, parentInstanceId, parentTaskId, "PENDING");
        }
    }

    /**
     * 使用指定的审批人列表创建任务（用于下一层审批人选择后）
     */
    private void createTasksForStageWithApprovers(Long instanceId, Long stageId, List<Long> approverIds, Long selectedByUserId) {
        boolean isFirst = true;
        for (Long approverId : approverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(instanceId);
            task.setStageId(stageId);
            task.setApproverId(approverId);
            task.setStatus("PENDING");
            task.setIsFirstApprover(isFirst ? 1 : 0);
            task.setSelectedByUserId(selectedByUserId);
            taskMapper.insert(task);
            isFirst = false;
        }

        // 更新进度记录
        updateProgressRecordWithApprovers(instanceId, stageId, approverIds);
    }

    /**
     * 获取实际的审批人ID列表（支持二级部门校验）
     */
    private List<Long> getActualApproverIds(StageApproverDO approver, Long applicantId) {
        List<Long> userIds = new ArrayList<>();
        String type = approver.getApproverType();
        Long id = approver.getApproverId();

        if ("USER".equals(type)) {
            userIds.add(id);
        } else if ("ROLE".equals(type)) {
            // 检查是否需要校验二级部门
            boolean checkSecondary = approver.getCheckSecondaryDept() != null && approver.getCheckSecondaryDept() == 1;

            if (checkSecondary && applicantId != null) {
                // 获取申请人的二级部门
                Long applicantSecondaryDeptId = getSecondaryDeptId(applicantId);
                if (applicantSecondaryDeptId != null) {
                    // 查询该二级部门下的该角色用户
                    List<Long> deptIds = new ArrayList<>();
                    deptIds.add(applicantSecondaryDeptId);
                    deptIds.addAll(getAllSubDeptIds(applicantSecondaryDeptId));

                    UserQuery userQuery = new UserQuery();
                    userQuery.setRoleId(id);
                    userQuery.setDeptIds(deptIds);
                    userQuery.setStatus(1);
                    List<UserDO> users = userMapper.selectList(userQuery);
                    for (UserDO user : users) {
                        userIds.add(user.getId());
                    }
                }
            } else {
                // 不需要校验二级部门，查询该角色下的所有用户
                userIds = userMapper.selectUserIdsByRoleId(id);
            }
        } else if ("DEPT".equals(type)) {
            UserQuery userQuery = new UserQuery();
            userQuery.setDeptId(id);
            userQuery.setStatus(1);
            List<UserDO> users = userMapper.selectList(userQuery);
            for (UserDO u : users) {
                userIds.add(u.getId());
            }
        }
        return userIds;
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

    private void cancelPendingTasks(Long instanceId, Long stageId) {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stageId);
        query.setStatus("PENDING");
        List<ApprovalTaskDO> pendingTasks = taskMapper.selectList(query);
        for (ApprovalTaskDO task : pendingTasks) {
            task.setStatus("CANCELLED");
            taskMapper.updateById(task);
        }
    }

    /**
     * 取消指定层（被退回层）的所有已通过任务
     * 确保上层重新选择时，不会跳过被退回的层
     * @param instanceId 实例ID
     * @param stageId 被退回的层ID
     */
    private void cancelApprovedTasksInStage(Long instanceId, Long stageId) {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stageId);
        query.setStatus("APPROVED");
        List<ApprovalTaskDO> approvedTasks = taskMapper.selectList(query);
        for (ApprovalTaskDO task : approvedTasks) {
            task.setStatus("CANCELLED");
            taskMapper.updateById(task);
        }
        logger.info("已取消被退回层的所有已通过任务: instanceId={}, stageId={}, count={}",
            instanceId, stageId, approvedTasks.size());
    }

    /**
     * 重置上一层的所有任务状态为 PENDING（恢复到刚接到任务时的状态）
     * 支持会签和或签两种场景：
     * - 会签：所有任务都是 APPROVED，重置为 PENDING
     * - 或签：第一个是 APPROVED，其他是 CANCELLED，全部重置为 PENDING
     * 同时也要重置 PENDING 状态的任务（清除已保存的下一层审批人选择）
     * @param instanceId 实例ID
     * @param previousStageId 上一层ID
     */
    private void resetPreviousStageTasks(Long instanceId, Long previousStageId) {
        // 查找上层的所有任务（APPROVED、CANCELLED 和 PENDING）
        // 需要重置所有任务，包括 PENDING，以清除已保存的下一层审批人选择
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(instanceId);
        query.setStageId(previousStageId);
        query.setStatusIn(Arrays.asList("APPROVED", "CANCELLED", "PENDING"));
        List<ApprovalTaskDO> tasksToReset = taskMapper.selectList(query);

        for (ApprovalTaskDO task : tasksToReset) {
            // 使用 XML Mapper 显式更新，将字段设置为 null
            taskMapper.resetForResubmit(task.getId());
        }

        // 同时重置进度记录中的审批人状态，确保与任务状态一致
        ApprovalProgressQuery progressQuery = new ApprovalProgressQuery();
        progressQuery.setInstanceId(instanceId);
        progressQuery.setStageId(previousStageId);
        ApprovalProgressDO progress = progressMapper.selectOne(progressQuery);

        if (progress != null) {
            // 使用 XML Mapper 显式更新，将字段设置为 null
            progressMapper.resetForResubmit(progress.getId());
        }

        logger.info("已重置上一层任务和进度状态: instanceId={}, previousStageId={}, count={}",
            instanceId, previousStageId, tasksToReset.size());
    }

    /**
     * 取消指定实例的所有待办任务（包括主流程和所有子流程）
     * @param rootInstanceId 根实例ID（主流程实例ID）
     */
    private void cancelAllPendingTasksForInstance(Long rootInstanceId) {
        // 查询主流程实例的所有待办任务
        ApprovalTaskQuery mainQuery = new ApprovalTaskQuery();
        mainQuery.setInstanceId(rootInstanceId);
        mainQuery.setStatus("PENDING");
        List<ApprovalTaskDO> mainPendingTasks = taskMapper.selectList(mainQuery);
        for (ApprovalTaskDO task : mainPendingTasks) {
            task.setStatus("CANCELLED");
            taskMapper.updateById(task);
        }
        logger.info("已取消主流程待办任务: rootInstanceId={}, count={}", rootInstanceId, mainPendingTasks.size());

        // 查询所有子流程实例的待办任务
        ApprovalInstanceQuery subInstanceQuery = new ApprovalInstanceQuery();
        subInstanceQuery.setRootInstanceId(rootInstanceId);
        List<ApprovalInstanceDO> subInstances = instanceMapper.selectList(subInstanceQuery);

        for (ApprovalInstanceDO subInstance : subInstances) {
            ApprovalTaskQuery subTaskQuery = new ApprovalTaskQuery();
            subTaskQuery.setInstanceId(subInstance.getId());
            subTaskQuery.setStatus("PENDING");
            List<ApprovalTaskDO> subPendingTasks = taskMapper.selectList(subTaskQuery);
            for (ApprovalTaskDO task : subPendingTasks) {
                task.setStatus("CANCELLED");
                taskMapper.updateById(task);
            }
            logger.info("已取消子流程待办任务: subInstanceId={}, count={}", subInstance.getId(), subPendingTasks.size());
        }
    }

    private WorkflowStageDO getFirstStage(Long workflowId) {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setWorkflowId(workflowId);
        query.setOrderByField("stage_order");
        query.setOrderByDirection("ASC");
        List<WorkflowStageDO> stages = stageMapper.selectList(query);
        return stages.isEmpty() ? null : stages.get(0);
    }

    /**
     * 更新进度记录状态（只更新整体状态，不更新单个审批人状态）
     */
    private void updateProgressRecord(Long instanceId, Long stageId, String status, Long approverId, String comment) {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stageId);
        ApprovalProgressDO progress = progressMapper.selectOne(query);

        if (progress != null) {
            progress.setStatus(status);
            if ("APPROVED".equals(status)) {
                progress.setApproveTime(LocalDateTime.now());
            }
            progressMapper.updateById(progress);
            logger.info("已更新进度记录状态: instanceId={}, stageId={}, status={}", instanceId, stageId, status);
        }
    }

    /**
     * 更新进度记录中单个审批人的状态
     */
    private void updateApproverStatusInProgress(Long instanceId, Long stageId, Long approverId, String status, String comment) {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stageId);
        ApprovalProgressDO progress = progressMapper.selectOne(query);

        if (progress == null) {
            logger.warn("进度记录不存在，无法更新审批人状态: instanceId={}, stageId={}", instanceId, stageId);
            return;
        }

        if (progress.getApprovers() == null || progress.getApprovers().isEmpty()) {
            logger.warn("进度记录中没有审批人信息: instanceId={}, stageId={}", instanceId, stageId);
            return;
        }

        try {
            logger.info("开始更新审批人状态: instanceId={}, stageId={}, approverId={}, status={}, 原始approvers={}",
                instanceId, stageId, approverId, status, progress.getApprovers());

            List<Map<String, Object>> approvers = objectMapper.readValue(
                progress.getApprovers(),
                new TypeReference<List<Map<String, Object>>>() {}
            );

            logger.info("解析后的审批人列表: approvers={}", approvers);

            // 更新指定审批人的状态
            for (Map<String, Object> approver : approvers) {
                if (approverId.equals(((Number) approver.get("id")).longValue())) {
                    approver.put("status", status);
                    approver.put("comment", comment);
                    if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
                        approver.put("approveTime", LocalDateTime.now().toString());
                    }
                    logger.info("找到匹配的审批人并更新: approverId={}, 新status={}", approverId, status);
                    break;
                }
            }

            logger.info("更新后的审批人列表: approvers={}", approvers);

            // 保存更新后的审批人列表
            progress.setApprovers(objectMapper.writeValueAsString(approvers));
            progressMapper.updateById(progress);
            logger.info("已更新审批人状态: instanceId={}, stageId={}, approverId={}, status={}",
                instanceId, stageId, approverId, status);
        } catch (Exception e) {
            logger.error("更新审批人状态失败: instanceId={}, stageId={}, approverId={}, error={}",
                instanceId, stageId, approverId, e.getMessage(), e);
        }
    }

    /**
     * 标记第一个审批通过的人为 is_first_approver=1
     * 在AND签（会签）和OR签（或签）阶段，第一个完成审批的人被标记为 is_first_approver=1
     * 这样可以确保第一个完成审批的人选择的下一层审批人被使用
     */
    private void markFirstApproverInAndSign(ApprovalTaskDO completedTask) {
        // 获取阶段信息
        WorkflowStageDO stage = stageMapper.selectById(completedTask.getStageId());
        if (stage == null) {
            logger.warn("阶段不存在，无法标记is_first_approver: stageId={}", completedTask.getStageId());
            return;
        }

        // 只有AND签（会签）和OR签（或签）才需要标记第一个完成的人
        if (!"AND".equals(stage.getApproveType()) && !"OR".equals(stage.getApproveType())) {
            logger.debug("非AND/OR签阶段，跳过is_first_approver标记: stageId={}, approveType={}",
                completedTask.getStageId(), stage.getApproveType());
            return;
        }

        // 如果该任务已经是 is_first_approver=1，无需重复标记
        if (completedTask.getIsFirstApprover() != null && completedTask.getIsFirstApprover() == 1) {
            logger.debug("任务已标记为is_first_approver=1，无需重复标记: taskId={}", completedTask.getId());
            return;
        }

        // 检查同阶段是否已有任务标记为 is_first_approver=1
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(completedTask.getInstanceId());
        query.setStageId(completedTask.getStageId());
        List<ApprovalTaskDO> tasks = taskMapper.selectList(query);

        boolean hasFirstApprover = tasks.stream()
            .anyMatch(t -> t.getIsFirstApprover() != null && t.getIsFirstApprover() == 1);

        if (hasFirstApprover) {
            logger.debug("同阶段已有任务标记为is_first_approver=1，跳过标记: stageId={}", completedTask.getStageId());
            return;
        }

        // 标记当前任务为 is_first_approver=1
        completedTask.setIsFirstApprover(1);
        taskMapper.updateById(completedTask);
        logger.info("已标记{}阶段第一个完成的人为is_first_approver=1: taskId={}, approverId={}, stageId={}",
            stage.getApproveType(), completedTask.getId(), completedTask.getApproverId(), completedTask.getStageId());
    }

    /**
     * 更新进度记录，添加审批人信息
     */
    private void updateProgressRecordWithApprovers(Long instanceId, Long stageId, List<Long> approverIds) {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(instanceId);
        query.setStageId(stageId);
        ApprovalProgressDO progress = progressMapper.selectOne(query);

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
                progressMapper.updateById(progress);
            } catch (Exception e) {
                // 忽略序列化错误
            }
        }
    }

    /**
     * 处理审批完成后的业务逻辑
     * 使用策略模式，根据业务类型调用对应的处理器
     */
    private void handleWorkflowCompletion(String businessType, Long businessId) {
        WorkflowCompletionHandler handler = handlerMap.get(businessType);
        if (handler == null) {
            logger.warn("未找到业务类型 {} 的审批完成处理器", businessType);
            return;
        }

        logger.info("使用 {} 处理审批完成: businessType={}, businessId={}",
            handler.getClass().getSimpleName(), businessType, businessId);
        handler.onApproved(businessId, null);
    }

    /**
     * 处理审批驳回后的业务逻辑
     * 使用策略模式，根据业务类型调用对应的处理器
     */
    private void handleWorkflowRejection(String businessType, Long businessId) {
        WorkflowCompletionHandler handler = handlerMap.get(businessType);
        if (handler == null) {
            logger.warn("未找到业务类型 {} 的审批驳回处理器", businessType);
            return;
        }

        logger.info("使用 {} 处理审批驳回: businessType={}, businessId={}",
            handler.getClass().getSimpleName(), businessType, businessId);
        handler.onRejected(businessId, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawInstance(Long instanceId, Long userId, String comment) {
        logger.info("发起人追回工单: instanceId={}, userId={}, comment={}", instanceId, userId, comment);

        // 1. 验证实例存在
        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new NotFoundException(MSG_INSTANCE_NOT_FOUND);
        }

        // 2. 验证是否为发起人
        if (!instance.getApplicantId().equals(userId)) {
            throw new BusinessException("只有发起人才能追回工单");
        }

        // 3. 验证实例状态：只有审批中的工单才能追回
        if (!"PENDING".equals(instance.getStatus())) {
            throw new BusinessException("只有审批中的工单才能追回，当前状态：" + instance.getStatus());
        }

        // 4. 执行追回（复用驳回逻辑）
        // 4.1 更新实例状态为 REJECTED
        instance.setStatus("REJECTED");
        instanceMapper.updateById(instance);
        logger.info("实例状态已更新为REJECTED: instanceId={}", instanceId);

        // 4.2 取消所有待办任务（包括主流程和所有子流程）
        Long rootInstanceId = instance.getRootInstanceId() != null ? instance.getRootInstanceId() : instance.getId();
        cancelAllPendingTasksForInstance(rootInstanceId);

        // 4.3 更新当前阶段进度记录为 REJECTED
        if (instance.getCurrentStageId() != null) {
            updateProgressRecord(instanceId, instance.getCurrentStageId(), "REJECTED", userId, comment);
        }

        // 4.4 更新业务数据状态
        handleWorkflowRejection(instance.getBusinessType(), instance.getBusinessId());

        logger.info("工单追回完成: instanceId={}", instanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restartSubWorkflow(Long taskId, Long userId, List<Long> approverIds) {
        logger.info("重新发起子流程: taskId={}, userId={}, approverIds={}", taskId, userId, approverIds);

        // 1. 验证任务
        ApprovalTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new NotFoundException(MSG_TASK_NOT_FOUND);
        }
        if (!task.getApproverId().equals(userId)) {
            throw new BusinessException("无权操作此任务");
        }
        if (!"RESTART_SUB_WORKFLOW".equals(task.getTaskType())) {
            throw new BusinessException("该任务不是重新发起子流程任务");
        }
        if (!"PENDING".equals(task.getStatus())) {
            throw new BusinessException("任务已处理，无法重新发起");
        }

        // 2. 解析子流程信息
        if (task.getSubWorkflowApproverIds() == null) {
            throw new BusinessException("子流程信息不存在");
        }
        Map<String, Long> subWorkflowInfo;
        try {
            subWorkflowInfo = objectMapper.readValue(task.getSubWorkflowApproverIds(),
                new TypeReference<Map<String, Long>>() {});
        } catch (Exception e) {
            throw new SystemException("解析子流程信息失败: " + e.getMessage());
        }

        Long subWorkflowId = subWorkflowInfo.get("subWorkflowId");
        Long originalParentTaskId = subWorkflowInfo.get("originalParentTaskId");
        if (subWorkflowId == null) {
            throw new BusinessException("子流程ID不存在");
        }

        // 3. 获取主流程实例和父任务
        ApprovalInstanceDO parentInstance = instanceMapper.selectById(task.getInstanceId());
        if (parentInstance == null) {
            throw new NotFoundException("主流程实例不存在");
        }

        // 4. 创建新的子流程实例
        ApprovalInstanceDO newSubInstance = new ApprovalInstanceDO();
        newSubInstance.setWorkflowId(subWorkflowId);
        newSubInstance.setBusinessType(parentInstance.getBusinessType());
        newSubInstance.setBusinessId(parentInstance.getBusinessId());
        newSubInstance.setApplicantId(parentInstance.getApplicantId());
        newSubInstance.setParentInstanceId(parentInstance.getId());
        newSubInstance.setParentTaskId(originalParentTaskId);
        newSubInstance.setRootInstanceId(parentInstance.getId());
        newSubInstance.setStatus("PENDING");
        instanceMapper.insert(newSubInstance);

        // 5. 获取子流程的第一阶段并设置当前阶段ID
        WorkflowStageDO firstStage = getFirstStage(subWorkflowId);
        if (firstStage == null) {
            throw new NotFoundException("子流程未配置阶段");
        }
        newSubInstance.setCurrentStageId(firstStage.getId());
        instanceMapper.updateById(newSubInstance);

        logger.info("已创建新子流程实例: subInstanceId={}, subWorkflowId={}, parentInstanceId={}",
            newSubInstance.getId(), subWorkflowId, parentInstance.getId());

        // 6. 创建子流程第一层进度记录
        createProgressRecordForSubWorkflow(newSubInstance.getId(), firstStage,
            newSubInstance.getRootInstanceId(), originalParentTaskId, "PENDING");

        // 7. 创建子流程第一层审批任务
        boolean isFirst = true;
        for (Long approverId : approverIds) {
            ApprovalTaskDO newTask = new ApprovalTaskDO();
            newTask.setInstanceId(newSubInstance.getId());
            newTask.setStageId(firstStage.getId());
            newTask.setApproverId(approverId);
            newTask.setStatus("PENDING");
            newTask.setIsFirstApprover(isFirst ? 1 : 0);
            newTask.setTaskType("NORMAL");
            taskMapper.insert(newTask);
            isFirst = false;
        }

        // 8. 更新进度记录（添加审批人信息）
        updateProgressRecordWithApprovers(newSubInstance.getId(), firstStage.getId(),
            new ArrayList<>(approverIds));

        // 7. 完成重新发起任务
        task.setStatus("APPROVED");
        task.setComment("已重新发起子流程");
        task.setApproveTime(LocalDateTime.now());
        taskMapper.updateById(task);

        logger.info("重新发起子流程完成: taskId={}, subInstanceId={}", taskId, newSubInstance.getId());
    }
}
