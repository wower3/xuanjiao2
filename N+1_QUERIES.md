# N+1 查询优化文档

本文档记录需要优化的N+1查询问题及优化标准。

---

## 优化标准

### 什么情况下需要优化

1. **列表查询场景**: 在循环中对每条记录执行独立查询
2. **高频功能**: 待办列表、选择审批人、任务详情等用户频繁使用的功能
3. **数据量大**: 列表可能有多条记录（如100条），循环查询会导致数百次数据库往返

### 优化原则

1. **优先使用JOIN查询**: 在Mapper XML中添加带JOIN的查询方法，一次性获取所有关联数据
2. **分页查询**: 对于数据量大但可分页的场景，优先使用分页减少单次查询数据量
3. **批量预加载**: 对于无法使用JOIN的场景，先批量查询所有需要的数据，再用Map查找
4. **避免循环中的数据库查询**: 循环中只做内存操作，不访问数据库

### 优化目标

将原来的 N+1 次查询优化为 1-3 次查询。

### 优化方式选择

| 场景 | 推荐方式 | 说明 |
|------|---------|------|
| 列表数据量大、无分页 | JOIN查询 | 一次性获取所有关联数据 |
| 列表有分页机制 | 分页+JOIN | 每页只查一页数据 |
| 关联数据复杂难JOIN | 批量预加载 | 先批量查，再用Map查找 |
| 详情页面 | 批量预加载 | 只查一次，数据量可控 |

---

## 优化进度总结

### 已完成优化（7项）

| 编号 | 问题 | 优化方式 | 效果 |
|------|------|---------|------|
| 问题1 | 待办列表/我发起的 | 分页+JOIN | 每页从8次查询减少为2次 |
| 问题2 | 选择审批人列表 | JOIN | 一次性获取审批人及关联信息 |
| 问题4+5 | 用户详情+可用用户列表 | JOIN | 一次性获取用户及部门角色信息 |
| 问题6 | 素材申请列表 | 分页+JOIN | 每页从4次查询减少为2次 |
| 问题12 | 流程列表 | JOIN | 1次查询获取流程及角色名称 |

### 待优化问题（5项）

- 问题3：任务详情（批量预加载，较复杂）
- 问题8：素材使用申请列表转换
- 问题9：素材列表-标签转换
- 问题10：流程设计器-审批人转换
- 问题11：流程设计器-阶段审批人列表

---

## 待优化问题汇总

| 编号 | 问题 | 文件 | 方法 | 优化方式 | 说明 |
|------|------|------|------|---------|------|
| 1 | 待办列表/我发起的 ✅已完成 | ApprovalServiceImpl | getMyTasks() | 分页+JOIN | 优化：使用selectPendingTaskPage方法，每页从8次查询减少为2次 |
| 2 | 选择审批人列表 ✅已完成 | ApproverSelectionServiceImpl | getFirstStageApprovers() | JOIN | 优化：使用selectWithDetails方法，一次性获取审批人及关联信息 |
| 3 | 任务详情 | ApprovalServiceImpl | getTaskDetail() | 批量预加载 | 详情页，涉及子流程，较复杂 |
| 4 | 用户详情转换 ✅已合并到问题5 | ApproverSelectionServiceImpl | convertUserToMap() | JOIN | 辅助问题5，问题5优化后自动解决，使用convertUserWithDetailsToMap替代 |
| 5 | 可用用户列表 ✅已完成 | ApproverSelectionServiceImpl | getAvailableUsersForConfig() | JOIN | 优化：使用selectListWithDetails方法，JOIN获取用户及部门角色信息 |
| 6 | 素材申请列表转换 ✅已完成 | MaterialApplicationServiceImpl | convert() | 分页+JOIN | 优化：使用selectListWithDetails方法，每页从4次查询减少为2次 |
| 8 | 素材使用申请列表转换 | UsageApplyServiceImpl | convert() | JOIN | 申请人名称可用JOIN获取 |
| 9 | 素材列表-标签转换 | AssetServiceImpl | convertWithTags() | JOIN | 一条SQL获取素材及标签 |
| 10 | 流程设计器-审批人转换 | WorkflowServiceImpl | convertApprover() | JOIN | 审批人详情可用JOIN获取 |
| 11 | 流程设计器-阶段审批人列表 | WorkflowServiceImpl | getWorkflowDetail() | JOIN | 与问题10一起优化 |
| **12** | **流程列表 ✅已完成** | **WorkflowServiceImpl** | **list()** | **JOIN** | **已优化：使用WorkflowMapper.xml的selectListWithRoleName方法，1次查询获取流程及角色名称** |

