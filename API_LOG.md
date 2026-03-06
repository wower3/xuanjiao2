# API_LOG.md - 使用日志接口文档

> **模块**: 使用日志 (usage-log)
> **Controller**: `UsageLogController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/usage/`
> **创建时间**: 2026-03-04

---

## 目录

1. [查询日志](#1-查询日志)
2. [查询素材使用记录](#2-查询素材使用记录)

---

## 1. 查询日志

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询日志 |
| 请求方法 | POST |
| 请求路径 | `/api/log/queryLogs` |
| 接口描述 | 分页查询素材使用日志，支持按操作类型筛选 |

### 请求参数

**LogQueryLogsQry**:
```json
{
  "action": "DOWNLOAD",
  "pageNum": 1,
  "pageSize": 10
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| action | String | 否 | 操作类型筛选 | - |
| pageNum | Integer | 是 | 页码 | @Min(1) |
| pageSize | Integer | 是 | 每页数量 | @Min(1) |

### 操作类型说明

| action | 说明 |
|--------|------|
| DOWNLOAD | 下载 |
| PREVIEW | 预览 |
| VIEW | 查看 |
| LOGIN | 登录 |
| LOGOUT | 登出 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1001,
        "assetId": 50,
        "userId": 10,
        "username": "张三",
        "action": "DOWNLOAD",
        "ip": "192.168.1.100",
        "deptName": "宣传部",
        "usageDescription": "用于微信公众号宣传推文配图",
        "usagePublishChannel": "微信公众号",
        "createTime": "2024-03-01 10:00:00"
      },
      {
        "id": 1002,
        "assetId": 51,
        "userId": 20,
        "username": "李四",
        "action": "DOWNLOAD",
        "ip": "192.168.1.101",
        "deptName": "技术部",
        "usageDescription": "用于官网Banner图",
        "usagePublishChannel": "官方网站",
        "createTime": "2024-03-01 11:00:00"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageLogController.list()` | 接收HTTP请求 |
| App | `UsageLogService.query()` | 业务逻辑 |
| Domain | `UsageLogRepository.selectPage()` | 分页查询 |
| Infrastructure | `UsageLogMapper.selectPage()` | MyBatis查询 |
| Database | `usage_log` 表 | MySQL |

### 查询条件

| 条件 | 值 |
|-----|-----|
| action | 可选筛选（如果指定） |
| orderBy | create_time DESC |

---

## 2. 查询素材使用记录

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询素材使用记录 |
| 请求方法 | POST |
| 请求路径 | `/api/log/getAssetUsageLogs` |
| 接口描述 | 查询指定素材的所有使用记录，包括使用者、使用时间、使用描述、发布渠道等信息 |

### 请求参数

**LogGetAssetUsageLogsQry**:
```json
{
  "assetId": 50,
  "pageNum": 1,
  "pageSize": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| assetId | Long | 是 | 素材ID |
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": 1001,
        "assetId": 50,
        "userId": 10,
        "username": "张三",
        "action": "DOWNLOAD",
        "ip": "192.168.1.100",
        "deptName": "宣传部",
        "usageDescription": "用于微信公众号宣传推文配图",
        "usagePublishChannel": "微信公众号",
        "createTime": "2024-03-01 10:00:00"
      },
      {
        "id": 1005,
        "assetId": 50,
        "userId": 15,
        "username": "王五",
        "action": "DOWNLOAD",
        "ip": "192.168.1.105",
        "deptName": "市场部",
        "usageDescription": "用于活动宣传材料",
        "usagePublishChannel": "线下活动",
        "createTime": "2024-03-02 14:00:00"
      }
    ],
    "total": 5,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageLogController.getAssetUsageLogs()` | 接收HTTP请求 |
| App | `UsageLogService.getAssetUsageLogs()` | 业务逻辑 |
| Domain | `UsageLogRepository.selectByAssetId()` | 按素材ID查询 |
| Infrastructure | `UsageLogMapper.selectByAssetId()` | MyBatis查询 |
| Database | `usage_log` 表 | MySQL |

### 查询条件

| 条件 | 值 |
|-----|-----|
| asset_id | 指定素材ID |
| orderBy | create_time DESC |

---

## 附录：服务方法（内部调用）

### 记录使用日志

```java
void log(Long assetId, Long userId, String action, String ip)
```

**说明**: 记录用户对素材的操作行为（下载、预览等）

**调用时机**:
- 用户下载素材时
- 用户预览素材时

**实现逻辑**:
1. 创建UsageLog实体
2. 设置assetId、userId、action、ip
3. 保存到数据库

### 记录下载日志

```java
void logDownload(Long assetId, Long userId, String ip, String deptName,
                 String usageDescription, String usagePublishChannel)
```

**说明**: 记录用户下载素材的详细信息，包含使用说明和发布渠道

**调用时机**:
- 用户通过使用申请下载素材时
- 记录完整的使用信息用于版权管理

**实现逻辑**:
1. 创建UsageLog实体
2. 设置action="DOWNLOAD"
3. 记录使用说明和发布渠道
4. 保存到数据库

---

## 附录：数据库表结构

### usage_log (使用日志表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| asset_id | BIGINT | 素材ID |
| user_id | BIGINT | 操作用户ID |
| action | VARCHAR | 操作类型（DOWNLOAD/PREVIEW/VIEW等） |
| ip | VARCHAR | 操作IP地址 |
| dept_name | VARCHAR | 用户所属部门名称 |
| usage_description | TEXT | 使用说明 |
| usage_publish_channel | VARCHAR | 使用发布渠道 |
| create_time | DATETIME | 创建时间 |

---

## 附录：使用场景

### 场景1：素材下载审计

管理员可通过查询素材使用记录了解：
- 哪些用户下载了该素材
- 下载的时间和IP地址
- 使用说明和发布渠道
- 用于版权追踪和使用统计

### 场景2：使用统计分析

根据使用日志可进行：
- 按部门统计素材使用量
- 按发布渠道分析素材传播情况
- 按时间维度统计使用趋势

### 场景3：日志记录流程

```
用户下载素材
    ↓
AssetController.download()
    ↓
UsageLogService.logDownload()
    ↓
记录到 usage_log 表
    ↓
返回文件流给用户
```

---

## 附录：前端调用示例

### 查询日志

```typescript
// xuanjiao-frontend/src/api/log.ts
export const queryLogs = (data: {
  action?: string
  pageNum: number
  pageSize: number
}) => {
  return request.post('/log/queryLogs', data)
}
```

### 查询素材使用记录

```typescript
export const getAssetUsageLogs = (data: {
  assetId: number
  pageNum: number
  pageSize: number
}) => {
  return request.post('/log/getAssetUsageLogs', data)
}
```

### 素材详情页展示使用记录

```typescript
// 在素材详情页面显示该素材的使用历史
const fetchUsageLogs = async (assetId: number) => {
  const result = await getAssetUsageLogs({
    assetId,
    pageNum: 1,
    pageSize: 20
  })
  return result.data.list
}
```

---

*本文档由API接口文档生成方法论自动生成。*
