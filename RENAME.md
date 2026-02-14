# 字段命名不一致问题分析报告

## 一、概述

本文档分析项目中后端与前端字段命名不一致的问题，通过追踪 Mapper -> Repository -> Service -> Controller -> 前端的完整调用链路，找出所有字段命名不一致的问题，并给出详细的修复建议。

---

## 二、真正需要修复的问题

### 问题1：UsageApplyMapper.xml 引用废弃字段 (P0)

**位置**: `xuanjiao-backend/xuanjiao-infrastructure/src/main/resources/mapper/UsageApplyMapper.xml:195-205`

**问题描述**:
`selectByAssetAndUser` 方法引用了 `usage_apply.asset_id` 字段，但该字段在系统重构后已不存在（已改为中间表 `usage_apply_asset`）。

**问题代码**:
```xml
<select id="selectByAssetAndUser" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM usage_apply
    <where>
        asset_id = #{assetId}  <!-- 错误：字段不存在 -->
        AND user_id = #{userId}
        AND status = #{status}
        AND deleted = 0
    </where>
    LIMIT 1
</select>
```

**修复建议**:
```xml
<!-- 方案1：删除此废弃方法（推荐） -->
<!-- 如果该方法不再使用，直接删除即可 -->

<!-- 方案2：如果需要保留功能，修改为查询中间表 -->
<select id="selectByAssetAndUser" resultMap="BaseResultMap">
    SELECT DISTINCT ua.*
    FROM usage_apply ua
    INNER JOIN usage_apply_asset uaa ON ua.id = uaa.usage_apply_id
    <where>
        uaa.asset_id = #{assetId}
        AND ua.user_id = #{userId}
        AND ua.status = #{status}
        AND ua.deleted = 0
    </where>
    LIMIT 1
</select>
```

**影响范围**:
- 该方法被 `UsageApplyServiceImpl.selectByAssetAndUser()` 调用
- 需要确认是否还有业务在使用此方法

---

### 问题2：ApprovalServiceImpl 中 BusinessType 判断不完整 (P1)

**位置**: `xuanjiao-backend/xuanjiao-app/src/main/java/com/xuanjiao/app/approval/impl/ApprovalServiceImpl.java`

**问题描述**:
代码中判断了无效的 `"ASSET"` 类型，但实际上系统的 business_type 只有：
- `MATERIAL_ENTRY` - 素材录入
- `ASSET_USAGE` - 素材使用
- `ASSET_DELETION` - 素材删除

而 `AssetServiceImpl` 中使用 `"ASSET"` 作为 businessType 创建审批实例，导致审批服务无法正确处理。

**问题代码** (第 378 行):
```java
} else if ("ASSET".equals(item.getBusinessType())) {
    // 素材类型，直接使用businessName
}
```

**问题代码** (第 480 行):
```java
} else if ("ASSET".equals(instance.getBusinessType())) {
    // 素材类型，直接使用businessName
}
```

**问题代码** (第 829 行):
```java
if ("ASSET".equals(instance.getBusinessType())) {
    // 素材类型
}
```

**修复建议**:

方案A（推荐）：移除无效的 "ASSET" 判断代码
```java
// 删除以下无效代码
// } else if ("ASSET".equals(item.getBusinessType())) {
//     // 素材类型，直接使用businessName
// }
```

方案B：如果需要支持素材直接审批，需要在 ApprovalServiceImpl 中添加对 "ASSET" 类型的处理逻辑。

---

### 问题3：FlowItemDO 缺少 instanceId 字段 (P1)

**位置**: `xuanjiao-backend/xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/approval/FlowItemDO.java`

**问题描述**:
FlowItemDO 没有 `instanceId` 字段，但前端 `flow-items.vue` 中使用了 `row.instanceId`。

**前端代码** (flow-items.vue 第 169 行):
```javascript
currentInstanceId.value = row.id || row.instanceId
```

**修复建议**:

方案A（推荐）：在 FlowItemDO 中添加 getter 方法
```java
// 在 FlowItemDO 中添加 getter 方法
public Long getInstanceId() {
    return this.id; // 返回 id 的值
}
```

方案B：在 ApprovalTaskMapper.xml 的 FlowItemResultMap 中添加映射
```xml
<resultMap id="FlowItemResultMap" type="com.xuanjiao.infrastructure.approval.FlowItemDO">
    <id column="id" property="id"/>
    <!-- 添加 -->
    <result column="id" property="instanceId"/>
    ...
</resultMap>
```

