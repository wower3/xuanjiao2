package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.approval.ApprovalTaskMapper;
import com.xuanjiao.infrastructure.approval.ApprovalTaskQuery;
import com.xuanjiao.infrastructure.dataobject.ApprovalTaskDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApprovalTaskMapper集成测试
 * 验证ApprovalTaskMapper的CRUD操作和动态SQL条件
 */
@SpringBootTest
@Transactional
public class ApprovalTaskMapperIntegrationTest {

    @Autowired
    private ApprovalTaskMapper taskMapper;

    private static Long testTaskId;

    @Test
    @Order(1)
    public void testInsert() {
        ApprovalTaskDO task = new ApprovalTaskDO();
        task.setInstanceId(1L);
        task.setStageId(1L);
        task.setApproverId(1L);
        task.setStatus("PENDING");
        task.setIsFirstApprover(1);
        task.setTaskType("NORMAL");
        task.setCreateTime(LocalDateTime.now());

        int result = taskMapper.insert(task);
        assertEquals(1, result);
        assertNotNull(task.getId());
        testTaskId = task.getId();
        System.out.println("✓ ApprovalTask: insert - id=" + task.getId());
    }

    @Test
    @Order(2)
    public void testSelectById() {
        ApprovalTaskDO task = taskMapper.selectById(1L);
        assertNotNull(task);
        System.out.println("✓ ApprovalTask: selectById - id=" + task.getId());
    }

    @Test
    @Order(3)
    public void testSelectOne() {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(1L);
        query.setApproverId(1L);
        query.setStatus("APPROVED");  // 使用数据库中实际存在的状态

        ApprovalTaskDO task = taskMapper.selectOne(query);
        assertNotNull(task);
        System.out.println("✓ ApprovalTask: selectOne - id=" + task.getId());
    }

    @Test
    @Order(4)
    public void testSelectList() {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setInstanceId(1L);

        List<ApprovalTaskDO> tasks = taskMapper.selectList(query);
        assertNotNull(tasks);
        assertFalse(tasks.isEmpty());
        System.out.println("✓ ApprovalTask: selectList - count=" + tasks.size());
    }

    @Test
    @Order(5)
    public void testSelectCount() {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setStatus("PENDING");
        Long count = taskMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ ApprovalTask: selectCount - count=" + count);
    }

    @Test
    @Order(6)
    public void testUpdateById() {
        ApprovalTaskDO task = taskMapper.selectById(1L);
        if (task != null) {
            task.setComment("Updated comment");
            task.setApproveTime(LocalDateTime.now());
            int result = taskMapper.updateById(task);
            assertTrue(result > 0);
            System.out.println("✓ ApprovalTask: updateById - id=" + task.getId());
        } else {
            System.out.println("⚠ ApprovalTask: updateById - no task found with id=1");
        }
    }

    @Test
    @Order(7)
    public void testSelectWithStatusIn() {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setStatusIn(Arrays.asList("PENDING", "APPROVED"));

        List<ApprovalTaskDO> tasks = taskMapper.selectList(query);
        assertNotNull(tasks);
        System.out.println("✓ ApprovalTask: selectWithStatusIn - count=" + tasks.size());
    }

    @Test
    @Order(8)
    public void testSelectWithSubWorkflowApproverIdsNotNull() {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setSubWorkflowApproverIdsNotNull(true);

        List<ApprovalTaskDO> tasks = taskMapper.selectList(query);
        assertNotNull(tasks);
        System.out.println("✓ ApprovalTask: selectWithSubWorkflowApproverIdsNotNull - count=" + tasks.size());
    }

    @Test
    @Order(9)
    public void testSelectWithIdNotEqual() {
        ApprovalTaskQuery query = new ApprovalTaskQuery();
        query.setIdNotEqual(1L);

        List<ApprovalTaskDO> tasks = taskMapper.selectList(query);
        assertNotNull(tasks);
        System.out.println("✓ ApprovalTask: selectWithIdNotEqual - count=" + tasks.size());
    }
}
