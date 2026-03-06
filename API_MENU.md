# API_MENU.md - 菜单管理接口文档

> **模块**: 菜单管理 (menu)
> **Controller**: `MenuController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/menu/`
> **创建时间**: 2026-03-04

---

## 目录

1. [获取菜单树](#1-获取菜单树)
2. [获取当前用户菜单](#2-获取当前用户菜单)
3. [菜单详情](#3-菜单详情)
4. [新增菜单](#4-新增菜单)
5. [更新菜单](#5-更新菜单)
6. [删除菜单](#6-删除菜单)
7. [分配角色菜单权限](#7-分配角色菜单权限)
8. [获取角色菜单权限](#8-获取角色菜单权限)

---

## 1. 获取菜单树

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取菜单树 |
| 请求方法 | POST |
| 请求路径 | `/api/menu/getTree` |
| 接口描述 | 查询系统中所有菜单的树形结构，用于后台管理界面展示菜单配置 |

### 请求参数

**MenuGetTreeQry**:
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
      "name": "系统管理",
      "path": "/system",
      "component": "Layout",
      "icon": "Setting",
      "sort": 1,
      "parentId": 0,
      "type": "MENU",
      "children": [
        {
          "id": 2,
          "name": "用户管理",
          "path": "/system/user",
          "component": "system/user/index",
          "icon": "User",
          "sort": 1,
          "parentId": 1,
          "type": "MENU",
          "children": []
        },
        {
          "id": 3,
          "name": "角色管理",
          "path": "/system/role",
          "component": "system/role/index",
          "icon": "UserFilled",
          "sort": 2,
          "parentId": 1,
          "type": "MENU",
          "children": []
        }
      ]
    }
  ]
}
```

### 调用链路

| 层级 | 类名/方法 | 文件位置 | 说明 |
|-----|----------|---------|------|
| Adapter | `MenuController.getTree()` | `adapter/web/menu/MenuController.java:68` | 接收HTTP请求 |
| App | `MenuService.getTree()` | `app/menu/MenuService.java:35` | 业务逻辑 |
| App | `MenuServiceImpl.getTree()` | `app/menu/impl/MenuServiceImpl.java:44` | 实现逻辑 |
| App | `buildTree()` | `app/menu/impl/MenuServiceImpl.java:159` | 递归构建树形结构 |
| Infrastructure | `MenuMapper.selectList()` | `infrastructure/menu/MenuMapper.java:34` | SQL查询 |
| Database | `sys_menu` 表 | MySQL | 数据表 |

### 数据库表

| 表名 | 操作 | 条件 |
|-----|------|------|
| sys_menu | SELECT | type='MENU', status=1, ORDER BY sort ASC |

---

## 2. 获取当前用户菜单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取当前用户菜单 |
| 请求方法 | POST |
| 请求路径 | `/api/menu/getCurrent` |
| 接口描述 | 根据当前登录用户的角色权限，查询其可访问的菜单树，用于前端动态生成导航菜单 |

### 请求参数

**MenuGetCurrentQry**:
```json
{}
```

| 参数名 | 类型 | 必填 | 说明 | 来源 |
|-------|------|------|------|------|
| userId | Long | 是 | 当前登录用户ID | @RequestAttribute 自动注入 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 10,
      "name": "素材管理",
      "path": "/asset",
      "component": "Layout",
      "icon": "Files",
      "sort": 2,
      "children": [
        {
          "id": 11,
          "name": "素材列表",
          "path": "/asset/list",
          "component": "asset/list/index",
          "icon": "Document",
          "sort": 1
        }
      ]
    }
  ]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `MenuController.getCurrentMenus()` | 接收请求，获取userId |
| App | `MenuService.getMenusByUserId()` | 业务逻辑 |
| App | `MenuServiceImpl.getMenusByUserId()` | 实现逻辑 |
| App | `selectMenusByUserId()` | 通过角色关联查询用户菜单 |
| App | `addWithParents()` | 递归添加父级菜单 |
| App | `buildTree()` | 构建树形结构 |
| Infrastructure | `MenuMapper.selectMenusByUserId()` | SQL查询（JOIN角色表） |

### 权限逻辑

```
1. 根据用户ID查询用户的角色
2. 根据角色ID查询关联的菜单ID列表
3. 获取用户有权限的菜单
4. 递归添加所有父级菜单（保证树结构完整）
5. 构建最终的菜单树
```

### 特殊规则

| 用户角色 | 菜单范围 |
|---------|---------|
| 系统管理员 (roleId=1) | 所有菜单 |
| 其他角色 | 角色关联的菜单及其父级菜单 |

---

## 3. 菜单详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取菜单详情 |
| 请求方法 | POST |
| 请求路径 | `/api/menu/getDetail` |
| 接口描述 | 根据菜单ID查询菜单的详细信息 |

### 请求参数

**MenuGetDetailQry**:
```json
{
  "id": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 菜单ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "name": "素材管理",
    "path": "/asset",
    "component": "Layout",
    "icon": "Files",
    "sort": 2,
    "parentId": 0,
    "type": "MENU",
    "status": 1
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `MenuController.getById()` | 接收HTTP请求 |
| App | `MenuService.getById()` | 业务逻辑 |
| Infrastructure | `MenuMapper.selectById()` | SQL查询 |

---

## 4. 新增菜单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 新增菜单 |
| 请求方法 | POST |
| 请求路径 | `/api/menu/create` |
| 接口描述 | 创建新的菜单项 |

### 请求参数

**MenuCreateCmd**:
```json
{
  "name": "素材审核",
  "path": "/asset/review",
  "component": "asset/review/index",
  "icon": "CircleCheck",
  "sort": 3,
  "parentId": 10,
  "menuType": 1
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| name | String | 是 | 菜单名称 | @NotBlank |
| path | String | 是 | 路由路径 | @NotBlank |
| component | String | 是 | 组件路径 | @NotBlank |
| icon | String | 否 | 图标 | - |
| sort | Integer | 否 | 排序号 | - |
| parentId | Long | 否 | 父菜单ID，顶级菜单传null或0 | - |
| menuType | Integer | 否 | 菜单类型 | - |

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
| Adapter | `MenuController.save()` | 接收请求，转换DTO |
| App | `MenuService.save()` | 业务逻辑 |
| Infrastructure | `MenuMapper.insert()` | 插入数据库 |

### 菜单类型说明

| menuType | type字段值 | 说明 |
|---------|-----------|------|
| 0 | BUTTON | 按钮权限 |
| 1 | MENU | 菜单 |

---

## 5. 更新菜单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新菜单 |
| 请求方法 | POST |
| 请求路径 | `/api/menu/update` |
| 接口描述 | 修改菜单信息 |

### 请求参数

**MenuUpdateCmd**:
```json
{
  "id": 11,
  "name": "素材列表（已更新）",
  "path": "/asset/list",
  "component": "asset/list/index",
  "icon": "Document",
  "sort": 1,
  "parentId": 10,
  "menuType": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 菜单ID |
| name | String | 否 | 菜单名称 |
| path | String | 否 | 路由路径 |
| component | String | 否 | 组件路径 |
| icon | String | 否 | 图标 |
| sort | Integer | 否 | 排序号 |
| parentId | Long | 否 | 父菜单ID |
| menuType | Integer | 否 | 菜单类型 |

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
| Adapter | `MenuController.update()` | 接收请求，转换DTO |
| App | `MenuService.update()` | 业务逻辑 |
| Infrastructure | `MenuMapper.updateById()` | 更新数据库 |

---

## 6. 删除菜单

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除菜单 |
| 请求方法 | POST |
| 请求路径 | `/api/menu/delete` |
| 接口描述 | 删除指定菜单（逻辑删除），同时删除角色菜单关联 |

### 请求参数

**MenuDeleteCmd**:
```json
{
  "id": 11
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 菜单ID |

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
| Adapter | `MenuController.delete()` | 接收HTTP请求 |
| App | `MenuService.delete()` | 业务逻辑 |
| App | `MenuServiceImpl.delete()` | 删除菜单和角色关联 |
| Infrastructure | `MenuMapper.deleteById()` | 逻辑删除菜单 |
| Infrastructure | `RoleMenuMapper.delete()` | 删除角色菜单关联 |

### 注意事项

> 删除菜单前需要确保：
> 1. 菜单下没有子菜单
> 2. 删除后会自动清除所有角色的该菜单权限

---

## 7. 分配角色菜单权限

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 分配角色菜单权限 |
| 请求方法 | POST |
| 请求路径 | `/api/menu/assign` |
| 接口描述 | 为指定角色分配菜单访问权限，会覆盖原有权限 |

### 请求参数

**URL参数**:
| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| roleId | Long | 是 | 角色ID（URL参数） |

**Body参数**:
```json
[1, 2, 3, 10, 11, 12]
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
| Adapter | `MenuController.assignMenus()` | 接收请求 |
| App | `MenuService.assignMenusToRole()` | 业务逻辑 |
| App | `MenuServiceImpl.assignMenusToRole()` | 事务操作：删除旧关联 + 插入新关联 |
| Infrastructure | `RoleMenuMapper.deleteByRoleId()` | 删除原有关联 |
| Infrastructure | `RoleMenuMapper.insert()` | 插入新的关联 |

### 事务处理

```
1. 开启事务
2. 删除角色的所有菜单关联
3. 批量插入新的菜单关联
4. 提交事务
```

---

## 8. 获取角色菜单权限

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取角色菜单权限 |
| 请求方法 | POST |
| 请求路径 | `/api/menu/getRoleMenus` |
| 接口描述 | 查询指定角色已分配的菜单ID列表 |

### 请求参数

**MenuGetRoleMenusQry**:
```json
{
  "roleId": 3
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
  "data": [1, 2, 3, 10, 11, 12]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `MenuController.getRoleMenus()` | 接收HTTP请求 |
| App | `MenuService.getMenuIdsByRoleId()` | 业务逻辑 |
| Infrastructure | `MenuMapper.selectMenuIdsByRoleId()` | 查询角色菜单关联表 |

---

## 附录：数据库表结构

### sys_menu (菜单表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 菜单名称 |
| path | VARCHAR | 路由路径 |
| component | VARCHAR | 组件路径 |
| icon | VARCHAR | 图标 |
| sort | INT | 排序号 |
| parent_id | BIGINT | 父菜单ID，顶级菜单为0 |
| type | VARCHAR | 类型（MENU=菜单, BUTTON=按钮） |
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

## 附录：菜单树形结构示例

```
根节点 (parentId = 0)
├── 1. 系统管理
│   ├── 2. 用户管理 (path: /system/user)
│   ├── 3. 角色管理 (path: /system/role)
│   └── 4. 菜单管理 (path: /system/menu)
├── 5. 素材管理
│   ├── 6. 素材列表 (path: /asset/list)
│   ├── 7. 待办事项 (path: /asset/pending)
│   └── 8. 草稿箱 (path: /asset/draft)
└── 9. 审批管理
    ├── 10. 待我审批 (path: /approval/pending)
    └── 11. 我发起的 (path: /approval/my)
```

---

## 附录：前端调用示例

### 获取当前用户菜单

```typescript
// xuanjiao-frontend/src/api/menu.ts
export const getCurrentMenus = () => {
  return request.post<MenuDTO[]>('/menu/getCurrent', {})
}

// 使用示例（在路由守卫中）
import { getCurrentMenus } from '@/api/menu'

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  if (userStore.isLoggedIn) {
    const menus = await getCurrentMenus()
    userStore.setMenus(menus.data)
    // 动态生成路由
    generateRoutes(menus.data)
  }
  next()
})
```

### 分配菜单权限

```typescript
export const assignMenuPermissions = (roleId: number, menuIds: number[]) => {
  return request.post(`/menu/assign?roleId=${roleId}`, menuIds)
}
```

---

*本文档由API接口文档生成方法论自动生成。*
