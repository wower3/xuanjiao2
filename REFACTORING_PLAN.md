# 代码重构计划 - 按功能模块分包

## 重构目标

将当前按技术分层的代码结构，重构为**COLA分层 + 功能模块分包**的结构，实现：

1. 同一层内不同模块代码互不影响
2. 为未来拆分为Maven多模块或微服务打好基础
3. 保持COLA架构的依赖规则

---

## 业务模块定义

| 模块代码 | 模块名称 | 说明 |
|----------|----------|------|
| `auth` | 认证模块 | 登录、登出、Token管理 |
| `user` | 用户模块 | 用户CRUD、用户权限查询 |
| `dept` | 部门模块 | 部门树、部门CRUD |
| `role` | 角色模块 | 角色CRUD、角色权限 |
| `menu` | 菜单模块 | 菜单树、菜单配置 |
| `asset` | 素材模块 | 素材上传、下载、管理 |
| `material` | 素材申请模块 | 素材录入申请（含未来删除功能） |
| `usage` | 使用申请模块 | 素材使用申请 |
| `workflow` | 流程定义模块 | 工作流设计、模板管理 |
| `approval` | 审批执行模块 | 审批实例、任务处理 |
| `log` | 日志模块 | 操作日志记录 |

---

## 目标包结构

```
com.xuanjiao
├── adapter
│   └── web
│       ├── auth/
│       │   └── AuthController.java
│       ├── user/
│       │   └── UserController.java
│       ├── dept/
│       │   └── DeptController.java
│       ├── role/
│       │   └── RoleController.java
│       ├── menu/
│       │   └── MenuController.java
│       ├── asset/
│       │   └── AssetController.java
│       ├── material/
│       │   └── MaterialApplicationController.java
│       ├── usage/
│       │   ├── UsageApplyController.java
│       │   └── UsageLogController.java
│       ├── workflow/
│       │   ├── WorkflowController.java
│       │   └── ApproverSelectionController.java
│       └── approval/
│           └── ApprovalController.java
│
├── app
│   ├── auth/
│   │   ├── AuthService.java
│   │   └── impl/
│   │       └── AuthServiceImpl.java
│   ├── user/
│   │   ├── UserService.java
│   │   └── impl/
│   │       └── UserServiceImpl.java
│   ├── dept/
│   │   ├── DeptService.java
│   │   └── impl/
│   │       └── DeptServiceImpl.java
│   ├── role/
│   │   ├── RoleService.java
│   │   └── impl/
│   │       └── RoleServiceImpl.java
│   ├── menu/
│   │   ├── MenuService.java
│   │   └── impl/
│   │       └── MenuServiceImpl.java
│   ├── asset/
│   │   ├── AssetService.java
│   │   └── impl/
│   │       └── AssetServiceImpl.java
│   ├── material/
│   │   ├── MaterialApplicationService.java
│   │   └── impl/
│   │       └── MaterialApplicationServiceImpl.java
│   ├── usage/
│   │   ├── UsageApplyService.java
│   │   ├── UsageLogService.java
│   │   └── impl/
│   │       ├── UsageApplyServiceImpl.java
│   │       └── UsageLogServiceImpl.java
│   ├── workflow/
│   │   ├── WorkflowService.java
│   │   ├── WorkflowEngineService.java
│   │   ├── ApproverSelectionService.java
│   │   └── impl/
│   │       ├── WorkflowServiceImpl.java
│   │       ├── WorkflowEngineServiceImpl.java
│   │       └── ApproverSelectionServiceImpl.java
│   └── approval/
│       ├── ApprovalService.java
│       └── impl/
│           └── ApprovalServiceImpl.java
│
├── domain
│   ├── auth/
│   │   └── (无实体，仅用于JWT工具)
│   ├── user/
│   │   ├── entity/
│   │   │   └── User.java
│   │   └── repository/
│   │       └── UserRepository.java
│   ├── dept/
│   │   ├── entity/
│   │   │   └── Dept.java
│   │   └── repository/
│   │       └── DeptRepository.java
│   ├── role/
│   │   ├── entity/
│   │   │   └── Role.java
│   │   └── repository/
│   │       └── RoleRepository.java
│   ├── menu/
│   │   ├── entity/
│   │   │   └── Menu.java
│   │   └── repository/
│   │       └── MenuRepository.java
│   ├── asset/
│   │   ├── entity/
│   │   │   └── Asset.java
│   │   └── repository/
│   │       └── AssetRepository.java
│   ├── material/
│   │   ├── entity/
│   │   │   └── MaterialApplication.java
│   │   └── repository/
│   │       └── MaterialApplicationRepository.java
│   ├── usage/
│   │   ├── entity/
│   │   │   ├── UsageApply.java
│   │   │   └── UsageLog.java
│   │   └── repository/
│   │       ├── UsageApplyRepository.java
│   │       └── UsageLogRepository.java
│   ├── workflow/
│   │   ├── entity/
│   │   │   ├── Workflow.java
│   │   │   ├── WorkflowStage.java
│   │   │   └── StageApprover.java
│   │   └── repository/
│   │       └── WorkflowRepository.java
│   └── approval/
│       ├── entity/
│       │   ├── ApprovalInstance.java
│       │   ├── ApprovalTask.java
│       │   └── ApprovalProgress.java
│       └── repository/
│           ├── ApprovalInstanceRepository.java
│           └── ApprovalTaskRepository.java
│
├── infrastructure
│   ├── user/
│   │   ├── UserMapper.java
│   │   └── UserMapper.xml
│   ├── dept/
│   │   ├── DeptMapper.java
│   │   └── DeptMapper.xml
│   ├── role/
│   │   ├── RoleMapper.java
│   │   └── RoleMapper.xml
│   ├── menu/
│   │   ├── MenuMapper.java
│   │   └── MenuMapper.xml
│   ├── asset/
│   │   ├── AssetMapper.java
│   │   └── AssetMapper.xml
│   ├── material/
│   │   ├── MaterialApplicationMapper.java
│   │   └── MaterialApplicationMapper.xml
│   ├── usage/
│   │   ├── UsageApplyMapper.java
│   │   ├── UsageApplyMapper.xml
│   │   ├── UsageLogMapper.java
│   │   └── UsageLogMapper.xml
│   ├── workflow/
│   │   ├── WorkflowMapper.java
│   │   ├── WorkflowStageMapper.java
│   │   ├── StageApproverMapper.java
│   │   └── (XML files)
│   └── approval/
│       ├── ApprovalInstanceMapper.java
│       ├── ApprovalTaskMapper.java
│       ├── ApprovalProgressMapper.java
│       └── (XML files)
│
└── client (保持现状)
    ├── dto/
    │   ├── (通用DTO，按模块添加子包)
    │   ├── auth/
    │   ├── user/
    │   ├── dept/
    │   ├── role/
    │   ├── menu/
    │   ├── asset/
    │   ├── material/
    │   ├── usage/
    │   ├── workflow/
    │   └── approval/
    └── ...
```

