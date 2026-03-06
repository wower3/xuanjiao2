# API_MATERIAL.md - 素材录入申请接口文档

> **模块**: 素材录入申请 (material-application)
> **Controller**: `MaterialApplicationController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/material/`
> **创建时间**: 2026-03-04

---

## 目录

1. [创建申请单（草稿）](#1-创建申请单草稿)
2. [更新申请单](#2-更新申请单)
3. [提交申请单](#3-提交申请单)
4. [删除申请单](#4-删除申请单)
5. [查询申请单详情](#5-查询申请单详情)
6. [查询草稿箱](#6-查询草稿箱)
7. [查询我的申请单](#7-查询我的申请单)
8. [复制申请单](#8-复制申请单)

---

## 1. 创建申请单（草稿）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 创建申请单（草稿） |
| 请求方法 | POST |
| 请求路径 | `/api/material-application/create` |
| 接口描述 | 创建新的素材录入申请单，初始状态为DRAFT |

### 请求参数

**MaterialApplicationCreateCmd**:
```json
{
  "title": "2024年第一季度宣传素材",
  "maintainerId": 10,
  "deptId": 5,
  "guaranteeDeclaration": 1
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| title | String | 是 | 申请标题 | @NotBlank |
| maintainerId | Long | 否 | 维护人ID（默认为当前用户） | - |
| deptId | Long | 否 | 部门ID（默认为当前用户部门） | - |
| guaranteeDeclaration | Integer | 否 | 保障声明（0/1） | - |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "title": "2024年第一季度宣传素材",
    "applicantId": 10,
    "applicantName": "张三",
    "maintainerId": 10,
    "maintainerName": "张三",
    "deptId": 5,
    "deptName": "宣传部",
    "status": "DRAFT",
    "guaranteeDeclaration": 1,
    "createTime": "2024-01-01 10:00:00",
    "assets": []
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `MaterialApplicationController.create()` | 接收HTTP请求，转换DTO |
| App | `MaterialApplicationService.create()` | 业务逻辑 |
| App | `MaterialApplicationServiceImpl.create()` | 创建申请单，设置默认值 |
| Domain | `MaterialApplicationRepository.save()` | 持久化申请单 |
| Infrastructure | `MaterialApplicationMapper.insert()` | 插入数据库 |
| Database | `material_application` 表 | MySQL |

### 默认值规则

| 字段 | 默认值 | 说明 |
|-----|-------|------|
| maintainerId | 当前用户ID | 未传则使用当前用户 |
| deptId | 当前用户部门ID | 未传则使用当前用户部门 |
| guaranteeDeclaration | 0 | 未传则默认为0 |
| status | DRAFT | 初始状态 |

---

## 2. 更新申请单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新申请单 |
| 请求方法 | POST |
| 请求路径 | `/api/material-application/update` |
| 接口描述 | 修改申请单信息，仅DRAFT状态且申请人是当前用户时可修改 |

### 请求参数

**MaterialApplicationUpdateCmd**:
```json
{
  "id": 100,
  "title": "2024年第一季度宣传素材（已修改）",
  "maintainerId": 11,
  "deptId": 5,
  "guaranteeDeclaration": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 申请单ID |
| title | String | 否 | 申请标题 |
| maintainerId | Long | 否 | 维护人ID |
| deptId | Long | 否 | 部门ID |
| guaranteeDeclaration | Integer | 否 | 保障声明 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 100,
    "title": "2024年第一季度宣传素材（已修改）",
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

```json
{
  "code": 500,
  "message": "只能修改自己的申请单"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `MaterialApplicationController.update()` | 接收请求，转换DTO |
| App | `MaterialApplicationService.update()` | 业务逻辑 |
| App | `validateUpdatePermission()` | 验证权限（DRAFT状态 + 申请人） |
| Domain | `MaterialApplicationRepository.update()` | 更新数据 |

### 修改条件

| 条件 | 要求 |
|-----|------|
| 状态 | 必须为DRAFT |
| 申请人 | 必须是当前用户 |

---

## 3. 提交申请单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 提交申请单 |
| 请求方法 | POST |
| 请求路径 | `/api/material-application/{id}/submit` |
| 接口描述 | 将申请单提交审批，需要至少上传一个素材文件 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| id | Long | 是 | 申请单ID | 路径参数 |
| workflowId | Long | 是 | 审批流程ID | URL参数 |

**请求示例**:
```
POST /api/material-application/100/submit?workflowId=1
```

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
  "message": "请至少上传一个素材文件"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `MaterialApplicationController.submit()` | 接收请求 |
| App | `MaterialApplicationService.submit()` | 业务逻辑 |
| App | `validateSubmitPermission()` | 验证权限（DRAFT/REJECTED状态 + 申请人） |
| App | `validateHasAssets()` | 验证至少有一个素材文件 |
| App | `AssetService.updateStatusByApplicationId()` | 更新关联素材状态为PENDING |
| App | `WorkflowEngineService.startProcess()` | 启动审批流程 |
| Infrastructure | `MaterialApplicationMapper.update()` | 更新申请单状态为PENDING |

### 提交验证

| 验证项 | 规则 |
|-------|------|
| 状态 | DRAFT或REJECTED |
| 申请人 | 必须是当前用户 |
| 素材文件 | 至少一个 |

### 提交后状态变化

| 对象 | 变化前 | 变化后 |
|-----|-------|-------|
| 申请单 | DRAFT/REJECTED | PENDING |
| 关联素材 | DRAFT | PENDING |
| 审批实例 | - | 创建新实例 |

---

## 4. 删除申请单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除申请单 |
| 请求方法 | POST |
| 请求路径 | `/api/material-application/delete` |
| 接口描述 | 删除申请单及其关联的所有素材文件（仅DRAFT状态） |

### 请求参数

**MaterialApplicationDeleteCmd**:
```json
{
  "id": 100
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
| Adapter | `MaterialApplicationController.delete()` | 接收请求 |
| App | `MaterialApplicationService.delete()` | 业务逻辑 |
| App | `cascadeDeleteAssets()` | 级联删除关联素材和标签 |
| Infrastructure | `AssetTagMapper.delete()` | 删除素材标签关联 |
| Infrastructure | `AssetMapper.deleteById()` | 删除素材 |
| Infrastructure | `MaterialApplicationMapper.delete()` | 删除申请单 |

### 删除级联

```
1. 删除素材标签关联（asset_tag表）
2. 删除素材文件记录（asset表）
3. 删除申请单（material_application表）
```

### 删除条件

| 条件 | 要求 |
|-----|------|
| 状态 | 必须为DRAFT |
| 申请人 | 必须是当前用户 |

---

## 5. 查询申请单详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询申请单详情 |
| 请求方法 | POST |
| 请求路径 | `/api/material-application/getDetail` |
| 接口描述 | 查询申请单详情，包含关联的素材和标签信息 |

### 请求参数

**MaterialApplicationGetDetailQry**:
```json
{
  "id": 100
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
    "id": 100,
    "title": "2024年第一季度宣传素材",
    "applicantId": 10,
    "applicantName": "张三",
    "maintainerId": 10,
    "maintainerName": "张三",
    "deptId": 5,
    "deptName": "宣传部",
    "status": "PENDING",
    "guaranteeDeclaration": 1,
    "workflowId": 1,
    "createTime": "2024-01-01 10:00:00",
    "assets": [
      {
        "id": 200,
        "name": "宣传海报.jpg",
        "type": "IMAGE",
        "status": "PENDING",
        "tags": [
          {"id": 1, "name": "宣传", "category": "主题"}
        ]
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `MaterialApplicationController.getDetail()` | 接收请求 |
| App | `MaterialApplicationService.getById()` | 业务逻辑 |
| App | `convert()` | 转换DTO并填充关联数据 |
| App | `populateUserInfo()` | 填充用户/部门信息 |
| App | `convertAssetsWithTags()` | 转换素材及标签 |
| Infrastructure | `MaterialApplicationMapper.selectById()` | 查询申请单 |
| Infrastructure | `AssetMapper.selectList()` | 查询关联素材 |
| Infrastructure | `AssetTagMapper.selectList()` | 查询素材标签 |

---

## 6. 查询草稿箱

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询草稿箱 |
| 请求方法 | POST |
| 请求路径 | `/api/material-application/getDrafts` |
| 接口描述 | 查询当前用户的所有DRAFT状态的申请单 |

### 请求参数

**MaterialApplicationGetDraftsQry**:
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
| Adapter | `MaterialApplicationController.queryDrafts()` | 接收请求 |
| App | `MaterialApplicationService.queryDrafts()` | 业务逻辑 |
| Infrastructure | `MaterialApplicationMapper.selectListWithDetails()` | JOIN查询（避免N+1） |
| App | `filter(DRAFT status)` | 内存过滤草稿状态 |
| App | `convertWithDetails()` | 转换DTO |

### 查询条件

| 条件 | 值 |
|-----|-----|
| applicantId | 当前用户ID |
| status | DRAFT |
| orderBy | create_time DESC |

---

## 7. 查询我的申请单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询我的申请单 |
| 请求方法 | POST |
| 请求路径 | `/api/material-application/getMyApplications` |
| 接口描述 | 查询当前用户的所有申请单（包含所有状态） |

### 请求参数

**MaterialApplicationGetMyApplicationsQry**:
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
        "id": 100,
        "title": "2024年第一季度宣传素材",
        "status": "APPROVED",
        ...
      },
      {
        "id": 101,
        "title": "2024年第二季度宣传素材",
        "status": "DRAFT",
        ...
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
| Adapter | `MaterialApplicationController.queryMyApplications()` | 接收请求 |
| App | `MaterialApplicationService.queryMyApplications()` | 业务逻辑 |
| Infrastructure | `MaterialApplicationMapper.selectListWithDetails()` | JOIN查询 |

---

## 8. 复制申请单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 复制申请单 |
| 请求方法 | POST |
| 请求路径 | `/api/material-application/{id}/copy` |
| 接口描述 | 复制原申请单及其关联的素材信息到新草稿 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| id | Long | 是 | 原申请单ID | 路径参数 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": 101  // 新申请单ID
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `MaterialApplicationController.copyApplication()` | 接收请求 |
| App | `MaterialApplicationService.copyApplication()` | 业务逻辑 |
| App | `createDraftCopyApplication()` | 创建新草稿申请单 |
| App | `copyAssetsFromOriginal()` | 复制素材文件 |
| App | `copyAssetFiles()` | 复制物理文件（主文件、缩略图、版权文件） |
| App | `copyAssetTags()` | 复制素材标签关联 |
| Infrastructure | `MaterialApplicationMapper.insert()` | 插入新申请单 |
| Infrastructure | `AssetMapper.insert()` | 插入新素材 |

### 复制内容

| 内容 | 处理方式 |
|-----|---------|
| 申请单基本信息 | 复制，标题添加" - 副本" |
| 素材文件记录 | 复制，文件名添加时间戳 |
| 物理文件 | 复制到同目录，文件名添加时间戳 |
| 素材标签关联 | 复制 |

### 新申请单状态

| 字段 | 值 |
|-----|-----|
| status | DRAFT |
| applicantId | 当前用户 |
| maintainerId | 当前用户 |
| deptId | 当前用户部门 |

---

## 附录：申请单状态流转

```
┌─────────┐
│  DRAFT  │ ← 创建申请单，可上传素材
└────┬────┘
     │ 提交审批
     ↓
┌─────────┐
│ PENDING │ ← 审批中，不可修改
└────┬────┘
     │ 审批结果
     ├────→ APPROVED ← 通过
     │
     └────→ REJECTED ← 驳回（可重新提交）
```

---

## 附录：数据库表结构

### material_application (素材录入申请表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| title | VARCHAR | 申请标题 |
| applicant_id | BIGINT | 申请人ID |
| maintainer_id | BIGINT | 维护人ID |
| dept_id | BIGINT | 部门ID |
| guarantee_declaration | INT | 保障声明（0/1） |
| workflow_id | BIGINT | 审批流程ID |
| status | VARCHAR | 状态（DRAFT/PENDING/APPROVED/REJECTED） |
| deleted | INT | 逻辑删除标记 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### asset (素材表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| application_id | BIGINT | 关联的申请单ID |
| ... | ... | 其他字段（见API_ASSET.md） |

---

## 附录：前端调用示例

### 创建申请单并上传素材

```typescript
// xuanjiao-frontend/src/api/material.ts
export const createApplication = (data: {
  title: string
  maintainerId?: number
  deptId?: number
  guaranteeDeclaration?: number
}) => {
  return request.post<number>('/material-application/create', data)
}

// 使用示例
import { createApplication } from '@/api/material'

const createAndUpload = async () => {
  // 1. 创建申请单
  const result = await createApplication({
    title: '2024年宣传素材',
    guaranteeDeclaration: 1
  })
  const applicationId = result.data

  // 2. 上传素材文件到该申请单
  await uploadAsset(file, null, {
    applicationId,
    name: file.name,
    type: 'IMAGE'
  })
}
```

---

*本文档由API接口文档生成方法论自动生成。*
