package com.xuanjiao.app.approval;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.app.approval.impl.ApprovalServiceImpl;
import com.xuanjiao.app.workflow.ApproverSelectionService;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceQuery;
import com.xuanjiao.infrastructure.approval.ApprovalTaskMapper;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.asset.AssetQuery;
import com.xuanjiao.infrastructure.dataobject.*;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.material.MaterialApplicationMapper;
import com.xuanjiao.infrastructure.role.RoleMapper;
import com.xuanjiao.infrastructure.role.RoleQuery;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverQuery;
import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageQuery;
import com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetMapper;
import com.xuanjiao.client.dto.PageResult;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ApprovalServiceImpl 单元测试
 * 验证 UserMapper 和 RoleMapper 重构后 ApprovalService 功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApprovalServiceImplTest {

    @Mock
    private ApprovalTaskMapper taskMapper;

    @Mock
    private ApprovalInstanceMapper instanceMapper;

    @Mock
    private WorkflowMapper workflowMapper;

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
    private AssetMapper assetMapper;

    @Mock
    private MaterialApplicationMapper materialApplicationMapper;

    @Mock
    private AssetDeletionApplicationMapper assetDeletionApplicationMapper;

    @Mock
    private AssetDeletionAssetMapper assetDeletionAssetMapper;

    @Mock
    private UsageApplyMapper usageApplyMapper;

    @Mock
    private UsageApplyAssetMapper usageApplyAssetMapper;

    @Mock
    private WorkflowEngineService workflowEngineService;

    @Mock
    private ApproverSelectionService approverSelectionService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private UserDO testUser;
    private RoleDO testRole;
    private DeptDO testDept;
    private ApprovalInstanceDO testInstance;

    @BeforeEach
    public void setUp() {
        testRole = new RoleDO();
        testRole.setId(1L);
        testRole.setName("系统管理员");
        testRole.setRoleType("SYSTEM_ADMIN");
        testRole.setStatus(1);

        testDept = new DeptDO();
        testDept.setId(100L);
        testDept.setName("总公司");

        testUser = new UserDO();
        testUser.setId(1L);
        testUser.setUsername("test_user");
        testUser.setRealName("测试用户");
        testUser.setDeptId(100L);
        testUser.setRoleId(1L);
        testUser.setStatus(1);

        testInstance = new ApprovalInstanceDO();
        testInstance.setId(1L);
        testInstance.setApplicantId(1L);
        testInstance.setBusinessType("ASSET_UPLOAD");
        testInstance.setStatus("PENDING");
    }

    // ==================== UserMapper Call Site Tests ====================

    @Test
    @Order(1)
    public void testGetMyApplied_WithDeptIdFilter() {
        // 测试按部门ID筛选申请人
        // This tests: UserQuery with deptId+status at line 122-124

        // Mock the instance query
        Page<ApprovalInstanceDO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testInstance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any(ApprovalInstanceQuery.class)))
                .thenReturn(mockPage);

        // Mock user query by dept
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        // Mock for buildInstanceInfo (minimal)
        when(workflowMapper.selectById(any())).thenReturn(new WorkflowDO());
        when(workflowStageMapper.selectList(any())).thenReturn(Arrays.asList(new WorkflowStageDO()));
        when(stageApproverMapper.selectList(any())).thenReturn(Arrays.asList(new StageApproverDO()));
        when(taskMapper.selectList(any())).thenReturn(Arrays.asList(new ApprovalTaskDO()));

        @SuppressWarnings("unchecked")
        PageResult<Map<String, Object>> result = approvalService.getMyApplied(
                1L, 1, 10, null, true, null, 100L, null, null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // Verify UserQuery was called with deptId and status
        verify(userMapper).selectList(argThat(query ->
                query != null && query.getDeptId() == 100L && query.getStatus() == 1
        ));
        System.out.println("✓ ApprovalService.getMyApplied(deptId筛选) - UserMapper测试通过");
    }

    @Test
    @Order(2)
    public void testGetMyApplied_WithRoleTypeFilter() {
        // 测试按角色类型筛选申请人
        // This tests: RoleQuery with roleType+status at line 133-136
        //             UserQuery with roleIds+status at line 141-144

        Page<ApprovalInstanceDO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testInstance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any(ApprovalInstanceQuery.class)))
                .thenReturn(mockPage);

        // Mock role query by roleType
        when(roleMapper.selectList(any(RoleQuery.class)))
                .thenReturn(Arrays.asList(testRole));

        // Mock user query by roleIds
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        when(workflowMapper.selectById(anyLong())).thenReturn(new WorkflowDO());
        when(workflowStageMapper.selectList(any())).thenReturn(Arrays.asList(new WorkflowStageDO()));
        when(stageApproverMapper.selectList(any())).thenReturn(Arrays.asList(new StageApproverDO()));
        when(taskMapper.selectList(any())).thenReturn(Arrays.asList(new ApprovalTaskDO()));
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(roleMapper.selectById(1L)).thenReturn(testRole);
        when(deptMapper.selectById(100L)).thenReturn(testDept);

        @SuppressWarnings("unchecked")
        PageResult<Map<String, Object>> result = approvalService.getMyApplied(
                1L, 1, 10, null, true, null, null, "SYSTEM_ADMIN", null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // Verify RoleQuery was called with roleType and status
        verify(roleMapper).selectList(argThat(query ->
                query != null && "SYSTEM_ADMIN".equals(query.getRoleType()) && query.getStatus() == 1
        ));
        // Verify UserQuery was called with roleIds and status
        verify(userMapper).selectList(argThat(query ->
                query != null && query.getRoleIds() != null && query.getRoleIds().contains(1L) && query.getStatus() == 1
        ));
        System.out.println("✓ ApprovalService.getMyApplied(roleType筛选) - RoleMapper+UserMapper测试通过");
    }

    @Test
    @Order(3)
    public void testGetMyApplied_WithBothFilters() {
        // 测试同时按部门和角色类型筛选
        // This tests both UserMapper call sites work together

        Page<ApprovalInstanceDO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testInstance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any(ApprovalInstanceQuery.class)))
                .thenReturn(mockPage);

        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        when(roleMapper.selectList(any(RoleQuery.class)))
                .thenReturn(Arrays.asList(testRole));

        when(workflowMapper.selectById(anyLong())).thenReturn(new WorkflowDO());
        when(workflowStageMapper.selectList(any())).thenReturn(Arrays.asList(new WorkflowStageDO()));
        when(stageApproverMapper.selectList(any())).thenReturn(Arrays.asList(new StageApproverDO()));
        when(taskMapper.selectList(any())).thenReturn(Arrays.asList(new ApprovalTaskDO()));
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(roleMapper.selectById(1L)).thenReturn(testRole);
        when(deptMapper.selectById(100L)).thenReturn(testDept);

        @SuppressWarnings("unchecked")
        PageResult<Map<String, Object>> result = approvalService.getMyApplied(
                1L, 1, 10, null, true, null, 100L, "SYSTEM_ADMIN", null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // Verify both UserMapper call sites were invoked
        verify(userMapper, atLeastOnce()).selectList(any(UserQuery.class));
        System.out.println("✓ ApprovalService.getMyApplied(deptId+roleType组合筛选) - 测试通过");
    }

    // ==================== RoleMapper Call Site Test ====================

    @Test
    @Order(10)
    public void testRoleMapperSelectList_WithRoleTypeAndStatus() {
        // 独立测试 RoleMapper.selectList 与 roleType+status
        // This tests the RoleMapper call at line 133-136

        when(roleMapper.selectList(any(RoleQuery.class)))
                .thenReturn(Arrays.asList(testRole));

        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        Page<ApprovalInstanceDO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testInstance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any(ApprovalInstanceQuery.class)))
                .thenReturn(mockPage);

        when(workflowMapper.selectById(anyLong())).thenReturn(new WorkflowDO());
        when(workflowStageMapper.selectList(any())).thenReturn(Arrays.asList(new WorkflowStageDO()));
        when(stageApproverMapper.selectList(any())).thenReturn(Arrays.asList(new StageApproverDO()));
        when(taskMapper.selectList(any())).thenReturn(Arrays.asList(new ApprovalTaskDO()));
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(roleMapper.selectById(1L)).thenReturn(testRole);
        when(deptMapper.selectById(100L)).thenReturn(testDept);

        @SuppressWarnings("unchecked")
        PageResult<Map<String, Object>> result = approvalService.getMyApplied(
                1L, 1, 10, null, true, null, null, "BRANCH_MGMT", null
        );

        assertNotNull(result);
        // Verify roleMapper was called with correct parameters
        verify(roleMapper).selectList(argThat(query ->
                query != null && "BRANCH_MGMT".equals(query.getRoleType()) && query.getStatus() == 1
        ));
        System.out.println("✓ ApprovalService - RoleMapper.selectList(roleType+status) 测试通过");
    }

    // ==================== AssetMapper Call Site Tests ====================

    @Test
    @Order(20)
    public void testAssetMapper_ForMaterialEntry() {
        // 测试素材录入申请的 AssetMapper 调用
        // This tests: AssetMapper.selectList with applicationId at line 273

        AssetDO testAsset = new AssetDO();
        testAsset.setId(1L);
        testAsset.setName("测试素材.jpg");
        testAsset.setType("IMAGE");
        testAsset.setApplicationId(100L);

        testInstance.setBusinessType("MATERIAL_ENTRY");
        testInstance.setBusinessId(100L);

        Page<ApprovalInstanceDO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testInstance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any(ApprovalInstanceQuery.class)))
                .thenReturn(mockPage);

        // Mock material application
        MaterialApplicationDO materialApp = new MaterialApplicationDO();
        materialApp.setId(100L);
        materialApp.setTitle("测试素材录入申请");
        when(materialApplicationMapper.selectById(100L)).thenReturn(materialApp);

        // Mock assetMapper.selectList for get asset count/type
        when(assetMapper.selectList(argThat(query ->
                query != null && query.getApplicationId() == 100L
        ))).thenReturn(Arrays.asList(testAsset));

        when(workflowMapper.selectById(anyLong())).thenReturn(new WorkflowDO());
        when(workflowStageMapper.selectList(any())).thenReturn(Arrays.asList(new WorkflowStageDO()));
        when(stageApproverMapper.selectList(any())).thenReturn(Arrays.asList(new StageApproverDO()));
        when(taskMapper.selectList(any())).thenReturn(Arrays.asList(new ApprovalTaskDO()));
        when(userMapper.selectById(1L)).thenReturn(testUser);

        @SuppressWarnings("unchecked")
        PageResult<Map<String, Object>> result = approvalService.getMyApplied(
                1L, 1, 10, "MATERIAL_ENTRY", false, null, null, null, null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // Verify AssetMapper.selectList was called with applicationId
        verify(assetMapper, atLeastOnce()).selectList(argThat(query ->
                query != null && query.getApplicationId() == 100L
        ));
        System.out.println("✓ ApprovalService - AssetMapper.selectList(applicationId) for MATERIAL_ENTRY 测试通过");
    }

    @Test
    @Order(21)
    public void testAssetMapper_ForAssetUpload() {
        // 测试素材上传申请的 AssetMapper.selectById 调用
        // This tests: AssetMapper.selectById at line 280

        AssetDO testAsset = new AssetDO();
        testAsset.setId(1L);
        testAsset.setName("测试素材.jpg");
        testAsset.setType("IMAGE");

        testInstance.setBusinessType("ASSET");
        testInstance.setBusinessId(1L);

        Page<ApprovalInstanceDO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testInstance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any(ApprovalInstanceQuery.class)))
                .thenReturn(mockPage);

        // Mock assetMapper.selectById for business name
        when(assetMapper.selectById(1L)).thenReturn(testAsset);

        when(workflowMapper.selectById(anyLong())).thenReturn(new WorkflowDO());
        when(workflowStageMapper.selectList(any())).thenReturn(Arrays.asList(new WorkflowStageDO()));
        when(stageApproverMapper.selectList(any())).thenReturn(Arrays.asList(new StageApproverDO()));
        when(taskMapper.selectList(any())).thenReturn(Arrays.asList(new ApprovalTaskDO()));
        when(userMapper.selectById(1L)).thenReturn(testUser);

        @SuppressWarnings("unchecked")
        PageResult<Map<String, Object>> result = approvalService.getMyApplied(
                1L, 1, 10, "ASSET", false, null, null, null, null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // Verify AssetMapper.selectById was called
        verify(assetMapper, atLeastOnce()).selectById(1L);
        System.out.println("✓ ApprovalService - AssetMapper.selectById for ASSET 测试通过");
    }

    @Test
    @Order(22)
    public void testAssetMapper_ForAssetDeletion() {
        // 测试素材删除申请的 AssetMapper 调用
        // This tests: AssetMapper.selectById for deletion assets at line 419

        AssetDO testAsset1 = new AssetDO();
        testAsset1.setId(1L);
        testAsset1.setName("待删除素材1.jpg");
        testAsset1.setType("IMAGE");

        AssetDO testAsset2 = new AssetDO();
        testAsset2.setId(2L);
        testAsset2.setName("待删除素材2.jpg");
        testAsset2.setType("VIDEO");

        testInstance.setBusinessType("ASSET_DELETION");
        testInstance.setBusinessId(100L);

        Page<ApprovalInstanceDO> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testInstance));
        mockPage.setTotal(1);
        when(instanceMapper.selectPage(any(Page.class), any(ApprovalInstanceQuery.class)))
                .thenReturn(mockPage);

        // Mock deletion application
        AssetDeletionApplicationDO deletionApp = new AssetDeletionApplicationDO();
        deletionApp.setId(100L);
        deletionApp.setTitle("测试素材删除申请");
        when(assetDeletionApplicationMapper.selectById(100L)).thenReturn(deletionApp);

        // Mock deletion assets
        AssetDeletionAssetDO deletionAsset1 = new AssetDeletionAssetDO();
        deletionAsset1.setAssetId(1L);
        AssetDeletionAssetDO deletionAsset2 = new AssetDeletionAssetDO();
        deletionAsset2.setAssetId(2L);
        when(assetDeletionAssetMapper.selectList(any())).thenReturn(Arrays.asList(deletionAsset1, deletionAsset2));

        // Mock assetMapper.selectById for each deletion asset
        when(assetMapper.selectById(1L)).thenReturn(testAsset1);
        when(assetMapper.selectById(2L)).thenReturn(testAsset2);

        when(workflowMapper.selectById(anyLong())).thenReturn(new WorkflowDO());
        when(workflowStageMapper.selectList(any())).thenReturn(Arrays.asList(new WorkflowStageDO()));
        when(stageApproverMapper.selectList(any())).thenReturn(Arrays.asList(new StageApproverDO()));
        when(taskMapper.selectList(any())).thenReturn(Arrays.asList(new ApprovalTaskDO()));
        when(userMapper.selectById(1L)).thenReturn(testUser);

        @SuppressWarnings("unchecked")
        PageResult<Map<String, Object>> result = approvalService.getMyApplied(
                1L, 1, 10, "ASSET_DELETION", false, null, null, null, null
        );

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // Verify AssetMapper.selectById was called for both assets
        verify(assetMapper, atLeastOnce()).selectById(1L);
        verify(assetMapper, atLeastOnce()).selectById(2L);
        System.out.println("✓ ApprovalService - AssetMapper.selectById for ASSET_DELETION 测试通过");
    }

    @Test
    @Order(23)
    public void testAssetMapper_InGetTaskDetail() {
        // 测试 getTaskDetail 中的 AssetMapper 调用
        // This tests: AssetMapper.selectById at line 690

        ApprovalTaskDO testTask = new ApprovalTaskDO();
        testTask.setId(1L);
        testTask.setInstanceId(1L);
        testTask.setApproverId(1L);
        testTask.setStatus("PENDING");

        testInstance.setBusinessType("ASSET");
        testInstance.setBusinessId(1L);

        AssetDO testAsset = new AssetDO();
        testAsset.setId(1L);
        testAsset.setName("测试素材.jpg");

        when(taskMapper.selectList(any())).thenReturn(Arrays.asList(testTask));
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(instanceMapper.selectById(1L)).thenReturn(testInstance);

        // Mock assetMapper.selectById for business name
        when(assetMapper.selectById(1L)).thenReturn(testAsset);

        when(workflowMapper.selectById(anyLong())).thenReturn(new WorkflowDO());
        when(workflowStageMapper.selectList(any())).thenReturn(Arrays.asList(new WorkflowStageDO()));
        when(stageApproverMapper.selectList(any())).thenReturn(Arrays.asList(new StageApproverDO()));
        when(userMapper.selectById(1L)).thenReturn(testUser);

        Map<String, Object> result = approvalService.getTaskDetail(1L);

        assertNotNull(result);
        assertEquals("测试素材.jpg", result.get("businessName"));
        // Verify AssetMapper.selectById was called
        verify(assetMapper, atLeastOnce()).selectById(1L);
        System.out.println("✓ ApprovalService - AssetMapper.selectById in getTaskDetail 测试通过");
    }

    // ==================== WorkflowStageMapper Call Site Tests ====================

    @Test
    @Order(30)
    public void testGetTaskDetail_WorkflowStageQuery() {
        // 测试获取任务详情 - 验证 workflowStageMapper.selectList 调用
        // This tests: getTaskDetail() -> getFirstStageOfWorkflow() -> workflowStageMapper.selectList

        ApprovalTaskDO task = new ApprovalTaskDO();
        task.setId(1L);
        task.setInstanceId(1L);
        task.setStageId(1L);
        task.setApproverId(1L);
        task.setStatus("PENDING");

        ApprovalInstanceDO instance = new ApprovalInstanceDO();
        instance.setId(1L);
        instance.setWorkflowId(1L);
        instance.setBusinessType("ASSET");
        instance.setBusinessId(1L);
        instance.setCurrentStageId(1L);

        WorkflowDO workflow = new WorkflowDO();
        workflow.setId(1L);
        workflow.setName("素材录入流程");

        WorkflowStageDO currentStage = new WorkflowStageDO();
        currentStage.setId(1L);
        currentStage.setName("第一阶段");
        currentStage.setStageOrder(1);
        currentStage.setApproveType("OR");

        AssetDO asset = new AssetDO();
        asset.setId(1L);
        asset.setName("测试素材.jpg");

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(instanceMapper.selectById(1L)).thenReturn(instance);
        when(workflowMapper.selectById(1L)).thenReturn(workflow);
        when(workflowStageMapper.selectById(1L)).thenReturn(currentStage);
        when(assetMapper.selectById(1L)).thenReturn(asset);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(taskMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(approverSelectionService.getApprovalProgress(1L)).thenReturn(new ArrayList<>());

        // Mock workflowStageMapper.selectList for getFirstStageOfWorkflow
        when(workflowStageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(currentStage));

        Map<String, Object> result = approvalService.getTaskDetail(1L);

        assertNotNull(result);
        // 验证 workflowStageMapper.selectList 被正确调用
        verify(workflowStageMapper).selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ ApprovalService.getTaskDetail() - workflowStageMapper.selectList 测试通过");
    }

    // ==================== StageApproverMapper Call Site Tests ====================

    @Test
    @Order(24)
    public void testStageApproverMapper_GetTaskDetail() {
        // 测试获取任务详情 - 验证 stageApproverMapper.selectList 调用
        // This tests: getTaskDetail() -> stageApproverMapper.selectList with subWorkflowIdNull

        ApprovalTaskDO task = new ApprovalTaskDO();
        task.setId(1L);
        task.setInstanceId(1L);
        task.setStageId(1L);
        task.setApproverId(1L);
        task.setStatus("PENDING");

        ApprovalInstanceDO instance = new ApprovalInstanceDO();
        instance.setId(1L);
        instance.setWorkflowId(1L);
        instance.setBusinessType("ASSET");
        instance.setBusinessId(1L);
        instance.setCurrentStageId(1L);

        WorkflowDO workflow = new WorkflowDO();
        workflow.setId(1L);
        workflow.setName("素材录入流程");

        WorkflowStageDO currentStage = new WorkflowStageDO();
        currentStage.setId(1L);
        currentStage.setName("第一阶段");
        currentStage.setStageOrder(1);
        currentStage.setApproveType("OR");

        WorkflowStageDO nextStage = new WorkflowStageDO();
        nextStage.setId(2L);
        nextStage.setName("第二阶段");
        nextStage.setStageOrder(2);

        AssetDO asset = new AssetDO();
        asset.setId(1L);
        asset.setName("测试素材.jpg");

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(instanceMapper.selectById(1L)).thenReturn(instance);
        when(workflowMapper.selectById(1L)).thenReturn(workflow);
        when(workflowStageMapper.selectById(1L)).thenReturn(currentStage);
        when(assetMapper.selectById(1L)).thenReturn(asset);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(taskMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(approverSelectionService.getApprovalProgress(1L)).thenReturn(new ArrayList<>());

        // Mock workflowStageMapper.selectList for getFirstStageOfWorkflow
        when(workflowStageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(currentStage, nextStage));

        // Mock stageApproverMapper.selectList 返回下一阶段审批人（排除子流程）
        when(stageApproverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 2L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNull()) &&
                "id".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(new ArrayList<>());

        // Mock stageApproverMapper.selectList 返回子流程审批人
        when(stageApproverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() != null &&
                query.getStageId() == 2L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNotNull())
        ))).thenReturn(new ArrayList<>());

        Map<String, Object> result = approvalService.getTaskDetail(1L);

        assertNotNull(result);
        // 验证 stageApproverMapper.selectList 被正确调用（排除子流程）
        verify(stageApproverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 2L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNull()) &&
                "id".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        // 验证 stageApproverMapper.selectList 被正确调用（仅子流程）
        verify(stageApproverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 2L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNotNull())
        ));
        System.out.println("✓ ApprovalService.getTaskDetail() - stageApproverMapper.selectList 测试通过");
    }

    @Test
    @Order(25)
    public void testStageApproverMapper_SubWorkflowInGetTaskDetail() {
        // 测试获取任务详情（含子流程）- 验证 stageApproverMapper.selectList 对子流程的调用
        // This tests: getTaskDetail() -> stageApproverMapper.selectList for sub-workflow first stage

        ApprovalTaskDO task = new ApprovalTaskDO();
        task.setId(1L);
        task.setInstanceId(1L);
        task.setStageId(1L);
        task.setApproverId(1L);
        task.setStatus("PENDING");

        ApprovalInstanceDO instance = new ApprovalInstanceDO();
        instance.setId(1L);
        instance.setWorkflowId(1L);
        instance.setBusinessType("ASSET");
        instance.setBusinessId(1L);
        instance.setCurrentStageId(1L);

        WorkflowDO workflow = new WorkflowDO();
        workflow.setId(1L);
        workflow.setName("素材录入流程");

        WorkflowStageDO currentStage = new WorkflowStageDO();
        currentStage.setId(1L);
        currentStage.setName("第一阶段");
        currentStage.setStageOrder(1);
        currentStage.setApproveType("OR");

        WorkflowStageDO nextStage = new WorkflowStageDO();
        nextStage.setId(2L);
        nextStage.setName("第二阶段");
        nextStage.setStageOrder(2);

        // 子流程配置
        WorkflowDO subWorkflow = new WorkflowDO();
        subWorkflow.setId(10L);
        subWorkflow.setName("子流程");

        WorkflowStageDO subFirstStage = new WorkflowStageDO();
        subFirstStage.setId(11L);
        subFirstStage.setWorkflowId(10L);
        subFirstStage.setName("子流程第一阶段");
        subFirstStage.setStageOrder(1);

        AssetDO asset = new AssetDO();
        asset.setId(1L);
        asset.setName("测试素材.jpg");

        when(taskMapper.selectById(1L)).thenReturn(task);
        when(instanceMapper.selectById(1L)).thenReturn(instance);
        when(workflowMapper.selectById(1L)).thenReturn(workflow);
        when(workflowStageMapper.selectById(1L)).thenReturn(currentStage);
        when(assetMapper.selectById(1L)).thenReturn(asset);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(taskMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(approverSelectionService.getApprovalProgress(1L)).thenReturn(new ArrayList<>());

        // Mock workflowStageMapper.selectList for getFirstStageOfWorkflow
        when(workflowStageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 1L
        ))).thenReturn(Arrays.asList(currentStage, nextStage));

        // Mock stageApproverMapper.selectList 返回下一阶段子流程审批人
        StageApproverDO subWorkflowApprover = new StageApproverDO();
        subWorkflowApprover.setId(1L);
        subWorkflowApprover.setStageId(2L);
        subWorkflowApprover.setApproverType("USER");
        subWorkflowApprover.setApproverId(2L);
        subWorkflowApprover.setSubWorkflowId(10L);

        when(stageApproverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 2L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNotNull())
        ))).thenReturn(Arrays.asList(subWorkflowApprover));

        // Mock workflow and first stage for sub-workflow
        when(workflowMapper.selectById(10L)).thenReturn(subWorkflow);
        when(workflowStageMapper.selectList(argThat(query ->
                query != null &&
                query.getWorkflowId() == 10L &&
                "stage_order".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(Arrays.asList(subFirstStage));

        // Mock stageApproverMapper.selectList 返回子流程第一阶段审批人（排除子流程）
        when(stageApproverMapper.selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 11L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNull()) &&
                "id".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ))).thenReturn(new ArrayList<>());

        Map<String, Object> result = approvalService.getTaskDetail(1L);

        assertNotNull(result);
        // 验证 stageApproverMapper.selectList 被正确调用（子流程第一阶段审批人）
        verify(stageApproverMapper).selectList(ArgumentMatchers.<StageApproverQuery>argThat(query ->
                query != null &&
                query.getStageId() == 11L &&
                Boolean.TRUE.equals(query.getSubWorkflowIdNull()) &&
                "id".equals(query.getOrderByField()) &&
                "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ ApprovalService.getTaskDetail(sub-workflow) - stageApproverMapper.selectList 测试通过");
    }
}
