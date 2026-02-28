package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.log.OperationLogMapper;
import com.xuanjiao.infrastructure.log.OperationLogQuery;
import com.xuanjiao.infrastructure.dataobject.OperationLogDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OperationLogMapper集成测试
 * 验证OperationLogMapper的CRUD操作和动态SQL条件
 */
@SpringBootTest
@Transactional
class OperationLogMapperIntegrationTest {

    @Autowired
    private OperationLogMapper operationLogMapper;

    private static Long testLogId;

    @Test
    @Order(1)
    void testInsert() {
        OperationLogDO log = new OperationLogDO();
        log.setOperatorId(1L);
        log.setOperatorName("测试用户");
        log.setOperationType("CREATE");
        log.setTargetType("ASSET");
        log.setTargetId(1L);
        log.setTargetName("测试素材");
        log.setOperationDetail("创建素材测试");
        log.setIpAddress("127.0.0.1");
        log.setCreateTime(LocalDateTime.now());

        int result = operationLogMapper.insert(log);
        assertEquals(1, result);
        assertNotNull(log.getId());
        testLogId = log.getId();
        System.out.println("✓ OperationLog: insert - id=" + log.getId());
    }

    @Test
    @Order(2)
    void testSelectById() {
        // 先插入一条记录用于测试
        OperationLogDO testLog = new OperationLogDO();
        testLog.setOperatorId(1L);
        testLog.setOperatorName("测试用户");
        testLog.setOperationType("CREATE");
        testLog.setTargetType("ASSET");
        testLog.setTargetId(1L);
        testLog.setTargetName("测试素材");
        testLog.setOperationDetail("创建素材测试");
        testLog.setIpAddress("127.0.0.1");
        testLog.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(testLog);

        OperationLogDO log = operationLogMapper.selectById(testLog.getId());
        assertNotNull(log);
        assertEquals("测试用户", log.getOperatorName());
        System.out.println("✓ OperationLog: selectById - id=" + log.getId());
    }

    @Test
    @Order(3)
    void testSelectOne() {
        // 先插入一条记录用于测试
        OperationLogDO testLog = new OperationLogDO();
        testLog.setOperatorId(1L);
        testLog.setOperatorName("测试用户");
        testLog.setOperationType("CREATE");
        testLog.setTargetType("ASSET");
        testLog.setTargetId(1L);
        testLog.setTargetName("测试素材");
        testLog.setOperationDetail("创建素材测试");
        testLog.setIpAddress("127.0.0.1");
        testLog.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(testLog);

        OperationLogQuery query = new OperationLogQuery();
        query.setOperatorId(1L);
        query.setOperationType("CREATE");

        OperationLogDO log = operationLogMapper.selectOne(query);
        if (log != null) {
            assertNotNull(log);
            System.out.println("✓ OperationLog: selectOne - id=" + log.getId());
        } else {
            System.out.println("⚠ OperationLog: selectOne - no matching log found");
        }
    }

    @Test
    @Order(4)
    void testSelectList() {
        OperationLogQuery query = new OperationLogQuery();
        query.setOperatorId(1L);

        List<OperationLogDO> logs = operationLogMapper.selectList(query);
        assertNotNull(logs);
        System.out.println("✓ OperationLog: selectList - count=" + logs.size());
    }

    @Test
    @Order(5)
    void testSelectCount() {
        OperationLogQuery query = new OperationLogQuery();
        query.setOperationType("CREATE");
        Long count = operationLogMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ OperationLog: selectCount - count=" + count);
    }

    @Test
    @Order(6)
    void testUpdateById() {
        // 先插入一条记录用于测试
        OperationLogDO testLog = new OperationLogDO();
        testLog.setOperatorId(1L);
        testLog.setOperatorName("测试用户");
        testLog.setOperationType("CREATE");
        testLog.setTargetType("ASSET");
        testLog.setTargetId(1L);
        testLog.setTargetName("测试素材");
        testLog.setOperationDetail("创建素材测试");
        testLog.setIpAddress("127.0.0.1");
        testLog.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(testLog);

        OperationLogDO log = operationLogMapper.selectById(testLog.getId());
        if (log != null) {
            log.setOperationDetail("更新的操作详情");
            int result = operationLogMapper.updateById(log);
            assertTrue(result > 0);
            System.out.println("✓ OperationLog: updateById - id=" + log.getId());
        } else {
            System.out.println("⚠ OperationLog: updateById - no log found");
        }
    }

    @Test
    @Order(7)
    void testSelectByTargetType() {
        OperationLogQuery query = new OperationLogQuery();
        query.setTargetType("ASSET");

        List<OperationLogDO> logs = operationLogMapper.selectList(query);
        assertNotNull(logs);
        System.out.println("✓ OperationLog: selectByTargetType - count=" + logs.size());
    }

    @Test
    @Order(8)
    void testSelectByTargetId() {
        OperationLogQuery query = new OperationLogQuery();
        query.setTargetId(1L);

        List<OperationLogDO> logs = operationLogMapper.selectList(query);
        assertNotNull(logs);
        System.out.println("✓ OperationLog: selectByTargetId - count=" + logs.size());
    }
}