方案C：前端已做兼容处理（使用 `row.id || row.instanceId`），可以保留现状

---

## 三、完整调用链路（22个Mapper.xml）

### 3.1 AssetDeletionAssetMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | AssetDeletionAssetMapper.selectById | AssetDeletionApplicationRepositoryImpl | AssetDeletionApplicationService | AssetDeletionController | assetDeletion.ts |
| selectList | AssetDeletionAssetMapper.selectList | AssetDeletionApplicationRepositoryImpl | AssetDeletionApplicationService | AssetDeletionController | assetDeletion.ts |
| insert | AssetDeletionAssetMapper.insert | AssetDeletionApplicationRepositoryImpl | AssetDeletionApplicationService | AssetDeletionController | assetDeletion.ts |
| findByDeletionApplicationIdWithAsset | AssetDeletionAssetMapper.findByDeletionApplicationIdWithAsset | - | AssetDeletionApplicationServiceImpl | AssetDeletionController | assetDeletion.ts |

### 3.2 MenuMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | MenuMapper.selectById | - | MenuServiceImpl | MenuController | menu.ts |
| selectList | MenuMapper.selectList | - | MenuServiceImpl | MenuController | menu.ts |
| selectMenuIdsByRoleId | MenuMapper.selectMenuIdsByRoleId | - | MenuServiceImpl | MenuController | menu.ts |
| selectMenusByUserId | MenuMapper.selectMenusByUserId | - | MenuServiceImpl | MenuController | menu.ts |
| insert | MenuMapper.insert | - | MenuServiceImpl | MenuController | menu.ts |
| updateById | MenuMapper.updateById | - | MenuServiceImpl | MenuController | menu.ts |
| deleteById | MenuMapper.deleteById | - | MenuServiceImpl | MenuController | menu.ts |

### 3.3 OperationLogMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | OperationLogMapper.selectById | OperationLogRepositoryImpl | OperationLogServiceImpl | - | - |
| selectList | OperationLogMapper.selectList | OperationLogRepositoryImpl | OperationLogServiceImpl | - | log.ts |
| insert | OperationLogMapper.insert | OperationLogRepositoryImpl | OperationLogServiceImpl | 多个Controller调用 | - |

### 3.4 RoleMenuMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectList | RoleMenuMapper.selectList | - | RoleServiceImpl | RoleController | role.ts |
| insert | RoleMenuMapper.insert | - | RoleServiceImpl | RoleController | role.ts |
| deleteByRoleId | RoleMenuMapper.deleteByRoleId | - | RoleServiceImpl | RoleController | role.ts |

### 3.5 TagMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | TagMapper.selectById | - | TagServiceImpl | TagController | tag.ts |
| selectList | TagMapper.selectList | - | TagServiceImpl | TagController | tag.ts |
| selectBatchIds | TagMapper.selectBatchIds | - | TagServiceImpl | AssetServiceImpl | - |
| insert | TagMapper.insert | - | TagServiceImpl | TagController | tag.ts |
| updateById | TagMapper.updateById | - | TagServiceImpl | TagController | tag.ts |
| deleteById | TagMapper.deleteById | - | TagServiceImpl | TagController | tag.ts |

### 3.6 ApprovalProgressMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | ApprovalProgressMapper.selectById | - | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectOne | ApprovalProgressMapper.selectOne | - | WorkflowEngineServiceImpl | - | - |
| selectList | ApprovalProgressMapper.selectList | - | ApprovalServiceImpl, WorkflowEngineServiceImpl | ApprovalController | approval.ts |
| selectByInstanceId | ApprovalProgressMapper.selectByInstanceId | - | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectByParentInstanceId | ApprovalProgressMapper.selectByParentInstanceId | - | WorkflowEngineServiceImpl | - | - |
| insert | ApprovalProgressMapper.insert | - | WorkflowEngineServiceImpl | - | - |
| updateById | ApprovalProgressMapper.updateById | - | WorkflowEngineServiceImpl | - | - |

