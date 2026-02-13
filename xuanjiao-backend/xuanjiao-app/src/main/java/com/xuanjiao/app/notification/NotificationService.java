package com.xuanjiao.app.notification;

import com.xuanjiao.client.dto.common.PageResult;
import com.xuanjiao.client.dto.notification.BatchCreateNotificationCmd;
import com.xuanjiao.client.dto.notification.BatchDeleteNotificationCmd;
import com.xuanjiao.client.dto.notification.BatchMarkReadCmd;
import com.xuanjiao.client.dto.notification.CreateNotificationCmd;
import com.xuanjiao.client.dto.notification.DeleteNotificationCmd;
import com.xuanjiao.client.dto.notification.GetNotificationRecordsQry;
import com.xuanjiao.client.dto.notification.MarkReadCmd;
import com.xuanjiao.client.dto.notification.dto.NotificationDTO;
import com.xuanjiao.client.dto.notification.NotificationPageQry;
import com.xuanjiao.client.dto.notification.dto.NotificationWithWorkOrderDTO;
import com.xuanjiao.client.dto.notification.NotifyUsersCmd;
import com.xuanjiao.infrastructure.notification.NotificationRecordDO;

import java.util.List;

/**
 * 系统通知服务接口
 *
 * <p>提供通知的查询、创建、已读管理等功能。
 * 支持审批通知、知会消息等多种通知类型。</p>
 *
 * <p>核心功能：</p>
 * <ul>
 *   <li>通知列表查询（分页）</li>
 *   <li>未读通知数量统计</li>
 *   <li>通知已读状态管理</li>
 *   <li>通知删除</li>
 *   <li>知会功能（将工单信息通知指定用户）</li>
 * </ul>
 *
 * <p>通知类型：</p>
 * <ul>
 *   <li>APPROVAL - 审批通知</li>
 *   <li>NOTIFY - 知会消息</li>
 *   <li>SYSTEM - 系统通知</li>
 * </ul>
 *
 * @author xuanjiao
 * @since 1.0.0
 * @see com.xuanjiao.app.notification.impl.NotificationServiceImpl
 */
public interface NotificationService {

    /**
     * 分页查询通知列表
     *
     * <p>返回当前用户的通知列表。支持按类型、已读状态筛选。</p>
     *
     * @param qry 查询参数，包含用户ID、类型筛选、已读状态、分页参数等
     * @return 分页结果，包含通知基本信息
     */
    PageResult<NotificationDTO> getNotificationPageDTO(NotificationPageQry qry);

    /**
     * 分页查询通知列表（包含工单信息）
     *
     * <p>返回当前用户的通知列表，并关联查询工单详情。
     * 用于在通知页面直接展示工单信息。</p>
     *
     * @param qry 查询参数，包含用户ID、类型筛选、已读状态、分页参数等
     * @return 分页结果，包含通知信息和关联的工单信息
     */
    PageResult<NotificationWithWorkOrderDTO> getNotificationPageWithWorkOrder(NotificationPageQry qry);

    /**
     * 根据ID获取通知详情
     *
     * <p>返回指定通知的详细信息。</p>
     *
     * @param id 通知ID
     * @return 通知DTO，不存在返回null
     */
    NotificationDTO getByIdDTO(Long id);

    /**
     * 获取未读通知数量
     *
     * <p>返回当前用户的未读通知总数。用于前端徽章显示。</p>
     *
     * @param userId 用户ID
     * @return 未读通知数量
     */
    long getUnreadCount(Long userId);

    /**
     * 创建单条通知
     *
     * <p>创建单条通知记录并发送给指定用户。
     * 通常由系统内部调用，如审批流程完成时。</p>
     *
     * @param cmd 通知参数，包含接收人ID、标题、内容、类型、关联实例ID等
     * @param senderId 发送人ID（可为null，表示系统发送）
     * @param senderName 发送人名称
     * @return 创建的通知ID
     */
    Long createNotification(CreateNotificationCmd cmd, Long senderId, String senderName);

    /**
     * 批量创建通知
     *
     * <p>批量创建通知记录并发送给多个用户。
     * 通常用于知会功能，将同一消息发送给多人。</p>
     *
     * @param cmd 批量通知参数，包含接收人ID列表、标题、内容、类型等
     * @param senderId 发送人ID
     * @param senderName 发送人名称
     */
    void batchCreateNotifications(BatchCreateNotificationCmd cmd, Long senderId, String senderName);

    /**
     * 标记单条通知为已读
     *
     * <p>将指定通知标记为已读状态。</p>
     *
     * @param cmd 参数，包含通知ID
     * @param userId 用户ID
     * @throws RuntimeException 如果通知不存在或不属于当前用户
     */
    void markAsRead(MarkReadCmd cmd, Long userId);

    /**
     * 批量标记通知为已读
     *
     * <p>将多个通知标记为已读状态。</p>
     *
     * @param cmd 参数，包含通知ID列表
     * @param userId 用户ID
     */
    void batchMarkAsRead(BatchMarkReadCmd cmd, Long userId);

    /**
     * 标记所有通知为已读
     *
     * <p>将当前用户的所有未读通知标记为已读。</p>
     *
     * @param userId 用户ID
     */
    void markAllAsRead(Long userId);

    /**
     * 删除单条通知
     *
     * <p>删除指定通知。只能删除自己的通知。</p>
     *
     * @param cmd 参数，包含通知ID
     * @param userId 用户ID
     * @throws RuntimeException 如果通知不存在或不属于当前用户
     */
    void deleteNotification(DeleteNotificationCmd cmd, Long userId);

    /**
     * 批量删除通知
     *
     * <p>批量删除多个通知。只能删除自己的通知。</p>
     *
     * @param cmd 参数，包含通知ID列表
     * @param userId 用户ID
     */
    void batchDeleteNotifications(BatchDeleteNotificationCmd cmd, Long userId);

    /**
     * 获取工单的知会记录
     *
     * <p>查询指定审批实例的所有知会记录。
     * 用于查看工单被知会给了哪些人。</p>
     *
     * @param qry 查询条件，包含审批实例ID
     * @return 知会记录列表
     */
    List<NotificationRecordDO> getNotificationRecordsByInstanceId(GetNotificationRecordsQry qry);

    /**
     * 知会用户关于审批实例
     *
     * <p>将审批实例的信息通知给指定用户列表。
     * 通常在审批过程中，审批人可以将工单信息知会给相关人员。</p>
     *
     * @param cmd 知会命令，包含审批实例ID、接收人ID列表、知会内容等
     * @param senderId 发送人ID（当前审批人）
     * @param senderName 发送人名称
     */
    void notifyUsersAboutInstance(NotifyUsersCmd cmd, Long senderId, String senderName);
}
