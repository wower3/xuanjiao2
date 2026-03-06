# API_DELETION.md - 素材删除申请接口文档

> **模块**: 素材删除申请 (asset-deletion)
> **Controller**: `AssetDeletionController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/deletion/`
> **创建时间**: 2026-03-04

---

## 目录

1. [创建删除申请](#1-创建删除申请)
2. [更新删除申请](#2-更新删除申请)
3. [查询申请单详情](#3-查询申请单详情)
4. [查询我的申请列表](#4-查询我的申请列表)
5. [查询草稿箱](#5-查询草稿箱)
6. [删除申请单](#6-删除申请单)
7. [提交审批](#7-提交审批)
8. [复制删除申请](#8-复制删除申请)

---

## 1. 创建删除申请

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 创建删除申请 |
| 请求方法 | POST |
| 请求路径 | `/api/asset-deletion/create` |
| 接口描述 | 创建素材删除申请，只能删除APPROVED状态的素材 |

### 请求参数

**AssetDeletionApplicationCreateCmd**:
```json
{
  "title": "过期素材清理申请",
  "deleteReason": "素材已过期，需要清理",
  "workflowId": 3,
  "attachmentPath": "/uploads/deletion/attachment.pdf",
  "assetIds": [100, 101, 102]
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| title | String | 是 | 申请标题 | @NotBlank |
| deleteReason | String | 否 | 删除原因 | - |
| workflowId | Long | 否 | 审批流程ID | - |
| attachmentPath | String | 否 | 附件路径 | - |
| assetIds | List\<Long\> | 否 | 素材ID列表 | - |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 50,
    "title": "过期素材清理申请",
    "applicantId": 10,
    "applicantName": "张三",
    "deptId": 5,
    "deptName": "宣传部",
    "status": "DRAFT",
    "deleteReason": "素材已过期，需要清理",
    "attachmentPath": "/uploads/deletion/attachment.pdf",
    "createTime": "2024-03-01 10:00:00",
    "assets": [
      {
        "id": 1,
        "assetId": 100,
        "assetName": "过期海报.jpg",
        "assetType": "IMAGE",
        "filePath": "/uploads/IMAGE/xxx.jpg",
        "thumbnailPath": "/uploads/thumbnail/xxx.jpg"
      }
    ]
  }
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "只能删除已通过审批的素材: 素材名称"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetDeletionController.create()` | 接收请求，转换DTO |
| App | `AssetDeletionApplicationService.create()` | 业务逻辑 |
| App | `validateAssetForDeletion()` | 验证素材状态为APPROVED |
| Domain | `AssetDeletionApplicationRepository.save()` | 保存申请单 |
| Infrastructure | `AssetDeletionApplicationMapper.insert()` | 插入数据库 |
| Infrastructure | `AssetDeletionAssetMapper.insert()` | 插入素材关联 |

### 删除规则

| 规则 | 说明 |
|-----|------|
| 素材状态 | 只能删除APPROVED状态的素材 |
| 删除流程 | 申请审批 → 素材变为DELETED → 7天后软删除 |

---

## 2. 更新删除申请

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新删除申请 |
| 请求方法 | POST |
| 请求路径 | `/api/asset-deletion/update` |
| 接口描述 | 修改删除申请信息，只能修改DRAFT状态的申请 |

### 请求参数

**AssetDeletionApplicationUpdateCmd**:
```json
{
  "id": 50,
  "title": "过期素材清理申请（已修改）",
  "deleteReason": "素材已过期，需要清理",
  "workflowId": 3,
  "attachmentPath": "/uploads/deletion/attachment.pdf",
  "assetIds": [100, 101]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 申请单ID |
| title | String | 否 | 申请标题 |
| deleteReason | String | 否 | 删除原因 |
| workflowId | Long | 否 | 审批流程ID |
| attachmentPath | String | 否 | 附件路径 |
| assetIds | List\<Long\> | 否 | 素材ID列表（会完全替换） |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 50,
    "title": "过期素材清理申请（已修改）",
    "status": "DRAFT",
    ...
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetDeletionController.update()` | 接收请求 |
| App | `AssetDeletionApplicationService.update()` | 业务逻辑 |
| App | `deleteDeletionAssetsByApplicationId()` | 删除旧素材关联 |
| App | `batchSaveDeletionAssets()` | 保存新素材关联 |

---

## 3. 查询申请单详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询申请单详情 |
| 请求方法 | POST |
| 请求路径 | `/api/asset-deletion/getDetail` |
| 接口描述 | 查询删除申请单详情，包含关联的素材信息 |

### 请求参数

**AssetDeletionApplicationGetDetailQry**:
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
    "title": "过期素材清理申请",
    "applicantId": 10,
    "applicantName": "张三",
    "deptId": 5,
    "deptName": "宣传部",
    "status": "PENDING",
    "deleteReason": "素材已过期，需要清理",
    "workflowId": 3,
    "createTime": "2024-03-01 10:00:00",
    "assets": [
      {
        "id": 1,
        "assetId": 100,
        "assetName": "过期海报.jpg",
        "assetType": "IMAGE",
        "filePath": "/uploads/IMAGE/xxx.jpg",
        "thumbnailPath": "/uploads/thumbnail/xxx.jpg"
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetDeletionController.getDetail()` | 接收请求 |
| App | `AssetDeletionApplicationService.getById()` | 业务逻辑 |
| Domain | `AssetDeletionApplicationRepository.findById()` | 查询申请单 |
| Infrastructure | `AssetDeletionAssetMapper.selectList()` | 查询素材关联 |
| Infrastructure | `AssetMapper.selectById()` | 查询素材详情 |

---

## 4. 查询我的申请列表

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询我的申请列表 |
| 请求方法 | POST |
| 请求路径 | `/api/asset-deletion/getMyApplications` |
| 接口描述 | 查询当前用户的所有删除申请 |

### 请求参数

**AssetDeletionApplicationGetMyApplicationsQry**:
```json
{
  "title": "过期",
  "status": "PENDING",
  "pageNum": 1,
  "pageSize": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| title | String | 否 | 标题筛选（模糊查询） |
| status | String | 否 | 状态筛选 |
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
        "title": "过期素材清理申请",
        "status": "PENDING",
        "assets": [...]
      }
    ],
    "total": 10,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetDeletionController.getMyApplications()` | 接收请求 |
| App | `AssetDeletionApplicationService.getMyApplications()` | 业务逻辑 |
| Domain | `AssetDeletionApplicationRepository.findByApplicant()` | 分页查询 |

---

## 5. 查询草稿箱

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询草稿箱 |
| 请求方法 | POST |
| 请求路径 | `/api/asset-deletion/getDrafts` |
| 接口描述 | 查询当前用户的DRAFT状态申请 |

### 请求参数

**AssetDeletionApplicationGetDraftsQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "title": "过期"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |
| title | String | 否 | 标题筛选（模糊查询） |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 3,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetDeletionController.queryDrafts()` | 接收请求 |
| App | `AssetDeletionApplicationService.queryDrafts()` | 业务逻辑 + 内存过滤 |
| Domain | `AssetDeletionApplicationRepository.findByApplicant()` | 查询后过滤DRAFT |

---

## 6. 删除申请单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除申请单 |
| 请求方法 | POST |
| 请求路径 | `/api/asset-deletion/delete` |
| 接口描述 | 删除DRAFT或REJECTED状态的申请单 |

### 请求参数

**AssetDeletionApplicationDeleteCmd**:
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
| Adapter | `AssetDeletionController.deleteById()` | 接收请求 |
| App | `AssetDeletionApplicationService.deleteById()` | 业务逻辑 |
| App | `deleteDeletionAssetsByApplicationId()` | 删除素材关联 |
| Domain | `AssetDeletionApplicationRepository.deleteById()` | 删除申请单 |

---

## 7. 提交审批

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 提交审批 |
| 请求方法 | POST |
| 请求路径 | `/api/asset-deletion/{id}/submit` |
| 接口描述 | 将申请提交到审批流程 |

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

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `AssetDeletionController.submitApproval()` | 接收请求 |
| App | `AssetDeletionApplicationService.submitApproval()` | 业务逻辑 |
| App | `WorkflowEngineService.startProcess()` | 启动审批流程 |
| Infrastructure | `AssetDeletionApplicationMapper.update()` | 更新状态为PENDING |

---

## 8. 复制删除申请

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 复制删除申请 |
| 请求方法 | POST |
| 请求路径 | `/api/asset-deletion/{id}/copy` |
| 接口描述 | 复制删除申请到新草稿 |

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
| Adapter | `AssetDeletionController.copyApplication()` | 接收请求 |
| App | `AssetDeletionApplicationService.copyApplication()` | 业务逻辑 |
| App | `createDraftCopyApplication()` | 创建新草稿 |
| App | `copyAssetAssociations()` | 复制素材关联 |

---

## 附录：删除流程说明

### 两阶段删除机制

```
┌─────────────────────────────────────────────────────────────────┐
│                    素材删除申请流程                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  用户提交申请                                                    │
│     ↓                                                           │
│  审批流程                                                        │
│     ↓                                                           │
│  审批通过                                                        │
│     ↓                                                           │
│  ┌─────────────────┐                                           │
│  │ 阶段1: DELETED   │ ← 素材状态变为DELETED                      │
│  │ - 可见但不可用    │   - 记录deletion_approve_time              │
│  │ - 标记删除时间    │   - 7天后自动清理                          │
│  └────────┬────────┘                                           │
│           │ 7天后                                              │
│           ↓                                                     │
│  ┌─────────────────┐                                           │
│  │ 阶段2: 软删除    │ ← 定时任务执行                             │
│  │ - deleted=1      │   - AssetDeletionCleanupTask              │
│  │ - 完全隐藏        │   - 每天2:00AM执行                         │
│  └─────────────────┘                                           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 状态可见性规则

| 状态 | 管理员可见 | 普通用户可见 | 可用 |
|-----|----------|------------|-----|
| APPROVED | ✓ | ✓ | ✓ |
| DRAFT | ✓ | ✗ | - |
| PENDING | ✓ | ✗ | - |
| DELETED | ✓ | ✓ | ✗ |
| deleted=1 | ✗ | ✗ | - |

---

## 附录：数据库表结构

### asset_deletion_application (删除申请表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR | 申请标题 |
| applicant_id | BIGINT | 申请人ID |
| dept_id | BIGINT | 部门ID |
| workflow_id | BIGINT | 审批流程ID |
| status | VARCHAR | 状态（DRAFT/PENDING/APPROVED/REJECTED） |
| delete_reason | TEXT | 删除原因 |
| attachment_path | VARCHAR | 附件路径 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### asset_deletion_asset (删除申请素材关联表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| deletion_application_id | BIGINT | 删除申请ID |
| asset_id | BIGINT | 素材ID |
| asset_name | VARCHAR | 素材名称（冗余） |
| asset_type | VARCHAR | 素材类型（冗余） |

---

*本文档由API接口文档生成方法论自动生成。*
