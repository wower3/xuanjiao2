# 审批流程重构总结

## 概述

本文档总结了宣传教育平台审批流程系统的重构，主要涉及子流程功能的实现和审批人选择机制的优化。

## 核心架构

### 审批流程模型

**"层间串行 + 层内并行"** 架构：
- **层间（串行）**：各阶段按顺序执行，第N层完成后才能进入第N+1层
- **层内（并行）**：同阶段所有审批人同时收到任务
- **会签**：所有审批人都必须通过
- **或签**：任一审批人通过即可

### 子流程架构

子流程已从阶段级别重构为审批人级别：

| 维度 | 重构前 | 重构后 |
|------|--------|--------|
| 配置位置 | 阶段级别 | 审批人级别 |
| 触发方式 | 阶段自动触发 | 审批人选择后触发 |
| 运行方式 | 阻塞主流程 | 独立运行，不阻塞主流程 |
| 审批人选择 | 由系统自动选择 | 由上层审批人选择 |

## 数据库结构

### 核心表结构

#### 1. workflow（审批流程表）
```sql
id                  -- 流程ID
name                -- 流程名称
description         -- 描述
version             -- 版本号
status              -- 状态（0-禁用，1-启用）
bound_role_id       -- 绑定的角色ID
role_name           -- 绑定的角色名称
workflow_type       -- 流程类型（ASSET_UPLOAD-素材录入，ASSET_USAGE-素材使用）
```

#### 2. workflow_stage（流程阶段表）
```sql
id                  -- 阶段ID
workflow_id         -- 流程ID
name                -- 阶段名称
stage_order         -- 阶段顺序
approve_type        -- 审批类型（OR-或签，AND-会签）
```

#### 3. stage_approver（阶段审批人配置表）
```sql
id                  -- 配置ID
stage_id            -- 阶段ID
approver_type       -- 审批人类型（USER-用户，ROLE-角色，DEPT-部门）
approver_id         -- 审批人/角色/部门ID
approver_name       -- 审批人名称（前端显示）
check_secondary_dept -- 是否校验二级部门（0-否，1-是）
sub_workflow_id     -- 关联的子流程ID（如果该审批人是子流程）
sub_workflow_name   -- 子流程名称（前端显示用）
```

#### 4. approval_instance（审批实例表）
```sql
id                      -- 实例ID
workflow_id             -- 流程ID
business_type           -- 业务类型（MATERIAL_ENTRY-素材录入，ASSET_USAGE-素材使用）
business_id             -- 业务数据ID
applicant_id            -- 申请人ID
status                  -- 实例状态（PENDING-审批中，APPROVED-已通过，REJECTED-已驳回，MAIN_COMPLETED-主流程完成等待子流程）
current_stage_id        -- 当前阶段ID
root_instance_id        -- 根实例ID（主流程为null）
parent_instance_id      -- 父实例ID（子流程的父实例ID）
parent_task_id          -- 父任务ID（触发该子流程的任务ID）
sub_workflow_approver_ids -- 子流程第一层审批人IDs（JSON格式：{subWorkflowId: [approverId1, approverId2]}）
create_time             -- 创建时间
update_time             -- 更新时间
```

#### 5. approval_task（审批任务表）
```sql
id                      -- 任务ID
instance_id             -- 实例ID
stage_id                -- 阶段ID
approver_id             -- 审批人ID
status                  -- 任务状态（PENDING-待审批，APPROVED-已通过，REJECTED-已驳回）
is_first_approver        -- 是否第一个审批人（1-是，0-否）
next_stage_approver_ids  -- 下一层审批人IDs（JSON数组）
sub_workflow_approver_ids -- 子流程第一层审批人IDs（JSON格式）
selected_by_user_id      -- 选择下一层审批人的用户ID
comment                 -- 审批意见
approve_time            -- 审批时间
create_time             -- 创建时间
update_time             -- 更新时间
```

#### 6. approval_progress（审批进度表）
```sql
id                  -- 进度ID
instance_id          -- 实例ID
stage_id             -- 阶段ID
stage_name           -- 阶段名称
stage_order          -- 阶段顺序
status               -- 状态（PENDING-待审批，APPROVED-已通过，REJECTED-已驳回）
is_sub_workflow      -- 是否子流程（1-是，0-否）
parent_instance_id   -- 父实例ID
parent_task_id       -- 父任务ID
approvers             -- 审批人信息（JSON格式）
approve_time         -- 审批时间
```

