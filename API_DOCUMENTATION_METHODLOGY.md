# API接口文档生成方法论

> **创建时间**: 2026-03-04
> **目的**: 为项目交接提供完整的接口文档，记录每个请求从前端到数据库的完整调用链路

---

## 一、文档组织方式

### 1.1 按后端Controller组织

文档按照 `xuanjiao-adapter` 层的Controller进行分组，每个Controller对应一个独立的文档文件。

| Controller | 文件名 | 模块 |
|-----------|--------|------|
| AuthController | API_AUTH.md | 认证管理 |
| UserController | API_USER.md | 用户管理 |
| DeptController | API_DEPT.md | 部门管理 |
| RoleController | API_ROLE.md | 角色管理 |
| MenuController | API_MENU.md | 菜单管理 |
| AssetController | API_ASSET.md | 素材管理 |
| TagController | API_TAG.md | 标签管理 |
| MaterialApplicationController | API_MATERIAL.md | 素材录入申请 |
| UsageApplyController | API_USAGE.md | 素材使用申请 |
| AssetDeletionController | API_DELETION.md | 素材删除申请 |
| WorkflowController | API_WORKFLOW.md | 流程管理 |
| ApproverSelectionController | API_APPROVER_SELECTION.md | 审批人选择 |
| ApprovalController | API_APPROVAL.md | 审批管理 |
| NotificationController | API_NOTIFICATION.md | 通知管理 |
| UsageLogController | API_LOG.md | 使用日志 |
| TaskController | API_TASK.md | 我的任务 |

---

## 二、接口文档模板

每个接口包含以下内容：

### 2.1 基础信息
- **接口名称**: 功能描述
- **请求方法**: POST / GET
- **请求路径**: `/api/{module}/{action}`
- **接口描述**: 详细功能说明

### 2.2 请求参数
| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| param | String | 是 | 参数说明 | @NotBlank |

### 2.3 响应结果
```json
{
  "code": 200,
  "message": "success",
  "data": {...}
}
```

### 2.4 调用链路（COLA架构）

```
前端 Vue3 → Adapter → App → Infrastructure → Database
   ↓          ↓         ↓           ↓            ↓
  API   Controller  Service  Repository/Mapper  MySQL
```

| 层级 | 类名/方法 | 说明 |
|-----|----------|------|
| Adapter | `xxxController.xxx()` | 接收HTTP请求，参数验证 |
| App | `xxxService.xxx()` | 业务逻辑处理 |
| Infrastructure | `xxxRepositoryImpl.xxx()` | 数据访问实现 |
| Infrastructure | `xxxMapper.xxx()` | MyBatis SQL映射 |
| Database | 表名 | 数据库表操作 |

### 2.5 权限要求
- **登录认证**: 是否需要登录
- **角色要求**: 允许访问的角色列表
- **数据权限**: 数据范围限制说明

### 2.6 数据库表
| 表名 | 说明 | 操作类型 |
|-----|------|---------|
| sys_user | 用户表 | SELECT |

### 2.7 错误码
| 错误码 | 说明 |
|-------|------|
| 401 | 未登录或登录已过期 |
| 403 | 无权限访问 |

---

## 三、生成步骤

### 步骤1: 读取Controller代码
```bash
Read: xuanjiao-backend/xuanjiao-adapter/.../xxxController.java
```

### 步骤2: 分析接口注解
- `@PostMapping` / `@GetMapping`: 请求方法和路径
- `@ApiOperation`: 接口描述（Swagger）
- `@RequestBody` / `@RequestParam`: 请求参数
- `@Valid`: 参数验证启用
- `@RequestAttribute("userId")`: 用户ID注入

### 步骤3: 追踪Service层
从Controller注入的Service字段找到对应的服务实现：
```java
@Resource
private XxxService xxxService;
```

### 步骤4: 追踪Repository/Mapper层
从Service实现找到数据访问层：
```java
// Repository接口 (domain层)
public interface XxxRepository {
    XxxDTO getById(Long id);
}

// Repository实现 (infrastructure层)
@Repository
public class XxxRepositoryImpl implements XxxRepository {
    @Resource
    private XxxMapper xxxMapper;
}

// MyBatis Mapper (infrastructure层)
public interface XxxMapper {
    XxxDO selectById(Long id);
}
```

### 步骤5: 查找MyBatis XML映射
```bash
Glob: **/infrastructure/**/XxxMapper.xml
```

### 步骤6: 识别数据库表
从Mapper XML的 `resultMap` 和 SQL语句识别操作的数据表。

---

