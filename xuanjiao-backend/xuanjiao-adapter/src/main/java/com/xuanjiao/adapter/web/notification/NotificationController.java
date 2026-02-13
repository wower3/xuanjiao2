package com.xuanjiao.adapter.web.notification;

import com.xuanjiao.app.notification.NotificationService;
import com.xuanjiao.client.dto.common.Result;
import com.xuanjiao.client.dto.notification.BatchCreateNotificationCmd;
import com.xuanjiao.client.dto.notification.BatchDeleteNotificationCmd;
import com.xuanjiao.client.dto.notification.BatchMarkReadCmd;
import com.xuanjiao.client.dto.notification.CreateNotificationCmd;
import com.xuanjiao.client.dto.notification.DeleteNotificationCmd;
import com.xuanjiao.client.dto.notification.GetNotificationRecordsQry;
import com.xuanjiao.client.dto.notification.MarkReadCmd;
import com.xuanjiao.client.dto.notification.dto.NotificationDTO;
import com.xuanjiao.client.dto.notification.NotificationGetDetailQry;
import com.xuanjiao.client.dto.notification.NotificationPageQry;
import com.xuanjiao.client.dto.notification.NotifyUsersCmd;
import com.xuanjiao.infrastructure.notification.NotificationRecordDO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知管理控制器
 *
 * <p>提供系统通知（知会）的查询和管理功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>知会列表：查询当前用户收到的知会通知</li>
 *   <li>知会详情：查询单个知会的详细信息</li>
 *   <li>标记已读：将知会标记为已读状态</li>
 *   <li>全部已读：将所有知会标记为已读</li>
 *   <li>知会其他人：将工单知会给其他用户</li>
 *   <li>未读数量：查询当前用户的未读知会数量</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Api(tags = "通知管理")
@RestController
@RequestMapping("/notification")
@Validated
public class NotificationController {

    /**
     * 通知服务
     *
     * <p>处理通知的创建、查询、标记已读等业务逻辑。</p>
     */
    private final NotificationService notificationService;

    /**
     * 构造函数
     *
     * <p>通过依赖注入初始化通知服务。</p>
     *
     * @param notificationService 通知服务实例
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 获取我的通知列表
     *
     * <p>分页查询当前用户收到的所有通知，支持按状态筛选。
     * 返回结果不包含关联的工单信息。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件，包含分页参数和状态筛选条件
     * @return 分页的通知列表
     */
    @ApiOperation("获取我的通知列表")
    @PostMapping("/getMyNotifications")
    public Result<?> getMyNotifications(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody NotificationPageQry qry) {
        // 修复：设置recipientId为当前用户，而非null
        qry.setRecipientId(userId);
        return Result.success(notificationService.getNotificationPageDTO(qry));
    }