---

## 当前文件清单

### Adapter层 ( Controllers )

| 当前路径 | 目标模块 | 新路径 |
|----------|----------|--------|
| `adapter/web/AuthController.java` | auth | `adapter/web/auth/` |
| `adapter/web/UserController.java` | user | `adapter/web/user/` |
| `adapter/web/DeptController.java` | dept | `adapter/web/dept/` |
| `adapter/web/RoleController.java` | role | `adapter/web/role/` |
| `adapter/web/MenuController.java` | menu | `adapter/web/menu/` |
| `adapter/web/AssetController.java` | asset | `adapter/web/asset/` |
| `adapter/web/MaterialApplicationController.java` | material | `adapter/web/material/` |
| `adapter/web/UsageApplyController.java` | usage | `adapter/web/usage/` |
| `adapter/web/UsageLogController.java` | usage | `adapter/web/usage/` |
| `adapter/web/WorkflowController.java` | workflow | `adapter/web/workflow/` |
| `adapter/web/ApproverSelectionController.java` | workflow | `adapter/web/workflow/` |
| `adapter/web/ApprovalController.java` | approval | `adapter/web/approval/` |
| `adapter/web/TagController.java` | **待定** | 可能移除或合并到asset |

### App层 ( Services )

| 当前路径 | 目标模块 | 新路径 |
|----------|----------|--------|
| `app/service/AuthService.java` + impl | auth | `app/auth/` |
| `app/service/UserService.java` + impl | user | `app/user/` |
| `app/service/DeptService.java` + impl | dept | `app/dept/` |
| `app/service/RoleService.java` + impl | role | `app/role/` |
| `app/service/MenuService.java` + impl | menu | `app/menu/` |
| `app/service/AssetService.java` + impl | asset | `app/asset/` |
| `app/service/MaterialApplicationService.java` + impl | material | `app/material/` |
| `app/service/UsageApplyService.java` + impl | usage | `app/usage/` |
| `app/service/UsageLogService.java` + impl | usage | `app/usage/` |
| `app/service/WorkflowService.java` + impl | workflow | `app/workflow/` |
| `app/service/WorkflowEngineService.java` + impl | workflow | `app/workflow/` |
| `app/service/ApproverSelectionService.java` + impl | workflow | `app/workflow/` |
| `app/service/ApprovalService.java` + impl | approval | `app/approval/` |
| `app/service/TagService.java` + impl | **待定** | 可能移除或合并到asset |

### Domain层 ( Entities + Repositories )

