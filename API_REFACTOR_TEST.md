# Mapper重构API一致性测试文档

## 测试目的
验证Mapper重构后，API返回数据与重构前保持一致。

## 版本信息

| 版本 | 分支/Commit | 说明 |
|------|-------------|------|
| 旧版本 | `refactor/before-mapper-refactor` | Mapper重构前（使用BaseMapper + LambdaQueryWrapper） |
| 新版本 | `refactor/after-mapper-refactor` | Mapper重构后（显式Mapper + Query对象） |

---

## 分支创建说明

```bash
# 1. 创建旧版本分支（Mapper重构前）
git checkout 8555a7c
git checkout -b refactor/before-mapper-refactor
git push -u origin refactor/before-mapper-refactor

# 2. 切换到当前分支
git checkout feat/2b_improved

# 3. 暂存当前修改（Mapper重构的修改）
git add -A
git stash

# 4. 创建新版本分支
git checkout -b refactor/after-mapper-refactor
git stash pop
git add -A
git commit -m "feat: Mapper refactoring - explicit Query + XML"
git push -u origin refactor/after-mapper-refactor

# 5. 切换回开发分支
git checkout feat/2b_improved
```

---

## 测试环境准备

### 1. 启动旧版本服务
```bash
cd xuanjiao-backend
git checkout refactor/before-mapper-refactor
mvn clean compile -DskipTests
mvn spring-boot:run -pl xuanjiao-start
```

### 2. 准备测试数据
```sql
SELECT id, title, status FROM material_application LIMIT 5;
SELECT id, name, status FROM asset LIMIT 5;
```

### 3. 记录旧版本响应
```bash
# 创建测试结果目录
mkdir -p test_result

# 素材申请单详情
curl -X POST "http://localhost:8080/api/material-application/getDetail" \
  -H "Content-Type: application/json" \
  -d '{"id": 88}' > test_result/before_material_detail.json

# 其他API...
```

### 4. 启动新版本服务
```bash
git checkout refactor/after-mapper-refactor
mvn clean compile -DskipTests
mvn spring-boot:run -pl xuanjiao-start
```

### 5. 记录新版本响应
```bash
curl -X POST "http://localhost:8080/api/material-application/getDetail" \
  -H "Content-Type: application/json" \
  -d '{"id": 88}' > test_result/after_material_detail.json
```

### 6. 对比结果
```bash
jq -S test_result/before_material_detail.json > test_result/before_sorted.json
jq -S test_result/after_material_detail.json > test_result/after_sorted.json
diff test_result/before_sorted.json test_result/after_sorted.json
```

---

## 一、ApprovalController（9个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 1 | `POST /approval/getMyTasks` | `{"pageNum":1,"pageSize":10}` | ApprovalTaskMapper |
| 2 | `POST /approval/getMyApplied` | `{"pageNum":1,"pageSize":10}` | ApprovalTaskMapper |
| 3 | `POST /approval/tasks/{id}/approve` | Body: approveCmd | ApprovalProgressMapper |
| 4 | `POST /approval/tasks/{id}/return` | Body: returnCmd | ApprovalProgressMapper |
| 5 | `POST /approval/getTaskDetail` | `{"id":1}` | ApprovalTaskMapper |
| 6 | `POST /approval/getInstanceDetail` | `{"id":1}` | ApprovalInstanceMapper |
| 7 | `POST /approval/instances/{id}/withdraw` | - | ApprovalProgressMapper |
| 8 | `POST /approval/tasks/{id}/restart-sub-workflow` | - | ApprovalTaskMapper |

---

