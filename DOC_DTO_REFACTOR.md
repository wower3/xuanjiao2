# DTO 重构文档

## 1. 背景与目标

### 问题描述
当前项目中多个 API 返回 `Map<String, Object>`，存在以下问题：
- 类型不安全，编译期无法检查
- IDE 支持差，无法自动补全
- Swagger 文档不准确
- 代码可读性差

### 目标
将返回 `Map<String, Object>` 的接口改为返回强类型 DTO，提升代码质量和可维护性。

## 2. 修改进度

### ✅ 阶段一：列表类 DTO（已完成）

| 方法 | 新增DTO | 状态 |
|------|---------|------|
| `getMyTasks` | `PendingTaskDTO` | ✅ 完成 |
| `getMyApplied` | `MyAppliedDTO` | ✅ 完成 |
| `getMyFlowItems` | 使用已有 `FlowItemDTO` | ✅ 完成 |

### ⏳ 阶段二：详情类 DTO（待处理）

| 方法 | 需要DTO | 状态 |
|------|---------|------|
| `getTaskDetail` | `TaskDetailDTO` | ⏳ 待处理 |
| `getInstanceDetail` | `InstanceDetailDTO` | ⏳ 待处理 |

### ✅ 阶段一：列表类 DTO（已完成）

| 方法 | 新增DTO | 状态 |
|------|---------|------|
| `getMyTasks` | `PendingTaskDTO` | ✅ 完成 |
| `getMyApplied` | `MyAppliedDTO` | ✅ 完成 |
| `getMyFlowItems` | 使用已有 `FlowItemDTO` | ✅ 完成 |

### ⏳ 阶段二：详情类 DTO（待处理）

| 方法 | 需要DTO | 状态 |
|------|---------|------|
| `getTaskDetail` | `TaskDetailDTO` | ⏳ 待处理 |
| `getInstanceDetail` | `InstanceDetailDTO` | ⏳ 待处理 |

### ✅ 阶段三：其他服务（已完成）

| Service | 方法 | 状态 |
|---------|------|------|
| NotificationService | `getNotificationPageDTO` | ✅ 完成 |
| NotificationService | `getNotificationPageWithWorkOrder` | ✅ 完成 |
| UserService | `searchUsers` | ✅ 完成 |
| UsageLogService | `query` | ✅ 完成 |

## 3. 已完成的修改

### 3.1 新增的文件

| 文件 | 说明 |
|------|------|
| `xuanjiao-client/.../approval/PendingTaskDTO.java` | 待办任务列表项DTO |
| `xuanjiao-client/.../approval/MyAppliedDTO.java` | 我发起的列表项DTO |
| `xuanjiao-client/.../notification/NotificationWithWorkOrderDTO.java` | 知会事项DTO |
| `xuanjiao-domain/.../entity/NotificationWithWorkOrder.java` | 知会事项实体(DO) |

### 3.2 修改的文件

| 文件 | 修改内容 |
|------|----------|
| `ApprovalService.java` | 接口返回类型改为DTO |
| `ApprovalServiceImpl.java` | 实现改为返回DTO |
| `ApprovalController.java` | 返回类型改为DTO |
| `ApprovalApiIntegrationTest.java` | 测试代码类型更新 |
| `FlowItemsApiIntegrationTest.java` | 测试代码类型更新 |
| `NotificationService.java` | getNotificationPageWithWorkOrder返回类型改为DTO |
| `NotificationServiceImpl.java` | 实现改为返回DTO |
| `NotificationRepository.java` | selectPageWithWorkOrder返回类型改为实体 |
| `NotificationRepositoryImpl.java` | 实现改为返回实体 |
| `NotificationApiIntegrationTest.java` | 测试代码类型更新 |
| `UserService.java` | searchUsers返回类型改为UserDTO |
| `UserServiceImpl.java` | 实现改为返回UserDTO |
| `UserController.java` | 返回类型改为UserDTO |
| `UsageLogService.java` | query返回类型改为UsageLogDTO |
| `UsageLogServiceImpl.java` | 实现改为返回UsageLogDTO |
| `UsageLogController.java` | 返回类型改为UsageLogDTO |

## 4. 开发规范

### 4.1 DTO 编写规范

```java
package com.xuanjiao.client.dto.xxx;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * XXX数据传输对象
 *
 * <p>用于在前后端之间传输XXX信息，包括XXX等完整属性。</p>
 *
 * @author xuanjiao
 * @since 1.0.0
 */
@Data
public class XxxDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 状态
     */
    private String status;
}
```

### 4.2 规范要点

1. **类头注释**：描述类的用途，包含 `@author xuanjiao` 和 `@since 1.0.0`
2. **使用 Lombok**：使用 `@Data` 注解简化 getter/setter
3. **字段注释**：每个字段必须有 Javadoc 注释说明其含义
4. **字段命名**：使用驼峰命名，与 JSON 字段名一致（保证前端兼容）
5. **包结构**：按模块分包，如 `dto/approval/`、`dto/notification/`

## 5. 兼容性保证

### 5.1 JSON 字段名一致性

修改前后的 JSON 输出必须完全一致，确保前端无需修改：

```java
// 修改前
map.put("workflowName", "素材审批");

// 修改后
private String workflowName;  // JSON: "workflowName"
```

### 5.2 测试验证

每个修改完成后，需要：
1. 编译通过
2. 运行相关测试
3. 对比 API 响应 JSON 格式

## 6. 后续工作

1. ⏳ 完成详情类 DTO（TaskDetailDTO、InstanceDetailDTO）
2. ✅ 完成其他服务的 DTO 重构
3. 考虑为前端添加 TypeScript 类型定义
