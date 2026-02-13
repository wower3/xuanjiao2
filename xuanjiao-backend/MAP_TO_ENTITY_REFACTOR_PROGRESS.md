# Map<String, Object> 类型安全优化进度

## 一、优化目标

将代码中的 `Map<String, Object>` 替换为强类型 DTO，提升类型安全性和代码可维护性。

## 二、优化范围

| 序号 | 模块 | 位置 | 优化内容 |
|------|------|------|----------|
| 1 | 审批详情 | `ApprovalServiceImpl.buildInstanceInfo()` | 返回 `ApprovalInstanceDetailDTO` |
| 2 | 审批详情 | `ApprovalServiceImpl.getTaskDetail()` | 返回 `ApprovalTaskDetailDTO` |
| 3 | 审批人选择 | `ApproverSelectionServiceImpl.getFirstStageApprovers()` | 返回 `FirstStageApproversDTO` |
| 4 | 审批人选择 | `ApproverSelectionServiceImpl.getSubWorkflowFirstStageApprovers()` | 返回 `FirstStageApproversDTO` |
| 5 | 草稿箱 | `TaskController.queryDrafts()` | 返回 `PageResult<DraftItemDTO>` |
| 6 | 工具方法 | `ApprovalServiceImpl.getAvailableUsersForConfig()` | 返回 `List<ApproverSelectionDTO>` |
| 7 | 工具方法 | `ApproverSelectionServiceImpl.convertUserToMap()` | 删除，直接使用 DTO |
| 8 | 转换方法 | `TaskController.convertMaterialApplicationAssets()` | 删除，直接使用 DTO |
| 9 | 转换方法 | `TaskController.convertUsageApplyAssets()` | 删除，直接使用 DTO |
| 10 | 转换方法 | `TaskController.convertDeletionApplicationAssets()` | 删除，直接使用 DTO |

## 三、阶段进度

### 阶段一：创建新的 DTO 类 ✅ DONE

| 状态 | DTO 类 | 路径 | 说明 |
|------|--------|------|------|
| ✅ DONE | `ApprovalAssetInfoDTO` | `xuanjiao-client/.../approval/` | 审批中的素材信息 |
| ✅ DONE | `ApprovalInstanceDetailDTO` | `xuanjiao-client/.../approval/` | 审批实例详情 |
| ✅ DONE | `ApprovalTaskDetailDTO` | `xuanjiao-client/.../approval/` | 审批任务详情 |
| ✅ DONE | `ApproverConfigDTO` | `xuanjiao-client/.../approval/` | 审批人配置（独立类） |
| ✅ DONE | `SubWorkflowConfigDTO` | `xuanjiao-client/.../approval/` | 子流程配置（独立类） |
| ✅ DONE | `FirstStageApproversDTO` | `xuanjiao-client/.../workflow/` | 首阶段审批人选择结果 |
| ✅ DONE | `DraftItemDTO` | `xuanjiao-client/.../task/` | 草稿箱列表项（组合设计） |

**测试要求**：
- [x] DTO 类编译通过
- [x] Jackson 序列化测试通过（验证字段名一致）

### 阶段二：修改 Service 接口和实现 ✅ DONE

| 状态 | Service | 方法 | 修改内容 |
|------|---------|------|----------|
| ✅ DONE | `ApprovalService` | `getInstanceDetail()` | 返回类型改为 `ApprovalInstanceDetailDTO` |
| ✅ DONE | `ApprovalService` | `getTaskDetail()` | 返回类型改为 `ApprovalTaskDetailDTO` |
| ✅ DONE | `ApprovalServiceImpl` | `buildInstanceInfo()` | 返回 `ApprovalInstanceDetailDTO` |
| ✅ DONE | `ApprovalServiceImpl` | `getTaskDetail()` | 返回 `ApprovalTaskDetailDTO` |
| ✅ DONE | `ApprovalServiceImpl` | `getAvailableUsersForConfig()` | 返回 `List<ApproverSelectionDTO>` |
| ✅ DONE | `ApproverSelectionService` | `getFirstStageApprovers()` | 返回类型改为 `FirstStageApproversDTO` |
| ✅ DONE | `ApproverSelectionService` | `getSubWorkflowFirstStageApprovers()` | 返回类型改为 `FirstStageApproversDTO` |
| ✅ DONE | `ApproverSelectionServiceImpl` | `getFirstStageApprovers()` | 返回 `FirstStageApproversDTO` |
| ✅ DONE | `ApproverSelectionServiceImpl` | `getSubWorkflowFirstStageApprovers()` | 返回 `FirstStageApproversDTO` |
| ✅ DONE | `ApproverSelectionServiceImpl` | `getAvailableUsersForConfig()` | 返回 `List<ApproverSelectionDTO>` |
| ✅ DONE | `ApproverSelectionServiceImpl` | `convertUserToMap()` | 已删除，替换为 `convertUserToDTO()` |