## 二、AssetController（10个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 9 | `POST /asset/upload` | multipart/form-data | AssetMapper.insert |
| 10 | `POST /asset/getDetail` | `{"id":1}` | AssetMapper.selectById |
| 11 | `POST /asset/list` | `{"pageNum":1,"pageSize":10}` | AssetMapper.selectList/selectCount |
| 12 | `POST /asset/getMyApproved` | `{"pageNum":1,"pageSize":10}` | AssetMapper.selectList |
| 13 | `POST /asset/adminDelete` | `{"id":1}` | AssetMapper.deleteById |
| 14 | `POST /asset/admin/trigger-cleanup` | - | AssetMapper |
| 15 | `POST /asset/adjustDeleteTime` | `{"id":1}` | AssetMapper.updateById |
| 16 | `GET /asset/preview/{id}` | - | AssetMapper.selectById |
| 17 | `GET /asset/download/{id}` | - | AssetMapper.selectById |

---

## 三、TagController（4个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 18 | `POST /tag/create` | Body: tagCmd | TagMapper.insert |
| 19 | `POST /tag/getList` | `{"pageNum":1,"pageSize":10}` | TagMapper.selectList |
| 20 | `POST /tag/getListByCategory` | `{"categoryId":1}` | TagMapper.selectList |
| 21 | `POST /tag/delete` | `{"id":1}` | TagMapper.deleteById |

---

## 四、AuthController（2个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 22 | `POST /auth/login` | Body: loginCmd | UserMapper |
| 23 | `POST /auth/logout` | - | - |

---

## 五、AssetDeletionController（7个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 24 | `POST /asset-deletion/create` | Body: createCmd | AssetDeletionAssetMapper.insert |
| 25 | `POST /asset-deletion/update` | Body: updateCmd | AssetDeletionAssetMapper.updateById |
| 26 | `POST /asset-deletion/{id}/submit` | - | AssetDeletionAssetMapper |
| 27 | `POST /asset-deletion/getDetail` | `{"id":1}` | AssetDeletionAssetMapper.selectList |
| 28 | `POST /asset-deletion/getMyApplications` | `{"pageNum":1,"pageSize":10}` | AssetDeletionAssetMapper.selectList |
| 29 | `POST /asset-deletion/delete` | `{"id":1}` | AssetDeletionAssetMapper.deleteById |
| 30 | `POST /asset-deletion/{id}/copy` | - | AssetDeletionAssetMapper |

---

## 六、DeptController（7个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 31 | `POST /dept/getList` | `{"pageNum":1,"pageSize":10}` | DeptMapper.selectList |
| 32 | `POST /dept/getTree` | `{}` | DeptMapper.selectList |
| 33 | `POST /dept/getDetail` | `{"id":1}` | DeptMapper.selectById |
| 34 | `POST /dept/create` | Body: createCmd | DeptMapper.insert |
| 35 | `POST /dept/update` | Body: updateCmd | DeptMapper.updateById |
| 36 | `POST /dept/delete` | `{"id":1}` | DeptMapper.deleteById |
| 37 | `GET /dept/generate-code` | - | DeptMapper |

---

## 七、MaterialApplicationController（9个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 38 | `POST /material-application/create` | Body: createCmd | MaterialApplicationMapper.insert |
| 39 | `POST /material-application/update` | Body: updateCmd | MaterialApplicationMapper.updateById |
| 40 | `POST /material-application/{id}/submit` | - | MaterialApplicationMapper |
| 41 | `POST /material-application/delete` | `{"id":1}` | MaterialApplicationMapper.deleteById |
| 42 | `POST /material-application/getDetail` | `{"id":88}` | MaterialApplicationMapper.selectById |
| 43 | `POST /material-application/getDrafts` | `{"pageNum":1,"pageSize":10}` | MaterialApplicationMapper.selectList/selectCount |
| 44 | `POST /material-application/getMyApplications` | `{"pageNum":1,"pageSize":10}` | MaterialApplicationMapper.selectList |
| 45 | `POST /material-application/{id}/copy` | - | MaterialApplicationMapper |
| 46 | `POST /material-application/getDetail` | `{"id":88}` | MaterialApplicationMapper.selectById |

---

