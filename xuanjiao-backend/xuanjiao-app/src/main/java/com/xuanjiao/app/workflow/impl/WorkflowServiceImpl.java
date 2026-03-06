package com.xuanjiao.app.workflow.impl;

import com.xuanjiao.app.workflow.WorkflowService;
import com.xuanjiao.client.workflow.WorkflowDTO;
import com.xuanjiao.client.workflow.WorkflowStageDTO;
import com.xuanjiao.client.workflow.StageApproverDTO;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowQuery;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverWithDetailsDO;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.common.ConvertUtils;
import com.xuanjiao.common.exception.BusinessException;
import com.xuanjiao.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 工作流定义服务实现类
 * <p>实现WorkflowService接口，封装工作流定义管理逻辑</p>
 * <p>核心功能：工作流CRUD、状态管理、角色绑定</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.workflow.WorkflowService
 */
@Service
public class WorkflowServiceImpl implements WorkflowService {

    /** 审批人类型常量 */
    private static final String APPROVER_TYPE_USER = "USER";
    private static final String APPROVER_TYPE_ROLE = "ROLE";
    private static final String APPROVER_TYPE_DEPT = "DEPT";

    /** 排序方向常量 */
    private static final String ORDER_ASC = "ASC";
    private static final String ORDER_DESC = "DESC";

