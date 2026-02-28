# 字段命名一致性问题分析报告

> 分析日期: 2026-02-14
> 分析范围: 数据库层 -> Mapper XML -> DO实体 -> DTO -> 前端API/组件
> Mapper XML 文件总数: 22个

---

## 一、严重问题（会导致运行时错误）

### 0. UserMapper.xml / MenuMapper.xml - role_id 字段依赖问题

**位置**:
- `xuanjiao-infrastructure/src/main/resources/mapper/UserMapper.xml`
- `xuanjiao-infrastructure/src/main/resources/mapper/MenuMapper.xml:90`

**问题描述**: 这两个 Mapper 依赖 `sys_user.role_id` 字段，但该字段在不同版本的数据库初始化脚本中存在不一致：

| 初始化脚本 | role_id 字段 |
|-----------|-------------|
| init_02_user_table.sql | ❌ 不存在 |
| init_all.sql | ❌ 不存在 |
| init_complete.sql | ✅ 存在 |
| init_complete_fixed.sql | ✅ 存在 |

**UserMapper.xml 错误代码**:
```xml
<!-- Base_Column_List -->
id, username, password, real_name, email, phone, dept_id, role_id, status

<!-- resultMap -->
<result column="role_id" property="roleId" jdbcType="BIGINT"/>

<!-- INSERT -->
username, password, real_name, email, phone, dept_id, role_id, status
```

**MenuMapper.xml 错误代码**:
```sql
-- selectMenusByUserId 方法
INNER JOIN sys_user u ON rm.role_id = u.role_id
```

**影响**: 如果使用 `init_all.sql` 初始化数据库（没有 role_id 字段），以下操作会失败：
- 用户查询
- 用户新增/更新
- 菜单按用户权限查询

**调用链路**:
```
前端 user.vue -> UserController -> UserService -> UserMapper -> SQL报错
前端 menu -> MenuController -> MenuService -> MenuMapper.selectMenusByUserId -> SQL报错
```

---

### 2. UsageApplyMapper.xml - 引用不存在的数据库字段

**位置**: `xuanjiao-infrastructure/src/main/resources/mapper/UsageApplyMapper.xml:195-205`

**问题描述**: `selectByAssetAndUser` 方法引用了 `usage_apply.asset_id` 字段，但该字段在数据库中已不存在。

**数据库实际字段**: `usage_apply` 表在 `init_24_refactor_to_intermediate_table.sql` 重构后，已移除 `asset_id` 字段，改为通过 `usage_apply_asset` 中间表建立多对多关系。

**错误代码**:
```xml
<select id="selectByAssetAndUser" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM usage_apply
    <where>
        asset_id = #{assetId}  <!-- ❌ 错误：usage_apply 表已无 asset_id 字段 -->
        AND user_id = #{userId}
        AND status = #{status}
        AND deleted = 0
    </where>
    LIMIT 1
</select>
```

**调用链路分析**:
```
前端 usageApply.ts -> UsageApplyService.selectByAssetAndUser() -> UsageApplyMapper.selectByAssetAndUser() -> 报错
```

---

### 3. Asset 模块 - 缺失 deletionApplicationId 字段

**位置**:
- `xuanjiao-infrastructure/src/main/resources/mapper/AssetMapper.xml`
- `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/dataobject/AssetDO.java`
- `xuanjiao-client/src/main/java/com/xuanjiao/client/dto/AssetDTO.java`

**问题描述**: 数据库 `asset` 表有 `deletion_application_id` 字段，但 Mapper、DO、DTO 均缺失该字段的映射。

**数据库字段**:
```sql
-- asset 表
deletion_application_id BIGINT DEFAULT NULL COMMENT '删除申请ID',
```

**缺失内容**:
1. AssetMapper.xml 的 resultMap 中缺少: `<result column="deletion_application_id" property="deletionApplicationId"/>`
2. AssetMapper.xml 的 Base_Column_List 中缺少: `deletion_application_id`
3. AssetDO.java 缺少字段: `private Long deletionApplicationId;`
4. AssetDTO.java 缺少字段: `private Long deletionApplicationId;`

**影响**: 当素材有关联删除申请时，该字段无法传递到前端。

---

### 4. ApprovalTaskDO - 缺失 selectedByUserId 字段

