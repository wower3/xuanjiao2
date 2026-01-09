package com.xuanjiao.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.app.service.WorkflowEngineService;
import com.xuanjiao.infrastructure.dataobject.*;
import com.xuanjiao.infrastructure.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

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
    private UserMapper userMapper;

    @Override
    @Transactional
    public Long startProcess(Long workflowId, String businessType, Long businessId, Long applicantId) {
        // 创建审批实例
        ApprovalInstanceDO instance = new ApprovalInstanceDO();
        instance.setWorkflowId(workflowId);
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setApplicantId(applicantId);
        instance.setStatus("PENDING");
        instanceMapper.insert(instance);

        // 获取第一个阶段并创建任务
        WorkflowStageDO firstStage = getFirstStage(workflowId);
        if (firstStage != null) {
            instance.setCurrentStageId(firstStage.getId());
            instanceMapper.updateById(instance);
            createTasksForStage(instance.getId(), firstStage.getId());
        }
        return instance.getId();
    }

    @Override
    @Transactional
    public void completeTask(Long taskId, Long userId, boolean approved, String comment) {
        ApprovalTaskDO task = taskMapper.selectById(taskId);
        if (task == null || !task.getApproverId().equals(userId)) {
            throw new RuntimeException("任务不存在或无权操作");
        }
        // 更新任务状态
        task.setStatus(approved ? "APPROVED" : "REJECTED");
        task.setComment(comment);
        task.setApproveTime(LocalDateTime.now());
        taskMapper.updateById(task);

        if (!approved) {
            // 驳回：更新实例状态
            ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());
            instance.setStatus("REJECTED");
            instanceMapper.updateById(instance);
            return;
        }
        // 检查当前阶段是否完成
        checkAndMoveToNextStage(task.getInstanceId(), task.getStageId());
    }

    private WorkflowStageDO getFirstStage(Long workflowId) {
        LambdaQueryWrapper<WorkflowStageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageDO::getWorkflowId, workflowId)
               .orderByAsc(WorkflowStageDO::getStageOrder)
               .last("LIMIT 1");
        return stageMapper.selectOne(wrapper);
    }

    private void createTasksForStage(Long instanceId, Long stageId) {
        LambdaQueryWrapper<StageApproverDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StageApproverDO::getStageId, stageId);
        List<StageApproverDO> approvers = approverMapper.selectList(wrapper);

        // 收集所有实际审批人ID，避免重复
        Set<Long> actualApproverIds = new HashSet<>();
        for (StageApproverDO approver : approvers) {
            List<Long> userIds = getActualApproverIds(approver);
            actualApproverIds.addAll(userIds);
        }

        // 为每个实际审批人创建任务
        for (Long approverId : actualApproverIds) {
            ApprovalTaskDO task = new ApprovalTaskDO();
            task.setInstanceId(instanceId);
            task.setStageId(stageId);
            task.setApproverId(approverId);
            task.setStatus("PENDING");
            taskMapper.insert(task);
        }
    }

    private List<Long> getActualApproverIds(StageApproverDO approver) {
        List<Long> userIds = new ArrayList<>();
        String type = approver.getApproverType();
        Long id = approver.getApproverId();

        if ("USER".equals(type)) {
            userIds.add(id);
        } else if ("ROLE".equals(type)) {
            // 查询该角色下的所有用户
            userIds = userMapper.selectUserIdsByRoleId(id);
        } else if ("DEPT".equals(type)) {
            // 查询该部门下的所有用户
            LambdaQueryWrapper<UserDO> w = new LambdaQueryWrapper<>();
            w.eq(UserDO::getDeptId, id).eq(UserDO::getStatus, 1);
            List<UserDO> users = userMapper.selectList(w);
            for (UserDO u : users) {
                userIds.add(u.getId());
            }
        }
        return userIds;
    }

    private void checkAndMoveToNextStage(Long instanceId, Long currentStageId) {
        WorkflowStageDO stage = stageMapper.selectById(currentStageId);
        LambdaQueryWrapper<ApprovalTaskDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTaskDO::getInstanceId, instanceId)
               .eq(ApprovalTaskDO::getStageId, currentStageId);
        List<ApprovalTaskDO> tasks = taskMapper.selectList(wrapper);

        boolean stageCompleted = false;
        if ("OR".equals(stage.getApproveType())) {
            // 或签：任一通过即可
            stageCompleted = tasks.stream().anyMatch(t -> "APPROVED".equals(t.getStatus()));
            // 如果或签完成，取消该层其他待办任务
            if (stageCompleted) {
                cancelPendingTasks(instanceId, currentStageId);
            }
        } else {
            // 会签：全部通过
            stageCompleted = tasks.stream().allMatch(t -> "APPROVED".equals(t.getStatus()));
        }

        if (stageCompleted) {
            moveToNextStage(instanceId, currentStageId);
        }
    }

    private void cancelPendingTasks(Long instanceId, Long stageId) {
        // 将该层所有PENDING状态的任务改为CANCELLED
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

    private void moveToNextStage(Long instanceId, Long currentStageId) {
        ApprovalInstanceDO instance = instanceMapper.selectById(instanceId);
        WorkflowStageDO currentStage = stageMapper.selectById(currentStageId);

        // 查找下一阶段
        LambdaQueryWrapper<WorkflowStageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageDO::getWorkflowId, instance.getWorkflowId())
               .gt(WorkflowStageDO::getStageOrder, currentStage.getStageOrder())
               .orderByAsc(WorkflowStageDO::getStageOrder)
               .last("LIMIT 1");
        WorkflowStageDO nextStage = stageMapper.selectOne(wrapper);

        if (nextStage != null) {
            instance.setCurrentStageId(nextStage.getId());
            instanceMapper.updateById(instance);
            createTasksForStage(instanceId, nextStage.getId());
        } else {
            // 没有下一阶段，流程结束
            instance.setStatus("APPROVED");
            instanceMapper.updateById(instance);
        }
    }
}
