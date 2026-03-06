# API_ASSET.md - 素材管理接口文档

> **模块**: 素材管理 (asset)
> **Controller**: `AssetController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/asset/`
> **创建时间**: 2026-03-04

---

## 目录

1. [上传素材](#1-上传素材)
2. [查询素材详情](#2-查询素材详情)
3. [分页查询素材](#3-分页查询素材)
4. [查询用户已录入的素材（APPROVED状态）](#4-查询用户已录入的素材approved状态)
5. [管理员彻底删除素材](#5-管理员彻底删除素材)
6. [管理员调整素材删除时间（测试功能）](#6-管理员调整素材删除时间测试功能)
7. [手动触发定时任务（测试功能）](#7-手动触发定时任务测试功能)
8. [删除素材](#8-删除素材)
9. [预览素材](#9-预览素材)
10. [查看视频缩略图](#10-查看视频缩略图)
11. [下载素材](#11-下载素材)

---

## 1. 上传素材

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 上传素材 |
| 请求方法 | POST |
| 请求路径 | `/api/asset/upload` |
| 接口描述 | 上传新的素材文件（图片/视频），支持缩略图，文件MD5去重 |

### 请求参数

**FormData参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| file | File | 是 | 主文件（图片/视频） |
| thumbnailFile | File | 否 | 缩略图文件（仅视频需要） |
| name | String | 是 | 素材名称 |
| type | String | 是 | 素材类型（IMAGE/VIDEO/DOCUMENT） |
| copyright | String | 否 | 版权信息 |
| applicationId | Long | 否 | 素材录入申请ID（草稿上传） |
| workflowId | Long | 否 | 审批流程ID（直接上传需审批） |
| tagIds | List\<Long\> | 否 | 标签ID列表 |
| copyrightFilePath | String | 否 | 版权文件路径 |
| copyrightText | String | 否 | 版权声明文本 |
| description | String | 否 | 素材描述 |
| publishChannel | String | 否 | 发布渠道 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "name": "宣传海报",
    "type": "IMAGE",
    "filePath": "D:/xuanjiao/uploads/IMAGE/xxx.jpg",
    "fileSize": 1024000,
    "md5": "abc123...",
    "status": "PENDING",
    "tags": [
      {"id": 1, "name": "宣传", "category": "主题"}
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.upload()` | 接收multipart请求 |
| App | `AssetService.upload()` | 业务逻辑 |
| App | `validateFileFormat()` | 校验文件格式 |
| App | `handleFileUpload()` | 处理文件上传，计算MD5 |
| App | `saveAssetWithStatus()` | 根据场景设置状态并保存 |
| App | `saveTagAssociations()` | 保存标签关联 |
| Infrastructure | `AssetMapper.insert()` | 插入数据库 |
| Infrastructure | `AssetTagMapper.insert()` | 插入标签关联 |

### 状态设置规则

| 场景 | 初始状态 | 说明 |
|-----|---------|------|
| 有applicationId | DRAFT | 草稿状态，保存到素材录入申请 |
| 有workflowId | PENDING | 待审批状态，发起审批流程 |
| 都没有 | APPROVED | 直接通过（系统管理员可能） |

### 支持的文件格式

| 类型 | 支持格式 |
|-----|---------|
| IMAGE | jpg, jpeg, png, gif, webp |
| VIDEO | mp4, webm, ogg, mov, avi, mkv, mpg, mpeg, 3gp |

---

## 2. 查询素材详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询素材详情 |
| 请求方法 | POST |
| 请求路径 | `/api/asset/getDetail` |
| 接口描述 | 根据素材ID查询详细信息，包含标签信息 |

### 请求参数

**AssetGetDetailQry**:
```json
{
  "id": 100
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 素材ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "name": "宣传海报",
    "type": "IMAGE",
    "filePath": "D:/xuanjiao/uploads/IMAGE/xxx.jpg",
    "fileSize": 1024000,
    "md5": "abc123...",
    "status": "APPROVED",
    "uploadUserId": 10,
    "createTime": "2024-01-01 10:00:00",
    "tags": [
      {"id": 1, "name": "宣传", "category": "主题"}
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.getDetail()` | 接收HTTP请求 |
| App | `AssetService.getById()` | 业务逻辑 |
| App | `convertWithTags()` | 转换并加载标签信息 |
| Infrastructure | `AssetMapper.selectById()` | SQL查询 |
| Infrastructure | `AssetTagMapper.selectList()` | 查询标签关联 |
| Infrastructure | `TagMapper.selectBatchIds()` | 批量查询标签详情 |

---

## 3. 分页查询素材

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 分页查询素材 |
| 请求方法 | POST |
| 请求路径 | `/api/asset/list` |
| 接口描述 | 分页查询素材列表，根据用户角色权限过滤可见范围 |

### 请求参数

**AssetQueryCmd**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "name": "海报",
  "type": "IMAGE",
  "status": "APPROVED"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |
| name | String | 否 | 素材名称（模糊查询） |
| type | String | 否 | 素材类型（IMAGE/VIDEO/DOCUMENT） |
| status | String | 否 | 素材状态 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.list()` | 接收HTTP请求 |
| App | `AssetService.queryWithRoleFilter()` | 应用角色权限过滤 |
| App | `queryAssetsByRole()` | 根据角色查询素材 |
| App | `convertWithDownloadPermission()` | 转换并设置下载权限 |
| Infrastructure | `AssetMapper.selectList()` | SQL查询 |

### 权限可见规则

| 角色 | 可见状态范围 |
|-----|-------------|
| SYSTEM_ADMIN | APPROVED, PENDING, DELETED |
| GENERAL_MGMT | APPROVED, PENDING, DELETED |
| BRANCH_MGMT | APPROVED, DELETED（所属分部） |
| 其他 | APPROVED, DELETED |

### 响应字段

| 字段 | 类型 | 说明 |
|-----|------|------|
| canDownload | Boolean | 是否有权限下载（仅APPROVED状态为true） |

---

## 4. 查询用户已录入的素材（APPROVED状态）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询用户已录入的素材（APPROVED状态） |
| 请求方法 | POST |
| 请求路径 | `/api/asset/getMyApproved` |
| 接口描述 | 查询当前用户上传且已通过审批的素材，用于素材使用申请选择 |

### 请求参数

**AssetGetMyApprovedQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "name": "海报",
  "type": "IMAGE"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |
| name | String | 否 | 素材名称（模糊查询） |
| type | String | 否 | 素材类型 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 20,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.getMyApprovedAssets()` | 接收请求 |
| App | `AssetService.getMyApprovedAssets()` | 查询用户素材 |
| Infrastructure | `AssetMapper.selectPage()` | 分页查询 |

### 查询条件

| 条件 | 值 |
|-----|-----|
| uploadUserId | 当前用户ID |
| status | APPROVED |
| orderBy | create_time DESC |

---

## 5. 管理员彻底删除素材

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 管理员彻底删除素材 |
| 请求方法 | POST |
| 请求路径 | `/api/asset/adminDelete` |
| 接口描述 | 管理员可直接删除素材（无需审批），仅系统管理员可执行 |

### 请求参数

**AssetAdminDeleteCmd**:
```json
{
  "id": 100,
  "reason": "素材内容违规"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 素材ID |
| reason | String | 是 | 删除原因 |

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
  "code": 403,
  "message": "只有管理员才能执行此操作"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.adminDelete()` | 检查管理员权限 |
| App | `AssetService.adminDelete()` | 业务逻辑 |
| App | `validateAdminPermission()` | 验证管理员权限 |
| Infrastructure | `AssetMapper.updateDeletedById()` | 更新deleted=1 |
| App | `OperationLogService.log()` | 记录操作日志 |

### 权限要求

| 项目 | 要求 |
|-----|------|
| 角色 | 系统管理员（ROLE_ID=1） |
| 素材状态 | 只能删除APPROVED或DELETED状态 |

---

## 6. 管理员调整素材删除时间（测试功能）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 管理员调整素材删除时间（测试功能） |
| 请求方法 | POST |
| 请求路径 | `/api/asset/adjustDeleteTime` |
| 接口描述 | 将素材删除时间调整为7天前，用于测试定时清理任务 |

### 请求参数

**AssetAdjustDeleteTimeCmd**:
```json
{
  "id": 100
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 素材ID |

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
| Adapter | `AssetController.adjustDeleteTime()` | 检查管理员权限 |
| App | `AssetService.adjustDeleteTime()` | 业务逻辑 |
| Infrastructure | `AssetMapper.updateById()` | 更新deletionApproveTime |

### 处理逻辑

```
1. 验证管理员权限
2. 验证素材状态为DELETED
3. 将deletionApproveTime设置为7天前
4. 可通过triggerCleanupTask立即清理
```

---

## 7. 手动触发定时任务（测试功能）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 手动触发定时任务（测试功能） |
| 请求方法 | POST |
| 请求路径 | `/api/asset/admin/trigger-cleanup` |
| 接口描述 | 手动触发素材清理定时任务，返回清理的素材数量 |

### 请求参数

无（仅依赖userId进行权限验证）

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": 5
}
```

| 字段 | 说明 |
|-----|------|
| data | 清理的素材数量 |

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.triggerCleanupTask()` | 检查管理员权限 |
| App | `AssetService.triggerCleanupTask()` | 业务逻辑 |
| App | `AssetDeletionCleanupTask.cleanupDeletedAssetsManually()` | 执行清理任务 |
| Infrastructure | `AssetMapper.cleanupDeletedAssets()` | 批量更新deleted=1 |

### 清理条件

```
status = 'DELETED'
AND deletion_approve_time < 7天前
AND deleted = 0
```

---

## 8. 删除素材

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除素材 |
| 请求方法 | POST |
| 请求路径 | `/api/asset/delete` |
| 接口描述 | 删除指定素材（仅草稿状态可直接删除，已审批的需通过删除申请） |

### 请求参数

**AssetDeleteCmd**:
```json
{
  "id": 100
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 素材ID |

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
| Adapter | `AssetController.delete()` | 接收HTTP请求 |
| App | `AssetService.delete()` | 业务逻辑 |
| Infrastructure | `AssetMapper.deleteById()` | 逻辑删除 |

### 删除规则

| 素材状态 | 是否可删除 | 说明 |
|---------|----------|------|
| DRAFT | ✓ 可直接删除 | 草稿状态 |
| PENDING | ✗ 需通过流程驳回 | 待审批中 |
| APPROVED | ✗ 需通过删除申请 | 已通过审批 |
| REJECTED | ✗ 需通过流程 | 已驳回 |
| DELETED | ✗ 已删除 | 已标记删除 |

---

## 9. 预览素材

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 预览素材 |
| 请求方法 | GET |
| 请求路径 | `/api/asset/preview/{id}` |
| 接口描述 | 在浏览器中直接预览素材文件，无需登录认证 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| id | Long | 是 | 素材ID | 路径参数 |

### 响应结果

**成功**: 返回文件流（图片/视频/文档）

**失败**: 404 Not Found

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.preview()` | 接收HTTP请求 |
| App | `AssetService.getById()` | 查询素材信息 |
| File | 返回文件流 | 根据类型设置Content-Type |

### Content-Type映射

| 素材类型 | Content-Type |
|---------|-------------|
| IMAGE | image/jpeg |
| VIDEO | video/mp4 (根据扩展名判断) |
| DOCUMENT | application/pdf |

### 前端使用

```html
<img :src="`/api/asset/preview/${asset.id}`" />
<video :src="`/api/asset/preview/${asset.id}`" />
```

---

## 10. 查看视频缩略图

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查看视频缩略图 |
| 请求方法 | GET |
| 请求路径 | `/api/asset/thumbnail/{id}` |
| 接口描述 | 获取视频素材的缩略图图片，无需登录认证 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| id | Long | 是 | 视频素材ID | 路径参数 |

### 响应结果

**成功**: 返回缩略图文件流（image/jpeg）

**失败**: 404 Not Found

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.viewThumbnail()` | 接收HTTP请求 |
| App | `AssetService.getById()` | 查询素材信息 |
| File | 返回缩略图文件流 | Content-Type: image/jpeg |

### 前端使用

```html
<img :src="`/api/asset/thumbnail/${videoAsset.id}`" />
```

---

## 11. 下载素材

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 下载素材 |
| 请求方法 | GET |
| 请求路径 | `/api/asset/download/{id}` |
| 接口描述 | 下载素材文件，需要通过使用审批，自动记录使用日志 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| id | Long | 是 | 素材ID | 路径参数 |
| userId | Long | 是 | 当前用户ID | @RequestAttribute自动注入 |

### 响应结果

**成功**: 返回文件下载流（Content-Disposition: attachment）

**失败**:
- 403 Forbidden - "您没有下载此素材的权限，请先申请使用"
- 404 Not Found - 素材不存在

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetController.download()` | 接收HTTP请求 |
| App | `AssetService.getById()` | 查询素材信息 |
| App | `UsageApplyService.canUseAsset()` | 检查使用权限 |
| App | `UsageLogService.logDownload()` | 记录使用日志 |
| Infrastructure | `UsageApplyAssetMapper.findByAssetId()` | 查询使用配置 |
| File | 返回文件流 | Content-Disposition: attachment |

### 权限检查逻辑

```
1. 检查用户是否有该素材的使用审批（状态为APPROVED）
2. 从usage_apply_asset中间表查询使用配置信息
3. 记录下载日志（包含使用描述、发布渠道等）
4. 返回文件流
```

### 使用日志记录

| 字段 | 来源 |
|-----|------|
| assetId | 请求参数 |
| userId | @RequestAttribute |
| ip | getClientIp(request) |
| deptName | 用户所属部门 |
| usageDescription | usage_apply_asset表 |
| usagePublishChannel | usage_apply_asset表 |

---

## 附录：素材状态流转图

```
┌─────────┐
│  DRAFT  │ ← 通过applicationId上传
└────┬────┘
     │ 提交审批
     ↓
┌─────────┐
│ PENDING │ ← 通过workflowId上传
└────┬────┘
     │ 审批
     ├────→ APPROVED ←─────────────┐
     │                           │
     └────→ REJECTED              │
                                  │
┌─────────┐                       │
│ DELETED │ ← 删除申请审批通过     │
└────┬────┘                       │
     │ 7天后                     │
     ↓                           │
┌─────────┐                       │
│deleted=1│ ← 彻底软删除（不可见） │
└─────────┘                       │
                                   │
           ┌───────────────────────┘
           │
           ↓
    管理员直接删除（adminDelete）
```

---

## 附录：数据库表结构

### asset (素材表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 素材名称 |
| type | VARCHAR | 类型（IMAGE/VIDEO/DOCUMENT） |
| file_path | VARCHAR | 文件路径 |
| file_size | BIGINT | 文件大小（字节） |
| thumbnail_path | VARCHAR | 缩略图路径（视频专用） |
| md5 | VARCHAR | 文件MD5值（用于去重） |
| status | VARCHAR | 状态（DRAFT/PENDING/APPROVED/REJECTED/DELETED） |
| copyright | VARCHAR | 版权信息 |
| upload_user_id | BIGINT | 上传用户ID |
| application_id | BIGINT | 素材录入申请ID |
| deletion_approve_time | DATETIME | 删除审批通过时间 |
| deleted | INT | 逻辑删除标记（0=正常，1=已删除） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### asset_tag (素材标签关联表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| asset_id | BIGINT | 素材ID |
| tag_id | BIGINT | 标签ID |

### tag (标签表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 标签名称 |
| category | VARCHAR | 标签分类 |

---

## 附录：前端调用示例

### 上传素材

```typescript
// xuanjiao-frontend/src/api/asset.ts
export const uploadAsset = (formData: FormData) => {
  return request.post<AssetDTO>('/asset/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 使用示例
const uploadFile = async (file: File, thumbnail?: File) => {
  const formData = new FormData()
  formData.append('file', file)
  if (thumbnail) {
    formData.append('thumbnailFile', thumbnail)
  }
  formData.append('name', file.name)
  formData.append('type', 'IMAGE')
  formData.append('tagIds', JSON.stringify([1, 2]))

  const result = await uploadAsset(formData)
  return result.data
}
```

### 下载素材

```typescript
export const downloadAsset = (id: number) => {
  return request.get(`/asset/download/${id}`, {
    responseType: 'blob'
  })
}

// 使用示例
const handleDownload = async (asset: Asset) => {
  try {
    const blob = await downloadAsset(asset.id)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = asset.name
    a.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载失败', error)
  }
}
```

---

*本文档由API接口文档生成方法论自动生成。*
