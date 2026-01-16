# 宣传教育平台 (Propaganda/Education Platform)

> 企业级媒体资产管理系统，提供素材统一管理、审批流转和使用追踪功能

## 目录

- [项目概述](#项目概述)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [目录结构](#目录结构)
- [快速开始](#快速开始)
- [核心功能](#核心功能)
- [审批流程模型](#审批流程模型)
- [API文档](#api文档)
- [数据库设计](#数据库设计)
- [开发指南](#开发指南)
- [部署指南](#部署指南)
- [常见问题](#常见问题)

---

## 项目概述

### 产品定位

宣传教育平台是一个企业级的素材资产管理系统，旨在解决企业内部素材的统一管理、审批流转和使用追踪问题。

### 核心价值

- **统一管理**：集中管理企业所有媒体素材（视频、图片、文档）
- **规范审批**：灵活可配置的审批工作流引擎
- **权限控制**：基于角色的细粒度权限管理
- **使用追踪**：完整的素材使用记录和审计日志

### 目标用户

| 用户类型 | 说明 |
|----------|------|
| 普通用户 | 上传素材、申请使用素材 |
| 审批员 | 处理素材录入和使用审批 |
| 管理员 | 系统配置、用户管理、流程设计 |

---

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 编程语言 |
| Spring Boot | 2.7.18 | 应用框架 |
| COLA Framework | 4.3.2 | 分层架构框架 |
| MyBatis Plus | 3.5.3.1 | ORM框架 |
| MySQL | 8.0 | 数据库 |
| Knife4j | 4.1.0 | API文档 (Swagger UI) |
| MapStruct | 1.5.5 | DTO映射 |
| Hutool | 5.8.22 | 工具库 |
| JWT | 0.9.1 | 用户认证 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 前端框架 |
| TypeScript | 5.3 | 编程语言 |
| Vite | 5.0 | 构建工具 |
| Element Plus | 2.4 | UI组件库 |
| Pinia | 2.1 | 状态管理 |
| Vue Router | 4.2 | 路由管理 |
| Axios | 1.6 | HTTP客户端 |
| vuedraggable | 4.1 | 拖拽组件 |

---

## 系统架构

### COLA架构设计

本项目采用阿里巴巴COLA（Clean Object-Oriented and Layered Architecture）分层架构：

```
┌─────────────────────────────────────────────────────────────┐
│                         Adapter Layer                       │
│  (用户接口层 - 处理HTTP请求、参数校验、响应封装)              │
│  - Controllers (REST API)                                   │
│  - Authentication Interceptor                              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                          App Layer                          │
│  (应用层 - 业务逻辑编排、事务控制)                            │
│  - Services (业务服务)                                      │
│  - WorkflowEngineService (审批引擎)                         │
│  - ApproverSelectionService (审批人选择)                     │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                        Domain Layer                         │
│  (领域层 - 核心业务逻辑、实体模型)                            │
│  - Entities (实体)                                          │
│  - Repository Interfaces (仓储接口)                         │
└─────────────────────────────────────────────────────────────┘
                              ↑
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                      │
│  (基础设施层 - 数据访问、外部服务集成)                        │
│  - MyBatis Mappers (数据访问)                               │
│  - Repository Implementations (仓储实现)                     │
└─────────────────────────────────────────────────────────────┘
                              ↑
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                         │
│  (客户端层 - DTO定义，各层共享)                              │
│  - Request/Response DTOs                                    │
│  - Result (统一响应封装)                                    │
└─────────────────────────────────────────────────────────────┘
```

### 依赖关系

```
xuanjiao-start  (启动模块)
    ↓
xuanjiao-adapter  →  xuanjiao-app  →  xuanjiao-domain
    ↓                    ↓               ↑
xuanjiao-infrastructure ─────────────────┘
    ↓
xuanjiao-client (所有模块共享)
```

---

## 目录结构

```
xuanjiao2/
├── xuanjiao-backend/              # 后端服务
│   ├── xuanjiao-start/           # 启动模块
│   ├── xuanjiao-adapter/         # 适配器层 (Controllers)
│   ├── xuanjiao-app/             # 应用层 (Services)
│   ├── xuanjiao-domain/          # 领域层 (Entities, Repository)
│   ├── xuanjiao-infrastructure/  # 基础设施层 (Mappers)
│   ├── xuanjiao-client/          # 客户端层 (DTOs)
│   └── sql/                      # 数据库脚本
│
├── xuanjiao-frontend/            # 前端服务
│   ├── src/
│   │   ├── api/                 # API客户端
│   │   ├── assets/              # 静态资源
│   │   ├── layouts/             # 布局组件
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # 状态管理
│   │   ├── utils/               # 工具函数
│   │   ├── views/               # 页面组件
│   │   ├── App.vue              # 根组件
│   │   └── main.ts              # 入口文件
│   ├── index.html               # HTML模板
│   ├── vite.config.ts           # Vite配置
│   └── package.json             # 依赖配置
│
├── REQUIREMENTS.md               # 产品需求文档
├── PROGRESS.md                   # 开发进度
└── README.md                     # 本文档
```

### 后端模块说明

| 模块 | 说明 | 主要内容 |
|------|------|----------|
| `xuanjiao-start` | 启动模块 | Application入口, application.yml |
| `xuanjiao-adapter` | 适配器层 | REST Controllers, 拦截器 |
| `xuanjiao-app` | 应用层 | 业务服务实现, 事务管理 |
| `xuanjiao-domain` | 领域层 | 实体类, 仓储接口 |
| `xuanjiao-infrastructure` | 基础设施层 | MyBatis Mapper, 仓储实现 |
| `xuanjiao-client` | 客户端层 | DTO定义, 统一响应 |

---

## 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|----------|
| JDK | 1.8+ |
| Node.js | 16+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |

### 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
mysql -u root -p123456 < xuanjiao-backend/sql/init_all.sql
```

### 后端启动

```bash
cd xuanjiao-backend

# 编译项目
mvn clean install

# 启动服务 (端口: 8080)
mvn spring-boot:run -pl xuanjiao-start
```

### 前端启动

```bash
cd xuanjiao-frontend

# 安装依赖
npm install

# 启动开发服务器 (端口: 3000)
npm run dev
```

### 访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端应用 | http://localhost:3000 | Web界面 |
| 后端API | http://localhost:8080/api | REST接口 |
| API文档 | http://localhost:8080/api/doc.html | Swagger/Knife4j |

### 默认账号

```
用户名: admin
密码: 123456
```

---

## 核心功能

### 功能模块

| 模块 | 功能列表 | 状态 |
|------|----------|------|
| **素材管理** | 上传、下载、预览、搜索、删除 | ✅ |
| **审批流程** | 可视化流程设计、流程版本管理 | ✅ |
| **审批执行** | 待办任务、审批操作、审批记录 | ✅ |
| **使用申请** | 素材使用申请、权限控制 | ✅ |
| **用户管理** | 用户CRUD、部门管理、角色管理 | ✅ |
| **菜单权限** | 菜单配置、角色权限分配 | ✅ |
| **操作日志** | 操作记录、日志查询 | ✅ |

### 素材类型支持

| 类型 | 支持格式 |
|------|----------|
| 视频 | mp4, avi, mov, mkv |
| 图片 | jpg, png, gif, bmp, webp |
| 文档 | pdf, doc, docx, xls, xlsx, ppt, pptx |

---

## 审批流程模型

### 架构设计

系统采用 **"层级顺序 + 层内并行 + 子流程"** 的审批模型：

```
主流程 (Main Workflow)
    │
    ├─ Stage 1 (第一层审批) ──┐
    │   ├─ Approver A          │ 并行处理
    │   ├─ Approver B          │
    │   └─ Approver C          │
    │                          │
    ├─ Stage 2 (第二层审批) ───┘
    │   ├─ Approver D
    │   └─ Approver E
    │
    └─ Stage 3 (第三层审批)
        └─ Approver F
```

### 核心概念

| 概念 | 说明 |
|------|------|
| **Workflow (流程)** | 审批流程定义，包含多个审批阶段 |
| **Stage (审批层)** | 流程中的单个审批阶段，层间串行 |
| **Stage Approver (审批人)** | 层级内的审批人，层内并行 |
| **Sub-Workflow (子流程)** | 独立的子审批流程，非阻塞运行 |
| **Instance (实例)** | 流程的一次执行 |
| **Task (任务)** | 分配给单个审批人的待办任务 |

### 审批规则

| 规则类型 | 说明 |
|----------|------|
| **串行流转** | 第N层完成后才能进入第N+1层 |
| **层内并行** | 同一层所有审批人同时收到任务 |
| **会签** | 所有审批人都必须通过 |
| **或签** | 任一审批人通过即可 |

### 子流程特性

- **配置位置**: 审批人级别 (非审批层级)
- **触发方式**: 第一个审批人完成任务时手动触发
- **执行模式**: 并行运行，不阻塞主流程
- **完成条件**: 主流程 + 所有子流程都完成

---

## API文档

### 认证方式

所有API请求需要在Header中携带JWT Token：

```http
Authorization: Bearer <token>
```

### 核心接口

#### 用户认证

```
POST   /api/auth/login          # 用户登录
```

#### 素材管理

```
GET    /api/asset/list          # 获取素材列表
POST   /api/asset/upload        # 上传素材
GET    /api/asset/{id}          # 获取素材详情
DELETE /api/asset/{id}          # 删除素材
GET    /api/asset/download/{id} # 下载素材
```

#### 审批流程

```
GET    /api/workflow/list       # 获取流程列表
GET    /api/workflow/{id}       # 获取流程详情
POST   /api/workflow/save       # 保存流程
DELETE /api/workflow/{id}       # 删除流程
```

#### 审批任务

```
GET    /api/approval/pending    # 获取待办任务
POST   /api/approval/complete   # 完成审批
POST   /api/approval/reject     # 驳回审批
GET    /api/approval/progress   # 获取审批进度
```

详细API文档请访问: http://localhost:8080/api/doc.html

---

## 数据库设计

### 核心表结构

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `sys_user` | 用户表 | id, username, real_name, dept_id, role_id |
| `sys_dept` | 部门表 | id, name, parent_id, level |
| `sys_role` | 角色表 | id, name, role_type, status |
| `sys_menu` | 菜单表 | id, name, parent_id, path |
| `sys_role_menu` | 角色菜单关系表 | role_id, menu_id |
| `asset` | 素材表 | id, name, type, status, file_path, md5 |
| `usage_apply` | 使用申请表 | id, asset_id, applicant_id, status |
| `usage_log` | 使用日志表 | id, asset_id, user_id, action |
| `workflow` | 流程定义表 | id, name, workflow_type, status, bound_role_id |
| `workflow_stage` | 流程阶段表 | id, workflow_id, stage_order, stage_type |
| `stage_approver` | 阶段审批人表 | id, stage_id, approver_type, approver_id, sub_workflow_id |
| `approval_instance` | 审批实例表 | id, workflow_id, business_type, status |
| `approval_task` | 审批任务表 | id, instance_id, approver_id, status |
| `approval_progress` | 审批进度表 | id, instance_id, stage_order, approver_ids |

### ER关系

```
sys_user ──┬─→ sys_dept (多对一)
           └─→ sys_role (多对一)

sys_role ←─→ sys_menu (多对多 via sys_role_menu)

asset ──→ sys_user (上传者)
       └─→ usage_apply (一对多)
           └─→ approval_instance (使用审批)

workflow ──┬─→ workflow_stage (一对多)
           └─→ approval_instance (一对多)

workflow_stage ──→ stage_approver (一对多)

approval_instance ──→ approval_task (一对多)
                   ──→ approval_progress (一对多)
```

---

## 开发指南

### 后端开发规范

#### Controller层规范

```java
@Api(tags = "模块名称")
@RestController
@RequestMapping("/api/模块")
public class XxxController {

    @Resource
    private XxxService xxxService;

    @ApiOperation("接口说明")
    @PostMapping("/action")
    public Result<ResponseType> action(@RequestAttribute("userId") Long userId,
                                       @RequestBody RequestDTO dto) {
        // 参数校验
        // 调用Service
        // 返回结果
        return Result.success(data);
    }
}
```

#### Service层规范

```java
@Service
public class XxxServiceImpl implements XxxService {

    @Resource
    private XxxMapper xxxMapper;

    @Transactional(rollbackFor = Exception.class)
    public void businessMethod(InputDTO input) {
        // 业务逻辑编排
        // 调用Repository/Mapper
        // 事务控制
    }
}
```

### 前端开发规范

#### API定义

```typescript
// src/api/xxx.ts
import request from '@/utils/request'

export function getData(params: any) {
  return request.get('/api/xxx/data', { params })
}

export function saveData(data: any) {
  return request.post('/api/xxx/save', data)
}
```

#### 组件结构

```vue
<template>
  <div class="xxx-page">
    <!-- 模板内容 -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getData } from '@/api/xxx'

// 响应式数据
const list = ref([])

// 生命周期
onMounted(() => {
  loadData()
})

// 方法
async function loadData() {
  const res = await getData()
  list.value = res.data
}
</script>

<style scoped>
.xxx-page {
  /* 样式 */
}
</style>
```

### 添加新功能流程

1. **数据库**: 在 `sql/` 目录下创建迁移脚本
2. **后端**:
   - `client/`: 定义DTO
   - `domain/`: 定义Entity和Repository接口
   - `infrastructure/`: 实现Mapper
   - `app/`: 实现Service
   - `adapter/`: 创建Controller
3. **前端**:
   - `api/`: 定义API函数
   - `views/`: 创建页面组件
   - `router/`: 配置路由

---

## 部署指南

### 生产环境配置

#### 后端配置 (`application-prod.yml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://生产数据库地址:3306/xuanjiao_s
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

file:
  upload-path: /data/xuanjiao/uploads/
```

#### 前端构建

```bash
cd xuanjiao-frontend

# 生产构建
npm run build

# 输出目录: dist/
```

### Docker部署

```dockerfile
# 后端 Dockerfile
FROM openjdk:8-jre-alpine
COPY xuanjiao-start/target/xuanjiao-start.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 构建镜像
docker build -t xuanjiao-backend .

# 运行容器
docker run -d -p 8080:8080 \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=123456 \
  xuanjiao-backend
```

---

## 常见问题

### 1. 后端启动失败

**问题**: 端口被占用
```bash
# 检查端口占用
lsof -i:8080
# 或修改 application.yml 中的 server.port
```

### 2. 前端API请求失败

**问题**: 跨域或代理配置错误
```javascript
// vite.config.ts 确认代理配置
server: {
  proxy: {
    '/api': 'http://localhost:8080'
  }
}
```

### 3. 数据库连接失败

**问题**: 连接配置或密码错误
```yaml
# 检查 application.yml 配置
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/xuanjiao_s
    username: root
    password: 你的密码
```

### 4. 审批流程不流转

**问题**: 检查流程配置和审批人设置
- 确认审批人存在且有效
- 检查流程状态是否为启用
- 查看后台日志排查错误

---

## 许可证

本项目为内部使用系统，保留所有权利。

---

## 联系方式

如有问题请联系开发团队。

**文档最后更新**: 2025-01-16
