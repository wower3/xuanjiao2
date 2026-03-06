# API_ROLE.md - 角色管理接口文档

> **模块**: 角色管理 (role)
> **Controller**: `RoleController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/role/`
> **创建时间**: 2026-03-04

---

## 目录

1. [角色列表](#1-角色列表)
2. [角色详情](#2-角色详情)
3. [新增角色](#3-新增角色)
4. [更新角色](#4-更新角色)
5. [删除角色](#5-删除角色)
6. [分配角色菜单权限](#6-分配角色菜单权限)
7. [获取角色菜单权限](#7-获取角色菜单权限)

---

## 1. 角色列表

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 角色列表 |
| 请求方法 | POST |
| 请求路径 | `/api/role/getList` |
| 接口描述 | 查询系统中所有角色的列表 |

### 请求参数

**RoleGetListQry**:
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
      "name": "系统管理员",
      "description": "拥有所有权限",
      "roleType": "SYSTEM_ADMIN",
      "status": 1
    },
    {
      "id": 2,
      "name": "总消保管理岗",
      "description": "可查看所有数据",
      "roleType": "GENERAL_MGMT",
      "status": 1
    },
    {
      "id": 3,
      "name": "分消保管理岗",
      "description": "只能查看所属分部数据",
      "roleType": "BRANCH_MGMT",
      "status": 1
    }
  ]
}
```

### 调用链路

| 层级 | 类名/方法 | 文件位置 | 说明 |
|-----|----------|---------|------|
| Adapter | `RoleController.list()` | `adapter/web/role/RoleController.java:65` | 接收HTTP请求 |
| App | `RoleService.list()` | `app/role/RoleService.java:38` | 业务逻辑 |
| App | `RoleServiceImpl.list()` | `app/role/impl/RoleServiceImpl.java:39` | 实现逻辑，按ID倒序 |
| Infrastructure | `RoleMapper.selectList()` | `infrastructure/role/RoleMapper.java:34` | SQL查询 |
| Database | `sys_role` 表 | MySQL | 数据表 |

### 数据库表

| 表名 | 操作 | 字段 |
|-----|------|------|
| sys_role | SELECT | id, name, description, role_type, status |

---

## 2. 角色详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取角色详情 |
| 请求方法 | POST |
| 请求路径 | `/api/role/getDetail` |
| 接口描述 | 根据角色ID查询角色的详细信息，包含关联的菜单ID列表 |

### 请求参数

**RoleGetDetailQry**:
```json
{
  "id": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 角色ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "系统管理员",
    "description": "拥有所有权限",
    "roleType": "SYSTEM_ADMIN",
    "status": 1,
    "menuIds": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `RoleController.getById()` | 接收HTTP请求 |
| App | `RoleService.getById()` | 业务逻辑 |
| App | `RoleServiceImpl.getById()` | 查询角色信息 |
| App | `MenuService.getMenuIdsByRoleId()` | 查询角色关联的菜单ID列表 |
| Infrastructure | `RoleMapper.selectById()` | SQL查询 |

---

## 3. 新增角色

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 新增角色 |
| 请求方法 | POST |
| 请求路径 | `/api/role/create` |
| 接口描述 | 创建新的角色，可同时分配菜单权限 |

### 请求参数

**RoleCreateCmd**:
```json
{
  "name": "内容审核员",
  "description": "负责素材内容审核",
  "roleType": "CONTENT_AUDITOR",
  "menuIds": [5, 6, 7]
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| name | String | 是 | 角色名称 | @NotBlank |
| description | String | 否 | 角色描述 | - |
| roleType | String | 是 | 角色类型 | @NotBlank, 格式: `^[A-Z0-9_]+$` |
| menuIds | List\<Long\> | 否 | 关联的菜单ID列表 | - |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "角色类型已存在：CONTENT_AUDITOR"
}
```

```json
{
  "code": 500,
  "message": "角色类型只能包含大写字母、数字和下划线"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `RoleController.create()` | 接收请求，转换DTO |
| App | `RoleService.create()` | 业务逻辑 |
| App | `RoleServiceImpl.validateRoleType()` | 校验角色类型格式和唯一性 |
| App | `RoleServiceImpl.create()` | 创建角色 |
| App | `MenuService.assignMenusToRole()` | 分配菜单权限 |
| Infrastructure | `RoleMapper.insert()` | 插入数据库 |

### 角色类型规则

| 规则 | 说明 |
|-----|------|
| 格式 | 只能包含大写字母、数字和下划线 `^[A-Z0-9_]+$` |
| 唯一性 | 角色类型必须唯一，不能重复 |
| 预设类型 | SYSTEM_ADMIN, GENERAL_MGMT, BRANCH_MGMT |

---

## 4. 更新角色

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新角色 |
| 请求方法 | POST |
| 请求路径 | `/api/role/update` |
| 接口描述 | 修改角色信息，可同时更新菜单权限 |

### 请求参数

**RoleUpdateCmd**:
```json
{
  "id": 5,
  "name": "高级审核员",
  "description": "负责素材内容审核和审批",
  "roleType": "CONTENT_AUDITOR",
  "menuIds": [5, 6, 7, 8]
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 角色ID |
| name | String | 否 | 角色名称 |
| description | String | 否 | 角色描述 |
| roleType | String | 否 | 角色类型 |
| menuIds | List\<Long\> | 否 | 关联的菜单ID列表 |

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
| Adapter | `RoleController.update()` | 接收请求，转换DTO |
| App | `RoleService.update()` | 业务逻辑 |
| App | `RoleServiceImpl.validateRoleType()` | 校验角色类型（排除自身） |
| App | `RoleServiceImpl.update()` | 更新角色 |
| App | `MenuService.assignMenusToRole()` | 更新菜单权限 |
| Infrastructure | `RoleMapper.updateById()` | 更新数据库 |

---

## 5. 删除角色

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除角色 |
| 请求方法 | POST |
| 请求路径 | `/api/role/delete` |
| 接口描述 | 删除指定角色（逻辑删除） |

### 请求参数

**RoleDeleteCmd**:
```json
{
  "id": 5
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 角色ID |

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
| Adapter | `RoleController.delete()` | 接收HTTP请求 |
| App | `RoleService.delete()` | 业务逻辑 |
| Infrastructure | `RoleMapper.deleteById()` | 逻辑删除 |

### 注意事项

> 删除角色前需要确保：
> 1. 角色未分配给任何用户
> 2. 如果有用户使用该角色，需要先将用户转移到其他角色

---

## 6. 分配角色菜单权限

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 分配角色菜单权限 |
| 请求方法 | POST |
| 请求路径 | `/api/role/{roleId}/menus` |
| 接口描述 | 为指定角色分配菜单访问权限，会覆盖原有权限 |

### 请求参数

**URL参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| roleId | Long | 是 | 角色ID（路径参数） |

**Body参数**:
```json
[1, 2, 3, 5, 6, 7]
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| - | List\<Long\> | 是 | 菜单ID列表 |

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
| Adapter | `RoleController.assignMenus()` | 接收请求 |
| App | `RoleService.assignMenus()` | 业务逻辑 |
| App | `MenuService.assignMenusToRole()` | 分配菜单权限 |
| Infrastructure | `RoleMenuMapper` | 更新角色-菜单关联表 |

---

## 7. 获取角色菜单权限

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取角色菜单权限 |
| 请求方法 | POST |
| 请求路径 | `/api/role/getRoleMenus` |
| 接口描述 | 查询指定角色已分配的菜单ID列表 |

### 请求参数

**RoleGetRoleMenusQry**:
```json
{
  "roleId": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| roleId | Long | 是 | 角色ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `RoleController.getRoleMenus()` | 接收HTTP请求 |
| App | `RoleService.getMenuIdsByRoleId()` | 业务逻辑 |
| App | `MenuService.getMenuIdsByRoleId()` | 查询菜单权限 |
| Infrastructure | `RoleMenuMapper` | 查询角色-菜单关联表 |

---

## 附录：预设角色说明

| 角色ID | 角色名称 | 角色类型 | 说明 |
|-------|---------|---------|------|
| 1 | 系统管理员 | SYSTEM_ADMIN | 拥有所有权限，可查看所有部门，可分配所有角色 |
| 4 | 总消保管理岗 | GENERAL_MGMT | 可查看所有部门，可分配所有角色 |
| 5 | 分消保管理岗 | BRANCH_MGMT | 只能查看所属二级机构及子部门，不能分配系统管理员和总消保管理岗角色 |

---

## 附录：数据库表结构

### sys_role (角色表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 角色名称 |
| description | VARCHAR | 角色描述 |
| role_type | VARCHAR | 角色类型（唯一，格式：`^[A-Z0-9_]+$`） |
| status | INT | 状态（1=启用，0=禁用） |
| deleted | INT | 逻辑删除标记（0=正常，1=已删除） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### sys_role_menu (角色菜单关联表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| role_id | BIGINT | 角色ID |
| menu_id | BIGINT | 菜单ID |

---

## 附录：角色类型格式校验

```java
// 正则表达式
Pattern pattern = Pattern.compile("^[A-Z0-9_]+$");

// 有效示例
"SYSTEM_ADMIN"     // ✓ 有效
"GENERAL_MGMT"     // ✓ 有效
"BRANCH_MGMT"      // ✓ 有效
"CONTENT_AUDITOR"  // ✓ 有效
"SALES_MANAGER_1"  // ✓ 有效

// 无效示例
"SystemAdmin"      // ✗ 包含小写字母
"SYSTEM-ADMIN"     // ✗ 包含连字符
"SYSTEM ADMIN"     // ✗ 包含空格
"系统管理员"       // ✗ 包含中文字符
```

---

## 附录：前端调用示例

### 获取角色列表

```typescript
// xuanjiao-frontend/src/api/role.ts
export const getRoleList = () => {
  return request.post<RoleDTO[]>('/role/getList', {})
}

// 使用示例
import { getRoleList } from '@/api/role'

const loadRoles = async () => {
  try {
    const roles = await getRoleList()
    console.log('角色列表:', roles.data)
  } catch (error) {
    console.error('加载角色列表失败', error)
  }
}
```

### 创建角色

```typescript
export const createRole = (data: {
  name: string
  description?: string
  roleType: string
  menuIds?: number[]
}) => {
  return request.post('/role/create', data)
}
```

### 分配菜单权限

```typescript
export const assignRoleMenus = (roleId: number, menuIds: number[]) => {
  return request.post(`/role/${roleId}/menus`, menuIds)
}
```

---

*本文档由API接口文档生成方法论自动生成。*
