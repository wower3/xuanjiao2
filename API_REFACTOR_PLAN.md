# 接口扁平化改造计划

> 将所有 RESTful 风格接口改造为 POST 请求，使用 Qry/Cmd 对象传递参数

## 改造原则

| 项目 | 说明 |
|-----|------|
| **路径规则** | 全部扁平化 (如 `GET /asset/{id}` → `POST /asset/getDetail`) |
| **DTO命名** | 查询类用 `Qry` 后缀，操作类用 `Cmd` 后缀 |
| **过渡期** | 立即删除原接口（前后端同步发布） |
| **改造顺序** | 按业务重要性（先边缘功能，后核心流程） |

---

## 改造总览

| 阶段 | Controller | 接口数 | Qry对象 | Cmd对象 | 风险等级 |
|-----|------------|-------|---------|---------|---------|
| 一 | TagController, UsageLogController | 5 | 5 | 1 | 低 |
| 二 | DeptController, MenuController, RoleController | 17 | 16 | 8 | 低 |
| 三 | TaskController, UserController, AuthController | 6 | 6 | 3 | 中 |
| 四 | ApproverSelectionController, WorkflowController | 9 | 9 | 6 | 中 |
| 五 | ApprovalController | 4 | 4 | 0 | 高 |
| 六 | AssetController, MaterialApplicationController, UsageApplyController, AssetDeletionController | 22 | 21 | 10 | 高 |
| **总计** | **15个Controller** | **57个** | **61个** | **28个** | - |

---

## 阶段一：标签与日志模块

### 1.1 TagController (`xuanjiao-adapter/.../asset/`)

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /tag/list` | `POST /tag/getList` | 创建 `TagGetListQry` |
| 2 | `GET /tag/list/{category}` | `POST /tag/getListByCategory` | 创建 `TagGetListByCategoryQry` |
| 3 | `DELETE /tag/{id}` | `POST /tag/delete` | 创建 `TagDeleteCmd` |

**需要创建的DTO：**
```java
// xuanjiao-client/src/main/java/com/xuanjiao/client/dto/TagGetListQry.java
@Data
public class TagGetListQry {}

// xuanjiao-client/src/main/java/com/xuanjiao/client/dto/TagGetListByCategoryQry.java
@Data
public class TagGetListByCategoryQry {
    private String category;
}

// xuanjiao-client/src/main/java/com/xuanjiao/client/dto/TagDeleteCmd.java
@Data
public class TagDeleteCmd {
    @NotNull(message = "ID不能为空")
    private Long id;
}
```

**前端改造 (`xuanjiao-frontend/src/api/tag.ts`)：**
```typescript
// 原: request.get('/tag/list')
// 新:
export function getTagList() {
  return request.post('/tag/getList', {})
}

// 原: request.get(`/tag/list/${category}`)
// 新:
export function getTagsByCategory(category: string) {
  return request.post('/tag/getListByCategory', { category })
}

// 原: request.delete(`/tag/${id}`)
// 新:
export function deleteTag(id: number) {
  return request.post('/tag/delete', { id })
}
```

**测试要点：**
- [ ] 标签列表正常显示
- [ ] 按分类筛选正常
- [ ] 删除标签成功

---

### 1.2 UsageLogController (`xuanjiao-adapter/.../log/`)

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /log/list` | `POST /log/queryLogs` | 创建 `LogQueryLogsQry` |
| 2 | `GET /log/asset/{assetId}/usage-logs` | `POST /log/getAssetUsageLogs` | 创建 `LogGetAssetUsageLogsQry` |

**需要创建的DTO：**
```java
// xuanjiao-client/src/main/java/com/xuanjiao/client/dto/LogQueryLogsQry.java
@Data
public class LogQueryLogsQry {
    private String action;
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}

// xuanjiao-client/src/main/java/com/xuanjiao/client/dto/LogGetAssetUsageLogsQry.java
@Data
public class LogGetAssetUsageLogsQry {
    @NotNull(message = "素材ID不能为空")
    private Long assetId;
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
```

**前端改造：**
```typescript
// xuanjiao-frontend/src/api/log.ts
export function getLogList(params: { action?: string; pageNum?: number; pageSize?: number }) {
  return request.post('/log/queryLogs', params)
}

// xuanjiao-frontend/src/api/usageLog.ts
export function getAssetUsageLogs(assetId: number, params?: { pageNum?: number; pageSize?: number }) {
  return request.post('/log/getAssetUsageLogs', { assetId, ...params })
}
```

**测试要点：**
- [ ] 日志查询正常
- [ ] 素材使用记录正常显示

---

