package com.xuanjiao.app.notification.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjiao.app.notification.NotificationService;
import com.xuanjiao.client.dto.PageResult;
import com.xuanjiao.client.dto.notification.*;
import com.xuanjiao.domain.notification.entity.Notification;
import com.xuanjiao.domain.notification.repository.NotificationRepository;
import com.xuanjiao.infrastructure.approval.ApprovalInstanceMapper;
import com.xuanjiao.infrastructure.dataobject.ApprovalInstanceDO;
import com.xuanjiao.infrastructure.dataobject.MaterialApplicationDO;
import com.xuanjiao.infrastructure.dataobject.AssetDeletionApplicationDO;
import com.xuanjiao.infrastructure.dataobject.UsageApplyDO;
import com.xuanjiao.infrastructure.deletion.AssetDeletionApplicationMapper;
import com.xuanjiao.infrastructure.material.MaterialApplicationMapper;
import com.xuanjiao.infrastructure.notification.NotificationMapper;
import com.xuanjiao.infrastructure.notification.NotificationRecordDO;
import com.xuanjiao.infrastructure.usage.UsageApplyMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private ApprovalInstanceMapper approvalInstanceMapper;

    @Resource
    private MaterialApplicationMapper materialApplicationMapper;

    @Resource
    private AssetDeletionApplicationMapper assetDeletionApplicationMapper;

    @Resource
    private UsageApplyMapper usageApplyMapper;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public PageResult<Map<String, Object>> getNotificationPageDTO(NotificationPageQry qry) {
        Notification query = new Notification();
        query.setRecipientId(qry.getRecipientId());
        query.setNotificationType(qry.getNotificationType());
        query.setIsRead(qry.getIsRead());
        query.setSourceType(qry.getSourceType());

        int offset = (qry.getPageNum() - 1) * qry.getPageSize();
        List<Notification> list = notificationRepository.selectPage(query, offset, qry.getPageSize());
        long total = notificationRepository.selectCount(query);

        List<Map<String, Object>> records = list.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());

        return PageResult.of(records, total, qry.getPageNum(), qry.getPageSize());
    }

    @Override
    public PageResult<Map<String, Object>> getNotificationPageWithWorkOrder(NotificationPageQry qry) {
        Notification query = new Notification();
        query.setRecipientId(qry.getRecipientId());
        query.setNotificationType(qry.getNotificationType());
        query.setIsRead(qry.getIsRead());
        query.setSourceType(qry.getSourceType());

        int offset = (qry.getPageNum() - 1) * qry.getPageSize();
        List<Map<String, Object>> list = notificationRepository.selectPageWithWorkOrder(query, offset, qry.getPageSize(), qry.getKeyword());
        long total = notificationRepository.selectCountWithKeyword(query, qry.getKeyword());

        return PageResult.of(list, total, qry.getPageNum(), qry.getPageSize());
    }

    @Override
    public NotificationDTO getByIdDTO(Long id) {
        Notification notification = notificationRepository.selectById(id);
        if (notification != null) {
            NotificationDTO dto = convertToDTO(notification);
            // 填充类型文本
            dto.setNotificationTypeText(getNotificationTypeText(notification.getNotificationType()));
            dto.setSourceTypeText(getSourceTypeText(notification.getSourceType()));
            return dto;
        }
        return null;
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createNotification(CreateNotificationCmd cmd, Long senderId, String senderName) {
        Notification notification = new Notification();
        notification.setTitle(cmd.getTitle());
        notification.setContent(cmd.getContent());
        notification.setNotificationType(cmd.getNotificationType());
        notification.setSourceType(cmd.getSourceType());
        notification.setSourceId(cmd.getSourceId());
        notification.setSenderId(senderId);
        notification.setSenderName(senderName);
        notification.setRecipientId(cmd.getRecipientIds().get(0));
        notification.setIsRead(0);

        notificationRepository.insert(notification);
        return notification.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateNotifications(BatchCreateNotificationCmd cmd, Long senderId, String senderName) {
        List<Notification> notifications = cmd.getRecipientIds().stream()
                .map(recipientId -> {
                    Notification notification = new Notification();
                    notification.setTitle(cmd.getTitle());
                    notification.setContent(cmd.getContent());
                    notification.setNotificationType(cmd.getNotificationType());
                    notification.setSourceType(cmd.getSourceType());
                    notification.setSourceId(cmd.getSourceId());
                    notification.setSenderId(senderId);
                    notification.setSenderName(senderName);
                    notification.setRecipientId(recipientId);
                    notification.setIsRead(0);
                    return notification;
                })
                .collect(Collectors.toList());

        notificationRepository.batchInsert(notifications);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(MarkReadCmd cmd, Long userId) {
        notificationRepository.markAsRead(cmd.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchMarkAsRead(BatchMarkReadCmd cmd, Long userId) {
        notificationRepository.batchMarkAsRead(cmd.getIds(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(DeleteNotificationCmd cmd, Long userId) {
        notificationRepository.deleteById(cmd.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteNotifications(BatchDeleteNotificationCmd cmd, Long userId) {
        notificationRepository.batchDelete(cmd.getIds(), userId);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(notification, dto);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Notification notification) {
        NotificationDTO dto = convertToDTO(notification);
        if (dto == null) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> map = objectMapper.convertValue(dto, Map.class);
            // 添加类型文本映射
            map.put("notificationTypeText", getNotificationTypeText(notification.getNotificationType()));
            map.put("sourceTypeText", getSourceTypeText(notification.getSourceType()));
            return map;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 获取通知类型文本
     */
    private String getNotificationTypeText(String type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "WORKFLOW_FLOW":
                return "流程流转";
            case "MENTION":
                return "知会";
            case "SYSTEM":
                return "系统通知";
            default:
                return type;
        }
    }

    /**
     * 获取来源类型文本
     */
    private String getSourceTypeText(String type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case "MATERIAL_ENTRY":
                return "素材录入";
            case "ASSET_USAGE":
                return "素材使用";
            case "ASSET_DELETION":
                return "素材删除";
            default:
                return type;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyUsersAboutInstance(NotifyUsersCmd cmd, Long senderId, String senderName) {
        // 获取审批实例信息
        ApprovalInstanceDO instance = approvalInstanceMapper.selectById(cmd.getInstanceId());
        if (instance == null) {
            throw new RuntimeException("审批实例不存在");
        }

        // 获取业务标题（申请标题）
        String businessTitle = getBusinessTitle(instance.getBusinessType(), instance.getBusinessId());

        // 生成通知标题和内容
        String title = buildNotificationTitle(instance.getBusinessType(), businessTitle);
        String content = buildNotificationContent(instance, cmd.getMessage(), businessTitle);

        // 创建通知
        List<Notification> notifications = cmd.getRecipientIds().stream()
                .map(recipientId -> {
                    Notification notification = new Notification();
                    notification.setTitle(title);
                    notification.setContent(content);
                    notification.setNotificationType("MENTION");
                    notification.setSourceType(instance.getBusinessType());
                    notification.setSourceId(instance.getId());
                    notification.setSenderId(senderId);
                    notification.setSenderName(senderName);
                    notification.setRecipientId(recipientId);
                    notification.setIsRead(0);
                    return notification;
                })
                .collect(Collectors.toList());

        notificationRepository.batchInsert(notifications);
    }

    /**
     * 构建通知内容
     */
    private String buildNotificationContent(ApprovalInstanceDO instance, String message, String businessTitle) {
        StringBuilder content = new StringBuilder();
        content.append("您有一条工单被知会：\n");
        content.append("申请标题：").append(businessTitle != null ? businessTitle : "无").append("\n");
        content.append("类型：").append(getSourceTypeText(instance.getBusinessType())).append("\n");
        content.append("状态：").append(getStatusText(instance.getStatus())).append("\n");
        if (message != null && !message.trim().isEmpty()) {
            content.append("附加消息：").append(message).append("\n");
        }
        content.append("请及时查看。");
        return content.toString();
    }

    /**
     * 构建通知标题（包含申请标题）
     */
    private String buildNotificationTitle(String businessType, String businessTitle) {
        StringBuilder title = new StringBuilder();
        title.append(getSourceTypeText(businessType));
        if (businessTitle != null && !businessTitle.isEmpty()) {
            title.append("：").append(businessTitle);
        }
        return title.toString();
    }

    /**
     * 获取业务标题（申请标题）
     */
    private String getBusinessTitle(String businessType, Long businessId) {
        if (businessType == null || businessId == null) {
            return null;
        }
        try {
            switch (businessType) {
                case "MATERIAL_ENTRY":
                    MaterialApplicationDO materialApp = materialApplicationMapper.selectById(businessId);
                    return materialApp != null ? materialApp.getTitle() : null;
                case "ASSET_DELETION":
                    AssetDeletionApplicationDO deletionApp = assetDeletionApplicationMapper.selectById(businessId);
                    return deletionApp != null ? deletionApp.getTitle() : null;
                case "ASSET_USAGE":
                    UsageApplyDO usageApp = usageApplyMapper.selectById(businessId);
                    return usageApp != null ? usageApp.getTitle() : null;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "审批中";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已驳回";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }

    @Override
    public List<NotificationRecordDO> getNotificationRecordsByInstanceId(GetNotificationRecordsQry qry) {
        return notificationMapper.selectNotificationRecordsByInstanceId(qry.getInstanceId());
    }
}