## API 端点

### 审批相关

| 端点 | 方法 | 描述 |
|------|------|------|
| `/approval/tasks` | GET | 获取待我审批的任务列表 |
| `/approval/applied` | GET | 获取我发起的申请列表 |
| `/approval/tasks/{id}/approve` | POST | 审批任务（通过/驳回） |
| `/approval/tasks/{id}/detail` | GET | 获取任务详情 |

### 审批人选择相关

| 端点 | 方法 | 描述 |
|------|------|------|
| `/workflow/list` | GET | 获取所有审批流程 |
| `/workflow/{id}` | GET | 根据ID获取流程详情 |
| `/workflow/by-role` | GET | 根据角色获取绑定的审批流程 |
| `/workflow/first-stage-approvers` | GET | 获取第一层可选审批人 |
| `/workflow/next-stage-approvers` | GET | 获取下一层可选审批人 |
| `/workflow/sub-workflow-approvers` | GET | 获取子流程第一层可选审批人 |
| `/workflow/select-first-stage-approvers` | POST | 选择第一层审批人（发起人发起时） |
| `/workflow/select-first-stage-approvers-with-subworkflows` | POST | 选择第一层审批人（包括子流程） |
| `/workflow/select-next-stage-approvers` | POST | 选择下一层审批人（审批人审批时） |
| `/workflow/select-next-stage-approvers-with-subworkflows` | POST | 选择下一层审批人（包括子流程） |
| `/workflow/progress/{instanceId}` | GET | 获取审批实例进度 |

## 业务规则

### 1. 发起流程

**发起人发起流程时：**
1. 根据当前用户角色自动匹配绑定的审批流程
2. 选择主流程第一层审批人
3. 选择第一层中包含的子流程的第一层审批人
4. 提交后创建审批实例和第一层任务

**规则：**
- 如果第一层只有子流程审批人（无主流程审批人），发起人无需选择
- 子流程第一层审批人必须选择，否则无法提交

### 2. 审批流程

**或签（OR）场景：**
- 第一个点击"通过"的审批人视为"第一个审批人"
- 第一个审批人需要选择：
  - 下一层主流程审批人（如果有下一层）
  - 当前层包含的子流程的第一层审批人（如果有子流程）
- 第一个审批人通过后，其他同层审批人的任务自动取消
- 其他同层审批人只能查看第一个审批人的选择（只读）

**会签（AND）场景：**
- 创建任务时标记的第一个审批人（`is_first_approver=1`）负责选择
- 第一个审批人需要选择：
  - 下一层主流程审批人（如果有下一层）
  - 当前层包含的子流程的第一层审批人（如果有子流程）
- 所有审批人都通过后，才会触发下一层
- 其他同层审批人只能查看第一个审批人的选择（只读）

### 3. 子流程触发

**触发时机：**
- 当前阶段完成时（无论或签还是会签）
- 由第一个审批人的选择决定子流程的第一层审批人

**触发规则：**
1. 检查当前阶段配置中哪些审批人是子流程
2. 为每个子流程创建独立的审批实例
3. 如果已选择子流程第一层审批人，直接创建任务
4. 子流程独立运行，不阻塞主流程

### 4. 完成条件

**整个审批完成需同时满足：**
1. 主流程所有阶段已完成
2. 所有子流程都已结束（APPROVED 或 REJECTED）

**主流程完成时的状态：**
- 如果所有子流程都已完成：`status = APPROVED`，调用业务完成处理
- 如果还有子流程未完成：`status = MAIN_COMPLETED`，等待子流程

**子流程完成时的检查：**
- 子流程完成后，检查父实例状态
- 如果主流程已完成且所有子流程都已完成，将父实例标记为 APPROVED

## 前端组件

### 1. material-entry.vue（素材录入页面）

**功能：**
- 发起人选择主流程第一层审批人
- 选择子流程第一层审批人
- 搜索审批人（支持用户名和姓名模糊查询）