## 阶段二：部门、菜单、角色模块

### 2.1 DeptController (`xuanjiao-adapter/.../dept/`)

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /dept/list` | `POST /dept/getList` | 创建 `DeptGetListQry` |
| 2 | `GET /dept/tree` | `POST /dept/getTree` | 创建 `DeptGetTreeQry` |
| 3 | `GET /dept/{id}` | `POST /dept/getDetail` | 创建 `DeptGetDetailQry` |
| 4 | `PUT /dept` | `POST /dept/update` | `DeptDTO` → `DeptUpdateCmd` |
| 5 | `DELETE /dept/{id}` | `POST /dept/delete` | 创建 `DeptDeleteCmd` |

**需要创建的DTO：**
```java
// DeptGetListQry.java
@Data
public class DeptGetListQry {}

// DeptGetTreeQry.java
@Data
public class DeptGetTreeQry {}

// DeptGetDetailQry.java
@Data
public class DeptGetDetailQry {
    @NotNull(message = "ID不能为空")
    private Long id;
}

// DeptUpdateCmd.java - 从 DeptDTO 迁移
@Data
public class DeptUpdateCmd {
    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private Integer sort;
    private String description;
}

// DeptDeleteCmd.java
@Data
public class DeptDeleteCmd {
    @NotNull(message = "ID不能为空")
    private Long id;
}
```

**注意：**
- `POST /dept` (save) 保持不变
- `GET /dept/generate-code` 保持不变

**前端改造 (`xuanjiao-frontend/src/api/dept.ts`)：**
```typescript
export function getDeptList() {
  return request.post('/dept/getList', {})
}
export function getDeptTree() {
  return request.post('/dept/getTree', {})
}
export function getDeptById(id: number) {
  return request.post('/dept/getDetail', { id })
}
export function updateDept(data: any) {
  return request.post('/dept/update', data)
}
export function deleteDept(id: number) {
  return request.post('/dept/delete', { id })
}
```

**测试要点：**
- [ ] 部门树正常显示
- [ ] 部门增删改查正常
- [ ] 部门编号生成正常

---

### 2.2 MenuController (`xuanjiao-adapter/.../menu/`)

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /menu/tree` | `POST /menu/getTree` | 创建 `MenuGetTreeQry` |
| 2 | `GET /menu/current` | `POST /menu/getCurrent` | 创建 `MenuGetCurrentQry` |
| 3 | `GET /menu/{id}` | `POST /menu/getDetail` | 创建 `MenuGetDetailQry` |
| 4 | `PUT /menu` | `POST /menu/update` | `MenuCmd` → `MenuUpdateCmd` |
| 5 | `DELETE /menu/{id}` | `POST /menu/delete` | 创建 `MenuDeleteCmd` |
| 6 | `GET /menu/role/{roleId}` | `POST /menu/getRoleMenus` | 创建 `MenuGetRoleMenusQry` |
| 7 | `POST /menu` | `POST /menu/create` | `MenuCmd` → `MenuCreateCmd` |

**需要创建的DTO：**
```java
// MenuGetTreeQry.java, MenuGetCurrentQry.java
@Data
public class MenuGetTreeQry {}
@Data
public class MenuGetCurrentQry {}

// MenuGetDetailQry.java
@Data
public class MenuGetDetailQry {
    @NotNull(message = "ID不能为空")
    private Long id;
}

// MenuGetRoleMenusQry.java
@Data
public class MenuGetRoleMenusQry {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}

// MenuCreateCmd.java, MenuUpdateCmd.java - 从 MenuCmd 迁移
@Data
public class MenuCreateCmd {
    private String name;
    private String path;
    private String component;
    private Integer sort;
    private Long parentId;
    private String icon;
    private Integer menuType;
}
@Data
public class MenuUpdateCmd {
    private Long id;
    // ... 其他字段
}

// MenuDeleteCmd.java
@Data
public class MenuDeleteCmd {
    @NotNull(message = "ID不能为空")
    private Long id;
}
```

**前端改造 (`xuanjiao-frontend/src/api/menu.ts`)：**
```typescript
export function getMenuTree() {
  return request.post('/menu/getTree', {})
}
export function getCurrentMenus() {
  return request.post('/menu/getCurrent', {})
}
export function getMenuById(id: number) {
  return request.post('/menu/getDetail', { id })
}
export function saveMenu(data: any) {
  return request.post('/menu/create', data)
}
export function updateMenu(data: any) {
  return request.post('/menu/update', data)
}
export function deleteMenu(id: number) {
  return request.post('/menu/delete', { id })
}
export function getRoleMenus(roleId: number) {
  return request.post('/menu/getRoleMenus', { roleId })
}
```