## 八、MenuController（8个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 47 | `POST /menu/getTree` | `{}` | MenuMapper.selectList |
| 48 | `POST /menu/getCurrent` | - | MenuMapper |
| 49 | `POST /menu/getDetail` | `{"id":1}` | MenuMapper.selectById |
| 50 | `POST /menu/create` | Body: createCmd | MenuMapper.insert |
| 51 | `POST /menu/update` | Body: updateCmd | MenuMapper.updateById |
| 52 | `POST /menu/delete` | `{"id":1}` | MenuMapper.deleteById |
| 53 | `POST /menu/assign` | Body: assignCmd | MenuMapper |
| 54 | `POST /menu/getRoleMenus` | `{"roleId":1}` | MenuMapper.selectList |

---

## 九、RoleController（7个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 55 | `POST /role/getList` | `{"pageNum":1,"pageSize":10}` | RoleMapper.selectList |
| 56 | `POST /role/getDetail` | `{"id":1}` | RoleMapper.selectById |
| 57 | `POST /role/create` | Body: createCmd | RoleMapper.insert |
| 58 | `POST /role/update` | Body: updateCmd | RoleMapper.updateById |
| 59 | `POST /role/delete` | `{"id":1}` | RoleMapper.deleteById |
| 60 | `POST /role/{roleId}/menus` | - | RoleMenuMapper |
| 61 | `POST /role/getRoleMenus` | `{"id":1}` | RoleMenuMapper.selectList |

---

## 十、TaskController（1个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 62 | `POST /task/queryDrafts` | `{"pageNum":1,"pageSize":10}` | MaterialApplicationMapper |

---

## 十一、UsageApplyController（12个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 63 | `POST /usage-apply/apply` | Body: applyCmd | UsageApplyMapper.insert, UsageApplyAssetMapper.insert |
| 64 | `GET /usage-apply/my-applications` | - | UsageApplyMapper |
| 65 | `POST /usage-apply/draft` | Body: draftCmd | UsageApplyMapper.insert |
| 66 | `POST /usage-apply/update` | Body: updateCmd | UsageApplyMapper.updateById |
| 67 | `POST /usage-apply/{id}/submit` | - | UsageApplyMapper |
| 68 | `POST /usage-apply/delete` | `{"id":1}` | UsageApplyMapper.deleteById |
| 69 | `POST /usage-apply/getDetail` | `{"id":1}` | UsageApplyMapper.selectById, UsageApplyAssetMapper.selectList |
| 70 | `POST /usage-apply/getDrafts` | `{"pageNum":1,"pageSize":10}` | UsageApplyMapper.selectList |
| 71 | `POST /usage-apply/getMyApplications` | `{"pageNum":1,"pageSize":10}` | UsageApplyMapper.selectList |
| 72 | `POST /usage-apply/canUseAsset` | `{"assetId":1}` | UsageApplyAssetMapper |
| 73 | `POST /usage-apply/{id}/copy` | - | UsageApplyMapper |
| 74 | `POST /usage-apply/getDetail` | `{"id":1}` | UsageApplyMapper.selectById |

---

## 十二、UsageLogController（2个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 75 | `POST /usage-log/queryLogs` | `{"pageNum":1,"pageSize":10}` | UsageLogMapper.selectList |
| 76 | `POST /usage-log/getAssetUsageLogs` | `{"assetId":1}` | UsageLogMapper.selectList |

---

## 十三、UserController（8个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 77 | `GET /user/current` | - | UserMapper |
| 78 | `POST /user/getList` | `{"pageNum":1,"pageSize":10}` | UserMapper.selectList |
| 79 | `POST /user/getListWithFilter` | Body: filterCmd | UserMapper.selectList |
| 80 | `POST /user/getDefaultFilterDept` | - | UserMapper |
| 81 | `POST /user/getDetail` | `{"id":1}` | UserMapper.selectById |
| 82 | `POST /user/create` | Body: createCmd | UserMapper.insert |
| 83 | `POST /user/update` | Body: updateCmd | UserMapper.updateById |
| 84 | `POST /user/delete` | `{"id":1}` | UserMapper.deleteById |