    /** 流程类型常量 */
    private static final String WORKFLOW_TYPE_ASSET_UPLOAD = "ASSET_UPLOAD";
    private static final String WORKFLOW_TYPE_ASSET_USAGE = "ASSET_USAGE";

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
        WorkflowQuery query = new WorkflowQuery();
        query.setOrderByField("id");
        query.setOrderByDirection(ORDER_DESC);
        // 使用JOIN查询一次性获取流程列表和角色名称，不需要加载阶段和审批人
        List<WorkflowDO> list = workflowMapper.selectListWithRoleName(query);
        return list.stream().map(workflow -> {
            WorkflowDTO dto = convert(workflow);
            // 从JOIN结果直接获取角色名称，无需单独查询
            dto.setRoleName(workflow.getRoleName());
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
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(id);
        stageQuery.setOrderByField("stage_order");
        stageQuery.setOrderByDirection(ORDER_ASC);
        List<WorkflowStageDO> stages = stageMapper.selectList(stageQuery);

        // 批量查询所有阶段的审批人（优化N+1问题）
        List<Long> stageIds = stages.stream().map(WorkflowStageDO::getId).collect(Collectors.toList());
        Map<Long, List<StageApproverWithDetailsDO>> approversByStageId = new HashMap<>();
        if (!stageIds.isEmpty()) {
            // 使用JOIN查询一次性获取所有审批人及其详情
            StageApproverQuery approverQuery = new StageApproverQuery();
            approverQuery.setStageIds(stageIds);
            List<StageApproverWithDetailsDO> allApprovers = approverMapper.selectWithDetails(approverQuery);

            // 按stageId分组
            for (StageApproverWithDetailsDO approver : allApprovers) {
                approversByStageId.computeIfAbsent(approver.getStageId(), k -> new ArrayList<>()).add(approver);
            }
        }

        List<WorkflowStageDTO> stageDTOs = new ArrayList<>();
        for (WorkflowStageDO stage : stages) {
            WorkflowStageDTO stageDTO = convertStage(stage);
            // 使用预加载的审批人数据进行转换
            List<StageApproverWithDetailsDO> approvers = approversByStageId.getOrDefault(stage.getId(), new ArrayList<>());
            stageDTO.setApprovers(approvers.stream().map(this::convertApproverWithDetails).collect(Collectors.toList()));
            stageDTOs.add(stageDTO);
        }
        dto.setStages(stageDTOs);
        return dto;
    }

    @Override
    @Transactional
    public WorkflowDTO save(WorkflowDTO dto) {
        WorkflowDO workflow = new WorkflowDO();
        ConvertUtils.copyProperties(dto, workflow);
        workflowMapper.insert(workflow);
        saveStages(workflow.getId(), dto.getStages());
        // 返回新创建的流程（包含ID）
        return getById(workflow.getId());
    }

    @Override
    @Transactional
    public void update(WorkflowDTO dto) {
        WorkflowDO workflow = new WorkflowDO();
        ConvertUtils.copyProperties(dto, workflow);
        workflowMapper.updateById(workflow);
        // 先查询旧的阶段ID
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(dto.getId());
        List<WorkflowStageDO> oldStages = stageMapper.selectList(stageQuery);
        // 删除旧的审批人
        for (WorkflowStageDO oldStage : oldStages) {
            StageApproverQuery approverQuery = new StageApproverQuery();
            approverQuery.setStageId(oldStage.getId());
            approverMapper.delete(approverQuery);
        }
        // 删除旧的阶段
        WorkflowStageQuery deleteQuery = new WorkflowStageQuery();
        deleteQuery.setWorkflowId(dto.getId());
        stageMapper.delete(deleteQuery);
        // 保存新的阶段和审批人
        saveStages(dto.getId(), dto.getStages());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 先删除阶段和审批人
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(id);
        List<WorkflowStageDO> stages = stageMapper.selectList(stageQuery);
        for (WorkflowStageDO stage : stages) {
            // 删除该阶段的审批人
            StageApproverQuery approverQuery = new StageApproverQuery();
            approverQuery.setStageId(stage.getId());
            approverMapper.delete(approverQuery);
        }
        // 删除所有阶段
        WorkflowStageQuery deleteQuery = new WorkflowStageQuery();
        deleteQuery.setWorkflowId(id);
        stageMapper.delete(deleteQuery);
        // 最后删除流程
        workflowMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        // 获取当前流程
        WorkflowDO currentWorkflow = workflowMapper.selectById(id);
        if (currentWorkflow == null) {
            throw new NotFoundException("流程不存在");
        }

        // 如果是启用操作，检查是否有冲突
        if (status == 1) {
            // 检查是否有其他同角色+流程类型的已启用流程
            if (currentWorkflow.getBoundRoleId() != null && currentWorkflow.getWorkflowType() != null) {
                WorkflowQuery query = new WorkflowQuery();
                query.setBoundRoleId(currentWorkflow.getBoundRoleId());
                query.setWorkflowType(currentWorkflow.getWorkflowType());
                query.setStatus(1);
                query.setDeleted(0);
                query.setExcludeIds(Arrays.asList(id));

                List<WorkflowDO> conflictingWorkflows = workflowMapper.selectList(query);
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
                    throw new BusinessException(errorMsg.toString());
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
        if (WORKFLOW_TYPE_ASSET_UPLOAD.equals(workflowType)) {
            return "素材录入";
        } else if (WORKFLOW_TYPE_ASSET_USAGE.equals(workflowType)) {
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
            throw new NotFoundException("流程不存在");
        }

        // 检查是否有其他同角色+流程类型的已启用流程
        WorkflowQuery query = new WorkflowQuery();
        query.setBoundRoleId(roleId);
        query.setWorkflowType(workflowType);
        query.setStatus(1);
        query.setDeleted(0);
        query.setExcludeIds(Arrays.asList(id));

        List<WorkflowDO> conflictingWorkflows = workflowMapper.selectList(query);
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
            throw new BusinessException(errorMsg.toString());
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
            throw new NotFoundException("原流程不存在");
        }

        // 获取原流程的所有阶段
        WorkflowStageQuery stageQuery = new WorkflowStageQuery();
        stageQuery.setWorkflowId(id);
        stageQuery.setOrderByField("stage_order");
        stageQuery.setOrderByDirection(ORDER_ASC);
        List<WorkflowStageDO> originalStages = stageMapper.selectList(stageQuery);

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

            StageApproverQuery approverQuery = new StageApproverQuery();
            approverQuery.setStageId(originalStage.getId());
            List<StageApproverDO> originalApprovers = approverMapper.selectList(approverQuery);

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
                    approver.setApproverType(approverDTO.getApproverType() != null ? approverDTO.getApproverType() : APPROVER_TYPE_USER);
                    approver.setApproverId(approverDTO.getApproverId());
                    approver.setCheckSecondaryDept(approverDTO.getCheckSecondaryDept() != null ? approverDTO.getCheckSecondaryDept() : 0);
                    approver.setSubWorkflowId(approverDTO.getSubWorkflowId());
                    approverMapper.insert(approver);
                }
            }
        }
    }

    private WorkflowDTO convert(WorkflowDO entity) {
        return ConvertUtils.copyProperties(entity, WorkflowDTO.class);
    }

    private WorkflowStageDTO convertStage(WorkflowStageDO entity) {
        return ConvertUtils.copyProperties(entity, WorkflowStageDTO.class);
    }

    /**
     * 使用预加载的详情转换审批人（优化N+1问题）
     */
    private StageApproverDTO convertApproverWithDetails(StageApproverWithDetailsDO entity) {
        StageApproverDTO dto = new StageApproverDTO();
        dto.setId(entity.getId());
        dto.setStageId(entity.getStageId());
        dto.setApproverType(entity.getApproverType());
        dto.setApproverId(entity.getApproverId());
        dto.setCheckSecondaryDept(entity.getCheckSecondaryDept());
        dto.setSubWorkflowId(entity.getSubWorkflowId());
        // 直接从JOIN结果获取名称，无需额外查询
        String name = getApproverNameFromDetails(entity);
        dto.setApproverName(name);
        // 直接从JOIN结果获取子流程名称
        dto.setSubWorkflowName(entity.getSubWorkflowName());
        return dto;
    }

    /**
     * 从JOIN详情结果中获取审批人名称
     */
    private String getApproverNameFromDetails(StageApproverWithDetailsDO entity) {
        if (APPROVER_TYPE_USER.equals(entity.getApproverType())) {
            String displayName = entity.getRealName() != null ? entity.getRealName() : entity.getUsername();
            return "[用户] " + (displayName != null ? displayName : "未知");
        } else if (APPROVER_TYPE_ROLE.equals(entity.getApproverType())) {
            return "[角色] " + (entity.getRoleName() != null ? entity.getRoleName() : "未知");
        } else if (APPROVER_TYPE_DEPT.equals(entity.getApproverType())) {
            return "[部门] " + (entity.getDeptName() != null ? entity.getDeptName() : "未知");
        }
        return "未知";
    }

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
}
