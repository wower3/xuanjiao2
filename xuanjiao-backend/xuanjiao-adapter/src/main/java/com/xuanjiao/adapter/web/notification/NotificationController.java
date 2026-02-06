package com.xuanjiao.adapter.web.notification;

import com.xuanjiao.app.notification.NotificationService;
import com.xuanjiao.client.dto.notification.*;
import com.xuanjiao.client.dto.Result;
import com.xuanjiao.infrastructure.notification.NotificationRecordDO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "通知管理")
@RestController
@RequestMapping("/notification")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ApiOperation("获取我的通知列表")
    @PostMapping("/getMyNotifications")
    public Result<?> getMyNotifications(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody NotificationPageQry qry) {
        // 修复：设置recipientId为当前用户，而非null
        qry.setRecipientId(userId);
        return Result.success(notificationService.getNotificationPageDTO(qry));
    }

    @ApiOperation("获取知会事项列表（包含工单信息）")
    @PostMapping("/getMyNotificationsWithWorkOrder")
    public Result<?> getMyNotificationsWithWorkOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody NotificationPageQry qry) {
        qry.setRecipientId(userId);
        return Result.success(notificationService.getNotificationPageWithWorkOrder(qry));
    }

    @ApiOperation("获取通知详情")
    @PostMapping("/getDetail")
    public Result<NotificationDTO> getDetail(@Valid @RequestBody NotificationGetDetailQry qry) {
        NotificationDTO notification = notificationService.getByIdDTO(qry.getId());
        if (notification == null) {
            return Result.error("Notification not found");
        }
        return Result.success(notification);
    }

    @ApiOperation("获取未读通知数量")
    @PostMapping("/getUnreadCount")
    public Result<Map<String, Long>> getUnreadCount(@RequestAttribute("userId") Long userId) {
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("count", count);
        return Result.success(resultMap);
    }

    @ApiOperation("创建通知")
    @PostMapping("/create")
    public Result<Long> create(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("username") String username,
            @Valid @RequestBody CreateNotificationCmd cmd) {
        Long id = notificationService.createNotification(cmd, userId, username);
        return Result.success(id);
    }

    @ApiOperation("批量创建通知")
    @PostMapping("/batchCreate")
    public Result<Void> batchCreate(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("username") String username,
            @Valid @RequestBody BatchCreateNotificationCmd cmd) {
        notificationService.batchCreateNotifications(cmd, userId, username);
        return Result.success();
    }

    @ApiOperation("标记通知为已读")
    @PostMapping("/markAsRead")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody MarkReadCmd cmd) {
        notificationService.markAsRead(cmd, userId);
        return Result.success();
    }

    @ApiOperation("批量标记通知为已读")
    @PostMapping("/batchMarkAsRead")
    public Result<Void> batchMarkAsRead(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BatchMarkReadCmd cmd) {
        notificationService.batchMarkAsRead(cmd, userId);
        return Result.success();
    }

    @ApiOperation("标记所有通知为已读")
    @PostMapping("/markAllAsRead")
    public Result<Void> markAllAsRead(@RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @ApiOperation("删除通知")
    @PostMapping("/delete")
    public Result<Void> delete(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody DeleteNotificationCmd cmd) {
        notificationService.deleteNotification(cmd, userId);
        return Result.success();
    }

    @ApiOperation("批量删除通知")
    @PostMapping("/batchDelete")
    public Result<Void> batchDelete(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BatchDeleteNotificationCmd cmd) {
        notificationService.batchDeleteNotifications(cmd, userId);
        return Result.success();
    }

    @ApiOperation("知会用户关于审批实例")
    @PostMapping("/notifyUsers")
    public Result<Void> notifyUsers(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("username") String username,
            @Valid @RequestBody NotifyUsersCmd cmd) {
        notificationService.notifyUsersAboutInstance(cmd, userId, username);
        return Result.success();
    }

    @ApiOperation("获取工单的知会记录")
    @PostMapping("/getNotificationRecords")
    public Result<List<NotificationRecordDO>> getNotificationRecords(
            @Valid @RequestBody GetNotificationRecordsQry qry) {
        List<NotificationRecordDO> records = notificationService.getNotificationRecordsByInstanceId(qry);
        return Result.success(records);
    }
}