---

## 十四、ApproverSelectionController（6个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 85 | `POST /approver-selection/getApprovalProgress` | `{"instanceId":1}` | UserMapper, WorkflowMapper |
| 86 | `POST /approver-selection/getFirstStageApprovers` | `{"workflowId":1}` | UserMapper.selectList |
| 87 | `POST /approver-selection/select-first-stage-approvers-with-subworkflows` | Body: selectCmd | UserMapper |
| 88 | `POST /approver-selection/select-next-stage-approvers-with-subworkflows` | Body: selectCmd | UserMapper |
| 89 | `POST /approver-selection/getSubWorkflowFirstStageApprovers` | `{"workflowId":1}` | UserMapper |
| 90 | `POST /approver-selection/getTaskDetail` | `{"taskId":1}` | UserMapper |

---

## 十五、WorkflowController（9个API）

| 序号 | 接口 | 请求示例 | 修改相关Mapper |
|------|------|----------|---------------|
| 91 | `POST /workflow/getList` | `{}` | WorkflowMapper.selectList |
| 92 | `POST /workflow/getDetail` | `{"id":1}` | WorkflowMapper, WorkflowStageMapper, StageApproverMapper |
| 93 | `POST /workflow/create` | Body: createCmd | WorkflowMapper.insert |
| 94 | `POST /workflow/update` | Body: updateCmd | WorkflowMapper.updateById |
| 95 | `POST /workflow/updateStatus` | `{"id":1,"status":"ENABLED"}` | WorkflowMapper.updateById |
| 96 | `POST /workflow/delete` | `{"id":1}` | WorkflowMapper.deleteById |
| 97 | `POST /workflow/bindRole` | Body: bindCmd | WorkflowMapper |
| 98 | `POST /workflow/unbindRole` | Body: unbindCmd | WorkflowMapper |
| 99 | `POST /workflow/{id}/copy` | - | WorkflowMapper |

---

## 测试记录表

