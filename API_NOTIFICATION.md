# API_NOTIFICATION.md - 通知管理接口文档

> **模块**: 通知管理 (notification)
> **Controller**: `NotificationController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/notification/`
> **创建时间**: 2026-03-04

---

## 目录

1. [获取我的通知列表](#1-获取我的通知列表)
2. [获取知会事项列表（包含工单信息）](#2-获取知会事项列表包含工单信息)
3. [获取通知详情](#3-获取通知详情)
4. [获取未读通知数量](#4-获取未读通知数量)
5. [创建通知](#5-创建通知)
6. [批量创建通知](#6-批量创建通知)
7. [标记通知为已读](#7-标记通知为已读)
8. [批量标记通知为已读](#8-批量标记通知为已读)
9. [标记所有通知为已读](#9-标记所有通知为已读)
10. [删除通知](#10-删除通知)
11. [批量删除通知](#11-批量删除通知)
12. [知会用户关于审批实例](#12-知会用户关于审批实例)
13. [获取工单的知会记录](#13-获取工单的知会记录)

---

## 1. 获取我的通知列表

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取我的通知列表 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/getMyNotifications` |
| 接口描述 | 分页查询当前用户收到的所有通知，支持按类型筛选 |

### 请求参数

**NotificationPageQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "notificationType": "APPROVAL",
  "isRead": 0,
  "sourceType": "WORKFLOW"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 否 | 页码（默认1） |
| pageSize | Integer | 否 | 每页数量（默认10） |
| notificationType | String | 否 | 通知类型筛选 |
| isRead | Integer | 否 | 已读状态（0=未读，1=已读） |
| sourceType | String | 否 | 来源类型筛选 |

### 通知类型说明

| notificationType | 说明 |
|------------------|------|
| SYSTEM | 系统通知 |
| APPROVAL | 审批通知 |
| NOTIFY | 知会消息 |

### 来源类型说明

| sourceType | 说明 |
|------------|------|
| WORKFLOW | 工作流 |
| ASSET | 素材 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 100,
        "title": "您有一个待审批任务",
        "content": "素材录入申请需要您审批",
        "notificationType": "APPROVAL",
        "notificationTypeText": "审批通知",
        "sourceType": "WORKFLOW",
        "sourceTypeText": "工作流",
        "sourceId": 500,
        "senderId": 10,
        "senderName": "张三",
        "recipientId": 20,
        "isRead": 0,
        "readTime": null,
        "createTime": "2024-03-01 10:00:00"
      }
    ],
    "total": 25,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.getMyNotifications()` | 接收HTTP请求，注入userId |
| App | `NotificationService.getNotificationPageDTO()` | 业务逻辑 |
| Domain | `NotificationRepository.selectPage()` | 分页查询 |
| Domain | `NotificationRepository.selectCount()` | 统计总数 |
| Infrastructure | `NotificationMapper.selectPage()` | MyBatis查询 |
| Database | `notification` 表 | MySQL |

---

## 2. 获取知会事项列表（包含工单信息）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取知会事项列表（包含工单信息） |
| 请求方法 | POST |
| 请求路径 | `/api/notification/getMyNotificationsWithWorkOrder` |
| 接口描述 | 分页查询当前用户收到的知会通知，返回结果包含关联的工单详细信息 |

### 请求参数

**NotificationPageQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "notificationType": "NOTIFY",
  "isRead": 0,
  "keyword": "素材"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 否 | 页码（默认1） |
| pageSize | Integer | 否 | 每页数量（默认10） |
| notificationType | String | 否 | 通知类型筛选 |
| isRead | Integer | 否 | 已读状态（0=未读，1=已读） |
| keyword | String | 否 | 搜索关键词（匹配标题或内容） |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 100,
        "title": "素材录入申请",
        "content": "请您查看该工单",
        "notificationType": "NOTIFY",
        "senderId": 10,
        "senderName": "张三",
        "recipientId": 20,
        "isRead": 0,
        "createTime": "2024-03-01 10:00:00",
        "instanceId": 500,
        "instanceStatus": "PENDING",
        "statusText": "审批中",
        "workflowId": 1,
        "workflowName": "素材录入审批流程",
        "applicantId": 10,
        "applicantName": "张三",
        "businessTitle": "2024年3月宣传素材",
        "displayWorkOrderId": "INST-500",
        "displayTitle": "素材录入申请 - 2024年3月宣传素材"
      }
    ],
    "total": 15,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.getMyNotificationsWithWorkOrder()` | 接收HTTP请求 |
| App | `NotificationService.getNotificationPageWithWorkOrder()` | 业务逻辑 |
| App | `convertMapToDTO()` | Map转DTO并填充工单信息 |
| Infrastructure | `NotificationMapper.selectPageWithWorkOrder()` | JOIN查询notification+approval_instance |
| Infrastructure | `NotificationMapper.selectCountWithKeyword()` | 统计数量（含关键词） |
| Database | `notification`, `approval_instance`, `workflow`, `sys_user` 表 | MySQL |

### JOIN查询说明

```
notification LEFT JOIN approval_instance ON notification.source_id = approval_instance.id
                       LEFT JOIN workflow ON approval_instance.workflow_id = workflow.id
                       LEFT JOIN sys_user ON approval_instance.applicant_id = sys_user.id
```

---

## 3. 获取通知详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取通知详情 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/getDetail` |
| 接口描述 | 根据通知ID查询详细信息 |

### 请求参数

**NotificationGetDetailQry**:
```json
{
  "id": 100
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 通知ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "title": "您有一个待审批任务",
    "content": "素材录入申请需要您审批",
    "notificationType": "APPROVAL",
    "notificationTypeText": "审批通知",
    "sourceType": "WORKFLOW",
    "sourceTypeText": "工作流",
    "sourceId": 500,
    "senderId": 10,
    "senderName": "张三",
    "recipientId": 20,
    "isRead": 0,
    "readTime": null,
    "createTime": "2024-03-01 10:00:00"
  }
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "Notification not found"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.getDetail()` | 接收请求 |
| App | `NotificationService.getByIdDTO()` | 业务逻辑 |
| App | `convertToDTO()` | 实体转DTO |
| Domain | `NotificationRepository.selectById()` | 查询通知 |

---

## 4. 获取未读通知数量

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取未读通知数量 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/getUnreadCount` |
| 接口描述 | 查询当前用户的未读通知总数，用于导航栏徽章显示 |

### 请求参数

无参数（userId由@RequestAttribute注入）

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "count": 5
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.getUnreadCount()` | 接收请求，注入userId |
| App | `NotificationService.getUnreadCount()` | 业务逻辑 |
| Domain | `NotificationRepository.countUnread()` | 统计未读数量 |
| Infrastructure | `NotificationMapper.countUnread()` | COUNT查询 |
| Database | `notification` 表 | MySQL |

### 查询条件

| 条件 | 值 |
|-----|-----|
| recipient_id | 当前用户ID |
| is_read | 0 |

---

## 5. 创建通知

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 创建通知 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/create` |
| 接口描述 | 创建单个通知，发送给指定接收者 |

### 请求参数

**CreateNotificationCmd**:
```json
{
  "title": "您有一个新的待审批任务",
  "content": "素材录入申请需要您审批",
  "notificationType": "APPROVAL",
  "sourceType": "WORKFLOW",
  "sourceId": 500,
  "recipientIds": [20]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| title | String | 是 | 通知标题 |
| content | String | 否 | 通知内容 |
| notificationType | String | 是 | 通知类型 |
| sourceType | String | 否 | 来源类型 |
| sourceId | Long | 否 | 来源ID |
| recipientIds | List\<Long\> | 是 | 接收人ID列表 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": 100  // 新创建的通知ID
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.create()` | 接收请求，注入发送人信息 |
| App | `NotificationService.createNotification()` | 业务逻辑 |
| Domain | `NotificationRepository.save()` | 保存通知 |
| Infrastructure | `NotificationMapper.insert()` | 插入数据库 |

---

## 6. 批量创建通知

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 批量创建通知 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/batchCreate` |
| 接口描述 | 批量创建多个通知，发送给多个接收者 |

### 请求参数

**BatchCreateNotificationCmd**:
```json
{
  "title": "工单已通过审批",
  "content": "您的素材录入申请已通过审批",
  "notificationType": "SYSTEM",
  "sourceType": "WORKFLOW",
  "sourceId": 500,
  "recipientIds": [10, 20, 30]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| title | String | 是 | 通知标题 |
| content | String | 否 | 通知内容 |
| notificationType | String | 是 | 通知类型 |
| sourceType | String | 否 | 来源类型 |
| sourceId | Long | 否 | 来源ID |
| recipientIds | List\<Long\> | 是 | 接收人ID列表 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.batchCreate()` | 接收请求 |
| App | `NotificationService.batchCreateNotifications()` | 业务逻辑 |
| App | `遍历recipientIds创建通知` | 批量处理 |
| Domain | `NotificationRepository.save()` | 保存每条通知 |
| Infrastructure | `NotificationMapper.insert()` | 批量插入 |

---

## 7. 标记通知为已读

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 标记通知为已读 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/markAsRead` |
| 接口描述 | 将指定的通知标记为已读状态 |

### 请求参数

**MarkReadCmd**:
```json
{
  "id": 100
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 通知ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "通知不存在或不属于当前用户"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.markAsRead()` | 接收请求，注入userId |
| App | `NotificationService.markAsRead()` | 业务逻辑+权限校验 |
| App | `验证通知归属` | 确保只能标记自己的通知 |
| Domain | `NotificationRepository.selectById()` | 查询通知 |
| Domain | `NotificationRepository.updateById()` | 更新is_read=1,read_time=NOW() |

---

## 8. 批量标记通知为已读

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 批量标记通知为已读 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/batchMarkAsRead` |
| 接口描述 | 批量将多个通知标记为已读状态 |

### 请求参数

**BatchMarkReadCmd**:
```json
{
  "ids": [100, 101, 102]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| ids | List\<Long\> | 是 | 通知ID列表 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.batchMarkAsRead()` | 接收请求 |
| App | `NotificationService.batchMarkAsRead()` | 业务逻辑 |
| App | `遍历ids标记已读` | 批量处理 |
| Domain | `NotificationRepository.updateById()` | 逐条更新 |

---

## 9. 标记所有通知为已读

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 标记所有通知为已读 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/markAllAsRead` |
| 接口描述 | 将当前用户的所有未读通知一次性标记为已读状态 |

### 请求参数

无参数（userId由@RequestAttribute注入）

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.markAllAsRead()` | 接收请求，注入userId |
| App | `NotificationService.markAllAsRead()` | 业务逻辑 |
| Infrastructure | `NotificationMapper.markAllAsRead()` | 批量更新 |
| Database | `UPDATE notification SET is_read=1, read_time=NOW() WHERE recipient_id=? AND is_read=0` | MySQL |

---

## 10. 删除通知

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除通知 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/delete` |
| 接口描述 | 删除指定的通知 |

### 请求参数

**DeleteNotificationCmd**:
```json
{
  "id": 100
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 通知ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "通知不存在或不属于当前用户"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.delete()` | 接收请求，注入userId |
| App | `NotificationService.deleteNotification()` | 业务逻辑+权限校验 |
| Domain | `NotificationRepository.selectById()` | 验证归属 |
| Domain | `NotificationRepository.deleteById()` | 删除通知 |

---

## 11. 批量删除通知

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 批量删除通知 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/batchDelete` |
| 接口描述 | 批量删除多个通知 |

### 请求参数

**BatchDeleteNotificationCmd**:
```json
{
  "ids": [100, 101, 102]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| ids | List\<Long\> | 是 | 通知ID列表 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.batchDelete()` | 接收请求 |
| App | `NotificationService.batchDeleteNotifications()` | 业务逻辑 |
| App | `遍历ids删除` | 批量处理 |
| Domain | `NotificationRepository.deleteById()` | 逐条删除 |

---

## 12. 知会用户关于审批实例

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 知会用户关于审批实例 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/notifyUsers` |
| 接口描述 | 将指定的审批实例知会给其他用户，被知会的用户可以查看该工单的详细信息 |

### 请求参数

**NotifyUsersCmd**:
```json
{
  "instanceId": 500,
  "recipientIds": [20, 30, 40],
  "message": "请查看该工单的审批进度"
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| instanceId | Long | 是 | 审批实例ID | @NotNull |
| recipientIds | List\<Long\> | 是 | 被知会用户ID列表 | @NotEmpty |
| message | String | 否 | 附加消息 | - |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "审批实例不存在"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.notifyUsers()` | 接收请求，注入发送人信息 |
| App | `NotificationService.notifyUsersAboutInstance()` | 业务逻辑 |
| App | `查询审批实例信息` | 获取工单详情 |
| App | `buildNotificationTitle()` | 生成通知标题（含业务类型） |
| App | `getBusinessTitle()` | 获取业务标题（素材/申请标题） |
| App | `创建知会通知` | 为每个接收人创建NOTIFY类型通知 |
| Infrastructure | `ApprovalInstanceMapper.selectById()` | 查询审批实例 |
| Infrastructure | `MaterialApplicationMapper/UsageApplyMapper/DeletionApplicationMapper.selectById()` | 查询业务标题 |
| Infrastructure | `NotificationMapper.insert()` | 插入通知记录 |

### 通知生成逻辑

```
1. 验证审批实例是否存在
2. 根据business_type查询对应业务表获取标题：
   - MATERIAL_ENTRY → material_application
   - ASSET_USAGE → usage_apply
   - ASSET_DELETION → asset_deletion_application
3. 生成通知标题格式："{业务类型} - {业务标题}"
4. 为每个recipientId创建NOTIFY类型通知
5. 记录notification_record（知会记录）
```

---

## 13. 获取工单的知会记录

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取工单的知会记录 |
| 请求方法 | POST |
| 请求路径 | `/api/notification/getNotificationRecords` |
| 接口描述 | 查询指定审批实例的所有知会记录，包括知会人、被知会人、知会时间等 |

### 请求参数

**GetNotificationRecordsQry**:
```json
{
  "instanceId": 500
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| instanceId | Long | 是 | 审批实例ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "instanceId": 500,
      "senderId": 10,
      "senderName": "张三",
      "recipientIds": "[20,30,40]",
      "message": "请查看该工单",
      "createTime": "2024-03-01 10:00:00"
    },
    {
      "id": 2,
      "instanceId": 500,
      "senderId": 15,
      "senderName": "李四",
      "recipientIds": "[25,35]",
      "message": "工单已通过",
      "createTime": "2024-03-02 14:00:00"
    }
  ]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `NotificationController.getNotificationRecords()` | 接收请求 |
| App | `NotificationService.getNotificationRecordsByInstanceId()` | 业务逻辑 |
| Infrastructure | `NotificationMapper.selectRecordsByInstanceId()` | 查询知会记录 |
| Database | `notification_record` 表 | MySQL |

---

## 附录：数据库表结构

### notification (通知表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR | 通知标题 |
| content | TEXT | 通知内容 |
| notification_type | VARCHAR | 通知类型（SYSTEM/APPROVAL/NOTIFY） |
| source_type | VARCHAR | 来源类型（WORKFLOW/ASSET） |
| source_id | BIGINT | 来源ID |
| sender_id | BIGINT | 发送人ID |
| sender_name | VARCHAR | 发送人姓名 |
| recipient_id | BIGINT | 接收人ID |
| is_read | INT | 是否已读（0=未读，1=已读） |
| read_time | DATETIME | 阅读时间 |
| create_time | DATETIME | 创建时间 |

### notification_record (知会记录表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| instance_id | BIGINT | 审批实例ID |
| sender_id | BIGINT | 知会人ID |
| sender_name | VARCHAR | 知会人姓名 |
| recipient_ids | TEXT | 被知会人ID列表（JSON） |
| message | TEXT | 附加消息 |
| create_time | DATETIME | 创建时间 |

---

## 附录：业务类型与标题映射

| business_type | 说明 | 业务标题来源表 |
|--------------|------|--------------|
| MATERIAL_ENTRY | 素材录入 | material_application.title |
| ASSET_USAGE | 素材使用 | usage_apply.title |
| ASSET_DELETION | 素材删除 | asset_deletion_application.title |

### 通知类型文本映射

| notificationType | notificationTypeText |
|------------------|---------------------|
| SYSTEM | 系统通知 |
| APPROVAL | 审批通知 |
| NOTIFY | 知会消息 |

### 来源类型文本映射

| sourceType | sourceTypeText |
|-----------|---------------|
| WORKFLOW | 工作流 |
| ASSET | 素材 |

---

## 附录：前端调用示例

### 获取知会事项列表（包含工单信息）

```typescript
// xuanjiao-frontend/src/api/notification.ts
export const getMyNotificationsWithWorkOrder = (data: {
  pageNum: number
  pageSize: number
  notificationType?: string
  isRead?: number
  keyword?: string
}) => {
  return request.post('/notification/getMyNotificationsWithWorkOrder', data)
}
```

### 知会用户

```typescript
export const notifyUsers = (data: {
  instanceId: number
  recipientIds: number[]
  message?: string
}) => {
  return request.post('/notification/notifyUsers', data)
}
```

### 获取未读数量

```typescript
export const getUnreadCount = () => {
  return request.post<{ count: number }>('/notification/getUnreadCount')
}
```

---

*本文档由API接口文档生成方法论自动生成。*
