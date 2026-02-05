package com.xuanjiao.infrastructure.deletion;

import com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssetDeletionApplicationMapper集成测试
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class AssetDeletionApplicationMapperIntegrationTest {

    private AssetDeletionApplicationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AssetDeletionApplicationMapperImpl();
    }

    @Test
    void testInsert() {
        AssetDeletionApplicationDO app = new AssetDeletionApplicationDO();
        app.setTitle("测试删除申请");
        app.setApplicantId(1L);
        app.setDeptId(1L);
        app.setWorkflowId(1L);
        app.setStatus("PENDING");
        app.setDeleteReason("测试删除原因");

        int result = mapper.insert(app);
        assertEquals(1, result);
        assertNotNull(app.getId());
    }

    @Test
    void testSelectById() {
        AssetDeletionApplicationDO app = new AssetDeletionApplicationDO();
        app.setTitle("测试删除申请");
        app.setApplicantId(1L);
        app.setDeptId(1L);
        app.setWorkflowId(1L);
        app.setStatus("PENDING");
        mapper.insert(app);

        AssetDeletionApplicationDO found = mapper.selectById(app.getId());
        assertNotNull(found);
        assertEquals(app.getId(), found.getId());
        assertEquals("测试删除申请", found.getTitle());
    }

    @Test
    void testSelectList() {
        AssetDeletionApplicationDO app1 = new AssetDeletionApplicationDO();
        app1.setTitle("申请1");
        app1.setApplicantId(1L);
        app1.setDeptId(1L);
        app1.setWorkflowId(1L);
        app1.setStatus("PENDING");
        mapper.insert(app1);

        AssetDeletionApplicationDO app2 = new AssetDeletionApplicationDO();
        app2.setTitle("申请2");
        app2.setApplicantId(1L);
        app2.setDeptId(1L);
        app2.setWorkflowId(1L);
        app2.setStatus("PENDING");
        mapper.insert(app2);

        AssetDeletionApplicationQuery query = new AssetDeletionApplicationQuery();
        query.setApplicantId(1L);
        List<AssetDeletionApplicationDO> list = mapper.selectList(query);

        assertTrue(list.size() >= 2);
    }

    @Test
    void testSelectCount() {
        AssetDeletionApplicationDO app = new AssetDeletionApplicationDO();
        app.setTitle("计数测试");
        app.setApplicantId(2L);
        app.setDeptId(1L);
        app.setWorkflowId(1L);
        app.setStatus("PENDING");
        mapper.insert(app);

        AssetDeletionApplicationQuery query = new AssetDeletionApplicationQuery();
        query.setApplicantId(2L);
        long count = mapper.selectCount(query);

        assertTrue(count >= 1);
    }

    @Test
    void testUpdateById() {
        AssetDeletionApplicationDO app = new AssetDeletionApplicationDO();
        app.setTitle("更新测试");
        app.setApplicantId(1L);
        app.setDeptId(1L);
        app.setWorkflowId(1L);
        app.setStatus("PENDING");
        mapper.insert(app);

        app.setTitle("更新后的标题");
        app.setStatus("APPROVED");
        int result = mapper.updateById(app);
        assertEquals(1, result);

        AssetDeletionApplicationDO updated = mapper.selectById(app.getId());
        assertEquals("更新后的标题", updated.getTitle());
        assertEquals("APPROVED", updated.getStatus());
    }

    @Test
    void testDeleteById() {
        AssetDeletionApplicationDO app = new AssetDeletionApplicationDO();
        app.setTitle("删除测试");
        app.setApplicantId(1L);
        app.setDeptId(1L);
        app.setWorkflowId(1L);
        app.setStatus("PENDING");
        mapper.insert(app);

        int result = mapper.deleteById(app.getId());
        assertEquals(1, result);

        AssetDeletionApplicationDO deleted = mapper.selectById(app.getId());
        assertNull(deleted);
    }

    @Test
    void testSelectListWithStatusIn() {
        AssetDeletionApplicationDO app1 = new AssetDeletionApplicationDO();
        app1.setTitle("状态测试1");
        app1.setApplicantId(3L);
        app1.setDeptId(1L);
        app1.setWorkflowId(1L);
        app1.setStatus("PENDING");
        mapper.insert(app1);

        AssetDeletionApplicationDO app2 = new AssetDeletionApplicationDO();
        app2.setTitle("状态测试2");
        app2.setApplicantId(3L);
        app2.setDeptId(1L);
        app2.setWorkflowId(1L);
        app2.setStatus("APPROVED");
        mapper.insert(app2);

        AssetDeletionApplicationQuery query = new AssetDeletionApplicationQuery();
        query.setApplicantId(3L);
        query.setStatusIn(java.util.Arrays.asList("PENDING", "APPROVED"));
        List<AssetDeletionApplicationDO> list = mapper.selectList(query);

        assertTrue(list.size() >= 2);
    }
}