**位置**: `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/dataobject/ApprovalTaskDO.java`

**问题描述**: Mapper XML 正确定义了 `selected_by_user_id` 字段映射，但 DO 类缺少对应字段。

**数据库字段**:
```sql
-- approval_task 表
selected_by_user_id BIGINT DEFAULT NULL COMMENT '选择下一层审批人的用户ID',
```

**Mapper XML 定义**:
```xml
<result column="selected_by_user_id" property="selectedByUserId" jdbcType="BIGINT"/>
```

**缺失内容**: ApprovalTaskDO.java 缺少字段: `private Long selectedByUserId;`

---

### 5. ApprovalInstanceDO - 缺失字段

**位置**: `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/dataobject/ApprovalInstanceDO.java`

**问题描述**: Mapper XML 定义了以下字段映射，但 DO 类缺少对应字段。

**缺失字段**:

| Mapper XML 定义 | DO 缺失 |
|----------------|---------|
| `<result column="root_instance_id" property="rootInstanceId"/>` | `rootInstanceId` |
| `<result column="sub_workflow_approver_ids" property="subWorkflowApproverIds"/>` | `subWorkflowApproverIds` |

---

### 6. Domain 层 - Workflow 实体字段问题

**位置**: `xuanjiao-domain/src/main/java/com/xuanjiao/domain/workflow/entity/Workflow.java`

**问题描述**:
1. 使用 `type` 字段而非 `workflowType`（与数据库和其他层不一致）
2. 缺失 `boundRoleId` 字段

**当前代码**:
```java
private String type;  // ❌ 应为 workflowType
// ❌ 缺失 boundRoleId 字段
```

**数据库字段**:
```sql
-- workflow 表
bound_role_id BIGINT DEFAULT NULL COMMENT '绑定的角色ID',
type VARCHAR(20) DEFAULT 'ASSET_UPLOAD' COMMENT '流程类型',
```

---

### 7. Domain 层 - StageApprover 实体字段缺失

**位置**: `xuanjiao-domain/src/main/java/com/xuanjiao/domain/workflow/entity/StageApprover.java`

**问题描述**: 完全缺少以下关键字段

**缺失字段**:
```java
private Integer checkSecondaryDept;  // 是否检查 Long subWorkflowId二级部门
private;          // 子流程ID
```

**数据库字段** (stage_approver 表):
```sql
check_secondary_dept TINYINT DEFAULT 0 COMMENT '是否检查二级部门',
sub_workflow_id BIGINT DEFAULT NULL COMMENT '子流程ID',
```

---

### 8. AssetDeletionAssetMapper - 引用不存在的数据库字段

**位置**: `xuanjiao-infrastructure/src/main/resources/mapper/AssetDeletionAssetMapper.xml`

**问题描述**: Mapper XML 引用了数据库表中不存在的 `asset_name` 和 `asset_type` 字段。

**数据库实际字段** (asset_deletion_asset 表):
```sql
-- 数据库只有这些字段
id, deletion_application_id, asset_id, deleted
```

**错误代码**:
```xml
<sql id="Base_Column_List">
    id, deletion_application_id, asset_id, asset_name, asset_type  <!-- ❌ 字段不存在 -->
</sql>

<insert id="insert" ...>
    INSERT INTO asset_deletion_asset (deletion_application_id, asset_id, asset_name, asset_type)
    VALUES (#{deletionApplicationId}, #{assetId}, #{assetName}, #{assetType})
</insert>
```

**注意**: 实际查询时通过 JOIN asset 表获取这些字段，设计是合理的，但 INSERT 语句会失败。

---

## 二、中等问题（可能导致数据展示异常）

### 9. Notification 模块 - status 字段类型不一致

**位置**:
- `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/notification/NotificationWithWorkOrderDO.java`
- `xuanjiao-infrastructure/src/main/resources/mapper/NotificationMapper.xml`

**问题描述**:
- `NotificationWithWorkOrderDO.status` 定义为 `Integer` (tinyint)
- 但实际存储的是 `approval_instance.status` 字符串枚举 ('PENDING', 'APPROVED', 'REJECTED')

**代码现状**:
```java
// NotificationWithWorkOrderDO.java
private Integer status;  // 映射 notification 表的 status
private String instanceStatus;  // 映射 approval_instance.status
```

