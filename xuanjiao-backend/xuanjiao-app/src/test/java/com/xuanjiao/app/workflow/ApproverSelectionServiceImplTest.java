package com.xuanjiao.app.workflow;

import com.xuanjiao.app.user.UserService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.app.workflow.impl.ApproverSelectionServiceImpl;
import com.xuanjiao.client.dto.ApproverSelectionDTO;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.approval.ApprovalProgressMapper;
import com.xuanjiao.infrastructure.approval.ApprovalTaskMapper;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverQuery;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowQuery;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ApproverSelectionServiceImpl 单元测试
 * 验证 UserMapper 和 WorkflowStageMapper 重构后功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApproverSelectionServiceImplTest {

    @Mock
    private WorkflowStageMapper workflowStageMapper;

    @Mock
    private StageApproverMapper stageApproverMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private DeptMapper deptMapper;

    @Mock
    private ApprovalTaskMapper approvalTaskMapper;

    @Mock
    private ApprovalInstanceMapper approvalInstanceMapper;

    @Mock
    private ApprovalProgressMapper approvalProgressMapper;

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private UserService userService;

    @Mock
    private WorkflowEngineService workflowEngineService;

    @InjectMocks
    private ApproverSelectionServiceImpl approverSelectionService;

    private WorkflowStageDO testStage;
    private StageApproverDO userApprover;
    private StageApproverDO roleApprover;
    private StageApproverDO deptApprover;
    private UserDO testUser;

    @BeforeEach
    public void setUp() {
        testStage = new WorkflowStageDO();
        testStage.setId(1L);
        testStage.setName("第一阶段");

        testUser = new UserDO();
        testUser.setId(1L);
        testUser.setUsername("test_user");
        testUser.setRealName("测试用户");
        testUser.setDeptId(100L);
        testUser.setRoleId(1L);
        testUser.setStatus(1);

        // 指定用户类型的审批人
        userApprover = new StageApproverDO();
        userApprover.setId(1L);
        userApprover.setStageId(1L);
        userApprover.setApproverType("USER");
        userApprover.setApproverId(1L);

        // 指定角色类型的审批人
        roleApprover = new StageApproverDO();
        roleApprover.setId(2L);
        roleApprover.setStageId(1L);
        roleApprover.setApproverType("ROLE");
        roleApprover.setApproverId(1L);
        roleApprover.setCheckSecondaryDept(0);

        // 指定部门类型的审批人
        deptApprover = new StageApproverDO();
        deptApprover.setId(3L);
        deptApprover.setStageId(1L);
        deptApprover.setApproverType("DEPT");
        deptApprover.setApproverId(100L);
    }

    // ==================== UserMapper Call Site Tests ====================

    @Test
    @Order(1)
    public void testGetNextStageApprovers_WithUserIds() {
        // 测试按用户ID查询审批人
        // This tests: UserQuery with userIds at line 113-118

        when(workflowStageMapper.selectById(1L)).thenReturn(testStage);
        when(stageApproverMapper.selectList(any(StageApproverQuery.class)))
                .thenReturn(Arrays.asList(userApprover));
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        List<ApproverSelectionDTO> result = approverSelectionService.getNextStageApprovers(1L, 1L, 1L, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify UserQuery was called with userIds
        verify(userMapper).selectList(argThat(query ->
                query != null && query.getUserIds() != null && query.getUserIds().contains(1L)
        ));
        System.out.println("✓ ApproverSelectionService.getNextStageApprovers(userIds) - UserMapper测试通过");
    }

    @Test
    @Order(2)
    public void testGetNextStageApprovers_WithRoleIds() {
        // 测试按角色ID查询审批人
        // This tests: UserQuery with roleId at line 136-151

        when(workflowStageMapper.selectById(1L)).thenReturn(testStage);
        when(stageApproverMapper.selectList(any(StageApproverQuery.class)))
                .thenReturn(Arrays.asList(roleApprover));
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        List<ApproverSelectionDTO> result = approverSelectionService.getNextStageApprovers(1L, 1L, 1L, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify UserQuery was called with roleId
        verify(userMapper).selectList(argThat(query ->
                query != null && query.getRoleId() != null && query.getRoleId() == 1L
        ));
        System.out.println("✓ ApproverSelectionService.getNextStageApprovers(roleId) - UserMapper测试通过");
    }

    @Test
    @Order(3)
    public void testGetNextStageApprovers_WithDeptId() {
        // 测试按部门ID查询审批人
        // This tests: UserQuery with deptId at line 165-172

        when(workflowStageMapper.selectById(1L)).thenReturn(testStage);
        when(stageApproverMapper.selectList(any(StageApproverQuery.class)))
                .thenReturn(Arrays.asList(deptApprover));
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        List<ApproverSelectionDTO> result = approverSelectionService.getNextStageApprovers(1L, 1L, 1L, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify UserQuery was called with deptId
        verify(userMapper).selectList(argThat(query ->
                query != null && query.getDeptId() != null && query.getDeptId() == 100L
        ));
        System.out.println("✓ ApproverSelectionService.getNextStageApprovers(deptId) - UserMapper测试通过");
    }

    @Test
    @Order(4)
    public void testGetNextStageApprovers_WithKeyword() {
        // 测试按关键词搜索
        // This tests: UserQuery with userIds + keyword at line 113-118

        when(workflowStageMapper.selectById(1L)).thenReturn(testStage);
        when(stageApproverMapper.selectList(any(StageApproverQuery.class)))
                .thenReturn(Arrays.asList(userApprover));
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        List<ApproverSelectionDTO> result = approverSelectionService.getNextStageApprovers(1L, 1L, 1L, "test");

        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify UserQuery was called with userIds and keyword
        verify(userMapper).selectList(argThat(query ->
                query != null && query.getUserIds() != null && "test".equals(query.getKeyword())
        ));
        System.out.println("✓ ApproverSelectionService.getNextStageApprovers(keyword) - UserMapper测试通过");
    }

    @Test
    @Order(5)
    public void testGetNextStageApprovers_WithRoleAndDeptIds() {
        // 测试按角色+二级部门查询审批人
        // This tests: UserQuery with roleId + deptIds at line 136-151

        roleApprover.setCheckSecondaryDept(1);

        // Create a dept for the user
        DeptDO userDept = new DeptDO();
        userDept.setId(100L);
        userDept.setName("用户部门");
        userDept.setLevel(2); // Secondary dept

        when(workflowStageMapper.selectById(1L)).thenReturn(testStage);
        when(stageApproverMapper.selectList(any(StageApproverQuery.class)))
                .thenReturn(Arrays.asList(roleApprover));
        when(userMapper.selectById(1L)).thenReturn(testUser); // For getSecondaryDeptId
        when(deptMapper.selectById(100L)).thenReturn(userDept); // User's dept is level 2
        when(deptMapper.selectByParentId(100L)).thenReturn(Arrays.asList()); // No sub-depts
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        List<ApproverSelectionDTO> result = approverSelectionService.getNextStageApprovers(1L, 1L, 1L, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify UserQuery was called with roleId and deptIds (deptIds contains the secondary dept)
        verify(userMapper).selectList(argThat(query ->
                query != null && query.getRoleId() != null && query.getDeptIds() != null && query.getDeptIds().contains(100L)
        ));
        System.out.println("✓ ApproverSelectionService.getNextStageApprovers(roleId+deptIds) - UserMapper测试通过");
    }

    // ==================== WorkflowMapper Call Site Tests ====================

    @Test
    @Order(6)
    public void testGetWorkflowByRole_Found() {
        // 测试根据角色和流程类型获取工作流（找到）
        // This tests: workflowMapper.selectList in getWorkflowByRole()

        WorkflowDO testWorkflow = new WorkflowDO();
        testWorkflow.setId(1L);
        testWorkflow.setName("素材录入审批流程");
        testWorkflow.setBoundRoleId(1L);
        testWorkflow.setWorkflowType("ASSET_UPLOAD");
        testWorkflow.setStatus(1);

        when(workflowMapper.selectList(argThat(query ->
                query != null && query.getBoundRoleId() == 1L &&
                "ASSET_UPLOAD".equals(query.getWorkflowType()) &&
                query.getStatus() == 1 &&
                query.getDeleted() == 0
        ))).thenReturn(Arrays.asList(testWorkflow));

        // Mock selectById for getWorkflowDetails() internal call
        when(workflowMapper.selectById(1L)).thenReturn(testWorkflow);
        when(workflowStageMapper.selectList(any())).thenReturn(new ArrayList<>());

        com.xuanjiao.client.dto.WorkflowDTO result = approverSelectionService.getWorkflowByRole(1L, "ASSET_UPLOAD");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // 验证 workflowMapper.selectList 被调用
        verify(workflowMapper).selectList(argThat(query ->
                query != null &&
                query.getBoundRoleId() == 1L &&
                "ASSET_UPLOAD".equals(query.getWorkflowType()) &&
                query.getStatus() == 1 &&
                query.getDeleted() == 0
        ));
        System.out.println("✓ ApproverSelectionService.getWorkflowByRole(found) - WorkflowMapper测试通过");
    }

    @Test
    @Order(7)
    public void testGetWorkflowByRole_NotFound() {
        // 测试根据角色和流程类型获取工作流（未找到）
        // This tests: workflowMapper.selectList in getWorkflowByRole() returns empty

        when(workflowMapper.selectList(any(WorkflowQuery.class)))
                .thenReturn(new ArrayList<>());

        com.xuanjiao.client.dto.WorkflowDTO result = approverSelectionService.getWorkflowByRole(999L, "UNKNOWN_TYPE");

        assertNull(result);
        // 验证 workflowMapper.selectList 被调用
        verify(workflowMapper).selectList(argThat(query ->
                query != null &&
                query.getBoundRoleId() == 999L &&
                "UNKNOWN_TYPE".equals(query.getWorkflowType()) &&
                query.getStatus() == 1 &&
                query.getDeleted() == 0
        ));
        System.out.println("✓ ApproverSelectionService.getWorkflowByRole(not found) - WorkflowMapper测试通过");
    }

    // ==================== WorkflowStageMapper Call Site Tests ====================

    @Test
    @Order(8)
    public void testGetMainWorkflowProgress_WorkflowStageQuery() {
        // 测试获取主流程进度 - 验证 WorkflowStageMapper.selectList 调用
        // This tests: getApprovalProgress() -> getMainWorkflowProgress() -> workflowStageMapper.selectList

        ApprovalInstanceDO instance = new ApprovalInstanceDO();
        instance.setId(1L);
        instance.setWorkflowId(1L);
        instance.setParentInstanceId(null); // 主流程

        when(approvalInstanceMapper.selectById(1L)).thenReturn(instance);
        when(approvalProgressMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(approvalInstanceMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(userMapper.selectList(any(UserQuery.class))).thenReturn(new ArrayList<>());

        // Mock workflowStageMapper.selectList 返回阶段列表
        WorkflowStageDO stage1 = new WorkflowStageDO();
        stage1.setId(1L);
        stage1.setWorkflowId(1L);
        stage1.setName("第一阶段");
        stage1.setStageOrder(1);

        when(workflowStageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(stage1));

        List<com.xuanjiao.client.dto.ApprovalProgressDTO> result =
                approverSelectionService.getApprovalProgress(1L);

        assertNotNull(result);
        // 验证 workflowStageMapper.selectList 被正确调用
        verify(workflowStageMapper).selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ ApproverSelectionService.getApprovalProgress(main) - WorkflowStageMapper测试通过");
    }

    @Test
    @Order(9)
    public void testGetSubWorkflowProgress_WorkflowStageQuery() {
        // 测试获取子流程进度 - 验证 WorkflowStageMapper.selectList 调用
        // This tests: getApprovalProgress() -> getSubWorkflowProgress() -> workflowStageMapper.selectList

        ApprovalInstanceDO subInstance = new ApprovalInstanceDO();
        subInstance.setId(2L);
        subInstance.setWorkflowId(2L);
        subInstance.setParentInstanceId(1L); // 子流程

        when(approvalInstanceMapper.selectById(2L)).thenReturn(subInstance);
        when(approvalProgressMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Mock workflowStageMapper.selectList 返回子流程阶段列表
        WorkflowStageDO subStage1 = new WorkflowStageDO();
        subStage1.setId(10L);
        subStage1.setWorkflowId(2L);
        subStage1.setName("子流程第一阶段");
        subStage1.setStageOrder(1);

        when(workflowStageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() != null &&
                query.getWorkflowId() == 2L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(subStage1));

        List<com.xuanjiao.client.dto.ApprovalProgressDTO> result =
                approverSelectionService.getApprovalProgress(2L);

        assertNotNull(result);
        // 验证 workflowStageMapper.selectList 被正确调用
        verify(workflowStageMapper).selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 2L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ ApproverSelectionService.getApprovalProgress(sub) - WorkflowStageMapper测试通过");
    }

    @Test
    @Order(8)
    public void testGetFirstStageApprovers_StageApproverQuery() {
        // 测试获取第一阶段审批人 - 验证 stageApproverMapper.selectList 调用
        // This tests: getFirstStageApprovers() -> stageApproverMapper.selectList

        // Mock workflow and first stage
        WorkflowDO workflow = new WorkflowDO();
        workflow.setId(1L);
        workflow.setName("测试流程");

        when(workflowMapper.selectById(1L)).thenReturn(workflow);

        WorkflowStageDO firstStage = new WorkflowStageDO();
        firstStage.setId(1L);
        firstStage.setWorkflowId(1L);
        firstStage.setName("第一阶段");
        firstStage.setStageOrder(1);

        when(workflowStageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(firstStage));

        // Mock stageApproverMapper.selectList 返回审批人列表
        when(stageApproverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 1L
        ))).thenReturn(new ArrayList<>());

        approverSelectionService.getFirstStageApprovers(1L, 1L, null);

        // 验证 stageApproverMapper.selectList 被正确调用
        verify(stageApproverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 1L
        ));
        System.out.println("✓ ApproverSelectionService.getFirstStageApprovers() - stageApproverMapper测试通过");
    }

    @Test
    @Order(9)
    public void testGetNextStageApprovers_StageApproverQuery() {
        // 测试获取下一阶段审批人 - 验证 stageApproverMapper.selectList 调用
        // This tests: getNextStageApprovers() -> stageApproverMapper.selectList

        // Mock the stage (the method uses stageId from parameter)
        WorkflowStageDO stage = new WorkflowStageDO();
        stage.setId(2L);
        stage.setWorkflowId(1L);
        stage.setStageOrder(2);
        stage.setName("第二阶段");

        when(workflowStageMapper.selectById(2L)).thenReturn(stage);

        // Mock stageApproverMapper.selectList 返回审批人列表
        when(stageApproverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 2L
        ))).thenReturn(new ArrayList<>());

        approverSelectionService.getNextStageApprovers(2L, 1L, 1L, null);

        // 验证 stageApproverMapper.selectList 被正确调用
        verify(stageApproverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 2L
        ));
        System.out.println("✓ ApproverSelectionService.getNextStageApprovers() - stageApproverMapper测试通过");
    }

    @Test
    @Order(10)
    public void testGetSubWorkflowFirstStageApprovers_StageApproverQuery() {
        // 测试获取子流程第一阶段审批人 - 验证 stageApproverMapper.selectList 调用
        // This tests: getSubWorkflowFirstStageApprovers() -> stageApproverMapper.selectList

        // Mock sub workflow and its first stage
        WorkflowDO subWorkflow = new WorkflowDO();
        subWorkflow.setId(2L);
        subWorkflow.setName("子流程");

        when(workflowMapper.selectById(2L)).thenReturn(subWorkflow);

        WorkflowStageDO subFirstStage = new WorkflowStageDO();
        subFirstStage.setId(10L);
        subFirstStage.setWorkflowId(2L);
        subFirstStage.setStageOrder(1);

        when(workflowStageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 2L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(subFirstStage));

        // Mock stageApproverMapper.selectList 返回子流程第一阶段审批人列表
        when(stageApproverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 10L
        ))).thenReturn(new ArrayList<>());

        approverSelectionService.getSubWorkflowFirstStageApprovers(2L, 1L, null);

        // 验证 stageApproverMapper.selectList 被正确调用
        verify(stageApproverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 10L
        ));
        System.out.println("✓ ApproverSelectionService.getSubWorkflowFirstStageApprovers() - stageApproverMapper测试通过");
    }
}