| 序号 | API接口 | Controller | 请求示例 | 旧版本结果 | 新版本结果 | 是否一致 | 备注 |
|------|---------|------------|----------|-----------|-----------|---------|------|
| 1 | /approval/getMyTasks | ApprovalController | `{"pageNum":1,"pageSize":10}` | | | ☐ | |
| 2 | /approval/getMyApplied | ApprovalController | `{"pageNum":1,"pageSize":10}` | | | ☐ | |
| 3 | /approval/tasks/{id}/approve | ApprovalController | Body | | | ☐ | |
| 4 | /approval/tasks/{id}/return | ApprovalController | Body | | | ☐ | |
| 5 | /approval/getTaskDetail | ApprovalController | `{"id":1}` | | | ☐ | |
| 6 | /approval/getInstanceDetail | ApprovalController | `{"id":1}` | | | ☐ | |
| 7 | /approval/instances/{id}/withdraw | ApprovalController | - | | | ☐ | |
| 8 | /approval/tasks/{id}/restart-sub-workflow | ApprovalController | - | | | ☐ | |
| 9 | /asset/upload | AssetController | multipart | | | ☐ | |
| 10 | /asset/getDetail | AssetController | `{"id":1}` | | | ☐ | |
| 11 | /asset/list | AssetController | `{"pageNum":1}` | | | ☐ | |
| 12 | /asset/getMyApproved | AssetController | `{"pageNum":1}` | | | ☐ | |
| 13 | /asset/adminDelete | AssetController | `{"id":1}` | | | ☐ | |
| 14 | /asset/admin/trigger-cleanup | AssetController | - | | | ☐ | |
| 15 | /asset/adjustDeleteTime | AssetController | `{"id":1}` | | | ☐ | |
| 16 | /asset/preview/{id} | AssetController | GET | | | ☐ | |
| 17 | /asset/download/{id} | AssetController | GET | | | ☐ | |
| 18 | /tag/create | TagController | Body | | | ☐ | |
| 19 | /tag/getList | TagController | `{"pageNum":1}` | | | ☐ | |
| 20 | /tag/getListByCategory | TagController | `{"categoryId":1}` | | | ☐ | |
| 21 | /tag/delete | TagController | `{"id":1}` | | | ☐ | |
| 22 | /auth/login | AuthController | Body | | | ☐ | |
| 23 | /auth/logout | AuthController | - | | | ☐ | |
| 24 | /asset-deletion/create | AssetDeletionController | Body | | | ☐ | |
| 25 | /asset-deletion/update | AssetDeletionController | Body | | | ☐ | |
| 26 | /asset-deletion/{id}/submit | AssetDeletionController | - | | | ☐ | |
| 27 | /asset-deletion/getDetail | AssetDeletionController | `{"id":1}` | | | ☐ | |
| 28 | /asset-deletion/getMyApplications | AssetDeletionController | `{"pageNum":1}` | | | ☐ | |
| 29 | /asset-deletion/delete | AssetDeletionController | `{"id":1}` | | | ☐ | |
| 30 | /asset-deletion/{id}/copy | AssetDeletionController | - | | | ☐ | |
| 31 | /dept/getList | DeptController | `{"pageNum":1}` | | | ☐ | |
| 32 | /dept/getTree | DeptController | `{}` | | | ☐ | |
| 33 | /dept/getDetail | DeptController | `{"id":1}` | | | ☐ | |
| 34 | /dept/create | DeptController | Body | | | ☐ | |
| 35 | /dept/update | DeptController | Body | | | ☐ | |
| 36 | /dept/delete | DeptController | `{"id":1}` | | | ☐ | |
| 37 | /dept/generate-code | DeptController | GET | | | ☐ | |
| 38 | /material-application/create | MaterialApplicationController | Body | | | ☐ | |
| 39 | /material-application/update | MaterialApplicationController | Body | | | ☐ | |
| 40 | /material-application/{id}/submit | MaterialApplicationController | - | | | ☐ | |
| 41 | /material-application/delete | MaterialApplicationController | `{"id":1}` | | | ☐ | |
| 42 | /material-application/getDetail | MaterialApplicationController | `{"id":88}` | | | ☐ | |
| 43 | /material-application/getDrafts | MaterialApplicationController | `{"pageNum":1}` | | | ☐ | |
| 44 | /material-application/getMyApplications | MaterialApplicationController | `{"pageNum":1}` | | | ☐ | |
| 45 | /material-application/{id}/copy | MaterialApplicationController | - | | | ☐ | |
| 46 | /menu/getTree | MenuController | `{}` | | | ☐ | |
| 47 | /menu/getCurrent | MenuController | - | | | ☐ | |
| 48 | /menu/getDetail | MenuController | `{"id":1}` | | | ☐ | |
| 49 | /menu/create | MenuController | Body | | | ☐ | |
| 50 | /menu/update | MenuController | Body | | | ☐ | |
| 51 | /menu/delete | MenuController | `{"id":1}` | | | ☐ | |
| 52 | /menu/assign | MenuController | Body | | | ☐ | |
| 53 | /menu/getRoleMenus | MenuController | `{"roleId":1}` | | | ☐ | |
| 54 | /role/getList | RoleController | `{"pageNum":1}` | | | ☐ | |
| 55 | /role/getDetail | RoleController | `{"id":1}` | | | ☐ | |
| 56 | /role/create | RoleController | Body | | | ☐ | |
| 57 | /role/update | RoleController | Body | | | ☐ | |
| 58 | /role/delete | RoleController | `{"id":1}` | | | ☐ | |
| 59 | /role/{roleId}/menus | RoleController | - | | | ☐ | |
| 60 | /role/getRoleMenus | RoleController | `{"id":1}` | | | ☐ | |
| 61 | /task/queryDrafts | TaskController | `{"pageNum":1}` | | | ☐ | |
| 62 | /usage-apply/apply | UsageApplyController | Body | | | ☐ | |
| 63 | /usage-apply/my-applications | UsageApplyController | GET | | | ☐ | |
| 64 | /usage-apply/draft | UsageApplyController | Body | | | ☐ | |
| 65 | /usage-apply/update | UsageApplyController | Body | | | ☐ | |
| 66 | /usage-apply/{id}/submit | UsageApplyController | - | | | ☐ | |
| 67 | /usage-apply/delete | UsageApplyController | `{"id":1}` | | | ☐ | |
| 68 | /usage-apply/getDetail | UsageApplyController | `{"id":1}` | | | ☐ | |
| 69 | /usage-apply/getDrafts | UsageApplyController | `{"pageNum":1}` | | | ☐ | |
| 70 | /usage-apply/getMyApplications | UsageApplyController | `{"pageNum":1}` | | | ☐ | |
| 71 | /usage-apply/canUseAsset | UsageApplyController | `{"assetId":1}` | | | ☐ | |
| 72 | /usage-apply/{id}/copy | UsageApplyController | - | | | ☐ | |
| 73 | /usage-log/queryLogs | UsageLogController | `{"pageNum":1}` | | | ☐ | |
| 74 | /usage-log/getAssetUsageLogs | UsageLogController | `{"assetId":1}` | | | ☐ | |
| 75 | /user/current | UserController | GET | | | ☐ | |
| 76 | /user/getList | UserController | `{"pageNum":1}` | | | ☐ | |
| 77 | /user/getListWithFilter | UserController | Body | | | ☐ | |
| 78 | /user/getDefaultFilterDept | UserController | - | | | ☐ | |
| 79 | /user/getDetail | UserController | `{"id":1}` | | | ☐ | |
| 80 | /user/create | UserController | Body | | | ☐ | |
| 81 | /user/update | UserController | Body | | | ☐ | |
| 82 | /user/delete | UserController | `{"id":1}` | | | ☐ | |
| 83 | /approver-selection/getApprovalProgress | ApproverSelectionController | `{"instanceId":1}` | | | ☐ | |
| 84 | /approver-selection/getFirstStageApprovers | ApproverSelectionController | `{"workflowId":1}` | | | ☐ | |
| 85 | /approver-selection/select-first-stage-approvers-with-subworkflows | ApproverSelectionController | Body | | | ☐ | |
| 86 | /approver-selection/select-next-stage-approvers-with-subworkflows | ApproverSelectionController | Body | | | ☐ | |
| 87 | /approver-selection/getSubWorkflowFirstStageApprovers | ApproverSelectionController | `{"workflowId":1}` | | | ☐ | |
| 88 | /approver-selection/getTaskDetail | ApproverSelectionController | `{"taskId":1}` | | | ☐ | |
| 89 | /workflow/getList | WorkflowController | `{}` | | | ☐ | |
| 90 | /workflow/getDetail | WorkflowController | `{"id":1}` | | | ☐ | |
| 91 | /workflow/create | WorkflowController | Body | | | ☐ | |
| 92 | /workflow/update | WorkflowController | Body | | | ☐ | |
| 93 | /workflow/updateStatus | WorkflowController | `{"id":1}` | | | ☐ | |
| 94 | /workflow/delete | WorkflowController | `{"id":1}` | | | ☐ | |
| 95 | /workflow/bindRole | WorkflowController | Body | | | ☐ | |
| 96 | /workflow/unbindRole | WorkflowController | Body | | | ☐ | |
| 97 | /workflow/{id}/copy | WorkflowController | - | | | ☐ | |

