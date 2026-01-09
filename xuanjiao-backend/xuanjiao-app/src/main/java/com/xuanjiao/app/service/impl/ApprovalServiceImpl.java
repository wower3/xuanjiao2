package com.xuanjiao.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.app.service.ApprovalService;
import com.xuanjiao.app.service.WorkflowEngineService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.infrastructure.dataobject.*;
import com.xuanjiao.infrastructure.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalServiceImpl implements ApprovalService {

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
    public PageResult<Map<String, Object>> getMyApplied(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<ApprovalInstanceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalInstanceDO::getApplicantId, userId)
               .orderByDesc(ApprovalInstanceDO::getCreateTime);
        Page<ApprovalInstanceDO> page = instanceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Map<String, Object>> list = page.getRecords().stream()
            .map(this::buildInstanceInfo).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Override
    @Transactional
    public void approve(Long taskId, Long userId, String comment, boolean passed) {
        // 调用工作流引擎完成任务
        workflowEngineService.completeTask(taskId, userId, passed, comment);

        // 获取任务和实例信息
        ApprovalTaskDO task = taskMapper.selectById(taskId);
        ApprovalInstanceDO instance = instanceMapper.selectById(task.getInstanceId());

        // 如果是素材审批，更新素材状态
        if ("ASSET".equals(instance.getBusinessType())) {
            AssetDO asset = assetMapper.selectById(instance.getBusinessId());
            if (asset != null) {
                if ("APPROVED".equals(instance.getStatus())) {
                    asset.setStatus("APPROVED");
                    assetMapper.updateById(asset);
                } else if ("REJECTED".equals(instance.getStatus())) {
                    asset.setStatus("REJECTED");
                    assetMapper.updateById(asset);
                }
            }
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

            // 获取业务名称（素材名称）
            if ("ASSET".equals(instance.getBusinessType())) {
                AssetDO asset = assetMapper.selectById(instance.getBusinessId());
                if (asset != null) {
                    map.put("businessName", asset.getName());
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

        // 获取流程名称
        WorkflowDO workflow = workflowMapper.selectById(instance.getWorkflowId());
        if (workflow != null) {
            map.put("workflowName", workflow.getName());
        }

        // 获取业务名称
        if ("ASSET".equals(instance.getBusinessType())) {
            AssetDO asset = assetMapper.selectById(instance.getBusinessId());
            if (asset != null) {
                map.put("businessName", asset.getName());
            }
        }
        return map;
    }
}
