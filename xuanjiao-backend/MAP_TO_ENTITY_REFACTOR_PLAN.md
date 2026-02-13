# Map<String, Object> 类型安全优化方案

## 一、概述

### 1.1 背景

当前代码中存在大量 `Map<String, Object>` 使用场景，主要分布在：
- 审批详情查询（`ApprovalServiceImpl`）
- 审批人选择（`ApproverSelectionServiceImpl`）
- 草稿箱查询（`TaskController`）

使用 `Map<String, Object>` 存在以下问题：
- 类型不安全，编译期无法检查
- 无 IDE 自动补全支持
- 字段名硬编码，容易出错
- 重构时无法自动追踪引用

### 1.2 优化目标

- 将所有 `Map<String, Object>` 替换为强类型 DTO
- 复用现有 DTO 类，减少冗余
- 保持前端接口兼容性（字段名不变）
- 提升代码可维护性和类型安全性

---

## 二、现有 DTO 类分析

### 2.1 已有且可复用的 DTO

| DTO 类 | 路径 | 说明 |
|--------|------|------|
| `UserDTO` | `com.xuanjiao.client.dto.UserDTO` | 用户信息 |
| `AssetDTO` | `com.xuanjiao.client.dto.AssetDTO` | 素材信息 |
| `WorkflowDTO` | `com.xuanjiao.client.dto.WorkflowDTO` | 工作流信息 |
| `WorkflowStageDTO` | `com.xuanjiao.client.dto.WorkflowStageDTO` | 工作流阶段 |
| `StageApproverDTO` | `com.xuanjiao.client.dto.StageApproverDTO` | 阶段审批人配置 |
| `ApproverSelectionDTO` | `com.xuanjiao.client.dto.ApproverSelectionDTO` | 审批人选择信息 |
| `ApprovalProgressDTO` | `com.xuanjiao.client.dto.ApprovalProgressDTO` | 审批进度 |
| `PendingTaskDTO` | `com.xuanjiao.client.dto.approval.dto.PendingTaskDTO` | 待办任务 |
| `MyAppliedDTO` | `com.xuanjiao.client.dto.approval.dto.MyAppliedDTO` | 我发起的工单 |
| `MaterialApplicationDTO` | `com.xuanjiao.client.dto.MaterialApplicationDTO` | 素材录入申请 |
| `UsageApplyDTO` | `com.xuanjiao.client.dto.UsageApplyDTO` | 素材使用申请 |
| `AssetDeletionApplicationDTO` | `com.xuanjiao.client.dto.AssetDeletionApplicationDTO` | 素材删除申请 |

### 2.2 需要新建的 DTO

| DTO 类 | 用途 | 原始使用位置 |
|--------|------|-------------|
| `ApprovalInstanceDetailDTO` | 审批实例详情 | `ApprovalServiceImpl.buildInstanceInfo()` |
| `ApprovalTaskDetailDTO` | 审批任务详情 | `ApprovalServiceImpl.getTaskDetail()` |
| `ApprovalAssetInfoDTO` | 审批中的素材信息 | `buildInstanceInfo()` 返回的素材列表 |
| `ApproverConfigDTO` | 审批人配置（含可选用户） | `getTaskDetail()` 返回的配置信息 |
| `SubWorkflowConfigDTO` | 子流程配置 | `getTaskDetail()` 返回的子流程信息 |
| `FirstStageApproversDTO` | 首阶段审批人选择结果 | `ApproverSelectionServiceImpl.getFirstStageApprovers()` |
| `DraftItemDTO` | 草稿箱列表项 | `TaskController.queryDrafts()` |

---

## 三、详细优化方案

### 3.1 审批实例详情（ApprovalInstanceDetailDTO）

**原始代码位置**: `ApprovalServiceImpl.buildInstanceInfo()`

**原始 Map 结构**:
```java
{
    "id", "instanceId", "status", "businessType", "businessId", "createTime",
    "workflowName", "workflowId", "applicationId", "applicationTitle", "businessName",
    "assetType", "assetStatus", "assetCount", "assetList", "assets",
    "applicantId", "applicantName", "currentStageId", "currentStageName", "approveType",
    "pendingApprovers", "approvalProgress"
}
```

**新建 DTO**: `xuanjiao-client/src/main/java/com/xuanjiao/client/dto/approval/ApprovalInstanceDetailDTO.java`

