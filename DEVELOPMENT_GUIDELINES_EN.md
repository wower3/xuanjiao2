# Propaganda/Education Platform - Development Guidelines (English Version)

## Project Overview

**Project Name**: Propaganda/Education Platform
**Tech Stack**: Java 8 + Spring Boot 2.7 + Vue 3 + TypeScript + MySQL 8.0 + MyBatis
**Architecture**: COLA (Clean Object-Oriented and Layered Architecture)

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Layered Architecture](#layered-architecture)
3. [Module Organization](#module-organization)
4. [Naming Conventions](#naming-conventions)
5. [Database Design](#database-design)
6. [API Design Standards](#api-design-standards)
7. [MyBatis Development Standards](#mybatis-development-standards)
8. [Workflow Engine Design](#workflow-engine-design)
9. [Frontend Development Standards](#frontend-development-standards)
10. [Testing Standards](#testing-standards)

---

## Project Structure

```
xuanjiao-backend/
├── xuanjiao-client/       # Client Layer - DTOs, API Request/Response Definitions
├── xuanjiao-domain/       # Domain Layer - Entities, Domain Services, Repository Interfaces
├── xuanjiao-infrastructure/ # Infrastructure Layer - MyBatis Mappers, Repository Implementations
├── xuanjiao-app/          # Application Layer - Business Logic, Service Implementations
├── xuanjiao-adapter/      # Adapter Layer - REST Controllers, External Integrations
└── xuanjiao-start/        # Start Module - Application Entry Point

xuanjiao-frontend/
├── src/api/               # API Client
├── src/stores/            # Pinia State Management
├── src/components/        # Reusable Components
├── src/views/             # Page Components
├── src/router/            # Vue Router Configuration
└── src/layouts/           # Layout Components
```

---

## Layered Architecture

### COLA Four-Layer Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Adapter Layer                                              │
│  - Handle HTTP Requests/Responses                           │
│  - Parameter Validation (@Valid)                             │
│  - Call Application Layer Services                          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Application Layer                                           │
│  - Business Logic Orchestration                             │
│  - Transaction Management (@Transactional)                   │
│  - Call Domain Layer and Infrastructure Layer                │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Domain Layer                                                │
│  - Domain Entities                                           │
│  - Repository Interface Definitions                         │
│  - Domain Services                                          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Infrastructure Layer                                       │
│  - MyBatis Mapper Implementations                           │
│  - Repository Implementations                              │
│  - Data Objects (DO)                                        │
└─────────────────────────────────────────────────────────────┘
```

**Dependency Rule**: Adapter → App → Domain (Client layer is shared by all layers)

---

## Module Organization

### Business Module List

| Module Code | Module Name | Description |
|-------------|-------------|-------------|
| `auth` | Authentication | Login, logout, token management |
| `user` | User | User CRUD, permission queries |
| `dept` | Department | Department tree, department management |
| `role` | Role | Role CRUD, role permissions |
| `menu` | Menu | Menu tree, menu configuration |
| `asset` | Asset | Asset upload, download, management |
| `material` | Material Application | Asset entry applications |
| `usage` | Usage Application | Asset usage applications, usage logs |
| `deletion` | Asset Deletion | Asset deletion applications |
| `workflow` | Workflow | Workflow definition, process engine |
| `approval` | Approval Execution | Approval instances, task processing |
| `log` | Log | Operation log recording |

### File Location Pattern

```
{layer}/src/main/java/com/xuanjiao/{layer}/{module}/
├── entity/          # Domain Layer Entities
├── repository/      # Domain Layer Repository Interfaces
├── impl/            # Implementation Classes
└── web/             # Adapter Layer Controllers
```

---

## Naming Conventions

### Backend Naming

#### Package Naming
- Controller: `com.xuanjiao.adapter.web.{module}`
- Service Interface: `com.xuanjiao.app.{module}`
- Service Implementation: `com.xuanjiao.app.{module}.impl`
- Mapper Interface: `com.xuanjiao.infrastructure.{module}`
- Repository Interface: `com.xuanjiao.domain.{module}.repository`
- Entity: `com.xuanjiao.domain.{module}.entity`
- DTO: `com.xuanjiao.client` or `com.xuanjiao.client.{module}`

#### Class Naming
- Controller: `{Module}Controller`
- Service: `{Module}Service`
- Service Impl: `{Module}ServiceImpl`
- Mapper: `{Module}Mapper`
- Repository: `{Module}Repository`
- Repository Impl: `{Module}RepositoryImpl`
- Entity: `{Module}` (e.g., `User`, `Asset`)
- DO: `{Module}DO` (Database mapping object)
- DTO: `{Module}DTO`

#### DTO Naming Conventions
```
Query DTO: {Action}Qry      Example: AssetGetDetailQry, UserGetMyTasksQry
Command DTO: {Action}Cmd    Example: AssetDeleteCmd, ApprovalApproveCmd
Response DTO: {Module}DTO   Example: UserDTO, AssetDTO
Pagination DTO: PageResult<T> Example: PageResult<AssetDTO>
```

#### Mapper XML Naming
- File Location: `resources/mapper/{Module}Mapper.xml`
- ResultMap: `BaseResultMap`
- Column List: `<sql id="Base_Column_List">`

### Frontend Naming

#### API File Naming
```
api/{module}.ts      Example: api/user.ts, api/asset.ts
```

#### Store File Naming
```
stores/{module}.ts   Example: stores/user.ts, stores/workflow.ts
```

#### Component Naming
- Page Components: PascalCase (e.g., `UserList.vue`, `AssetDetail.vue`)
- Reusable Components: PascalCase (e.g., `AssetSelector.vue`, `UserPicker.vue`)

---

## Database Design

### Table Naming Convention
```
{business}_{function}        Example: workflow_stage, stage_approver
Join Table: {main}_{sub}        Example: usage_apply_asset, asset_tag
```

### Column Naming Convention
```
Primary Key: id
Foreign Key: {table}_id       Example: user_id, workflow_id, asset_id
Timestamp: {action}_time       Example: create_time, approve_time
Status: {entity}_status       Example: workflow_status, asset_status
Soft Delete: deleted           Default 0=not deleted, 1=deleted
```

### Special Field Design
```sql
-- Approval Related
approval_instance_id    -- Approval instance ID
current_stage_id        -- Current stage ID
parent_instance_id      -- Parent flow instance ID (for sub-workflows)

-- Workflow Related
is_first_approver        -- Is first approver (for AND-sign)
next_stage_approver_ids  -- Next stage approver IDs (JSON)
sub_workflow_approver_ids -- Sub-workflow approver IDs (JSON)

-- Asset Related
md5                      -- File MD5 (deduplication)
deletion_approve_time    -- Deletion approval time (scheduled cleanup)
```

---

## API Design Standards

### POST-First Principle

**Default Rule**: Use POST for all API endpoints, parameters in request body

```java
// ✅ Correct - Query operations use POST
@PostMapping("/getDetail")
public Result<AssetDTO> getDetail(@Valid @RequestBody AssetGetDetailQry qry)

// ✅ Correct - Command operations use POST
@PostMapping("/delete")
public Result<Void> delete(@Valid @RequestBody AssetDeleteCmd cmd)

// ⚠️ Exception - File preview must use GET (browser limitation)
@GetMapping("/preview/{id}")
public FileSystemResource preview(@PathVariable Long id)
```

### Response Format
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### DTO Validation Annotations
```java
public class AssetCreateCmd {
    @NotBlank(message = "Asset name cannot be empty")
    private String name;

    @NotBlank(message = "Asset type cannot be empty")
    private String type;

    @Min(value = 1, message = "File size must be greater than 0")
    private Long fileSize;
}
```

---

## MyBatis Development Standards

### Mapper Interface Definition

**All Mappers must explicitly define methods, no BaseMapper inheritance**

```java
@Mapper
public interface AssetMapper {
    // Basic CRUD Methods
    AssetDO selectById(@Param("id") Long id);
    AssetDO selectOne(AssetQuery query);
    List<AssetDO> selectList(AssetQuery query);
    Long selectCount(AssetQuery query);
    IPage<AssetDO> selectPage(Page<AssetDO> page, @Param("query") AssetQuery query);
    int insert(AssetDO assetDO);
    int updateById(AssetDO assetDO);

    // Business-specific Methods
    AssetDO selectByMd5(@Param("md5") String md5);
    List<AssetDO> selectByApplicationId(@Param("applicationId") Long applicationId);
}
```

### Query Object Pattern

**Define dedicated Query classes instead of dynamic conditions**

```java
@Data
public class AssetQuery {
    // Basic Query Conditions
    private Long id;
    private String name;
    private String type;
    private String status;
    private Integer deleted;

    // Extended Query Conditions
    private String nameKeyword;           // Fuzzy search
    private List<String> statusIn;        // IN query
    private List<Long> userIds;           // IN query
    private Long idNotEqual;             // != query
    private Boolean deletedIsNull;       // IS NULL query
}
```

### Mapper XML Development Standards

#### ResultMap Definition
```xml
<resultMap id="BaseResultMap" type="com.xuanjiao.infrastructure.dataobject.AssetDO">
    <id column="id" property="id" jdbcType="BIGINT"/>
    <result column="name" property="name" jdbcType="VARCHAR"/>
    <result column="type" property="type" jdbcType="VARCHAR"/>
    <!-- Explicit mapping from column (database) to property (Java) -->
</resultMap>
```

#### Column List Definition
```xml
<sql id="Base_Column_List">
    id, name, type, file_path, thumbnail_path, file_size,
    md5, status, deleted, create_time, update_time
</sql>
```

**NEVER use `SELECT *`**

#### Dynamic SQL Standards
```xml
<!-- WHERE Clause: Always use <where> tag -->
<select id="selectList" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM asset
    <where>
        <if test="id != null">
            AND id = #{id}
        </if>
        <if test="name != null and name != ''">
            AND name = #{name}
        </if>
        <if test="deleted != null">
            AND deleted = #{deleted}
        </if>
        <if test="deletedIsNull != null and deletedIsNull">
            AND deleted IS NULL
        </if>
        <if test="statusIn != null and statusIn.size() > 0">
            AND status IN
            <foreach collection="statusIn" item="status" open="(" separator="," close=")">
                #{status}
            </foreach>
        </if>
    </where>
    ORDER BY create_time DESC
</select>
```

#### UPDATE Statement Standards
```xml
<!-- SET Clause: Use <set> tag -->
<update id="updateById" parameterType="com.xuanjiao.infrastructure.dataobject.AssetDO">
    UPDATE asset
    <set>
        <if test="name != null and name != ''">name = #{name},</if>
        <if test="type != null and type != ''">type = #{type},</if>
        <if test="status != null and status != ''">status = #{status},</if>
        <if test="deleted != null">deleted = #{deleted},</if>
    </set>
    WHERE id = #{id}
</update>
```

#### INSERT Statement Standards
```xml
<!-- Explicitly list all fields -->
<insert id="insert" parameterType="com.xuanjiao.infrastructure.dataobject.AssetDO"
        useGeneratedKeys="true" keyProperty="id">
    INSERT INTO asset (
        name, type, file_path, thumbnail_path, file_size,
        md5, status, deleted, create_time, update_time
    ) VALUES (
        #{name}, #{type}, #{filePath}, #{thumbnailPath}, #{fileSize},
        #{md5}, #{status}, #{deleted}, #{createTime}, #{updateTime}
    )
</insert>
```

### Special Query Scenarios

#### IS NULL / IS NOT NULL Query
```java
// Add Boolean field to Query object
@Data
public class ApprovalProgressQuery {
    private Long parentInstanceId;
    private Boolean parentInstanceIdIsNull;  // IS NULL query
}
```

```xml
<if test="parentInstanceIdIsNull != null and parentInstanceIdIsNull">
    AND parent_instance_id IS NULL
</if>
<if test="parentInstanceIdIsNull != null and !parentInstanceIdIsNull">
    AND parent_instance_id IS NOT NULL
</if>
```

#### IN Query
```java
@Data
public class AssetQuery {
    private List<String> statusIn;
}
```

```xml
<if test="statusIn != null and statusIn.size() > 0">
    AND status IN
    <foreach collection="statusIn" item="status" open="(" separator="," close=")">
        #{status}
    </foreach>
</if>
```

#### != Query
```java
@Data
public class ApprovalTaskQuery {
    private Long idNotEqual;  // != query
}
```

```xml
<if test="idNotEqual != null">
    AND id != #{idNotEqual}
</if>
```

### Pagination Query Standards

```java
// Mapper Interface
IPage<AssetDO> selectPage(Page<AssetDO> page, @Param("query") AssetQuery query);

// Usage
IPage<AssetDO> page = assetMapper.selectPage(
    new Page<>(pageNum, pageSize),
    query
);
```

### Force Field to NULL

**Issue**: `updateById()` cannot update field to null

**Solution**: Use explicit XML method

```java
// Add method to Mapper interface
int resetApprovers(@Param("id") Long id);

// Mapper XML
<update id="resetApprovers">
    UPDATE approval_progress
    SET approvers = NULL
    WHERE id = #{id}
</update>
```

### Field Mapping Standards

**column**: Database field name (underscore_case, like `role_id`)
**property**: Java property name (camelCase, like `roleId`)

```xml
<result column="role_id" property="roleId" jdbcType="BIGINT"/>
<result column="create_time" property="createTime" jdbcType="TIMESTAMP"/>
```

---

## Workflow Engine Design

### Workflow Types

| Type Code | Type Name | Description |
|-----------|-----------|-------------|
| `ASSET_UPLOAD` | Asset Entry | Asset upload approval |
| `ASSET_USAGE` | Asset Usage | Asset usage application approval |
| `ASSET_DELETION` | Asset Deletion | Asset deletion approval |

### Flow Structure
```
Main Flow
├── Stage 1
│   ├── OR-sign / AND-sign
│   └── Sub-Workflow (optional)
├── Stage 2
│   └── ...
```

### Approval Types

| Type | Rule |
|------|------|
| **OR (Or-sign)** | Any one approver can approve |
| **AND (And-sign)** | All approvers must approve |

### Approver Types

| Type | Description |
|------|-------------|
| `USER` | Specific User |
| `ROLE` | Specific Role (with secondary department filter) |
| `DEPT` | Specific Department |

### Sub-Workflow Rules
- Configured at approver level (not stage level)
- Runs independently, does not block main flow
- Completion condition: Main flow + all sub-workflows completed

### Approval Status Flow
```
PENDING → MAIN_COMPLETED → APPROVED
         ↳ REJECTED
```

---

## Frontend Development Standards

### Component Communication
```typescript
// API Call - Always use POST
import request from '@/utils/request'

export function getAssetList(params: any) {
  return request.post('/asset/list', params)
}

// Pinia Store
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  return { userInfo }
})
```

### Reactive Data
```typescript
// Recommended: Use ref or reactive
const fileList = ref<any[]>([])
const formData = reactive({
  name: '',
  type: 'IMAGE'
})

// ❌ Avoid: Direct let declaration
let count = 0
```

### Route Guards
```typescript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})
```

---

## Testing Standards

### Testing Layers
```
Unit Test
├── Mock external dependencies
├── Test business logic
└── Location: xuanjiao-app/src/test

Integration Test
├── @SpringBootTest
├── Test Mapper + Service
└── Location: xuanjiao-start/src/test

API Test
├── Full request-response testing
└── Location: xuanjiao-start/src/test
```

### Test Naming Convention
```java
// Unit Tests
test{MethodName}_{Scenario}_{ExpectedResult}
Example: testSubmit_WithValidData_ReturnsInstanceId

// Integration Tests
test{MapperName}_{Operation}
Example: testAssetMapper_selectList
```

---

## Common Issues & Solutions

### 1. IS NULL Query
```java
// ❌ Wrong
query.setParentInstanceId(null)  // Cannot generate proper IS NULL

// ✅ Correct - Use dedicated field
query.setParentInstanceIdIsNull(true)  // Generates "IS NULL"
query.setParentInstanceIdIsNull(false) // Generates "IS NOT NULL"
```

### 2. Field Type Mismatch
```xml
<!-- column: database field (underscore_case) -->
<!-- property: Java property (camelCase) -->
<result column="role_id" property="roleId"/>
```

### 3. updateById() Cannot Update to NULL
```java
// ❌ Doesn't work - null value is ignored
progress.setApprovers(null);
progressMapper.updateById(progress);

// ✅ Correct - Use explicit XML method
progressMapper.resetApprovers(progress.getId());
```

### 4. FormData Parameter Type
```typescript
// Frontend FormData converts numbers to strings
formData.append('applicationId', data.applicationId)

// Backend @ModelAttribute auto-converts
public Result<AssetDTO> upload(
    @RequestParam("file") MultipartFile file,
    @ModelAttribute AssetUploadCmd cmd
)
```

---

## Version History

- **v1.0** (2025-01) - Initial version, COLA architecture
- **v1.1** (2025-01) - Added sub-workflow support
- **v1.2** (2025-02) - Mapper refactoring, pure XML Mapper approach
- **v1.3** (2025-02) - Removed MyBatis-Plus dependency, using native MyBatis

---

## Appendix

### Related Documents
- `REQUIREMENTS.md` - Product Requirements Document
- `PROGRESS.md` - Development Progress
- `WORKFLOW_REFACTOR_SUMMARY.md` - Workflow Refactoring Summary
- `MAPPER_REFACTOR_LOG.md` - Mapper Refactoring Log

### Contact
- Technical Issues: Check project Wiki or submit Issue