**关键状态：**
```typescript
firstStageApprovers: []      // 主流程第一层审批人列表
selectedApproverIds: []      // 已选择的主流程审批人ID
subWorkflows: []             // 子流程列表
  - id: 子流程ID
  - name: 子流程名称
  - approvers: 审批人列表
  - selectedIds: 已选择的审批人ID
  - keyword: 搜索关键词
  - loading: 加载状态
```

### 2. approval/index.vue（审批页面）

**功能：**
- 查看待我审批和我发起的列表
- 查看审批详情和进度
- 审批操作（通过/驳回）
- 选择下一层审批人和子流程审批人（第一个审批人）
- 查看其他同层审批人的选择（只读）

**关键状态：**
```typescript
taskDetail: {
  canSelectNextApprovers: boolean,      // 是否可以选择下一层审批人
  nextStageId: number,                   // 下一阶段ID
  nextStageName: string,                 // 下一阶段名称
  hasSubWorkflows: boolean,              // 是否有子流程
  subWorkflows: [],                      // 子流程列表
  selectedNextApprovers: [],             // 已选择的下一层审批人（只读显示）
  selectedSubWorkflowApprovers: {},     // 已选择的子流程审批人（只读显示）
  selectedByUserId: number               // 选择者用户ID
}
```

## 后端服务

### 1. WorkflowEngineServiceImpl

**核心方法：**

| 方法 | 描述 |
|------|------|
| `createInstance(workflowId, businessType, businessId, applicantId)` | 创建审批实例 |
| `completeTask(taskId, userId, approved, comment)` | 完成任务（审批通过/驳回） |
| `selectFirstStageApprovers(instanceId, approverIds, subWorkflowApproverIds)` | 发起人选择第一层审批人 |
| `checkAndMoveToNextStage(instanceId, currentStageId)` | 检查阶段完成并移动到下一阶段 |
| `moveToNextStage(instanceId, currentStageId, nextStageApproverIds)` | 移动到下一阶段 |
| `startSubProcessesForStage(parentInstanceId, parentStageId, parentTaskId, subWorkflowApproverIds)` | 启动子流程 |
| `areAllSubWorkflowsComplete(parentInstanceId)` | 检查所有子流程是否完成 |
| `checkParentCompletion(parentInstanceId)` | 检查父实例完成条件 |

### 2. ApproverSelectionServiceImpl

**核心方法：**

| 方法 | 描述 |
|------|------|
| `selectNextStageApprovers(taskId, approverIds, subWorkflowApproverIds)` | 选择下一层审批人（包括子流程） |
| `selectFirstStageApproversWithSubWorkflows(instanceId, approverIds, subWorkflowApproverIds)` | 选择第一层审批人（包括子流程） |
| `getFirstStageApprovers(workflowId, applicantId, keyword)` | 获取第一层可选审批人 |
| `getNextStageApprovers(stageId, instanceId, applicantId, keyword)` | 获取下一层可选审批人 |
| `getSubWorkflowFirstStageApprovers(subWorkflowId, applicantId, keyword)` | 获取子流程第一层可选审批人 |
| `getApprovalProgress(instanceId)` | 获取审批实例进度 |
| `getTaskDetail(taskId)` | 获取任务详情 |

## 审批流程流转规则

### 场景1：简单审批流程（无子流程）

```
发起人发起 → 选择第一层审批人 → 第一层审批（或签/会签）
  → 选择第二层审批人 → 第二层审批 → ... → 完成
```

### 场景2：带子流程的审批流程

```
发起人发起 → 选择第一层审批人 + 子流程第一层审批人
  ↓
第一层审批通过（或签第一个，会签所有）
  ├─ 主流程：选择第二层审批人 → 进入第二层
  └─ 子流程：独立运行（不阻塞主流程）
       ↓ 子流程完成
  ↓
第二层审批 → ... → 主流程完成
  ↓
等待所有子流程完成 → 整个审批完成
```

### 场景3：或签流程

```
第一层：审批人A、审批人B
  ↓
审批人A第一个点击"通过"
  ├─ 选择第二层审批人
  ├─ 选择子流程第一层审批人（如有）
  └─ 审批人B的任务自动取消
  ↓
进入第二层
```

### 场景4：会签流程

```
第一层：审批人A（第一个）、审批人B
  ↓
审批人A点击"通过"
  ├─ 选择第二层审批人
  ├─ 选择子流程第一层审批人（如有）
  └─ 等待审批人B
  ↓
审批人B点击"通过"
  ↓
进入第二层
```

