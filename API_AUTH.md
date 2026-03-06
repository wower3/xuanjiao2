# API_AUTH.md - 认证管理接口文档

> **模块**: 认证管理 (auth)
> **Controller**: `AuthController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/auth/`
> **创建时间**: 2026-03-04

---

## 目录

1. [用户登录](#1-用户登录)
2. [用户登出](#2-用户登出)

---

## 1. 用户登录

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 用户登录 |
| 请求方法 | POST |
| 请求路径 | `/api/auth/login` |
| 接口描述 | 使用用户名和密码进行登录验证，验证成功后返回JWT令牌和用户信息 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| username | String | 是 | 用户名 | @NotBlank |
| password | String | 是 | 密码（明文传输） | @NotBlank |

**请求示例**:
```json
{
  "username": "admin",
  "password": "123456"
}
```

### 响应结果

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": "admin@example.com",
      "phone": "13800138000",
      "deptId": 1,
      "roleId": 1,
      "roleName": "系统管理员",
      "roleType": "SYSTEM_ADMIN"
    }
  }
}
```

**失败响应**:
```json
{
  "code": 500,
  "message": "用户名或密码错误"
}
```

```json
{
  "code": 500,
  "message": "用户已被禁用"
}
```

### 调用链路（COLA架构）

```
前端 Vue3 → Adapter → App → Domain → Infrastructure → Database
   ↓          ↓         ↓        ↓            ↓             ↓
 login.vue  AuthController  AuthService  UserRepository  UserMapper
                                     impl           impl
```

| 层级 | 类名/方法 | 文件位置 | 说明 |
|-----|----------|---------|------|
| **Adapter** | `AuthController.login()` | `adapter/web/auth/AuthController.java:58` | 接收HTTP请求，参数验证 |
| **App** | `AuthService.login()` | `app/auth/AuthService.java:34` | 业务逻辑接口定义 |
| **App** | `AuthServiceImpl.login()` | `app/auth/impl/AuthServiceImpl.java:41` | 登录业务逻辑实现 |
| **Domain** | `UserRepository.findByUsername()` | `domain/user/repository/UserRepository.java:22` | 数据访问接口定义 |
| **Infrastructure** | `UserRepositoryImpl.findByUsername()` | `infrastructure/user/UserRepositoryImpl.java:34` | 数据访问实现 |
| **Infrastructure** | `UserMapper.selectOneByUsername()` | `infrastructure/user/UserMapper.java:34` | MyBatis SQL调用 |
| **Database** | `sys_user` 表 | MySQL数据库 | 用户数据存储 |

### 详细处理流程

1. **参数验证**: Adapter层使用 `@Valid` 验证请求参数
2. **用户查询**: 通过用户名查询用户 (`UserRepository.findByUsername`)
3. **密码验证**: 使用MD5加密比对密码 (`DigestUtil.md5Hex`)
4. **状态检查**: 检查用户状态是否为启用状态 (`status = 1`)
5. **Token生成**: 使用JWT生成访问令牌 (`JwtUtil.generateToken`)
6. **角色信息填充**: 查询关联的角色信息 (`RoleMapper.selectById`)
7. **返回结果**: 封装Token和用户信息返回

### 权限要求

| 项目 | 说明 |
|-----|------|
| 登录认证 | **不需要**（登录接口本身用于获取认证） |
| 角色要求 | 所有角色均可访问 |
| 数据权限 | 无 |

### 数据库表

| 表名 | 说明 | 操作类型 | 字段 |
|-----|------|---------|------|
| sys_user | 用户表 | SELECT | id, username, password, status, real_name, email, phone, dept_id, role_id |
| sys_role | 角色表 | SELECT | id, name, role_type |

### 相关SQL（UserMapper.xml）

```xml
<select id="selectOneByUsername" resultType="com.xuanjiao.infrastructure.dataobject.UserDO">
    SELECT id, username, password, real_name, email, phone, dept_id, role_id, status
    FROM sys_user
    WHERE username = #{username}
    AND deleted = 0
