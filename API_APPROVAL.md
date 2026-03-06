# API_APPROVAL.md - 审批管理接口文档

> **模块**: 审批管理 (approval)
> **Controller**: `ApprovalController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/approval/`
> **创建时间**: 2026-03-04

---

## 目录

1. [获取待办任务列表](#1-获取待办任务列表)
2. [获取待办任务数量](#2-获取待办任务数量)
3. [获取我发起的申请](#3-获取我发起的申请)
4. [获取任务详情](#4-获取任务详情)
5. [获取审批实例详情](#5-获取审批实例详情)
6. [审批通过/拒绝](#6-审批通过拒绝)
7. [退回上一级](#7-退回上一级)
8. [获取流经事项列表](#8-获取流经事项列表)

---

## 1. 获取待办任务列表

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取待办任务列表 |
| 请求方法 | POST |
| 请求路径 | `/api/approval/getMyTasks` |
| 接口描述 | 查询当前用户待处理的审批任务，支持按业务类型筛选 |

### 请求参数

**ApprovalGetMyTasksQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "businessType": "MATERIAL_ENTRY"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |
| businessType | String | 否 | 业务类型（MATERIAL_ENTRY/ASSET_USAGE/ASSET_DELETION） |

### 业务类型说明

| businessType | 说明 |
|-------------|------|
| MATERIAL_ENTRY | 素材录入 |
| ASSET_USAGE | 素材使用 |
| ASSET_DELETION | 素材删除 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "taskId": 500,
        "taskName": "部门审批-素材录入申请",
        "businessType": "MATERIAL_ENTRY",
        "businessTitle": "2024年3月宣传素材",
        "applicantId": 10,
        "applicantName": "张三",
        "deptId": 5,
        "deptName": "宣传部",
        "workflowId": 1,
        "workflowName": "素材录入审批流程",
        "stageId": 10,
        "stageName": "部门审批",
        "stageOrder": 1,
        "approveType": "AND",
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
| Adapter | `ApprovalController.getMyTasks()` | 接收HTTP请求 |
| App | `ApprovalService.getMyTasks()` | 业务逻辑 |
| Infrastructure | `ApprovalTaskMapper.selectPendingTasksWithFilters()` | 分页查询待办任务 |
| Database | `approval_task`, `approval_instance` 表 | MySQL |

### 查询条件

| 条件 | 值 |
|-----|-----|
| approver_id | 当前用户ID |
| status | PENDING |
| business_type | 可选筛选（如果指定） |

---

## 2. 获取待办任务数量

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取待办任务数量 |
| 请求方法 | POST |
| 请求路径 | `/api/approval/getMyTasksCount` |
| 接口描述 | 查询当前用户待处理任务总数，用于前端徽章显示 |

### 请求参数

**ApprovalGetMyTasksCountQry**:
```json
{}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| - | - | - | 无参数（从token获取userId） |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": 5
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `ApprovalController.getMyTasksCount()` | 接收请求 |
| App | `ApprovalService.getMyTasksCount()` | 业务逻辑 |
| Infrastructure | `ApprovalTaskMapper.countPendingTasksByUserId()` | 统计数量 |
| Database | `approval_task` 表 | MySQL |

---

## 3. 获取我发起的申请

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取我发起的申请 |
| 请求方法 | POST |
| 请求路径 | `/api/approval/getMyApplied` |
| 接口描述 | 查询当前用户发起的审批申请，支持多条件筛选 |

### 请求参数

**ApprovalGetMyAppliedQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "businessType": "MATERIAL_ENTRY",
  "status": "APPROVED",
  "forAllUsers": false
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |
| businessType | String | 否 | 业务类型筛选 |
| status | String | 否 | 状态筛选（PENDING/APPROVED/REJECTED/CANCELLED） |
| forAllUsers | Boolean | 否 | 是否查看所有用户（管理员功能） |
| applicantId | Long | 否 | 发起人ID筛选（管理员查看时可用） |
| deptId | Long | 否 | 部门ID筛选（管理员查看时可用） |
| roleType | String | 否 | 角色类型筛选（管理员查看时可用） |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "instanceId": 100,
        "businessType": "MATERIAL_ENTRY",
        "businessTitle": "2024年3月宣传素材",
        "applicantId": 10,
        "applicantName": "张三",
        "deptName": "宣传部",
        "workflowName": "素材录入审批流程",
        "status": "APPROVED",
        "createTime": "2024-03-01 10:00:00",
        "approveTime": "2024-03-02 15:30:00"
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
| Adapter | `ApprovalController.getMyApplied()` | 接收请求 |
| App | `ApprovalService.getMyApplied()` | 业务逻辑 |
| Infrastructure | `ApprovalInstanceMapper.selectWithFilters()` | 分页查询实例 |

---

## 4. 获取任务详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取任务详情 |
| 请求方法 | POST |
| 请求路径 | `/api/approval/getTaskDetail` |
| 接口描述 | 查询审批任务的完整详情，包含可选审批人信息 |

### 请求参数

**ApprovalGetTaskDetailQry**:
```json
{
  "taskId": 500
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| taskId | Long | 是 | 任务ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 500,
    "taskName": "部门审批-素材录入申请",
    "businessType": "MATERIAL_ENTRY",
    "businessTitle": "2024年3月宣传素材",
    "applicantId": 10,
    "applicantName": "张三",
    "deptId": 5,
    "deptName": "宣传部",
    "workflowId": 1,
    "workflowName": "素材录入审批流程",
    "stageId": 10,
    "stageName": "部门审批",
    "stageOrder": 1,
    "approveType": "AND",
    "createTime": "2024-03-01 10:00:00",
    "availableApprovers": [
      {
        "approverType": "USER",
        "approverId": 15,
        "approverName": "李审核",
        "deptName": "审核部"
      }
    ],
    "currentStageApprovers": [
      {
        "approverId": 11,
        "approverName": "王审核",
        "status": "PENDING"
      }
    ],
    "approvalProgress": [
      {
        "stageId": 10,
        "stageName": "部门审批",
        "stageOrder": 1,
        "status": "IN_PROGRESS",
        "approvers": [...]
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `ApprovalController.getTaskDetail()` | 接收请求 |
| App | `ApprovalService.getTaskDetail()` | 业务逻辑 |
| App | `ApproverSelectionService.getTaskDetail()` | 获取可选审批人 |
| Infrastructure | `ApprovalTaskMapper.selectById()` | 查询任务 |
| Infrastructure | `ApprovalInstanceMapper.selectById()` | 查询实例 |
| Infrastructure | `ApprovalProgressMapper.selectByInstanceId()` | 查询进度 |

---

## 5. 获取审批实例详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取审批实例详情 |
| 请求方法 | POST |
| 请求路径 | `/api/approval/getInstanceDetail` |
| 接口描述 | 查询审批实例的完整详情 |

### 请求参数

**ApprovalGetInstanceDetailQry**:
```json
{
  "instanceId": 100
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
  "data": {
    "instanceId": 100,
    "businessType": "MATERIAL_ENTRY",
    "businessTitle": "2024年3月宣传素材",
    "applicantId": 10,
    "applicantName": "张三",
    "deptName": "宣传部",
    "workflowId": 1,
    "workflowName": "素材录入审批流程",
    "status": "APPROVED",
    "createTime": "2024-03-01 10:00:00",
    "completeTime": "2024-03-02 15:30:00",
    "approvalProgress": [
      {
        "stageId": 10,
        "stageName": "部门审批",
        "stageOrder": 1,
        "status": "APPROVED",
        "approvers": [
          {
            "approverId": 11,
            "approverName": "王审核",
            "status": "APPROVED",
            "comment": "同意",
            "approveTime": "2024-03-01 14:00:00"
          }
        ]
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `ApprovalController.getInstanceDetail()` | 接收请求 |
| App | `ApprovalService.getInstanceDetail()` | 业务逻辑 |
| Infrastructure | `ApprovalInstanceMapper.selectById()` | 查询实例 |
| Infrastructure | `ApprovalProgressMapper.selectByInstanceId()` | 查询进度 |

---

## 6. 审批通过/拒绝

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 审批通过/拒绝 |
| 请求方法 | POST |
| 请求路径 | `/api/approval/approve` |
| 接口描述 | 处理审批任务，通过或拒绝后自动推进流程 |

### 请求参数

**ApprovalApproveCmd**:
```json
{
  "taskId": 500,
  "comment": "审批意见",
  "passed": true
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| taskId | Long | 是 | 任务ID |
| comment | String | 否 | 审批意见 |
| passed | Boolean | 是 | true=通过，false=拒绝 |

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
| Adapter | `ApprovalController.approve()` | 接收请求 |
| App | `ApprovalService.approve()` | 业务逻辑 |
| App | `WorkflowEngineService.completeTask()` | 完成任务 |
| App | `checkAndMoveToNextStage()` | 检查并推进到下一阶段 |
| App | `updateApprovalStatus()` | 更新申请单状态 |
| Infrastructure | `ApprovalTaskMapper.updateById()` | 更新任务状态 |
| Infrastructure | `ApprovalInstanceMapper.updateById()` | 更新实例状态 |
| Database | `approval_task`, `approval_instance` 表 | MySQL |

### 审批后状态变化

| 场景 | 状态变化 |
|-----|---------|
| 通过 | 任务APPROVED → 检查阶段是否完成 → 创建下一阶段任务或完成流程 |
| 拒绝 | 任务REJECTED → 实例状态REJECTED → 更新申请单状态 |

---

## 7. 退回上一级

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 退回上一级 |
| 请求方法 | POST |
| 请求路径 | `/api/approval/return` |
| 接口描述 | 将任务退回到上一阶段，上一阶段重新生成待办任务 |

### 请求参数

**ApprovalReturnCmd**:
```json
{
  "taskId": 500,
  "comment": "需要补充材料"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| taskId | Long | 是 | 任务ID |
| comment | String | 否 | 退回原因 |

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
| Adapter | `ApprovalController.returnTask()` | 接收请求 |
| App | `ApprovalService.returnTask()` | 业务逻辑 |
| App | `WorkflowEngineService.returnToPreviousStage()` | 退回上一阶段 |
| Infrastructure | `ApprovalTaskMapper.updateById()` | 更新任务状态为RETURNED |
| Infrastructure | `ApprovalTaskMapper.insert()` | 为上一阶段创建新任务 |

---

## 8. 获取流经事项列表

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取流经事项列表 |
| 请求方法 | POST |
| 请求路径 | `/api/approval/getMyFlowItems` |
| 接口描述 | 查询用户发起或审批过的所有工单记录 |

### 请求参数

**ApprovalGetMyFlowItemsQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "businessType": "MATERIAL_ENTRY",
  "status": "APPROVED"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |
| businessType | String | 否 | 业务类型筛选 |
| status | String | 否 | 状态筛选 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "instanceId": 100,
        "businessType": "MATERIAL_ENTRY",
        "businessTitle": "2024年3月宣传素材",
        "applicantId": 10,
        "applicantName": "张三",
        "deptName": "宣传部",
        "workflowName": "素材录入审批流程",
        "status": "APPROVED",
        "createTime": "2024-03-01 10:00:00"
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
| Adapter | `ApprovalController.getMyFlowItems()` | 接收请求 |
| App | `ApprovalService.getMyFlowItems()` | 业务逻辑 |
| Infrastructure | `ApprovalInstanceMapper.selectFlowItems()` | 查询流经事项 |

---

## 附录：任务状态说明

| 状态 | 说明 |
|-----|------|
| PENDING | 待处理 |
| APPROVED | 已通过 |
| REJECTED | 已拒绝 |
| RETURNED | 已退回 |
| CANCELLED | 已取消 |

---

## 附录：实例状态说明

| 状态 | 说明 | 触发条件 |
|-----|------|---------|
| PENDING | 待审批 | 创建实例时 |
| IN_PROGRESS | 进行中 | 第一阶段有任务通过 |
| MAIN_COMPLETED | 主流程完成 | 所有阶段完成，等待子流程 |
| APPROVED | 已通过 | 主流程和所有子流程都通过 |
| REJECTED | 已拒绝 | 任一任务拒绝 |
| CANCELLED | 已取消 | 撤回申请 |

---

*本文档由API接口文档生成方法论自动生成。*