---

## 差异记录模板

**API**: ____________
**Controller**: ____________
**请求**: ____________

**旧版本返回**:
```json

```

**新版本返回**:
```json

```

**差异分析**:
- 差异字段: ____________
- 差异原因: ____________
- 是否需要修复: ☐是 ☐否

---

## 测试结果统计

| 分类 | 数量 |
|------|------|
| 总测试API数 | **97** |
| 一致 | 0 |
| 不一致 | 0 |
| 待测试 | 97 |

**结论**: ☐通过 ☐不通过

---

## 测试执行人: ____________

## 测试日期: ____________

---

## 附录：对比命令

```bash
# 安装jq (Windows)
choco install jq

# 对比JSON
jq -S before.json > before_sorted.json
jq -S after.json > after_sorted.json
diff before_sorted.json after_sorted.json

# 对比字段
jq 'keys' before_sorted.json
jq 'keys' after_sorted.json

# 对比特定字段
jq '.data.items[0].assets' before_sorted.json
jq '.data.items[0].assets' after_sorted.json
```

---

## Mapper修改对照表

| Mapper | 修改文件 | 修改内容 |
|--------|---------|---------|
| ApprovalInstanceMapper | Infrastructure | 重构为显式方法 |
| ApprovalProgressMapper | Infrastructure | 重构为显式方法 |
| ApprovalTaskMapper | Infrastructure | 重构为显式方法 |
| AssetMapper | Infrastructure | 重构为显式方法 + deleted处理 |
| AssetTagMapper | Infrastructure | 重构为显式方法 |
| TagMapper | Infrastructure | 重构为显式方法 |
| DeptMapper | Infrastructure | 重构为显式方法 |
| MaterialApplicationMapper | Infrastructure | 重构为显式方法 + deleted处理 |
| MenuMapper | Infrastructure | 重构为显式方法 |
| RoleMapper | Infrastructure | 重构为显式方法 |
| RoleMenuMapper | Infrastructure | 重构为显式方法 |
| UsageApplyMapper | Infrastructure | 重构为显式方法 |
| UsageApplyAssetMapper | Infrastructure | 重构为显式方法 |
| UsageLogMapper | Infrastructure | 重构为显式方法 |
| UserMapper | Infrastructure | 重构为显式方法 |
| WorkflowMapper | Infrastructure | 重构为显式方法 |
| WorkflowStageMapper | Infrastructure | 重构为显式方法 |
| StageApproverMapper | Infrastructure | 重构为显式方法 |
| AssetDeletionAssetMapper | Infrastructure | 重构为显式方法 |
| OperationLogMapper | Infrastructure | 重构为显式方法 |

