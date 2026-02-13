package com.xuanjiao.domain.notification.repository;

import com.xuanjiao.domain.notification.entity.Notification;
import com.xuanjiao.domain.notification.entity.NotificationWithWorkOrder;

import java.util.List;

/**
 * 通知仓储接口
 *
 * <p>定义通知的持久化操作，包括通知的查询、插入、标记已读和删除。</p>
 * <p>通知用于向用户发送消息，包括审批通知、知会消息等。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
public interface NotificationRepository {

    /**
     * 分页查询通知列表
     *
     * @param query 查询条件
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @return 通知列表
     */
    List<Notification> selectPage(Notification query, int offset, int limit);

    /**
     * 分页查询知会事项（包含工单信息）
     *
     * @param query 查询条件
     * @param offset 分页偏移量
     * @param limit 分页大小
     * @param keyword 关键词
     * @return 知会事项列表
     */
    List<NotificationWithWorkOrder> selectPageWithWorkOrder(Notification query, int offset, int limit, String keyword);

    /**
     * 查询总数（支持关键词）
     *
     * @param query 查询条件
     * @param keyword 关键词
     * @return 匹配的记录数量
     */
    long selectCountWithKeyword(Notification query, String keyword);

    /**
     * 查询总数
     *
     * @param query 查询条件
     * @return 匹配的记录数量
     */
    long selectCount(Notification query);

    /**
     * 根据ID查询通知详情
     *
     * @param id 通知ID
     * @return 通知实体，如果不存在返回 null
     */
    Notification selectById(Long id);

    /**
     * 查询用户未读通知数量
     *
     * @param recipientId 接收人ID
     * @return 未读通知数量
     */
    long countUnread(Long recipientId);

    /**
     * 插入通知
     *
     * @param notification 通知实体
     * @return 影响的行数
     */
    int insert(Notification notification);

    /**
     * 批量插入通知
     *
     * @param notifications 通知列表
     * @return 影响的行数
     */
    int batchInsert(List<Notification> notifications);

    /**
     * 标记单条通知为已读
     *
     * @param id 通知ID
     * @param recipientId 接收人ID
     * @return 影响的行数
     */
    int markAsRead(Long id, Long recipientId);

    /**
     * 批量标记通知为已读
     *
     * @param ids 通知ID列表
     * @param recipientId 接收人ID
     * @return 影响的行数
     */
    int batchMarkAsRead(List<Long> ids, Long recipientId);

    /**
     * 标记所有通知为已读
     *
     * @param recipientId 接收人ID
     * @return 影响的行数
     */
    int markAllAsRead(Long recipientId);

    /**
     * 删除单条通知
     *
     * @param id 通知ID
     * @param recipientId 接收人ID
     * @return 影响的行数
     */
    int deleteById(Long id, Long recipientId);

    /**
     * 批量删除通知
     *
     * @param ids 通知ID列表
     * @param recipientId 接收人ID
     * @return 影响的行数
     */
    int batchDelete(List<Long> ids, Long recipientId);
}