## 数据库迁移脚本

| 脚本 | 描述 |
|------|------|
| `init_17_workflow_refactor.sql` | 初始重构脚本 |
| `init_18_add_sub_workflow_approver_ids.sql` | 为 approval_task 表添加 sub_workflow_approver_ids 字段 |
| `init_19_add_sub_workflow_approver_ids_to_instance.sql` | 为 approval_instance 表添加 sub_workflow_approver_ids 字段 |

## 状态流转

### 审批实例状态

```
PENDING（审批中）
  ↓
MAIN_COMPLETED（主流程完成，等待子流程）- 如果有子流程
  ↓
APPROVED（已完成）- 主流程和所有子流程都完成

或

REJECTED（已驳回）- 任一审批人驳回
```

### 审批任务状态

```
PENDING（待审批）
  ↓
APPROVED（已通过）- 审批人点击"通过"

或

REJECTED（已驳回）- 审批人点击"驳回"
```

## 关键业务逻辑

### 1. 第一个审批人识别

**或签（OR）：**
- 第一个点击"通过"的审批人自动成为第一个审批人
- 其他同层审批人的任务被取消

**会签（AND）：**
- 创建任务时，第一个创建的任务标记为 `is_first_approver=1`
- 其他任务标记为 `is_first_approver=0`
- 只有第一个审批人可以选择下一层审批人和子流程审批人

### 2. 审批人选择权限

| 角色 | 可选择的内容 | 说明 |
|------|------------|------|
| 发起人 | 第一层主流程审批人 + 第一层子流程审批人 | 发起时一次性选择 |
| 第一个审批人 | 下一层主流程审批人 + 当前层子流程审批人 | 审批通过时选择 |
| 其他同层审批人 | 只读查看第一个审批人的选择 | 不能修改 |

### 3. 子流程审批人选择来源

**优先级：**
1. 第一个审批人的选择（存储在 `approval_task.sub_workflow_approver_ids`）
2. 发起人的选择（存储在 `approval_instance.sub_workflow_approver_ids`）

**解析顺序：**
```java
// 优先从任务获取
if (firstCompletedTask.getSubWorkflowApproverIds() != null) {
    subWorkflowApproverIds = parse(task.getSubWorkflowApproverIds());
}
// 如果任务中没有，从实例获取（发起人发起时选择的）
if (subWorkflowApproverIds == null) {
    subWorkflowApproverIds = parse(instance.getSubWorkflowApproverIds());
}
```

## 技术栈

### 后端
- Java 8
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- COLA 4.3.2（Clean Object-Oriented and Layered Architecture）
- SLF4J 日志

### 前端
- Vue 3.4
- TypeScript
- Vite 5.0
- Element Plus 2.4.4
- Pinia

## 日志记录

所有关键操作都有详细日志记录：

```java
// 审批操作
logger.info("开始审批: taskId={}, userId={}, passed={}", taskId, userId, passed);

// 阶段流转
logger.info("检查并移动到下一阶段: instanceId={}, stageId={}", instanceId, stageId);
logger.info("阶段已完成，开始处理下一阶段: instanceId={}, stageId={}", instanceId, stageId);

// 子流程操作
logger.info("启动子流程: instanceId={}, stageId={}", instanceId, stageId);
logger.info("成功启动子流程: subWorkflowId={}, subInstanceId={}", subWorkflowId, subInstanceId);

// 完成检查
logger.info("主流程检查子流程完成状态: allSubWorkflowsComplete={}", allSubWorkflowsComplete);
logger.info("整个审批已完成: instanceId={}", instanceId);
```

## 错误处理

### 子流程不存在或配置错误

```java
// 之前：直接抛出异常，中断整个流程
if (subWorkflow == null) {
    throw new RuntimeException("子流程不存在: " + subWorkflowId);
}

// 现在：记录警告，跳过该子流程，继续处理其他流程
if (subWorkflow == null) {
    logger.warn("子流程不存在，跳过: subWorkflowId={}", subWorkflowId);
    continue;
}
```

### 事务管理

```java
@Transactional(rollbackFor = Exception.class)
public void completeTask(Long taskId, Long userId, boolean approved, String comment) {
    // 审批逻辑
    // 异常会自动回滚
}
```

