# API接口文档索引

> **项目**: 宣传教育平台（Propaganda/Education Platform）
> **架构**: COLA (Clean Object-Oriented and Layered Architecture)
> **文档生成时间**: 2026-03-04
> **总计**: 16个控制器，96个接口

---

## 文档概览

本文档集包含系统中所有API接口的完整文档，每个接口都详细记录了从前端请求到后端处理的完整调用链路。

### 按模块分类

| 模块 | 文档 | 接口数量 | 说明 |
|-----|------|---------|------|
| 基础认证 | [API_AUTH.md](#1-api_authmd) | 2 | 登录、登出 |
| 用户管理 | [API_USER.md](#2-api_usermd) | 8 | 用户CRUD、密码修改 |
| 部门管理 | [API_DEPT.md](#3-api_deptmd) | 7 | 部门树、部门CRUD |
| 角色管理 | [API_ROLE.md](#4-api_rolemd) | 7 | 角色CRUD、菜单权限 |
| 菜单管理 | [API_MENU.md](#5-api_menumd) | 8 | 菜单树、菜单CRUD |
| 素材管理 | [API_ASSET.md](#6-api_assetmd) | 11 | 素材上传、下载、标签 |
| 标签管理 | [API_TAG.md](#7-api_tagmd) | 4 | 标签CRUD |
| 素材录入 | [API_MATERIAL.md](#8-api_materialmd) | 8 | 素材录入申请 |
| 素材使用 | [API_USAGE.md](#9-api_usagemd) | 9 | 素材使用申请 |
| 素材删除 | [API_DELETION.md](#10-api_deletionmd) | 8 | 素材删除申请 |
| 工作流管理 | [API_WORKFLOW.md](#11-api_workflowmd) | 9 | 工作流定义 |
| 审批人选择 | [API_APPROVER_SELECTION.md](#12-api_approver_selectionmd) | 5 | 审批人配置 |
| 审批管理 | [API_APPROVAL.md](#13-api_approvalmd) | 8 | 审批任务处理 |
| 通知管理 | [API_NOTIFICATION.md](#14-api_notificationmd) | 13 | 通知、知会 |
| 使用日志 | [API_LOG.md](#15-api_logmd) | 2 | 素材使用日志 |
| 我的任务 | [API_TASK.md](#16-api_taskmd) | 1 | 草稿箱 |

---

## 文档详情

### 1. API_AUTH.md - 基础认证

| 接口 | 路径 | 说明 |
|-----|------|------|
| 登录 | POST /auth/login | JWT认证，MD5密码校验 |
| 登出 | POST /auth/logout | 清除Token |

### 2. API_USER.md - 用户管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取用户列表 | POST /user/getList | 分页查询，支持部门筛选 |
| 获取用户详情 | POST /user/getDetail | 根据ID查询 |
| 新增用户 | POST /user/create | 创建用户 |
| 更新用户 | POST /user/update | 更新用户信息 |
| 删除用户 | POST /user/delete | 逻辑删除 |
| 修改密码 | POST /user/changePassword | 修改当前用户密码 |
| 重置密码 | POST /user/resetPassword | 管理员重置 |
| 获取部门用户 | POST /user/getDeptUsers | 按部门查询用户 |

### 3. API_DEPT.md - 部门管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取部门树 | POST /dept/getTree | 查询完整部门树 |
| 获取部门详情 | POST /dept/getDetail | 根据ID查询 |
| 新增部门 | POST /dept/create | 创建部门 |
| 更新部门 | POST /dept/update | 更新部门信息 |
| 删除部门 | POST /dept/delete | 删除部门 |
| 获取子部门 | POST /dept/getChildren | 查询子部门列表 |
| 生成部门编码 | POST /dept/generateCode | 自动生成部门编号 |

### 4. API_ROLE.md - 角色管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取角色列表 | POST /role/getList | 分页查询角色 |
| 获取角色详情 | POST /role/getDetail | 根据ID查询 |
| 新增角色 | POST /role/create | 创建角色 |
| 更新角色 | POST /role/update | 更新角色信息 |
| 删除角色 | POST /role/delete | 删除角色 |
| 分配菜单权限 | POST /role/assignMenus | 设置角色可访问菜单 |
| 获取角色菜单 | POST /role/getMenus | 查询角色菜单权限 |

### 5. API_MENU.md - 菜单管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取菜单树 | POST /menu/getTree | 查询完整菜单树 |
| 获取用户菜单 | POST /menu/getUserMenus | 查询当前用户可访问菜单 |
| 获取菜单详情 | POST /menu/getDetail | 根据ID查询 |
| 新增菜单 | POST /menu/create | 创建菜单 |
| 更新菜单 | POST /menu/update | 更新菜单信息 |
| 删除菜单 | POST /menu/delete | 删除菜单 |
| 获取子菜单 | POST /menu/getChildren | 查询子菜单列表 |

### 6. API_ASSET.md - 素材管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取素材列表 | POST /asset/getList | 分页查询，支持多条件筛选 |
| 获取素材详情 | POST /asset/getDetail | 根据ID查询 |
| 新增素材 | POST /asset/create | 上传素材，MD5去重 |
| 更新素材 | POST /asset/update | 更新素材信息 |
| 删除素材 | POST /asset/delete | 删除素材 |
| 上传文件 | POST /asset/upload | 文件上传 |
| 预览文件 | GET /asset/preview/{id} | 文件预览 |
| 下载文件 | GET /asset/download/{id} | 文件下载 |
| 调整删除时间（管理员） | PUT /asset/admin/{id}/adjust-delete-time | 测试用 |
| 执行清理（管理员） | POST /asset/admin/trigger-cleanup | 手动触发清理任务 |
| 检查可用状态 | POST /asset/checkAvailable | 检查素材是否可用 |

### 7. API_TAG.md - 标签管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取标签列表 | POST /tag/getList | 按分类查询标签 |
| 新增标签 | POST /tag/create | 创建标签 |
| 更新标签 | POST /tag/update | 更新标签信息 |
| 删除标签 | POST /tag/delete | 删除标签 |

### 8. API_MATERIAL.md - 素材录入申请

| 接口 | 路径 | 说明 |
|-----|------|------|
| 创建素材录入申请 | POST /material-entry/create | 创建草稿 |
| 更新素材录入申请 | POST /material-entry/update | 更新草稿 |
| 查询申请详情 | POST /material-entry/getDetail | 查询详情 |
| 查询我的申请 | POST /material-entry/getMyApplications | 我的申请列表 |
| 查询草稿箱 | POST /material-entry/getDrafts | 草稿列表 |
| 删除申请 | POST /material-entry/delete | 删除草稿 |
| 提交审批 | POST /material-entry/{id}/submit | 提交审批 |
| 复制申请 | POST /material-entry/{id}/copy | 复制为新草稿 |

### 9. API_USAGE.md - 素材使用申请

| 接口 | 路径 | 说明 |
|-----|------|------|
| 创建使用申请草稿 | POST /usage-apply/draft | 创建草稿（支持多素材） |
| 更新使用申请草稿 | POST /usage-apply/update | 更新草稿 |
| 提交使用申请 | POST /usage-apply/{id}/submit | 提交审批 |
| 删除使用申请 | POST /usage-apply/delete | 删除草稿 |
| 查询申请单详情 | POST /usage-apply/getDetail | 查询详情 |
| 查询草稿箱 | POST /usage-apply/getDrafts | 草稿列表 |
| 查询我的所有申请 | POST /usage-apply/getMyApplications | 我的申请列表 |
| 检查是否有权限使用素材 | POST /usage-apply/canUseAsset | 权限检查 |
| 复制使用申请 | POST /usage-apply/{id}/copy | 复制为新草稿 |

### 10. API_DELETION.md - 素材删除申请

| 接口 | 路径 | 说明 |
|-----|------|------|
| 创建删除申请 | POST /asset-deletion/create | 创建草稿 |
| 更新删除申请 | POST /asset-deletion/update | 更新草稿 |
| 查询申请单详情 | POST /asset-deletion/getDetail | 查询详情 |
| 查询我的申请列表 | POST /asset-deletion/getMyApplications | 我的申请列表 |
| 查询草稿箱 | POST /asset-deletion/getDrafts | 草稿列表 |
| 删除申请单 | POST /asset-deletion/delete | 删除草稿 |
| 提交审批 | POST /asset-deletion/{id}/submit | 提交审批 |
| 复制删除申请 | POST /asset-deletion/{id}/copy | 复制为新草稿 |

### 11. API_WORKFLOW.md - 工作流管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取工作流列表 | POST /workflow/getList | 查询所有工作流 |
| 获取工作流详情 | POST /workflow/getDetail | 查询工作流配置 |
| 保存工作流 | POST /workflow/create | 创建新工作流 |
| 更新工作流 | POST /workflow/update | 更新工作流配置 |
| 删除工作流 | POST /workflow/delete | 删除工作流 |
| 更新工作流状态 | POST /workflow/updateStatus | 启用/停用（含冲突检查） |
| 绑定角色 | POST /workflow/bindRole | 绑定角色+流程类型 |
| 解绑角色 | POST /workflow/unbindRole | 解除绑定 |
| 复制工作流 | POST /workflow/{id}/copy | 复制工作流 |

### 12. API_APPROVER_SELECTION.md - 审批人选择

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取审批实例进度 | POST /workflow/getApprovalProgress | 查询审批进度（含子流程） |
| 获取第一层可选审批人 | POST /workflow/getFirstStageApprovers | 查询第一阶段审批人 |
| 选择第一层审批人（含子流程） | POST /workflow/select-first-stage-approvers-with-subworkflows | 选择审批人 |
| 选择下一层审批人（含子流程） | POST /workflow/select-next-stage-approvers-with-subworkflows | 选择下一阶段 |
| 获取子流程第一层可选审批人 | POST /workflow/getSubWorkflowFirstStageApprovers | 子流程审批人 |

### 13. API_APPROVAL.md - 审批管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取待办任务列表 | POST /approval/getMyTasks | 待办任务（含业务类型筛选） |
| 获取待办任务数量 | POST /approval/getMyTasksCount | 待办数量（徽章显示） |
| 获取我发起的申请 | POST /approval/getMyApplied | 我的申请列表 |
| 获取任务详情 | POST /approval/getTaskDetail | 任务详情（含可选审批人） |
| 获取审批实例详情 | POST /approval/getInstanceDetail | 实例详情 |
| 审批通过/拒绝 | POST /approval/approve | 处理审批任务 |
| 退回上一级 | POST /approval/return | 退回上一阶段 |
| 获取流经事项列表 | POST /approval/getMyFlowItems | 流经事项 |

### 14. API_NOTIFICATION.md - 通知管理

| 接口 | 路径 | 说明 |
|-----|------|------|
| 获取我的通知列表 | POST /notification/getMyNotifications | 通知列表 |
| 获取知会事项列表（含工单） | POST /notification/getMyNotificationsWithWorkOrder | 知会事项（JOIN工单） |
| 获取通知详情 | POST /notification/getDetail | 通知详情 |
| 获取未读通知数量 | POST /notification/getUnreadCount | 未读数量（徽章） |
| 创建通知 | POST /notification/create | 创建单条通知 |
| 批量创建通知 | POST /notification/batchCreate | 批量创建 |
| 标记通知为已读 | POST /notification/markAsRead | 标记已读 |
| 批量标记通知为已读 | POST /notification/batchMarkAsRead | 批量标记已读 |
| 标记所有通知为已读 | POST /notification/markAllAsRead | 全部已读 |
| 删除通知 | POST /notification/delete | 删除通知 |
| 批量删除通知 | POST /notification/batchDelete | 批量删除 |
| 知会用户关于审批实例 | POST /notification/notifyUsers | 知会其他人 |
| 获取工单的知会记录 | POST /notification/getNotificationRecords | 知会记录 |

### 15. API_LOG.md - 使用日志

| 接口 | 路径 | 说明 |
|-----|------|------|
| 查询日志 | POST /log/queryLogs | 使用日志列表 |
| 查询素材使用记录 | POST /log/getAssetUsageLogs | 指定素材的使用记录 |

### 16. API_TASK.md - 我的任务

| 接口 | 路径 | 说明 |
|-----|------|------|
| 查询草稿箱（按类型/标题筛选） | POST /task/queryDrafts | 统一草稿箱（聚合三种类型） |

---

## 架构说明

### COLA四层架构

```
┌─────────────────────────────────────────────────────────┐
│                    Adapter层（适配层）                    │
│              Controller - 接收HTTP请求                    │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                      App层（应用层）                       │
│           Service - 业务逻辑处理、事务编排                  │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   Domain层（领域层）                       │
│        Entity + Repository - 领域模型+仓储接口              │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│              Infrastructure层（基础设施层）                  │
│              Mapper - MyBatis数据访问                      │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                   Database（数据库）                       │
│                      MySQL数据库                          │
└─────────────────────────────────────────────────────────┘
```

### 模块化包结构

| 层级 | 包路径 | 说明 |
|-----|--------|------|
| Adapter | `xuanjiao-adapter/.../web/{module}/` | 按业务模块划分的Controller |
| App | `xuanjiao-app/.../{module}/` | 按业务模块划分的Service |
| Domain | `xuanjiao-domain/.../{module}/` | 按业务模块划分的Entity和Repository |
| Infrastructure | `xuanjiao-infrastructure/.../{module}/` | 按业务模块划分的Mapper |

### 业务模块列表

| 模块代码 | 模块名称 | 说明 |
|---------|---------|------|
| auth | 认证模块 | 登录登出 |
| user | 用户模块 | 用户管理 |
| dept | 部门模块 | 部门管理 |
| role | 角色模块 | 角色权限 |
| menu | 菜单模块 | 菜单管理 |
| asset | 素材模块 | 素材管理 |
| material | 素材录入模块 | 素材录入申请 |
| usage | 素材使用模块 | 素材使用申请 |
| deletion | 素材删除模块 | 素材删除申请 |
| workflow | 工作流模块 | 工作流定义 |
| approval | 审批模块 | 审批执行 |
| notification | 通知模块 | 通知知会 |
| log | 日志模块 | 使用日志 |
| task | 任务模块 | 我的任务 |

---

## 设计规范

### API命名规范

| 类型 | 规范 | 示例 |
|-----|------|------|
| 查询 | POST /{module}/get{Action} | POST /asset/getList |
| 命令 | POST /{module}/{action} | POST /asset/create |
| 详情 | POST /{module}/getDetail | POST /asset/getDetail |
| 路径参数 | {id}/{action} | /material-entry/{id}/submit |

### DTO命名规范

| 类型 | 后缀 | 示例 |
|-----|------|------|
| 查询DTO | Qry | AssetGetListQry |
| 命令DTO | Cmd | AssetCreateCmd |
| 响应DTO | DTO | AssetDTO |
| 分页DTO | PageResult\<T\> | PageResult\<AssetDTO\> |

### 认证机制

- **JWT Token**: 请求头 `Authorization: Bearer <token>`
- **用户注入**: 通过 `@RequestAttribute("userId")` 获取当前用户ID
- **权限控制**: 基于角色的菜单权限控制

---

## 数据库表概览

| 表名 | 说明 | 关联模块 |
|-----|------|---------|
| sys_user | 用户表 | user |
| sys_dept | 部门表 | dept |
| sys_role | 角色表 | role |
| sys_menu | 菜单表 | menu |
| sys_role_menu | 角色菜单关联表 | role |
| asset | 素材表 | asset |
| tag | 标签表 | tag |
| material_application | 素材录入申请表 | material |
| material_asset | 素材录入素材关联表 | material |
| usage_apply | 素材使用申请表 | usage |
| usage_apply_asset | 使用申请素材关联表 | usage |
| usage_log | 使用日志表 | log |
| asset_deletion_application | 素材删除申请表 | deletion |
| asset_deletion_asset | 删除申请素材关联表 | deletion |
| workflow | 工作流表 | workflow |
| workflow_stage | 工作流阶段表 | workflow |
| stage_approver | 阶段审批人表 | workflow |
| approval_instance | 审批实例表 | approval |
| approval_task | 审批任务表 | approval |
| approval_progress | 审批进度表 | approval |
| notification | 通知表 | notification |
| notification_record | 知会记录表 | notification |

---

## 文档使用指南

### 快速查找接口

1. **按功能查找**: 查看本文档的"按模块分类"章节，找到对应模块
2. **按路径查找**: 使用编辑器搜索功能，搜索接口路径
3. **按Controller查找**: 打开对应的API_*.md文件

### 接口调试

1. 使用Knife4j文档: http://localhost:8080/api/doc.html
2. 参考本文档中的请求参数示例
3. 查看调用链路了解后端处理流程

### 新增接口

1. 在对应Controller中添加接口方法
2. 在对应的API_*.md中添加接口文档
3. 更新本索引文件

---

*本文档集由API接口文档生成方法论自动生成。*
