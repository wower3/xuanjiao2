package com.xuanjiao.app.workflow;

import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.app.workflow.handler.WorkflowCompletionHandler;
import com.xuanjiao.app.workflow.impl.WorkflowEngineServiceImpl;
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
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageQuery;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WorkflowEngineServiceImpl 单元测试
 * 验证 UserMapper 和 WorkflowStageMapper 重构后功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkflowEngineServiceImplTest {

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private WorkflowStageMapper stageMapper;

    @Mock
    private StageApproverMapper approverMapper;

    @Mock
    private ApprovalInstanceMapper instanceMapper;

    @Mock
    private ApprovalTaskMapper taskMapper;

    @Mock
    private ApprovalProgressMapper progressMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private DeptMapper deptMapper;

    @Mock
    private com.xuanjiao.app.workflow.ApproverSelectionService approverSelectionService;

    @Mock
    private List<WorkflowCompletionHandler> completionHandlers;

    private WorkflowEngineServiceImpl workflowEngineService;

    private StageApproverDO roleApprover;
    private StageApproverDO deptApprover;
    private UserDO testUser;
    private DeptDO userDept;

    @BeforeEach
    void setUp() {
        // 手动创建实例并注入依赖，避免 @PostConstruct 问题
        workflowEngineService = new WorkflowEngineServiceImpl();
        ReflectionTestUtils.setField(workflowEngineService, "userMapper", userMapper);
        ReflectionTestUtils.setField(workflowEngineService, "deptMapper", deptMapper);
        ReflectionTestUtils.setField(workflowEngineService, "roleMapper", roleMapper);
        ReflectionTestUtils.setField(workflowEngineService, "stageMapper", stageMapper);
        ReflectionTestUtils.setField(workflowEngineService, "approverMapper", approverMapper);
        ReflectionTestUtils.setField(workflowEngineService, "instanceMapper", instanceMapper);
        ReflectionTestUtils.setField(workflowEngineService, "objectMapper", new com.fasterxml.jackson.databind.ObjectMapper());

        testUser = new UserDO();
        testUser.setId(1L);
        testUser.setUsername("test_user");
        testUser.setRealName("测试用户");
        testUser.setDeptId(100L);
        testUser.setRoleId(1L);
        testUser.setStatus(1);

        userDept = new DeptDO();
        userDept.setId(100L);
        userDept.setName("用户部门");
        userDept.setLevel(2);
        userDept.setParentId(10L);

        // 角色类型的审批人（需要校验二级部门）
        roleApprover = new StageApproverDO();
        roleApprover.setId(1L);
        roleApprover.setStageId(1L);
        roleApprover.setApproverType("ROLE");
        roleApprover.setApproverId(1L);
        roleApprover.setCheckSecondaryDept(1);

        // 部门类型的审批人
        deptApprover = new StageApproverDO();
        deptApprover.setId(2L);
        deptApprover.setStageId(1L);
        deptApprover.setApproverType("DEPT");
        deptApprover.setApproverId(100L);
    }

    // ==================== UserMapper Call Site Tests ====================

    @Test
    @Order(1)
    void testGetActualApproverIds_RoleWithSecondaryDept() throws Exception {
        // 测试按角色+二级部门查询审批人
        // This tests: UserQuery with roleId + deptIds + status at line 1237-1241

        // Setup mocks
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(deptMapper.selectById(100L)).thenReturn(userDept);
        when(deptMapper.selectByParentId(100L)).thenReturn(Arrays.asList());
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        // Use reflection to call private method
        Method method = WorkflowEngineServiceImpl.class.getDeclaredMethod(
                "getActualApproverIds", StageApproverDO.class, Long.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Long> result = (List<Long>) method.invoke(workflowEngineService, roleApprover, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0));

        // Verify UserQuery was called with roleId, deptIds, and status
        verify(userMapper).selectList(argThat(query ->
                query != null
                && query.getRoleId() != null
                && query.getRoleId() == 1L
                && query.getDeptIds() != null
                && query.getDeptIds().contains(100L)
                && query.getStatus() == 1
        ));
        System.out.println("✓ WorkflowEngineService.getActualApproverIds(roleId+deptIds+status) - UserMapper测试通过");
    }

    @Test
    @Order(2)
    void testGetActualApproverIds_DeptType() throws Exception {
        // 测试按部门查询审批人
        // This tests: UserQuery with deptId + status at line 1251-1254

        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        // Use reflection to call private method
        Method method = WorkflowEngineServiceImpl.class.getDeclaredMethod(
                "getActualApproverIds", StageApproverDO.class, Long.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Long> result = (List<Long>) method.invoke(workflowEngineService, deptApprover, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0));

        // Verify UserQuery was called with deptId and status
        verify(userMapper).selectList(argThat(query ->
                query != null
                && query.getDeptId() != null
                && query.getDeptId() == 100L
                && query.getStatus() == 1
        ));
        System.out.println("✓ WorkflowEngineService.getActualApproverIds(deptId+status) - UserMapper测试通过");
    }

    @Test
    @Order(3)
    void testGetActualApproverIds_RoleWithoutSecondaryDept() throws Exception {
        // 测试按角色查询审批人（不需要校验二级部门）
        // This should call userMapper.selectUserIdsByRoleId instead of selectList

        roleApprover.setCheckSecondaryDept(0);

        when(userMapper.selectUserIdsByRoleId(1L))
                .thenReturn(Arrays.asList(1L, 2L));

        // Use reflection to call private method
        Method method = WorkflowEngineServiceImpl.class.getDeclaredMethod(
                "getActualApproverIds", StageApproverDO.class, Long.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Long> result = (List<Long>) method.invoke(workflowEngineService, roleApprover, 1L);

        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify selectUserIdsByRoleId was called instead of selectList
        verify(userMapper).selectUserIdsByRoleId(1L);
        verify(userMapper, never()).selectList(any(UserQuery.class));

        System.out.println("✓ WorkflowEngineService.getActualApproverIds(roleId without secondary dept) - UserMapper测试通过");
    }

    // ==================== WorkflowStageMapper Call Site Tests ====================

    @Test
    @Order(4)
    void testGetFirstStage_WorkflowStageQuery() throws Exception {
        // 测试获取第一阶段 - 验证 stageMapper.selectList 调用
        // This tests: getFirstStage() -> stageMapper.selectList with WorkflowStageQuery

        WorkflowStageDO firstStage = new WorkflowStageDO();
        firstStage.setId(1L);
        firstStage.setWorkflowId(1L);
        firstStage.setName("第一阶段");
        firstStage.setStageOrder(1);
        firstStage.setApproveType("OR");

        when(stageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(firstStage));

        // Use reflection to call private method
        Method method = WorkflowEngineServiceImpl.class.getDeclaredMethod("getFirstStage", Long.class);
        method.setAccessible(true);

        WorkflowStageDO result = (WorkflowStageDO) method.invoke(workflowEngineService, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getStageOrder());
        // 验证 stageMapper.selectList 被正确调用
        verify(stageMapper).selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ WorkflowEngineService.getFirstStage() - stageMapper.selectList 测试通过");
    }

    @Test
    @Order(5)
    void testGetFirstStage_NotFound() throws Exception {
        // 测试获取第一阶段（未找到）- 验证返回null
        // This tests: getFirstStage() -> stageMapper.selectList returns empty

        when(stageMapper.selectList(any())).thenReturn(new ArrayList<>());

        // Use reflection to call private method
        Method method = WorkflowEngineServiceImpl.class.getDeclaredMethod("getFirstStage", Long.class);
        method.setAccessible(true);

        WorkflowStageDO result = (WorkflowStageDO) method.invoke(workflowEngineService, 999L);

        assertNull(result);
        System.out.println("✓ WorkflowEngineService.getFirstStage(not found) - stageMapper测试通过");
    }

    @Test
    @Order(6)
    void testStartSubProcessesForStage_StageApproverQuery() {
        // 测试启动子流程 - 验证 approverMapper.selectList 调用
        // This tests: startSubProcessesForStage() -> approverMapper.selectList with subWorkflowIdNotNull

        // Mock approverMapper.selectList 返回有子流程的审批人
        when(approverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 1L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNotNull())
        ))).thenReturn(new ArrayList<>());

        workflowEngineService.startSubProcessesForStage(1L, 1L, 1L, new java.util.HashMap<>());

        // 验证 approverMapper.selectList 被正确调用
        verify(approverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 1L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNotNull())
        ));
        System.out.println("✓ WorkflowEngineService.startSubProcessesForStage() - approverMapper测试通过");
    }
}