```java
package com.xuanjiao.client.dto.approval;

import com.xuanjiao.client.dto.ApprovalProgressDTO;
import com.xuanjiao.client.dto.workflow.WorkflowBasicInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批实例详情数据传输对象
 *
 * <p>用于在前后端之间传输审批实例的完整信息，包括：
 * <ul>
 *   <li>实例基本信息（ID、状态、业务类型等）</li>
 *   <li>业务信息（申请单标题、素材列表等）</li>
 *   <li>申请人信息</li>
 *   <li>当前阶段信息</li>
 *   <li>待审批人列表</li>
 *   <li>审批进度</li>
 * </ul>
 */
@Data
public class ApprovalInstanceDetailDTO {

    /**
     * 实例ID（兼容前端字段名 instanceId）
     */
    private Long id;
    private Long instanceId; // 前端兼容字段，与 id 相同

    /**
     * 审批状态（PENDING-待审批、APPROVED-已通过、REJECTED-已驳回、MAIN_COMPLETED-主流程完成）
     */
    private String status;

    /**
     * 业务类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String businessType;

    /**
     * 业务ID（申请单ID或素材ID）
     */
    private Long businessId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 工作流基本信息
     */
    private WorkflowBasicInfo workflow;

    /**
     * 申请单ID（素材录入/删除申请）
     */
    private Long applicationId;

    /**
     * 申请单标题
     */
    private String applicationTitle;

    /**
     * 业务名称（用于前端显示）
     */
    private String businessName;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String assetType;

    /**
     * 素材状态
     */
    private String assetStatus;

    /**
     * 素材数量
     */
    private Integer assetCount;

    /**
     * 素材列表
     */
    private List<ApprovalAssetInfoDTO> assets;
    private List<ApprovalAssetInfoDTO> assetList; // 兼容旧字段名

    /**
     * 申请人信息
     */
    private ApplicantInfo applicant;

    /**
     * 当前阶段信息
     */
    private CurrentStageInfo currentStage;

    /**
     * 待审批人列表
     */
    private List<ApproverSimpleInfo> pendingApprovers;

    /**
     * 审批进度
     */
    private List<ApprovalProgressDTO> approvalProgress;

    /**
     * 删除原因（仅 ASSET_DELETION 类型）
     */
    private String deleteReason;

    /**
     * 工作流基本信息
     */
    @Data
    public static class WorkflowBasicInfo {
        private Long id;
        private String name;
    }

    /**
     * 申请人信息
     */
    @Data
    public static class ApplicantInfo {
        private Long id;
        private String name;
    }

    /**
     * 当前阶段信息
     */
    @Data
    public static class CurrentStageInfo {
        private Long id;
        private String name;
        private String approveType; // AND-会签，OR-或签
    }

    /**
     * 审批人简单信息
     */
    @Data
    public static class ApproverSimpleInfo {
        private Long id;
        private String name;
    }
}
```

**新建 DTO**: `ApprovalAssetInfoDTO.java`

```java
package com.xuanjiao.client.dto.approval;

import lombok.Data;

/**
 * 审批中的素材信息数据传输对象
 *
 * <p>用于在审批详情中展示素材的基本信息和文件路径，
 * 支持预览和下载功能。</p>
 */
@Data
public class ApprovalAssetInfoDTO {

    /**
     * 素材ID
     */
    private Long id;

    /**
     * 素材名称
     */
    private String name;

    /**
     * 素材类型（IMAGE-图片、VIDEO-视频、DOCUMENT-文档）
     */
    private String type;

    /**
     * 素材状态（PENDING-待审批、APPROVED-已通过）
     */
    private String status;

    /**
     * 文件路径（用于预览和下载）
     */
    private String filePath;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 素材描述（素材录入申请填写）
     */
    private String description;

    /**
     * 发布渠道（素材录入申请填写）
     */
    private String publishChannel;

    /**
     * 版权文件路径
     */
    private String copyrightFilePath;

    /**
     * 使用描述（素材使用申请填写）
     */
    private String usageDescription;

    /**
     * 使用发布渠道（素材使用申请填写）
     */
    private String usagePublishChannel;
}
```

---

### 3.2 审批任务详情（ApprovalTaskDetailDTO）

**原始代码位置**: `ApprovalServiceImpl.getTaskDetail()`

**原始 Map 结构**:
```java
{
    "id", "status", "taskType", "isFirstApprover", "nextStageApproverIds",
    "selectedByUserId", "approverId", "subWorkflowApproverIds", "instanceId",
    "businessType", "businessId", "workflowId", "currentStageId", "workflowName",
    "stageId", "stageName", "approveType", "nextStageId", "nextStageName",
    "nextStageApproveType", "nextStageApproverConfigs", "nextStageApproverCount",
    "subWorkflows", "hasSubWorkflows", "isLastStage", "businessName", "applicationId",
    "applicationTitle", "deleteReason", "applicantId", "applicantName",
    "approvalProgress", "canSelectNextApprovers", "selectedNextApprovers",
    "selectedSubWorkflowApprovers", "otherApprovers"
}
```