### 3.7 NotificationMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectList | NotificationMapper.selectList | NotificationRepositoryImpl | NotificationServiceImpl | NotificationController | notification.ts |
| selectListWithWorkOrder | NotificationMapper.selectListWithWorkOrder | NotificationRepositoryImpl | NotificationServiceImpl | NotificationController | notification.ts |
| selectCount | NotificationMapper.selectCount | NotificationRepositoryImpl | NotificationServiceImpl | NotificationController | notification.ts |
| selectById | NotificationMapper.selectById | NotificationRepositoryImpl | NotificationServiceImpl | NotificationController | notification.ts |
| insert | NotificationMapper.insert | NotificationRepositoryImpl | NotificationServiceImpl | 多个Service调用 | - |
| markAsRead | NotificationMapper.markAsRead | NotificationRepositoryImpl | NotificationServiceImpl | NotificationController | notification.ts |

### 3.8 AssetMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | AssetMapper.selectById | AssetRepositoryImpl | AssetServiceImpl | AssetController | asset.ts |
| selectByMd5IncludeDeleted | AssetMapper.selectByMd5IncludeDeleted | AssetRepositoryImpl | AssetServiceImpl | - | - |
| selectByApplicationId | AssetMapper.selectByApplicationId | AssetRepositoryImpl | AssetServiceImpl, MaterialApplicationServiceImpl | AssetController | asset.ts |
| selectList | AssetMapper.selectList | AssetRepositoryImpl | AssetServiceImpl | AssetController | asset.ts |
| selectPage | AssetMapper.selectPage | AssetRepositoryImpl | AssetServiceImpl | AssetController | asset.ts |
| selectByIds | AssetMapper.selectByIds | AssetRepositoryImpl | AssetServiceImpl | AssetController | asset.ts |
| insert | AssetMapper.insert | AssetRepositoryImpl | AssetServiceImpl | AssetController | asset.ts |
| updateById | AssetMapper.updateById | AssetRepositoryImpl | AssetServiceImpl | AssetController | asset.ts |
| updateStatusByApplicationId | AssetMapper.updateStatusByApplicationId | - | MaterialApplicationServiceImpl | - | - |

### 3.9 AssetDeletionApplicationMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | AssetDeletionApplicationMapper.selectById | AssetDeletionApplicationRepositoryImpl | AssetDeletionApplicationServiceImpl | AssetDeletionController | assetDeletion.ts |
| selectList | AssetDeletionApplicationMapper.selectList | AssetDeletionApplicationRepositoryImpl | AssetDeletionApplicationServiceImpl | AssetDeletionController | assetDeletion.ts |
| insert | AssetDeletionApplicationMapper.insert | AssetDeletionApplicationRepositoryImpl | AssetDeletionApplicationServiceImpl | AssetDeletionController | assetDeletion.ts |
| updateById | AssetDeletionApplicationMapper.updateById | AssetDeletionApplicationRepositoryImpl | AssetDeletionApplicationServiceImpl | AssetDeletionController | assetDeletion.ts |

### 3.10 UsageLogMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | UsageLogMapper.selectById | - | UsageLogServiceImpl | UsageLogController | usageLog.ts |
| selectList | UsageLogMapper.selectList | - | UsageLogServiceImpl | UsageLogController | usageLog.ts |
| insert | UsageLogMapper.insert | - | UsageLogServiceImpl | UsageApplyController | - |

### 3.11 DeptMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | DeptMapper.selectById | - | DeptServiceImpl | DeptController | dept.ts |
| selectByCode | DeptMapper.selectByCode | - | DeptServiceImpl | - | - |
| selectByParentId | DeptMapper.selectByParentId | - | DeptServiceImpl | DeptController | dept.ts |
| selectAll | DeptMapper.selectAll | - | DeptServiceImpl | DeptController | dept.ts |
| selectList | DeptMapper.selectList | - | DeptServiceImpl | DeptController | dept.ts |
| insert | DeptMapper.insert | - | DeptServiceImpl | DeptController | dept.ts |
| updateById | DeptMapper.updateById | - | DeptServiceImpl | DeptController | dept.ts |
| deleteById | DeptMapper.deleteById | - | DeptServiceImpl | DeptController | dept.ts |

### 3.12 RoleMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | RoleMapper.selectById | - | RoleServiceImpl | RoleController | role.ts |
| selectList | RoleMapper.selectList | - | RoleServiceImpl | RoleController | role.ts |
| insert | RoleMapper.insert | - | RoleServiceImpl | RoleController | role.ts |
| updateById | RoleMapper.updateById | - | RoleServiceImpl | RoleController | role.ts |
| deleteById | RoleMapper.deleteById | - | RoleServiceImpl | RoleController | role.ts |

