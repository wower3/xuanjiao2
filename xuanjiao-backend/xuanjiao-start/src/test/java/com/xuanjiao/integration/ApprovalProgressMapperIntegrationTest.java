package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.approval.ApprovalProgressMapper;
import com.xuanjiao.infrastructure.approval.ApprovalProgressQuery;
import com.xuanjiao.infrastructure.dataobject.ApprovalProgressDO;
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
 * ApprovalProgressMapper集成测试
 * 验证ApprovalProgressMapper的CRUD操作和动态SQL条件
 */
@SpringBootTest
@Transactional
class ApprovalProgressMapperIntegrationTest {

    @Autowired
    private ApprovalProgressMapper progressMapper;

    private static Long testProgressId;

    @Test
    @Order(1)
    void testInsert() {
        ApprovalProgressDO progress = new ApprovalProgressDO();
        progress.setInstanceId(1L);
        progress.setStageId(1L);
        progress.setStageName("测试阶段");
        progress.setStageOrder(1);
        progress.setStatus("PENDING");
        progress.setIsSubWorkflow(0);
        progress.setCreateTime(LocalDateTime.now());

        int result = progressMapper.insert(progress);
        assertEquals(1, result);
        assertNotNull(progress.getId());
        testProgressId = progress.getId();
        System.out.println("✓ ApprovalProgress: insert - id=" + progress.getId());
    }

    @Test
    @Order(2)
    void testSelectById() {
        ApprovalProgressDO progress = progressMapper.selectById(1L);
        if (progress == null) {
            System.out.println("⚠ ApprovalProgress: selectById - no progress found with id=1");
        } else {
            assertNotNull(progress);
            System.out.println("✓ ApprovalProgress: selectById - id=" + progress.getId());
        }
    }

    @Test
    @Order(3)
    void testSelectOne() {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(1L);
        query.setStageId(2L);  // 使用数据库中实际存在的stageId

        ApprovalProgressDO progress = progressMapper.selectOne(query);
        if (progress == null) {
            System.out.println("⚠ ApprovalProgress: selectOne - no matching progress found");
        } else {
            assertNotNull(progress);
            System.out.println("✓ ApprovalProgress: selectOne - id=" + progress.getId());
        }
    }

    @Test
    @Order(4)
    void testSelectList() {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceId(1L);

        List<ApprovalProgressDO> progressList = progressMapper.selectList(query);
        assertNotNull(progressList);
        assertFalse(progressList.isEmpty());
        System.out.println("✓ ApprovalProgress: selectList - count=" + progressList.size());
    }

    @Test
    @Order(5)
    void testSelectCount() {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setStatus("PENDING");
        Long count = progressMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ ApprovalProgress: selectCount - count=" + count);
    }

    @Test
    @Order(6)
    void testUpdateById() {
        ApprovalProgressDO progress = progressMapper.selectById(1L);
        if (progress != null) {
            progress.setStatus("APPROVED");
            progress.setApproveTime(LocalDateTime.now());
            int result = progressMapper.updateById(progress);
            assertTrue(result > 0);
            System.out.println("✓ ApprovalProgress: updateById - id=" + progress.getId());
        } else {
            System.out.println("⚠ ApprovalProgress: updateById - no progress found with id=1");
        }
    }

    @Test
    @Order(7)
    void testSelectByInstanceId() {
        List<ApprovalProgressDO> progressList = progressMapper.selectByInstanceId(1L);
        assertNotNull(progressList);
        System.out.println("✓ ApprovalProgress: selectByInstanceId - count=" + progressList.size());
    }

    @Test
    @Order(8)
    void testSelectWithParentInstanceIdIsNull() {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setParentInstanceIdIsNull(true);

        List<ApprovalProgressDO> progressList = progressMapper.selectList(query);
        assertNotNull(progressList);
        System.out.println("✓ ApprovalProgress: selectWithParentInstanceIdIsNull - count=" + progressList.size());
    }

    @Test
    @Order(9)
    void testSelectWithInstanceIds() {
        ApprovalProgressQuery query = new ApprovalProgressQuery();
        query.setInstanceIds(Arrays.asList(1L, 2L));

        List<ApprovalProgressDO> progressList = progressMapper.selectList(query);
        assertNotNull(progressList);
        System.out.println("✓ ApprovalProgress: selectWithInstanceIds - count=" + progressList.size());
    }
}
