package com.xuanjiao.app.asset;

import com.xuanjiao.app.asset.impl.TagServiceImpl;
import com.xuanjiao.client.asset.TagDTO;
import com.xuanjiao.infrastructure.asset.TagMapper;
import com.xuanjiao.infrastructure.asset.TagQuery;
import com.xuanjiao.infrastructure.dataobject.TagDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TagServiceImpl 单元测试
 * 验证 TagMapper 重构后功能正确
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    private TagDO testTag;

    @BeforeEach
    void setUp() {
        testTag = new TagDO();
        testTag.setId(1L);
        testTag.setName("测试标签");
        testTag.setCategory("测试分类");
        testTag.setCreateTime(LocalDateTime.now());
        testTag.setDeleted(0);
    }

    // ==================== TagMapper Call Site Tests ====================

    @Test
    @Order(1)
    void testCreate_TagInsert() {
        // 测试创建标签
        // This tests: TagMapper.insert at line 29

        when(tagMapper.insert(any(TagDO.class))).thenAnswer(invocation -> {
            TagDO tag = invocation.getArgument(0);
            tag.setId(100L);
            return 1;
        });

        TagDTO result = tagService.create("新标签", "新分类");

        assertNotNull(result);
        assertEquals("新标签", result.getName());
        assertEquals("新分类", result.getCategory());

        verify(tagMapper).insert(argThat(tag ->
                tag != null && "新标签".equals(tag.getName()) && "新分类".equals(tag.getCategory())
        ));
        System.out.println("✓ TagService.create() - TagMapper.insert测试通过");
    }

    @Test
    @Order(2)
    void testList_TagQuery() {
        // 测试查询所有标签
        // This tests: TagMapper.selectList with orderBy at line 36-37

        when(tagMapper.selectList(any(TagQuery.class))).thenReturn(Arrays.asList(testTag));

        List<TagDTO> result = tagService.list();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(tagMapper).selectList(argThat(query ->
                query != null && "category".equals(query.getOrderByField()) && "ASC".equals(query.getOrderByDirection())
        ));
        System.out.println("✓ TagService.list() - TagMapper.selectList测试通过");
    }

    @Test
    @Order(3)
    void testListByCategory_WithTagQuery() {
        // 测试按分类查询标签
        // This tests: TagMapper.selectList with category filter at line 48

        TagDO tag2 = new TagDO();
        tag2.setId(2L);
        tag2.setName("标签2");
        tag2.setCategory("IMAGE");
        tag2.setDeleted(0);

        when(tagMapper.selectList(any(TagQuery.class))).thenReturn(Arrays.asList(testTag, tag2));

        List<TagDTO> result = tagService.listByCategory("IMAGE");

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(tagMapper).selectList(argThat(query ->
                query != null && "IMAGE".equals(query.getCategory()) && "name".equals(query.getOrderByField())
        ));
        System.out.println("✓ TagService.listByCategory() - TagMapper.selectList测试通过");
    }

    @Test
    @Order(4)
    void testDelete_TagDeleteById() {
        // 测试删除标签
        // This tests: TagMapper.deleteById at line 54

        when(tagMapper.deleteById(1L)).thenReturn(1);

        tagService.delete(1L);

        verify(tagMapper).deleteById(1L);
        System.out.println("✓ TagService.delete() - TagMapper.deleteById测试通过");
    }

    @Test
    @Order(5)
    void testListByCategory_EmptyCategory() {
        // 测试空分类查询
        // This tests: TagMapper.selectList without category filter

        when(tagMapper.selectList(any(TagQuery.class))).thenReturn(Arrays.asList(testTag));

        List<TagDTO> result = tagService.listByCategory(null);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(tagMapper).selectList(argThat(query ->
                query != null && query.getCategory() == null && "name".equals(query.getOrderByField())
        ));
        System.out.println("✓ TagService.listByCategory(null) - TagMapper.selectList测试通过");
    }
}