**新建 DTO**: `ApprovalTaskDetailDTO.java`

```java
package com.xuanjiao.client.dto.approval;

import com.xuanjiao.client.dto.ApprovalProgressDTO;
import com.xuanjiao.client.dto.ApproverSelectionDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审批任务详情数据传输对象
 *
 * <p>用于在审批任务详情页展示完整的任务信息，包括：
 * <ul>
 *   <li>任务基本信息（ID、状态、类型等）</li>
 *   <li>关联的审批实例信息</li>
 *   <li>下一阶段配置（审批人配置、子流程配置）</li>
 *   <li>已选择的审批人信息</li>
 *   <li>同阶段其他审批人</li>
 *   <li>审批权限判断（是否可选择下一层审批人）</li>
 * </ul>
 */
@Data
public class ApprovalTaskDetailDTO {

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务状态（PENDING-待审批、APPROVED-已通过、REJECTED-已驳回、RETURNED-已退回、CANCELLED-已取消）
     */
    private String status;

    /**
     * 任务类型（NORMAL-普通审批、RESTART_SUB_WORKFLOW-重新发起子流程）
     */
    private String taskType;

    /**
     * 是否是第一个审批人（或签场景下，第一个完成任务的审批人）
     */
    private Integer isFirstApprover;

    /**
     * 下一阶段审批人ID列表（JSON 格式存储）
     */
    private String nextStageApproverIds;

    /**
     * 选择该审批人的用户ID（子流程场景）
     */
    private Long selectedByUserId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 子流程审批人ID列表（JSON 格式：子流程ID -> 审批人ID列表）
     */
    private String subWorkflowApproverIds;

    /**
     * 审批实例ID
     */
    private Long instanceId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 当前阶段ID
     */
    private Long currentStageId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 阶段ID
     */
    private Long stageId;

    /**
     * 阶段名称
     */
    private String stageName;

    /**
     * 审批类型（AND-会签，OR-或签）
     */
    private String approveType;

    /**
     * 下一阶段ID
     */
    private Long nextStageId;

    /**
     * 下一阶段名称
     */
    private String nextStageName;

    /**
     * 下一阶段审批类型
     */
    private String nextStageApproveType;

    /**
     * 是否是最后阶段
     */
    private Boolean isLastStage;

    /**
     * 下一阶段审批人配置列表
     */
    private List<ApproverConfigDTO> nextStageApproverConfigs;

    /**
     * 下一阶段审批人配置数量
     */
    private Integer nextStageApproverCount;

    /**
     * 子流程列表
     */
    private List<SubWorkflowConfigDTO> subWorkflows;

    /**
     * 是否有子流程
     */
    private Boolean hasSubWorkflows;

    /**
     * 业务名称
     */
    private String businessName;

    /**
     * 申请单ID（素材删除申请）
     */
    private Long applicationId;

    /**
     * 申请单标题
     */
    private String applicationTitle;

    /**
     * 删除原因（素材删除申请）
     */
    private String deleteReason;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 审批进度
     */
    private List<ApprovalProgressDTO> approvalProgress;

    /**
     * 是否可以选择下一层审批人
     */
    private Boolean canSelectNextApprovers;

    /**
     * 已选择的下一层审批人列表
     */
    private List<ApproverSelectionDTO> selectedNextApprovers;

    /**
     * 已选择的子流程审批人（子流程ID -> 审批人列表）
     */
    private Map<Long, List<ApproverSelectionDTO>> selectedSubWorkflowApprovers;

    /**
     * 同阶段其他审批人
     */
    private List<OtherApproverInfo> otherApprovers;
}

/**
 * 审批人配置DTO（含可选用户列表）
 */
@Data
class ApproverConfigDTO {
    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 审批人类型（USER-用户、DEPT-部门、ROLE-角色）
     */
    private String approverType;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 是否校验二级部门
     */
    private Integer checkSecondaryDept;

    /**
     * 审批人类型名称（中文显示）
     */
    private String approverTypeName;

    /**
     * 审批人名称
     */
    private String approverName;

    /**
     * 该配置的可选用户列表
     */
    private List<ApproverSelectionDTO> availableUsers;
}

/**
 * 子流程配置DTO
 */
@Data
class SubWorkflowConfigDTO {
    /**
     * 子流程ID
     */
    private Long id;

    /**
     * 子流程名称
     */
    private String name;

    /**
     * 子流程类型
     */
    private String workflowType;

    /**
     * 审批类型（AND-会签，OR-或签）
     */
    private String approveType;

    /**
     * 审批人配置列表
     */
    private List<ApproverConfigDTO> approverConfigs;

    /**
     * 审批人配置数量
     */
    private Integer approverCount;
}

/**
 * 同阶段其他审批人信息
 */
@Data
class OtherApproverInfo {
    private Long id;
    private String name;
    private String status;
}
```

