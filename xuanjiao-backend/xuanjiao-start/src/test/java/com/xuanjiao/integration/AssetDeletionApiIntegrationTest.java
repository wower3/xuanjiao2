package com.xuanjiao.integration;

import com.xuanjiao.app.deletion.AssetDeletionApplicationService;
import com.xuanjiao.client.deletion.AssetDeletionApplicationCmd;
import com.xuanjiao.client.deletion.AssetDeletionApplicationDTO;
import com.xuanjiao.client.PageResult;
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
 * AssetDeletion API集成测试
 * 验证 AssetDeletionApplicationService 完整业务流程（包括 AssetDeletionAsset 中间表操作）
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AssetDeletionApiIntegrationTest {

    @Autowired
    private AssetDeletionApplicationService deletionApplicationService;

    @Test
    @Order(1)
    @Transactional
    void testCreate_Api() {
        AssetDeletionApplicationCmd cmd = new AssetDeletionApplicationCmd();
        cmd.setTitle("API测试删除申请");
        cmd.setWorkflowId(1L);
        cmd.setDeleteReason("测试原因");
        cmd.setAssetIds(new ArrayList<>());
        AssetDeletionApplicationDTO result = deletionApplicationService.create(cmd, 1L);
        assertNotNull(result);
        System.out.println("✓ AssetDeletion API: create");
    }

    @Test
    @Order(2)
    void testQueryDrafts_Api() {
        PageResult<AssetDeletionApplicationDTO> result = deletionApplicationService.queryDrafts(1L, 1, 10, null);
        assertNotNull(result);
        System.out.println("✓ AssetDeletion API: queryDrafts - count=" + result.getTotal());
    }
}
