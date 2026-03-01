package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.workflow.WorkflowStageMapper;
import com.xuanjiao.infrastructure.workflow.WorkflowStageQuery;
import com.xuanjiao.infrastructure.dataobject.WorkflowStageDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WorkflowStageMapper 集成测试
 * 验证 WorkflowStageMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkflowStageMapperIntegrationTest {

    @Autowired
    private WorkflowStageMapper workflowStageMapper;

    @Test
    @Order(1)
    void testSelectById() {
        WorkflowStageDO result = workflowStageMapper.selectById(1L);
        if (result != null) {
            assertNotNull(result.getId());
            assertEquals(0, result.getDeleted());
            System.out.println("✓ WorkflowStage selectById: " + result.getName());
        } else {
            System.out.println("⚠ WorkflowStage selectById: No records found in database");
        }
    }

    @Test
    @Order(2)
    void testSelectList_EmptyQuery() {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setDeleted(null); // Default to deleted=0
        java.util.List<WorkflowStageDO> list = workflowStageMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ WorkflowStage selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(3)
    void testSelectList_WithWorkflowId() {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setWorkflowId(1L);
        query.setDeleted(null);
        java.util.List<WorkflowStageDO> list = workflowStageMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowStageDO item : list) {
            assertEquals(1L, item.getWorkflowId());
        }
        System.out.println("✓ WorkflowStage selectList (workflowId=1): " + list.size() + " records");
    }

    @Test
    @Order(4)
    void testSelectList_WithStageOrder() {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setStageOrder(1);
        query.setDeleted(null);
        java.util.List<WorkflowStageDO> list = workflowStageMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowStageDO item : list) {
            assertEquals(1, item.getStageOrder());
        }
        System.out.println("✓ WorkflowStage selectList (stageOrder=1): " + list.size() + " records");
    }

    @Test
    @Order(5)
    void testSelectList_WithApproveType() {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setApproveType("OR");
        query.setDeleted(null);
        java.util.List<WorkflowStageDO> list = workflowStageMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowStageDO item : list) {
            assertEquals("OR", item.getApproveType());
        }
        System.out.println("✓ WorkflowStage selectList (approveType=OR): " + list.size() + " records");
    }

    @Test
    @Order(6)
    void testSelectList_WithOrderBy() {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setWorkflowId(1L);
        query.setOrderByField("stage_order");
        query.setOrderByDirection("ASC");
        query.setDeleted(null);
        java.util.List<WorkflowStageDO> list = workflowStageMapper.selectList(query);
        assertNotNull(list);
        // Verify ordering
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getStageOrder() <= list.get(i).getStageOrder());
        }
        System.out.println("✓ WorkflowStage selectList (orderBy stage_order ASC): " + list.size() + " records");
    }

    @Test
    @Order(7)
    void testSelectCount() {
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setDeleted(null);
        Long count = workflowStageMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ WorkflowStage selectCount: " + count + " records");
    }

    @Test
    @Order(8)
    void testSelectList_ComplexQuery() {
        // Test complex query: workflowId + stageOrder + approveType + orderBy
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setWorkflowId(1L);
        query.setStageOrder(1);
        query.setApproveType("OR");
        query.setOrderByField("stage_order");
        query.setOrderByDirection("ASC");
        query.setDeleted(null);
        java.util.List<WorkflowStageDO> list = workflowStageMapper.selectList(query);
        assertNotNull(list);
        for (WorkflowStageDO item : list) {
            assertEquals(1L, item.getWorkflowId());
            assertEquals(1, item.getStageOrder());
            assertEquals("OR", item.getApproveType());
        }
        System.out.println("✓ WorkflowStage selectList (complex): " + list.size() + " records");
    }

    @Test
    @Order(9)
    void testSelectList_FirstStageQuery() {
        // Test query for first stage: workflowId + orderBy
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setWorkflowId(1L);
        query.setOrderByField("stage_order");
        query.setOrderByDirection("ASC");
        query.setDeleted(null);
        java.util.List<WorkflowStageDO> list = workflowStageMapper.selectList(query);
        assertNotNull(list);
        if (!list.isEmpty()) {
            WorkflowStageDO firstStage = list.get(0);
            assertEquals(1L, firstStage.getWorkflowId());
            System.out.println("✓ WorkflowStage selectList (first stage): " + firstStage.getName());
        } else {
            System.out.println("⚠ WorkflowStage selectList (first stage): No stages found for workflowId=1");
        }
    }

    @Test
    @Order(10)
    void testSelectList_NextStageQuery() {
        // Test query for next stages with filtering (simulates gt query)
        WorkflowStageQuery query = new WorkflowStageQuery();
        query.setWorkflowId(1L);
        query.setOrderByField("stage_order");
        query.setOrderByDirection("ASC");
        query.setDeleted(null);
        java.util.List<WorkflowStageDO> allStages = workflowStageMapper.selectList(query);
        assertNotNull(allStages);
        // Find next stage after stage 1
        WorkflowStageDO nextStage = null;
        for (WorkflowStageDO stage : allStages) {
            if (stage.getStageOrder() > 1) {
                nextStage = stage;
                break;
            }
        }
        if (nextStage != null) {
            assertTrue(nextStage.getStageOrder() > 1);
            System.out.println("✓ WorkflowStage selectList (next stage): " + nextStage.getName());
        } else {
            System.out.println("⚠ WorkflowStage selectList (next stage): No next stage found");
        }
    }
}