---

### 2.3 RoleController (`xuanjiao-adapter/.../role/`)

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /role/list` | `POST /role/getList` | 创建 `RoleGetListQry` |
| 2 | `GET /role/{id}` | `POST /role/getDetail` | 创建 `RoleGetDetailQry` |
| 3 | `PUT /role` | `POST /role/update` | `RoleDTO` → `RoleUpdateCmd` |
| 4 | `DELETE /role/{id}` | `POST /role/delete` | 创建 `RoleDeleteCmd` |
| 5 | `GET /role/{roleId}/menus` | `POST /role/getRoleMenus` | 创建 `RoleGetRoleMenusQry` |
| 6 | `POST /role` | `POST /role/create` | `RoleDTO` → `RoleCreateCmd` |

**前端改造 (`xuanjiao-frontend/src/api/role.ts`)：**
```typescript
export function getRoleList() {
  return request.post('/role/getList', {})
}
export function getRoleById(id: number) {
  return request.post('/role/getDetail', { id })
}
export function createRole(data: any) {
  return request.post('/role/create', data)
}
export function updateRole(data: any) {
  return request.post('/role/update', data)
}
export function deleteRole(id: number) {
  return request.post('/role/delete', { id })
}
export function getRoleMenus(roleId: number) {
  return request.post('/role/getRoleMenus', { roleId })
}
```

---

## 阶段三：用户与认证模块

### 3.1 TaskController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /task/drafts` | `POST /task/queryDrafts` | 创建 `TaskQueryDraftsQry` |

```java
@Data
public class TaskQueryDraftsQry {
    @Min(value = 1)
    private Integer pageNum = 1;
    @Min(value = 1)
    private Integer pageSize = 10;
    private String draftType;
    private String title;
}
```

---

### 3.2 UserController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /user/list` | `POST /user/getList` | 创建 `UserGetListQry` |
| 2 | `GET /user/listWithFilter` | `POST /user/getListWithFilter` | 创建 `UserGetListWithFilterQry` |
| 3 | `GET /user/defaultFilterDept` | `POST /user/getDefaultFilterDept` | 创建 `UserGetDefaultFilterDeptQry` |
| 4 | `DELETE /user/{id}` | `POST /user/delete` | 创建 `UserDeleteCmd` |
| 5 | `POST /user` | `POST /user/create` | `UserDTO` → `UserCreateCmd` |
| 6 | `PUT /user` | `POST /user/update` | `UserDTO` → `UserUpdateCmd` |

---

### 3.3 AuthController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /auth/logout` | `POST /auth/logout` | 创建 `LogoutCmd` |

---

## 阶段四：工作流模块

### 4.1 ApproverSelectionController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /workflow/progress/{instanceId}` | `POST /workflow/getApprovalProgress` | 创建 `WorkflowGetApprovalProgressQry` |
| 2 | `GET /workflow/first-stage-approvers` | `POST /workflow/getFirstStageApprovers` | 创建 `WorkflowGetFirstStageApproversQry` |
| 3 | `GET /workflow/sub-workflow-approvers` | `POST /workflow/getSubWorkflowFirstStageApprovers` | 创建 `WorkflowGetSubWorkflowFirstStageApproversQry` |

---

### 4.2 WorkflowController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /workflow/list` | `POST /workflow/getList` | 创建 `WorkflowGetListQry` |
| 2 | `GET /workflow/{id}` | `POST /workflow/getDetail` | 创建 `WorkflowGetDetailQry` |
| 3 | `PUT /workflow/{id}/status` | `POST /workflow/updateStatus` | 创建 `WorkflowUpdateStatusCmd` |
| 4 | `DELETE /workflow/{id}` | `POST /workflow/delete` | 创建 `WorkflowDeleteCmd` |
| 5 | `PUT /workflow/{id}/bind-role` | `POST /workflow/bindRole` | 创建 `WorkflowBindRoleCmd` |
| 6 | `PUT /workflow/{id}/unbind-role` | `POST /workflow/unbindRole` | 创建 `WorkflowUnbindRoleCmd` |
| 7 | `POST /workflow` | `POST /workflow/create` | `WorkflowDTO` → `WorkflowCreateCmd` |
| 8 | `PUT /workflow` | `POST /workflow/update` | `WorkflowDTO` → `WorkflowUpdateCmd` |

---

## 阶段五：审批模块