## 已完成的功能清单

### 数据库层
- ✅ 添加 `sub_workflow_approver_ids` 字段到 `approval_task` 表
- ✅ 添加 `sub_workflow_approver_ids` 字段到 `approval_instance` 表
- ✅ 更新所有 DO/DTO 类

### 后端服务层
- ✅ 支持发起人发起时选择子流程第一层审批人
- ✅ 支持审批人审批时选择子流程第一层审批人
- ✅ 子流程触发后自动创建第一层任务
- ✅ 同层其他审批人反显第一个审批人的选择
- ✅ 完成条件检查：主流程+所有子流程都结束
- ✅ 添加详细日志记录和错误处理

### 前端层
- ✅ 素材录入页面支持子流程审批人选择
- ✅ 审批页面支持子流程审批人选择
- ✅ 审批页面显示已选择的子流程审批人（只读）
- ✅ 新增 API 函数支持子流程操作

### 控制器层
- ✅ 新增 `/select-first-stage-approvers-with-subworkflows` 端点
- ✅ 新增 `/select-next-stage-approvers-with-subworkflows` 端点
- ✅ 任务详情返回子流程审批人选择信息

## 测试场景

### 场景1：简单或签流程

1. 发起人发起流程
2. 选择第一层审批人（2人）
3. 第一人审批通过，选择第二层审批人
4. 第二层审批人收到任务
5. 审批通过，流程完成

### 场景2：带子流程的会签流程

1. 发起人发起流程
2. 选择第一层审批人（2人）+ 子流程第一层审批人（1人）
3. 第一人审批通过，选择第二层审批人
4. 第二人审批通过
5. 主流程进入第二层，子流程独立运行
6. 主流程完成，等待子流程
7. 子流程完成
8. 整个审批完成

### 场景3：多层嵌套子流程

1. 主流程第一层：审批人A + 子流程1
2. 子流程1第一层：审批人B + 子流程2
3. 审批人A选择：第二层审批人 + 子流程1第一层审批人
4. 子流程1独立运行，审批人B选择：子流程2第一层审批人
5. 主流程、子流程1、子流程2独立并行运行
6. 所有流程完成后，整个审批完成

## 常见问题

### Q1：子流程和主流程的关系？

A：子流程是独立的审批实例，有自己的阶段和审批人。子流程由上层审批人触发，但触发后独立运行，不阻塞主流程。只有当主流程和所有子流程都完成后，整个审批才算完成。

### Q2：第一个审批人如何确定？

A：
- **或签**：第一个点击"通过"的审批人
- **会签**：创建任务时标记为 `is_first_approver=1` 的审批人

### Q3：其他同层审批人能看到什么？

A：其他同层审批人可以看到第一个审批人的选择（下一层审批人、子流程审批人），但只能查看，不能修改。

### Q4：子流程审批人选择存储在哪里？

A：存储在两个地方：
1. `approval_task.sub_workflow_approver_ids` - 第一个审批人的选择
2. `approval_instance.sub_workflow_approver_ids` - 发起人发起时的选择

优先使用任务中的选择，如果没有则使用实例中的选择。

### Q5：如果子流程配置错误怎么办？

A：系统会记录警告日志并跳过该子流程，继续处理其他流程。不会因为单个子流程配置错误而中断整个审批流程。

### Q6：或签和会签对子流程和审批人的影响？

A：不影响。无论或签还是会签，子流程的触发时机都是当前阶段完成时。审批人的选择逻辑都是：第一个审批人（或签为第一个通过的人，会签为标记的人）负责选择下一层审批人和子流程审批人。

### Q7：发起人和审批人选择的子流程审批人有何区别？

A：
- **发起人**：只能选择主流程第一层中包含的子流程的第一层审批人
- **审批人**：只能选择当前阶段中包含的子流程的第一层审批人

## 更新记录

- 2026-01-14：完成子流程重构，支持子流程独立运行和审批人选择
- 2026-01-14：添加详细日志记录和错误处理
- 2026-01-14：优化完成条件检查逻辑
- 2026-01-14：修复业务类型处理（MATERIAL_ENTRY）
- 2026-01-14：前端模板修复（子流程审批人选择UI）
