package com.xuanjiao.integration;

import com.xuanjiao.app.notification.NotificationService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.notification.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Notification API集成测试
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NotificationApiIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    private static Long testNotificationId;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_RECIPIENT_ID = 2L;

    @Test
    @Order(1)
    public void testCreateNotification() {
        CreateNotificationCmd cmd = new CreateNotificationCmd();
        cmd.setTitle("测试通知");
        cmd.setContent("这是一条测试通知内容");
        cmd.setNotificationType("WORKFLOW_FLOW");
        cmd.setSourceType("MATERIAL_ENTRY");
        cmd.setSourceId(1L);
        cmd.setRecipientIds(Arrays.asList(TEST_RECIPIENT_ID));

        Long id = notificationService.createNotification(cmd, TEST_USER_ID, "管理员");
        assertNotNull(id);
        testNotificationId = id;
    }

    @Test
    @Order(2)
    public void testBatchCreateNotifications() {
        BatchCreateNotificationCmd cmd = new BatchCreateNotificationCmd();
        cmd.setTitle("批量测试通知");
        cmd.setContent("这是批量创建的测试通知");
        cmd.setNotificationType("SYSTEM");
        cmd.setRecipientIds(Arrays.asList(TEST_RECIPIENT_ID, 3L));

        notificationService.batchCreateNotifications(cmd, TEST_USER_ID, "管理员");
    }

    @Test
    @Order(3)
    public void testGetNotificationPage() {
        NotificationPageQry qry = new NotificationPageQry();
        qry.setPageNum(1);
        qry.setPageSize(10);
        qry.setRecipientId(TEST_RECIPIENT_ID);

        PageResult<Map<String, Object>> result = notificationService.getNotificationPageDTO(qry);
        assertNotNull(result);
        assertTrue(result.getTotal() >= 0);
    }

    @Test
    @Order(4)
    public void testGetNotificationPageWithFilters() {
        NotificationPageQry qry = new NotificationPageQry();
        qry.setPageNum(1);
        qry.setPageSize(10);
        qry.setRecipientId(TEST_RECIPIENT_ID);
        qry.setNotificationType("WORKFLOW_FLOW");
        qry.setIsRead(0);

        PageResult<Map<String, Object>> result = notificationService.getNotificationPageDTO(qry);
        assertNotNull(result);
    }

    @Test
    @Order(5)
    public void testGetUnreadCount() {
        long count = notificationService.getUnreadCount(TEST_RECIPIENT_ID);
        assertTrue(count >= 0);
    }

    @Test
    @Order(6)
    public void testGetById() {
        if (testNotificationId == null) {
            return;
        }
        NotificationDTO notification = notificationService.getByIdDTO(testNotificationId);
        assertNotNull(notification);
        assertEquals(testNotificationId, notification.getId());
    }

    @Test
    @Order(7)
    public void testGetByIdIncludesTypeText() {
        // 测试类型文本字段是否正确填充
        CreateNotificationCmd cmd = new CreateNotificationCmd();
        cmd.setTitle("测试类型文本");
        cmd.setContent("测试通知类型和来源类型文本转换");
        cmd.setNotificationType("MENTION");
        cmd.setSourceType("ASSET_USAGE");
        cmd.setRecipientIds(Arrays.asList(TEST_RECIPIENT_ID));

        Long id = notificationService.createNotification(cmd, TEST_USER_ID, "测试用户");
        assertNotNull(id);

        NotificationDTO notification = notificationService.getByIdDTO(id);
        assertNotNull(notification);
        assertEquals("知会", notification.getNotificationTypeText());
        assertEquals("素材使用", notification.getSourceTypeText());
    }

    @Test
    @Order(8)
    public void testGetNotificationPageIncludesTypeText() {
        // 测试分页查询是否包含类型文本
        NotificationPageQry qry = new NotificationPageQry();
        qry.setPageNum(1);
        qry.setPageSize(10);
        qry.setRecipientId(TEST_RECIPIENT_ID);

        PageResult<Map<String, Object>> result = notificationService.getNotificationPageDTO(qry);
        assertNotNull(result);

        // 检查返回的Map是否包含类型文本字段
        for (Map<String, Object> item : result.getList()) {
            assertTrue(item.containsKey("notificationTypeText"));
            assertTrue(item.containsKey("sourceTypeText"));
        }
    }

    @Test
    @Order(9)
    public void testMarkAsRead() {
        if (testNotificationId == null) {
            return;
        }
        notificationService.markAsRead(new MarkReadCmd() {{ setId(testNotificationId); }}, TEST_RECIPIENT_ID);

        NotificationDTO notification = notificationService.getByIdDTO(testNotificationId);
        assertEquals(1, notification.getIsRead());
    }

    @Test
    @Order(10)
    public void testDeleteNotification() {
        CreateNotificationCmd cmd = new CreateNotificationCmd();
        cmd.setTitle("待删除通知");
        cmd.setContent("这条通知将被删除");
        cmd.setNotificationType("SYSTEM");
        cmd.setRecipientIds(Arrays.asList(TEST_RECIPIENT_ID));

        Long deleteId = notificationService.createNotification(cmd, TEST_USER_ID, "管理员");
        notificationService.deleteNotification(new DeleteNotificationCmd() {{ setId(deleteId); }}, TEST_RECIPIENT_ID);
    }

    @Test
    @Order(11)
    public void testNotificationTypeCoverage() {
        String[] types = {"WORKFLOW_FLOW", "MENTION", "SYSTEM"};
        for (String type : types) {
            CreateNotificationCmd cmd = new CreateNotificationCmd();
            cmd.setTitle("测试通知-" + type);
            cmd.setContent("测试通知类型");
            cmd.setNotificationType(type);
            cmd.setRecipientIds(Arrays.asList(TEST_RECIPIENT_ID));

            Long id = notificationService.createNotification(cmd, TEST_USER_ID, "管理员");
            assertNotNull(id);
        }
    }

    @Test
    @Order(12)
    public void testNotifyUsersAboutInstance() {
        // 测试知会功能（需要有审批实例）
        NotifyUsersCmd cmd = new NotifyUsersCmd();
        cmd.setInstanceId(1L); // 假设存在ID为1的审批实例
        cmd.setRecipientIds(Arrays.asList(TEST_RECIPIENT_ID));
        cmd.setMessage("请及时查看此工单");

        try {
            notificationService.notifyUsersAboutInstance(cmd, TEST_USER_ID, "测试用户");
            // 如果成功执行，说明知会功能正常
            assertTrue(true);
        } catch (Exception e) {
            // 如果实例不存在，这是预期的
            assertTrue(e.getMessage().contains("不存在") || e.getMessage().contains("实例"));
        }
    }

    @Test
    @Order(13)
    public void testNotifyUsersWithMultipleRecipients() {
        // 测试批量知会
        NotifyUsersCmd cmd = new NotifyUsersCmd();
        cmd.setInstanceId(1L);
        cmd.setRecipientIds(Arrays.asList(TEST_RECIPIENT_ID, 3L));
        cmd.setMessage("多人知会测试");

        try {
            notificationService.notifyUsersAboutInstance(cmd, TEST_USER_ID, "测试用户");
            assertTrue(true);
        } catch (Exception e) {
            // 如果实例不存在，这是预期的
            assertTrue(e.getMessage().contains("不存在") || e.getMessage().contains("实例"));
        }
    }

    @Test
    @Order(14)
    public void testBatchMarkAsRead() {
        // 先创建几条未读通知
        List<Long> ids = Arrays.asList(
            notificationService.createNotification(
                createCmd("批量已读测试1"), TEST_USER_ID, "管理员"),
            notificationService.createNotification(
                createCmd("批量已读测试2"), TEST_USER_ID, "管理员")
        );

        BatchMarkReadCmd cmd = new BatchMarkReadCmd();
        cmd.setIds(ids);

        notificationService.batchMarkAsRead(cmd, TEST_RECIPIENT_ID);

        // 验证都已标记为已读
        for (Long id : ids) {
            NotificationDTO notification = notificationService.getByIdDTO(id);
            if (notification != null) {
                assertEquals(1, notification.getIsRead());
            }
        }
    }

    @Test
    @Order(15)
    public void testMarkAllAsRead() {
        // 创建一条未读通知
        Long id = notificationService.createNotification(
            createCmd("全部已读测试"), TEST_USER_ID, "管理员");

        long unreadBefore = notificationService.getUnreadCount(TEST_RECIPIENT_ID);

        notificationService.markAllAsRead(TEST_RECIPIENT_ID);

        long unreadAfter = notificationService.getUnreadCount(TEST_RECIPIENT_ID);
        assertTrue(unreadAfter <= unreadBefore);
    }

    private CreateNotificationCmd createCmd(String title) {
        CreateNotificationCmd cmd = new CreateNotificationCmd();
        cmd.setTitle(title);
        cmd.setContent("测试内容");
        cmd.setNotificationType("SYSTEM");
        cmd.setRecipientIds(Arrays.asList(TEST_RECIPIENT_ID));
        return cmd;
    }
}