### 3.13 WorkflowStageMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | WorkflowStageMapper.selectById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| selectList | WorkflowStageMapper.selectList | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| insert | WorkflowStageMapper.insert | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| updateById | WorkflowStageMapper.updateById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| deleteById | WorkflowStageMapper.deleteById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |

### 3.14 WorkflowMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | WorkflowMapper.selectById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| selectList | WorkflowMapper.selectList | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| selectListWithRoleName | WorkflowMapper.selectListWithRoleName | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| insert | WorkflowMapper.insert | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| updateById | WorkflowMapper.updateById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| deleteById | WorkflowMapper.deleteById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |

### 3.15 ApprovalTaskMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | ApprovalTaskMapper.selectById | ApprovalTaskRepositoryImpl | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectList | ApprovalTaskMapper.selectList | ApprovalTaskRepositoryImpl | ApprovalServiceImpl, WorkflowEngineServiceImpl | ApprovalController | approval.ts |
| selectPage | ApprovalTaskMapper.selectPage | ApprovalTaskRepositoryImpl | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectPendingTaskPage | ApprovalTaskMapper.selectPendingTaskPage | - | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectPendingTaskCount | ApprovalTaskMapper.selectPendingTaskCount | - | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectFlowItemsByUser | ApprovalTaskMapper.selectFlowItemsByUser | - | ApprovalServiceImpl | ApprovalController | flowItems.ts |
| insert | ApprovalTaskMapper.insert | ApprovalTaskRepositoryImpl | WorkflowEngineServiceImpl | - | - |
| updateById | ApprovalTaskMapper.updateById | ApprovalTaskRepositoryImpl | ApprovalServiceImpl, WorkflowEngineServiceImpl | - | - |

### 3.16 UserMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | UserMapper.selectById | - | UserServiceImpl | UserController | user.ts |
| selectOneByUsername | UserMapper.selectOneByUsername | - | AuthServiceImpl | AuthController | auth.ts |
| selectList | UserMapper.selectList | - | UserServiceImpl | UserController | user.ts |
| selectListWithDetails | UserMapper.selectListWithDetails | - | UserServiceImpl | UserController | user.ts |
| selectUserIdsByRoleId | UserMapper.selectUserIdsByRoleId | - | ApproverSelectionServiceImpl | - | - |
| insert | UserMapper.insert | - | UserServiceImpl | UserController | user.ts |
| updateById | UserMapper.updateById | - | UserServiceImpl, AuthServiceImpl | UserController | user.ts |
| deleteById | UserMapper.deleteById | - | UserServiceImpl | UserController | user.ts |

### 3.17 MaterialApplicationMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | MaterialApplicationMapper.selectById | MaterialApplicationRepositoryImpl | MaterialApplicationServiceImpl | MaterialApplicationController | materialApplication.ts |
| selectList | MaterialApplicationMapper.selectList | MaterialApplicationRepositoryImpl | MaterialApplicationServiceImpl | MaterialApplicationController | materialApplication.ts |
| selectListWithDetails | MaterialApplicationMapper.selectListWithDetails | MaterialApplicationRepositoryImpl | MaterialApplicationServiceImpl | MaterialApplicationController | materialApplication.ts |
| insert | MaterialApplicationMapper.insert | MaterialApplicationRepositoryImpl | MaterialApplicationServiceImpl | MaterialApplicationController | materialApplication.ts |
| updateById | MaterialApplicationMapper.updateById | MaterialApplicationRepositoryImpl | MaterialApplicationServiceImpl | MaterialApplicationController | materialApplication.ts |
| deleteById | MaterialApplicationMapper.deleteById | MaterialApplicationRepositoryImpl | MaterialApplicationServiceImpl | MaterialApplicationController | materialApplication.ts |

### 3.18 AssetTagMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectList | AssetTagMapper.selectList | - | AssetServiceImpl | AssetController | asset.ts |
| selectByAssetIds | AssetTagMapper.selectByAssetIds | - | AssetServiceImpl | AssetController | asset.ts |
| insert | AssetTagMapper.insert | - | AssetServiceImpl | AssetController | asset.ts |
| delete | AssetTagMapper.delete | - | AssetServiceImpl | AssetController | asset.ts |

### 3.19 StageApproverMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | StageApproverMapper.selectById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| selectList | StageApproverMapper.selectList | - | WorkflowServiceImpl, ApproverSelectionServiceImpl | WorkflowController | workflow.ts |
| selectWithDetails | StageApproverMapper.selectWithDetails | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| insert | StageApproverMapper.insert | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| updateById | StageApproverMapper.updateById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |
| deleteById | StageApproverMapper.deleteById | - | WorkflowServiceImpl | WorkflowController | workflow.ts |