---

## 问题1：待办列表 / 我发起的（高优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/approval/impl/ApprovalServiceImpl.java`
**方法**: `buildTaskInfo()` (行325-404)
**场景**: 分页查询待办任务列表
**影响**: 100条记录会产生600+次查询

**当前状态**: 已有分页机制（每页10-20条）

**优化方式**: 分页 + JOIN查询

**优化方案**: 在 `ApprovalTaskMapper.xml` 中添加带JOIN的分页查询方法 `selectPageWithDetails`，每页数据一次性获取。

**SQL示例**:
```sql
SELECT
    t.id AS task_id, t.status AS task_status, t.create_time, t.task_type,
    i.id AS instance_id, i.business_type, i.business_id, i.applicant_id,
    w.id AS workflow_id, w.name AS workflow_name,
    u.real_name AS applicant_name,
    ma.id AS material_app_id, ma.title AS material_title,
    ad.id AS deletion_id, ad.title AS deletion_title,
    (SELECT COUNT(*) FROM asset a WHERE a.application_id = ma.id) AS asset_count,
    (SELECT a.type FROM asset a WHERE a.application_id = ma.id LIMIT 1) AS asset_type
FROM approval_task t
LEFT JOIN approval_instance i ON t.instance_id = i.id
LEFT JOIN workflow w ON i.workflow_id = w.id
LEFT JOIN sys_user u ON i.applicant_id = u.id
LEFT JOIN material_application ma ON i.business_type = 'MATERIAL_ENTRY' AND i.business_id = ma.id
LEFT JOIN asset_deletion_application ad ON i.business_type = 'ASSET_DELETION' AND i.business_id = ad.id
WHERE t.approver_id = #{userId} AND t.status = 'PENDING'
ORDER BY t.create_time DESC
LIMIT #{offset}, #{pageSize}
```

**涉及Mapper**:
- ApprovalTaskMapper (新增 selectPageWithDetails)
- ApprovalInstanceMapper
- WorkflowMapper
- UserMapper
- MaterialApplicationMapper
- AssetDeletionApplicationMapper
- UsageApplyAssetMapper

---

## 问题2：选择审批人列表（高优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/ApproverSelectionServiceImpl.java`
**方法**: `getFirstStageApprovers()` (行547-635)
**场景**: 选择第一阶段审批人时显示可选审批人列表
**影响**: 10个审批人配置会产生50+次查询

**优化方式**: JOIN查询

**优化方案**: 在 `StageApproverMapper.xml` 中添加带JOIN的查询方法，获取审批人配置及关联的用户/角色/部门信息。

**SQL示例**:
```sql
SELECT
    sa.id AS config_id, sa.approver_type, sa.approver_id, sa.check_secondary_dept,
    u.username, u.real_name, u.dept_id,
    r.name AS role_name,
    d.name AS dept_name
FROM stage_approver sa
LEFT JOIN sys_user u ON sa.approver_type = 'USER' AND sa.approver_id = u.id
LEFT JOIN sys_role r ON sa.approver_type = 'ROLE' AND sa.approver_id = r.id
LEFT JOIN sys_dept d ON sa.approver_type = 'DEPT' AND sa.approver_id = d.id
WHERE sa.stage_id = #{stageId} AND sa.sub_workflow_id IS NULL
ORDER BY sa.id
```

