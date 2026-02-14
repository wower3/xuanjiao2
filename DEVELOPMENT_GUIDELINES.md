# 宣教教育平台 - 开发范式指南 (中文版)

## 项目概述

**项目名称**: 宣教教育平台 (Propaganda/Education Platform)
**技术栈**: Java 8 + Spring Boot 2.7 + Vue 3 + TypeScript + MySQL 8.0 + MyBatis
**架构模式**: COLA (Clean Object-Oriented and Layered Architecture)

---

## 目录

1. [项目结构](#项目结构)
2. [分层架构](#分层架构)
3. [模块化组织](#模块化组织)
4. [命名规范](#命名规范)
5. [数据库设计](#数据库设计)
6. [API 设计规范](#api-设计规范)
7. [MyBatis 开发规范](#mybatis-开发规范)
8. [工作流引擎设计](#工作流引擎设计)
9. [前端开发规范](#前端开发规范)
10. [测试规范](#测试规范)

---

## 项目结构

```
xuanjiao-backend/
├── xuanjiao-client/       # Client 层 - DTO、API 请求/响应定义
├── xuanjiao-domain/       # Domain 层 - 实体、领域服务、Repository 接口
├── xuanjiao-infrastructure/ # Infrastructure 层 - MyBatis Mapper、Repository 实现
├── xuanjiao-app/          # Application 层 - 业务逻辑、Service 实现
├── xuanjiao-adapter/      # Adapter 层 - REST Controller、外部集成
└── xuanjiao-start/        # Start 模块 - 启动入口

xuanjiao-frontend/
├── src/api/               # API 客户端
├── src/stores/            # Pinia 状态管理
├── src/components/        # 可复用组件
├── src/views/             # 页面组件
├── src/router/            # Vue Router 配置
└── src/layouts/           # 布局组件
```

---

## 分层架构

### COLA 四层架构

```
┌─────────────────────────────────────────────────────────────┐
│  Adapter 层 (适配层)                                        │
│  - 处理 HTTP 请求/响应                                       │
│  - 参数验证 (@Valid)                                         │
│  - 调用 Application 层服务                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Application 层 (应用层)                                     │
│  - 业务逻辑编排                                             │
│  - 事务管理 (@Transactional)                                  │
│  - 调用 Domain 层和 Infrastructure 层                         │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Domain 层 (领域层)                                          │
│  - 领域实体 (Entity)                                         │
│  - Repository 接口定义                                       │
│  - 领域服务 (Domain Service)                                 │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Infrastructure 层 (基础设施层)                              │
│  - MyBatis Mapper 实现                                      │
│  - Repository 实现                                           │
│  - 数据对象 (DO)                                            │
└─────────────────────────────────────────────────────────────┘
```

**依赖规则**: Adapter → App → Domain (Client 层被所有层共享)

---

## 模块化组织

### 业务模块列表

| 模块代码 | 模块名称 | 说明 |
|---------|---------|------|
| `auth` | 认证模块 | 登录、登出、令牌管理 |
| `user` | 用户模块 | 用户 CRUD、权限查询 |
| `dept` | 部门模块 | 部门树、部门管理 |
| `role` | 角色模块 | 角色 CRUD、角色权限 |
| `menu` | 菜单模块 | 菜单树、菜单配置 |
| `asset` | 素材模块 | 素材上传、下载、管理 |
| `material` | 素材申请模块 | 素材录入申请 |
| `usage` | 使用申请模块 | 素材使用申请、使用记录 |
| `deletion` | 素材删除模块 | 素材删除申请 |
| `workflow` | 工作流模块 | 工作流定义、流程引擎 |
| `approval` | 审批执行模块 | 审批实例、任务处理 |
| `log` | 日志模块 | 操作日志记录 |

### 文件位置模式

```
{layer}/src/main/java/com/xuanjiao/{layer}/{module}/
├── entity/          # Domain 层实体
├── repository/      # Domain 层 Repository 接口
├── impl/            # 各层实现类
└── web/             # Adapter 层 Controller
```

---

## 命名规范

### 后端命名

#### 包命名
- Controller: `com.xuanjiao.adapter.web.{module}`
- Service 接口: `com.xuanjiao.app.{module}`
- Service 实现: `com.xuanjiao.app.{module}.impl`
- Mapper 接口: `com.xuanjiao.infrastructure.{module}`
- Repository 接口: `com.xuanjiao.domain.{module}.repository`
- Entity: `com.xuanjiao.domain.{module}.entity`
- DTO: `com.xuanjiao.client` 或 `com.xuanjiao.client.{module}`

#### 类命名
- Controller: `{Module}Controller`
- Service: `{Module}Service`
- Service Impl: `{Module}ServiceImpl`
- Mapper: `{Module}Mapper`
- Repository: `{Module}Repository`
- Repository Impl: `{Module}RepositoryImpl`
- Entity: `{Module}` (如 `User`, `Asset`)
- DO: `{Module}DO` (数据库映射对象)
- DTO: `{Module}DTO`

#### DTO 命名规范
```
查询 DTO: {Action}Qry      例: AssetGetDetailQry, UserGetMyTasksQry
命令 DTO: {Action}Cmd      例: AssetDeleteCmd, ApprovalApproveCmd
响应 DTO: {Module}DTO      例: UserDTO, AssetDTO
分页 DTO: PageResult<T>   例: PageResult<AssetDTO>
```

#### Mapper XML 命名
- 文件位置: `resources/mapper/{Module}Mapper.xml`
- ResultMap: `BaseResultMap`
- 列定义: `<sql id="Base_Column_List">`

### 前端命名

#### API 文件命名
```
api/{module}.ts      例: api/user.ts, api/asset.ts
```

#### Store 文件命名
```
stores/{module}.ts   例: stores/user.ts, stores/workflow.ts
```

#### 组件命名
- 页面组件: PascalCase (如 `UserList.vue`, `AssetDetail.vue`)
- 可复用组件: PascalCase (如 `AssetSelector.vue`, `UserPicker.vue`)

---

## 数据库设计

### 表命名规范
```
{业务}_{功能}        例: workflow_stage, stage_approver
关联表: {主表}_{从表}  例: usage_apply_asset, asset_tag
```

### 字段命名规范
```
主键: id
外键: {关联表}_id    例: user_id, workflow_id, asset_id
时间: {操作}_time    例: create_time, approve_time
状态: {实体}_status  例: workflow_status, asset_status
逻辑删除: deleted     默认 0=未删除, 1=已删除
```

### 特殊字段设计
```sql
-- 审批相关
approval_instance_id    -- 审批实例 ID
current_stage_id        -- 当前阶段 ID
parent_instance_id      -- 父流程实例 ID (子流程使用)

-- 工作流相关
is_first_approver        -- 是否首个审批人 (AND-sign 场景)
next_stage_approver_ids  -- 下一阶段审批人 IDs (JSON)
sub_workflow_approver_ids -- 子流程审批人 IDs (JSON)

-- 素材相关
md5                      -- 文件 MD5 (去重校验)
deletion_approve_time    -- 删除审批时间 (定时清理任务)
```

---

## API 设计规范

### POST-First 原则

**默认规则**: 所有 API 使用 POST 请求，参数放在请求体中

```java
// ✅ 正确 - 查询操作使用 POST
@PostMapping("/getDetail")
public Result<AssetDTO> getDetail(@Valid @RequestBody AssetGetDetailQry qry)

// ✅ 正确 - 命令操作使用 POST
@PostMapping("/delete")
public Result<Void> delete(@Valid @RequestBody AssetDeleteCmd cmd)

// ⚠️ 例外 - 文件预览必须用 GET (浏览器限制)
@GetMapping("/preview/{id}")
public FileSystemResource preview(@PathVariable Long id)
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### DTO 验证注解
```java
public class AssetCreateCmd {
    @NotBlank(message = "素材名称不能为空")
    private String name;

    @NotBlank(message = "素材类型不能为空")
    private String type;

    @Min(value = 1, message = "文件大小必须大于0")
    private Long fileSize;
}
```

---

## MyBatis 开发规范

### Mapper 接口定义

**所有 Mapper 必须显式定义方法，不继承 BaseMapper**

```java
@Mapper
public interface AssetMapper {
    // 基础 CRUD 方法
    AssetDO selectById(@Param("id") Long id);
    AssetDO selectOne(AssetQuery query);
    List<AssetDO> selectList(AssetQuery query);
    Long selectCount(AssetQuery query);
    IPage<AssetDO> selectPage(Page<AssetDO> page, @Param("query") AssetQuery query);
    int insert(AssetDO assetDO);
    int updateById(AssetDO assetDO);

    // 业务特定方法
    AssetDO selectByMd5(@Param("md5") String md5);
    List<AssetDO> selectByApplicationId(@Param("applicationId") Long applicationId);
}
```

### Query 对象模式

**定义专门的 Query 类替代动态条件**

```java
@Data
public class AssetQuery {
    // 基础查询条件
    private Long id;
    private String name;
    private String type;
    private String status;
    private Integer deleted;

    // 扩展查询条件
    private String nameKeyword;           // 模糊查询
    private List<String> statusIn;        // IN 查询
    private List<Long> userIds;           // IN 查询
    private Long idNotEqual;             // != 查询
    private Boolean deletedIsNull;       // IS NULL 查询
}
```

### Mapper XML 开发规范

#### ResultMap 定义
```xml
<resultMap id="BaseResultMap" type="com.xuanjiao.infrastructure.dataobject.AssetDO">
    <id column="id" property="id" jdbcType="BIGINT"/>
    <result column="name" property="name" jdbcType="VARCHAR"/>
    <result column="type" property="type" jdbcType="VARCHAR"/>
    <!-- 明确映射 column (数据库) 到 property (Java) -->
</resultMap>
```

#### 列定义规范
```xml
<sql id="Base_Column_List">
    id, name, type, file_path, thumbnail_path, file_size,
    md5, status, deleted, create_time, update_time
</sql>
```

**禁止使用 `SELECT *`**

#### 动态 SQL 规范
```xml
<!-- WHERE 子句：必须使用 <where> 标签 -->
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

#### UPDATE 语句规范
```xml
<!-- SET 子句：使用 <set> 标签 -->
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

#### INSERT 语句规范
```xml
<!-- 明确列出所有字段 -->
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

### 特殊查询场景

#### IS NULL / IS NOT NULL 查询
```java
// Query 对象中添加 Boolean 字段
@Data
public class ApprovalProgressQuery {
    private Long parentInstanceId;
    private Boolean parentInstanceIdIsNull;  // IS NULL 查询
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

#### IN 查询
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

#### != 查询
```java
@Data
public class ApprovalTaskQuery {
    private Long idNotEqual;  // != 查询
}
```

```xml
<if test="idNotEqual != null">
    AND id != #{idNotEqual}
</if>
```

### 分页查询规范

```java
// Mapper 接口
IPage<AssetDO> selectPage(Page<AssetDO> page, @Param("query") AssetQuery query);

// 调用方式
IPage<AssetDO> page = assetMapper.selectPage(
    new Page<>(pageNum, pageSize),
    query
);
```

### 强制设置字段为 NULL

**问题**: `updateById()` 方法无法将字段更新为 NULL

**解决方案**: 使用显式 XML 方法

```java
// Mapper 接口添加方法
int resetApprovers(@Param("id") Long id);

// Mapper XML
<update id="resetApprovers">
    UPDATE approval_progress
    SET approvers = NULL
    WHERE id = #{id}
</update>
```

### 字段映射规范

**column**: 数据库字段名（下划线命名，如 `role_id`）
**property**: Java 属性名（驼峰命名，如 `roleId`）

```xml
<result column="role_id" property="roleId" jdbcType="BIGINT"/>
<result column="create_time" property="createTime" jdbcType="TIMESTAMP"/>
```

---

## 工作流引擎设计

### 工作流类型

| 类型代码 | 类型名称 | 说明 |
|---------|---------|------|
| `ASSET_UPLOAD` | 素材录入 | 素材上传审批 |
| `ASSET_USAGE` | 素材使用 | 素材使用申请审批 |
| `ASSET_DELETION` | 素材删除 | 素材删除审批 |

### 流程结构
```
主流程 (Main Flow)
├── 阶段 1 (Stage 1)
│   ├── 或签/会签
│   └── 子流程 (Sub-Workflow, 可选)
├── 阶段 2 (Stage 2)
│   └── ...
```

### 审批类型

| 类型 | 规则 |
|------|------|
| **OR (或签)** | 任一审批人通过即可 |
| **AND (会签)** | 所有审批人都需通过 |

### 审批人类型

| 类型 | 说明 |
|------|-------------|
| `USER` | 指定用户 |
| `ROLE` | 指定角色 (支持二级部门过滤) |
| `DEPT` | 指定部门 |

### 子流程规则
- 在审批人级别配置 (非阶段级别)
- 独立运行，不阻塞主流程
- 完成条件: 主流程 + 所有子流程都完成

### 审批状态流
```
PENDING → MAIN_COMPLETED → APPROVED
         ↳ REJECTED
```

---

## 前端开发规范

### 组件通信
```typescript
// API 调用 - 统一使用 POST
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

### 响应式数据
```typescript
// 推荐使用 ref 或 reactive
const fileList = ref<any[]>([])
const formData = reactive({
  name: '',
  type: 'IMAGE'
})

// ❌ 避免: 直接使用 let
let count = 0
```

### 路由守卫
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

## 测试规范

### 测试层次
```
单元测试 (Unit Test)
├── Mock 外部依赖
├── 测试业务逻辑
└── 位于: xuanjiao-app/src/test

集成测试 (Integration Test)
├── @SpringBootTest
├── 测试 Mapper + Service
└── 位于: xuanjiao-start/src/test

API 测试 (API Test)
├── 完整请求-响应测试
└── 位于: xuanjiao-start/src/test
```

### 测试命名规范
```java
// 单元测试
test{MethodName}_{Scenario}_{ExpectedResult}
例: testSubmit_WithValidData_ReturnsInstanceId

// 集成测试
test{MapperName}_{Operation}
例: testAssetMapper_selectList
```

---

## 常见问题与解决方案

### 1. IS NULL 查询
```java
// ❌ 错误
query.setParentInstanceId(null)  // 无法生成正确的 IS NULL

// ✅ 正确 - 使用专门的字段
query.setParentInstanceIdIsNull(true)  // 生成 "IS NULL"
query.setParentInstanceIdIsNull(false) // 生成 "IS NOT NULL"
```

### 2. 字段类型不匹配
```xml
<!-- column: 数据库字段 (下划线) -->
<!-- property: Java 属性 (驼峰) -->
<result column="role_id" property="roleId"/>
```

### 3. updateById() 无法更新为 NULL
```java
// ❌ 不工作 - null 值被忽略
progress.setApprovers(null);
progressMapper.updateById(progress);

// ✅ 正确 - 使用显式 XML 方法
progressMapper.resetApprovers(progress.getId());
```

### 4. FormData 参数类型
```typescript
// 前端 FormData 会将数字转为字符串
formData.append('applicationId', data.applicationId)

// 后端 @ModelAttribute 会自动转换
public Result<AssetDTO> upload(
    @RequestParam("file") MultipartFile file,
    @ModelAttribute AssetUploadCmd cmd
)
```

---

## 版本历史

- **v1.0** (2025-01) - 初始版本，COLA 架构
- **v1.1** (2025-01) - 添加子流程支持
- **v1.2** (2025-02) - Mapper 重构，使用纯 XML Mapper
- **v1.3** (2025-02) - 移除 MyBatis-Plus 依赖，使用原生 MyBatis

---

## 附录

### 相关文档
- `REQUIREMENTS.md` - 产品需求文档
- `PROGRESS.md` - 开发进度
- `WORKFLOW_REFACTOR_SUMMARY.md` - 工作流重构总结
- `MAPPER_REFACTOR_LOG.md` - Mapper 重构日志

### 联系方式
- 技术问题: 查看项目 Wiki 或提交 Issue
