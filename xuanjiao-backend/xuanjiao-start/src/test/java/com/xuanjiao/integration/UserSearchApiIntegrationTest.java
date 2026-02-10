package com.xuanjiao.integration;

import com.xuanjiao.app.user.UserService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.user.UserGetListWithFilterQry;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * User Search API集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserSearchApiIntegrationTest {

    @Autowired
    private UserService userService;

    private static final Long TEST_USER_ID = 1L;

    @Test
    @Order(1)
    public void testSearchUsersBasic() {
        UserGetListWithFilterQry qry = new UserGetListWithFilterQry();
        qry.setPageNum(1);
        qry.setPageSize(10);

        PageResult<Map<String, Object>> result = userService.searchUsers(TEST_USER_ID, qry);
        assertNotNull(result);
        assertNotNull(result.getList());
        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @Order(2)
    public void testSearchUsersWithKeyword() {
        UserGetListWithFilterQry qry = new UserGetListWithFilterQry();
        qry.setPageNum(1);
        qry.setPageSize(10);
        qry.setKeyword("admin");

        PageResult<Map<String, Object>> result = userService.searchUsers(TEST_USER_ID, qry);
        assertNotNull(result);
        assertNotNull(result.getList());
    }

    @Test
    @Order(3)
    public void testSearchUsersWithRoleFilter() {
        UserGetListWithFilterQry qry = new UserGetListWithFilterQry();
        qry.setPageNum(1);
        qry.setPageSize(10);
        qry.setRoleIds(Arrays.asList(1L)); // 系统管理员角色

        PageResult<Map<String, Object>> result = userService.searchUsers(TEST_USER_ID, qry);
        assertNotNull(result);
        assertNotNull(result.getList());

        // 验证返回的用户都有正确的角色
        for (Map<String, Object> user : result.getList()) {
            assertEquals(1L, user.get("roleId"));
        }
    }

    @Test
    @Order(4)
    public void testSearchUsersWithDeptFilter() {
        UserGetListWithFilterQry qry = new UserGetListWithFilterQry();
        qry.setPageNum(1);
        qry.setPageSize(10);
        qry.setDeptId(1L); // 总部
        qry.setIncludeSubDept(false);

        PageResult<Map<String, Object>> result = userService.searchUsers(TEST_USER_ID, qry);
        assertNotNull(result);
        assertNotNull(result.getList());

        // 验证返回的用户都有正确的部门
        for (Map<String, Object> user : result.getList()) {
            assertEquals(1L, user.get("deptId"));
        }
    }

    @Test
    @Order(5)
    public void testSearchUsersWithCombinedFilters() {
        UserGetListWithFilterQry qry = new UserGetListWithFilterQry();
        qry.setPageNum(1);
        qry.setPageSize(10);
        qry.setKeyword("admin");
        qry.setRoleIds(Arrays.asList(1L));
        qry.setDeptId(1L);
        qry.setIncludeSubDept(true);

        PageResult<Map<String, Object>> result = userService.searchUsers(TEST_USER_ID, qry);
        assertNotNull(result);
        assertNotNull(result.getList());
    }

    @Test
    @Order(6)
    public void testSearchUsersPagination() {
        UserGetListWithFilterQry qry = new UserGetListWithFilterQry();
        qry.setPageNum(1);
        qry.setPageSize(5);

        PageResult<Map<String, Object>> page1 = userService.searchUsers(TEST_USER_ID, qry);
        assertNotNull(page1);
        assertNotNull(page1.getList());

        // 如果总数超过5，测试第二页
        if (page1.getTotal() > 5) {
            qry.setPageNum(2);
            PageResult<Map<String, Object>> page2 = userService.searchUsers(TEST_USER_ID, qry);
            assertNotNull(page2);
            assertNotNull(page2.getList());
        }
    }

    @Test
    @Order(7)
    public void testSearchUsersResultFields() {
        // 验证返回字段包含角色和部门信息
        UserGetListWithFilterQry qry = new UserGetListWithFilterQry();
        qry.setPageNum(1);
        qry.setPageSize(10);

        PageResult<Map<String, Object>> result = userService.searchUsers(TEST_USER_ID, qry);
        assertNotNull(result);

        if (result.getList().size() > 0) {
            Map<String, Object> firstUser = result.getList().get(0);
            // 检查基本字段
            assertTrue(firstUser.containsKey("id"));
            assertTrue(firstUser.containsKey("username"));
            assertTrue(firstUser.containsKey("realName"));
            // 检查角色和部门字段
            assertTrue(firstUser.containsKey("roleId"));
            assertTrue(firstUser.containsKey("deptId"));
            // 检查角色名称和部门名称（可能为null）
            assertTrue(firstUser.containsKey("roleName"));
            assertTrue(firstUser.containsKey("deptName"));
        }
    }

    @Test
    @Order(8)
    public void testSearchUsersEmptyResult() {
        UserGetListWithFilterQry qry = new UserGetListWithFilterQry();
        qry.setPageNum(1);
        qry.setPageSize(10);
        qry.setKeyword("这个用户名应该不存在xyz123");

        PageResult<Map<String, Object>> result = userService.searchUsers(TEST_USER_ID, qry);
        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals(0, result.getList().size());
        assertEquals(0L, result.getTotal());
    }
}
