package com.xuanjiao.app.notification.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuanjiao.app.notification.NotificationService;
import com.xuanjiao.client.PageResult;
import com.xuanjiao.client.notification.BatchCreateNotificationCmd;
import com.xuanjiao.client.notification.BatchDeleteNotificationCmd;
import com.xuanjiao.client.notification.BatchMarkReadCmd;
import com.xuanjiao.client.notification.CreateNotificationCmd;
import com.xuanjiao.client.notification.DeleteNotificationCmd;
import com.xuanjiao.client.notification.GetNotificationRecordsQry;
import com.xuanjiao.client.notification.MarkReadCmd;
import com.xuanjiao.client.notification.NotificationDTO;
import com.xuanjiao.client.notification.NotificationPageQry;
import com.xuanjiao.client.notification.NotifyUsersCmd;
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
import com.xuanjiao.common.ConvertUtils;
import com.xuanjiao.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统通知服务实现类
 * <p>实现NotificationService接口，封装通知业务逻辑</p>
 * <p>核心功能：通知查询、创建、已读管理、知会功能</p>
 *
 * @author system
 * @version 1.0
 * @see com.xuanjiao.app.notification.NotificationService
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    /** 消息常量 */
    private static final String MSG_INSTANCE_NOT_FOUND = "审批实例不存在";

    /** 业务类型常量 */
    private static final String BUSINESS_TYPE_MATERIAL_ENTRY = "MATERIAL_ENTRY";
    private static final String BUSINESS_TYPE_ASSET_USAGE = "ASSET_USAGE";
    private static final String BUSINESS_TYPE_ASSET_DELETION = "ASSET_DELETION";

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
    public PageResult<NotificationDTO> getNotificationPageDTO(NotificationPageQry qry) {
        Notification query = new Notification();
        query.setRecipientId(qry.getRecipientId());
        query.setNotificationType(qry.getNotificationType());
        query.setIsRead(qry.getIsRead());
        query.setSourceType(qry.getSourceType());

        int offset = (qry.getPageNum() - 1) * qry.getPageSize();
        List<Notification> list = notificationRepository.selectPage(query, offset, qry.getPageSize());
        long total = notificationRepository.selectCount(query);

        List<NotificationDTO> records = list.stream()
                .map(this::convertToDTOWithTypeText)
                .collect(Collectors.toList());

        return PageResult.of(records, total, qry.getPageNum(), qry.getPageSize());
    }

    @Override
    public PageResult<NotificationDTO> getNotificationPageWithWorkOrder(NotificationPageQry qry) {
        Notification query = new Notification();
        query.setRecipientId(qry.getRecipientId());
        query.setNotificationType(qry.getNotificationType());
        query.setIsRead(qry.getIsRead());
        query.setSourceType(qry.getSourceType());

        int offset = (qry.getPageNum() - 1) * qry.getPageSize();
        List<Map<String, Object>> mapList = notificationRepository.selectPageWithWorkOrder(query, offset, qry.getPageSize(), qry.getKeyword());
        long total = notificationRepository.selectCountWithKeyword(query, qry.getKeyword());

        // 将 Map 转换为 NotificationDTO
        List<NotificationDTO> list = mapList.stream()
                .map(this::convertMapToDTO)
                .collect(Collectors.toList());

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
        ConvertUtils.copyProperties(notification, dto);
        return dto;
    }

    /**
     * 将 Notification 转换为 NotificationDTO 并填充类型文本
     *
     * <p>用于列表查询，填充 notificationTypeText 和 sourceTypeText 字段。</p>
     *
     * @param notification 通知实体
     * @return 带类型文本的通知DTO
     */
    private NotificationDTO convertToDTOWithTypeText(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationDTO dto = convertToDTO(notification);
        if (dto != null) {
            dto.setNotificationTypeText(getNotificationTypeText(notification.getNotificationType()));
            dto.setSourceTypeText(getSourceTypeText(notification.getSourceType()));
        }
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
     * 将Map转换为NotificationDTO（包含工单信息）
     */
    private NotificationDTO convertMapToDTO(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        NotificationDTO dto = new NotificationDTO();
        // 基础字段
        dto.setId(getLongValue(map, "id"));
        dto.setTitle((String) map.get("title"));
        dto.setContent((String) map.get("content"));
        dto.setNotificationType((String) map.get("notificationType"));
        dto.setSourceType((String) map.get("sourceType"));
        dto.setSourceId(getLongValue(map, "sourceId"));
        dto.setSenderId(getLongValue(map, "senderId"));
        dto.setSenderName((String) map.get("senderName"));
        dto.setRecipientId(getLongValue(map, "recipientId"));
        dto.setIsRead(getIntegerValue(map, "isRead"));
        dto.setReadTime(getLocalDateTimeValue(map, "readTime"));
        dto.setCreateTime(getLocalDateTimeValue(map, "createTime"));
        // 类型文本
        dto.setNotificationTypeText((String) map.get("notificationTypeText"));
        dto.setSourceTypeText((String) map.get("sourceTypeText"));
        // 工单相关字段
        dto.setInstanceId(getLongValue(map, "instanceId"));
        dto.setInstanceStatus((String) map.get("instanceStatus"));
        dto.setWorkflowId(getLongValue(map, "workflowId"));
        dto.setWorkflowName((String) map.get("workflowName"));
        dto.setApplicantId(getLongValue(map, "applicantId"));
        dto.setApplicantName((String) map.get("applicantName"));
        dto.setBusinessTitle((String) map.get("businessTitle"));
        dto.setDisplayWorkOrderId((String) map.get("displayWorkOrderId"));
        dto.setDisplayTitle((String) map.get("displayTitle"));
        dto.setStatusText((String) map.get("statusText"));
        return dto;
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
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
            case BUSINESS_TYPE_MATERIAL_ENTRY:
                return "素材录入";
            case BUSINESS_TYPE_ASSET_USAGE:
                return "素材使用";
            case BUSINESS_TYPE_ASSET_DELETION:
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
            throw new NotFoundException(MSG_INSTANCE_NOT_FOUND);
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
            return fetchBusinessTitleByType(businessType, businessId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据业务类型获取业务标题
     */
    private String fetchBusinessTitleByType(String businessType, Long businessId) {
        switch (businessType) {
            case BUSINESS_TYPE_MATERIAL_ENTRY:
                return getMaterialApplicationTitle(businessId);
            case BUSINESS_TYPE_ASSET_DELETION:
                return getAssetDeletionApplicationTitle(businessId);
            case BUSINESS_TYPE_ASSET_USAGE:
                return getUsageApplyTitle(businessId);
            default:
                return null;
        }
    }

    /**
     * 获取素材录入申请标题
     */
    private String getMaterialApplicationTitle(Long businessId) {
        MaterialApplicationDO materialApp = materialApplicationMapper.selectById(businessId);
        return materialApp != null ? materialApp.getTitle() : null;
    }

    /**
     * 获取素材删除申请标题
     */
    private String getAssetDeletionApplicationTitle(Long businessId) {
        AssetDeletionApplicationDO deletionApp = assetDeletionApplicationMapper.selectById(businessId);
        return deletionApp != null ? deletionApp.getTitle() : null;
    }

    /**
     * 获取素材使用申请标题
     */
    private String getUsageApplyTitle(Long businessId) {
        UsageApplyDO usageApp = usageApplyMapper.selectById(businessId);
        return usageApp != null ? usageApp.getTitle() : null;
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
