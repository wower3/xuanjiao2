package com.xuanjiao.infrastructure.notification;

import com.xuanjiao.infrastructure.notification.NotificationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统通知数据访问接口
 * <p>定义系统通知的数据库操作方法，对应SQL实现</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.domain.notification.entity.Notification
 */
@Mapper
public interface NotificationMapper {

    // ==================== 基础CRUD方法 ====================

    /**
     * 根据ID查询
     */
    NotificationDO selectById(@Param("id") Long id);

    /**
     * 根据查询条件查询列表
     */
    List<NotificationDO> selectList(NotificationQuery query);

    /**
     * 查询知会事项列表（JOIN工单信息）
     */
    List<NotificationWithWorkOrderDO> selectListWithWorkOrder(NotificationQuery query);

    /**
     * 根据查询条件统计数量
     */
    long selectCount(NotificationQuery query);

    /**
     * 插入
     */
    int insert(NotificationDO notificationDO);

    // ==================== 业务方法 ====================

    /**
     * 标记为已读
     */
    int markAsRead(@Param("id") Long id, @Param("recipientId") Long recipientId);

    /**
     * 批量标记为已读
     */
    int batchMarkAsRead(@Param("ids") List<Long> ids, @Param("recipientId") Long recipientId);

    /**
     * 标记所有为已读
     */
    int markAllAsRead(@Param("recipientId") Long recipientId);

    /**
     * 删除（逻辑删除）
     */
    int deleteById(@Param("id") Long id, @Param("recipientId") Long recipientId);

    /**
     * 批量删除（逻辑删除）
     */
    int batchDelete(@Param("ids") List<Long> ids, @Param("recipientId") Long recipientId);

    /**
     * 查询工单的知会记录（包含发起人和收件人部门信息）
     */
    List<NotificationRecordDO> selectNotificationRecordsByInstanceId(@Param("instanceId") Long instanceId);
}
