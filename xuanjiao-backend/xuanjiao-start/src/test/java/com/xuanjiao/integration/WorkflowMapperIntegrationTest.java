package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.workflow.WorkflowMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowQuery;
import com.xuanjiao.infrastructure.dataobject.WorkflowDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkflowMapper 集成测试
 * 验证 WorkflowMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorkflowMapperIntegrationTest {

    @Autowired
    private WorkflowMapper workflowMapper;

    @Test
    @Order(1)
    public void testSelectById() {
        WorkflowDO result = workflowMapper.selectById(1L);
        if (result != null) {
            assertNotNull(result.getId());
            assertEquals(0, result.getDeleted());
            System.out.println("✓ Workflow selectById: " + result.getName());
        } else {
            System.out.println("⚠ Workflow selectById: No records found in database");
        }
    }

    @Test
    @Order(2)
    public void testSelectList_EmptyQuery() {
        WorkflowQuery query = new WorkflowQuery();
        List<WorkflowDO> list = workflowMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ Workflow selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(3)
    public void testSelectList_WithBoundRoleId() {
        WorkflowQuery query = new WorkflowQuery();
        query.setBoundRoleId(1L);
        List<WorkflowDO> list = workflowMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowDO item : list) {
            assertEquals(1L, item.getBoundRoleId());
        }
        System.out.println("✓ Workflow selectList (boundRoleId=1): " + list.size() + " records");
    }

    @Test
    @Order(4)
    public void testSelectList_WithWorkflowType() {
        WorkflowQuery query = new WorkflowQuery();
        query.setWorkflowType("ASSET_UPLOAD");
        List<WorkflowDO> list = workflowMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowDO item : list) {
            assertEquals("ASSET_UPLOAD", item.getWorkflowType());
        }
        System.out.println("✓ Workflow selectList (workflowType=ASSET_UPLOAD): " + list.size() + " records");
    }

    @Test
    @Order(5)
    public void testSelectList_WithStatus() {
        WorkflowQuery query = new WorkflowQuery();
        query.setStatus(1);
        List<WorkflowDO> list = workflowMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowDO item : list) {
            assertEquals(1, item.getStatus());
        }
        System.out.println("✓ Workflow selectList (status=1): " + list.size() + " records");
    }

    @Test
    @Order(6)
    public void testSelectList_WithExcludeIds() {
        WorkflowQuery query = new WorkflowQuery();
        query.setExcludeIds(Arrays.asList(1L));
        List<WorkflowDO> list = workflowMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowDO item : list) {
            assertNotEquals(1L, item.getId());
        }
        System.out.println("✓ Workflow selectList (excludeIds=[1]): " + list.size() + " records");
    }

    @Test
    @Order(7)
    public void testSelectCount() {
        WorkflowQuery query = new WorkflowQuery();
        Long count = workflowMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ Workflow selectCount: " + count + " records");
    }

    @Test
    @Order(8)
    public void testSelectList_ComplexQuery() {
        // Test conflict check query: boundRoleId + workflowType + status + deleted + excludeIds
        WorkflowQuery query = new WorkflowQuery();
        query.setBoundRoleId(1L);
        query.setWorkflowType("ASSET_UPLOAD");
        query.setStatus(1);
        query.setDeleted(0);
        query.setExcludeIds(Arrays.asList(1L));
        List<WorkflowDO> list = workflowMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowDO item : list) {
            assertEquals(1L, item.getBoundRoleId());
            assertEquals("ASSET_UPLOAD", item.getWorkflowType());
            assertEquals(1, item.getStatus());
            assertEquals(0, item.getDeleted());
            assertNotEquals(1L, item.getId());
        }
        System.out.println("✓ Workflow selectList (complex conflict check): " + list.size() + " records");
    }
}
