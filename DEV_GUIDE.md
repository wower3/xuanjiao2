# 宣传教育平台 - 开发文档

## 一、项目概述

### 1.1 项目简介
宣传教育平台是一个企业级素材资产管理系统，用于管理企业内部素材的统一管理、审批流转和使用追踪。

### 1.2 技术栈

**后端：**
- Java 8
- Spring Boot 2.7.18
- COLA 4.3.2 架构
- MyBatis Plus 3.5.3.1
- MySQL 8.0
- JWT 认证

**前端：**
- Vue 3.4
- TypeScript
- Vite 5
- Element Plus 2.4
- Pinia 状态管理
- Vue Router 4

### 1.3 项目结构

```
xuanjiao2/
├── xuanjiao-backend/          # 后端项目
│   ├── xuanjiao-client/       # 客户端层 (DTO、API定义)
│   ├── xuanjiao-domain/       # 领域层 (实体、仓储接口)
│   ├── xuanjiao-app/          # 应用层 (服务实现)
│   ├── xuanjiao-infrastructure/ # 基础设施层 (Mapper、仓储实现)
│   ├── xuanjiao-adapter/      # 适配层 (Controller)
│   ├── xuanjiao-start/        # 启动模块
│   └── sql/                   # 数据库脚本
├── xuanjiao-frontend/         # 前端项目
│   └── src/
│       ├── api/               # API接口
│       ├── router/            # 路由配置
│       ├── stores/            # 状态管理
│       ├── layouts/           # 布局组件
│       └── views/             # 页面视图
├── REQUIREMENTS.md            # 需求文档
├── PROGRESS.md               # 进度文档
└── DEV_GUIDE.md              # 本开发文档
```

## 二、环境配置

### 2.1 数据库配置

```
HOST: 127.0.0.1
PORT: 3306
USER: root
PASSWORD: 123456
DATABASE: xuanjiao_s
```

### 2.2 后端配置文件

配置文件位置：`xuanjiao-start/src/main/resources/application.yml`

关键配置项：
- 服务端口：8080
- 上下文路径：/api
- 文件上传路径：D:/xuanjiao/uploads/
- JWT密钥和过期时间

### 2.3 前端配置

- 开发端口：3000
- API代理：/api -> http://localhost:8080

## 三、启动说明

### 3.1 初始化数据库

```bash
mysql -u root -p123456 < xuanjiao-backend/sql/init_all.sql
```

### 3.2 启动后端

```bash
cd xuanjiao-backend
mvn clean install
mvn spring-boot:run -pl xuanjiao-start
```

### 3.3 启动前端

```bash
conda activate python20251006
cd xuanjiao-frontend
npm install
npm run dev
```

### 3.4 访问地址

- 前端：http://localhost:3000
- 后端API：http://localhost:8080/api
- Swagger文档：http://localhost:8080/api/doc.html
- 默认账号：admin / 123456

## 四、API接口说明

### 4.1 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/logout | POST | 用户登出 |

### 4.2 用户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/user/current | GET | 获取当前用户 |
| /api/user/list | GET | 用户列表 |

### 4.3 素材接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/asset/upload | POST | 上传素材 |
| /api/asset/list | GET | 素材列表 |
| /api/asset/{id} | GET | 素材详情 |
| /api/asset/{id} | DELETE | 删除素材 |
| /api/asset/preview/{id} | GET | 预览素材 |

### 4.4 流程接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/workflow/list | GET | 流程列表 |
| /api/workflow/{id} | GET | 流程详情 |
| /api/workflow | POST | 保存流程 |
| /api/workflow | PUT | 更新流程 |
| /api/workflow/{id}/status | PUT | 更新状态 |

### 4.5 审批接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/approval/tasks | GET | 待我审批 |
| /api/approval/applied | GET | 我发起的 |
| /api/approval/tasks/{id}/approve | POST | 审批操作 |

### 4.6 其他接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/dept/list | GET | 部门列表 |
| /api/role/list | GET | 角色列表 |
| /api/log/list | GET | 日志列表 |

## 五、数据库表结构

### 5.1 用户相关

| 表名 | 说明 |
|------|------|
| sys_user | 用户表 |
| sys_role | 角色表 |
| sys_dept | 部门表 |
| sys_user_role | 用户角色关联表 |

### 5.2 素材相关

| 表名 | 说明 |
|------|------|
| asset | 素材表 |
| usage_log | 使用日志表 |

### 5.3 审批相关

| 表名 | 说明 |
|------|------|
| workflow | 审批流程定义表 |
| workflow_stage | 审批流程阶段表 |
| stage_approver | 阶段审批人配置表 |
| approval_instance | 审批实例表 |
| approval_task | 审批任务表 |

## 六、待开发功能

### 6.1 审批实例创建和流转（P0）

**功能描述：** 素材上传后发起审批流程，按层级顺序流转

**开发要点：**
1. 素材上传时选择审批流程
2. 创建审批实例和第一层任务
3. 审批通过后自动流转到下一层
4. 支持会签（全部通过）和或签（任一通过）

### 6.2 权限控制（P0）

**功能描述：** 基于角色的权限控制

**开发要点：**
1. 定义权限资源表
2. 角色-权限关联
3. 前端菜单权限控制
4. 后端接口权限校验

### 6.3 其他待开发功能（P1/P2）

- 流程版本管理
- 内置审批模板
- 版权信息管理
- 日志统计分析
