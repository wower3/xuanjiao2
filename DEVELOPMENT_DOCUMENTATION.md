# 宣传教育平台 - 详细开发文档

本文档记录平台所有功能的实现细节，包括前端、后端和数据库的交互方式，帮助新开发者快速了解项目并进行开发。

## 目录
- [项目架构](#项目架构)
- [技术栈](#技术栈)
- [功能模块详解](#功能模块详解)
- [数据库设计](#数据库设计)
- [API接口规范](#api接口规范)
- [开发规范](#开发规范)
- [常见开发场景](#常见开发场景)

---

## 项目架构

### 整体架构 (COLA框架)

```
┌─────────────────────────────────────────────────────────────────┐
│                         前端 (Vue 3 + TypeScript)                 │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐   │
│  │   Views    │  │  Stores   │  │   API     │  │ Components│   │
│  └───────────┘  └───────────┘  └───────────┘  └───────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ HTTP/REST API
┌─────────────────────────────────────────────────────────────────┐
│                     后端 (Spring Boot + COLA)                     │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                      Adapter Layer                         │  │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  │  │
│  │  │Controller│ │Controller│ │Controller│ │Controller│ │Controller│  │  │
│  │  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                        App Layer                         │  │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  │  │
│  │  │ Service │ │ Service │ │ Service │ │ Service │ │ Service │  │  │
│  │  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                       Domain Layer                       │  │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  │  │
│  │  │ Entity │ │ Entity │ │ Entity │ │ Entity │ │ Entity │  │  │
│  │  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    Infrastructure Layer                   │  │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  │  │
│  │  │ Mapper │ │ Mapper │ │ Mapper │ │ Mapper │ │ Mapper │  │  │
│  │  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                   │
└──────────────────────────────┼───────────────────────────────────┘
                               ▼
                    ┌─────────────────────┐
                    │    MySQL 8.0        │
                    └─────────────────────┘
```

### 模块结构

```
xuanjiao-backend/
├── xuanjiao-client/          # 客户端层 - DTO定义
├── xuanjiao-domain/          # 领域层 - 实体、仓储接口
│   ├── auth/                 # 认证模块
│   ├── user/                 # 用户模块
│   ├── dept/                 # 部门模块
│   ├── role/                 # 角色模块
│   ├── menu/                 # 菜单模块
│   ├── asset/                # 素材模块
│   ├── material/             # 素材录入模块
│   ├── usage/                # 素材使用模块
│   ├── deletion/             # 素材删除模块
│   ├── notification/         # 通知模块
│   ├── workflow/             # 工作流模块
│   ├── approval/             # 审批模块
│   └── log/                 # 日志模块
├── xuanjiao-app/             # 应用层 - 服务实现
├── xuanjiao-infrastructure/   # 基础设施层 - MyBatis Mapper
└── xuanjiao-adapter/         # 适配层 - REST Controller
```

---

## 技术栈

### 后端技术
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 8 | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| COLA | 4.3.2 | 架构框架 |
| MyBatis Plus | 3.5.3.1 | 数据访问（使用BaseMapper） |
| MySQL | 8.0 | 数据库 |
| JWT | - | 认证授权 |
| Knife4j | 4.1.0 | API文档 |
| MapStruct | 1.5.5 | DTO映射 |
| Lombok | 1.18.30 | 代码生成 |

### 前端技术
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4 | 框架 |
| TypeScript | 5.3.3 | 语言 |
| Vite | 5.0 | 构建工具 |
| Element Plus | 2.4.4 | UI组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.x | 路由 |
| Axios | 1.6.2 | HTTP客户端 |
| vuedraggable | 4.1.0 | 拖拽组件 |

---

## 功能模块详解

### 1. 用户认证模块

#### 功能描述
提供用户登录、登出、Token管理功能。

#### 前端交互
```
登录页面 (login/index.vue)
  ↓ 输入用户名密码
  POST /auth/login
  ↓ 返回Token
  存储到localStorage
  ↓ 跳转首页
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `AuthController.java` | 认证接口 |
| `AuthService.java` | 认证服务接口 |
| `AuthServiceImpl.java` | 认证服务实现 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 用户登录 |

#### 请求示例
```javascript
// 前端调用
POST /auth/login
{
  "username": "admin",
  "password": "123456"
}

// 响应
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "管理员"
    }
  }
}
```

#### 数据库表
| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表 |
| `sys_user_role` | 用户角色关联表 |

---

### 2. 用户管理模块

#### 功能描述
提供用户的增删改查、角色分配等功能。

#### 前端页面
```
用户管理 (system/user.vue)
├── 列表展示
│   ├── 表格展示用户信息
│   ├── 支持按角色筛选
│   └── 支持按部门筛选(含子部门)
├── 新增用户
├── 编辑用户
└── 删除用户
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `UserController.java` | 用户接口 |
| `UserService.java` | 用户服务接口 |
| `UserServiceImpl.java` | 用户服务实现 |
| `UserMapper.java` | 用户数据访问 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/list` | 分页查询用户 |
| POST | `/user/getDetail` | 获取用户详情 |
| POST | `/user/create` | 创建用户 |
| POST | `/user/update` | 更新用户 |
| POST | `/user/delete` | 删除用户 |

#### 数据库表
| 表名 | 字段 | 说明 |
|------|------|------|
| `sys_user` | id, username, password, real_name, dept_id | 用户信息 |
| `sys_user_role` | user_id, role_id | 用户角色关联 |

---

### 3. 素材管理模块

#### 功能描述
提供素材的上传、预览、下载、标签管理、列表查询等功能。

#### 前端页面
```
素材管理 (asset/index.vue)
├── 素材列表
│   ├── 列表模式/预览模式切换
│   ├── 按名称搜索
│   ├── 按类型筛选(视频/图片/文档)
│   └── 管理员功能(彻底删除、调整删除时间)
├── 素材预览
├── 素材下载
└── 素材录入申请

素材录入 (asset/material-entry.vue)
├── 添加素材文件
├── 设置标签
├── 设置版权声明
├── 设置发布渠道
├── 选择审批流程
├── 选择审批人
└── 提交审批
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `AssetController.java` | 素材接口 |
| `AssetService.java` | 素材服务接口 |
| `AssetServiceImpl.java` | 素材服务实现 |
| `AssetMapper.java` | 素材数据访问 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/asset/list` | 获取素材列表 |
| POST | `/asset/getDetail` | 获取素材详情 |
| POST | `/asset/upload` | 上传素材 |
| POST | `/asset/delete` | 删除素材 |
| POST | `/asset/adminDelete` | 管理员彻底删除 |
| POST | `/asset/adjustDeleteTime` | 调整删除时间（测试） |
| POST | `/asset/admin/trigger-cleanup` | 手动触发清理 |
| POST | `/asset/getMyApproved` | 获取我的已录入素材 |
| GET | `/asset/preview/{id}` | 预览素材 |
| GET | `/asset/thumbnail/{id}` | 查看缩略图 |
| GET | `/asset/download/{id}` | 下载素材 |

#### 数据库表
| 表名 | 字段 | 说明 |
|------|------|------|
| `asset` | id, name, type, file_path, status | 素材主表 |
| `asset_tag` | id, name, category | 标签表 |
| `asset_asset_tag` | asset_id, tag_id | 素材标签关联 |

#### 文件上传流程
```
1. 前端选择文件
2. 前端计算文件MD5
3. 检查MD5是否已存在(去重)
4. POST /asset/upload (multipart/form-data)
5. 后端:
   - 检查MD5
   - 如存在返回已有素材ID
   - 如不存在保存文件
   - 记录素材信息
6. 返回素材ID
```

---

### 4. 审批流程模块

#### 功能描述
提供审批流程的可视化设计、配置、管理功能。

#### 前端页面
```
流程列表 (workflow/index.vue)
├── 流程列表展示
├── 新建流程
├── 编辑流程
├── 复制流程
├── 绑定/解绑角色
└── 启用/禁用/删除

流程设计 (workflow/design.vue)
├── 拖拽添加审批层
├── 配置每层的审批方式
│   ├── OR-或签(任一通过)
│   └── AND-会签(全部通过)
├── 添加审批人
│   ├── 指定用户
│   ├── 指定角色
│   └── 指定部门
└── 配置子流程
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `WorkflowController.java` | 流程接口 |
| `WorkflowService.java` | 流程服务接口 |
| `WorkflowEngineService.java` | 流程引擎服务 |
| `ApproverSelectionService.java` | 审批人选择服务 |
| `WorkflowMapper.java` | 流程数据访问 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/workflow/getList` | 获取流程列表 |
| POST | `/workflow/getDetail` | 获取流程详情 |
| POST | `/workflow/create` | 创建流程 |
| POST | `/workflow/update` | 更新流程 |
| POST | `/workflow/delete` | 删除流程 |
| POST | `/workflow/bindRole` | 绑定角色 |
| POST | `/workflow/unbindRole` | 解绑角色 |
| POST | `/workflow/copy` | 复制流程 |

#### 数据库表
| 表名 | 字段 | 说明 |
|------|------|------|
| `workflow` | id, name, bound_role_id, workflow_type | 流程定义表 |
| `workflow_stage` | id, workflow_id, name, stage_order, approve_type | 阶段表 |
| `stage_approver` | id, stage_id, approver_type, approver_id, sub_workflow_id | 审批人配置表 |

#### 流程数据结构
```javascript
{
  "id": 1,
  "name": "素材录入审批流程",
  "stages": [
    {
      "id": 1,
      "name": "部门主管审批",
      "order": 1,
      "approveType": "OR",  // 或签
      "approvers": [
        { "type": "USER", "id": 2 },
        { "type": "ROLE", "id": 2 }
      ]
    },
    {
      "id": 2,
      "name": "总监审批",
      "order": 2,
      "approveType": "AND",  // 会签
      "approvers": [
        { "type": "USER", "id": 3 }
      ]
    }
  ]
}
```

---

### 5. 审批执行模块

#### 功能描述
提供审批任务的处理、查看、流转功能。

#### 前端页面
```
待办事项 (task/pending-approval.vue)
├── 待审批任务列表
├── 按业务类型筛选
│   ├── 素材录入
│   ├── 素材使用
│   └── 素材删除
├── 审批详情
│   ├── 查看申请内容
│   ├── 查看审批进度
│   ├── 查看素材信息
│   └── 审批操作
│       ├── 通过
│       ├── 驳回
│       └── 退回
└── 选择审批人
    ├── 选择下一层审批人
    └── 选择子流程审批人
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `ApprovalController.java` | 审批接口 |
| `ApprovalService.java` | 审批服务 |
| `WorkflowEngineService.java` | 流程引擎 |
| `ApproverSelectionService.java` | 审批人选择 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/approval/getMyTasks` | 获取待办任务 |
| POST | `/approval/getMyTasksCount` | 获取待办数量 |
| POST | `/approval/getTaskDetail` | 获取任务详情 |
| POST | `/approval/getInstanceDetail` | 获取实例详情 |
| POST | `/approval/tasks/{id}/approve` | 审批通过/驳回 |
| POST | `/approval/tasks/{id}/return` | 退回任务 |
| POST | `/approval/instances/{id}/withdraw` | 撤回申请 |

#### 数据库表
| 表名 | 字段 | 说明 |
|------|------|------|
| `approval_instance` | id, workflow_id, business_type, business_id, status | 审批实例表 |
| `approval_task` | id, instance_id, stage_id, approver_id, status | 审批任务表 |
| `approval_progress` | id, instance_id, stage_id, status | 审批进度表 |

#### 审批状态流转
```
PENDING(审批中)
    ├── APPROVE → APPROVED(通过)
    ├── REJECT → REJECTED(驳回)
    ├── RETURN → RETURNED(退回)
    │   └── 重新提交 → PENDING
    └── CANCEL → CANCELLED(取消)
```

#### 子流程机制
```
主流程阶段完成
    ├── 普通审批人 → 进入下一阶段
    └── 子流程 → 触发子流程实例
                    ├── 子流程并行执行
                    └── 主流程等待子流程完成
```

---

### 6. 素材使用申请模块

#### 功能描述
提供素材使用申请的创建、提交、审批、追踪功能。支持一个申请关联多个素材。

#### 前端页面
```
素材使用申请 (asset/usage-apply.vue)
├── 申请标题
├── 选择素材
│   └── AssetSelector组件
│       ├── 搜索素材
│       ├── 按类型筛选
│       └── 多选素材
├── 配置每个素材的使用信息
│   ├── 使用说明
│   ├── 发布渠道
│   └── 是否二次创作
├── 选择审批流程
├── 选择审批人
└── 提交审批

使用申请列表
├── 待审批
├── 已通过
├── 已驳回
└── 已取消
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `UsageApplyController.java` | 使用申请接口 |
| `UsageApplyService.java` | 使用申请服务 |
| `UsageApplyMapper.java` | 使用申请数据访问 |
| `UsageApplyAssetMapper.java` | 申请素材关联访问 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/usage-apply/create` | 创建申请 |
| POST | `/usage-apply/update` | 更新申请 |
| POST | `/usage-apply/submit` | 提交审批 |
| POST | `/usage-apply/delete` | 删除申请 |
| POST | `/usage-apply/getDetail` | 获取详情 |
| POST | `/usage-apply/getDrafts` | 获取草稿列表 |
| POST | `/usage-apply/getMyApplications` | 获取我的申请 |

---

### 11. 标签管理模块

#### 功能描述
提供素材标签的创建、查询、删除功能。标签按分类组织（图片/视频）。

#### 后端实现
| 文件 | 说明 |
|------|------|
| `TagController.java` | 标签接口 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/tag/getList` | 获取标签列表 |
| POST | `/tag/getListByCategory` | 按类别获取标签 |
| POST | `/tag/create` | 创建标签 |
| POST | `/tag/delete` | 删除标签 |

#### 数据库表
| 表名 | 字段 | 说明 |
|------|------|------|
| `usage_apply` | id, title, status, applicant_id | 使用申请表 |
| `usage_apply_asset` | id, usage_apply_id, asset_id, usage_description | 申请素材关联 |

#### 多对多关系
```
usage_apply(1) ←→ (N) usage_apply_asset (N) ←→ (1) asset
一个申请单  ↔   多个关联记录   ↔   一个素材
              ↔   每个素材独立配置
```

---

### 7. 素材删除申请模块

#### 功能描述
提供素材删除申请的创建、审批、执行功能。支持两级删除机制。

#### 前端页面
```
素材删除申请
├── 选择要删除的素材
├── 输入删除原因
├── 提交审批
└── 审批通过后标记为DELETED

两级删除机制
├── Stage 1: DELETED状态
│   ├── 审批通过后标记
│   ├── 素材仍可见但不可用
│   └── 7天冷静期
│
└── Stage 2: 软删除(deleted=1)
    ├── 7天后自动执行
    ├── 管理员可手动触发
    └── 素材完全隐藏
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `AssetDeletionController.java` | 删除申请接口 |
| `AssetDeletionApplicationService.java` | 删除申请服务 |
| `AssetDeletionCleanupTask.java` | 定时清理任务 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/deletion/create` | 创建删除申请 |
| POST | `/deletion/update` | 更新删除申请 |
| POST | `/deletion/{id}/submit` | 提交审批 |
| POST | `/deletion/delete` | 删除申请 |
| POST | `/deletion/getDetail` | 获取详情 |
| POST | `/deletion/getMyApplications` | 获取我的申请 |
| POST | `/asset/admin/trigger-cleanup` | 手动触发清理(管理员) |

#### 数据库表
| 表名 | 字段 | 说明 |
|------|------|------|
| `asset_deletion_application` | id, title, status, applicant_id, deletion_reason | 删除申请表 |
| `asset_deletion_asset` | id, application_id, asset_id | 删除申请素材关联 |

---

### 8. 通知模块

#### 功能描述
提供系统通知的发送、接收、阅读管理功能。支持知会(mention)功能。

#### 前端页面
```
知会事项 (task/notifications.vue)
├── 通知列表
│   ├── 按类型筛选
│   ├── 按阅读状态筛选
│   └── 搜索通知
├── 通知详情
│   ├── 查看工单信息
│   ├── 查看审批进度
│   └── 知会其他用户
└── 操作
    ├── 标记已读
    ├── 批量标记已读
    └── 全部已读
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `NotificationController.java` | 通知接口 |
| `NotificationService.java` | 通知服务 |
| `NotificationMapper.java` | 通知数据访问 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/notification/page` | 分页查询通知 |
| POST | `/notification/getDetail` | 获取通知详情 |
| POST | `/notification/create` | 创建通知 |
| POST | `/notification/markAsRead` | 标记已读 |
| POST | `/notification/batchMarkAsRead` | 批量标记已读 |
| POST | `/notification/markAllAsRead` | 全部标记已读 |
| POST | `/notification/delete` | 删除通知 |
| POST | `/notification/batchDelete` | 批量删除 |
| POST | `/notification/batchCreate` | 批量创建 |
| POST | `/notification/notifyUsers` | 知会用户 |

#### 数据库表
| 表名 | 字段 | 说明 |
|------|------|------|
| `sys_notification` | id, title, content, notification_type, recipient_id, is_read | 通知表 |

#### 通知类型
| 类型 | 说明 |
|------|------|
| WORKFLOW_FLOW | 流程流转通知 |
| MENTION | 知会通知 |
| SYSTEM | 系统通知 |

---

### 9. 流经事项模块

#### 功能描述
展示当前用户参与过的所有审批流程(作为发起人或审批人)。

#### 前端页面
```
流经事项 (task/flow-items.vue)
├── 参与列表
│   ├── 我发起的
│   └── 我审批的
├── 筛选条件
│   ├── 按类型
│   ├── 按状态
│   └── 搜索标题
└── 查看详情
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `ApprovalController.java` | 流经事项接口 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/approval/getMyFlowItems` | 获取流经事项 |

---

### 10. 草稿箱模块

#### 功能描述
统一的草稿箱，管理所有类型的草稿申请（素材录入、使用申请、删除申请）。

#### 前端页面
```
草稿箱 (task/draft-box.vue)
├── 筛选区域
│   ├── 类型下拉框（全部/素材录入/使用申请/删除申请）
│   └── 标题搜索框
├── 草稿列表
│   ├── 类型标签
│   ├── 事项标题
│   ├── 维护人/申请人
│   ├── 文件数量
│   └── 操作按钮（查看详情、继续编辑、删除）
```

#### 后端实现
| 文件 | 说明 |
|------|------|
| `TaskController.java` | 草稿箱接口 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/task/queryDrafts` | 查询草稿列表（支持类型和标题筛选） |

#### 查询逻辑
```sql
-- 发起或审批过的实例
SELECT ai.* FROM approval_instance ai
WHERE ai.applicant_id = #{userId}
   OR EXISTS (
     SELECT 1 FROM approval_task at
     WHERE at.instance_id = ai.id
       AND at.approver_id = #{userId}
   )
ORDER BY ai.create_time DESC
```

---

### 12. 使用日志模块

#### 功能描述
记录素材的使用/下载日志，包含使用人、时间、IP、使用说明、发布渠道等信息。

#### 后端实现
| 文件 | 说明 |
|------|------|
| `UsageLogController.java` | 使用日志接口 |
| `UsageLogService.java` | 使用日志服务 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/usage-log/page` | 分页查询使用日志 |

---

### 13. 操作日志模块

#### 功能描述
记录系统的操作日志，用于审计和问题追溯。

#### 后端实现
| 文件 | 说明 |
|------|------|
| `LogController.java` | 日志接口 |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/log/queryLogs` | 查询操作日志 |

---

## 数据库设计

### ER图

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  sys_user   │──────▶│sys_user_role│◀──────│  sys_role   │
└─────────────┘       └─────────────┘       └─────────────┘
      │                                             │
      │                                             ▼
      │                                       ┌─────────────┐
      │                                       │  sys_menu   │
      │◀──────────────────────────────────────│sys_role_menu│
      │                                       └─────────────┘
      │
      ▼
┌─────────────┐       ┌─────────────────────┐       ┌─────────────┐
│    asset    │──────▶│asset_asset_tag◀──────│  asset_tag  │
└─────────────┘       └─────────────────────┘       └─────────────┘

┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│  workflow   │──────▶│workflow_stage│──────▶│stage_approver│
└─────────────┘       └─────────────┘       └─────────────┘
      │
      ▼
┌─────────────┐       ┌─────────────┐       ┌─────────────────┐
│approval_instance│───▶│approval_task│       │approval_progress│
└─────────────┘       └─────────────┘       └─────────────────┘

┌─────────────────┐   ┌───────────────────┐
│material_application│─▶│material_application_asset│
└─────────────────┘   └───────────────────┘

┌─────────────┐   ┌───────────────────┐   ┌─────────────────┐
│  usage_apply│──▶│usage_apply_asset │──▶│     asset       │
└─────────────┘   └───────────────────┘   └─────────────────┘

┌───────────────────────┐   ┌───────────────────┐
│asset_deletion_application│─▶│asset_deletion_asset │
└───────────────────────┘   └───────────────────┘

┌─────────────────┐
│sys_notification │
└─────────────────┘

┌─────────────┐
│  usage_log  │
└─────────────┘
```

### 核心表说明

#### 用户角色权限表
| 表名 | 说明 |
|------|------|
| `sys_user` | 用户表，存储用户基本信息 |
| `sys_role` | 角色表，存储角色定义 |
| `sys_user_role` | 用户角色关联表 |
| `sys_menu` | 菜单表，存储系统菜单 |
| `sys_role_menu` | 角色菜单关联表 |

#### 素材相关表
| 表名 | 说明 |
|------|------|
| `asset` | 素材主表，存储文件信息 |
| `asset_tag` | 标签表 |
| `asset_asset_tag` | 素材标签关联表 |

#### 工作流相关表
| 表名 | 说明 |
|------|------|
| `workflow` | 流程定义表 |
| `workflow_stage` | 阶段定义表 |
| `stage_approver` | 审批人配置表 |

#### 审批相关表
| 表名 | 说明 |
|------|------|
| `approval_instance` | 审批实例表，记录每次审批 |
| `approval_task` | 审批任务表，记录每个审批人的任务 |
| `approval_progress` | 审批进度表，记录审批历史 |

#### 业务申请表
| 表名 | 说明 |
|------|------|
| `material_application` | 素材录入申请 |
| `material_application_asset` | 素材录入申请素材关联 |
| `usage_apply` | 素材使用申请 |
| `usage_apply_asset` | 使用申请素材关联（中间表） |
| `asset_deletion_application` | 素材删除申请 |
| `asset_deletion_asset` | 删除申请素材关联 |

---

### 数据库迁移脚本

项目使用编号的迁移脚本来管理数据库变更：

| 脚本文件 | 说明 |
|---------|------|
| `init_complete_fixed.sql` | 完整数据库初始化脚本 |
| `init_17_sub_workflow_refactor.sql` | 子流程重构 |
| `init_18_add_sub_workflow_approver_ids.sql` | 添加子流程审批人ID |
| `init_19_add_sub_workflow_approver_ids_to_instance.sql` | 添加实例子流程审批人ID |
| `init_20_add_material_approval_menu.sql` | 添加素材审批菜单 |
| `init_21_add_usage_menu.sql` | 添加使用申请菜单 |
| `init_22_extend_asset_for_usage.sql` | 扩展素材表支持使用申请 |
| `init_22_add_returned_status.sql` | 添加已退回状态 |
| `init_23_add_task_type.sql` | 添加任务类型字段 |
| `init_23_refactor_to_intermediate_table.sql` | 重构到中间表 |
| `init_24_refactor_to_intermediate_table.sql` | 使用申请中间表重构 |
| `init_27_rename_pending_menu.sql` | 重命名待办菜单 |

---

## API接口规范

### 统一响应格式
```json
{
  "code": 200,        // 状态码: 200成功, 其他失败
  "message": "成功",  // 提示信息
  "data": {}          // 响应数据
}
```

### 错误码说明
| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### POST-First原则

所有接口统一使用POST方法：

```javascript
// 查询类
POST /{module}/getDetail      // 获取详情
POST /{module}/getList        // 获取列表
POST /{module}/getTree        // 获取树形

// 操作类
POST /{module}/create         // 创建
POST /{module}/update         // 更新
POST /{module}/delete         // 删除
POST /{module}/{action}       // 自定义操作
```

### 请求体格式
```json
{
  "id": 1,
  "name": "示例名称"
}
```

---

## 开发规范

### 后端开发规范

#### COLA架构分层
```
domain/           # 领域层
├── entity/       # 实体类
└── repository/   # 仓储接口

app/              # 应用层
├── service/      # 服务接口
└── impl/         # 服务实现

infrastructure/  # 基础设施层
├── mapper/       # MyBatis Mapper
└── repository/   # 仓储实现

adapter/          # 适配层
└── web/          # Controller
```

#### 文件命名规范
| 层级 | 命名模式 | 示例 |
|------|---------|------|
| 实体 | `{Module}Entity` | `AssetEntity.java` |
| DO | `{Module}DO` | `AssetDO.java` |
| Mapper | `{Module}Mapper` | `AssetMapper.java` |
| Service | `{Module}Service` | `AssetService.java` |
| Controller | `{Module}Controller` | `AssetController.java` |
| DTO Query | `{Action}Qry` | `AssetGetListQry.java` |
| DTO Cmd | `{Action}Cmd` | `AssetCreateCmd.java` |

#### Mapper开发规范
```java
@Mapper
public interface AssetMapper {
    // 明确指定参数名
    AssetDO selectById(@Param("id") Long id);

    // 使用专门的Query类
    List<AssetDO> selectList(@Param("query") AssetQuery query);

    // 返回类型使用DO类，不使用Map
    List<AssetDO> selectList(AssetQuery query);
}
```

### 前端开发规范

#### 目录结构
```
src/
├── api/              # API调用
│   ├── asset.ts      # 素材相关
│   ├── approval.ts   # 审批相关
│   ├── auth.ts       # 认证相关
│   ├── task.ts       # 任务相关
│   ├── workflow.ts   # 工作流相关
│   └── ...
├── views/            # 页面组件
│   ├── asset/        # 素材管理
│   │   ├── index.vue              # 素材列表
│   │   ├── material-entry.vue     # 素材录入
│   │   └── usage-apply.vue        # 使用申请
│   ├── workflow/      # 工作流
│   │   ├── index.vue              # 工作流列表
│   │   └── design.vue             # 流程设计器
│   ├── task/          # 任务管理
│   │   ├── pending-approval.vue   # 待办审批
│   │   ├── my-initiated.vue       # 我发起的
│   │   ├── draft-box.vue          # 草稿箱
│   │   ├── material-approval.vue  # 素材审批
│   │   ├── flow-items.vue         # 流经事项
│   │   └── notifications.vue      # 知会事项
│   ├── log/           # 日志
│   │   └── index.vue              # 日志查询
│   └── system/        # 系统管理
│       ├── user.vue               # 用户管理
│       ├── dept.vue               # 部门管理
│       ├── role.vue               # 角色管理
│       └── menu.vue               # 菜单管理
├── components/       # 公共组件
│   ├── AssetSelector.vue          # 素材选择器
│   ├── UserSelector.vue           # 用户选择器
│   ├── NotifyDialog.vue           # 通知对话框
│   └── WorkOrderDetailDialog.vue  # 工单详情对话框
├── layouts/          # 布局组件
│   └── MainLayout.vue             # 主布局
├── stores/           # Pinia状态
│   └── user.ts                    # 用户状态
├── router/           # 路由配置
│   └── index.ts
├── utils/            # 工具函数
│   └── request.ts                # Axios封装
└── main.ts           # 应用入口
```

#### 路由配置

主要路由路径：

| 路径 | 组件 | 说明 |
|------|------|------|
| `/login` | `login/index.vue` | 登录页 |
| `/asset` | `asset/index.vue` | 素材列表 |
| `/asset/material-entry` | `asset/material-entry.vue` | 素材录入 |
| `/asset/usage-apply` | `asset/usage-apply.vue` | 使用申请 |
| `/asset/deletion` | `asset/deletion/index.vue` | 删除申请 |
| `/workflow` | `workflow/index.vue` | 工作流列表 |
| `/workflow/design/:id?` | `workflow/design.vue` | 流程设计器 |
| `/task/pending-approval` | `task/pending-approval.vue` | 待办审批 |
| `/task/my-initiated` | `task/my-initiated.vue` | 我发起的 |
| `/task/draft-box` | `task/draft-box.vue` | 草稿箱 |
| `/task/flow-items` | `task/flow-items.vue` | 流经事项 |
| `/task/notifications` | `task/notifications.vue` | 知会事项 |
| `/log` | `log/index.vue` | 日志查询 |
| `/system/user` | `system/user.vue` | 用户管理 |
| `/system/dept` | `system/dept.vue` | 部门管理 |
| `/system/role` | `system/role.vue` | 角色管理 |
| `/system/menu` | `system/menu.vue` | 菜单管理 |

#### 组件命名
| 类型 | 命名模式 | 示例 |
|------|---------|------|
| 页面 | `{module}/{page}.vue` | `asset/index.vue` |
| 组件 | `{Name}Selector.vue` | `AssetSelector.vue` |
| 组件 | `{Name}Dialog.vue` | `NotifyDialog.vue` |

---

## 常见开发场景

### 场景1: 添加新功能模块

#### 步骤1: 创建数据库表
```sql
CREATE TABLE example_feature (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例功能表';
```

#### 步骤2: 创建后端代码
```
domain/
├── example/                      # 业务模块目录
│   ├── entity/ExampleFeature.java
│   └── repository/ExampleFeatureRepository.java

app/
├── example/
│   ├── ExampleFeatureService.java
│   └── impl/ExampleFeatureServiceImpl.java

infrastructure/
├── example/
│   ├── ExampleFeatureMapper.java
│   ├── ExampleFeatureQuery.java
│   ├── repository/ExampleFeatureRepositoryImpl.java
│   └── mapper/ExampleFeatureMapper.xml
├── dataobject/ExampleFeatureDO.java

client/
├── example/
│   ├── ExampleFeatureDTO.java
│   ├── ExampleFeatureCreateCmd.java
│   └── ExampleFeatureGetDetailQry.java

adapter/
└── web/example/
    └── ExampleFeatureController.java
```

#### 步骤3: 创建前端代码
```
api/exampleFeature.ts
views/example-feature/index.vue
```

#### 步骤4: 添加菜单数据
```sql
INSERT INTO sys_menu (name, path, component) VALUES
('示例功能', '/example', 'example/index');
```

### 场景2: 添加审批类型

#### 步骤1: 定义业务类型
```java
// ApprovalController.java
public enum BusinessType {
    MATERIAL_ENTRY("素材录入"),
    ASSET_USAGE("素材使用"),
    ASSET_DELETION("素材删除"),
    // 新增
    NEW_FEATURE("新功能");
}
```

#### 步骤2: 创建申请表
参照 `material_application` 创建新表。

#### 步骤3: 配置审批流程
在 `workflow` 表中配置对应流程。

### 场景3: 添加审批人选择

#### 步骤1: 服务层实现
```java
// ApproverSelectionService.java
public List<UserDTO> getFirstStageApprovers(Long workflowId, Long applicantId);
public List<UserDTO> getNextStageApprovers(Long workflowId, Long currentStageId);
```

#### 步骤2: 前端组件
```vue
<!-- ApproverSelector.vue -->
<template>
  <el-select v-model="selectedApprovers" multiple>
    <el-option
      v-for="user in userList"
      :key="user.id"
      :label="user.realName"
      :value="user.id"
    />
  </el-select>
</template>
```

---

## 快速参考

### 默认账号
| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 管理员 (ROLE_ID=1) | 系统默认管理员账号 |

### 访问地址
| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:3000 |
| 后端API | http://localhost:8080/api |
| API文档 | http://localhost:8080/api/doc.html |

### 端口说明
| 端口 | 服务 |
|------|------|
| 80/443 | Nginx代理 |
| 3000 | Vue开发服务器 |
| 8080 | Spring Boot应用 |
| 3306 | MySQL数据库 |
