package com.xuanjiao.domain.notification.repository;

import com.xuanjiao.domain.notification.entity.Notification;

import java.util.List;
import java.util.Map;

/**
 * 通知仓储接口
 */
public interface NotificationRepository {

    /**
     * 分页查询
     */
    List<Notification> selectPage(Notification query, int offset, int limit);

    /**
     * 分页查询知会事项（包含工单信息）
     * 返回Map结构以便灵活扩展字段
     */
    List<Map<String, Object>> selectPageWithWorkOrder(Notification query, int offset, int limit, String keyword);

    /**
     * 查询总数（支持关键词）
     */
    long selectCountWithKeyword(Notification query, String keyword);

    /**
     * 查询总数
     */
    long selectCount(Notification query);

    /**
     * 查询详情
     */
    Notification selectById(Long id);

    /**
     * 查询未读数量
     */
    long countUnread(Long recipientId);

    /**
     * 插入
     */
    int insert(Notification notification);

    /**
     * 批量插入
     */
    int batchInsert(List<Notification> notifications);

    /**
     * 标记已读
     */
    int markAsRead(Long id, Long recipientId);

    /**
     * 批量标记已读
     */
    int batchMarkAsRead(List<Long> ids, Long recipientId);

    /**
     * 标记所有已读
     */
    int markAllAsRead(Long recipientId);

    /**
     * 删除
     */
    int deleteById(Long id, Long recipientId);

    /**
     * 批量删除
     */
    int batchDelete(List<Long> ids, Long recipientId);
}