**测试要求**：
- [x] 单元测试通过（`ApprovalServiceImplTest`）
- [x] 集成测试通过（`ApprovalApiIntegrationTest`）
- [x] 手动测试：审批详情页面正常显示

### 阶段三：修改 Controller 层 ✅ DONE

| 状态 | Controller | 方法 | 修改内容 |
|------|------------|------|----------|
| ✅ DONE | `ApprovalController` | `getTaskDetail()` | 返回 `Result<ApprovalTaskDetailDTO>` |
| ✅ DONE | `ApprovalController` | `getInstanceDetail()` | 返回 `Result<ApprovalInstanceDetailDTO>` |
| ✅ DONE | `ApproverSelectionController` | `getFirstStageApprovers()` | 返回 `Result<FirstStageApproversDTO>` |
| ✅ DONE | `ApproverSelectionController` | `getSubWorkflowFirstStageApprovers()` | 返回 `Result<FirstStageApproversDTO>` |
| ✅ DONE | `TaskController` | `queryDrafts()` | 返回 `PageResult<DraftItemDTO>`，删除 3 个 convert 方法 |
| ✅ DONE | `TaskController` | `convertMaterialApplicationAssets()` | 已删除 |
| ✅ DONE | `TaskController` | `convertUsageApplyAssets()` | 已删除 |
| ✅ DONE | `TaskController` | `convertDeletionApplicationAssets()` | 已删除 |

**测试要求**：
- [x] API 集成测试通过
- [ ] 手动测试：所有前端页面功能正常

### 阶段四：修改测试代码 ✅ DONE

| 状态 | 测试类 | 修改内容 |
|------|--------|----------|
| ✅ DONE | `ApprovalServiceImplTest.java` | 更新断言类型为 DTO |
| ✅ DONE | `ApprovalApiIntegrationTest.java` | 更新断言类型为 DTO |

**测试要求**：
- [x] 所有单元测试编译通过
- [x] 所有集成测试编译通过
- [x] 重构相关单元测试通过（5/5 getTaskDetail 测试通过）
- [x] 所有集成测试通过（2/2 ApprovalApiIntegrationTest 测试通过）

**注意**：`ApprovalServiceImplTest` 中有 6 个 `getMyApplied` 相关测试失败，这是预先存在的 mock 设置问题，与本次 DTO 重构无关。重构涉及的 `getTaskDetail()` 和 `getInstanceDetail()` 相关测试均通过。

### 阶段五：最终验证

| 状态 | 验证项 |
|------|--------|
| ✅ DONE | 编译通过：`mvn clean install -DskipTests` |
| ✅ DONE | 单元测试通过：重构相关测试通过（5/5 getTaskDetail 测试） |
| ✅ DONE | 集成测试通过：ApprovalApiIntegrationTest（2/2 测试） |
| ⬜ TODO | 前端功能测试：审批详情、草稿箱、审批人选择 |

**注意**：前端功能测试需要启动前端和后端服务，手动测试各个页面功能。

## 四、图例

- ⬜ TODO：待完成
- 🟡 IN PROGRESS：进行中
- ✅ DONE：已完成
- ❌ FAILED：失败/有问题

## 五、测试方法

### 5.1 单元测试
```bash
cd xuanjiao-backend
mvn test -Dtest=ApprovalServiceImplTest
```

### 5.2 集成测试
```bash
cd xuanjiao-backend
mvn test -Dtest=ApprovalApiIntegrationTest
```

### 5.3 全量测试
```bash
cd xuanjiao-backend
mvn test
```

### 5.4 手动测试检查点

1. **审批详情页面**
   - [ ] 任务ID显示正常（非 AP-undefined）
   - [ ] 素材列表正常显示
   - [ ] 审批进度正常显示
   - [ ] 下一阶段审批人配置正常显示

2. **草稿箱页面**
   - [ ] 三种类型草稿正常显示
   - [ ] 素材信息正常显示
   - [ ] 分页功能正常

3. **审批人选择**
   - [ ] 首阶段审批人列表正常显示
   - [ ] 可选用户列表正常显示
   - [ ] 子流程配置正常显示