### 5.1 ApprovalController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /approval/tasks` | `POST /approval/getMyTasks` | 创建 `ApprovalGetMyTasksQry` |
| 2 | `GET /approval/applied` | `POST /approval/getMyApplied` | 创建 `ApprovalGetMyAppliedQry` |
| 3 | `GET /approval/tasks/{id}/detail` | `POST /approval/getTaskDetail` | 创建 `ApprovalGetTaskDetailQry` |
| 4 | `GET /approval/instances/{id}/detail` | `POST /approval/getInstanceDetail` | 创建 `ApprovalGetInstanceDetailQry` |

---

## 阶段六：素材与申请模块

### 6.1 AssetController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /asset/{id}` | `POST /asset/getDetail` | 创建 `AssetGetDetailQry` |
| 2 | `GET /asset/list` | `POST /asset/getList` | 创建 `AssetGetListQry` |
| 3 | `GET /asset/my-approved` | `POST /asset/getMyApproved` | 创建 `AssetGetMyApprovedQry` |
| 4 | `DELETE /asset/{id}` | `POST /asset/delete` | 创建 `AssetDeleteCmd` |
| 5 | `DELETE /asset/admin/{id}` | `POST /asset/adminDelete` | 创建 `AssetAdminDeleteCmd` |
| 6 | `PUT /asset/admin/{id}/adjust-delete-time` | `POST /asset/adjustDeleteTime` | 创建 `AssetAdjustDeleteTimeCmd` |
| 7 | `GET /asset/preview/{id}` | `POST /asset/preview` | 创建 `AssetPreviewQry` |

**注意：** 以下接口保持不变
- `POST /asset/upload` - 已是POST
- `POST /asset/admin/trigger-cleanup` - 已是POST
- `GET /asset/download/{id}` - 下载接口特殊处理

---

### 6.2 MaterialApplicationController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /material-application/{id}` | `POST /material-application/getDetail` | 创建 `MaterialApplicationGetDetailQry` |
| 2 | `PUT /material-application/{id}` | `POST /material-application/update` | 创建 `MaterialApplicationUpdateCmd` |
| 3 | `DELETE /material-application/{id}` | `POST /material-application/delete` | 创建 `MaterialApplicationDeleteCmd` |
| 4 | `GET /material-application/drafts` | `POST /material-application/getDrafts` | 创建 `MaterialApplicationGetDraftsQry` |
| 5 | `GET /material-application/my` | `POST /material-application/getMyApplications` | 创建 `MaterialApplicationGetMyApplicationsQry` |
| 6 | `POST /material-application` | `POST /material-application/create` | `MaterialApplicationCmd` → `MaterialApplicationCreateCmd` |

---

### 6.3 UsageApplyController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /usage-apply/{id}` | `POST /usage-apply/getDetail` | 创建 `UsageApplyGetDetailQry` |
| 2 | `PUT /usage-apply/{id}` | `POST /usage-apply/update` | 创建 `UsageApplyUpdateCmd` |
| 3 | `DELETE /usage-apply/{id}` | `POST /usage-apply/delete` | 创建 `UsageApplyDeleteCmd` |
| 4 | `GET /usage-apply/drafts` | `POST /usage-apply/getDrafts` | 创建 `UsageApplyGetDraftsQry` |
| 5 | `GET /usage-apply/my` | `POST /usage-apply/getMyApplications` | 创建 `UsageApplyGetMyApplicationsQry` |
| 6 | `GET /usage-apply/can-use/{assetId}` | `POST /usage-apply/canUseAsset` | 创建 `UsageApplyCanUseAssetQry` |
| 7 | `POST /usage-apply/draft` | `POST /usage-apply/createDraft` | `UsageApplyCmd` → `UsageApplyCreateDraftCmd` |

---

### 6.4 AssetDeletionController

| # | 原接口 | 新接口 | DTO改造 |
|---|--------|--------|---------|
| 1 | `GET /deletion/{id}` | `POST /deletion/getDetail` | 创建 `DeletionGetDetailQry` |
| 2 | `PUT /deletion/{id}` | `POST /deletion/update` | 创建 `DeletionUpdateCmd` |
| 3 | `DELETE /deletion/{id}` | `POST /deletion/delete` | 创建 `DeletionDeleteCmd` |
| 4 | `GET /deletion/my` | `POST /deletion/getMyApplications` | 创建 `DeletionGetMyApplicationsQry` |
| 5 | `POST /deletion` | `POST /deletion/create` | `AssetDeletionApplicationCmd` → `DeletionCreateCmd` |

---

## 改造检查清单

### 每个阶段完成后检查

- [ ] 后端DTO对象创建完成
- [ ] 后端Controller接口改造完成
- [ ] 前端API函数改造完成
- [ ] 前端相关页面调用更新
- [ ] 本地测试通过
- [ ] Swagger文档验证

