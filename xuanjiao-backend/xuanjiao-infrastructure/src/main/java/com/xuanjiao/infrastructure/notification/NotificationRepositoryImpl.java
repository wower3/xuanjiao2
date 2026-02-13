package com.xuanjiao.infrastructure.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuanjiao.domain.notification.entity.Notification;
import com.xuanjiao.domain.notification.entity.NotificationWithWorkOrder;
import com.xuanjiao.domain.notification.repository.NotificationRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public List<NotificationWithWorkOrder> selectPageWithWorkOrder(Notification query, int offset, int limit, String keyword) {
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
                .map(this::convertToEntityWithWorkOrder)
                .collect(Collectors.toList());
    }

    private NotificationWithWorkOrder convertToEntityWithWorkOrder(NotificationWithWorkOrderDO doWithWorkOrder) {
        if (doWithWorkOrder == null) {
            return null;
        }
        NotificationWithWorkOrder entity = new NotificationWithWorkOrder();
        // 基础字段
        entity.setId(doWithWorkOrder.getId());
        entity.setTitle(doWithWorkOrder.getTitle());
        entity.setContent(doWithWorkOrder.getContent());
        entity.setNotificationType(doWithWorkOrder.getNotificationType());
        entity.setSourceType(doWithWorkOrder.getSourceType());
        entity.setSourceId(doWithWorkOrder.getSourceId());
        entity.setSenderId(doWithWorkOrder.getSenderId());
        entity.setSenderName(doWithWorkOrder.getSenderName());
        entity.setRecipientId(doWithWorkOrder.getRecipientId());
        entity.setIsRead(doWithWorkOrder.getIsRead());
        entity.setReadTime(doWithWorkOrder.getReadTime());
        entity.setStatus(doWithWorkOrder.getStatus());
        entity.setCreateTime(doWithWorkOrder.getCreateTime());
        entity.setUpdateTime(doWithWorkOrder.getUpdateTime());
        entity.setDeleted(doWithWorkOrder.getDeleted());
        // 工单相关字段
        entity.setInstanceId(doWithWorkOrder.getInstanceId());
        entity.setInstanceStatus(doWithWorkOrder.getInstanceStatus());
        entity.setWorkflowId(doWithWorkOrder.getWorkflowId());
        entity.setWorkflowName(doWithWorkOrder.getWorkflowName());
        entity.setApplicantId(doWithWorkOrder.getApplicantId());
        entity.setApplicantName(doWithWorkOrder.getApplicantName());
        entity.setBusinessTitle(doWithWorkOrder.getBusinessTitle());
        return entity;
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
