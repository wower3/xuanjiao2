# Map<String, Object> 优化文档

## 概述

本文档记录项目中 `Map<String, Object>` 返回类型的优化计划，将逐步改造为强类型的 DTO，提升代码可维护性和类型安全。

## 优化范围

| 优先级 | Service 方法 | 当前返回类型 | 目标 DTO | 状态 |
|--------|-------------|-------------|----------|------|
| **P0** | `UsageLogService.query()` | `PageResult<Map>` | `PageResult<UsageLogDTO>` | ✅ 已完成 |
| **P1** | `UserService.searchUsers()` | `PageResult<Map>` | `PageResult<UserDTO>` | ✅ 已完成 |
| **P2** | `NotificationService.getNotificationPageWithWorkOrder()` | `PageResult<Map>` | `PageResult<NotificationDTO>` | ✅ 已完成 |
| **P3** | `ApprovalService.getTaskDetail()` | `Map` | `TaskDetailDTO` | ✅ 已完成 |
| **P3** | `ApprovalService.getInstanceDetail()` | `Map` | `InstanceDetailDTO` | ✅ 已完成 |
| **P4** | ApproverSelectionService 方法 | `Map` | 暂不改造 | 不涉及 |

## 改造原则

1. **复用已有 DTO**：优先使用项目中已存在的 DTO 类
2. **保持兼容性**：确保前端接收的数据结构兼容（或同步前端一起修改）
3. **编译验证**：改造完成后执行 `mvn compile` 验证编译通过
4. **单元测试**：运行相关单元测试确保功能正常

## 详细改造计划

### P0: UsageLogService.query()

**文件位置**：
- Service: `xuanjiao-app/src/main/java/com/xuanjiao/app/usage/UsageLogService.java`
- ServiceImpl: `xuanjiao-app/src/main/java/com/xuanjiao/app/usage/impl/UsageLogServiceImpl.java`
- Controller: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/usage/UsageLogController.java`

**已有 DTO**：`UsageLogDTO` (xuanjiao-client)

**改造方案**：
- 已有 `getAssetUsageLogs()` 方法返回 `PageResult<UsageLogDTO>`，可复用其逻辑
- 将 `query()` 方法的返回类型改为 `PageResult<UsageLogDTO>`
- 复用 `toDTO()` 转换方法（包含用户名）

---

### P1: UserService.searchUsers()

**文件位置**：
- Service: `xuanjiao-app/src/main/java/com/xuanjiao/app/user/UserService.java`
- ServiceImpl: `xuanjiao-app/src/main/java/com/xuanjiao/app/user/impl/UserServiceImpl.java`
- Controller: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/user/UserController.java`

**已有 DTO**：`UserDTO` (xuanjiao-client)

**改造方案**：
- 将返回类型从 `PageResult<Map>` 改为 `PageResult<UserDTO>`
- 复用现有的 Entity→DTO 转换逻辑
- 扩展 `UserDTO` 添加必要的搜索结果字段（如果需要）

---

### P2: NotificationService.getNotificationPageWithWorkOrder()

**文件位置**：
- Service: `xuanjiao-app/src/main/java/com/xuanjiao/app/notification/NotificationService.java`
- ServiceImpl: `xuanjiao-app/src/main/java/com/xuanjiao/app/notification/impl/NotificationServiceImpl.java`
- Controller: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/notification/NotificationController.java`

**已有 DTO**：`NotificationDTO` (xuanjiao-client)

**改造方案**：
- 将返回类型改为 `PageResult<NotificationDTO>`
- 扩展 `NotificationDTO` 添加工单相关字段（如果需要）
- 复用现有的 `convertToDTO()` 方法

---

### P3: ApprovalService.getTaskDetail() / getInstanceDetail()

**文件位置**：
- Service: `xuanjiao-app/src/main/java/com/xuanjiao/app/approval/ApprovalService.java`
- ServiceImpl: `xuanjiao-app/src/main/java/com/xuanjiao/app/approval/impl/ApprovalServiceImpl.java`
- Controller: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/approval/ApprovalController.java`

#### 可复用的已有 DTO