---

## 测试中断后继续提示词

```
根据 API_REFACTOR_TEST.md 文档继续 Mapper 重构一致性测试：

【当前状态】
- 已完成：___个API测试
- 待测试：___个API测试
- 上次测试到序号：___（如：测试到第35个API）

【下一步操作】

1. 启动旧版本服务
   git checkout refactor/before-mapper-refactor
   mvn spring-boot:run -pl xuanjiao-start

2. 记录旧版本响应（从序号___开始）
   curl -X POST "http://localhost:8080/api/..." > test_result/before_序号_接口名.json

3. 启动新版本服务
   git checkout refactor/after-mapper-refactor
   mvn spring-boot:run -pl xuanjiao-start

4. 记录新版本响应
   curl -X POST "http://localhost:8080/api/..." > test_result/after_序号_接口名.json

5. 对比结果
   jq -S test_result/before_*.json > test_result/before_sorted.json
   jq -S test_result/after_*.json > test_result/after_sorted.json
   diff test_result/before_sorted.json test_result/after_sorted.json

6. 在文档中标记结果（"一致"或"不相同"，不相同则记录两个返回结果）

【快速定位】
从文档"测试记录表"部分找到序号___，继续填写该行。
```

---

## 当前测试进度记录

| 项目 | 值 |
|------|-----|
| 已完成测试数 | |
| 待测试数 | 97 |
| 上次测试序号 | |
| 测试执行人 | |
| 最后测试日期 | |
