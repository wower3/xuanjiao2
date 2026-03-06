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
├── xuanjiao-common/        # 通用工具模块 - ConvertUtils对象复制工具
├── xuanjiao-client/       # 客户端层 - DTO定义
│   ├── auth/             # 认证相关DTO
│   ├── user/             # 用户相关DTO
│   ├── dept/             # 部门相关DTO
│   ├── role/             # 角色相关DTO
│   ├── menu/             # 菜单相关DTO
│   ├── asset/            # 素材相关DTO
│   ├── material/         # 素材录入相关DTO
│   ├── deletion/         # 素材删除相关DTO
│   ├── notification/     # 通知相关DTO
│   ├── usage/            # 素材使用相关DTO
│   ├── workflow/         # 工作流相关DTO
│   ├── approval/         # 审批相关DTO
│   ├── log/              # 日志相关DTO
│   └── task/             # 任务相关DTO
├── xuanjiao-domain/       # 领域层 - 实体、仓储接口
│   ├── auth/             # 认证模块
│   ├── user/             # 用户模块
│   ├── dept/             # 部门模块
│   ├── role/             # 角色模块
│   ├── menu/             # 菜单模块
│   ├── asset/            # 素材模块
│   ├── material/         # 素材录入模块
│   ├── deletion/         # 素材删除模块
│   ├── notification/     # 通知模块
│   ├── usage/            # 素材使用模块
│   ├── workflow/         # 工作流模块
│   ├── approval/         # 审批模块
│   └── log/             # 日志模块
├── xuanjiao-app/         # 应用层 - 服务实现
│   ├── auth/             # 认证服务
│   ├── user/             # 用户服务
│   ├── dept/             # 部门服务
│   ├── role/             # 角色服务
│   ├── menu/             # 菜单服务
│   ├── asset/            # 素材服务
│   ├── deletion/         # 素材删除服务
│   ├── material/         # 素材录入服务
│   ├── notification/     # 通知服务
│   ├── usage/            # 素材使用服务
│   ├── workflow/         # 工作流服务
│   └── approval/         # 审批服务
├── xuanjiao-infrastructure/ # 基础设施层 - MyBatis Mapper
│   ├── user/             # 用户Mapper和仓储实现
│   ├── dept/             # 部门Mapper
│   ├── role/             # 角色Mapper
│   ├── menu/             # 菜单Mapper
│   ├── asset/            # 素材Mapper和仓储实现
│   ├── deletion/         # 素材删除Mapper和仓储实现
│   ├── material/         # 素材录入Mapper和仓储实现
│   ├── notification/     # 通知Mapper
│   ├── usage/            # 素材使用Mapper
│   ├── workflow/         # 工作流Mapper
│   ├── approval/         # 审批Mapper
│   ├── dataobject/       # DO类(数据库映射实体)
│   └── config/           # 基础设施配置(MyBatisConfig)
├── xuanjiao-adapter/     # 适配层 - REST Controller
│   ├── auth/             # AuthController
│   ├── user/             # UserController
│   ├── dept/             # DeptController
│   ├── role/             # RoleController
│   ├── menu/             # MenuController
│   ├── asset/            # AssetController, TagController
│   ├── deletion/         # AssetDeletionController
│   ├── material/         # MaterialApplicationController
│   ├── notification/     # NotificationController
│   ├── usage/            # UsageApplyController, UsageLogController
│   ├── workflow/         # WorkflowController, ApproverSelectionController
│   ├── approval/         # ApprovalController
│   └── task/             # TaskController
└── xuanjiao-start/       # 启动模块 - Spring Boot应用入口
```

---

## 技术栈

### 后端技术
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 8 | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| COLA | 4.3.2 | 架构框架 |
| MyBatis | 原生XML Mapper方式 | 数据访问 |
| MySQL | 8.0 | 数据库 |
| JWT | - | 认证授权 |
| Knife4j | 4.1.0 | API文档 |
| MapStruct | 1.5.5 | DTO映射(复杂转换) |
| Lombok | 1.18.30 | 代码生成 |
| ConvertUtils | 自定义 | 对象复制(简单复制) |

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

### 1. 用户认证模块 (auth)

#### 功能描述
提供用户登录、登出、Token管理功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `AuthController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/auth/` |
| `AuthService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/auth/` |
| `AuthServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/auth/impl/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 用户登录 |
| POST | `/auth/logout` | 用户登出 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `login/index.vue` | `xuanjiao-frontend/src/views/login/` |
| `auth.ts` | `xuanjiao-frontend/src/api/` |

---

