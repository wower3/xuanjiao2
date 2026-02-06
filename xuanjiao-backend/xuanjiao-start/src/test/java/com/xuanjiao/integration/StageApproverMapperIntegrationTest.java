package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.workflow.StageApproverMapper;
import com.xuanjiao.infrastructure.workflow.StageApproverQuery;
import com.xuanjiao.infrastructure.dataobject.StageApproverDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StageApproverMapper 集成测试
 * 验证 StageApproverMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StageApproverMapperIntegrationTest {

    @Autowired
    private StageApproverMapper stageApproverMapper;

    @Test
    @Order(1)
    public void testSelectById() {
        StageApproverDO result = stageApproverMapper.selectById(1L);
        if (result != null) {
            assertNotNull(result.getId());
            System.out.println("✓ StageApprover selectById: approverType=" + result.getApproverType());
        } else {
            System.out.println("⚠ StageApprover selectById: No records found in database");
        }
    }

    @Test
    @Order(2)
    public void testSelectList_EmptyQuery() {
        StageApproverQuery query = new StageApproverQuery();
        java.util.List<StageApproverDO> list = stageApproverMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ StageApprover selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(3)
    public void testSelectList_WithStageId() {
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(1L);
        java.util.List<StageApproverDO> list = stageApproverMapper.selectList(query);
        assertNotNull(list);
        for (StageApproverDO item : list) {
            assertEquals(1L, item.getStageId());
        }
        System.out.println("✓ StageApprover selectList (stageId=1): " + list.size() + " records");
    }

    @Test
    @Order(4)
    public void testSelectList_WithApproverType() {
        StageApproverQuery query = new StageApproverQuery();
        query.setApproverType("USER");
        java.util.List<StageApproverDO> list = stageApproverMapper.selectList(query);
        assertNotNull(list);
        for (StageApproverDO item : list) {
            assertEquals("USER", item.getApproverType());
        }
        System.out.println("✓ StageApprover selectList (approverType=USER): " + list.size() + " records");
    }

    @Test
    @Order(5)
    public void testSelectList_WithSubWorkflowId() {
        StageApproverQuery query = new StageApproverQuery();
        // Try to find a stage with sub-workflow
        java.util.List<StageApproverDO> allApprovers = stageApproverMapper.selectList(query);
        StageApproverDO withSubWorkflow = null;
        for (StageApproverDO approver : allApprovers) {
            if (approver.getSubWorkflowId() != null) {
                withSubWorkflow = approver;
                break;
            }
        }
        if (withSubWorkflow != null) {
            StageApproverQuery subWorkflowQuery = new StageApproverQuery();
            subWorkflowQuery.setSubWorkflowId(withSubWorkflow.getSubWorkflowId());
            java.util.List<StageApproverDO> list = stageApproverMapper.selectList(subWorkflowQuery);
            assertNotNull(list);
            for (StageApproverDO item : list) {
                assertEquals(withSubWorkflow.getSubWorkflowId(), item.getSubWorkflowId());
            }
            System.out.println("✓ StageApprover selectList (subWorkflowId): " + list.size() + " records");
        } else {
            System.out.println("⚠ StageApprover selectList (subWorkflowId): No sub-workflows found in database");
        }
    }

    @Test
    @Order(6)
    public void testSelectList_SubWorkflowIdNotNull() {
        // Test IS NOT NULL query
        StageApproverQuery query = new StageApproverQuery();
        query.setSubWorkflowIdNotNull(true);
        java.util.List<StageApproverDO> list = stageApproverMapper.selectList(query);
        assertNotNull(list);
        for (StageApproverDO item : list) {
            assertNotNull(item.getSubWorkflowId());
        }
        System.out.println("✓ StageApprover selectList (subWorkflowId IS NOT NULL): " + list.size() + " records");
    }

    @Test
    @Order(7)
    public void testSelectList_SubWorkflowIdNull() {
        // Test IS NULL query
        StageApproverQuery query = new StageApproverQuery();
        query.setSubWorkflowIdNull(true);
        java.util.List<StageApproverDO> list = stageApproverMapper.selectList(query);
        assertNotNull(list);
        for (StageApproverDO item : list) {
            assertNull(item.getSubWorkflowId());
        }
        System.out.println("✓ StageApprover selectList (subWorkflowId IS NULL): " + list.size() + " records");
    }

    @Test
    @Order(8)
    public void testSelectList_WithOrderBy() {
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(1L);
        query.setOrderByField("id");
        query.setOrderByDirection("ASC");
        java.util.List<StageApproverDO> list = stageApproverMapper.selectList(query);
        assertNotNull(list);
        // Verify ordering
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1).getId() <= list.get(i).getId());
        }
        System.out.println("✓ StageApprover selectList (orderBy id ASC): " + list.size() + " records");
    }

    @Test
    @Order(9)
    public void testSelectCount() {
        StageApproverQuery query = new StageApproverQuery();
        Long count = stageApproverMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ StageApprover selectCount: " + count + " records");
    }

    @Test
    @Order(10)
    public void testSelectList_ComplexQuery() {
        // Test complex query: stageId + approverType + orderBy
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(1L);
        query.setApproverType("USER");
        query.setSubWorkflowIdNull(true);
        query.setOrderByField("id");
        query.setOrderByDirection("ASC");
        java.util.List<StageApproverDO> list = stageApproverMapper.selectList(query);
        assertNotNull(list);
        for (StageApproverDO item : list) {
            assertEquals(1L, item.getStageId());
            assertEquals("USER", item.getApproverType());
            assertNull(item.getSubWorkflowId());
        }
        System.out.println("✓ StageApprover selectList (complex): " + list.size() + " records");
    }

    @Test
    @Order(11)
    public void testSelectList_AllApproversForStage() {
        // Test query for all approvers of a stage (common pattern in WorkflowServiceImpl)
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(1L);
        java.util.List<StageApproverDO> approvers = stageApproverMapper.selectList(query);
        assertNotNull(approvers);
        System.out.println("✓ StageApprover selectList (all approvers for stage): " + approvers.size() + " records");
    }

    @Test
    @Order(12)
    public void testSelectList_SubWorkflowApprovers() {
        // Test query for sub-workflow approvers only (used in startSubProcessesForStage)
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(1L);
        query.setSubWorkflowIdNotNull(true);
        java.util.List<StageApproverDO> subWorkflowApprovers = stageApproverMapper.selectList(query);
        assertNotNull(subWorkflowApprovers);
        for (StageApproverDO item : subWorkflowApprovers) {
            assertEquals(1L, item.getStageId());
            assertNotNull(item.getSubWorkflowId());
        }
        System.out.println("✓ StageApprover selectList (sub-workflow approvers): " + subWorkflowApprovers.size() + " records");
    }

    @Test
    @Order(13)
    public void testSelectList_NonSubWorkflowApprovers() {
        // Test query for non-sub-workflow approvers (used in getTaskDetail)
        StageApproverQuery query = new StageApproverQuery();
        query.setStageId(1L);
        query.setSubWorkflowIdNull(true);
        query.setOrderByField("id");
        query.setOrderByDirection("ASC");
        java.util.List<StageApproverDO> approvers = stageApproverMapper.selectList(query);
        assertNotNull(approvers);
        for (StageApproverDO item : approvers) {
            assertEquals(1L, item.getStageId());
            assertNull(item.getSubWorkflowId());
        }
        System.out.println("✓ StageApprover selectList (non-sub-workflow approvers): " + approvers.size() + " records");
    }
}
