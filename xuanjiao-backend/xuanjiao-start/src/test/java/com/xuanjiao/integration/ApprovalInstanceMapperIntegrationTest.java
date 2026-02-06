package com.xuanjiao.integration;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceQuery;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApprovalInstanceMapper 集成测试
 * 验证 ApprovalInstanceMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApprovalInstanceMapperIntegrationTest {

    @Autowired
    private ApprovalInstanceMapper approvalInstanceMapper;

    @Test
    @Order(1)
    public void testSelectById() {
        ApprovalInstanceDO result = approvalInstanceMapper.selectById(1L);
        if (result != null) {
            assertNotNull(result.getId());
            System.out.println("✓ ApprovalInstance selectById: " + result.getBusinessType());
        } else {
            System.out.println("⚠ ApprovalInstance selectById: No records found in database");
        }
    }

    @Test
    @Order(2)
    public void testSelectList_EmptyQuery() {
        ApprovalInstanceQuery query = new ApprovalInstanceQuery();
        java.util.List<ApprovalInstanceDO> list = approvalInstanceMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ ApprovalInstance selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(3)
    public void testSelectList_WithParentInstanceId() {
        ApprovalInstanceQuery query = new ApprovalInstanceQuery();
        query.setParentInstanceIdIsNull(true);  // 只查询主流程
        java.util.List<ApprovalInstanceDO> list = approvalInstanceMapper.selectList(query);
        assertNotNull(list);
        for (ApprovalInstanceDO item : list) {
            assertNull(item.getParentInstanceId(), "Should only have main flow instances");
        }
        System.out.println("✓ ApprovalInstance selectList (main flows only): " + list.size() + " records");
    }

    @Test
    @Order(4)
    public void testSelectList_WithStatus() {
        ApprovalInstanceQuery query = new ApprovalInstanceQuery();
        query.setStatus("PENDING");
        java.util.List<ApprovalInstanceDO> list = approvalInstanceMapper.selectList(query);
        assertNotNull(list);
        for (ApprovalInstanceDO item : list) {
            assertEquals("PENDING", item.getStatus());
        }
        System.out.println("✓ ApprovalInstance selectList (status=PENDING): " + list.size() + " records");
    }

    @Test
    @Order(5)
    public void testSelectCount() {
        ApprovalInstanceQuery query = new ApprovalInstanceQuery();
        Long count = approvalInstanceMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ ApprovalInstance selectCount: " + count + " records");
    }

    @Test
    @Order(6)
    public void testSelectPage() {
        ApprovalInstanceQuery query = new ApprovalInstanceQuery();
        query.setParentInstanceIdIsNull(true);  // 只查询主流程
        Page<ApprovalInstanceDO> page = new Page<>(1, 10);
        Page<ApprovalInstanceDO> result = approvalInstanceMapper.selectPage(page, query);
        assertNotNull(result);
        for (ApprovalInstanceDO item : result.getRecords()) {
            assertNull(item.getParentInstanceId(), "Should only have main flow instances");
        }
        System.out.println("✓ ApprovalInstance selectPage: " + result.getRecords().size() + " records, total: " + result.getTotal());
    }
}