### 2. 用户管理模块 (user)

#### 功能描述
提供用户的增删改查、角色分配等功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `UserController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/user/` |
| `UserService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/user/` |
| `UserServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/user/impl/` |
| `UserMapper.java` | `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/user/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/user/list` | 分页查询用户 |
| POST | `/user/getDetail` | 获取用户详情 |
| POST | `/user/create` | 创建用户 |
| POST | `/user/update` | 更新用户 |
| POST | `/user/delete` | 删除用户 |
| POST | `/user/resetPassword` | 重置密码 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `system/user.vue` | `xuanjiao-frontend/src/views/system/` |
| `user.ts` | `xuanjiao-frontend/src/api/` |

---

### 3. 部门管理模块 (dept)

#### 功能描述
提供部门的树形结构管理功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `DeptController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/dept/` |
| `DeptService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/dept/` |
| `DeptServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/dept/impl/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/dept/getTree` | 获取部门树 |
| POST | `/dept/create` | 创建部门 |
| POST | `/dept/update` | 更新部门 |
| POST | `/dept/delete` | 删除部门 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `system/dept.vue` | `xuanjiao-frontend/src/views/system/` |
| `dept.ts` | `xuanjiao-frontend/src/api/` |

---

### 4. 角色管理模块 (role)

#### 功能描述
提供角色的增删改查、权限分配、菜单绑定、审批流程绑定功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `RoleController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/role/` |
| `RoleService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/role/` |
| `RoleServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/role/impl/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/role/list` | 分页查询角色 |
| POST | `/role/getDetail` | 获取角色详情 |
| POST | `/role/create` | 创建角色 |
| POST | `/role/update` | 更新角色 |
| POST | `/role/delete` | 删除角色 |
| POST | `/role/bindMenu` | 绑定菜单权限 |
| POST | `/role/bindWorkflow` | 绑定审批流程 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `system/role.vue` | `xuanjiao-frontend/src/views/system/` |
| `role.ts` | `xuanjiao-frontend/src/api/` |

---

### 5. 菜单管理模块 (menu)

#### 功能描述
提供系统菜单的树形结构管理功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `MenuController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/menu/` |
| `MenuService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/menu/` |
| `MenuServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/menu/impl/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/menu/getTree` | 获取菜单树 |
| POST | `/menu/create` | 创建菜单 |
| POST | `/menu/update` | 更新菜单 |
| POST | `/menu/delete` | 删除菜单 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `system/menu.vue` | `xuanjiao-frontend/src/views/system/` |
| `menu.ts` | `xuanjiao-frontend/src/api/` |

---

### 6. 素材管理模块 (asset)

#### 功能描述
提供素材的上传、预览、下载、标签管理、列表查询等功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `AssetController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/asset/` |
| `TagController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/asset/` |
| `AssetService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/asset/` |
| `AssetServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/asset/impl/` |
| `TagService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/asset/` |
| `TagServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/asset/impl/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/asset/list` | 获取素材列表 |
| POST | `/asset/getDetail` | 获取素材详情 |
| POST | `/asset/upload` | 上传素材 |
| POST | `/asset/delete` | 删除素材 |
| POST | `/asset/adminDelete` | 管理员彻底删除 |
| POST | `/asset/adjustDeleteTime` | 调整删除时间(测试) |
| POST | `/asset/admin/trigger-cleanup` | 手动触发清理 |
| POST | `/asset/getMyApproved` | 获取我的已录入素材 |
| GET | `/asset/preview/{id}` | 预览素材 |
| GET | `/asset/thumbnail/{id}` | 查看缩略图 |
| GET | `/asset/download/{id}` | 下载素材 |
| POST | `/asset/upload-copyright` | 上传版权附件 |
| POST | `/tag/getList` | 获取标签列表 |
| POST | `/tag/getListByCategory` | 按类别获取标签 |
| POST | `/tag/create` | 创建标签 |
| POST | `/tag/delete` | 删除标签 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `asset/index.vue` | `xuanjiao-frontend/src/views/asset/` |
| `asset/material-entry.vue` | `xuanjiao-frontend/src/views/asset/` |
| `asset/usage-apply.vue` | `xuanjiao-frontend/src/views/asset/` |
| `asset/deletion/index.vue` | `xuanjiao-frontend/src/views/asset/deletion/` |
| `asset/deletion/MyAssets.vue` | `xuanjiao-frontend/src/views/asset/deletion/` |
| `asset/deletion/DeletionApply.vue` | `xuanjiao-frontend/src/views/asset/deletion/` |
| `asset.ts` | `xuanjiao-frontend/src/api/` |
| `tag.ts` | `xuanjiao-frontend/src/api/` |