**涉及Mapper**:
- StageApproverMapper (新增 selectWithApproverDetails)
- UserMapper
- RoleMapper
- DeptMapper

---

## 问题3：任务详情（高优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/approval/impl/ApprovalServiceImpl.java`
**方法**: `getTaskDetail()` (行598-903)
**场景**: 查看待办任务详情
**影响**: 每次查看详情会产生50+次查询

**优化方式**: 批量预加载（详情页数据量小，批量预加载更简单）

**当前代码**:
```java
// 获取审批人配置详情 (行678-690)
for (StageApproverDO config : configs) {
    UserDO user = userMapper.selectById(config.getApproverId());      // 1次/审批人
    RoleDO role = roleMapper.selectById(config.getApproverId());    // 1次/审批人
    DeptDO dept = deptMapper.selectById(config.getApproverId());    // 1次/审批人
}

// 获取子流程信息 (行717)
WorkflowDO subWorkflow = workflowMapper.selectById(sw.getSubWorkflowId()); // 1次/子流程

// 获取子流程审批人配置 (行756-768)
for (StageApproverDO subConfig : subApprovers) {
    UserDO user = userMapper.selectById(subConfig.getApproverId());    // 1次/审批人
    RoleDO role = roleMapper.selectById(subConfig.getApproverId());    // 1次/审批人
    DeptDO dept = deptMapper.selectById(subConfig.getApproverId());    // 1次/审批人
}
```

**优化方案**: 创建专门的DTO类封装任务详情相关数据，使用批量查询或JOIN查询获取所有审批人信息。

**涉及Mapper**:
- UserMapper
- RoleMapper
- DeptMapper
- WorkflowMapper

---

## 问题4：用户详情转换（低优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/ApproverSelectionServiceImpl.java`
**方法**: `convertUserToMap()` (行962-983)
**场景**: 将用户DO转换为Map时获取部门和角色名称
**影响**: 每次调用都会产生2次数据库查询

**优化方式**: JOIN（问题5优化后自动解决）

**说明**: 此方法是问题5的辅助方法，当问题5使用JOIN优化后，该方法不再需要。

---

## 问题5：可用用户列表（高优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/ApproverSelectionServiceImpl.java`
**方法**: `getAvailableUsersForConfig()` (行899-957)
**场景**: 获取某配置下的可用用户列表（角色下用户、部门下用户）
**影响**: 10个用户会产生30次查询

**优化方式**: JOIN查询

**当前代码**:
```java
// 按角色查询用户
UserQuery userQuery = new UserQuery();
userQuery.setRoleId(config.getApproverId());
List<UserDO> roleUsers = userMapper.selectList(userQuery);
for (UserDO user : roleUsers) {
    convertUserToMap(user);  // 循环中再查部门和角色
}

// 按部门查询用户
UserQuery userQuery = new UserQuery();
userQuery.setDeptId(config.getApproverId());
List<UserDO> deptUsers = userMapper.selectList(userQuery);
for (UserDO user : deptUsers) {
    convertUserToMap(user);  // 循环中再查部门和角色
}
```

**优化方案**: 在UserMapper.xml中新增带JOIN的查询方法，一次性获取用户及其部门、角色名称。

**SQL示例**:
```sql
-- 按角色查询用户（带部门、角色名称）
SELECT u.*, d.name AS dept_name, r.name AS role_name
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.id
LEFT JOIN sys_role r ON u.role_id = r.id
WHERE u.role_id = #{roleId} AND u.status = 1 AND u.deleted = 0

-- 按部门查询用户（带部门、角色名称）
SELECT u.*, d.name AS dept_name, r.name AS role_name
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.id
LEFT JOIN sys_role r ON u.role_id = r.id
WHERE u.dept_id = #{deptId} AND u.status = 1 AND u.deleted = 0
```

**涉及Mapper**:
- UserMapper (新增 selectListByRoleIdWithDetails, selectListByDeptIdWithDetails)

