package com.xuanjiao.app.notification;

import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.notification.*;
import com.xuanjiao.infrastructure.notification.NotificationRecordDO;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    PageResult<Map<String, Object>> getNotificationPageDTO(NotificationPageQry qry);
    PageResult<Map<String, Object>> getNotificationPageWithWorkOrder(NotificationPageQry qry);
    NotificationDTO getByIdDTO(Long id);
    long getUnreadCount(Long userId);
    Long createNotification(CreateNotificationCmd cmd, Long senderId, String senderName);
    void batchCreateNotifications(BatchCreateNotificationCmd cmd, Long senderId, String senderName);
    void markAsRead(MarkReadCmd cmd, Long userId);
    void batchMarkAsRead(BatchMarkReadCmd cmd, Long userId);
    void markAllAsRead(Long userId);
    void deleteNotification(DeleteNotificationCmd cmd, Long userId);
    void batchDeleteNotifications(BatchDeleteNotificationCmd cmd, Long userId);

    /**
     * 获取工单的知会记录
     * @param qry 查询条件
     * @return 知会记录列表
     */
    List<NotificationRecordDO> getNotificationRecordsByInstanceId(GetNotificationRecordsQry qry);

    /**
     * 知会用户关于审批实例
     * @param cmd 知会命令
     * @param senderId 发送人ID
     * @param senderName 发送人名称
     */
    void notifyUsersAboutInstance(NotifyUsersCmd cmd, Long senderId, String senderName);
}
