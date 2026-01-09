package com.xuanjiao.app.service.impl;

import com.xuanjiao.app.service.WorkflowService;
import com.xuanjiao.client.dto.WorkflowDTO;
import com.xuanjiao.client.dto.WorkflowStageDTO;
import com.xuanjiao.client.dto.StageApproverDTO;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import com.xuanjiao.infrastructure.mapper.WorkflowMapper;
import com.xuanjiao.infrastructure.mapper.WorkflowStageMapper;
import com.xuanjiao.infrastructure.mapper.StageApproverMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Resource
    private WorkflowMapper workflowMapper;

    @Resource
    private WorkflowStageMapper stageMapper;

    @Resource
    private StageApproverMapper approverMapper;

    @Override
    public List<WorkflowDTO> list() {
        List<WorkflowDO> list = workflowMapper.selectList(null);
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public WorkflowDTO getById(Long id) {
        WorkflowDO workflow = workflowMapper.selectById(id);
        if (workflow == null) return null;
        WorkflowDTO dto = convert(workflow);
        LambdaQueryWrapper<WorkflowStageDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowStageDO::getWorkflowId, id).orderByAsc(WorkflowStageDO::getStageOrder);
        List<WorkflowStageDO> stages = stageMapper.selectList(wrapper);
        List<WorkflowStageDTO> stageDTOs = new ArrayList<>();
        for (WorkflowStageDO stage : stages) {
            WorkflowStageDTO stageDTO = convertStage(stage);
            // 查询该阶段的审批人
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
    public void delete(Long id) {
        workflowMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        WorkflowDO workflow = new WorkflowDO();
        workflow.setId(id);
        workflow.setStatus(status);
        workflowMapper.updateById(workflow);
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
            // 保存审批人
            if (stageDTO.getApprovers() != null) {
                for (StageApproverDTO approverDTO : stageDTO.getApprovers()) {
                    StageApproverDO approver = new StageApproverDO();
                    approver.setStageId(stage.getId());
                    // 默认为USER类型
                    approver.setApproverType(approverDTO.getApproverType() != null ? approverDTO.getApproverType() : "USER");
                    approver.setApproverId(approverDTO.getApproverId());
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
        dto.setApproverType(entity.getApproverType());
        dto.setApproverId(entity.getApproverId());
        return dto;
    }
}
