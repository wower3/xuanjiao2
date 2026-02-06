package com.xuanjiao.app.notification;

import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.notification.*;
import com.xuanjiao.infrastructure.notification.NotificationRecordDO;

import java.util.List;
import java.util.Map;

/**
 * 系统通知服务接口
 * <p>提供通知的查询、创建、已读管理等功能</p>
 * <p>支持审批通知、知会消息等</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.notification.impl.NotificationServiceImpl
 */
public interface NotificationService {

    /**
     * 分页查询通知列表
     *
     * @param qry 查询参数
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getNotificationPageDTO(NotificationPageQry qry);

    /**
     * 分页查询通知列表（包含工单信息）
     *
     * @param qry 查询参数
     * @return 分页结果
     */
    PageResult<Map<String, Object>> getNotificationPageWithWorkOrder(NotificationPageQry qry);

    /**
     * 根据ID获取通知详情
     *
     * @param id 通知ID
     * @return 通知DTO
     */
    NotificationDTO getByIdDTO(Long id);

    /**
     * 获取未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    long getUnreadCount(Long userId);

    /**
     * 创建单条通知
     *
     * @param cmd 通知参数
     * @param senderId 发送人ID
     * @param senderName 发送人名称
     * @return 创建的通知ID
     */
    Long createNotification(CreateNotificationCmd cmd, Long senderId, String senderName);

    /**
     * 批量创建通知
     *
     * @param cmd 通知参数
     * @param senderId 发送人ID
     * @param senderName 发送人名称
     */
    void batchCreateNotifications(BatchCreateNotificationCmd cmd, Long senderId, String senderName);

    /**
     * 标记单条通知为已读
     *
     * @param cmd 参数
     * @param userId 用户ID
     */
    void markAsRead(MarkReadCmd cmd, Long userId);

    /**
     * 批量标记通知为已读
     *
     * @param cmd 参数
     * @param userId 用户ID
     */
    void batchMarkAsRead(BatchMarkReadCmd cmd, Long userId);

    /**
     * 标记所有通知为已读
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 删除单条通知
     *
     * @param cmd 参数
     * @param userId 用户ID
     */
    void deleteNotification(DeleteNotificationCmd cmd, Long userId);

    /**
     * 批量删除通知
     *
     * @param cmd 参数
     * @param userId 用户ID
     */
    void batchDeleteNotifications(BatchDeleteNotificationCmd cmd, Long userId);

    /**
     * 获取工单的知会记录
     *
     * @param qry 查询条件
     * @return 知会记录列表
     */
    List<NotificationRecordDO> getNotificationRecordsByInstanceId(GetNotificationRecordsQry qry);

    /**
     * 知会用户关于审批实例
     *
     * @param cmd 知会命令
     * @param senderId 发送人ID
     * @param senderName 发送人名称
     */
    void notifyUsersAboutInstance(NotifyUsersCmd cmd, Long senderId, String senderName);
}
