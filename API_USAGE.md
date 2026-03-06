# API_USAGE.md - 素材使用申请接口文档

> **模块**: 素材使用申请 (usage-apply)
> **Controller**: `UsageApplyController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/usage/`
> **创建时间**: 2026-03-04

---

## 目录

1. [创建使用申请草稿](#1-创建使用申请草稿)
2. [更新使用申请草稿](#2-更新使用申请草稿)
3. [提交使用申请](#3-提交使用申请)
4. [删除使用申请](#4-删除使用申请)
5. [查询申请单详情](#5-查询申请单详情)
6. [查询草稿箱](#6-查询草稿箱)
7. [查询我的所有申请](#7-查询我的所有申请)
8. [检查是否有权限使用素材](#8-检查是否有权限使用素材)
9. [复制使用申请](#9-复制使用申请)

---

## 1. 创建使用申请草稿

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 创建使用申请草稿（多素材支持） |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/draft` |
| 接口描述 | 创建素材使用申请草稿，支持同时申请多个素材，每个素材可独立配置使用信息 |

### 请求参数

**UsageApplyCreateDraftCmd**:
```json
{
  "title": "2024年3月宣传素材使用申请",
  "assetConfigs": [
    {
      "assetId": 100,
      "usageDescription": "用于微信公众号宣传推文配图",
      "usagePublishChannel": "微信公众号",
      "usageIsSecondaryCreation": 1,
      "usageAttachmentPath": "/uploads/attachment/xxx.pdf"
    },
    {
      "assetId": 101,
      "usageDescription": "用于官网Banner图",
      "usagePublishChannel": "官方网站",
      "usageIsSecondaryCreation": 0
    }
  ]
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| title | String | 是 | 申请标题 | @NotBlank |
| assetConfigs | Array | 是 | 素材配置列表 | @NotEmpty |

**AssetUsageConfig**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| assetId | Long | 是 | 素材ID |
| usageDescription | String | 否 | 使用描述 |
| usagePublishChannel | String | 否 | 发布渠道 |
| usageIsSecondaryCreation | Integer | 否 | 是否二次创作（0/1） |
| usageAttachmentPath | String | 否 | 附件路径 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 50,
    "title": "2024年3月宣传素材使用申请",
    "userId": 10,
    "username": "张三",
    "deptId": 5,
    "deptName": "宣传部",
    "status": "DRAFT",
    "draft": 1,
    "createTime": "2024-03-01 10:00:00",
    "assets": [
      {
        "assetId": 100,
        "assetName": "宣传海报.jpg",
        "assetType": "IMAGE",
        "assetStatus": "APPROVED",
        "assetFilePath": "/uploads/IMAGE/xxx.jpg",
        "usageDescription": "用于微信公众号宣传推文配图",
        "usagePublishChannel": "微信公众号",
        "usageIsSecondaryCreation": 1
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageApplyController.createDraft()` | 接收请求，转换DTO |
| App | `UsageApplyService.createDraft()` | 业务逻辑 |
| App | `validateAndGetUser()` | 验证用户存在 |
| App | `createDraftApplication()` | 创建草稿申请单 |
| App | `saveAssetConfigurations()` | 保存素材配置到中间表 |
| Domain | `UsageApplyRepository.save()` | 保存申请单 |
| Domain | `UsageApplyAssetRepository.batchSave()` | 批量保存素材配置 |
| Infrastructure | `UsageApplyMapper.insert()` | 插入数据库 |
| Infrastructure | `UsageApplyAssetMapper.batchInsert()` | 插入中间表 |
| Database | `usage_apply`, `usage_apply_asset` 表 | MySQL |

### 中间表设计

**usage_apply_asset（素材使用配置表）**:
| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| usage_apply_id | BIGINT | 使用申请ID |
| asset_id | BIGINT | 素材ID |
| usage_description | VARCHAR | 使用描述 |
| usage_publish_channel | VARCHAR | 发布渠道 |
| usage_is_secondary_creation | INT | 是否二次创作 |
| usage_attachment_path | VARCHAR | 附件路径 |

---

## 2. 更新使用申请草稿

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新使用申请草稿 |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/update` |
| 接口描述 | 修改草稿状态的申请单，只能修改自己的草稿 |

### 请求参数

**UsageApplyUpdateCmd**:
```json
{
  "id": 50,
  "title": "2024年3月宣传素材使用申请（已修改）",
  "assetConfigs": [
    {
      "assetId": 102,
      "usageDescription": "用于官网Banner图",
      "usagePublishChannel": "官方网站"
    }
  ]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 申请单ID |
| title | String | 否 | 申请标题 |
| assetConfigs | Array | 否 | 素材配置列表（会完全替换） |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 50,
    "title": "2024年3月宣传素材使用申请（已修改）",
    "status": "DRAFT",
    ...
  }
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "只有草稿状态可以修改"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageApplyController.updateDraft()` | 接收请求 |
| App | `UsageApplyService.updateDraft()` | 业务逻辑 |
| App | `findAndValidateDraft()` | 验证草稿状态和权限 |
| App | `updateAssetConfigurations()` | 删除旧配置，保存新配置 |
| Infrastructure | `UsageApplyAssetMapper.deleteByUsageApplyId()` | 删除旧配置 |

### 更新条件

| 条件 | 要求 |
|-----|------|
| draft | 必须为1（草稿状态） |
| userId | 必须是当前用户 |

---

## 3. 提交使用申请

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 提交使用申请 |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/{id}/submit` |
| 接口描述 | 将草稿状态的申请提交审批 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| id | Long | 是 | 申请单ID | 路径参数 |
| workflowId | Long | 是 | 审批流程ID | URL参数 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": 500  // 审批实例ID
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "只有草稿或已驳回状态可以提交"
}
```

```json
{
  "code": 500,
  "message": "请至少选择一个素材并配置使用信息"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageApplyController.submit()` | 接收请求 |
| App | `UsageApplyService.submit()` | 业务逻辑 |
| App | `validateSubmitPermission()` | 验证权限 |
| App | `validateHasAssets()` | 验证至少有一个素材 |
| App | `WorkflowEngineService.startProcess()` | 启动审批流程 |
| Infrastructure | `UsageApplyMapper.update()` | 更新状态为PENDING |

---

## 4. 删除使用申请

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除使用申请（仅草稿） |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/delete` |
| 接口描述 | 删除申请单及其关联的素材配置 |

### 请求参数

**UsageApplyDeleteCmd**:
```json
{
  "id": 50
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 申请单ID |

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
| Adapter | `UsageApplyController.delete()` | 接收请求 |
| App | `UsageApplyService.delete()` | 业务逻辑 |
| Domain | `UsageApplyRepository.deleteById()` | 删除申请单（自动级联） |

---

## 5. 查询申请单详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询申请单详情 |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/getDetail` |
| 接口描述 | 查询申请单详情，包含素材配置信息 |

### 请求参数

**UsageApplyGetDetailQry**:
```json
{
  "id": 50
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 申请单ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 50,
    "title": "2024年3月宣传素材使用申请",
    "userId": 10,
    "username": "张三",
    "deptId": 5,
    "deptName": "宣传部",
    "status": "APPROVED",
    "workflowId": 2,
    "createTime": "2024-03-01 10:00:00",
    "assets": [
      {
        "assetId": 100,
        "assetName": "宣传海报.jpg",
        "assetType": "IMAGE",
        "assetStatus": "APPROVED",
        "assetFilePath": "/uploads/IMAGE/xxx.jpg",
        "usageDescription": "用于微信公众号宣传推文配图",
        "usagePublishChannel": "微信公众号",
        "usageIsSecondaryCreation": 1,
        "usageAttachmentPath": "/uploads/attachment/xxx.pdf"
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageApplyController.getDetail()` | 接收请求 |
| App | `UsageApplyService.getById()` | 业务逻辑 |
| Domain | `UsageApplyRepository.findById()` | 查询申请单 |
| Domain | `UsageApplyAssetRepository.findByUsageApplyId()` | 查询素材配置 |
| Infrastructure | `AssetMapper.selectById()` | 查询素材详情 |

---

## 6. 查询草稿箱

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询草稿箱 |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/getDrafts` |
| 接口描述 | 查询当前用户的草稿申请单 |

### 请求参数

**UsageApplyGetDraftsQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 5,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageApplyController.queryDrafts()` | 接收请求 |
| App | `UsageApplyService.queryDrafts()` | 业务逻辑 |
| Infrastructure | `UsageApplyMapper.selectListWithDetails()` | JOIN查询 |
| App | `convertWithDetails()` | 转换DTO |

### 查询条件

| 条件 | 值 |
|-----|-----|
| userId | 当前用户ID |
| draft | 1 |
| orderBy | create_time DESC |

---

## 7. 查询我的所有申请

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询我的所有申请 |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/getMyApplications` |
| 接口描述 | 查询当前用户的所有使用申请（包含所有状态） |

### 请求参数

**UsageApplyGetMyApplicationsQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
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
        "id": 50,
        "title": "2024年3月宣传素材使用申请",
        "status": "APPROVED",
        "assets": [...]
      },
      {
        "id": 51,
        "title": "2024年4月宣传素材使用申请",
        "status": "DRAFT",
        "assets": [...]
      }
    ],
    "total": 20,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageApplyController.queryMyApplications()` | 接收请求 |
| App | `UsageApplyService.queryMyApplications()` | 业务逻辑 |
| Infrastructure | `UsageApplyMapper.selectListWithDetails()` | JOIN查询 |

---

## 8. 检查是否有权限使用素材

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 检查是否有权限使用素材 |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/canUseAsset` |
| 接口描述 | 检查当前用户是否有已通过的申请可以使用该素材 |

### 请求参数

**UsageApplyCanUseAssetQry**:
```json
{
  "assetId": 100
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| assetId | Long | 是 | 素材ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": true  // true表示有权限，false表示无权限
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageApplyController.canUseAsset()` | 接收请求 |
| App | `UsageApplyService.canUseAsset()` | 业务逻辑 |
| Infrastructure | `AssetMapper.selectById()` | 检查素材状态 |
| Infrastructure | `UsageApplyAssetMapper.findByAssetId()` | 查询使用申请 |
| Domain | `UsageApplyRepository.findById()` | 检查申请状态 |

### 权限检查逻辑

```
1. 检查素材是否存在且可用（deleted=0, status≠DELETED）
2. 查询该素材的所有使用申请记录
3. 检查是否有当前用户的APPROVED状态申请
4. 返回检查结果
```

---

## 9. 复制使用申请

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 复制使用申请 |
| 请求方法 | POST |
| 请求路径 | `/api/usage-apply/{id}/copy` |
| 接口描述 | 复制申请单到新草稿，仅复制引用不复制文件 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| id | Long | 是 | 原申请单ID | 路径参数 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": 51  // 新申请单ID
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UsageApplyController.copyApplication()` | 接收请求 |
| App | `UsageApplyService.copyApplication()` | 业务逻辑 |
| App | `createDraftCopyApplication()` | 创建新草稿 |
| App | `copyAssetConfigurations()` | 复制素材配置 |
| Infrastructure | `UsageApplyAssetMapper.selectList()` | 查询原配置 |
| Infrastructure | `UsageApplyAssetMapper.insert()` | 插入新配置 |

---

## 附录：申请单状态流转

```
┌─────────┐
│  DRAFT  │ ← 创建申请单，可添加素材配置
└────┬────┘
     │ 提交审批
     ↓
┌─────────┐
│ PENDING │ ← 审批中，不可修改
└────┬────┘
     │ 审批结果
     ├────→ APPROVED ← 通过（可下载使用素材）
     │
     └────→ REJECTED ← 驳回（可重新提交）
```

---

## 附录：数据库表结构

### usage_apply (使用申请表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR | 申请标题 |
| user_id | BIGINT | 申请人ID |
| dept_id | BIGINT | 部门ID |
| workflow_id | BIGINT | 审批流程ID |
| status | VARCHAR | 状态（DRAFT/PENDING/APPROVED/REJECTED） |
| draft | INT | 是否草稿（1=草稿，0=非草稿） |
| create_time | DATETIME | 创建时间 |

### usage_apply_asset (使用申请素材关联表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| usage_apply_id | BIGINT | 使用申请ID |
| asset_id | BIGINT | 素材ID |
| usage_description | TEXT | 使用描述 |
| usage_publish_channel | VARCHAR | 发布渠道 |
| usage_is_secondary_creation | INT | 是否二次创作 |
| usage_attachment_path | VARCHAR | 附件路径 |

---

## 附录：前端调用示例

### 创建使用申请

```typescript
// xuanjiao-frontend/src/api/usage.ts
export const createUsageDraft = (data: {
  title: string
  assetConfigs: Array<{
    assetId: number
    usageDescription?: string
    usagePublishChannel?: string
    usageIsSecondaryCreation?: number
    usageAttachmentPath?: string
  }>
}) => {
  return request.post<number>('/usage-apply/draft', data)
}
```

### 提交申请

```typescript
export const submitUsageApplication = (id: number, workflowId: number) => {
  return request.post<number>(`/usage-apply/${id}/submit?workflowId=${workflowId}`)
}
```

---

*本文档由API接口文档生成方法论自动生成。*
