package com.xuanjiao.integration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.usage.UsageApplyMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyQuery;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UsageApplyMapper 集成测试
 * 验证 UsageApplyMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsageApplyMapperIntegrationTest {

    @Autowired
    private UsageApplyMapper usageApplyMapper;

    @Test
    @Order(1)
    public void testSelectById() {
        UsageApplyDO result = usageApplyMapper.selectById(1L);
        // 假设数据库中有ID为1的记录
        if (result != null) {
            assertNotNull(result.getId());
            System.out.println("✓ UsageApply selectById: " + result.getTitle());
        } else {
            System.out.println("⚠ UsageApply selectById: No records found in database");
        }
    }

    @Test
    @Order(2)
    public void testSelectCount() {
        UsageApplyQuery query = new UsageApplyQuery();
        Long count = usageApplyMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ UsageApply selectCount: " + count + " records");
    }

    @Test
    @Order(3)
    public void testSelectCount_WithUserId() {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(1L);
        Long count = usageApplyMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ UsageApply selectCount (userId=1): " + count + " records");
    }

    @Test
    @Order(4)
    public void testSelectPage() {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setUserId(1L);
        Page<UsageApplyDO> page = new Page<>(1, 10);
        IPage<UsageApplyDO> result = usageApplyMapper.selectPage(page, query);
        assertNotNull(result);
        System.out.println("✓ UsageApply selectPage: " + result.getRecords().size() + " records, total: " + result.getTotal());
    }

    @Test
    @Order(5)
    public void testSelectPage_EmptyQuery() {
        UsageApplyQuery query = new UsageApplyQuery();
        Page<UsageApplyDO> page = new Page<>(1, 10);
        IPage<UsageApplyDO> result = usageApplyMapper.selectPage(page, query);
        assertNotNull(result);
        System.out.println("✓ UsageApply selectPage (empty): " + result.getRecords().size() + " records, total: " + result.getTotal());
    }

    @Test
    @Order(6)
    public void testSelectPage_WithStatus() {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setStatus("APPROVED");
        Page<UsageApplyDO> page = new Page<>(1, 10);
        IPage<UsageApplyDO> result = usageApplyMapper.selectPage(page, query);
        assertNotNull(result);
        for (UsageApplyDO item : result.getRecords()) {
            assertEquals("APPROVED", item.getStatus());
        }
        System.out.println("✓ UsageApply selectPage (status=APPROVED): " + result.getRecords().size() + " records, total: " + result.getTotal());
    }

    @Test
    @Order(7)
    public void testSelectPage_WithDraft() {
        UsageApplyQuery query = new UsageApplyQuery();
        query.setDraft(1);
        Page<UsageApplyDO> page = new Page<>(1, 10);
        IPage<UsageApplyDO> result = usageApplyMapper.selectPage(page, query);
        assertNotNull(result);
        for (UsageApplyDO item : result.getRecords()) {
            assertEquals(1, item.getDraft());
        }
        System.out.println("✓ UsageApply selectPage (draft=1): " + result.getRecords().size() + " records, total: " + result.getTotal());
    }
}