</select>
```

### 错误码

| 错误码 | 说明 | 处理建议 |
|-------|------|---------|
| 500 | 用户名或密码错误 | 检查用户名和密码是否正确 |
| 500 | 用户已被禁用 | 联系管理员启用账号 |

---

## 2. 用户登出

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 用户登出 |
| 请求方法 | POST |
| 请求路径 | `/api/auth/logout` |
| 接口描述 | 注销当前用户的会话，使令牌失效 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| token | String | 是 | JWT访问令牌 | @NotBlank |

**请求示例**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 响应结果

**成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 调用链路（COLA架构）

| 层级 | 类名/方法 | 文件位置 | 说明 |
|-----|----------|---------|------|
| **Adapter** | `AuthController.logout()` | `adapter/web/auth/AuthController.java:72` | 接收HTTP请求 |
| **App** | `AuthService.logout()` | `app/auth/AuthService.java:43` | 登出业务逻辑 |
| **App** | `AuthServiceImpl.logout()` | `app/auth/impl/AuthServiceImpl.java:75` | 当前为空实现 |
| **Database** | - | - | 无数据库操作 |

### 详细处理流程

1. **接收Token**: 从请求体中获取JWT令牌
2. **令牌失效**: 当前实现为空操作，可扩展为将令牌加入黑名单
3. **返回成功**: 直接返回成功响应

**注意**: 当前版本中，logout操作并未实际将token加入黑名单，JWT仍然有效直到过期。

### 权限要求

| 项目 | 说明 |
|-----|------|
| 登录认证 | **不需要**（logout通常在客户端删除token） |
| 角色要求 | 所有角色均可访问 |
| 数据权限 | 无 |

### 数据库表

| 表名 | 说明 | 操作类型 |
|-----|------|---------|
| - | - | - |

> **说明**: 当前实现中logout不涉及数据库操作。如需实现token黑名单功能，可创建 `sys_token_blacklist` 表。

### 错误码

无特定错误码。

---

## 附录：相关实体类

### LoginCmd
**位置**: `xuanjiao-client/src/main/java/com/xuanjiao/client/auth/LoginCmd.java`

```java
public class LoginCmd {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

### LogoutCmd
**位置**: `xuanjiao-client/src/main/java/com/xuanjiao/client/auth/LogoutCmd.java`

```java
public class LogoutCmd {
    @NotBlank(message = "令牌不能为空")
    private String token;
}
```

### LoginResultDTO
**位置**: `xuanjiao-client/src/main/java/com/xuanjiao/client/auth/LoginResultDTO.java`

```java
public class LoginResultDTO {
    private String token;      // JWT访问令牌
    private UserDTO user;      // 用户信息
}
```

---

## 附录：前端调用示例

### 登录

```typescript
// xuanjiao-frontend/src/api/auth.ts
export const login = (data: { username: string; password: string }) => {
  return request.post<LoginResult>('/auth/login', data)
}

// 使用示例
import { login } from '@/api/auth'

const handleLogin = async () => {
  try {
    const res = await login({
      username: 'admin',
      password: '123456'
    })
    // 保存token到localStorage
    localStorage.setItem('token', res.data.token)
    // 保存用户信息
    localStorage.setItem('user', JSON.stringify(res.data.user))
  } catch (error) {
    console.error('登录失败', error)
  }
}
```

### 登出

```typescript
// xuanjiao-frontend/src/api/auth.ts
export const logout = (token: string) => {
  return request.post('/auth/logout', { token })
}

// 使用示例
import { logout } from '@/api/auth'

const handleLogout = async () => {
  try {
    const token = localStorage.getItem('token')
    await logout(token || '')
    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  } catch (error) {
    console.error('登出失败', error)
  }
}
```

---

*本文档由API接口文档生成方法论自动生成。*
