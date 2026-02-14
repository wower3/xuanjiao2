package com.xuanjiao.integration;

import com.xuanjiao.app.approval.ApprovalService;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.approval.FlowItemDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlowItems API集成测试
 *
 * <p>验证流经事项查询功能的正确性。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlowItemsApiIntegrationTest {

    @Autowired
    private ApprovalService approvalService;

    @Test
    @Order(1)
    public void testGetMyFlowItems_Basic() {
        PageResult<FlowItemDTO> result = approvalService.getMyFlowItems(1L, 1, 10, null, null);
        assertNotNull(result);
    }

    @Test
    @Order(2)
    public void testGetMyFlowItems_FilterByBusinessType() {
        PageResult<FlowItemDTO> result1 = approvalService.getMyFlowItems(1L, 1, 10, "MATERIAL_ENTRY", null);
        assertNotNull(result1);

        PageResult<FlowItemDTO> result2 = approvalService.getMyFlowItems(1L, 1, 10, "ASSET_USAGE", null);
        assertNotNull(result2);

        PageResult<FlowItemDTO> result3 = approvalService.getMyFlowItems(1L, 1, 10, "ASSET_DELETION", null);
        assertNotNull(result3);
    }

    @Test
    @Order(3)
    public void testGetMyFlowItems_FilterByStatus() {
        PageResult<FlowItemDTO> result1 = approvalService.getMyFlowItems(1L, 1, 10, null, "PENDING");
        assertNotNull(result1);

        PageResult<FlowItemDTO> result2 = approvalService.getMyFlowItems(1L, 1, 10, null, "APPROVED");
        assertNotNull(result2);

        PageResult<FlowItemDTO> result3 = approvalService.getMyFlowItems(1L, 1, 10, null, "REJECTED");
        assertNotNull(result3);
    }

    @Test
    @Order(4)
    public void testGetMyFlowItems_WithPagination() {
        PageResult<FlowItemDTO> page1 = approvalService.getMyFlowItems(1L, 1, 5, null, null);
        assertNotNull(page1);
        assertNotNull(page1.getList());

        if (page1.getTotal() > 5) {
            PageResult<FlowItemDTO> page2 = approvalService.getMyFlowItems(1L, 2, 5, null, null);
            assertNotNull(page2);
        }
    }

    @Test
    @Order(5)
    public void testGetMyFlowItems_WithMyRole() {
        PageResult<FlowItemDTO> result = approvalService.getMyFlowItems(1L, 1, 20, null, null);
        assertNotNull(result);

        for (FlowItemDTO item : result.getList()) {
            String myRole = item.getMyRole();
            assertNotNull(myRole);
            assertTrue("initiator".equals(myRole) || "approver".equals(myRole));
        }
    }

    @Test
    @Order(6)
    public void testGetMyFlowItems_CombinedFilters() {
        PageResult<FlowItemDTO> result = approvalService.getMyFlowItems(1L, 1, 10, "MATERIAL_ENTRY", "APPROVED");
        assertNotNull(result);
    }

    @Test
    @Order(7)
    public void testGetMyFlowItems_DifferentUsers() {
        PageResult<FlowItemDTO> user1Result = approvalService.getMyFlowItems(1L, 1, 10, null, null);
        assertNotNull(user1Result);

        PageResult<FlowItemDTO> user2Result = approvalService.getMyFlowItems(2L, 1, 10, null, null);
        assertNotNull(user2Result);
    }

    @Test
    @Order(8)
    public void testGetMyFlowItems_DataIntegrity() {
        PageResult<FlowItemDTO> result = approvalService.getMyFlowItems(1L, 1, 10, null, null);

        assertNotNull(result.getTotal());
        assertTrue(result.getTotal() >= 0);
        assertNotNull(result.getList());

        for (FlowItemDTO item : result.getList()) {
            assertNotNull(item.getId());
            assertNotNull(item.getStatus());
            assertNotNull(item.getMyRole());

            String status = item.getStatus();
            assertTrue("PENDING".equals(status) || "APPROVED".equals(status) ||
                       "REJECTED".equals(status) || "CANCELLED".equals(status));
        }
    }
}
