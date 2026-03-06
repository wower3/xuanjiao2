# API_USER.md - 用户管理接口文档

> **模块**: 用户管理 (user)
> **Controller**: `UserController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/user/`
> **创建时间**: 2026-03-04

---

## 目录

1. [获取当前用户](#1-获取当前用户)
2. [用户列表](#2-用户列表)
3. [用户列表（带筛选条件）](#3-用户列表带筛选条件)
4. [搜索用户（分页）](#4-搜索用户分页)
5. [获取默认筛选部门](#5-获取默认筛选部门)
6. [新增用户](#6-新增用户)
7. [更新用户](#7-更新用户)
8. [删除用户](#8-删除用户)

---

## 1. 获取当前用户

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取当前用户 |
| 请求方法 | GET |
| 请求路径 | `/api/user/current` |
| 接口描述 | 根据用户ID查询当前登录用户的详细信息，包含角色和部门信息 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 来源 |
|-------|------|------|------|------|
| userId | Long | 是 | 当前登录用户ID | @RequestAttribute 自动注入 |

**请求示例**:
```http
GET /api/user/current
Authorization: Bearer {token}
```

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "deptId": 1,
    "deptName": "总公司",
    "roleId": 1,
    "roleName": "系统管理员",
    "roleType": "SYSTEM_ADMIN",
    "status": 1
  }
}
```

### 调用链路

| 层级 | 类名/方法 | 文件位置 | 说明 |
|-----|----------|---------|------|
| Adapter | `UserController.getCurrentUser()` | `adapter/web/user/UserController.java:93` | 接收HTTP请求 |
| App | `UserService.getCurrentUser()` | `app/user/UserService.java:38` | 业务逻辑 |
| App | `UserServiceImpl.getById()` | `app/user/impl/UserServiceImpl.java:134` | 实现逻辑 |
| Infrastructure | `UserMapper.selectById()` | `infrastructure/user/UserMapper.java:26` | SQL查询 |
| Infrastructure | `RoleMapper.selectById()` | `infrastructure/role/RoleMapper.java` | 角色信息填充 |
| Infrastructure | `DeptMapper.selectById()` | `infrastructure/dept/DeptMapper.java` | 部门信息填充 |
| Database | `sys_user`, `sys_role`, `sys_dept` | MySQL | 数据表 |

### 数据库表

| 表名 | 操作 | 字段 |
|-----|------|------|
| sys_user | SELECT | id, username, real_name, email, phone, dept_id, role_id, status |
| sys_role | SELECT | id, name, role_type |
| sys_dept | SELECT | id, name |

---

## 2. 用户列表

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 用户列表 |
| 请求方法 | POST |
| 请求路径 | `/api/user/getList` |
| 接口描述 | 查询用户列表，分消保管理岗只能查看所属二级机构的用户 |

### 请求参数

**UserGetListQry**:
```json
{}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| - | - | - | 当前无过滤参数 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "deptId": 1,
      "deptName": "总公司",
      "roleId": 1,
      "roleName": "系统管理员",
      "roleType": "SYSTEM_ADMIN"
    }
  ]
}
```

### 调用链路

| 层级 | 类名/方法 | 说明 |
|-----|----------|------|
| Adapter | `UserController.list()` | 接收请求，判断角色类型 |
| App | `UserService.list()` 或 `listByBranchDept()` | 根据角色返回不同范围数据 |
| Infrastructure | `UserMapper.selectList()` | 查询所有用户后过滤 |

### 权限控制

| 角色 | 数据范围 |
|-----|---------|
| SYSTEM_ADMIN | 所有用户 |
| GENERAL_MGMT | 所有用户 |
| BRANCH_MGMT | 所属二级机构及子部门的用户 |

---

## 3. 用户列表（带筛选条件）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 用户列表（带筛选条件） |
| 请求方法 | POST |
| 请求路径 | `/api/user/getListWithFilter` |
| 接口描述 | 根据角色ID、部门ID筛选用户，支持包含子部门 |

### 请求参数

**UserGetListWithFilterQry**:
```json
{
  "roleIds": [1, 2],
  "deptId": 10,
  "includeSubDept": true
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| roleIds | List\<Long\> | 否 | 角色ID列表 |
| deptId | Long | 否 | 部门ID |
| includeSubDept | Boolean | 否 | 是否包含子部门 |

### 响应结果

同"用户列表"接口。

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UserController.listWithFilter()` | 接收筛选条件 |
| App | `UserService.listWithFilter()` | 应用权限过滤 + 用户筛选 |
| App | `UserServiceImpl.getAllowedDeptIds()` | 计算允许的部门范围 |
| App | `UserServiceImpl.getAllSubDeptIds()` | 递归获取子部门 |

### 权限控制逻辑

```
1. 获取当前用户角色
2. 计算允许查询的部门范围:
   - SYSTEM_ADMIN/GENERAL_MGMT → 所有部门
   - BRANCH_MGMT → 所属二级机构及子部门
   - 其他 → 空集合（无权限）
3. 应用用户指定的筛选条件（角色、部门、子部门）
4. 返回筛选后的用户列表
```

---

## 4. 搜索用户（分页）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 搜索用户（支持角色/部门/姓名筛选，带分页） |
| 请求方法 | POST |
| 请求路径 | `/api/user/search` |
| 接口描述 | 分页查询用户，支持多条件筛选，用于审批人选择器 |

### 请求参数

**UserGetListWithFilterQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "roleIds": [1, 2],
  "deptId": 10,
  "includeSubDept": true,
  "keyword": "张"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| pageNum | Integer | 是 | 页码 |
| pageSize | Integer | 是 | 每页数量 |
| roleIds | List\<Long\> | 否 | 角色ID列表 |
| deptId | Long | 否 | 部门ID |
| includeSubDept | Boolean | 否 | 是否包含子部门 |
| keyword | String | 否 | 搜索关键词（匹配用户名/姓名） |

### 响应结果

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

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UserController.searchUsers()` | 接收搜索条件 |
| App | `UserService.searchUsers()` | 应用权限+筛选+分页 |

### 搜索逻辑

```
1. 获取当前用户权限范围（允许查询的部门）
2. 计算最终筛选部门集合（权限范围 ∩ 用户筛选条件）
3. 筛选用户:
   - 部门必须在筛选集合内
   - 角色匹配（如果指定了roleIds）
   - 用户名或姓名包含关键词（如果指定了keyword）
4. 内存分页返回结果
```

---

## 5. 获取默认筛选部门

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取当前用户的默认筛选部门 |
| 请求方法 | POST |
| 请求路径 | `/api/user/getDefaultFilterDept` |
| 接口描述 | 返回分消保管理岗的默认部门筛选配置 |

### 请求参数

**UserGetDefaultFilterDeptQry**:
```json
{}
```

### 响应结果

**分消保管理岗**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hasFilter": true,
    "deptId": 100,
    "includeSubDept": true,
    "canAssignAllRoles": false,
    "allowedDeptIds": [100, 101, 102, 105],
    "rootDeptId": 100
  }
}
```

**其他角色**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hasFilter": false,
    "deptId": null,
    "includeSubDept": null,
    "canAssignAllRoles": null,
    "allowedDeptIds": null,
    "rootDeptId": null
  }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|-----|------|------|
| hasFilter | Boolean | 是否有默认筛选 |
| deptId | Long | 默认筛选的部门ID |
| includeSubDept | Boolean | 是否包含子部门 |
| canAssignAllRoles | Boolean | 是否可分配所有角色 |
| allowedDeptIds | Set\<Long\> | 允许选择的部门ID集合 |
| rootDeptId | Long | 根部门ID（用于前端构建部门树） |

---

## 6. 新增用户

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 新增用户 |
| 请求方法 | POST |
| 请求路径 | `/api/user/create` |
| 接口描述 | 创建新用户，默认密码123456（MD5加密） |

### 请求参数

**UserCreateCmd**:
```json
{
  "username": "zhangsan",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13900139000",
  "deptId": 10,
  "roleId": 3
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| username | String | 是 | 用户名 | @NotBlank |
| realName | String | 是 | 姓名 | @NotBlank |
| email | String | 否 | 邮箱 | @Email |
| phone | String | 否 | 电话 | - |
| deptId | Long | 是 | 部门ID | @NotNull |
| roleId | Long | 是 | 角色ID | @NotNull |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UserController.create()` | 接收请求，权限检查 |
| Adapter | `checkCreateUpdatePermission()` | 检查部门权限 |
| Adapter | `checkRoleAssignmentPermission()` | 检查角色分配权限 |
| App | `UserService.create()` | 创建用户 |
| Infrastructure | `UserMapper.insert()` | 插入数据库 |

### 权限控制

| 当前角色 | 可创建部门范围 | 可分配角色 |
|---------|---------------|-----------|
| SYSTEM_ADMIN | 所有部门 | 所有角色 |
| GENERAL_MGMT | 所有部门 | 所有角色 |
| BRANCH_MGMT | 允许的部门 | 除系统管理员(1)和总消保管理岗(4)外的角色 |

### 默认值

| 字段 | 默认值 |
|-----|-------|
| password | MD5("123456") |
| status | 1（启用） |

---

## 7. 更新用户

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新用户 |
| 请求方法 | POST |
| 请求路径 | `/api/user/update` |
| 接口描述 | 修改用户信息（姓名、邮箱、电话、部门、角色） |

### 请求参数

**UserUpdateCmd**:
```json
{
  "id": 10,
  "username": "zhangsan",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13900139000",
  "deptId": 11,
  "roleId": 3
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 用户ID |
| username | String | 否 | 用户名 |
| realName | String | 否 | 姓名 |
| email | String | 否 | 邮箱 |
| phone | String | 否 | 电话 |
| deptId | Long | 否 | 部门ID |
| roleId | Long | 否 | 角色ID |

### 响应结果

成功:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

失败:
```json
{
  "code": 500,
  "message": "用户不存在"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UserController.update()` | 接收请求，权限检查 |
| App | `UserService.getById()` | 检查用户是否存在 |
| App | `UserService.update()` | 更新用户 |
| Infrastructure | `UserMapper.updateById()` | 更新数据库 |

---

## 8. 删除用户

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除用户 |
| 请求方法 | POST |
| 请求路径 | `/api/user/delete` |
| 接口描述 | 删除指定用户（逻辑删除，设置deleted=1） |

### 请求参数

**UserDeleteCmd**:
```json
{
  "id": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 用户ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `UserController.delete()` | 接收请求，权限检查 |
| App | `UserService.delete()` | 删除用户 |
| Infrastructure | `UserMapper.deleteById()` | 逻辑删除 |

---

## 附录：角色类型说明

| 角色类型代码 | 角色类型名称 | 说明 |
|-------------|-------------|------|
| SYSTEM_ADMIN | 系统管理员 | 全部权限，可查看所有部门，可分配所有角色 |
| GENERAL_MGMT | 总消保管理岗 | 可查看所有部门，可分配所有角色 |
| BRANCH_MGMT | 分消保管理岗 | 只能查看所属二级机构及子部门，不能分配系统管理员和总消保管理岗角色 |

---

## 附录：数据权限规则

### 部门层级说明

```
一级机构 (level=1): 总公司
├── 二级机构A (level=2)
│   ├── 三级机构A1 (level=3)
│   └── 三级机构A2 (level=3)
└── 二级机构B (level=2)
    ├── 三级机构B1 (level=3)
    └── 三级机构B2 (level=3)
```

### 数据范围计算

1. **二级机构获取**: 从用户所在部门向上遍历，找到 `level=2` 的部门
2. **子部门获取**: 递归查询指定部门的所有子部门
3. **允许部门集合**:
   - `SYSTEM_ADMIN/GENERAL_MGMT`: 所有部门
   - `BRANCH_MGMT`: 二级机构 + 所有子部门
   - 其他: 空集合（无权限）

---

## 附录：数据库表结构

### sys_user (用户表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR | 用户名（唯一） |
| password | VARCHAR | 密码（MD5加密） |
| real_name | VARCHAR | 姓名 |
| email | VARCHAR | 邮箱 |
| phone | VARCHAR | 电话 |
| dept_id | BIGINT | 部门ID |
| role_id | BIGINT | 角色ID |
| status | INT | 状态（1=启用，0=禁用） |
| deleted | INT | 逻辑删除标记（0=正常，1=已删除） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### sys_role (角色表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 角色名称 |
| role_type | VARCHAR | 角色类型 |
| description | VARCHAR | 描述 |

### sys_dept (部门表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 部门名称 |
| code | VARCHAR | 部门编码 |
| parent_id | BIGINT | 父部门ID |
| level | INT | 层级（1=一级机构，2=二级机构...） |
| sort | INT | 排序 |

---

*本文档由API接口文档生成方法论自动生成。*
