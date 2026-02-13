package com.xuanjiao.app.usage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.app.usage.impl.UsageLogServiceImpl;
import com.xuanjiao.client.dto.common.PageResult;
import com.xuanjiao.client.dto.usage.dto.UsageLogDTO;
import com.xuanjiao.infrastructure.dataobject.UsageLogDO;
import com.xuanjiao.infrastructure.dataobject.UsageLogWithUserDO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.usage.UsageLogMapper;
import com.xuanjiao.infrastructure.usage.UsageLogQuery;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UsageLogServiceImpl 单元测试
 * 验证 UsageLogMapper 重构后功能正确
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsageLogServiceImplTest {

    @Mock
    private UsageLogMapper usageLogMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UsageLogServiceImpl usageLogService;

    private UsageLogDO testUsageLog;
    private UsageLogWithUserDO testUsageLogWithUser;
    private UserDO testUser;

    @BeforeEach
    public void setUp() {
        testUser = new UserDO();
        testUser.setId(1L);
        testUser.setUsername("test_user");
        testUser.setRealName("测试用户");

        testUsageLog = new UsageLogDO();
        testUsageLog.setId(1L);
        testUsageLog.setAssetId(1L);
        testUsageLog.setUserId(1L);
        testUsageLog.setAction("DOWNLOAD");
        testUsageLog.setIp("127.0.0.1");
        testUsageLog.setDeptName("技术部");
        testUsageLog.setUsageDescription("测试使用描述");
        testUsageLog.setUsagePublishChannel("官网");
        testUsageLog.setCreateTime(LocalDateTime.now());

        // 用于JOIN查询的测试数据
        testUsageLogWithUser = new UsageLogWithUserDO();
        testUsageLogWithUser.setId(1L);
        testUsageLogWithUser.setAssetId(1L);
        testUsageLogWithUser.setUserId(1L);
        testUsageLogWithUser.setAction("DOWNLOAD");
        testUsageLogWithUser.setIp("127.0.0.1");
        testUsageLogWithUser.setDeptName("技术部");
        testUsageLogWithUser.setUsageDescription("测试使用描述");
        testUsageLogWithUser.setUsagePublishChannel("官网");
        testUsageLogWithUser.setCreateTime(LocalDateTime.now());
        testUsageLogWithUser.setUsername("test_user");
        testUsageLogWithUser.setRealName("测试用户");
    }

    // ==================== UsageLogMapper Call Site Tests ====================

    @Test
    @Order(1)
    public void testLog() {
        // 测试记录日志
        // This tests: usageLogMapper.insert

        doAnswer(invocation -> {
            UsageLogDO log = invocation.getArgument(0);
            log.setId(1L);
            return null;
        }).when(usageLogMapper).insert(any(UsageLogDO.class));

        usageLogService.log(1L, 1L, "DOWNLOAD", "127.0.0.1");

        // 验证 usageLogMapper.insert 被调用
        verify(usageLogMapper).insert(argThat(log ->
                log != null && log.getAssetId() == 1L && log.getUserId() == 1L &&
                "DOWNLOAD".equals(log.getAction())
        ));
        System.out.println("✓ UsageLogService.log() - usageLogMapper.insert 测试通过");
    }

    @Test
    @Order(2)
    public void testLogDownload() {
        // 测试记录下载日志
        // This tests: usageLogMapper.insert

        doAnswer(invocation -> {
            UsageLogDO log = invocation.getArgument(0);
            log.setId(1L);
            return null;
        }).when(usageLogMapper).insert(any(UsageLogDO.class));

        usageLogService.logDownload(1L, 1L, "127.0.0.1", "技术部", "测试描述", "官网");

        // 验证 usageLogMapper.insert 被调用
        verify(usageLogMapper).insert(argThat(log ->
                log != null && log.getAssetId() == 1L && log.getUserId() == 1L &&
                "DOWNLOAD".equals(log.getAction()) &&
                "技术部".equals(log.getDeptName()) &&
                "测试描述".equals(log.getUsageDescription())
        ));
        System.out.println("✓ UsageLogService.logDownload() - usageLogMapper.insert 测试通过");
    }

    @Test
    @Order(3)
    public void testQuery_WithAction() {
        // 测试查询日志（带action过滤）
        // This tests: usageLogMapper.selectPageWithUser with action query

        when(usageLogMapper.selectPageWithUser(any(Page.class), any(UsageLogQuery.class)))
                .thenReturn(new Page<UsageLogWithUserDO>(1, 10).setRecords(Arrays.asList(testUsageLogWithUser)).setTotal(1L));

        PageResult<UsageLogDTO> result = usageLogService.query("DOWNLOAD", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // 验证 usageLogMapper.selectPageWithUser 被调用
        verify(usageLogMapper).selectPageWithUser(argThat(page -> page != null && page.getCurrent() == 1),
                argThat(query ->
                        query != null && "DOWNLOAD".equals(query.getAction()) &&
                        "create_time".equals(query.getOrderByField()) &&
                        "DESC".equals(query.getOrderByDirection())
                ));
        System.out.println("✓ UsageLogService.query(action) - usageLogMapper.selectPageWithUser 测试通过");
    }

    @Test
    @Order(4)
    public void testQuery_WithoutAction() {
        // 测试查询日志（无过滤）
        // This tests: usageLogMapper.selectPageWithUser with empty query

        when(usageLogMapper.selectPageWithUser(any(Page.class), any(UsageLogQuery.class)))
                .thenReturn(new Page<UsageLogWithUserDO>(1, 10).setRecords(Arrays.asList(testUsageLogWithUser)).setTotal(1L));

        PageResult<UsageLogDTO> result = usageLogService.query(null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        // 验证 usageLogMapper.selectPageWithUser 被调用
        verify(usageLogMapper).selectPageWithUser(argThat(page -> page != null),
                argThat(query ->
                        query != null && query.getAction() == null &&
                        "create_time".equals(query.getOrderByField())
                ));
        System.out.println("✓ UsageLogService.query(no action) - usageLogMapper.selectPageWithUser 测试通过");
    }

    @Test
    @Order(5)
    public void testGetAssetUsageLogs() {
        // 测试获取素材使用记录
        // This tests: usageLogMapper.selectPageWithUser with assetId + action=DOWNLOAD

        when(usageLogMapper.selectPageWithUser(any(Page.class), any(UsageLogQuery.class)))
                .thenReturn(new Page<UsageLogWithUserDO>(1, 10).setRecords(Arrays.asList(testUsageLogWithUser)).setTotal(1L));

        PageResult<UsageLogDTO> result = usageLogService.getAssetUsageLogs(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getList().size());
        // 验证 usageLogMapper.selectPageWithUser 被调用
        verify(usageLogMapper).selectPageWithUser(argThat(page -> page != null),
                argThat(query ->
                        query != null && query.getAssetId() == 1L &&
                        "DOWNLOAD".equals(query.getAction()) &&
                        "create_time".equals(query.getOrderByField()) &&
                        "DESC".equals(query.getOrderByDirection())
                ));
        System.out.println("✓ UsageLogService.getAssetUsageLogs() - usageLogMapper.selectPageWithUser 测试通过");
    }

    @Test
    @Order(6)
    public void testToDTO() {
        // 测试转换为DTO的逻辑（通过 query 方法间接测试）

        when(usageLogMapper.selectPageWithUser(any(Page.class), any(UsageLogQuery.class)))
                .thenReturn(new Page<UsageLogWithUserDO>(1, 10).setRecords(Arrays.asList(testUsageLogWithUser)).setTotal(1L));

        PageResult<UsageLogDTO> result = usageLogService.query("DOWNLOAD", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        UsageLogDTO dto = result.getList().get(0);
        assertEquals("测试用户", dto.getUsername());  // 验证用户名被正确映射
        System.out.println("✓ UsageLogService - DTO转换测试通过");
    }

    @Test
    @Order(7)
    public void testToDTO_FieldMapping() {
        // 测试转换为DTO的字段映射逻辑（通过 getAssetUsageLogs 方法间接测试）

        when(usageLogMapper.selectPageWithUser(any(Page.class), any(UsageLogQuery.class)))
                .thenReturn(new Page<UsageLogWithUserDO>(1, 10).setRecords(Arrays.asList(testUsageLogWithUser)).setTotal(1L));

        PageResult<UsageLogDTO> result = usageLogService.getAssetUsageLogs(1L, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getList().size());
        UsageLogDTO dto = result.getList().get(0);
        assertEquals(1L, dto.getId());
        assertEquals(1L, dto.getAssetId());
        assertEquals(1L, dto.getUserId());
        assertEquals("DOWNLOAD", dto.getAction());
        assertEquals("127.0.0.1", dto.getIp());
        assertEquals("技术部", dto.getDeptName());
        assertEquals("测试使用描述", dto.getUsageDescription());
        assertEquals("官网", dto.getUsagePublishChannel());
        assertEquals("测试用户", dto.getUsername());
        System.out.println("✓ UsageLogService toDTO() - 字段映射验证通过");
    }
}
