# API_WORKFLOW.md - 工作流管理接口文档

> **模块**: 工作流管理 (workflow)
> **Controller**: `WorkflowController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/workflow/`
> **创建时间**: 2026-03-04

---

## 目录

1. [获取工作流列表](#1-获取工作流列表)
2. [获取工作流详情](#2-获取工作流详情)
3. [保存工作流](#3-保存工作流)
4. [更新工作流](#4-更新工作流)
5. [删除工作流](#5-删除工作流)
6. [更新工作流状态](#6-更新工作流状态)
7. [绑定角色](#7-绑定角色)
8. [解绑角色](#8-解绑角色)
9. [复制工作流](#9-复制工作流)

---

## 1. 获取工作流列表

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取工作流列表 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/getList` |
| 接口描述 | 查询系统中所有工作流的列表，按ID倒序排列 |

### 请求参数

**WorkflowGetListQry**:
```json
{}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| - | - | - | 当前无过滤参数 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "素材录入审批流程",
      "description": "用于素材录入的三级审批流程",
      "version": 1,
      "status": 1,
      "boundRoleId": 3,
      "roleName": "分消保管理岗",
      "workflowType": "ASSET_UPLOAD"
    },
    {
      "id": 2,
      "name": "素材使用审批流程",
      "description": "用于素材使用的两级审批流程",
      "version": 1,
      "status": 0,
      "boundRoleId": null,
      "roleName": null,
      "workflowType": null
    }
  ]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `WorkflowController.list()` | 接收HTTP请求 |
| App | `WorkflowService.list()` | 业务逻辑 |
| App | `WorkflowServiceImpl.list()` | 实现逻辑 |
| Infrastructure | `WorkflowMapper.selectListWithRoleName()` | JOIN查询（含角色名） |
| Database | `workflow` 表, `sys_role` 表 | MySQL |

---

## 2. 获取工作流详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取工作流详情 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/getDetail` |
| 接口描述 | 查询工作流的完整配置，包含阶段和审批人信息 |

### 请求参数

**WorkflowGetDetailQry**:
```json
{
  "id": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 工作流ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "素材录入审批流程",
    "description": "用于素材录入的三级审批流程",
    "version": 1,
    "status": 1,
    "boundRoleId": 3,
    "roleName": "分消保管理岗",
    "workflowType": "ASSET_UPLOAD",
    "stages": [
      {
        "id": 10,
        "name": "部门审批",
        "stageOrder": 1,
        "approveType": "AND",
        "approvers": [
          {
            "id": 100,
            "approverType": "USER",
            "approverId": 20,
            "approverName": "[用户] 张经理",
            "checkSecondaryDept": 0,
            "subWorkflowId": null,
            "subWorkflowName": null
          },
          {
            "id": 101,
            "approverType": "ROLE",
            "approverId": 5,
            "approverName": "[角色] 审核员",
            "checkSecondaryDept": 1,
            "subWorkflowId": 5,
            "subWorkflowName": "技术审核子流程"
          }
        ]
      },
      {
        "id": 11,
        "name": "分公司审批",
        "stageOrder": 2,
        "approveType": "OR",
        "approvers": [...]
      }
    ]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `WorkflowController.getById()` | 接收请求 |
| App | `WorkflowService.getById()` | 业务逻辑 |
| App | `WorkflowServiceImpl.getById()` | 查询流程、阶段、审批人 |
| Infrastructure | `WorkflowMapper.selectById()` | 查询工作流 |
| Infrastructure | `WorkflowStageMapper.selectList()` | 查询阶段 |
| Infrastructure | `StageApproverMapper.selectWithDetails()` | JOIN查询审批人及详情 |

### 审批人类型

| approverType | 说明 | approverId指向 |
|--------------|------|--------------|
| USER | 用户审批人 | sys_user.id |
| ROLE | 角色审批人（角色下所有用户） | sys_role.id |
| DEPT | 部门审批人（部门下所有用户） | sys_dept.id |

### 审批类型

| approveType | 说明 |
|-------------|------|
| AND | 会签（所有审批人必须通过） |
| OR | 或签（任一审批人通过即可） |

---

## 3. 保存工作流

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 保存工作流 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/create` |
| 接口描述 | 创建新的工作流定义，包含阶段和审批人配置 |

### 请求参数

**WorkflowCreateCmd**:
```json
{
  "name": "新素材审批流程",
  "description": "新素材的两级审批流程",
  "version": 1,
  "status": 0,
  "stages": [
    {
      "name": "初审",
      "approveType": "AND",
      "approvers": [
        {
          "approverType": "USER",
          "approverId": 10,
          "checkSecondaryDept": 0
        },
        {
          "approverType": "ROLE",
          "approverId": 3,
          "checkSecondaryDept": 1,
          "subWorkflowId": 5
        }
      ]
    },
    {
      "name": "终审",
      "approveType": "OR",
      "approvers": [
        {
          "approverType": "USER",
          "approverId": 20
        }
      ]
    }
  ]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| name | String | 是 | 工作流名称 |
| description | String | 否 | 工作流描述 |
| version | Integer | 否 | 版本号 |
| status | Integer | 否 | 状态（0=禁用，1=启用） |
| stages | Array | 是 | 阶段配置列表 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3,
    "name": "新素材审批流程",
    ...
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `WorkflowController.save()` | 接收请求，转换DTO |
| App | `WorkflowService.save()` | 业务逻辑 |
| App | `saveStages()` | 保存阶段和审批人 |
| Infrastructure | `WorkflowMapper.insert()` | 插入工作流 |
| Infrastructure | `WorkflowStageMapper.insert()` | 插入阶段 |
| Infrastructure | `StageApproverMapper.insert()` | 插入审批人 |

---

## 4. 更新工作流

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新工作流 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/update` |
| 接口描述 | 修改工作流定义，会替换所有阶段和审批人配置 |

### 请求参数

**WorkflowUpdateCmd**:
```json
{
  "id": 3,
  "name": "新素材审批流程（已修改）",
  "description": "修改后的描述",
  "stages": [...]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 工作流ID |
| name | String | 否 | 工作流名称 |
| description | String | 否 | 工作流描述 |
| stages | Array | 否 | 阶段配置列表（会完全替换） |

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
| Adapter | `WorkflowController.update()` | 接收请求 |
| App | `WorkflowService.update()` | 业务逻辑 |
| App | `删除旧阶段和审批人` | 删除旧配置 |
| App | `saveStages()` | 保存新配置 |

---

## 5. 删除工作流

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除工作流 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/delete` |
| 接口描述 | 删除工作流定义及其所有阶段和审批人配置 |

### 请求参数

**WorkflowDeleteCmd**:
```json
{
  "id": 3
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 工作流ID |

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
| Adapter | `WorkflowController.delete()` | 接收请求 |
| App | `WorkflowService.delete()` | 业务逻辑 |
| App | `删除审批人` | 遍历删除各阶段审批人 |
| App | `删除阶段` | 删除所有阶段 |
| Infrastructure | `WorkflowMapper.deleteById()` | 删除工作流 |

---

## 6. 更新工作流状态

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新工作流状态 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/updateStatus` |
| 接口描述 | 启用或停用工作流，启用时会检查冲突 |

### 请求参数

**WorkflowUpdateStatusCmd**:
```json
{
  "id": 3,
  "status": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 工作流ID |
| status | Integer | 是 | 状态（0=停用，1=启用） |

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
  "message": "启用失败：角色【分消保管理岗】的【素材录入】类型已有启用的流程：《素材录入审批流程v1》"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `WorkflowController.updateStatus()` | 接收请求 |
| App | `WorkflowService.updateStatus()` | 业务逻辑 |
| App | `checkConflictingWorkflows()` | 检查同角色+流程类型冲突 |
| Infrastructure | `WorkflowMapper.updateById()` | 更新状态 |

### 冲突检查规则

```
启用工作流时，检查是否存在：
- 相同的 boundRoleId
- 相同的 workflowType
- status = 1
- 排除当前工作流

如果存在冲突的工作流，抛出异常并提示
```

---

## 7. 绑定角色

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 绑定角色 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/bindRole` |
| 接口描述 | 将工作流绑定到角色和流程类型，同时启用工作流 |

### 请求参数

**WorkflowBindRoleCmd**:
```json
{
  "id": 3,
  "roleId": 3,
  "workflowType": "ASSET_UPLOAD"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 工作流ID |
| roleId | Long | 是 | 角色ID |
| workflowType | String | 是 | 流程类型 |

### 流程类型

| workflowType | 说明 |
|-------------|------|
| ASSET_UPLOAD | 素材录入 |
| ASSET_USAGE | 素材使用 |
| ASSET_DELETION | 素材删除 |

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
| Adapter | `WorkflowController.bindRole()` | 接收请求 |
| App | `WorkflowService.bindRole()` | 业务逻辑 |
| App | `checkConflictingWorkflows()` | 检查冲突 |
| Infrastructure | `WorkflowMapper.updateById()` | 更新绑定信息并启用 |

---

## 8. 解绑角色

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 解绑角色 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/unbindRole` |
| 接口描述 | 解除工作流与角色的绑定关系 |

### 请求参数

**WorkflowUnbindRoleCmd**:
```json
{
  "id": 3
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 工作流ID |

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
| Adapter | `WorkflowController.unbindRole()` | 接收请求 |
| App | `WorkflowService.unbindRole()` | 业务逻辑 |
| Infrastructure | `WorkflowMapper.updateById()` | 清空绑定信息 |

---

## 9. 复制工作流

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 复制工作流 |
| 请求方法 | POST |
| 请求路径 | `/api/workflow/{id}/copy` |
| 接口描述 | 复制工作流创建新流程，包含所有阶段和审批人配置 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| id | Long | 是 | 原工作流ID | 路径参数 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 4,
    "name": "素材录入审批流程 (副本)",
    "status": 0,
    "boundRoleId": null,
    "workflowType": null,
    "stages": [...]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `WorkflowController.copy()` | 接收请求 |
| App | `WorkflowService.copy()` | 业务逻辑 |
| App | `复制工作流基本信息` | 名称添加"(副本)" |
| App | `复制所有阶段` | 保持stageOrder |
| App | `复制所有审批人` | 包含子流程配置 |
| Infrastructure | `WorkflowMapper.insert()` | 插入新工作流 |

### 复制规则

| 内容 | 处理方式 |
|-----|---------|
| 工作流名称 | 添加" (副本)"后缀 |
| status | 默认为0（禁用） |
| boundRoleId | 不复制（设为null） |
| workflowType | 不复制（设为null） |
| 阶段配置 | 完全复制 |
| 审批人配置 | 完全复制（含子流程） |

---

## 附录：数据库表结构

### workflow (工作流表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 工作流名称 |
| description | TEXT | 工作流描述 |
| version | INT | 版本号 |
| status | INT | 状态（0=禁用，1=启用） |
| bound_role_id | BIGINT | 绑定角色ID |
| workflow_type | VARCHAR | 流程类型 |
| deleted | INT | 逻辑删除标记 |

### workflow_stage (工作流阶段表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| workflow_id | BIGINT | 工作流ID |
| name | VARCHAR | 阶段名称 |
| stage_order | INT | 阶段顺序 |
| approve_type | VARCHAR | 审批类型（AND/OR） |
| sub_workflow_id | BIGINT | 子流程ID（阶段级，已弃用） |

### stage_approver (阶段审批人表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| stage_id | BIGINT | 阶段ID |
| approver_type | VARCHAR | 审批人类型（USER/ROLE/DEPT） |
| approver_id | BIGINT | 审批人ID（用户/角色/部门ID） |
| check_secondary_dept | INT | 是否检查二级部门（0/1） |
| sub_workflow_id | BIGINT | 子流程ID（审批人级） |

---

## 附录：工作流结构示例

```
工作流: 素材录入审批流程
├── 阶段1: 部门审批 (stageOrder=1, approveType=AND)
│   ├── 审批人1: 张三 (USER, id=10)
│   └── 审批人2: 技术审核子流程 (ROLE, id=5, subWorkflowId=5)
├── 阶段2: 分公司审批 (stageOrder=2, approveType=OR)
│   ├── 审批人1: 李四 (USER, id=20)
│   └── 审批人2: 王五 (USER, id=21)
└── 阶段3: 总公司审批 (stageOrder=3, approveType=AND)
    └── 审批人1: 赵六 (USER, id=30)
```

---

*本文档由API接口文档生成方法论自动生成。*