| 当前路径 | 目标模块 | 新路径 |
|----------|----------|--------|
| `domain/user/` | user | `domain/user/` (已符合) |
| `domain/workflow/` | workflow | `domain/workflow/` (已符合) |
| `domain/usage/` | usage | `domain/usage/` (已符合) |
| `domain/material/` | material | `domain/material/` (已符合) |
| `domain/asset/` | asset | `domain/asset/` (已符合) |
| **需新增** | dept | `domain/dept/entity/Dept.java` |
| **需新增** | role | `domain/role/entity/Role.java` |
| **需新增** | menu | `domain/menu/entity/Menu.java` |
| **需新增** | approval | `domain/approval/entity/` |
| **需新增** | log | `domain/log/entity/UsageLog.java` |

### Infrastructure层 ( Mappers )

| 当前路径 | 目标模块 | 新路径 |
|----------|----------|--------|
| `infrastructure/mapper/UserMapper.java` | user | `infrastructure/user/` |
| `infrastructure/mapper/DeptMapper.java` | dept | `infrastructure/dept/` |
| `infrastructure/mapper/RoleMapper.java` | role | `infrastructure/role/` |
| `infrastructure/mapper/MenuMapper.java` | menu | `infrastructure/menu/` |
| `infrastructure/mapper/AssetMapper.java` | asset | `infrastructure/asset/` |
| `infrastructure/mapper/*MaterialApplication*` | material | `infrastructure/material/` |
| `infrastructure/mapper/*UsageApply*` | usage | `infrastructure/usage/` |
| `infrastructure/mapper/*UsageLog*` | usage | `infrastructure/usage/` |
| `infrastructure/mapper/*Workflow*` | workflow | `infrastructure/workflow/` |
| `infrastructure/mapper/*Approval*` | approval | `infrastructure/approval/` |
| `infrastructure/mapper/*Tag*` | **待定** | 可能移除 |

---

## 执行步骤

### Phase 1: 创建包结构骨架

1. 在 `adapter/web/` 下创建所有模块子包
2. 在 `app/` 下创建所有模块子包（含 `impl/` 子包）
3. 在 `domain/` 下创建所有模块子包（含 `entity/` 和 `repository/` 子包）
4. 在 `infrastructure/` 下创建所有模块子包

### Phase 2: 移动 Domain 层

1. 移动现有的 entity 和 repository
2. 创建缺失的 entity 类（Dept, Role, Menu, ApprovalInstance等）
3. 更新 repository 接口位置

### Phase 3: 移动 Infrastructure 层

1. 移动 Mapper 接口
2. 移动 Mapper XML 文件
3. 更新 MyBatis 扫描配置

### Phase 4: 移动 App 层

1. 移动 Service 接口和实现类
2. 更新所有 import 语句
3. 更新 `@Service` 注解扫描

### Phase 5: 移动 Adapter 层

1. 移动 Controller 类
2. 更新所有 import 语句
3. 更新 RequestMapping 路径（如需要）

### Phase 6: 更新 Client 层 DTO

1. 按模块创建 DTO 子包
2. 移动现有 DTO 到对应模块包
3. 更新所有 DTO 的 import

### Phase 7: 验证测试

1. 编译检查
2. 单元测试
3. 集成测试
4. API 功能测试

---

## 需要注意的事项

### 1. 跨模块依赖处理

**原则**：同层内模块间不能直接依赖

**示例**：
- ❌ `app.user` 不能 `import com.xuanjiao.app.dept.DeptService`
- ✅ `app.user` 可以调用 `domain.dept.repository.DeptRepository`

### 2. 共享代码处理

对于跨模块共享的工具类、常量等，建议：
- 创建 `common` 包存放
- 或放入 `client` 层

### 3. Tag模块处理

`TagController` 和 `TagService` 当前功能较少，建议：
- **方案A**：合并到 `asset` 模块（标签主要关联素材）
- **方案B**：保留独立 `tag` 模块（如果未来标签会关联其他实体）

### 4. 循环依赖检查

重构后需确保：
- `adapter` → `app` → `domain` ← `infrastructure`
- 无任何反向依赖
- 无同层横向依赖

---

## 预计工作量

| 阶段 | 预计时间 |
|------|----------|
| Phase 1: 创建包结构 | 0.5小时 |
| Phase 2: Domain层 | 1小时 |
| Phase 3: Infrastructure层 | 1.5小时 |
| Phase 4: App层 | 2小时 |
| Phase 5: Adapter层 | 1小时 |
| Phase 6: Client层 | 1小时 |
| Phase 7: 验证测试 | 2小时 |
| **总计** | **约9小时** |

---

## 执行前检查清单

- [ ] 确认当前代码已提交到Git
- [ ] 创建重构分支 `refactor/module-packages`
- [ ] 备份当前工作目录
- [ ] 确认所有测试用例通过

---

## 执行后验证清单

- [ ] 项目编译无错误
- [ ] 所有单元测试通过
- [ ] 所有API接口正常工作
- [ ] 无循环依赖警告
- [ ] 代码风格检查通过

---

**文档创建时间**: 2025-01-16
**文档版本**: v1.0