#### 前端组件
| 组件 | 路径 |
|------|------|
| `AssetSelector.vue` | `xuanjiao-frontend/src/components/` |

---

### 7. 素材录入模块 (material)

#### 功能描述
提供素材录入申请的创建、修改、提交审批功能，支持多素材上传。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `MaterialApplicationController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/material/` |
| `MaterialApplicationService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/material/` |
| `MaterialApplicationServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/material/impl/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/material-application/create` | 创建申请单(草稿) |
| POST | `/material-application/update` | 更新申请单 |
| POST | `/material-application/{id}/submit` | 提交申请单 |
| POST | `/material-application/delete` | 删除申请单 |
| POST | `/material-application/getDetail` | 查询申请单详情 |
| POST | `/material-application/getDrafts` | 查询草稿箱 |
| POST | `/material-application/getMyApplications` | 查询我的申请单 |
| POST | `/material-application/{id}/copy` | 复制申请单 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `task/material-approval.vue` | `xuanjiao-frontend/src/views/task/` |
| `materialApplication.ts` | `xuanjiao-frontend/src/api/` |

---

### 8. 素材使用模块 (usage)

#### 功能描述
提供素材使用申请的创建、修改、提交审批功能，支持多对多关系(一个申请包含多个素材)。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `UsageApplyController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/usage/` |
| `UsageLogController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/usage/` |
| `UsageApplyService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/usage/` |
| `UsageApplyServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/usage/impl/` |
| `UsageLogService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/usage/` |
| `UsageLogServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/usage/impl/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/usage-apply/draft` | 创建使用申请草稿 |
| POST | `/usage-apply/update` | 更新使用申请草稿 |
| POST | `/usage-apply/{id}/submit` | 提交使用申请 |
| POST | `/usage-apply/delete` | 删除使用申请 |
| POST | `/usage-apply/getDetail` | 查询申请单详情 |
| POST | `/usage-apply/getDrafts` | 查询草稿箱 |
| POST | `/usage-apply/getMyApplications` | 查询我的所有申请 |
| POST | `/usage-apply/canUseAsset` | 检查是否有权限使用素材 |
| POST | `/usage-apply/{id}/copy` | 复制使用申请 |
| POST | `/usage-apply/apply` | 申请使用素材(旧API) |
| GET | `/usage-apply/my-applications` | 查询我的申请列表(旧API) |
| POST | `/usage-log/page` | 分页查询使用日志 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `asset/usage-apply.vue` | `xuanjiao-frontend/src/views/asset/` |
| `usageApply.ts` | `xuanjiao-frontend/src/api/` |
| `usageLog.ts` | `xuanjiao-frontend/src/api/` |

---

### 9. 素材删除模块 (deletion)

#### 功能描述
提供素材删除申请的创建、审批、执行功能，支持两级删除机制。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `AssetDeletionController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/deletion/` |
| `AssetDeletionApplicationService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/deletion/` |
| `AssetDeletionApplicationServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/deletion/impl/` |
| `AssetDeletionCleanupTask.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/schedule/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/deletion/create` | 创建删除申请 |
| POST | `/deletion/update` | 更新删除申请 |
| POST | `/deletion/{id}/submit` | 提交审批 |
| POST | `/deletion/delete` | 删除申请 |
| POST | `/deletion/getDetail` | 获取详情 |
| POST | `/deletion/getMyApplications` | 获取我的申请 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `asset/deletion/index.vue` | `xuanjiao-frontend/src/views/asset/deletion/` |
| `asset/deletion/MyAssets.vue` | `xuanjiao-frontend/src/views/asset/deletion/` |
| `asset/deletion/DeletionApply.vue` | `xuanjiao-frontend/src/views/asset/deletion/` |
| `assetDeletion.ts` | `xuanjiao-frontend/src/api/` |

#### 两级删除机制
1. **Stage 1: DELETED状态**
   - 审批通过后标记为DELETED
   - 素材可见但不可用
   - 7天冷静期

2. **Stage 2: 软删除(deleted=1)**
   - 7天后自动执行
   - 管理员可手动触发
   - 素材完全隐藏

---

### 10. 审批流程模块 (workflow)