| 需要的嵌套 DTO | 已有 DTO | 复用情况 |
|---------------|----------|---------|
| 用户信息 | `UserDTO` | ✅ 可直接复用 |
| 素材信息 | `AssetDTO` | ✅ 可直接复用 |
| 审批人配置 | `StageApproverDTO` | ⚠️ 需扩展 `availableUsers` 字段 |
| 审批阶段 | `ApprovalProgressDTO` | ✅ 可直接复用（含内部类 `ApproverInfo`） |
| 子流程信息 | 无 | ❌ 需新建 `SubWorkflowDTO` |

#### 需要创建的 DTO

1. **扩展 `StageApproverDTO`**：添加 `availableUsers: List<UserDTO>` 字段
2. **新建 `SubWorkflowDTO`**：子流程信息 + 审批人配置列表
3. **新建 `TaskDetailDTO`**：任务详情
4. **新建 `InstanceDetailDTO`**：实例详情

#### 为什么子流程没有单独的 DTO？

子流程（Sub-Workflow）在数据库中的定义是：
- `workflow` 表中的一条记录（与主流程共用同一张表）
- 通过 `stage_approver.sub_workflow_id` 关联到另一个 workflow
- 运行时通过 `approval_instance.parent_instance_id` 建立父子关系

因此子流程在代码中是**复用 `WorkflowDTO`** 的，但需要构建一个包含审批人配置的完整信息对象，这就是 `SubWorkflowDTO` 存在的意义。

#### 详细 DTO 结构

**SubWorkflowDTO**
```java
@Data
public class SubWorkflowDTO {
    private Long id;                    // 子流程ID
    private String name;                 // 子流程名称
    private String workflowType;         // 流程类型
    private String approveType;           // 审批类型（OR/AND）
    private List<StageApproverDTO> approverConfigs;  // 审批人配置
    private Integer approverCount;       // 审批人数量
}
```

**TaskDetailDTO**
```java
@Data
public class TaskDetailDTO {
    // 任务基础信息
    private Long id;
    private String status;
    private String taskType;
    private Integer isFirstApprover;
    private String nextStageApproverIds;
    private Long selectedByUserId;
    private Long approverId;
    private String subWorkflowApproverIds;

    // 实例信息
    private Long instanceId;
    private String businessType;
    private Long businessId;
    private Long workflowId;
    private Long currentStageId;

    // 流程信息
    private String workflowName;

    // 当前阶段信息
    private Long stageId;
    private String stageName;
    private String approveType;

    // 下一阶段信息
    private Long nextStageId;
    private String nextStageName;
    private String nextStageApproveType;

    // 嵌套结构（复用已有 DTO）
    private List<StageApproverDTO> nextStageApproverConfigs;
    private Integer nextStageApproverCount;
    private List<SubWorkflowDTO> subWorkflows;
    private List<UserDTO> selectedNextApprovers;
    private Map<Long, List<UserDTO>> selectedSubWorkflowApprovers;
    private List<UserDTO> otherApprovers;
    private List<AssetDTO> assets;
    private Boolean canSelectNextApprovers;
}
```

**InstanceDetailDTO**
```java
@Data
public class InstanceDetailDTO {
    // 实例基础信息
    private Long id;
    private String status;
    private String businessType;
    private Long businessId;
    private LocalDateTime createTime;

    // 流程信息
    private Long workflowId;
    private String workflowName;

    // 业务信息
    private Long applicationId;
    private String applicationTitle;
    private String businessName;
    private String assetType;
    private String assetStatus;
    private Integer assetCount;

    // 嵌套结构
    private List<AssetDTO> assets;
    private UserDTO applicant;
    private List<ApprovalProgressDTO> approvalProgress;
    private Long currentStageId;
    private String currentStageName;
    private Boolean canSelectNextApprovers;
}
```

---

## 改造流程

1. 修改 Service 接口返回类型
2. 修改 ServiceImpl 实现
3. 修改 Controller 接收类型
4. 执行 `mvn compile -DskipTests` 编译验证
5. 执行单元测试 `mvn test`
6. 验证通过后提交 commit
7. 确认后再进行下一个改造