    /**
     * 获取知会事项列表（包含工单信息）
     *
     * <p>分页查询当前用户收到的知会通知，返回结果包含关联的工单详细信息，
     * 用于知会事项页面的完整展示。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param qry 查询条件，包含分页参数和状态筛选条件
     * @return 分页的通知列表（包含工单信息）
     */
    @ApiOperation("获取知会事项列表（包含工单信息）")
    @PostMapping("/getMyNotificationsWithWorkOrder")
    public Result<?> getMyNotificationsWithWorkOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody NotificationPageQry qry) {
        qry.setRecipientId(userId);
        return Result.success(notificationService.getNotificationPageWithWorkOrder(qry));
    }

    /**
     * 获取通知详情
     *
     * <p>根据通知ID查询详细信息，包括通知内容、发送者、创建时间等。</p>
     *
     * @param qry 查询条件，包含通知ID
     * @return 通知详情信息
     */
    @ApiOperation("获取通知详情")
    @PostMapping("/getDetail")
    public Result<NotificationDTO> getDetail(@Valid @RequestBody NotificationGetDetailQry qry) {
        NotificationDTO notification = notificationService.getByIdDTO(qry.getId());
        if (notification == null) {
            return Result.error("Notification not found");
        }
        return Result.success(notification);
    }

    /**
     * 获取未读通知数量
     *
     * <p>查询当前用户的未读通知总数，用于在导航栏显示未读消息角标。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 包含未读数量的Map
     */
    @ApiOperation("获取未读通知数量")
    @PostMapping("/getUnreadCount")
    public Result<Map<String, Long>> getUnreadCount(@RequestAttribute("userId") Long userId) {
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> resultMap = new HashMap<>();
        resultMap.put("count", count);
        return Result.success(resultMap);
    }

    /**
     * 创建通知
     *
     * <p>创建单个通知，发送给指定接收者。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param username 当前登录用户名，由拦截器注入
     * @param cmd 创建命令，包含通知内容和接收者信息
     * @return 新创建的通知ID
     */
    @ApiOperation("创建通知")
    @PostMapping("/create")
    public Result<Long> create(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("username") String username,
            @Valid @RequestBody CreateNotificationCmd cmd) {
        Long id = notificationService.createNotification(cmd, userId, username);
        return Result.success(id);
    }

    /**
     * 批量创建通知
     *
     * <p>批量创建多个通知，发送给多个接收者。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param username 当前登录用户名，由拦截器注入
     * @param cmd 批量创建命令，包含通知内容和接收者ID列表
     * @return 操作结果
     */
    @ApiOperation("批量创建通知")
    @PostMapping("/batchCreate")
    public Result<Void> batchCreate(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("username") String username,
            @Valid @RequestBody BatchCreateNotificationCmd cmd) {
        notificationService.batchCreateNotifications(cmd, userId, username);
        return Result.success();
    }

    /**
     * 标记通知为已读
     *
     * <p>将指定的通知标记为已读状态。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param cmd 标记命令，包含通知ID
     * @return 操作结果
     */
    @ApiOperation("标记通知为已读")
    @PostMapping("/markAsRead")
    public Result<Void> markAsRead(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody MarkReadCmd cmd) {
        notificationService.markAsRead(cmd, userId);
        return Result.success();
    }

    /**
     * 批量标记通知为已读
     *
     * <p>批量将多个通知标记为已读状态。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param cmd 批量标记命令，包含通知ID列表
     * @return 操作结果
     */
    @ApiOperation("批量标记通知为已读")
    @PostMapping("/batchMarkAsRead")
    public Result<Void> batchMarkAsRead(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BatchMarkReadCmd cmd) {
        notificationService.batchMarkAsRead(cmd, userId);
        return Result.success();
    }

    /**
     * 标记所有通知为已读
     *
     * <p>将当前用户的所有未读通知一次性标记为已读状态。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @return 操作结果
     */
    @ApiOperation("标记所有通知为已读")
    @PostMapping("/markAllAsRead")
    public Result<Void> markAllAsRead(@RequestAttribute("userId") Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 删除通知
     *
     * <p>删除指定的通知。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param cmd 删除命令，包含通知ID
     * @return 操作结果
     */
    @ApiOperation("删除通知")
    @PostMapping("/delete")
    public Result<Void> delete(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody DeleteNotificationCmd cmd) {
        notificationService.deleteNotification(cmd, userId);
        return Result.success();
    }

    /**
     * 批量删除通知
     *
     * <p>批量删除多个通知。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param cmd 批量删除命令，包含通知ID列表
     * @return 操作结果
     */
    @ApiOperation("批量删除通知")
    @PostMapping("/batchDelete")
    public Result<Void> batchDelete(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody BatchDeleteNotificationCmd cmd) {
        notificationService.batchDeleteNotifications(cmd, userId);
        return Result.success();
    }

    /**
     * 知会用户关于审批实例
     *
     * <p>将指定的审批实例知会给其他用户，被知会的用户可以查看该工单的详细信息。
     * 常用于需要让其他人员了解工单进展的场景。</p>
     *
     * @param userId 当前登录用户ID，由拦截器注入
     * @param username 当前登录用户名，由拦截器注入
     * @param cmd 知会命令，包含审批实例ID和被知会用户ID列表
     * @return 操作结果
     */
    @ApiOperation("知会用户关于审批实例")
    @PostMapping("/notifyUsers")
    public Result<Void> notifyUsers(
            @RequestAttribute("userId") Long userId,
            @RequestAttribute("username") String username,
            @Valid @RequestBody NotifyUsersCmd cmd) {
        notificationService.notifyUsersAboutInstance(cmd, userId, username);
        return Result.success();
    }

    /**
     * 获取工单的知会记录
     *
     * <p>查询指定审批实例的所有知会记录，包括知会人、被知会人、知会时间等信息。</p>
     *
     * @param qry 查询条件，包含审批实例ID
     * @return 知会记录列表
     */
    @ApiOperation("获取工单的知会记录")
    @PostMapping("/getNotificationRecords")
    public Result<List<NotificationRecordDO>> getNotificationRecords(
            @Valid @RequestBody GetNotificationRecordsQry qry) {
        List<NotificationRecordDO> records = notificationService.getNotificationRecordsByInstanceId(qry);
        return Result.success(records);
    }
}
