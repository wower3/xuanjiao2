package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetMapper;
import com.xuanjiao.infrastructure.deletion.AssetDeletionAssetQuery;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionAssetDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssetDeletionAssetMapper 集成测试
 * 验证 AssetDeletionAssetMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssetDeletionAssetMapperIntegrationTest {

    @Autowired
    private AssetDeletionAssetMapper assetDeletionAssetMapper;

    @Test
    @Order(1)
    public void testSelectList_EmptyQuery() {
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        java.util.List<AssetDeletionAssetDO> list = assetDeletionAssetMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ AssetDeletionAsset selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(2)
    public void testSelectList_WithDeletionApplicationId() {
        // Find a deletion application with assets
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        java.util.List<AssetDeletionAssetDO> allAssets = assetDeletionAssetMapper.selectList(query);

        if (!allAssets.isEmpty()) {
            Long applicationId = allAssets.get(0).getDeletionApplicationId();
            AssetDeletionAssetQuery applicationQuery = new AssetDeletionAssetQuery();
            applicationQuery.setDeletionApplicationId(applicationId);
            java.util.List<AssetDeletionAssetDO> list = assetDeletionAssetMapper.selectList(applicationQuery);
            assertNotNull(list);
            for (AssetDeletionAssetDO item : list) {
                assertEquals(applicationId, item.getDeletionApplicationId());
            }
            System.out.println("✓ AssetDeletionAsset selectList (deletionApplicationId=" + applicationId + "): " + list.size() + " records");
        } else {
            System.out.println("⚠ AssetDeletionAsset selectList (deletionApplicationId): No records found in database");
        }
    }

    @Test
    @Order(3)
    public void testSelectList_WithAssetId() {
        // Find an asset with deletion applications
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        java.util.List<AssetDeletionAssetDO> allAssets = assetDeletionAssetMapper.selectList(query);

        if (!allAssets.isEmpty()) {
            Long assetId = allAssets.get(0).getAssetId();
            AssetDeletionAssetQuery assetQuery = new AssetDeletionAssetQuery();
            assetQuery.setAssetId(assetId);
            java.util.List<AssetDeletionAssetDO> list = assetDeletionAssetMapper.selectList(assetQuery);
            assertNotNull(list);
            for (AssetDeletionAssetDO item : list) {
                assertEquals(assetId, item.getAssetId());
            }
            System.out.println("✓ AssetDeletionAsset selectList (assetId=" + assetId + "): " + list.size() + " records");
        } else {
            System.out.println("⚠ AssetDeletionAsset selectList (assetId): No records found in database");
        }
    }

    @Test
    @Order(4)
    public void testSelectCount() {
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        Long count = assetDeletionAssetMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ AssetDeletionAsset selectCount: " + count + " records");
    }

    @Test
    @Order(5)
    public void testInsertAndDelete() {
        // Test insert
        AssetDeletionAssetDO newRecord = new AssetDeletionAssetDO();
        newRecord.setDeletionApplicationId(999999L);
        newRecord.setAssetId(999999L);
        newRecord.setAssetName("测试素材");
        newRecord.setAssetType("IMAGE");

        int insertResult = assetDeletionAssetMapper.insert(newRecord);
        assertTrue(insertResult > 0);
        assertNotNull(newRecord.getId());
        System.out.println("✓ AssetDeletionAsset insert: 1 record inserted, id=" + newRecord.getId());

        // Test delete by query
        AssetDeletionAssetQuery deleteQuery = new AssetDeletionAssetQuery();
        deleteQuery.setDeletionApplicationId(999999L);
        int deleteResult = assetDeletionAssetMapper.delete(deleteQuery);
        assertTrue(deleteResult > 0);
        System.out.println("✓ AssetDeletionAsset delete: " + deleteResult + " record(s) deleted");
    }

    @Test
    @Order(6)
    public void testFindByDeletionApplicationIdWithAsset() {
        // Test custom query method with asset details
        AssetDeletionAssetQuery query = new AssetDeletionAssetQuery();
        java.util.List<AssetDeletionAssetDO> allRecords = assetDeletionAssetMapper.selectList(query);

        if (!allRecords.isEmpty()) {
            Long applicationId = allRecords.get(0).getDeletionApplicationId();
            java.util.List<AssetDeletionAssetDO> list = assetDeletionAssetMapper.findByDeletionApplicationIdWithAsset(applicationId);
            assertNotNull(list);
            System.out.println("✓ AssetDeletionAsset findByDeletionApplicationIdWithAsset: " + list.size() + " records");
        } else {
            System.out.println("⚠ AssetDeletionAsset findByDeletionApplicationIdWithAsset: No records found in database");
        }
    }
}
