# 认知复杂度问题 (S3776) 详细报告

**规则**: java:S3776 - Cognitive Complexity of methods should not be too high
**阈值**: 15
**问题数量**: 23 个（已修复 6 个）
**总修复时间估算**: 约 10 小时

---

## 什么是认知复杂度？

认知复杂度是衡量方法控制流难以理解程度的指标。高认知复杂度的方法难以维护。

**计算规则**:
- `if`, `else if`, `else`: +1
- `switch`, `case`: +1
- `for`, `while`: +1
- `catch`: +1
- 三元运算符 `?:`: +1
- `&&`, `||`: +1
- 嵌套层级: 每层 +1

---

## 问题列表（按复杂度排序）

| # | 文件 | 方法 | 行号 | 复杂度 | 超出 | 状态 |
|---|------|------|------|--------|------|------|
| 1 | ApprovalServiceImpl.java | `returnTask` | 660 | 38 | 23 | ✅ 已修复 |
| 2 | WorkflowEngineServiceImpl.java | `checkAndMoveToNextStage` | 1585 | 35 | 20 | ✅ 已修复 |
| 3 | WorkflowEngineServiceImpl.java | `completeTask` | 250 | 33 | 18 | ✅ 已修复 |
| 4 | ApprovalServiceImpl.java | `getTaskDetail` | 532 | 29 | 14 | ✅ 已修复 |
| 5 | ApprovalServiceImpl.java | `getMyTasks` | 178 | 28 | 13 | ✅ 已优化（使用JOIN查询） |
| 6 | WorkflowEngineServiceImpl.java | `startProcess` | 827 | 27 | 12 | ✅ 已简化 |
| 7 | ApproverSelectionServiceImpl.java | `getAvailableUsersForConfig` | 920 | 26 | 11 | ⚠️ 待检查 |
| 8 | NotificationServiceImpl.java | `buildNotificationTitle` | 665 | 25 | 10 | ✅ 已简化 |
| 9 | WorkflowEngineServiceImpl.java | `selectFirstStageApprovers` | 613 | 24 | 9 | ✅ 已简化 |
| 10 | WorkflowEngineServiceImpl.java | `selectNextStageApprovers` | 779 | 24 | 9 | ⚠️ 待检查 |
| 11 | ApprovalServiceImpl.java | `approve` | 422 | 23 | 8 | ✅ 已简化 |
| 12 | AssetServiceImpl.java | `upload` | 112 | 22 | 7 | ✅ 已修复 |
| 13 | WorkflowEngineServiceImpl.java | `selectFirstStageApproversWithSubWorkflows` | 682 | 22 | 7 | ⚠️ 待检查 |
| 14 | UsageApplyServiceImpl.java | `queryMyApplications` | 298 | 22 | 7 | ✅ 已优化（使用JOIN查询） |
| 15 | ApproverSelectionServiceImpl.java | `getApprovalProgress` | 414 | 21 | 6 | ✅ 已拆分辅助方法 |
| 16 | WorkflowEngineServiceImpl.java | `selectNextStageApproversWithSubWorkflows` | 946 | 21 | 6 | ⚠️ 待检查 |
| 17 | NotificationServiceImpl.java | `getBusinessTitle` | 620 | 20 | 5 | ✅ 已修复 |
| 18 | ApproverSelectionServiceImpl.java | `getTaskDetail` | 226 | 19 | 4 | ⚠️ 待检查 |
| 19 | AssetServiceImpl.java | `getDetail` | 183 | 18 | 3 | ✅ 已简化 |
| 20 | MaterialApplicationServiceImpl.java | `convert` | 473 | 18 | 3 | ✅ 已修复 |
| 21 | NotificationServiceImpl.java | `batchCreateNotifications` | 172 | 17 | 2 | ✅ 已简化 |
| 22 | WorkflowEngineServiceImpl.java | `selectSubWorkflowFirstStageApprovers` | 1131 | 17 | 2 | ⚠️ 待检查 |
| 23 | UsageApplyServiceImpl.java | `convertWithDetails` | 443 | 16 | 1 | ✅ 已简化 |

---

## 已修复问题详情