#### 功能描述
提供审批流程的可视化设计、配置、管理功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `WorkflowController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/workflow/` |
| `ApproverSelectionController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/workflow/` |
| `WorkflowService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/` |
| `WorkflowServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/` |
| `ApproverSelectionService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/` |
| `ApproverSelectionServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/` |
| `WorkflowEngineService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/` |
| `WorkflowEngineServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/` |

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
| POST | `/approver-selection/getFirstStageApprovers` | 获取第一层审批人 |
| POST | `/approver-selection/getNextStageApprovers` | 获取下一层审批人 |
| POST | `/approver-selection/getSubWorkflowFirstStageApprovers` | 获取子流程第一层审批人 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `workflow/index.vue` | `xuanjiao-frontend/src/views/workflow/` |
| `workflow/design.vue` | `xuanjiao-frontend/src/views/workflow/` |
| `workflow.ts` | `xuanjiao-frontend/src/api/` |

#### 流程数据结构
```javascript
{
  "id": 1,
  "name": "素材录入审批流程",
  "workflowType": "ASSET_UPLOAD",
  "boundRoleId": 2,
  "stages": [
    {
      "id": 1,
      "name": "部门主管审批",
      "order": 1,
      "approveType": "OR",
      "approvers": [
        { "type": "USER", "id": 2 },
        { "type": "ROLE", "id": 2 }
      ]
    }
  ]
}
```

---

### 11. 审批执行模块 (approval)

#### 功能描述
提供审批任务的处理、查看、流转功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `ApprovalController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/approval/` |
| `ApprovalService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/approval/` |
| `ApprovalServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/approval/impl/` |

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
| POST | `/approval/getMyApplied` | 获取我的已发起申请 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `task/pending-approval.vue` | `xuanjiao-frontend/src/views/task/` |
| `task/my-initiated.vue` | `xuanjiao-frontend/src/views/task/` |
| `task/draft-box.vue` | `xuanjiao-frontend/src/views/task/` |
| `task/flow-items.vue` | `xuanjiao-frontend/src/views/task/` |
| `task/notifications.vue` | `xuanjiao-frontend/src/views/task/` |
| `approval.ts` | `xuanjiao-frontend/src/api/` |
| `flowItems.ts` | `xuanjiao-frontend/src/api/` |

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

### 12. 通知模块 (notification)

#### 功能描述
提供系统通知的发送、接收、阅读管理功能，支持知会(mention)功能。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `NotificationController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/notification/` |
| `NotificationService.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/notification/` |
| `NotificationServiceImpl.java` | `xuanjiao-app/src/main/java/com/xuanjiao/app/notification/impl/` |

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
| GET | `/notification/getUnreadCount` | 获取未读数量 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `task/notifications.vue` | `xuanjiao-frontend/src/views/task/` |
| `notification.ts` | `xuanjiao-frontend/src/api/` |

#### 通知类型
| 类型 | 说明 |
|------|------|
| WORKFLOW_FLOW | 流程流转通知 |
| MENTION | 知会通知 |
| SYSTEM | 系统通知 |

---

### 13. 统一草稿箱模块 (task)

#### 功能描述
统一的草稿箱，管理所有类型的草稿申请(素材录入、使用申请、删除申请)。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `TaskController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/task/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/task/queryDrafts` | 查询草稿列表(支持类型和标题筛选) |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `task/draft-box.vue` | `xuanjiao-frontend/src/views/task/` |
| `task.ts` | `xuanjiao-frontend/src/api/` |

---

### 14. 操作日志模块 (log)

#### 功能描述
记录系统的操作日志，用于审计和问题追溯。

#### 后端实现
| 文件 | 路径 |
|------|------|
| `LogController.java` | `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/log/` |

#### API接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/log/queryLogs` | 查询操作日志 |

#### 前端实现
| 文件 | 路径 |
|------|------|
| `log/index.vue` | `xuanjiao-frontend/src/views/log/` |
| `log.ts` | `xuanjiao-frontend/src/api/` |

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
| `usage_apply_asset` | 使用申请素材关联(中间表) |
| `asset_deletion_application` | 素材删除申请 |
| `asset_deletion_asset` | 删除申请素材关联 |

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
│   │   ├── usage-apply.vue        # 使用申请
│   │   └── deletion/              # 删除申请
│   ├── workflow/      # 工作流
│   ├── task/          # 任务管理
│   ├── log/           # 日志
│   └── system/        # 系统管理
├── components/       # 公共组件
│   ├── AssetSelector.vue
│   ├── UserSelector.vue
│   ├── NotifyDialog.vue
│   └── WorkOrderDetailDialog.vue
├── layouts/          # 布局组件
├── stores/           # Pinia状态
├── router/           # 路由配置
├── utils/            # 工具函数
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