---

### 3.3 首阶段审批人选择（FirstStageApproversDTO）

**原始代码位置**: `ApproverSelectionServiceImpl.getFirstStageApprovers()`

**新建 DTO**: `FirstStageApproversDTO.java`

```java
package com.xuanjiao.client.dto.workflow;

import com.xuanjiao.client.dto.ApproverSelectionDTO;
import com.xuanjiao.client.dto.StageApproverDTO;
import lombok.Data;

import java.util.List;

/**
 * 首阶段审批人选择结果数据传输对象
 *
 * <p>用于返回首阶段审批人配置的可选用户列表，
 * 供前端在创建申请时选择审批人。</p>
 */
@Data
public class FirstStageApproversDTO {

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 工作流类型
     */
    private String workflowType;

    /**
     * 首阶段ID
     */
    private Long firstStageId;

    /**
     * 首阶段名称
     */
    private String firstStageName;

    /**
     * 审批类型（AND-会签，OR-或签）
     */
    private String approveType;

    /**
     * 审批人配置列表（每个配置包含类型、名称、可选用户）
     */
    private List<StageApproverConfigDTO> approverConfigs;

    /**
     * 阶段审批人配置DTO
     */
    @Data
    public static class StageApproverConfigDTO {

        /**
         * 配置ID
         */
        private Long configId;

        /**
         * 审批人类型（USER-用户、DEPT-部门、ROLE-角色）
         */
        private String approverType;

        /**
         * 审批人ID
         */
        private Long approverId;

        /**
         * 是否校验二级部门
         */
        private Integer checkSecondaryDept;

        /**
         * 子流程ID
         */
        private Long subWorkflowId;

        /**
         * 子流程名称
         */
        private String subWorkflowName;

        /**
         * 审批人类型名称（中文显示）
         */
        private String approverTypeName;

        /**
         * 审批人名称
         */
        private String approverName;

        /**
         * 该配置的可选用户列表
         */
        private List<ApproverSelectionDTO> availableUsers;
    }
}
```

---

### 3.4 草稿箱列表项（DraftItemDTO）

**原始代码位置**: `TaskController.queryDrafts()`

**新建 DTO**: `DraftItemDTO.java`

```java
package com.xuanjiao.client.dto.task;

import com.xuanjiao.client.dto.MaterialApplicationDTO;
import com.xuanjiao.client.dto.UsageApplyDTO;
import com.xuanjiao.client.dto.AssetDeletionApplicationDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 草稿箱列表项数据传输对象
 *
 * <p>用于在草稿箱中展示不同类型的草稿申请，
 * 包括素材录入、素材使用、素材删除三种类型。</p>
 */
@Data
public class DraftItemDTO {

    /**
     * 草稿类型（MATERIAL_ENTRY-素材录入、ASSET_USAGE-素材使用、ASSET_DELETION-素材删除）
     */
    private String type;

    /**
     * 草稿ID
     */
    private Long id;

    /**
     * 草稿标题
     */
    private String title;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 素材录入申请数据（仅当 type = MATERIAL_ENTRY 时有值）
     */
    private MaterialApplicationDTO materialEntry;

    /**
     * 素材使用申请数据（仅当 type = ASSET_USAGE 时有值）
     */
    private UsageApplyDTO assetUsage;

    /**
     * 素材删除申请数据（仅当 type = ASSET_DELETION 时有值）
     */
    private AssetDeletionApplicationDTO assetDeletion;
}
```

---

## 四、Service 层修改

### 4.1 ApprovalService 接口修改

**修改前**:
```java
Map<String, Object> getTaskDetail(Long taskId);
Map<String, Object> getInstanceDetail(Long instanceId);
```

**修改后**:
```java
ApprovalTaskDetailDTO getTaskDetail(Long taskId);
ApprovalInstanceDetailDTO getInstanceDetail(Long instanceId);
```

### 4.2 ApproverSelectionService 接口修改

**修改前**:
```java
Map<String, Object> getFirstStageApprovers(Long workflowId, Long applicantId, String keyword);
Map<String, Object> getSubWorkflowFirstStageApprovers(Long subWorkflowId, Long applicantId, String keyword);
```