## 四、COLA架构调用链路图

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端 Vue3                                 │
│                    (xuanjiao-frontend/)                          │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP POST /api/xxx
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│              Adapter 层 (xuanjiao-adapter/)                       │
│         ┌─────────────────────────────────────────┐              │
│         │  XxxController.xxx()                    │              │
│         │  - @RestController                      │              │
│         │  - @RequestMapping("/xxx")              │              │
│         │  - 参数验证 (@Valid @RequestBody)       │              │
│         │  - 用户注入 (@RequestAttribute userId)   │              │
│         └─────────────────┬───────────────────────┘              │
└────────────────────────────┼───────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│               App 层 (xuanjiao-app/)                              │
│         ┌─────────────────────────────────────────┐              │
│         │  XxxService (接口)                      │              │
│         │  └─ impl/XxxServiceImpl (实现)          │              │
│         │  - 业务逻辑处理                          │              │
│         │  - 事务管理 (@Transactional)            │              │
│         │  - 调用Repository接口                   │              │
│         └─────────────────┬───────────────────────┘              │
└────────────────────────────┼───────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│         Domain 层 (xuanjiao-domain/)                             │
│         ┌─────────────────────────────────────────┐              │
│         │  XxxRepository (接口)                    │              │
│         │  - 定义数据访问规范                      │              │
│         │  - 返回DTO对象                          │              │
│         └─────────────────┬───────────────────────┘              │
└────────────────────────────┼───────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│      Infrastructure 层 (xuanjiao-infrastructure/)                 │
│         ┌─────────────────────────────────────────┐              │
│         │  XxxRepositoryImpl                       │              │
│         │  - 实现Repository接口                   │              │
│         │  - 调用Mapper执行SQL                    │              │
│         │  - DO与DTO转换 (ConvertUtils)           │              │
│         └─────────────────┬───────────────────────┘              │
│         ┌─────────────────────────────────────────┐              │
│         │  XxxMapper (接口)                        │              │
│         │  - 定义数据库操作方法                    │              │
│         │  - 配合XML执行SQL                       │              │
│         └─────────────────┬───────────────────────┘              │
│         ┌─────────────────────────────────────────┐              │
│         │  XxxMapper.xml                           │              │
│         │  - SQL语句定义                          │              │
│         │  - ResultMap映射                        │              │
│         └─────────────────┬───────────────────────┘              │
└────────────────────────────┼───────────────────────────────────┘
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                    数据库 MySQL 8.0                              │
│                    (Database: xuanjiao_s)                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 五、重要说明

### 5.1 POST-First原则
项目遵循POST-First设计原则：
- **查询接口**: `POST /{module}/get{Action}` (如 `POST /user/getList`)
- **命令接口**: `POST /{module}/{action}` (如 `POST /user/create`)
- **GET仅用于**: 文件预览/下载等特殊场景

### 5.2 DTO命名规范
- **查询DTO**: `{Action}Qry` (如 `UserGetListQry`)
- **命令DTO**: `{Action}Cmd` (如 `UserCreateCmd`)
- **响应DTO**: `{Module}DTO` (如 `UserDTO`)

### 5.3 用户认证机制
- **JWT Token**: 存储在请求头 `Authorization: Bearer <token>`
- **用户ID注入**: 通过拦截器注入到 `@RequestAttribute("userId")`
- **权限控制**: 基于角色(role_id)和部门(dept_id)

### 5.4 分页参数
```json
{
  "pageNum": 1,
  "pageSize": 10
}
```

### 5.5 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {...}
}
```

分页响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

---

## 六、生成进度

| 序号 | Controller | 文件名 | 状态 |
|-----|-----------|--------|------|
| 1 | AuthController | API_AUTH.md | 待生成 |
| 2 | UserController | API_USER.md | 待生成 |
| 3 | DeptController | API_DEPT.md | 待生成 |
| 4 | RoleController | API_ROLE.md | 待生成 |
| 5 | MenuController | API_MENU.md | 待生成 |
| 6 | AssetController | API_ASSET.md | 待生成 |
| 7 | TagController | API_TAG.md | 待生成 |
| 8 | MaterialApplicationController | API_MATERIAL.md | 待生成 |
| 9 | UsageApplyController | API_USAGE.md | 待生成 |
| 10 | AssetDeletionController | API_DELETION.md | 待生成 |
| 11 | WorkflowController | API_WORKFLOW.md | 待生成 |
| 12 | ApproverSelectionController | API_APPROVER_SELECTION.md | 待生成 |
| 13 | ApprovalController | API_APPROVAL.md | 待生成 |
| 14 | NotificationController | API_NOTIFICATION.md | 待生成 |
| 15 | UsageLogController | API_LOG.md | 待生成 |
| 16 | TaskController | API_TASK.md | 待生成 |

---

## 七、快速查找代码位置

### 7.1 Controller层
```
xuanjiao-backend/xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/{module}/
```

### 7.2 Service层
```
xuanjiao-backend/xuanjiao-app/src/main/java/com/xuanjiao/app/{module}/
├── XxxService.java (接口)
└── impl/XxxServiceImpl.java (实现)
```

### 7.3 Repository层
```
xuanjiao-backend/xuanjiao-domain/src/main/java/com/xuanjiao/domain/{module}/repository/
└── XxxRepository.java (接口)

xuanjiao-backend/xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/
└── XxxRepositoryImpl.java (实现)
```

### 7.4 Mapper层
```
xuanjiao-backend/xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/
└── XxxMapper.java

xuanjiao-backend/xuanjiao-infrastructure/src/main/resources/mapper/{module}/
└── XxxMapper.xml
```

### 7.5 DTO层
```
xuanjiao-backend/xuanjiao-client/src/main/java/com/xuanjiao/client/{module}/
├── *Qry.java (查询DTO)
├── *Cmd.java (命令DTO)
└── *DTO.java (响应DTO)
```

---

*本文档由AI辅助生成，用于项目交接文档的方法论指导。*
