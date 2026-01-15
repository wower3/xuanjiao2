package com.xuanjiao.app.service.impl;

import com.xuanjiao.app.service.WorkflowService;
import com.xuanjiao.client.dto.WorkflowDTO;
import com.xuanjiao.client.dto.WorkflowStageDTO;
import com.xuanjiao.client.dto.StageApproverDTO;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.mapper.WorkflowMapper;
import com.xuanjiao.infrastructure.mapper.WorkflowStageMapper;
import com.xuanjiao.infrastructure.mapper.StageApproverMapper;
import com.xuanjiao.infrastructure.mapper.UserMapper;
import com.xuanjiao.infrastructure.mapper.RoleMapper;
import com.xuanjiao.infrastructure.mapper.DeptMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Resource
    private WorkflowMapper workflowMapper;

    @Resource
    private WorkflowStageMapper stageMapper;

    @Resource
    private StageApproverMapper approverMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private DeptMapper deptMapper;

    @Override
    public List<WorkflowDTO> list() {
        List<WorkflowDO> list = workflowMapper.selectList(null);
        return list.stream().map(workflow -> {
            WorkflowDTO dto = convert(workflow);
            // 加载绑定的角色名称
            if (workflow.getBoundRoleId() != null) {
                RoleDO role = roleMapper.selectById(workflow.getBoundRoleId());
                if (role != null) {
                    dto.setRoleName(role.getName());
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public WorkflowDTO getById(Long id) {
        WorkflowDO workflow = workflowMapper.selectById(id);
        if (workflow == null) return null;
        WorkflowDTO dto = convert(workflow);
        // 如果绑定了角色，加载角色名称
        if (workflow.getBoundRoleId() != null) {
            RoleDO role = roleMapper.selectById(workflow.getBoundRoleId());
            if (role != null) {
                dto.setRoleName(role.getName());
            }
        }
        LambdaQueryWrapper<WorkflowStageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageDO::getWorkflowId, id).orderByAsc(WorkflowStageDO::getStageOrder);
        List<WorkflowStageDO> stages = stageMapper.selectList(wrapper);
        List<WorkflowStageDTO> stageDTOs = new ArrayList<>();
        for (WorkflowStageDO stage : stages) {
            WorkflowStageDTO stageDTO = convertStage(stage);
            // 查询该阶段的所有审批人（包括普通审批人和子流程）
            LambdaQueryWrapper<StageApproverDO> approverWrapper = new LambdaQueryWrapper<>();
            approverWrapper.eq(StageApproverDO::getStageId, stage.getId());
            List<StageApproverDO> approvers = approverMapper.selectList(approverWrapper);
            stageDTO.setApprovers(approvers.stream().map(this::convertApprover).collect(Collectors.toList()));
            stageDTOs.add(stageDTO);
        }
        dto.setStages(stageDTOs);
        return dto;
    }

    @Override
    @Transactional
    public void save(WorkflowDTO dto) {
        WorkflowDO workflow = new WorkflowDO();
        BeanUtils.copyProperties(dto, workflow);
        workflowMapper.insert(workflow);
        saveStages(workflow.getId(), dto.getStages());
    }

    @Override
    @Transactional
    public void update(WorkflowDTO dto) {
        WorkflowDO workflow = new WorkflowDO();
        BeanUtils.copyProperties(dto, workflow);
        workflowMapper.updateById(workflow);
        // 先查询旧的阶段ID
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, dto.getId());
        List<WorkflowStageDO> oldStages = stageMapper.selectList(stageWrapper);
        // 删除旧的审批人
        for (WorkflowStageDO oldStage : oldStages) {
            LambdaQueryWrapper<StageApproverDO> approverWrapper = new LambdaQueryWrapper<>();
            approverWrapper.eq(StageApproverDO::getStageId, oldStage.getId());
            approverMapper.delete(approverWrapper);
        }
        // 删除旧的阶段
        stageMapper.delete(stageWrapper);
        // 保存新的阶段和审批人
        saveStages(dto.getId(), dto.getStages());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 先删除阶段和审批人
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, id);
        List<WorkflowStageDO> stages = stageMapper.selectList(stageWrapper);
        for (WorkflowStageDO stage : stages) {
            // 删除该阶段的审批人
            LambdaQueryWrapper<StageApproverDO> approverWrapper = new LambdaQueryWrapper<>();
            approverWrapper.eq(StageApproverDO::getStageId, stage.getId());
            approverMapper.delete(approverWrapper);
        }
        // 删除所有阶段
        stageMapper.delete(stageWrapper);
        // 最后删除流程
        workflowMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        // 获取当前流程
        WorkflowDO currentWorkflow = workflowMapper.selectById(id);
        if (currentWorkflow == null) {
            throw new RuntimeException("流程不存在");
        }

        // 如果是启用操作，检查是否有冲突
        if (status == 1) {
            // 检查是否有其他同角色+流程类型的已启用流程
            if (currentWorkflow.getBoundRoleId() != null && currentWorkflow.getWorkflowType() != null) {
                LambdaQueryWrapper<WorkflowDO> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(WorkflowDO::getBoundRoleId, currentWorkflow.getBoundRoleId())
                       .eq(WorkflowDO::getWorkflowType, currentWorkflow.getWorkflowType())
                       .eq(WorkflowDO::getStatus, 1)
                       .eq(WorkflowDO::getDeleted, 0)
                       .ne(WorkflowDO::getId, id);

                List<WorkflowDO> conflictingWorkflows = workflowMapper.selectList(wrapper);
                if (!conflictingWorkflows.isEmpty()) {
                    // 获取角色名称
                    RoleDO role = roleMapper.selectById(currentWorkflow.getBoundRoleId());
                    String roleName = role != null ? role.getName() : "未知角色";
                    String workflowTypeName = getWorkflowTypeName(currentWorkflow.getWorkflowType());

                    StringBuilder errorMsg = new StringBuilder();
                    errorMsg.append("启用失败：角色【").append(roleName).append("】的【").append(workflowTypeName).append("】类型已有启用的流程：");
                    for (WorkflowDO wf : conflictingWorkflows) {
                        errorMsg.append("《").append(wf.getName()).append("》");
                    }
                    throw new RuntimeException(errorMsg.toString());
                }
            }
        }

        // 更新流程状态
        WorkflowDO workflow = new WorkflowDO();
        workflow.setId(id);
        workflow.setStatus(status);
        workflowMapper.updateById(workflow);
    }

    /**
     * 获取流程类型名称
     */
    private String getWorkflowTypeName(String workflowType) {
        if ("ASSET_UPLOAD".equals(workflowType)) {
            return "素材录入";
        } else if ("ASSET_USAGE".equals(workflowType)) {
            return "素材使用";
        }
        return workflowType != null ? workflowType : "未知类型";
    }

    @Override
    @Transactional
    public void bindRole(Long id, Long roleId, String workflowType) {
        // 获取当前流程
        WorkflowDO currentWorkflow = workflowMapper.selectById(id);
        if (currentWorkflow == null) {
            throw new RuntimeException("流程不存在");
        }

        // 检查是否有其他同角色+流程类型的已启用流程
        LambdaQueryWrapper<WorkflowDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowDO::getBoundRoleId, roleId)
               .eq(WorkflowDO::getWorkflowType, workflowType)
               .eq(WorkflowDO::getStatus, 1)
               .eq(WorkflowDO::getDeleted, 0)
               .ne(WorkflowDO::getId, id);

        List<WorkflowDO> conflictingWorkflows = workflowMapper.selectList(wrapper);
        if (!conflictingWorkflows.isEmpty()) {
            // 获取角色名称
            RoleDO role = roleMapper.selectById(roleId);
            String roleName = role != null ? role.getName() : "未知角色";
            String workflowTypeName = getWorkflowTypeName(workflowType);

            StringBuilder errorMsg = new StringBuilder();
            errorMsg.append("绑定失败：角色【").append(roleName).append("】的【").append(workflowTypeName).append("】类型已有启用的流程：");
            for (WorkflowDO wf : conflictingWorkflows) {
                errorMsg.append("《").append(wf.getName()).append("》");
            }
            throw new RuntimeException(errorMsg.toString());
        }

        // 绑定角色和流程类型，并启用当前流程
        WorkflowDO workflow = new WorkflowDO();
        workflow.setId(id);
        workflow.setBoundRoleId(roleId);
        workflow.setWorkflowType(workflowType);
        workflow.setStatus(1); // 绑定即启用
        workflowMapper.updateById(workflow);
    }

    @Override
    @Transactional
    public void unbindRole(Long id) {
        WorkflowDO workflow = new WorkflowDO();
        workflow.setId(id);
        workflow.setBoundRoleId(null);
        workflow.setWorkflowType(null);
        workflowMapper.updateById(workflow);
    }

    @Override
    @Transactional
    public WorkflowDTO copy(Long id) {
        // 获取原流程
        WorkflowDO originalWorkflow = workflowMapper.selectById(id);
        if (originalWorkflow == null) {
            throw new RuntimeException("原流程不存在");
        }

        // 获取原流程的所有阶段
        LambdaQueryWrapper<WorkflowStageDO> stageWrapper = new LambdaQueryWrapper<>();
        stageWrapper.eq(WorkflowStageDO::getWorkflowId, id).orderByAsc(WorkflowStageDO::getStageOrder);
        List<WorkflowStageDO> originalStages = stageMapper.selectList(stageWrapper);

        // 创建新流程
        WorkflowDO newWorkflow = new WorkflowDO();
        newWorkflow.setName(originalWorkflow.getName() + " (副本)");
        newWorkflow.setDescription(originalWorkflow.getDescription());
        newWorkflow.setVersion(1);
        newWorkflow.setStatus(0); // 默认禁用
        // 不复制角色绑定
        newWorkflow.setBoundRoleId(null);
        newWorkflow.setWorkflowType(null);
        workflowMapper.insert(newWorkflow);

        // 复制所有阶段
        Map<Long, Long> stageIdMap = new HashMap<>(); // 原阶段ID -> 新阶段ID
        for (WorkflowStageDO originalStage : originalStages) {
            WorkflowStageDO newStage = new WorkflowStageDO();
            newStage.setWorkflowId(newWorkflow.getId());
            newStage.setName(originalStage.getName());
            newStage.setStageOrder(originalStage.getStageOrder());
            newStage.setApproveType(originalStage.getApproveType());
            stageMapper.insert(newStage);
            stageIdMap.put(originalStage.getId(), newStage.getId());
        }

        // 复制所有审批人（包括子流程配置）
        for (WorkflowStageDO originalStage : originalStages) {
            Long newStageId = stageIdMap.get(originalStage.getId());
            if (newStageId == null) continue;

            LambdaQueryWrapper<StageApproverDO> approverWrapper = new LambdaQueryWrapper<>();
            approverWrapper.eq(StageApproverDO::getStageId, originalStage.getId());
            List<StageApproverDO> originalApprovers = approverMapper.selectList(approverWrapper);

            for (StageApproverDO originalApprover : originalApprovers) {
                StageApproverDO newApprover = new StageApproverDO();
                newApprover.setStageId(newStageId);
                newApprover.setApproverType(originalApprover.getApproverType());
                newApprover.setApproverId(originalApprover.getApproverId());
                newApprover.setCheckSecondaryDept(originalApprover.getCheckSecondaryDept());
                newApprover.setSubWorkflowId(originalApprover.getSubWorkflowId());
                approverMapper.insert(newApprover);
            }
        }

        // 返回新流程的DTO
        return getById(newWorkflow.getId());
    }

    private void saveStages(Long workflowId, List<WorkflowStageDTO> stages) {
        if (stages == null) return;
        for (int i = 0; i < stages.size(); i++) {
            WorkflowStageDTO stageDTO = stages.get(i);
            WorkflowStageDO stage = new WorkflowStageDO();
            stage.setWorkflowId(workflowId);
            stage.setName(stageDTO.getName());
            stage.setStageOrder(i + 1);
            stage.setApproveType(stageDTO.getApproveType());
            stageMapper.insert(stage);
            // 保存审批人（包括普通审批人和子流程配置）
            if (stageDTO.getApprovers() != null) {
                for (StageApproverDTO approverDTO : stageDTO.getApprovers()) {
                    StageApproverDO approver = new StageApproverDO();
                    approver.setStageId(stage.getId());
                    approver.setApproverType(approverDTO.getApproverType() != null ? approverDTO.getApproverType() : "USER");
                    approver.setApproverId(approverDTO.getApproverId());
                    approver.setCheckSecondaryDept(approverDTO.getCheckSecondaryDept() != null ? approverDTO.getCheckSecondaryDept() : 0);
                    approver.setSubWorkflowId(approverDTO.getSubWorkflowId());
                    approverMapper.insert(approver);
                }
            }
        }
    }

    private WorkflowDTO convert(WorkflowDO entity) {
        WorkflowDTO dto = new WorkflowDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private WorkflowStageDTO convertStage(WorkflowStageDO entity) {
        WorkflowStageDTO dto = new WorkflowStageDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private StageApproverDTO convertApprover(StageApproverDO entity) {
        StageApproverDTO dto = new StageApproverDTO();
        dto.setId(entity.getId());
        dto.setStageId(entity.getStageId());
        dto.setApproverType(entity.getApproverType());
        dto.setApproverId(entity.getApproverId());
        dto.setCheckSecondaryDept(entity.getCheckSecondaryDept());
        dto.setSubWorkflowId(entity.getSubWorkflowId());
        // 根据类型查询名称
        String name = getApproverName(entity.getApproverType(), entity.getApproverId());
        dto.setApproverName(name);
        // 如果是子流程，加载子流程名称
        if (entity.getSubWorkflowId() != null) {
            WorkflowDO subWorkflow = workflowMapper.selectById(entity.getSubWorkflowId());
            if (subWorkflow != null) {
                dto.setSubWorkflowName(subWorkflow.getName());
            }
        }
        return dto;
    }

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
}
