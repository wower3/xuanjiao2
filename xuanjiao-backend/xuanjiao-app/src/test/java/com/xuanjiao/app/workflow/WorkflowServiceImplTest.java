package com.xuanjiao.app.workflow;

import com.xuanjiao.app.workflow.impl.WorkflowServiceImpl;
import com.xuanjiao.client.dto.WorkflowDTO;
import com.xuanjiao.client.dto.WorkflowStageDTO;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowQuery;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.dataobject.RoleDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.ArgumentMatchers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WorkflowServiceImpl 单元测试
 * 验证 WorkflowMapper 重构后功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkflowServiceImplTest {

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private WorkflowStageMapper stageMapper;

    @Mock
    private StageApproverMapper approverMapper;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private WorkflowServiceImpl workflowService;

    private WorkflowDO testWorkflow;

    @BeforeEach
    public void setUp() {
        testWorkflow = new WorkflowDO();
        testWorkflow.setId(1L);
        testWorkflow.setName("测试流程");
        testWorkflow.setDescription("测试流程描述");
        testWorkflow.setVersion(1);
        testWorkflow.setStatus(1);
        testWorkflow.setBoundRoleId(1L);
        testWorkflow.setWorkflowType("ASSET_UPLOAD");
        testWorkflow.setDeleted(0);
        testWorkflow.setCreateTime(LocalDateTime.now());
        testWorkflow.setUpdateTime(LocalDateTime.now());
    }

    // ==================== WorkflowMapper Call Site Tests ====================

    @Test
    @Order(1)
    public void testList() {
        // 测试获取流程列表
        // This tests: workflowMapper.selectList with empty query

        when(workflowMapper.selectList(any(WorkflowQuery.class)))
                .thenReturn(Arrays.asList(testWorkflow));
        when(roleMapper.selectById(1L)).thenReturn(new RoleDO());
        when(stageMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<WorkflowDTO> result = workflowService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        // 验证 workflowMapper.selectList 被调用
        verify(workflowMapper).selectList(argThat(query ->
                query != null && "id".equals(query.getOrderByField()) &&
                "DESC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ WorkflowService.list() - workflowMapper.selectList 测试通过");
    }

    @Test
    @Order(2)
    public void testGetById() {
        // 测试根据ID获取流程
        // This tests: workflowMapper.selectById

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(roleMapper.selectById(1L)).thenReturn(new RoleDO());
        when(stageMapper.selectList(any())).thenReturn(new ArrayList<>());

        WorkflowDTO result = workflowService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // 验证 workflowMapper.selectById 被调用
        verify(workflowMapper).selectById(1L);
        System.out.println("✓ WorkflowService.getById() - workflowMapper.selectById 测试通过");
    }

    @Test
    @Order(3)
    public void testSave() {
        // 测试保存流程
        // This tests: workflowMapper.insert, selectById

        doAnswer(invocation -> {
            WorkflowDO workflow = invocation.getArgument(0);
            workflow.setId(1L);
            return null;
        }).when(workflowMapper).insert(any(WorkflowDO.class));

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(roleMapper.selectById(any())).thenReturn(null);
        when(stageMapper.selectList(any())).thenReturn(new ArrayList<>());

        WorkflowDTO dto = new WorkflowDTO();
        dto.setName("新流程");
        dto.setStages(new ArrayList<>());

        WorkflowDTO result = workflowService.save(dto);

        assertNotNull(result);
        // 验证 workflowMapper.insert 和 selectById 被调用
        verify(workflowMapper).insert(any(WorkflowDO.class));
        verify(workflowMapper).selectById(1L);
        System.out.println("✓ WorkflowService.save() - workflowMapper.insert 测试通过");
    }

    @Test
    @Order(4)
    public void testUpdate() {
        // 测试更新流程
        // This tests: workflowMapper.updateById, selectList (for old stages)

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(workflowMapper.updateById(any(WorkflowDO.class))).thenReturn(1);
        when(stageMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(approverMapper.delete(any())).thenReturn(0);
        when(stageMapper.delete(any())).thenReturn(0);
        when(stageMapper.insert(any())).thenReturn(1);

        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(1L);
        dto.setName("更新后的流程");
        dto.setStages(new ArrayList<>());

        workflowService.update(dto);

        // 验证 workflowMapper.updateById 被调用
        verify(workflowMapper).updateById(argThat(workflow ->
                workflow != null && workflow.getId() == 1L &&
                "更新后的流程".equals(workflow.getName())
        ));
        System.out.println("✓ WorkflowService.update() - workflowMapper.updateById 测试通过");
    }

    @Test
    @Order(5)
    public void testDelete() {
        // 测试删除流程
        // This tests: workflowMapper.deleteById, selectList (for stages)

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(stageMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(approverMapper.delete(any())).thenReturn(0);
        when(stageMapper.delete(any())).thenReturn(0);
        when(workflowMapper.deleteById(1L)).thenReturn(1);

        workflowService.delete(1L);

        // 验证 workflowMapper.deleteById 被调用
        verify(workflowMapper).deleteById(1L);
        System.out.println("✓ WorkflowService.delete() - workflowMapper.deleteById 测试通过");
    }

    @Test
    @Order(6)
    public void testUpdateStatus_EnableWithConflict() {
        // 测试启用流程（有冲突）
        // This tests: workflowMapper.selectById, selectList with conflict check query

        WorkflowDO conflictingWorkflow = new WorkflowDO();
        conflictingWorkflow.setId(2L);
        conflictingWorkflow.setName("冲突流程");

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(workflowMapper.selectList(argThat(query ->
                query != null && query.getBoundRoleId() != null &&
                query.getWorkflowType() != null &&
                query.getExcludeIds() != null && !query.getExcludeIds().isEmpty()
        ))).thenReturn(Arrays.asList(conflictingWorkflow));

        // 应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            workflowService.updateStatus(1L, 1);
        });

        // 验证 selectById 和 selectList 被调用
        verify(workflowMapper).selectById(1L);
        verify(workflowMapper).selectList(argThat(query ->
                query != null &&
                query.getBoundRoleId() == 1L &&
                "ASSET_UPLOAD".equals(query.getWorkflowType()) &&
                query.getStatus() == 1 &&
                query.getDeleted() == 0 &&
                query.getExcludeIds().contains(1L)
        ));
        System.out.println("✓ WorkflowService.updateStatus(conflict) - workflowMapper.selectList 测试通过");
    }

    @Test
    @Order(7)
    public void testUpdateStatus_EnableWithoutConflict() {
        // 测试启用流程（无冲突）
        // This tests: workflowMapper.selectById, selectList, updateById

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(workflowMapper.selectList(argThat(query ->
                query != null && query.getExcludeIds() != null
        ))).thenReturn(new ArrayList<>());
        when(workflowMapper.updateById(any(WorkflowDO.class))).thenReturn(1);

        workflowService.updateStatus(1L, 1);

        // 验证 workflowMapper.updateById 被调用
        verify(workflowMapper).updateById(argThat(workflow ->
                workflow != null && workflow.getId() == 1L &&
                workflow.getStatus() == 1
        ));
        System.out.println("✓ WorkflowService.updateStatus(no conflict) - workflowMapper 测试通过");
    }

    @Test
    @Order(8)
    public void testBindRole_WithConflict() {
        // 测试绑定角色（有冲突）
        // This tests: workflowMapper.selectById, selectList with conflict check query

        WorkflowDO conflictingWorkflow = new WorkflowDO();
        conflictingWorkflow.setId(2L);
        conflictingWorkflow.setName("冲突流程");

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(workflowMapper.selectList(argThat(query ->
                query != null && query.getBoundRoleId() != null &&
                query.getWorkflowType() != null &&
                query.getExcludeIds() != null
        ))).thenReturn(Arrays.asList(conflictingWorkflow));

        // 应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            workflowService.bindRole(1L, 1L, "ASSET_UPLOAD");
        });

        // 验证冲突检查查询
        verify(workflowMapper).selectList(argThat(query ->
                query != null &&
                query.getBoundRoleId() == 1L &&
                "ASSET_UPLOAD".equals(query.getWorkflowType()) &&
                query.getStatus() == 1 &&
                query.getDeleted() == 0 &&
                query.getExcludeIds().contains(1L)
        ));
        System.out.println("✓ WorkflowService.bindRole(conflict) - workflowMapper.selectList 测试通过");
    }

    @Test
    @Order(9)
    public void testBindRole_WithoutConflict() {
        // 测试绑定角色（无冲突）
        // This tests: workflowMapper.selectById, selectList, updateById

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(workflowMapper.selectList(argThat(query ->
                query != null && query.getExcludeIds() != null
        ))).thenReturn(new ArrayList<>());
        when(workflowMapper.updateById(any(WorkflowDO.class))).thenReturn(1);

        workflowService.bindRole(1L, 1L, "ASSET_UPLOAD");

        // 验证 workflowMapper.updateById 被调用
        verify(workflowMapper).updateById(argThat(workflow ->
                workflow != null && workflow.getId() == 1L &&
                workflow.getBoundRoleId() == 1L &&
                "ASSET_UPLOAD".equals(workflow.getWorkflowType()) &&
                workflow.getStatus() == 1
        ));
        System.out.println("✓ WorkflowService.bindRole(no conflict) - workflowMapper 测试通过");
    }

    @Test
    @Order(10)
    public void testUnbindRole() {
        // 测试解绑角色
        // This tests: workflowMapper.updateById

        when(workflowMapper.updateById(any(WorkflowDO.class))).thenReturn(1);

        workflowService.unbindRole(1L);

        // 验证 workflowMapper.updateById 被调用
        verify(workflowMapper).updateById(argThat(workflow ->
                workflow != null && workflow.getId() == 1L &&
                workflow.getBoundRoleId() == null &&
                workflow.getWorkflowType() == null
        ));
        System.out.println("✓ WorkflowService.unbindRole() - workflowMapper.updateById 测试通过");
    }

    @Test
    @Order(11)
    public void testCopy() {
        // 测试复制流程
        // This tests: workflowMapper.selectById, selectList, insert

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(stageMapper.selectList(any())).thenReturn(new ArrayList<>());
        doAnswer(invocation -> {
            WorkflowDO workflow = invocation.getArgument(0);
            workflow.setId(2L);
            return null;
        }).when(workflowMapper).insert(any(WorkflowDO.class));
        when(stageMapper.insert(any())).thenReturn(1);
        when(approverMapper.selectList(any())).thenReturn(new ArrayList<>());

        when(workflowMapper.selectById(2L)).thenReturn(testWorkflow);
        when(roleMapper.selectById(any())).thenReturn(null);

        WorkflowDTO result = workflowService.copy(1L);

        assertNotNull(result);
        // 验证 workflowMapper.insert 被调用
        verify(workflowMapper).insert(argThat(workflow ->
                workflow != null && workflow.getName().contains("副本")
        ));
        System.out.println("✓ WorkflowService.copy() - workflowMapper.insert 测试通过");
    }

    // ==================== WorkflowStageMapper Call Site Tests ====================

    @Test
    @Order(12)
    public void testList_WorkflowStageQuery() {
        // 测试获取流程列表 - 验证 stageMapper.selectList 调用
        // This tests: list() -> stageMapper.selectList for each workflow's stages

        when(workflowMapper.selectList(any(WorkflowQuery.class)))
                .thenReturn(Arrays.asList(testWorkflow));
        when(roleMapper.selectById(1L)).thenReturn(new RoleDO());

        // Mock stageMapper.selectList 返回阶段列表
        WorkflowStageDO stage1 = new WorkflowStageDO();
        stage1.setId(1L);
        stage1.setWorkflowId(1L);
        stage1.setName("第一阶段");
        stage1.setStageOrder(1);
        stage1.setApproveType("OR");

        when(stageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(stage1));
        when(approverMapper.selectList(any())).thenReturn(new ArrayList<>());

        List<WorkflowDTO> result = workflowService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        // 验证 stageMapper.selectList 被正确调用
        verify(stageMapper).selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ WorkflowService.list() - stageMapper.selectList 测试通过");
    }

    @Test
    @Order(13)
    public void testGetById_WorkflowStageQuery() {
        // 测试根据ID获取流程 - 验证 stageMapper.selectList 调用
        // This tests: getById() -> stageMapper.selectList for workflow stages

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(roleMapper.selectById(1L)).thenReturn(new RoleDO());

        // Mock stageMapper.selectList 返回阶段列表
        WorkflowStageDO stage1 = new WorkflowStageDO();
        stage1.setId(1L);
        stage1.setWorkflowId(1L);
        stage1.setName("第一阶段");
        stage1.setStageOrder(1);

        when(stageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(stage1));
        when(approverMapper.selectList(any())).thenReturn(new ArrayList<>());

        WorkflowDTO result = workflowService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // 验证 stageMapper.selectList 被正确调用
        verify(stageMapper).selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ WorkflowService.getById() - stageMapper.selectList 测试通过");
    }

    @Test
    @Order(14)
    public void testList_StageApproverQuery() {
        // 测试获取流程列表 - 验证 approverMapper.selectList 调用
        // This tests: list() -> approverMapper.selectList for stage approvers

        when(workflowMapper.selectList(any())).thenReturn(Arrays.asList(testWorkflow));
        when(roleMapper.selectById(1L)).thenReturn(new RoleDO());

        // Mock stageMapper.selectList 返回阶段列表
        WorkflowStageDO stage1 = new WorkflowStageDO();
        stage1.setId(1L);
        stage1.setWorkflowId(1L);
        stage1.setName("第一阶段");
        stage1.setStageOrder(1);

        when(stageMapper.selectList(any())).thenReturn(Arrays.asList(stage1));

        // Mock approverMapper.selectList 返回审批人列表
        when(approverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 1L
        ))).thenReturn(new ArrayList<>());

        List<WorkflowDTO> result = workflowService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        // 验证 approverMapper.selectList 被正确调用
        verify(approverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 1L
        ));
        System.out.println("✓ WorkflowService.list() - approverMapper.selectList 测试通过");
    }

    @Test
    @Order(15)
    public void testGetById_StageApproverQuery() {
        // 测试根据ID获取流程 - 验证 approverMapper.selectList 调用
        // This tests: getById() -> approverMapper.selectList for stage approvers

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(roleMapper.selectById(1L)).thenReturn(new RoleDO());

        // Mock stageMapper.selectList 返回阶段列表
        WorkflowStageDO stage1 = new WorkflowStageDO();
        stage1.setId(1L);
        stage1.setWorkflowId(1L);
        stage1.setName("第一阶段");
        stage1.setStageOrder(1);

        when(stageMapper.selectList(any())).thenReturn(Arrays.asList(stage1));

        // Mock approverMapper.selectList 返回审批人列表
        when(approverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 1L
        ))).thenReturn(new ArrayList<>());

        WorkflowDTO result = workflowService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // 验证 approverMapper.selectList 被正确调用
        verify(approverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 1L
        ));
        System.out.println("✓ WorkflowService.getById() - approverMapper.selectList 测试通过");
    }

    @Test
    @Order(16)
    public void testCopy_StageApproverQuery() {
        // 测试复制流程 - 验证 approverMapper.selectList 调用
        // This tests: copy() -> approverMapper.selectList for copying approvers

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);

        // Mock for getById call at the end of copy method (new workflow)
        WorkflowDO newWorkflow = new WorkflowDO();
        newWorkflow.setId(2L);
        newWorkflow.setName("测试流程 (副本)");
        newWorkflow.setDescription("测试流程描述");
        newWorkflow.setVersion(1);
        newWorkflow.setStatus(0);
        newWorkflow.setDeleted(0);
        when(workflowMapper.selectById(2L)).thenReturn(newWorkflow);

        // Mock stageMapper.selectList 返回原流程的阶段列表
        WorkflowStageDO originalStage = new WorkflowStageDO();
        originalStage.setId(100L);
        originalStage.setWorkflowId(1L);
        originalStage.setName("原阶段");
        originalStage.setStageOrder(1);

        // Mock for copy operation
        when(stageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L
        ))).thenReturn(Arrays.asList(originalStage));

        // Mock approverMapper.selectList 返回原审批人列表
        when(approverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 100L
        ))).thenReturn(new ArrayList<>());

        // Mock insert operations
        when(workflowMapper.insert(any())).thenAnswer(invocation -> {
            WorkflowDO workflow = invocation.getArgument(0);
            workflow.setId(2L);
            return 1;
        });
        when(stageMapper.insert(any())).thenAnswer(invocation -> {
            WorkflowStageDO stage = invocation.getArgument(0);
            stage.setId(200L);
            return 1;
        });

        // Mock for getById call at the end of copy method (new stages)
        WorkflowStageDO newStage = new WorkflowStageDO();
        newStage.setId(200L);
        newStage.setWorkflowId(2L);
        newStage.setName("原阶段");
        newStage.setStageOrder(1);
        when(stageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 2L
        ))).thenReturn(Arrays.asList(newStage));
        when(approverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 200L
        ))).thenReturn(new ArrayList<>());

        WorkflowDTO result = workflowService.copy(1L);

        assertNotNull(result);
        assertTrue(result.getName().contains("副本"));
        // 验证 approverMapper.selectList 被正确调用（用于复制原审批人）
        verify(approverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 100L
        ));
        System.out.println("✓ WorkflowService.copy() - approverMapper.selectList 测试通过");
    }

    @Test
    @Order(17)
    public void testUpdate_StageApproverDelete() {
        // 测试更新流程 - 验证 approverMapper.delete 调用
        // This tests: update() -> approverMapper.delete for removing old approvers

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(workflowMapper.updateById(any())).thenReturn(1);
        when(roleMapper.selectById(1L)).thenReturn(new RoleDO());

        // Mock stageMapper.selectList 返回旧的阶段列表
        WorkflowStageDO oldStage = new WorkflowStageDO();
        oldStage.setId(100L);
        oldStage.setWorkflowId(1L);
        oldStage.setName("旧阶段");
        oldStage.setStageOrder(1);

        when(stageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L
        ))).thenReturn(Arrays.asList(oldStage));

        // Mock approverMapper.delete
        when(approverMapper.delete(any())).thenReturn(1);
        when(stageMapper.delete(any())).thenReturn(1);
        when(stageMapper.insert(any())).thenReturn(1);

        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(1L);
        dto.setName("更新后的流程");
        dto.setStages(new ArrayList<>());

        workflowService.update(dto);

        // 验证 approverMapper.delete 被调用
        verify(approverMapper).delete(any());
        System.out.println("✓ WorkflowService.update() - approverMapper.delete 测试通过");
    }

    @Test
    @Order(18)
    public void testDelete_StageApproverDelete() {
        // 测试删除流程 - 验证 approverMapper.delete 调用
        // This tests: delete() -> approverMapper.delete for removing approvers

        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(workflowMapper.deleteById(1L)).thenReturn(1);

        // Mock stageMapper.selectList 返回阶段列表
        WorkflowStageDO stage = new WorkflowStageDO();
        stage.setId(100L);
        stage.setWorkflowId(1L);
        stage.setName("阶段");
        stage.setStageOrder(1);

        when(stageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L
        ))).thenReturn(Arrays.asList(stage));

        // Mock approverMapper.delete
        when(approverMapper.delete(any())).thenReturn(1);
        when(stageMapper.delete(any())).thenReturn(1);

        workflowService.delete(1L);

        // 验证 approverMapper.delete 被正确调用
        verify(approverMapper).delete(any());
        System.out.println("✓ WorkflowService.delete() - approverMapper.delete 测试通过");
    }
}