### 3.20 UsageApplyMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | UsageApplyMapper.selectById | UsageApplyRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| selectList | UsageApplyMapper.selectList | UsageApplyRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| selectListWithDetails | UsageApplyMapper.selectListWithDetails | UsageApplyRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| selectByAssetAndUser | UsageApplyMapper.selectByAssetAndUser | UsageApplyRepositoryImpl | UsageApplyServiceImpl | - | ⚠️ 有问题 |
| insert | UsageApplyMapper.insert | UsageApplyRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| updateById | UsageApplyMapper.updateById | UsageApplyRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| deleteById | UsageApplyMapper.deleteById | UsageApplyRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |

### 3.21 ApprovalInstanceMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectById | ApprovalInstanceMapper.selectById | ApprovalInstanceRepositoryImpl | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectList | ApprovalInstanceMapper.selectList | ApprovalInstanceRepositoryImpl | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectPage | ApprovalInstanceMapper.selectPage | ApprovalInstanceRepositoryImpl | ApprovalServiceImpl | ApprovalController | approval.ts |
| selectMyAppliedList | ApprovalInstanceMapper.selectMyAppliedList | - | ApprovalServiceImpl | ApprovalController | approval.ts |
| insert | ApprovalInstanceMapper.insert | ApprovalInstanceRepositoryImpl | WorkflowEngineServiceImpl | - | - |
| updateById | ApprovalInstanceMapper.updateById | ApprovalInstanceRepositoryImpl | WorkflowEngineServiceImpl, ApprovalServiceImpl | - | - |

### 3.22 UsageApplyAssetMapper.xml

| 方法 | Mapper接口 | Repository | Service | Controller | 前端 |
|------|-----------|------------|---------|------------|------|
| selectList | UsageApplyAssetMapper.selectList | UsageApplyAssetRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| selectCount | UsageApplyAssetMapper.selectCount | UsageApplyAssetRepositoryImpl | UsageApplyServiceImpl | - | - |
| findByUsageApplyIdWithAsset | UsageApplyAssetMapper.findByUsageApplyIdWithAsset | UsageApplyAssetRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| findByAssetId | UsageApplyAssetMapper.findByAssetId | UsageApplyAssetRepositoryImpl | UsageApplyServiceImpl | - | - |
| insert | UsageApplyAssetMapper.insert | UsageApplyAssetRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| deleteById | UsageApplyAssetMapper.deleteById | UsageApplyAssetRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |
| deleteByUsageApplyId | UsageApplyAssetMapper.deleteByUsageApplyId | UsageApplyAssetRepositoryImpl | UsageApplyServiceImpl | UsageApplyController | usageApply.ts |

---

## 四、问题汇总与优先级

### P0 级别 - 必须修复

| # | 位置 | 问题描述 | 修复方案 | 状态 |
|---|------|---------|---------|------|
| 1 | UsageApplyMapper.xml:195 | `selectByAssetAndUser`引用不存在的`asset_id`字段 | 删除废弃方法 | ✅ 已修复 |

### P1 级别 - 应该修复

| # | 位置 | 问题描述 | 修复方案 | 状态 |
|---|------|---------|---------|------|
| 1 | ApprovalServiceImpl.java 第378、480、829行 | BusinessType判断使用了无效的`"ASSET"`值 | 移除无效代码 | ✅ 已修复 |
| 2 | FlowItemDO.java | 缺少`instanceId`字段 | 添加getInstanceId()方法 | 待处理 |

---

## 五、Mapper层字段映射规范

### 5.1 正确示例

```xml
<resultMap id="BaseResultMap" type="com.xuanjiao.infrastructure.dataobject.UserDO">
    <id column="id" property="id" jdbcType="BIGINT"/>
    <result column="username" property="username" jdbcType="VARCHAR"/>
    <result column="real_name" property="realName" jdbcType="VARCHAR"/>
    <result column="dept_id" property="deptId" jdbcType="BIGINT"/>
</resultMap>
```

### 5.2 常见错误

```xml
<!-- 错误：column使用了Java属性名而不是数据库字段名 -->
<result column="userId" property="userId"/>

<!-- 正确：column应该是数据库下划线命名 -->
<result column="user_id" property="userId"/>
```

---

*报告生成时间: 2026-02-14*
