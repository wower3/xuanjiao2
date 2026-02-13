package com.xuanjiao.integration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.usage.UsageLogMapper;
import com.xuanjiao.infrastructure.usage.UsageLogQuery;
import com.xuanjiao.infrastructure.dataobject.UsageLogDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UsageLogMapper 集成测试
 * 验证 UsageLogMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsageLogMapperIntegrationTest {

    @Autowired
    private UsageLogMapper usageLogMapper;

    @Test
    @Order(1)
    public void testSelectById() {
        UsageLogDO result = usageLogMapper.selectById(1L);
        if (result != null) {
            assertNotNull(result.getId());
            System.out.println("✓ UsageLog selectById: " + result.getAction());
        } else {
            System.out.println("⚠ UsageLog selectById: No records found in database");
        }
    }

    @Test
    @Order(2)
    public void testSelectCount() {
        UsageLogQuery query = new UsageLogQuery();
        Long count = usageLogMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ UsageLog selectCount: " + count + " records");
    }

    @Test
    @Order(3)
    public void testSelectCount_WithAssetIdAndAction() {
        UsageLogQuery query = new UsageLogQuery();
        query.setAssetId(1L);
        query.setAction("DOWNLOAD");
        Long count = usageLogMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ UsageLog selectCount (assetId=1, action=DOWNLOAD): " + count + " records");
    }

    @Test
    @Order(4)
    public void testSelectPage() {
        UsageLogQuery query = new UsageLogQuery();
        query.setAction("DOWNLOAD");
        Page<UsageLogDO> page = new Page<>(1, 10);
        IPage<UsageLogDO> result = usageLogMapper.selectPage(page, query);
        assertNotNull(result);
        System.out.println("✓ UsageLog selectPage: " + result.getRecords().size() + " records, total: " + result.getTotal());
    }

    @Test
    @Order(5)
    public void testSelectPage_WithAssetId() {
        UsageLogQuery query = new UsageLogQuery();
        query.setAssetId(1L);
        query.setAction("DOWNLOAD");
        query.setOrderByField("create_time");
        query.setOrderByDirection("DESC");
        Page<UsageLogDO> page = new Page<>(1, 10);
        IPage<UsageLogDO> result = usageLogMapper.selectPage(page, query);
        assertNotNull(result);
        System.out.println("✓ UsageLog selectPage (assetId=1, action=DOWNLOAD): " + result.getRecords().size() + " records, total: " + result.getTotal());
    }

    @Test
    @Order(6)
    public void testSelectPage_EmptyQuery() {
        UsageLogQuery query = new UsageLogQuery();
        Page<UsageLogDO> page = new Page<>(1, 10);
        IPage<UsageLogDO> result = usageLogMapper.selectPage(page, query);
        assertNotNull(result);
        System.out.println("✓ UsageLog selectPage (empty): " + result.getRecords().size() + " records, total: " + result.getTotal());
    }

    @Test
    @Order(7)
    public void testSelectPage_WithAction() {
        UsageLogQuery query = new UsageLogQuery();
        query.setAction("DOWNLOAD");
        Page<UsageLogDO> page = new Page<>(1, 10);
        IPage<UsageLogDO> result = usageLogMapper.selectPage(page, query);
        assertNotNull(result);
        for (UsageLogDO item : result.getRecords()) {
            assertEquals("DOWNLOAD", item.getAction());
        }
        System.out.println("✓ UsageLog selectPage (action=DOWNLOAD): " + result.getRecords().size() + " records, total: " + result.getTotal());
    }
}
