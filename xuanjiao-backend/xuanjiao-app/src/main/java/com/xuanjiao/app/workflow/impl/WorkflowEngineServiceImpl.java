package com.xuanjiao.app.workflow.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.app.material.MaterialApplicationService;
import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.infrastructure.dataobject.*;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.approval.ApprovalTaskMapper;
import com.xuanjiao.infrastructure.approval.ApprovalProgressMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 工作流引擎服务实现（改造版）
 * 支持每层通过后选择下一层审批人、子流程、审批进度记录等新功能
 */
@Service
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngineServiceImpl.class);

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
    @Lazy
    private MaterialApplicationService materialApplicationService;
    @Resource
    @Lazy
    private AssetService assetService;
    @Resource
    private ApproverSelectionService approverSelectionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Long startProcess(Long workflowId, String businessType, Long businessId, Long applicantId) {
        // 检查流程是否存在且已启用
        WorkflowDO workflow = workflowMapper.selectById(workflowId);
        if (workflow == null) {
            throw new RuntimeException("流程不存在，ID: " + workflowId);
        }
        if (workflow.getStatus() == null || workflow.getStatus() != 1) {
            throw new RuntimeException("流程已禁用，无法创建审批实例。流程名称：《" + workflow.getName() + "》");
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
            throw new RuntimeException("任务不存在");
        }
        if (!task.getApproverId().equals(userId)) {
            logger.error("无权操作: taskId={}, taskApproverId={}, userId={}", taskId, task.getApproverId(), userId);
            throw new RuntimeException("无权操作此任务");
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

                // 取消所有待办任务（包括主流程和所有子流程）
                // 获取根实例ID：主流程的rootInstanceId为null，使用当前实例ID；子流程使用rootInstanceId
                Long rootInstanceId = instance.getRootInstanceId() != null ? instance.getRootInstanceId() : instance.getId();
                cancelAllPendingTasksForInstance(rootInstanceId);

                // 更新进度记录
                updateProgressRecord(task.getInstanceId(), task.getStageId(), "REJECTED", userId, comment);

                // 更新业务数据状态
                handleWorkflowRejection(instance.getBusinessType(), instance.getBusinessId());
            }
            return;
        }

        // 审批通过：先更新当前审批人的状态（会签时，每个人完成后都要更新）
        updateApproverStatusInProgress(task.getInstanceId(), task.getStageId(), userId, "APPROVED", comment);

        // 检查当前阶段是否完成
        logger.info("检查阶段是否完成: instanceId={}, stageId={}", task.getInstanceId(), task.getStageId());
        checkAndMoveToNextStage(task.getInstanceId(), task.getStageId());
        logger.info("完成任务处理完成: taskId={}", taskId);
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
            throw new RuntimeException("审批实例不存在");
        }

        WorkflowStageDO firstStage = getFirstStage(instance.getWorkflowId());
        if (firstStage == null) {
            throw new RuntimeException("未找到第一阶段");
        }

        // 保存子流程审批人选择到实例
        if (subWorkflowApproverIds != null && !subWorkflowApproverIds.isEmpty()) {
            try {
                String subWorkflowApproverIdsJson = objectMapper.writeValueAsString(subWorkflowApproverIds);
                instance.setSubWorkflowApproverIds(subWorkflowApproverIdsJson);
                instanceMapper.updateById(instance);
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
            throw new RuntimeException("阶段不存在: " + currentStageId);
        }

        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            logger.error("审批实例不存在: instanceId={}", instanceId);
            throw new RuntimeException("审批实例不存在: " + instanceId);
        }

        LambdaQueryWrapper<ApprovalTaskDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTaskDO::getInstanceId, instanceId)
               .eq(ApprovalTaskDO::getStageId, currentStageId);
        List<ApprovalTaskDO> tasks = taskMapper.selectList(wrapper);
        logger.info("当前阶段任务数: instanceId={}, stageId={}, taskCount={}", instanceId, currentStageId, tasks.size());

        boolean stageCompleted = false;
        ApprovalTaskDO firstCompletedTask = null;

        if ("OR".equals(stage.getApproveType())) {
            // 或签：任一通过即可
            firstCompletedTask = tasks.stream()
                .filter(t -> "APPROVED".equals(t.getStatus()))
                .findFirst()
                .orElse(null);
            stageCompleted = firstCompletedTask != null;
            logger.info("或签检查: stageCompleted={}, firstCompletedTaskId={}", stageCompleted,
                firstCompletedTask != null ? firstCompletedTask.getId() : null);

            // 如果或签完成，取消该层其他待办任务
            if (stageCompleted) {
                cancelPendingTasks(instanceId, currentStageId);
                logger.info("已取消同层其他待办任务: instanceId={}, stageId={}", instanceId, currentStageId);
            }
        } else {
            // 会签：全部通过
            stageCompleted = tasks.stream().allMatch(t -> "APPROVED".equals(t.getStatus()));
            if (stageCompleted) {
                firstCompletedTask = tasks.stream()
                    .filter(t -> "APPROVED".equals(t.getStatus()))
                    .findFirst()
                    .orElse(null);
            }
            logger.info("会签检查: stageCompleted={}", stageCompleted);
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
                moveToNextStage(instanceId, currentStageId, firstCompletedTask.getNextStageApproverIds(), nextSubWorkflowApproverIds);
            } else {
                // 需要等待第一个审批人选择下一层审批人
                // 在新流程中，审批人在审批时选择下一层审批人
                logger.info("未选择下一层审批人，移动到下一阶段");
                moveToNextStage(instanceId, currentStageId, null, null);
            }
        } else {
            logger.info("阶段未完成，无需移动到下一阶段: instanceId={}, stageId={}", instanceId, currentStageId);
        }
    }

    /**
     * 移动到下一阶段
     */
    private void moveToNextStage(Long instanceId, Long currentStageId, String nextStageApproverIds, Map<Long, List<Long>> subWorkflowApproverIds) {
        logger.info("移动到下一阶段: instanceId={}, currentStageId={}, nextStageApproverIds={}",
            instanceId, currentStageId, nextStageApproverIds);

        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            logger.error("审批实例不存在: instanceId={}", instanceId);
            throw new RuntimeException("审批实例不存在: " + instanceId);
        }

        WorkflowStageDO currentStage = stageMapper.selectById(currentStageId);
        if (currentStage == null) {
            logger.error("当前阶段不存在: stageId={}", currentStageId);
            throw new RuntimeException("当前阶段不存在: " + currentStageId);
        }

        // 查找下一阶段
        LambdaQueryWrapper<WorkflowStageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageDO::getWorkflowId, instance.getWorkflowId())
               .gt(WorkflowStageDO::getStageOrder, currentStage.getStageOrder())
               .orderByAsc(WorkflowStageDO::getStageOrder)
               .last("LIMIT 1");
        WorkflowStageDO nextStage = stageMapper.selectOne(wrapper);

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
                logger.info("启动下一层的子流程: instanceId={}, nextStageId={}", instanceId, nextStage.getId());
                startSubProcessesForStage(instanceId, nextStage.getId(), null, subWorkflowApproverIds);
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
     * @param parentInstanceId 父实例ID
     * @return true-所有子流程都已完成，false-还有子流程未完成或不存在子流程
     */
    private boolean areAllSubWorkflowsComplete(Long parentInstanceId) {
        // 查找所有子流程实例
        LambdaQueryWrapper<ApprovalInstanceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalInstanceDO::getParentInstanceId, parentInstanceId);
        List<ApprovalInstanceDO> subInstances = instanceMapper.selectList(wrapper);

        // 如果没有子流程，返回true（没有子流程需要等待）
        if (subInstances.isEmpty()) {
            return true;
        }

        // 检查所有子流程是否都已完成（APPROVED 或 REJECTED）
        for (ApprovalInstanceDO subInstance : subInstances) {
            if (!"APPROVED".equals(subInstance.getStatus()) && !"REJECTED".equals(subInstance.getStatus())) {
                return false; // 还有子流程未完成
            }
        }

        return true; // 所有子流程都已完成
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
        LambdaQueryWrapper<StageApproverDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StageApproverDO::getStageId, parentStageId)
               .isNotNull(StageApproverDO::getSubWorkflowId);
        List<StageApproverDO> subWorkflowApprovers = approverMapper.selectList(wrapper);

        if (subWorkflowApprovers.isEmpty()) {
            return; // 没有子流程需要启动
        }

        ApprovalInstanceDO parentInstance = instanceMapper.selectById(parentInstanceId);
        if (parentInstance == null) {
            throw new RuntimeException("父实例不存在，无法启动子流程");
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
        LambdaQueryWrapper<StageApproverDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StageApproverDO::getStageId, stageId);
        List<StageApproverDO> approvers = approverMapper.selectList(wrapper);

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
        LambdaQueryWrapper<ApprovalProgressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProgressDO::getInstanceId, instanceId)
               .eq(ApprovalProgressDO::getStageId, stage.getId());
        ApprovalProgressDO existing = progressMapper.selectOne(wrapper);

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
        LambdaQueryWrapper<ApprovalProgressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProgressDO::getInstanceId, instanceId)
               .eq(ApprovalProgressDO::getStageId, stage.getId());
        ApprovalProgressDO existing = progressMapper.selectOne(wrapper);

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
            throw new RuntimeException("父实例不存在");
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

                    LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(UserDO::getRoleId, id)
                           .in(UserDO::getDeptId, deptIds)
                           .eq(UserDO::getStatus, 1);
                    List<UserDO> users = userMapper.selectList(wrapper);
                    for (UserDO user : users) {
                        userIds.add(user.getId());
                    }
                }
            } else {
                // 不需要校验二级部门，查询该角色下的所有用户
                userIds = userMapper.selectUserIdsByRoleId(id);
            }
        } else if ("DEPT".equals(type)) {
            LambdaQueryWrapper<UserDO> w = new LambdaQueryWrapper<>();
            w.eq(UserDO::getDeptId, id).eq(UserDO::getStatus, 1);
            List<UserDO> users = userMapper.selectList(w);
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
        LambdaQueryWrapper<ApprovalTaskDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTaskDO::getInstanceId, instanceId)
               .eq(ApprovalTaskDO::getStageId, stageId)
               .eq(ApprovalTaskDO::getStatus, "PENDING");
        List<ApprovalTaskDO> pendingTasks = taskMapper.selectList(wrapper);
        for (ApprovalTaskDO task : pendingTasks) {
            task.setStatus("CANCELLED");
            taskMapper.updateById(task);
        }
    }

    /**
     * 取消指定实例的所有待办任务（包括主流程和所有子流程）
     * @param rootInstanceId 根实例ID（主流程实例ID）
     */
    private void cancelAllPendingTasksForInstance(Long rootInstanceId) {
        // 查询主流程实例的所有待办任务
        LambdaQueryWrapper<ApprovalTaskDO> mainWrapper = new LambdaQueryWrapper<>();
        mainWrapper.eq(ApprovalTaskDO::getInstanceId, rootInstanceId)
                   .eq(ApprovalTaskDO::getStatus, "PENDING");
        List<ApprovalTaskDO> mainPendingTasks = taskMapper.selectList(mainWrapper);
        for (ApprovalTaskDO task : mainPendingTasks) {
            task.setStatus("CANCELLED");
            taskMapper.updateById(task);
        }
        logger.info("已取消主流程待办任务: rootInstanceId={}, count={}", rootInstanceId, mainPendingTasks.size());

        // 查询所有子流程实例的待办任务
        LambdaQueryWrapper<ApprovalInstanceDO> subInstanceWrapper = new LambdaQueryWrapper<>();
        subInstanceWrapper.eq(ApprovalInstanceDO::getRootInstanceId, rootInstanceId);
        List<ApprovalInstanceDO> subInstances = instanceMapper.selectList(subInstanceWrapper);

        for (ApprovalInstanceDO subInstance : subInstances) {
            LambdaQueryWrapper<ApprovalTaskDO> subTaskWrapper = new LambdaQueryWrapper<>();
            subTaskWrapper.eq(ApprovalTaskDO::getInstanceId, subInstance.getId())
                         .eq(ApprovalTaskDO::getStatus, "PENDING");
            List<ApprovalTaskDO> subPendingTasks = taskMapper.selectList(subTaskWrapper);
            for (ApprovalTaskDO task : subPendingTasks) {
                task.setStatus("CANCELLED");
                taskMapper.updateById(task);
            }
            logger.info("已取消子流程待办任务: subInstanceId={}, count={}", subInstance.getId(), subPendingTasks.size());
        }
    }

    private WorkflowStageDO getFirstStage(Long workflowId) {
        LambdaQueryWrapper<WorkflowStageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageDO::getWorkflowId, workflowId)
               .orderByAsc(WorkflowStageDO::getStageOrder)
               .last("LIMIT 1");
        return stageMapper.selectOne(wrapper);
    }

    /**
     * 更新进度记录状态（只更新整体状态，不更新单个审批人状态）
     */
    private void updateProgressRecord(Long instanceId, Long stageId, String status, Long approverId, String comment) {
        LambdaQueryWrapper<ApprovalProgressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProgressDO::getInstanceId, instanceId)
               .eq(ApprovalProgressDO::getStageId, stageId);
        ApprovalProgressDO progress = progressMapper.selectOne(wrapper);

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
        LambdaQueryWrapper<ApprovalProgressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProgressDO::getInstanceId, instanceId)
               .eq(ApprovalProgressDO::getStageId, stageId);
        ApprovalProgressDO progress = progressMapper.selectOne(wrapper);

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
     * 更新进度记录，添加审批人信息
     */
    private void updateProgressRecordWithApprovers(Long instanceId, Long stageId, List<Long> approverIds) {
        LambdaQueryWrapper<ApprovalProgressDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalProgressDO::getInstanceId, instanceId)
               .eq(ApprovalProgressDO::getStageId, stageId);
        ApprovalProgressDO progress = progressMapper.selectOne(wrapper);

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

    private void handleWorkflowCompletion(String businessType, Long businessId) {
        if ("MATERIAL_ENTRY".equals(businessType)) {
            materialApplicationService.updateStatus(businessId, "APPROVED");
            assetService.updateStatusByApplicationId(businessId, "APPROVED");
        }
    }

    private void handleWorkflowRejection(String businessType, Long businessId) {
        if ("MATERIAL_ENTRY".equals(businessType)) {
            materialApplicationService.updateStatus(businessId, "REJECTED");
            assetService.updateStatusByApplicationId(businessId, "REJECTED");
        }
    }
}
