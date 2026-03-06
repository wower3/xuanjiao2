# API_APPROVER_SELECTION.md - 审批人选择接口文档

> **模块**: 审批人选择 (approver-selection)
> **Controller**: `ApproverSelectionController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/workflow/`
> **创建时间**: 2026-03-04

---

## 目录

1. [获取审批实例进度](#1-获取审批实例进度)
2. [获取第一层可选审批人](#2-获取第一层可选审批人)
3. [选择第一层审批人（含子流程）](#3-选择第一层审批人含子流程)
4. [选择下一层审批人（含子流程）](#4-选择下一层审批人含子流程)
5. [获取子流程第一层可选审批人](#5-获取子流程第一层可选审批人)

---

## 1. 获取审批实例进度

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取审批实例进度 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/getApprovalProgress` |
| 接口描述 | 查询审批实例的详细进度，包含主流程和所有子流程 |

### 请求参数

**WorkflowGetApprovalProgressQry**:
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
  "data": [
    {
      "stageId": 10,
      "stageName": "部门审批",
      "stageOrder": 1,
      "approveType": "AND",
      "status": "APPROVED",
      "approvers": [
        {
          "id": 1001,
          "approverId": 10,
          "approverName": "张三",
          "status": "APPROVED",
          "comment": "同意",
          "approveTime": "2024-03-01 10:00:00"
        },
        {
          "id": 1002,
          "approverId": 11,
          "approverName": "李四",
          "status": "PENDING",
          "comment": null,
          "approveTime": null
        }
      ],
      "subWorkflows": [
        {
          "subInstanceId": 101,
          "workflowId": 5,
          "workflowName": "技术审核子流程",
          "status": "APPROVED",
          "stages": [...]
        }
      ]
    },
    {
      "stageId": 11,
      "stageName": "分公司审批",
      "stageOrder": 2,
      "approveType": "OR",
      "status": "PENDING",
      "approvers": [],
      "subWorkflows": []
    }
  ]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `ApproverSelectionController.getApprovalProgress()` | 接收HTTP请求 |
| App | `ApproverSelectionService.getApprovalProgress()` | 业务逻辑 |
| Infrastructure | `ApprovalProgressMapper.selectByInstanceId()` | 查询进度数据 |
| Database | `approval_progress` 表 | MySQL |

---

## 2. 获取第一层可选审批人

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取第一层可选审批人 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/getFirstStageApprovers` |
| 接口描述 | 获取流程第一阶段的审批人配置，用于提交申请时选择审批人 |

### 请求参数

**WorkflowGetFirstStageApproversQry**:
```json
{
  "workflowId": 1,
  "applicantId": 10,
  "keyword": "张"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| workflowId | Long | 是 | 工作流ID |
| applicantId | Long | 是 | 申请人ID |
| keyword | String | 否 | 搜索关键词（用户名或姓名） |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stageId": 10,
    "stageName": "部门审批",
    "approveType": "AND",
    "approvers": [
      {
        "approverType": "USER",
        "approverId": 10,
        "approverName": "张三",
        "deptId": 5,
        "deptName": "技术部"
      },
      {
        "approverType": "ROLE",
        "approverId": 3,
        "approverName": "审核员",
        "roleId": 3,
        "roleName": "审核员",
        "subWorkflowId": 5,
        "subWorkflowName": "技术审核子流程"
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `ApproverSelectionController.getFirstStageApprovers()` | 接收请求 |
| App | `ApproverSelectionService.getFirstStageApprovers()` | 业务逻辑 |
| Infrastructure | `WorkflowStageMapper.selectByWorkflowId()` | 查询第一阶段 |
| Infrastructure | `StageApproverMapper.selectByStageId()` | 查询审批人 |
| App | `按部门分组审批人` | 构建返回结构 |

---

## 3. 选择第一层审批人（含子流程）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 选择第一层审批人（含子流程） |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/select-first-stage-approvers-with-subworkflows` |
| 接口描述 | 为审批实例选择第一阶段审批人，可同时指定子流程审批人 |

### 请求参数

**SelectFirstStageApproversWithSubWorkflowsRequest**:
```json
{
  "instanceId": 100,
  "approverIds": [10, 20],
  "subWorkflowApproverIds": {
    "5": [30, 31],
    "6": [32]
  }
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| instanceId | Long | 是 | 审批实例ID |
| approverIds | List\<Long\> | 是 | 主流程审批人ID列表 |
| subWorkflowApproverIds | Map | 否 | 子流程ID -> 审批人ID列表 |

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
| Adapter | `ApproverSelectionController.selectFirstStageApproversWithSubWorkflows()` | 接收请求 |
| App | `ApproverSelectionService.selectFirstStageApproversWithSubWorkflows()` | 业务逻辑 |
| App | `创建主流程审批任务` | 为选择的审批人创建任务 |
| App | `创建子流程实例` | 为子流程创建审批实例 |
| Infrastructure | `ApprovalTaskMapper.batchInsert()` | 批量插入任务 |

---

## 4. 选择下一层审批人（含子流程）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 选择下一层审批人（含子流程） |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/select-next-stage-approvers-with-subworkflows` |
| 接口描述 | 在当前任务完成后选择下一阶段审批人，可同时指定子流程审批人 |

### 请求参数

**SelectNextStageApproversWithSubWorkflowsRequest**:
```json
{
  "taskId": 500,
  "approverIds": [15, 16],
  "subWorkflowApproverIds": {
    "7": [33]
  }
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| taskId | Long | 是 | 当前任务ID |
| approverIds | List\<Long\> | 是 | 下一阶段审批人ID列表 |
| subWorkflowApproverIds | Map | 否 | 子流程ID -> 审批人ID列表 |

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
| Adapter | `ApproverSelectionController.selectNextStageApproversWithSubWorkflows()` | 接收请求 |
| App | `ApproverSelectionService.selectNextStageApprovers()` | 业务逻辑 |
| App | `WorkflowEngineService.createNextStageTasks()` | 创建下一阶段任务 |
| App | `WorkflowEngineService.startSubProcessesForStage()` | 启动子流程 |
| Infrastructure | `ApprovalTaskMapper.batchInsert()` | 批量插入任务 |

---

## 5. 获取子流程第一层可选审批人

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取子流程第一层可选审批人 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/getSubWorkflowFirstStageApprovers` |
| 接口描述 | 获取子流程第一阶段的审批人配置 |

### 请求参数

**WorkflowGetSubWorkflowFirstStageApproversQry**:
```json
{
  "subWorkflowId": 5,
  "applicantId": 10,
  "keyword": "李"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| subWorkflowId | Long | 是 | 子流程ID |
| applicantId | Long | 是 | 申请人ID |
| keyword | String | 否 | 搜索关键词 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stageId": 25,
    "stageName": "技术审核",
    "approveType": "OR",
    "approvers": [
      {
        "approverType": "USER",
        "approverId": 30,
        "approverName": "李技术",
        "deptId": 6,
        "deptName": "技术部"
      },
      {
        "approverType": "USER",
        "approverId": 31,
        "approverName": "王工程师",
        "deptId": 6,
        "deptName": "技术部"
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `ApproverSelectionController.getSubWorkflowFirstStageApprovers()` | 接收请求 |
| App | `ApproverSelectionService.getSubWorkflowFirstStageApprovers()` | 业务逻辑 |
| Infrastructure | `WorkflowStageMapper.selectByWorkflowId()` | 查询子流程阶段 |
| Infrastructure | `StageApproverMapper.selectByStageId()` | 查询审批人 |

---

## 附录：审批人类型说明

| approverType | 说明 | approverId指向 | 示例 |
|--------------|------|--------------|------|
| USER | 用户审批人 | sys_user.id | 具体某个用户 |
| ROLE | 角色审批人 | sys_role.id | 角色下所有用户都是审批人 |
| DEPT | 部门审批人 | sys_dept.id | 部门下所有用户都是审批人 |

### 审批类型说明

| approveType | 说明 | 通过条件 |
|-------------|------|---------|
| AND | 会签 | 所有审批人都必须通过 |
| OR | 或签 | 任一审批人通过即可 |

### 子流程说明

```
主流程阶段配置:
├── 审批人1: 张三 (USER)
├── 审批人2: 技术审核角色 (ROLE, subWorkflowId=5)
│
└── 当选择"技术审核角色"作为审批人时，需要同时指定其子流程的第一层审批人:
    ├── 子流程5第一层: 李技术、王工程师 (OR签)
    └── 子流程5后续阶段...

子流程执行逻辑:
- 子流程在主流程该审批人审批通过时触发
- 子流程独立运行，不阻塞主流程
- 主流程和子流程都必须完成才算整体完成
```

---

*本文档由API接口文档生成方法论自动生成。*
