package com.xuanjiao.integration;

import com.xuanjiao.app.approval.ApprovalService;
import com.xuanjiao.client.dto.PageResult;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Approval API集成测试
 * 验证 ApprovalService 完整业务流程（包括 ApprovalInstance 和相关实体的操作）
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApprovalApiIntegrationTest {

    @Autowired
    private ApprovalService approvalService;

    @Test
    @Order(1)
    public void testGetMyTasks_Api() {
        PageResult<Map<String, Object>> result = approvalService.getMyTasks(1L, 1, 10);
        assertNotNull(result);
        System.out.println("✓ Approval API: getMyTasks - count=" + result.getTotal());
    }

    @Test
    @Order(2)
    public void testGetInstanceDetail_Api() {
        PageResult<Map<String, Object>> tasks = approvalService.getMyTasks(1L, 1, 10);
        if (tasks.getList().isEmpty()) {
            System.out.println("⚠ Approval API: 没有待办任务，跳过详情测试");
            return;
        }

        Map<String, Object> task = tasks.getList().get(0);
        Object instanceIdObj = task.get("instanceId");
        if (instanceIdObj == null) {
            System.out.println("⚠ Approval API: 任务中没有instanceId，跳过详情测试");
            return;
        }

        Long instanceId = Long.valueOf(instanceIdObj.toString());
        Map<String, Object> result = approvalService.getInstanceDetail(instanceId);
        assertNotNull(result);
        System.out.println("✓ Approval API: getInstanceDetail - instanceId=" + instanceId);
    }
}