---

## 问题6：素材申请列表转换（中优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/material/impl/MaterialApplicationServiceImpl.java`
**方法**: `convert()` (行257-299)
**场景**: 素材申请列表转换为DTO
**影响**: 100条记录会产生400+次查询

**当前状态**: 已有分页机制

**优化方式**: 分页 + JOIN查询

**优化方案**: 在 `MaterialApplicationMapper.xml` 中添加带JOIN的分页查询方法。

**SQL示例**:
```sql
SELECT
    ma.id, ma.title, ma.status, ma.applicant_id, ma.maintainer_id, ma.dept_id,
    u1.real_name AS applicant_name,
    u2.real_name AS maintainer_name,
    d.name AS dept_name,
    (SELECT COUNT(*) FROM asset WHERE application_id = ma.id) AS asset_count
FROM material_application ma
LEFT JOIN sys_user u1 ON ma.applicant_id = u1.id
LEFT JOIN sys_user u2 ON ma.maintainer_id = u2.id
LEFT JOIN sys_dept d ON ma.dept_id = d.id
WHERE ma.applicant_id = #{userId} OR ma.maintainer_id = #{userId}
ORDER BY ma.create_time DESC
LIMIT #{offset}, #{pageSize}
```

**涉及Mapper**:
- MaterialApplicationMapper (新增 selectPageWithDetails)
- UserMapper
- DeptMapper
- AssetMapper

---

## 问题8：素材使用申请列表转换（中优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/usage/impl/UsageApplyServiceImpl.java`
**方法**: `convert()` (行399-431)
**场景**: 素材使用申请列表转换为DTO
**影响**: 100条记录会产生100次查询

**优化方式**: JOIN查询

**当前代码**:
```java
for (UsageApply usageApply : usageApplyList) {
    UserDO user = userMapper.selectById(usageApply.getUserId());  // 1次/记录
}
```

**优化方案**: 在UsageApplyMapper.xml中新增带JOIN的分页查询方法。

**SQL示例**:
```sql
SELECT ua.*, u.real_name AS applicant_name
FROM usage_apply ua
LEFT JOIN sys_user u ON ua.user_id = u.id
WHERE ua.user_id = #{userId}
ORDER BY ua.create_time DESC
LIMIT #{offset}, #{pageSize}
```

**涉及Mapper**:
- UsageApplyMapper (新增 selectPageWithDetails)

---

## 问题9：素材列表-标签转换（中优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/asset/impl/AssetServiceImpl.java`
**方法**: `convertWithTags()` (行294-321)
**场景**: 素材列表带标签转换为DTO
**影响**: 100条记录会产生200+次查询

**优化方式**: JOIN查询

**当前代码**:
```java
for (Asset asset : assets) {
    List<AssetTagDO> assetTags = assetTagMapper.selectList(query);  // 1次/素材
    List<TagDO> tags = tagMapper.selectBatchIds(tagIds);          // 1次/素材
}
```

**优化方案**: 在AssetMapper.xml中新增带JOIN和GROUP_CONCAT的查询方法，一条SQL获取素材及其标签。

**SQL示例**:
```sql
SELECT a.*,
    GROUP_CONCAT(t.name SEPARATOR ',') AS tag_names,
    GROUP_CONCAT(t.id SEPARATOR ',') AS tag_ids
FROM asset a
LEFT JOIN asset_tag at ON a.id = at.asset_id
LEFT JOIN tag t ON at.tag_id = t.id
WHERE a.deleted = 0
GROUP BY a.id
ORDER BY a.create_time DESC
LIMIT #{offset}, #{pageSize}
```

**涉及Mapper**:
- AssetMapper (新增 selectListWithTags)

---

## 问题10：流程设计器-审批人转换（低优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/WorkflowServiceImpl.java`
**方法**: `convertApprover()` (行391-410) 和 `getApproverName()` (行412-422)
**场景**: 流程设计器中审批人配置转换为DTO
**影响**: 10个审批人会产生40次查询