### 测试验证要点

1. **功能测试**：所有增删改查功能正常
2. **参数校验**：@Valid 注解生效
3. **错误处理**：异常情况返回正确错误码
4. **权限控制**：@RequestAttribute("userId") 正常获取
5. **分页查询**：pageNum/pageSize 参数正确

---

## 风险提示

1. **前后端同步发布**：必须确保前后端同时更新，避免调用失败
2. **参数校验**：使用 @Valid 和 JSR-303 注解确保参数合法性
3. **兼容性**：立即删除原接口，无过渡期，需确保新接口完全可用
4. **文件下载**：`GET /asset/download/{id}` 等特殊接口需单独处理
5. **测试覆盖**：每个阶段必须完整测试后再进行下一阶段

---

## 附录：完整DTO清单

### Qry对象（61个）

| 序号 | 类名 | 字段 | 所在阶段 |
|-----|------|------|---------|
| 1 | TagGetListQry | 无 | 1 |
| 2 | TagGetListByCategoryQry | category | 1 |
| 3 | LogQueryLogsQry | action, pageNum, pageSize | 1 |
| 4 | LogGetAssetUsageLogsQry | assetId, pageNum, pageSize | 1 |
| 5 | DeptGetListQry | 无 | 2 |
| 6 | DeptGetTreeQry | 无 | 2 |
| 7 | DeptGetDetailQry | id | 2 |
| 8 | MenuGetTreeQry | 无 | 2 |
| 9 | MenuGetCurrentQry | 无 | 2 |
| 10 | MenuGetDetailQry | id | 2 |
| 11 | MenuGetRoleMenusQry | roleId | 2 |
| 12 | RoleGetListQry | 无 | 2 |
| 13 | RoleGetDetailQry | id | 2 |
| 14 | RoleGetRoleMenusQry | roleId | 2 |
| 15 | TaskQueryDraftsQry | pageNum, pageSize, draftType, title | 3 |
| 16 | UserGetListQry | 无 | 3 |
| 17 | UserGetListWithFilterQry | roleIds, deptId, includeSubDept | 3 |
| 18 | UserGetDefaultFilterDeptQry | 无 | 3 |
| 19 | WorkflowGetApprovalProgressQry | instanceId | 4 |
| 20 | WorkflowGetFirstStageApproversQry | workflowId, applicantId, keyword | 4 |
| 21 | WorkflowGetSubWorkflowFirstStageApproversQry | subWorkflowId, applicantId, keyword | 4 |
| 22 | WorkflowGetListQry | 无 | 4 |
| 23 | WorkflowGetDetailQry | id | 4 |
| 24 | ApprovalGetMyTasksQry | pageNum, pageSize | 5 |
| 25 | ApprovalGetMyAppliedQry | pageNum, pageSize, businessType, forAllUsers, applicantId, deptId, roleType, status | 5 |
| 26 | ApprovalGetTaskDetailQry | id | 5 |
| 27 | ApprovalGetInstanceDetailQry | id | 5 |
| 28 | AssetGetDetailQry | id | 6 |
| 29 | AssetGetListQry | (从AssetQueryCmd迁移) | 6 |
| 30 | AssetGetMyApprovedQry | name, type, pageNum, pageSize | 6 |
| 31 | AssetPreviewQry | id | 6 |
| 32-61 | (其他Qry对象) | ... | ... |

### Cmd对象（28个）

| 序号 | 类名 | 字段 | 所在阶段 |
|-----|------|------|---------|
| 1 | TagDeleteCmd | id | 1 |
| 2 | DeptUpdateCmd | (从DeptDTO迁移) | 2 |
| 3 | DeptDeleteCmd | id | 2 |
| 4 | MenuCreateCmd | (从MenuCmd迁移) | 2 |
| 5 | MenuUpdateCmd | (从MenuCmd迁移) | 2 |
| 6 | MenuDeleteCmd | id | 2 |
| 7 | RoleCreateCmd | (从RoleDTO迁移) | 2 |
| 8 | RoleUpdateCmd | (从RoleDTO迁移) | 2 |
| 9 | RoleDeleteCmd | id | 2 |
| 10 | UserCreateCmd | (从UserDTO迁移) | 3 |
| 11 | UserUpdateCmd | (从UserDTO迁移) | 3 |
| 12 | UserDeleteCmd | id | 3 |
| 13 | LogoutCmd | token | 3 |
| 14-28 | (其他Cmd对象) | ... | ... |

---

*文档生成时间：2025-01-29*