**修改后**:
```java
FirstStageApproversDTO getFirstStageApprovers(Long workflowId, Long applicantId, String keyword);
FirstStageApproversDTO getSubWorkflowFirstStageApprovers(Long subWorkflowId, Long applicantId, String keyword);
```

### 4.3 TaskController 修改

**修改前**:
```java
public Result<PageResult<Map<String, Object>>> queryDrafts(...)
```

**修改后**:
```java
public Result<PageResult<DraftItemDTO>> queryDrafts(...)
```

---

## 五、实施计划

### 阶段一：创建新的 DTO 类（不影响现有功能）

1. 创建 `ApprovalAssetInfoDTO.java`
2. 创建 `ApprovalInstanceDetailDTO.java`
3. 创建 `ApprovalTaskDetailDTO.java`
4. 创建 `ApproverConfigDTO.java`（内部类）
5. 创建 `SubWorkflowConfigDTO.java`（内部类）
6. 创建 `FirstStageApproversDTO.java`
7. 创建 `DraftItemDTO.java`

### 阶段二：修改 Service 实现（返回强类型）

1. 修改 `ApprovalServiceImpl.buildInstanceInfo()` 返回 `ApprovalInstanceDetailDTO`
2. 修改 `ApprovalServiceImpl.getTaskDetail()` 返回 `ApprovalTaskDetailDTO`
3. 修改 `ApproverSelectionServiceImpl.getFirstStageApprovers()` 返回 `FirstStageApproversDTO`
4. 修改 `ApproverSelectionServiceImpl.getSubWorkflowFirstStageApprovers()` 返回 `FirstStageApproversDTO`

### 阶段三：修改 Controller 层

1. 修改 `ApprovalController.getTaskDetail()` 返回类型
2. 修改 `ApprovalController.getInstanceDetail()` 返回类型
3. 修改 `ApproverSelectionController.getFirstStageApprovers()` 返回类型
4. 修改 `ApproverSelectionController.getSubWorkflowFirstStageApprovers()` 返回类型
5. 修改 `TaskController.queryDrafts()` 返回类型

### 阶段四：修改测试代码

1. 修改 `ApprovalServiceImplTest.java` 中的测试用例
2. 修改 `ApprovalApiIntegrationTest.java` 中的测试用例

### 阶段五：删除工具方法

删除以下不再需要的转换方法：
- `ApprovalServiceImpl.getAvailableUsersForConfig()` - 返回 List<Map> 的方法
- `ApproverSelectionServiceImpl.convertUserToMap()` - 转换 UserDO 到 Map
- `TaskController.convertMaterialApplicationAssets()` - 转换 DTO 到 Map
- `TaskController.convertUsageApplyAssets()` - 转换 DTO 到 Map
- `TaskController.convertDeletionApplicationAssets()` - 转换 DTO 到 Map

---

## 六、向后兼容性说明

### 6.1 Jackson 序列化

由于使用 `@Data` 注解，Jackson 会自动将 DTO 序列化为 JSON，字段名与原来 Map 的 key 一致：
- `instanceId` 字段：DTO 中同时存在 `id` 和 `instanceId` 两个属性，序列化后都会保留
- `assets` 和 `assetList`：同时保留两个字段，兼容旧前端

### 6.2 前端无感知

前端代码无需修改，因为：
1. JSON 字段名与原来完全一致
2. 嵌套结构保持不变
3. 数组类型保持不变

---

## 七、收益分析

### 7.1 类型安全

| 方面 | Map<String, Object> | 强类型 DTO |
|------|---------------------|------------|
| 编译期检查 | ❌ | ✅ |
| IDE 自动补全 | ❌ | ✅ |
| 重构支持 | ❌ | ✅ |
| 字段名拼写 | 运行时发现 | 编译期发现 |

### 7.2 代码可维护性

- 清晰的类型定义，一目了然
- 方法签名自文档化
- 减少注释依赖
- 便于新人理解代码结构

### 7.3 性能影响

- 无性能损失：DTO 只是编译期类型检查，运行时与 Map 相同
- Jackson 序列化性能相同

---

## 八、注意事项

1. **渐进式迁移**：先创建 DTO，再修改返回类型，最后删除旧代码
2. **保持字段名一致**：确保 JSON 序列化后的字段名与原来 Map 的 key 完全一致
3. **兼容性字段**：保留如 `instanceId`（与 `id` 相同）、`assetList`（与 `assets` 相同）等兼容字段
4. **内部类处理**：`ApproverConfigDTO`、`SubWorkflowConfigDTO` 等可考虑作为独立类或内部类
5. **测试覆盖**：确保所有修改的接口都有对应的单元测试和集成测试
