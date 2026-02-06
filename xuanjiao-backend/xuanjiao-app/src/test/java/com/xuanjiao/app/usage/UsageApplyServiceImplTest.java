package com.xuanjiao.app.usage;

import com.xuanjiao.app.usage.impl.UsageApplyServiceImpl;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetQuery;
import com.xuanjiao.app.workflow.WorkflowEngineService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.UsageApplyCmd;
import com.xuanjiao.client.dto.UsageApplyDTO;
import com.xuanjiao.domain.usage.entity.UsageApply;
import com.xuanjiao.domain.usage.entity.UsageApplyAsset;
import com.xuanjiao.domain.usage.repository.UsageApplyAssetRepository;
import com.xuanjiao.domain.usage.repository.UsageApplyRepository;
import com.xuanjiao.infrastructure.asset.AssetMapper;
import com.xuanjiao.infrastructure.dataobject.AssetDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper;
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
 * UsageApplyServiceImpl 单元测试
 * 验证 UsageApplyRepository（间接验证 UsageApplyMapper）重构后功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsageApplyServiceImplTest {

    @Mock
    private UsageApplyRepository usageApplyRepository;

    @Mock
    private UsageApplyAssetRepository usageApplyAssetRepository;

    @Mock
    private UsageApplyAssetMapper usageApplyAssetMapper;

    @Mock
    private WorkflowEngineService workflowEngineService;

    @Mock
    private AssetMapper assetMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UsageApplyServiceImpl usageApplyService;

    private UsageApply testUsageApply;
    private UserDO testUser;
    private AssetDO testAsset;

    @BeforeEach
    public void setUp() {
        testUser = new UserDO();
        testUser.setId(1L);
        testUser.setUsername("test_user");
        testUser.setRealName("测试用户");
        testUser.setDeptId(100L);

        testAsset = new AssetDO();
        testAsset.setId(1L);
        testAsset.setName("测试素材.jpg");
        testAsset.setType("IMAGE");
        testAsset.setStatus("APPROVED");
        testAsset.setDeleted(0);

        testUsageApply = new UsageApply();
        testUsageApply.setId(1L);
        testUsageApply.setTitle("测试使用申请");
        testUsageApply.setUserId(1L);
        testUsageApply.setDeptId(100L);
        testUsageApply.setStatus("DRAFT");
        testUsageApply.setDraft(1);
        testUsageApply.setCreateTime(LocalDateTime.now());
    }

    // ==================== UsageApplyRepository Call Site Tests ====================

    @Test
    @Order(1)
    public void testCreateDraft() {
        // 测试创建草稿申请
        // This tests: UsageApplyRepository.save

        when(userMapper.selectById(1L)).thenReturn(testUser);
        doAnswer(invocation -> {
            UsageApply apply = invocation.getArgument(0);
            apply.setId(1L);
            return null;
        }).when(usageApplyRepository).save(any(UsageApply.class));
        when(assetMapper.selectById(1L)).thenReturn(testAsset);

        UsageApplyCmd cmd = new UsageApplyCmd();
        cmd.setTitle("测试申请");
        cmd.setAssetConfigs(new ArrayList<>());

        UsageApplyDTO result = usageApplyService.createDraft(cmd, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // 验证 usageApplyRepository.save 被调用
        verify(usageApplyRepository).save(argThat(apply ->
                apply != null && "测试申请".equals(apply.getTitle())
        ));
        System.out.println("✓ UsageApplyService.createDraft() - UsageApplyRepository.save 测试通过");
    }

    @Test
    @Order(2)
    public void testGetById() {
        // 测试获取申请详情
        // This tests: UsageApplyRepository.findById

        when(usageApplyRepository.findById(1L)).thenReturn(testUsageApply);

        UsageApplyDTO result = usageApplyService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        // 验证 usageApplyRepository.findById 被调用
        verify(usageApplyRepository).findById(1L);
        System.out.println("✓ UsageApplyService.getById() - UsageApplyRepository.findById 测试通过");
    }

    @Test
    @Order(3)
    public void testQueryDrafts() {
        // 测试查询草稿列表
        // This tests: UsageApplyRepository.findDraftsByUserId, countDraftsByUserId

        when(usageApplyRepository.findDraftsByUserId(eq(1L), eq(0), eq(10)))
                .thenReturn(Arrays.asList(testUsageApply));
        when(usageApplyRepository.countDraftsByUserId(1L)).thenReturn(1L);

        PageResult<UsageApplyDTO> result = usageApplyService.queryDrafts(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // 验证 usageApplyRepository 方法被调用
        verify(usageApplyRepository).findDraftsByUserId(eq(1L), eq(0), eq(10));
        verify(usageApplyRepository).countDraftsByUserId(1L);
        System.out.println("✓ UsageApplyService.queryDrafts() - UsageApplyRepository 测试通过");
    }

    @Test
    @Order(4)
    public void testQueryMyApplications() {
        // 测试查询我的申请列表
        // This tests: UsageApplyRepository.findByUserId, countByUserId

        when(usageApplyRepository.findByUserId(eq(1L), eq(0), eq(10)))
                .thenReturn(Arrays.asList(testUsageApply));
        when(usageApplyRepository.countByUserId(1L)).thenReturn(1L);

        PageResult<UsageApplyDTO> result = usageApplyService.queryMyApplications(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // 验证 usageApplyRepository 方法被调用
        verify(usageApplyRepository).findByUserId(eq(1L), eq(0), eq(10));
        verify(usageApplyRepository).countByUserId(1L);
        System.out.println("✓ UsageApplyService.queryMyApplications() - UsageApplyRepository 测试通过");
    }

    @Test
    @Order(5)
    public void testUpdateDraft() {
        // 测试更新草稿
        // This tests: UsageApplyRepository.findById, update

        when(usageApplyRepository.findById(1L)).thenReturn(testUsageApply);
        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        doNothing().when(usageApplyRepository).update(any());

        UsageApplyCmd cmd = new UsageApplyCmd();
        cmd.setTitle("更新后的标题");
        cmd.setAssetConfigs(new ArrayList<>());

        UsageApplyDTO result = usageApplyService.updateDraft(1L, cmd, 1L);

        assertNotNull(result);
        // 验证 usageApplyRepository.findById 和 update 被调用
        verify(usageApplyRepository).findById(1L);
        verify(usageApplyRepository).update(argThat(apply ->
                apply != null && "更新后的标题".equals(apply.getTitle())
        ));
        System.out.println("✓ UsageApplyService.updateDraft() - UsageApplyRepository 测试通过");
    }

    @Test
    @Order(6)
    public void testSubmit() {
        // 测试提交申请
        // This tests: UsageApplyRepository.findById, update

        when(usageApplyRepository.findById(1L)).thenReturn(testUsageApply);
        when(usageApplyAssetRepository.findByUsageApplyId(1L)).thenReturn(Arrays.asList(new UsageApplyAsset()));
        when(workflowEngineService.startProcess(anyLong(), eq("ASSET_USAGE"), eq(1L), eq(1L)))
                .thenReturn(1000L);
        doNothing().when(usageApplyRepository).update(any());

        Long instanceId = usageApplyService.submit(1L, 1L, 1L);

        assertNotNull(instanceId);
        assertEquals(1000L, instanceId);
        // 验证 usageApplyRepository.findById 和 update 被调用
        verify(usageApplyRepository).findById(1L);
        verify(usageApplyRepository).update(argThat(apply ->
                apply != null && "PENDING".equals(apply.getStatus())
        ));
        System.out.println("✓ UsageApplyService.submit() - UsageApplyRepository 测试通过");
    }

    @Test
    @Order(7)
    public void testDelete() {
        // 测试删除草稿
        // This tests: UsageApplyRepository.findById, deleteById

        when(usageApplyRepository.findById(1L)).thenReturn(testUsageApply);
        doNothing().when(usageApplyRepository).deleteById(1L);

        usageApplyService.delete(1L, 1L);

        // 验证 usageApplyRepository.findById 和 deleteById 被调用
        verify(usageApplyRepository).findById(1L);
        verify(usageApplyRepository).deleteById(1L);
        System.out.println("✓ UsageApplyService.delete() - UsageApplyRepository 测试通过");
    }

    @Test
    @Order(8)
    public void testUpdateStatus() {
        // 测试更新状态
        // This tests: UsageApplyRepository.findById, update

        when(usageApplyRepository.findById(1L)).thenReturn(testUsageApply);
        doNothing().when(usageApplyRepository).update(any());

        usageApplyService.updateStatus(1L, "APPROVED");

        // 验证 usageApplyRepository.findById 和 update 被调用
        verify(usageApplyRepository).findById(1L);
        verify(usageApplyRepository).update(argThat(apply ->
                apply != null && "APPROVED".equals(apply.getStatus())
        ));
        System.out.println("✓ UsageApplyService.updateStatus() - UsageApplyRepository 测试通过");
    }

    @Test
    @Order(9)
    public void testCopyApplication() {
        // 测试复制申请
        // This tests: UsageApplyRepository.findById, save

        when(usageApplyRepository.findById(1L)).thenReturn(testUsageApply);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        doAnswer(invocation -> {
            UsageApply apply = invocation.getArgument(0);
            apply.setId(2L);
            return null;
        }).when(usageApplyRepository).save(any(UsageApply.class));
        when(usageApplyAssetMapper.selectList(any(UsageApplyAssetQuery.class))).thenReturn(new ArrayList<>());

        Long newId = usageApplyService.copyApplication(1L, 1L);

        assertNotNull(newId);
        assertEquals(2L, newId);
        // 验证 usageApplyRepository.findById 和 save 被调用
        verify(usageApplyRepository).findById(1L);
        verify(usageApplyRepository).save(argThat(apply ->
                apply != null && apply.getTitle().contains("副本")
        ));
        // 验证 usageApplyAssetMapper.selectList 被调用
        verify(usageApplyAssetMapper).selectList(argThat(query ->
                query != null && query.getUsageApplyId() == 1L
        ));
        System.out.println("✓ UsageApplyService.copyApplication() - UsageApplyRepository+UsageApplyAssetMapper 测试通过");
    }

    @Test
    @Order(10)
    public void testCanUseAsset() {
        // 测试检查素材使用权限
        // This tests: UsageApplyRepository.findById（通过 UsageApplyAssetRepository）

        // 创建一个 APPROVED 状态的 UsageApply 用于测试
        UsageApply approvedApply = new UsageApply();
        approvedApply.setId(1L);
        approvedApply.setTitle("测试使用申请");
        approvedApply.setUserId(1L);
        approvedApply.setStatus("APPROVED");  // 必须是 APPROVED 状态才能返回 true

        UsageApplyAsset applyAsset = new UsageApplyAsset();
        applyAsset.setUsageApplyId(1L);
        applyAsset.setAssetId(1L);

        when(assetMapper.selectById(1L)).thenReturn(testAsset);
        when(usageApplyAssetRepository.findByAssetId(1L)).thenReturn(Arrays.asList(applyAsset));
        when(usageApplyRepository.findById(1L)).thenReturn(approvedApply);

        boolean result = usageApplyService.canUseAsset(1L, 1L);

        assertTrue(result);
        // 验证 usageApplyRepository.findById 被调用
        verify(usageApplyRepository).findById(1L);
        System.out.println("✓ UsageApplyService.canUseAsset() - UsageApplyRepository 测试通过");
    }
}