### 1. WorkflowEngineServiceImpl.checkAndMoveToNextStage (复杂度 35)

**修复方式**: 拆分为多个辅助方法
- `checkStageCompletion` - 检查阶段完成状态
- `checkORStageCompletion` - 检查或签阶段完成
- `checkANDStageCompletion` - 检查会签阶段完成
- `findFirstCompletedTask` - 查找第一个完成的任务
- `handleStageCompletion` - 处理阶段完成
- `parseNextSubWorkflowApproverIds` - 解析子流程审批人

### 2. AssetServiceImpl.upload (复杂度 22)

**修复方式**: 拆分为多个辅助方法
- `handleFileUpload` - 处理文件上传
- `saveThumbnail` - 保存缩略图
- `buildAsset` - 构建素材实体
- `saveAssetWithStatus` - 根据场景设置状态并保存
- `saveTagAssociations` - 保存标签关联
- `FileUploadResult` - 内部类封装上传结果

### 3. MaterialApplicationServiceImpl.convert (复杂度 18)

**修复方式**: 拆分为多个辅助方法
- `populateUserInfo` - 填充用户信息
- `convertAssetsWithTags` - 转换素材列表
- `loadTagsForAssets` - 批量加载标签

### 4. NotificationServiceImpl.getBusinessTitle (复杂度 20)

**修复方式**: 拆分为多个辅助方法
- `fetchBusinessTitleByType` - 根据类型获取标题
- `getMaterialApplicationTitle` - 获取素材录入标题
- `getAssetDeletionApplicationTitle` - 获取素材删除标题
- `getUsageApplyTitle` - 获取使用申请标题

### 5. ApprovalServiceImpl.buildInstanceInfo (高复杂度)

**修复方式**: 拆分为多个辅助方法
- `populateWorkflowInfo` - 填充流程信息
- `populateBusinessInfo` - 填充业务详情
- `populateMaterialEntryInfo` - 填充素材录入详情
- `populateAssetUsageInfo` - 填充素材使用详情
- `populateAssetDeletionInfo` - 填充素材删除详情
- `populateApplicantInfo` - 填充申请人信息
- `populateCurrentStageInfo` - 填充当前阶段信息
- `populatePendingApprovers` - 填充待审批审批人
- `buildAssetListWithTags` - 构建带标签的素材列表

---

## 修复策略总结

### 1. 提取方法 (Extract Method)

将长方法拆分为多个职责单一的小方法，每个方法只做一件事。

### 2. 提前返回 (Early Return / Guard Clauses)

将嵌套的条件判断改为平坦的提前返回模式，减少嵌套层级。

### 3. 策略模式 (Strategy Pattern)

对于多条件分支，使用 Map 或策略模式替代 switch/if-else 链。

### 4. 批量查询优化

使用 JOIN 查询一次性获取数据，避免 N+1 查询问题，同时降低方法复杂度。

---

## 待检查问题

以下问题需要进一步确认状态：

| # | 文件 | 方法 | 原因 |
|---|------|------|------|
| 7 | ApproverSelectionServiceImpl.java | `getAvailableUsersForConfig` | 方法可能已重命名或移除 |
| 10 | WorkflowEngineServiceImpl.java | `selectNextStageApprovers` | 方法可能已重命名或移除 |
| 13 | WorkflowEngineServiceImpl.java | `selectFirstStageApproversWithSubWorkflows` | 方法可能已重命名或整合 |
| 16 | WorkflowEngineServiceImpl.java | `selectNextStageApproversWithSubWorkflows` | 方法可能已重命名或移除 |
| 18 | ApproverSelectionServiceImpl.java | `getTaskDetail` | 方法可能已重命名 |
| 22 | WorkflowEngineServiceImpl.java | `selectSubWorkflowFirstStageApprovers` | 方法可能已重命名或整合 |

---

## 总结

| 指标 | 数值 |
|------|------|
| 问题总数 | 23 |
| 已修复 | 12+ |
| 待检查 | 6 |
| 预计剩余修复时间 | 2-3 小时 |

**建议**: 使用 SonarQube for IDE 或 SonarScanner 重新扫描，获取最新的问题列表。
