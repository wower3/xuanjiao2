package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.usage.UsageApplyAssetMapper;
import com.xuanjiao.infrastructure.usage.UsageApplyAssetQuery;
import com.xuanjiao.infrastructure.dataobject.UsageApplyAssetDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UsageApplyAssetMapper 集成测试
 * 验证 UsageApplyAssetMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsageApplyAssetMapperIntegrationTest {

    @Autowired
    private UsageApplyAssetMapper usageApplyAssetMapper;

    @Test
    @Order(1)
    void testSelectList_EmptyQuery() {
        UsageApplyAssetQuery query = new UsageApplyAssetQuery();
        java.util.List<UsageApplyAssetDO> list = usageApplyAssetMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ UsageApplyAsset selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(2)
    void testSelectList_WithUsageApplyId() {
        // Find a usage apply with assets
        UsageApplyAssetQuery query = new UsageApplyAssetQuery();
        java.util.List<UsageApplyAssetDO> allAssets = usageApplyAssetMapper.selectList(query);

        if (!allAssets.isEmpty()) {
            Long applyId = allAssets.get(0).getUsageApplyId();
            UsageApplyAssetQuery applyQuery = new UsageApplyAssetQuery();
            applyQuery.setUsageApplyId(applyId);
            java.util.List<UsageApplyAssetDO> list = usageApplyAssetMapper.selectList(applyQuery);
            assertNotNull(list);
            for (UsageApplyAssetDO item : list) {
                assertEquals(applyId, item.getUsageApplyId());
            }
            System.out.println("✓ UsageApplyAsset selectList (usageApplyId=" + applyId + "): " + list.size() + " records");
        } else {
            System.out.println("⚠ UsageApplyAsset selectList (usageApplyId): No records found in database");
        }
    }

    @Test
    @Order(3)
    void testSelectList_WithAssetId() {
        // Find an asset with usage applies
        UsageApplyAssetQuery query = new UsageApplyAssetQuery();
        java.util.List<UsageApplyAssetDO> allAssets = usageApplyAssetMapper.selectList(query);

        if (!allAssets.isEmpty()) {
            Long assetId = allAssets.get(0).getAssetId();
            UsageApplyAssetQuery assetQuery = new UsageApplyAssetQuery();
            assetQuery.setAssetId(assetId);
            java.util.List<UsageApplyAssetDO> list = usageApplyAssetMapper.selectList(assetQuery);
            assertNotNull(list);
            for (UsageApplyAssetDO item : list) {
                assertEquals(assetId, item.getAssetId());
            }
            System.out.println("✓ UsageApplyAsset selectList (assetId=" + assetId + "): " + list.size() + " records");
        } else {
            System.out.println("⚠ UsageApplyAsset selectList (assetId): No records found in database");
        }
    }

    @Test
    @Order(4)
    void testSelectCount() {
        UsageApplyAssetQuery query = new UsageApplyAssetQuery();
        Long count = usageApplyAssetMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ UsageApplyAsset selectCount: " + count + " records");
    }

    @Test
    @Order(5)
    void testInsertAndDelete() {
        // Test insert
        UsageApplyAssetDO newRecord = new UsageApplyAssetDO();
        newRecord.setUsageApplyId(999999L);
        newRecord.setAssetId(999999L);
        newRecord.setUsageDescription("测试描述");
        newRecord.setUsagePublishChannel("测试渠道");
        newRecord.setUsageIsSecondaryCreation(0);

        int insertResult = usageApplyAssetMapper.insert(newRecord);
        assertTrue(insertResult > 0);
        assertNotNull(newRecord.getId());
        System.out.println("✓ UsageApplyAsset insert: 1 record inserted, id=" + newRecord.getId());

        // Test delete by query
        UsageApplyAssetQuery deleteQuery = new UsageApplyAssetQuery();
        deleteQuery.setUsageApplyId(999999L);
        int deleteResult = usageApplyAssetMapper.delete(deleteQuery);
        assertTrue(deleteResult > 0);
        System.out.println("✓ UsageApplyAsset delete: " + deleteResult + " record(s) deleted");
    }

    @Test
    @Order(6)
    void testFindByUsageApplyIdWithAsset() {
        // Test custom query method with asset details
        UsageApplyAssetQuery query = new UsageApplyAssetQuery();
        java.util.List<UsageApplyAssetDO> allRecords = usageApplyAssetMapper.selectList(query);

        if (!allRecords.isEmpty()) {
            Long applyId = allRecords.get(0).getUsageApplyId();
            java.util.List<UsageApplyAssetDO> list = usageApplyAssetMapper.findByUsageApplyIdWithAsset(applyId);
            assertNotNull(list);
            System.out.println("✓ UsageApplyAsset findByUsageApplyIdWithAsset: " + list.size() + " records");
        } else {
            System.out.println("⚠ UsageApplyAsset findByUsageApplyIdWithAsset: No records found in database");
        }
    }
}