```xml
<!-- NotificationMapper.xml -->
<result property="instanceStatus" column="instance_status"/>
<result property="status" column="status"/>
```

**问题**: 前端收到的 status 值类型可能不正确。

---

### 10. UsageLogDTO - 包含数据库不存在的字段

**位置**: `xuanjiao-client/src/main/java/com/xuanjiao/client/dto/UsageLogDTO.java`

**问题描述**: DTO 包含 `username` 字段，但数据库 `usage_log` 表没有该字段。

**DTO 字段**:
```java
private String username;  // ❌ 数据库表没有此字段
```

**说明**: 如果需要显示用户名，Mapper XML 应该 JOIN `sys_user` 表获取。

---

## 三、轻微问题（代码风格不一致）

### 11. TagDTO - 缺少 deleted 字段

**位置**: `xuanjiao-client/src/main/java/com/xuanjiao/client/dto/TagDTO.java`

**问题描述**: 数据库 `tag` 表有 `deleted` 字段，但 DTO 缺少该字段。

**说明**: 可能是设计意图（不向前端暴露删除状态），但会导致前后端字段不对称。

---

### 12. OperationLogDO - 缺少 @Data 注解

**位置**: `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/dataobject/OperationLogDO.java`

**问题描述**: 唯一使用手动 getter/setter 的 DO 类，没有 @Data 注解（对比其他 DO 都使用 @Data）。

---

## 四、字段映射正确无误的模块

经过分析，以下模块的字段命名保持一致：

| 模块 | 状态 |
|------|------|
| Material Application (素材录入) | ✅ 一致 |
| Dept (部门) | ✅ 一致 |
| Role (角色) | ✅ 一致 |
| Workflow Stage | ✅ 一致 |
| UsageApplyAsset (中间表) | ✅ 一致 |
| AssetTag (中间表) | ✅ 一致 |

---

## 五、修复建议优先级

### P0 - 必须立即修复

1. **UserMapper.xml / MenuMapper.xml** - 确认数据库使用的初始化脚本，如果是 init_all.sql 则需要移除 role_id 相关代码
2. **UsageApplyMapper.xml** - 移除或修复 `selectByAssetAndUser` 方法
3. **Asset 模块** - 添加 `deletionApplicationId` 字段到 Mapper、DO、DTO
4. **ApprovalTaskDO** - 添加 `selectedByUserId` 字段
5. **ApprovalInstanceDO** - 添加 `rootInstanceId` 和 `subWorkflowApproverIds` 字段
6. **AssetDeletionAssetMapper.xml** - 修复 INSERT 语句中的字段引用

### P1 - 应该尽快修复

7. **Domain/Workflow.java** - 统一字段命名为 `workflowType`，添加 `boundRoleId`
8. **Domain/StageApprover.java** - 添加 `checkSecondaryDept` 和 `subWorkflowId` 字段

### P2 - 建议修复

9. **Notification 模块** - 统一 `status` 字段类型
10. **UsageLogDTO** - 确认是否需要 JOIN 查询 username

---

## 六、附录：调用链路示例

### 素材模块调用链路
```
前端 asset.ts
  ↓
AssetController
  ↓
AssetService
  ↓
AssetMapper (AssetMapper.xml)
  ↓
AssetDO ←→ 数据库 asset 表
```

### 素材使用申请调用链路
```
前端 usageApply.ts
  ↓
UsageApplyController
  ↓
UsageApplyService
  ↓
UsageApplyMapper (UsageApplyMapper.xml) ←→ 数据库 usage_apply 表
  ↓
UsageApplyAssetMapper (UsageApplyAssetMapper.xml) ←→ 数据库 usage_apply_asset 表
```

### 审批模块调用链路
```
前端 pending-approval.vue / approval.ts
  ↓
ApprovalController
  ↓
ApprovalService
  ↓
ApprovalTaskMapper (ApprovalTaskMapper.xml) ←→ 数据库 approval_task 表
ApprovalInstanceMapper (ApprovalInstanceMapper.xml) ←→ 数据库 approval_instance 表
ApprovalProgressMapper (ApprovalProgressMapper.xml) ←→ 数据库 approval_progress 表
```
