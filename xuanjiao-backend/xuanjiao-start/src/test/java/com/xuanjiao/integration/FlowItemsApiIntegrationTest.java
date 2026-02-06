package com.xuanjiao.integration;

import com.xuanjiao.app.approval.ApprovalService;
import com.xuanjiao.client.dto.PageResult;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlowItems API集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlowItemsApiIntegrationTest {

    @Autowired
    private ApprovalService approvalService;

    @Test
    @Order(1)
    public void testGetMyFlowItems_Basic() {
        PageResult<Map<String, Object>> result = approvalService.getMyFlowItems(1L, 1, 10, null, null);
        assertNotNull(result);
    }

    @Test
    @Order(2)
    public void testGetMyFlowItems_FilterByBusinessType() {
        PageResult<Map<String, Object>> result1 = approvalService.getMyFlowItems(1L, 1, 10, "MATERIAL_ENTRY", null);
        assertNotNull(result1);

        PageResult<Map<String, Object>> result2 = approvalService.getMyFlowItems(1L, 1, 10, "ASSET_USAGE", null);
        assertNotNull(result2);

        PageResult<Map<String, Object>> result3 = approvalService.getMyFlowItems(1L, 1, 10, "ASSET_DELETION", null);
        assertNotNull(result3);
    }

    @Test
    @Order(3)
    public void testGetMyFlowItems_FilterByStatus() {
        PageResult<Map<String, Object>> result1 = approvalService.getMyFlowItems(1L, 1, 10, null, "PENDING");
        assertNotNull(result1);

        PageResult<Map<String, Object>> result2 = approvalService.getMyFlowItems(1L, 1, 10, null, "APPROVED");
        assertNotNull(result2);

        PageResult<Map<String, Object>> result3 = approvalService.getMyFlowItems(1L, 1, 10, null, "REJECTED");
        assertNotNull(result3);
    }

    @Test
    @Order(4)
    public void testGetMyFlowItems_WithPagination() {
        PageResult<Map<String, Object>> page1 = approvalService.getMyFlowItems(1L, 1, 5, null, null);
        assertNotNull(page1);
        assertNotNull(page1.getList());

        if (page1.getTotal() > 5) {
            PageResult<Map<String, Object>> page2 = approvalService.getMyFlowItems(1L, 2, 5, null, null);
            assertNotNull(page2);
        }
    }

    @Test
    @Order(5)
    public void testGetMyFlowItems_WithMyRole() {
        PageResult<Map<String, Object>> result = approvalService.getMyFlowItems(1L, 1, 20, null, null);
        assertNotNull(result);

        for (Map<String, Object> item : result.getList()) {
            Object myRole = item.get("myRole");
            assertNotNull(myRole);
            assertTrue("initiator".equals(myRole) || "approver".equals(myRole));
        }
    }

    @Test
    @Order(6)
    public void testGetMyFlowItems_CombinedFilters() {
        PageResult<Map<String, Object>> result = approvalService.getMyFlowItems(1L, 1, 10, "MATERIAL_ENTRY", "APPROVED");
        assertNotNull(result);
    }

    @Test
    @Order(7)
    public void testGetMyFlowItems_DifferentUsers() {
        PageResult<Map<String, Object>> user1Result = approvalService.getMyFlowItems(1L, 1, 10, null, null);
        assertNotNull(user1Result);

        PageResult<Map<String, Object>> user2Result = approvalService.getMyFlowItems(2L, 1, 10, null, null);
        assertNotNull(user2Result);
    }

    @Test
    @Order(8)
    public void testGetMyFlowItems_DataIntegrity() {
        PageResult<Map<String, Object>> result = approvalService.getMyFlowItems(1L, 1, 10, null, null);

        assertNotNull(result.getTotal());
        assertTrue(result.getTotal() >= 0);
        assertNotNull(result.getList());

        for (Map<String, Object> item : result.getList()) {
            assertNotNull(item.get("id"));
            assertNotNull(item.get("status"));
            assertNotNull(item.get("myRole"));

            String status = (String) item.get("status");
            assertTrue("PENDING".equals(status) || "APPROVED".equals(status) ||
                       "REJECTED".equals(status) || "CANCELLED".equals(status));
        }
    }
}