**优化方式**: JOIN查询

**当前代码**:
```java
for (StageApproverDO entity : approvers) {
    String name = getApproverName(entity.getApproverType(), entity.getApproverId());
    // 内部: userMapper.selectById / roleMapper.selectById / deptMapper.selectById

    if (entity.getSubWorkflowId() != null) {
        WorkflowDO subWorkflow = workflowMapper.selectById(entity.getSubWorkflowId());
    }
}
```

**优化方案**: 在StageApproverMapper.xml中新增带JOIN的查询方法，一次性获取审批人及其关联的用户/角色/部门/子流程信息。

**SQL示例**:
```sql
SELECT
    sa.*,
    u.username, u.real_name, u.dept_id,
    r.name AS role_name,
    d.name AS dept_name,
    sub_w.name AS sub_workflow_name
FROM stage_approver sa
LEFT JOIN sys_user u ON sa.approver_type = 'USER' AND sa.approver_id = u.id
LEFT JOIN sys_role r ON sa.approver_type = 'ROLE' AND sa.approver_id = r.id
LEFT JOIN sys_dept d ON sa.approver_type = 'DEPT' AND sa.approver_id = d.id
LEFT JOIN workflow sub_w ON sa.sub_workflow_id = sub_w.id
WHERE sa.stage_id IN
<foreach collection="stageIds" item="stageId" open="(" separator="," close=")">#{stageId}</foreach>
```

**涉及Mapper**:
- StageApproverMapper (新增 selectWithUserRoleDeptDetails)

---

## 问题11：流程设计器-阶段审批人列表（低优先级）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/WorkflowServiceImpl.java`
**方法**: `getWorkflowDetail()` 中的stream().map() (行89, 121)
**场景**: 获取流程详情时转换审批人列表

**优化方式**: JOIN查询（与问题10一起优化）

**当前代码**:
```java
for (WorkflowStageDO stage : stages) {
    List<StageApproverDO> approvers = approverMapper.selectList(query);
    stageDTO.setApprovers(approvers.stream()
        .map(this::convertApprover)  // 每个审批人都有独立查询
        .collect(Collectors.toList()));
}
```

**优化方案**: 与问题10使用相同的JOIN查询方法，流程详情查询也使用StageApproverMapper的JOIN方法。

**涉及Mapper**: 同问题10

---

## 问题12：流程列表（高优先级 - 新增）

**文件**: `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/WorkflowServiceImpl.java`
**方法**: `list()` (行62-95)
**场景**: 流程列表查询
**影响**: 10个流程会产生100+次查询

**当前状态**: **无分页机制**，一次性加载所有流程！

**优化方式**: JOIN查询（必须优化）

**当前代码**:
```java
list.stream().map(workflow -> {
    // 1. 查询每个流程绑定的角色
    RoleDO role = roleMapper.selectById(workflow.getBoundRoleId());  // 1次/流程

    // 2. 查询每个流程的阶段
    List<WorkflowStageDO> stages = stageMapper.selectList(stageQuery); // 1次/流程

    // 3. 对每个阶段查询审批人
    for (WorkflowStageDO stage : stages) {
        List<StageApproverDO> approvers = approverMapper.selectList(...); // 1次/阶段

        // 4. 对每个审批人调用convertApprover（内部又有查询！）
        stageDTO.setApprovers(approvers.stream()
            .map(this::convertApprover)  // 这里面又有多次查询！
            .collect(Collectors.toList()));
    }
});
```

**convertApprover 内部查询**:
```java
// 每个审批人：
UserDO user = userMapper.selectById(id);      // 1次/审批人
RoleDO role = roleMapper.selectById(id);      // 1次/审批人
DeptDO dept = deptMapper.selectById(id);      // 1次/审批人
WorkflowDO subWorkflow = workflowMapper.selectById(...); // 1次/子流程
```

