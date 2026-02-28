package com.xuanjiao.integration;

import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.asset.TagQuery;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TagMapper 集成测试
 * 验证 TagMapper 重构后与数据库交互正确
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TagMapperIntegrationTest {

    @Autowired
    private TagMapper tagMapper;

    @Test
    @Order(1)
    void testSelectById() {
        TagDO result = tagMapper.selectById(1L);
        if (result != null) {
            assertNotNull(result.getId());
            System.out.println("✓ Tag selectById: name=" + result.getName());
        } else {
            System.out.println("⚠ Tag selectById: No records found in database");
        }
    }

    @Test
    @Order(2)
    void testSelectList_EmptyQuery() {
        TagQuery query = new TagQuery();
        List<TagDO> list = tagMapper.selectList(query);
        assertNotNull(list);
        System.out.println("✓ Tag selectList (empty): " + list.size() + " records");
    }

    @Test
    @Order(3)
    void testSelectList_WithCategory() {
        TagQuery query = new TagQuery();
        List<TagDO> allTags = tagMapper.selectList(query);

        if (!allTags.isEmpty()) {
            String category = allTags.get(0).getCategory();
            if (category != null) {
                TagQuery categoryQuery = new TagQuery();
                categoryQuery.setCategory(category);
                List<TagDO> list = tagMapper.selectList(categoryQuery);
                assertNotNull(list);
                for (TagDO item : list) {
                    assertEquals(category, item.getCategory());
                }
                System.out.println("✓ Tag selectList (category=" + category + "): " + list.size() + " records");
            } else {
                System.out.println("⚠ Tag selectList (category): No category found in database");
            }
        } else {
            System.out.println("⚠ Tag selectList (category): No records found in database");
        }
    }

    @Test
    @Order(4)
    void testSelectList_WithOrderBy() {
        TagQuery query = new TagQuery();
        query.setOrderByField("category");
        query.setOrderByDirection("ASC");
        List<TagDO> list = tagMapper.selectList(query);
        assertNotNull(list);
        // Verify ordering
        for (int i = 1; i < list.size(); i++) {
            String prevCategory = list.get(i - 1).getCategory();
            String currCategory = list.get(i).getCategory();
            if (prevCategory != null && currCategory != null) {
                assertTrue(prevCategory.compareTo(currCategory) <= 0);
            }
        }
        System.out.println("✓ Tag selectList (orderBy category ASC): " + list.size() + " records");
    }

    @Test
    @Order(5)
    void testSelectBatchIds() {
        TagQuery query = new TagQuery();
        List<TagDO> allTags = tagMapper.selectList(query);

        if (!allTags.isEmpty()) {
            List<Long> ids = Arrays.asList(allTags.get(0).getId());
            List<TagDO> list = tagMapper.selectBatchIds(ids);
            assertNotNull(list);
            assertTrue(list.size() <= 1);
            System.out.println("✓ Tag selectBatchIds: " + list.size() + " records");
        } else {
            System.out.println("⚠ Tag selectBatchIds: No records found in database");
        }
    }

    @Test
    @Order(6)
    void testSelectCount() {
        TagQuery query = new TagQuery();
        Long count = tagMapper.selectCount(query);
        assertNotNull(count);
        assertTrue(count >= 0);
        System.out.println("✓ Tag selectCount: " + count + " records");
    }

    @Test
    @Order(7)
    void testInsertAndDelete() {
        // Test insert
        TagDO newTag = new TagDO();
        newTag.setName("测试标签" + System.currentTimeMillis());
        newTag.setCategory("测试分类");
        newTag.setCreateTime(java.time.LocalDateTime.now());

        int insertResult = tagMapper.insert(newTag);
        assertTrue(insertResult > 0);
        assertNotNull(newTag.getId());
        Long newTagId = newTag.getId();
        System.out.println("✓ Tag insert: 1 record inserted, id=" + newTagId);

        // Test delete (soft delete)
        int deleteResult = tagMapper.deleteById(newTagId);
        assertTrue(deleteResult > 0);
        System.out.println("✓ Tag delete: " + deleteResult + " record(s) deleted");
    }
}
