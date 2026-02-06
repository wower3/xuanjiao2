package com.xuanjiao.infrastructure.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.domain.notification.entity.Notification;
import com.xuanjiao.domain.notification.repository.NotificationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationMapper notificationMapper;

    public NotificationRepositoryImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public List<Notification> selectPage(Notification query, int offset, int limit) {
        NotificationQuery notificationQuery = new NotificationQuery();
        if (query != null) {
            notificationQuery.setRecipientId(query.getRecipientId());
            notificationQuery.setNotificationType(query.getNotificationType());
            notificationQuery.setIsRead(query.getIsRead());
            notificationQuery.setSourceType(query.getSourceType());
        }
        notificationQuery.setOffset(offset);
        notificationQuery.setLimit(limit);
        notificationQuery.setOrderByField("create_time");
        notificationQuery.setOrderByDirection("DESC");

        List<NotificationDO> list = notificationMapper.selectList(notificationQuery);
        return list.stream().map(this::convertToEntity).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> selectPageWithWorkOrder(Notification query, int offset, int limit, String keyword) {
        NotificationQuery notificationQuery = new NotificationQuery();
        if (query != null) {
            notificationQuery.setRecipientId(query.getRecipientId());
            notificationQuery.setNotificationType(query.getNotificationType());
            notificationQuery.setIsRead(query.getIsRead());
            notificationQuery.setSourceType(query.getSourceType());
        }
        notificationQuery.setKeyword(keyword);
        notificationQuery.setOffset(offset);
        notificationQuery.setLimit(limit);
        notificationQuery.setOrderByField("create_time");
        notificationQuery.setOrderByDirection("DESC");

        List<NotificationWithWorkOrderDO> list = notificationMapper.selectListWithWorkOrder(notificationQuery);
        return list.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    @Override
    public long selectCountWithKeyword(Notification query, String keyword) {
        NotificationQuery notificationQuery = new NotificationQuery();
        if (query != null) {
            notificationQuery.setRecipientId(query.getRecipientId());
            notificationQuery.setNotificationType(query.getNotificationType());
            notificationQuery.setIsRead(query.getIsRead());
            notificationQuery.setSourceType(query.getSourceType());
        }
        notificationQuery.setKeyword(keyword);
        return notificationMapper.selectCount(notificationQuery);
    }

    private Map<String, Object> convertToMap(NotificationWithWorkOrderDO notification) {
        if (notification == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        // 基础字段
        map.put("id", notification.getId());
        map.put("title", notification.getTitle());
        map.put("content", notification.getContent());
        map.put("notificationType", notification.getNotificationType());
        map.put("sourceType", notification.getSourceType());
        map.put("sourceId", notification.getSourceId());
        map.put("senderId", notification.getSenderId());
        map.put("senderName", notification.getSenderName());
        map.put("recipientId", notification.getRecipientId());
        map.put("isRead", notification.getIsRead());
        map.put("readTime", notification.getReadTime());
        map.put("status", notification.getStatus());
        map.put("createTime", notification.getCreateTime());
        map.put("updateTime", notification.getUpdateTime());
        map.put("deleted", notification.getDeleted());
        // 工单相关字段
        map.put("instanceId", notification.getInstanceId());
        map.put("instanceStatus", notification.getInstanceStatus());
        map.put("workflowId", notification.getWorkflowId());
        map.put("workflowName", notification.getWorkflowName());
        map.put("applicantId", notification.getApplicantId());
        map.put("applicantName", notification.getApplicantName());
        map.put("businessTitle", notification.getBusinessTitle());
        // 显示用字段
        map.put("displayWorkOrderId", notification.getDisplayWorkOrderId());
        map.put("displayTitle", notification.getDisplayTitle());
        // 类型文本映射
        map.put("notificationTypeText", getNotificationTypeText(notification.getNotificationType()));
        map.put("sourceTypeText", getSourceTypeText(notification.getSourceType()));
        map.put("statusText", getStatusText(notification.getInstanceStatus()));
        // 工单状态使用 instanceStatus
        map.put("status", notification.getInstanceStatus() != null ? notification.getInstanceStatus() : notification.getStatus());
        return map;
    }

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
    public long selectCount(Notification query) {
        NotificationQuery notificationQuery = new NotificationQuery();
        if (query != null) {
            notificationQuery.setRecipientId(query.getRecipientId());
            notificationQuery.setNotificationType(query.getNotificationType());
            notificationQuery.setIsRead(query.getIsRead());
            notificationQuery.setSourceType(query.getSourceType());
        }
        return notificationMapper.selectCount(notificationQuery);
    }

    @Override
    public Notification selectById(Long id) {
        NotificationDO notificationDO = notificationMapper.selectById(id);
        if (notificationDO != null && notificationDO.getDeleted() == 0) {
            return convertToEntity(notificationDO);
        }
        return null;
    }

    @Override
    public long countUnread(Long recipientId) {
        NotificationQuery query = new NotificationQuery();
        query.setRecipientId(recipientId);
        query.setIsRead(0);
        return notificationMapper.selectCount(query);
    }

    @Override
    public int insert(Notification notification) {
        NotificationDO notificationDO = convertToDO(notification);
        int result = notificationMapper.insert(notificationDO);
        // 设置生成的ID回实体
        if (notificationDO.getId() != null) {
            notification.setId(notificationDO.getId());
        }
        return result;
    }

    @Override
    public int batchInsert(List<Notification> notifications) {
        List<NotificationDO> dos = notifications.stream()
                .map(this::convertToDO)
                .collect(Collectors.toList());
        for (NotificationDO notificationDO : dos) {
            notificationMapper.insert(notificationDO);
        }
        return notifications.size();
    }

    @Override
    public int markAsRead(Long id, Long recipientId) {
        return notificationMapper.markAsRead(id, recipientId);
    }

    @Override
    public int batchMarkAsRead(List<Long> ids, Long recipientId) {
        return notificationMapper.batchMarkAsRead(ids, recipientId);
    }

    @Override
    public int markAllAsRead(Long recipientId) {
        return notificationMapper.markAllAsRead(recipientId);
    }

    @Override
    public int deleteById(Long id, Long recipientId) {
        return notificationMapper.deleteById(id, recipientId);
    }

    @Override
    public int batchDelete(List<Long> ids, Long recipientId) {
        return notificationMapper.batchDelete(ids, recipientId);
    }

    private Notification convertToEntity(NotificationDO notificationDO) {
        if (notificationDO == null) {
            return null;
        }
        Notification notification = new Notification();
        BeanUtils.copyProperties(notificationDO, notification);
        return notification;
    }

    private NotificationDO convertToDO(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationDO notificationDO = new NotificationDO();
        BeanUtils.copyProperties(notification, notificationDO);
        notificationDO.setStatus(1);
        notificationDO.setDeleted(0);
        if (notificationDO.getCreateTime() == null) {
            notificationDO.setCreateTime(LocalDateTime.now());
        }
        notificationDO.setUpdateTime(LocalDateTime.now());
        return notificationDO;
    }
}
