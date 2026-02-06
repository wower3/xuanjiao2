package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.asset.AssetTagMapper;
import com.xuanjiao.infrastructure.asset.AssetTagQuery;
import com.xuanjiao.infrastructure.dataobject.AssetTagDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssetTagMapper 集成测试
 * 验证 AssetTagMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AssetTagMapperIntegrationTest {

    @Autowired
    private AssetTagMapper assetTagMapper;

    @Test
    @Order(1)
    public void testSelectList_EmptyQuery() {
        AssetTagQuery query = new AssetTagQuery();
        java.util.List<AssetTagDO> list = assetTagMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ AssetTag selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(2)
    public void testSelectList_WithAssetId() {
        // Find an asset with tags
        AssetTagQuery query = new AssetTagQuery();
        java.util.List<AssetTagDO> allAssetTags = assetTagMapper.selectList(query);

        if (!allAssetTags.isEmpty()) {
            Long assetId = allAssetTags.get(0).getAssetId();
            AssetTagQuery assetQuery = new AssetTagQuery();
            assetQuery.setAssetId(assetId);
            java.util.List<AssetTagDO> list = assetTagMapper.selectList(assetQuery);
            assertNotNull(list);
            for (AssetTagDO item : list) {
                assertEquals(assetId, item.getAssetId());
            }
            System.out.println("✓ AssetTag selectList (assetId=" + assetId + "): " + list.size() + " records");
        } else {
            System.out.println("⚠ AssetTag selectList (assetId): No records found in database");
        }
    }

    @Test
    @Order(3)
    public void testSelectList_WithTagId() {
        // Find a tag with assets
        AssetTagQuery query = new AssetTagQuery();
        java.util.List<AssetTagDO> allAssetTags = assetTagMapper.selectList(query);

        if (!allAssetTags.isEmpty()) {
            Long tagId = allAssetTags.get(0).getTagId();
            AssetTagQuery tagQuery = new AssetTagQuery();
            tagQuery.setTagId(tagId);
            java.util.List<AssetTagDO> list = assetTagMapper.selectList(tagQuery);
            assertNotNull(list);
            for (AssetTagDO item : list) {
                assertEquals(tagId, item.getTagId());
            }
            System.out.println("✓ AssetTag selectList (tagId=" + tagId + "): " + list.size() + " records");
        } else {
            System.out.println("⚠ AssetTag selectList (tagId): No records found in database");
        }
    }

    @Test
    @Order(4)
    public void testSelectList_ComplexQuery() {
        // Test query with both assetId and tagId
        AssetTagQuery query = new AssetTagQuery();
        java.util.List<AssetTagDO> allAssetTags = assetTagMapper.selectList(query);

        if (!allAssetTags.isEmpty()) {
            AssetTagDO firstTag = allAssetTags.get(0);
            AssetTagQuery complexQuery = new AssetTagQuery();
            complexQuery.setAssetId(firstTag.getAssetId());
            complexQuery.setTagId(firstTag.getTagId());
            java.util.List<AssetTagDO> list = assetTagMapper.selectList(complexQuery);
            assertNotNull(list);
            for (AssetTagDO item : list) {
                assertEquals(firstTag.getAssetId(), item.getAssetId());
                assertEquals(firstTag.getTagId(), item.getTagId());
            }
            System.out.println("✓ AssetTag selectList (complex): " + list.size() + " records");
        } else {
            System.out.println("⚠ AssetTag selectList (complex): No records found in database");
        }
    }

    @Test
    @Order(5)
    public void testSelectCount() {
        AssetTagQuery query = new AssetTagQuery();
        Long count = assetTagMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ AssetTag selectCount: " + count + " records");
    }

    @Test
    @Order(6)
    public void testSelectCount_WithAssetId() {
        AssetTagQuery query = new AssetTagQuery();
        java.util.List<AssetTagDO> allAssetTags = assetTagMapper.selectList(query);

        if (!allAssetTags.isEmpty()) {
            Long assetId = allAssetTags.get(0).getAssetId();
            AssetTagQuery assetQuery = new AssetTagQuery();
            assetQuery.setAssetId(assetId);
            Long count = assetTagMapper.selectCount(assetQuery);
            assertNotNull(count);
            assertTrue(count >= 0);
            System.out.println("✓ AssetTag selectCount (assetId=" + assetId + "): " + count + " records");
        } else {
            System.out.println("⚠ AssetTag selectCount (assetId): No records found in database");
        }
    }

    @Test
    @Order(7)
    public void testInsertAndDelete() {
        // Test insert
        AssetTagDO newTag = new AssetTagDO();
        newTag.setAssetId(999999L); // Use a non-existent asset ID for testing
        newTag.setTagId(999999L);   // Use a non-existent tag ID for testing

        int insertResult = assetTagMapper.insert(newTag);
        assertTrue(insertResult > 0);
        System.out.println("✓ AssetTag insert: 1 record inserted");

        // Test delete
        AssetTagQuery deleteQuery = new AssetTagQuery();
        deleteQuery.setAssetId(999999L);
        deleteQuery.setTagId(999999L);
        int deleteResult = assetTagMapper.delete(deleteQuery);
        assertTrue(deleteResult > 0);
        System.out.println("✓ AssetTag delete: " + deleteResult + " record(s) deleted");
    }
}
