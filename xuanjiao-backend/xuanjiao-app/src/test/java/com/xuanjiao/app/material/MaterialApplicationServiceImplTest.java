package com.xuanjiao.app.material;

import com.xuanjiao.app.material.impl.MaterialApplicationServiceImpl;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.app.asset.AssetService;
import com.xuanjiao.client.MaterialApplicationCmd;
import com.xuanjiao.client.MaterialApplicationDTO;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.domain.material.entity.MaterialApplication;
import com.xuanjiao.domain.material.repository.MaterialApplicationRepository;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.asset.AssetTagMapper;
import com.xuanjiao.infrastructure.asset.AssetTagQuery;
import com.xuanjiao.infrastructure.asset.AssetQuery;
import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.AssetTagDO;
import com.xuanjiao.infrastructure.dataobject.DeptDO;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.dept.DeptMapper;
import com.xuanjiao.infrastructure.user.UserMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MaterialApplicationServiceImpl 单元测试
 * 验证 MaterialApplicationMapper 重构后 MaterialApplicationService 功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MaterialApplicationServiceImplTest {

    @Mock
    private MaterialApplicationRepository materialApplicationRepository;

    @Mock
    private WorkflowEngineService workflowEngineService;

    @Mock
    private AssetService assetService;

    @Mock
    private AssetMapper assetMapper;

    @Mock
    private AssetTagMapper assetTagMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private DeptMapper deptMapper;

    @InjectMocks
    private MaterialApplicationServiceImpl materialApplicationService;

    private MaterialApplication testApplication;
    private UserDO testUser;
    private DeptDO testDept;
    private AssetDO testAsset;

    @BeforeEach
    public void setUp() {
        testUser = new UserDO();
        testUser.setId(1L);
        testUser.setUsername("test_user");
        testUser.setRealName("测试用户");
        testUser.setDeptId(100L);
        testUser.setStatus(1);

        testDept = new DeptDO();
        testDept.setId(100L);
        testDept.setName("总公司");

        testApplication = new MaterialApplication();
        testApplication.setId(100L);
        testApplication.setTitle("测试素材录入申请");
        testApplication.setApplicantId(1L);
        testApplication.setMaintainerId(1L);
        testApplication.setDeptId(100L);
        testApplication.setStatus("DRAFT");
        testApplication.setCreateTime(LocalDateTime.now());

        testAsset = new AssetDO();
        testAsset.setId(1L);
        testAsset.setName("测试素材.jpg");
        testAsset.setType("IMAGE");
        testAsset.setStatus("DRAFT");
        testAsset.setApplicationId(100L);
        testAsset.setFilePath("/path/to/test.jpg");
        testAsset.setMd5("abc123");
        testAsset.setUploadUserId(1L);
        testAsset.setCreateTime(LocalDateTime.now());
    }

    // ==================== MaterialApplicationRepository Call Site Tests ====================

    @Test
    @Order(1)
    public void testCreate_RepositorySave() {
        // 测试创建时调用 Repository.save -> MaterialApplicationMapper.insert

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(materialApplicationRepository.save(any(MaterialApplication.class))).thenAnswer(invocation -> {
            MaterialApplication app = invocation.getArgument(0);
            if (app.getId() == null) {
                app.setId(100L);
            }
            return app;
        });

        MaterialApplicationCmd cmd = new MaterialApplicationCmd();
        cmd.setTitle("测试素材录入申请");
        cmd.setGuaranteeDeclaration(0);

        MaterialApplicationDTO result = materialApplicationService.create(cmd, 1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        // Verify Repository.save was called (which internally calls MaterialApplicationMapper.insert)
        verify(materialApplicationRepository).save(any(MaterialApplication.class));
        System.out.println("✓ MaterialApplicationService.create() - Repository.save 测试通过");
    }

    @Test
    @Order(2)
    public void testGetById_RepositoryFindById() {
        // 测试获取详情时调用 Repository.findById -> MaterialApplicationMapper.selectById

        when(materialApplicationRepository.findById(100L)).thenReturn(testApplication);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(deptMapper.selectById(100L)).thenReturn(testDept);
        when(assetMapper.selectList(any(AssetQuery.class))).thenReturn(new ArrayList<>());
        when(assetTagMapper.selectList(any(AssetTagQuery.class))).thenReturn(new ArrayList<>());
        when(tagMapper.selectBatchIds(any())).thenReturn(new ArrayList<>());

        MaterialApplicationDTO result = materialApplicationService.getById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        // Verify Repository.findById was called (which internally calls MaterialApplicationMapper.selectById)
        verify(materialApplicationRepository).findById(100L);
        System.out.println("✓ MaterialApplicationService.getById() - Repository.findById 测试通过");
    }

    @Test
    @Order(3)
    public void testUpdate_RepositoryUpdate() {
        // 测试更新时调用 Repository.update -> MaterialApplicationMapper.updateById

        when(materialApplicationRepository.findById(100L)).thenReturn(testApplication);
        when(materialApplicationRepository.update(any(MaterialApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MaterialApplicationCmd cmd = new MaterialApplicationCmd();
        cmd.setTitle("新标题");
        cmd.setGuaranteeDeclaration(1);

        MaterialApplicationDTO result = materialApplicationService.update(100L, cmd, 1L);

        assertNotNull(result);
        // Verify Repository.update was called (which internally calls MaterialApplicationMapper.updateById)
        verify(materialApplicationRepository).update(any(MaterialApplication.class));
        System.out.println("✓ MaterialApplicationService.update() - Repository.update 测试通过");
    }

    @Test
    @Order(4)
    public void testDelete_RepositoryDeleteById() {
        // 测试删除时调用 Repository.deleteById -> MaterialApplicationMapper.deleteById

        when(materialApplicationRepository.findById(100L)).thenReturn(testApplication);
        when(assetMapper.selectList(any(AssetQuery.class))).thenReturn(new ArrayList<>());
        doNothing().when(materialApplicationRepository).deleteById(anyLong());

        materialApplicationService.delete(100L, 1L);

        // Verify Repository.deleteById was called (which internally calls MaterialApplicationMapper.deleteById)
        verify(materialApplicationRepository).deleteById(100L);
        System.out.println("✓ MaterialApplicationService.delete() - Repository.deleteById 测试通过");
    }

    @Test
    @Order(5)
    public void testSubmit_RepositoryFindByIdAndUpdate() {
        // 测试提交时调用 Repository.findById 和 Repository.update

        when(materialApplicationRepository.findById(100L)).thenReturn(testApplication);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(assetMapper.selectCount(any(AssetQuery.class))).thenReturn(1L);
        when(assetMapper.selectList(any(AssetQuery.class))).thenReturn(Arrays.asList(testAsset));
        doNothing().when(assetService).updateStatusByApplicationId(anyLong(), anyString());
        when(workflowEngineService.startProcess(anyLong(), anyString(), anyLong(), anyLong())).thenReturn(1000L);
        when(materialApplicationRepository.update(any(MaterialApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Long instanceId = materialApplicationService.submit(100L, 1L, 1L);

        assertNotNull(instanceId);
        // Verify Repository methods were called
        verify(materialApplicationRepository).findById(100L);
        verify(materialApplicationRepository).update(any(MaterialApplication.class));
        System.out.println("✓ MaterialApplicationService.submit() - Repository.findById+update 测试通过");
    }

    @Test
    @Order(6)
    public void testQueryDrafts_RepositoryFindByApplicant() {
        // 测试查询草稿时调用 Repository.findByApplicant

        when(materialApplicationRepository.findByApplicant(eq(1L), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(testApplication));
        when(materialApplicationRepository.countByApplicant(1L)).thenReturn(1L);

        PageResult<MaterialApplicationDTO> result = materialApplicationService.queryDrafts(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        // Verify Repository.findByApplicant was called
        verify(materialApplicationRepository).findByApplicant(eq(1L), eq(0), eq(10));
        System.out.println("✓ MaterialApplicationService.queryDrafts() - Repository.findByApplicant 测试通过");
    }

    @Test
    @Order(7)
    public void testQueryMyApplications_RepositoryFindByApplicant() {
        // 测试查询我的申请时调用 Repository.findByApplicant

        when(materialApplicationRepository.findByApplicant(eq(1L), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(testApplication));
        when(materialApplicationRepository.countByApplicant(1L)).thenReturn(1L);

        PageResult<MaterialApplicationDTO> result = materialApplicationService.queryMyApplications(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        // Verify Repository.findByApplicant was called
        verify(materialApplicationRepository).findByApplicant(eq(1L), eq(0), eq(10));
        System.out.println("✓ MaterialApplicationService.queryMyApplications() - Repository.findByApplicant 测试通过");
    }

    @Test
    @Order(8)
    public void testCopyApplication_RepositoryMethods() {
        // 测试复制申请时调用 Repository 方法

        MaterialApplication newApplication = new MaterialApplication();
        newApplication.setId(101L);
        newApplication.setTitle("测试素材录入申请 - 副本");
        newApplication.setApplicantId(1L);
        newApplication.setStatus("DRAFT");

        when(materialApplicationRepository.findById(100L)).thenReturn(testApplication);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(materialApplicationRepository.save(any(MaterialApplication.class))).thenAnswer(invocation -> {
            MaterialApplication app = invocation.getArgument(0);
            if (app.getId() == null) {
                app.setId(101L);
            }
            return app;
        });
        when(assetMapper.selectList(any(AssetQuery.class))).thenReturn(Arrays.asList(testAsset));
        when(assetMapper.insert(any(AssetDO.class))).thenAnswer(invocation -> {
            AssetDO asset = invocation.getArgument(0);
            if (asset.getId() == null) {
                asset.setId(2L);
            }
            return 1;
        });
        when(assetTagMapper.selectList(any(AssetTagQuery.class))).thenReturn(new ArrayList<>());
        when(assetTagMapper.insert(any(AssetTagDO.class))).thenReturn(1);
        when(assetMapper.updateById(any(AssetDO.class))).thenReturn(1);

        Long newApplicationId = materialApplicationService.copyApplication(100L, 1L);

        assertNotNull(newApplicationId);
        assertEquals(101L, newApplicationId);
        // Verify Repository methods were called
        verify(materialApplicationRepository).findById(100L);
        verify(materialApplicationRepository, atLeastOnce()).save(any(MaterialApplication.class));
        System.out.println("✓ MaterialApplicationService.copyApplication() - Repository.save 测试通过");
    }

    // ==================== AssetMapper Call Site Tests (保留原有测试，修复mock) ====================

    @Test
    @Order(11)
    public void testSubmit_CheckAssetCount() {
        // 测试提交前检查素材数量
        // This tests: AssetMapper.selectCount with applicationId

        when(materialApplicationRepository.findById(100L)).thenReturn(testApplication);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(assetMapper.selectCount(any(AssetQuery.class))).thenReturn(2L);
        @SuppressWarnings("unchecked")
        List<AssetDO> mockAssetList = Arrays.asList(testAsset, testAsset);
        when(assetMapper.selectList(any(AssetQuery.class))).thenReturn(mockAssetList);
        doAnswer(invocation -> null).when(assetService).updateStatusByApplicationId(eq(100L), eq("PENDING"));
        when(workflowEngineService.startProcess(anyLong(), eq("MATERIAL_ENTRY"), eq(100L), eq(1L)))
                .thenReturn(1000L);
        when(materialApplicationRepository.update(any(MaterialApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Long instanceId = materialApplicationService.submit(100L, 1L, 1L);

        assertNotNull(instanceId);
        // Verify AssetMapper.selectCount was called
        verify(assetMapper).selectCount(any(AssetQuery.class));
        System.out.println("✓ MaterialApplicationService.submit() - AssetMapper.selectCount 测试通过");
    }

    @Test
    @Order(12)
    public void testDelete_GetAssociatedAssets() {
        // 测试删除时获取关联素材

        AssetDO asset2 = new AssetDO();
        asset2.setId(2L);
        asset2.setApplicationId(100L);

        AssetTagDO assetTag = new AssetTagDO();
        assetTag.setAssetId(1L);
        assetTag.setTagId(10L);

        when(materialApplicationRepository.findById(100L)).thenReturn(testApplication);
        when(assetMapper.selectList(any(AssetQuery.class))).thenReturn(Arrays.asList(testAsset, asset2));
        when(assetTagMapper.selectList(any(AssetTagQuery.class))).thenReturn(Arrays.asList(assetTag));
        when(assetTagMapper.delete(any(AssetTagQuery.class))).thenReturn(1);
        when(assetMapper.deleteById(anyLong())).thenReturn(1);
        doNothing().when(materialApplicationRepository).deleteById(anyLong());

        materialApplicationService.delete(100L, 1L);

        verify(assetMapper).selectList(any(AssetQuery.class));
        System.out.println("✓ MaterialApplicationService.delete() - AssetMapper 测试通过");
    }
}