**性能影响**:
假设有 10个流程，3个阶段/流程，2个审批人/阶段：
| 操作 | 次数 |
|------|------|
| 查询角色 | 10次 |
| 查询阶段 | 10次 |
| 查询审批人 | 30次 |
| 查询审批人详情 | 60-80次 |
| **总计** | **110-130次** |

**优化方案**: 在 `WorkflowMapper.xml` 中添加带JOIN的查询方法，一次性获取所有关联数据。

**SQL示例**:
```sql
SELECT
    w.id, w.name, w.workflow_type, w.bound_role_id, w.create_time,
    r.name AS role_name,
    ws.id AS stage_id, ws.stage_order, ws.name AS stage_name, ws.approve_type,
    sa.id AS approver_id, sa.approver_type, sa.approver_id AS approver_ref_id,
    sa.check_secondary_dept, sa.sub_workflow_id,
    u.username, u.real_name, u.dept_id,
    role.name AS approver_role_name,
    dept.name AS approver_dept_name,
    sub_w.name AS sub_workflow_name
FROM workflow w
LEFT JOIN sys_role r ON w.bound_role_id = r.id
LEFT JOIN workflow_stage ws ON w.id = ws.workflow_id
LEFT JOIN stage_approver sa ON ws.id = sa.stage_id
LEFT JOIN sys_user u ON sa.approver_type = 'USER' AND sa.approver_id = u.id
LEFT JOIN sys_role role ON sa.approver_type = 'ROLE' AND sa.approver_id = role.id
LEFT JOIN sys_dept dept ON sa.approver_type = 'DEPT' AND sa.approver_id = dept.id
LEFT JOIN workflow sub_w ON sa.sub_workflow_id = sub_w.id
ORDER BY w.id, ws.stage_order, sa.id
```

**涉及Mapper**:
- WorkflowMapper (新增 selectListWithDetails)
- RoleMapper
- WorkflowStageMapper
- StageApproverMapper
- UserMapper
- DeptMapper

---

## 涉及需要新增的Mapper方法

| Mapper | 方法名 | 用途 | 优化方式 |
|--------|--------|------|---------|
| ApprovalTaskMapper | selectPageWithDetails | 待办列表带JOIN分页查询 | 分页+JOIN |
| StageApproverMapper | selectWithApproverDetails | 审批人配置带JOIN查询 | JOIN |
| MaterialApplicationMapper | selectPageWithDetails | 素材申请带JOIN分页查询 | 分页+JOIN |
| WorkflowMapper | selectListWithDetails | 流程列表带JOIN查询 | JOIN |
| UserMapper | selectListByRoleIdWithDetails | 按角色查询用户(带部门角色名称) | JOIN |
| UserMapper | selectListByDeptIdWithDetails | 按部门查询用户(带部门角色名称) | JOIN |
| UsageApplyMapper | selectPageWithDetails | 素材使用申请带JOIN分页查询 | JOIN |
| AssetMapper | selectListWithTags | 素材列表带标签JOIN查询 | JOIN |
| StageApproverMapper | selectWithUserRoleDeptDetails | 审批人配置带用户/角色/部门详情 | JOIN |
| WorkflowMapper | selectByIdWithStages | 流程详情带阶段审批人JOIN查询 | JOIN |

---

## 实施顺序建议

1. **问题12** (流程列表) - **最严重，无分页，必须优先优化** - JOIN
2. **问题1** (待办列表) - 用户最常用 - 分页+JOIN
3. **问题2** (选择审批人) - 必用功能 - JOIN
4. **问题5** + **问题4** (可用用户列表 + 用户详情) - JOIN
5. **问题6** (素材申请列表) - 分页+JOIN
6. **问题8** (素材使用申请列表) - JOIN
7. **问题9** (素材标签) - JOIN
8. **问题10** + **问题11** (流程设计器) - JOIN
9. **问题3** (任务详情) - 批量预加载（最复杂，涉及子流程）

---

*持续更新中...*
