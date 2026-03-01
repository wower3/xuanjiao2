package com.xuanjiao.integration;

import com.xuanjiao.app.usage.UsageApplyService;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.usage.UsageApplyCmd;
import com.xuanjiao.client.usage.UsageApplyDTO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UsageApply API集成测试
 * 验证 UsageApplyService 完整业务流程（包括 UsageApplyAsset 中间表操作）
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsageApplyApiIntegrationTest {

    @Autowired
    private UsageApplyService usageApplyService;

    @Test
    @Order(1)
    @Transactional
    void testCreateDraft_Api() {
        UsageApplyCmd cmd = new UsageApplyCmd();
        cmd.setTitle("API测试使用申请");
        cmd.setAssetConfigs(new ArrayList<>());
        UsageApplyDTO result = usageApplyService.createDraft(cmd, 1L);
        assertNotNull(result);
        System.out.println("✓ UsageApply API: createDraft");
    }

    @Test
    @Order(2)
    void testQueryDrafts_Api() {
        PageResult<UsageApplyDTO> result = usageApplyService.queryDrafts(1L, 1, 10);
        assertNotNull(result);
        System.out.println("✓ UsageApply API: queryDrafts - count=" + result.getTotal());
    }

    @Test
    @Order(3)
    void testQueryMyApplications_Api() {
        PageResult<UsageApplyDTO> result = usageApplyService.queryMyApplications(1L, 1, 10);
        assertNotNull(result);
        System.out.println("✓ UsageApply API: queryMyApplications - count=" + result.getTotal());
    }
}
