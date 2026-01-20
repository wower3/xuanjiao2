# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**宣传教育平台 (Propaganda/Education Platform)** - An enterprise media asset management system for managing corporate media assets (videos, images, documents), handling approval workflows, and tracking asset usage.

See `REQUIREMENTS.md` for the complete Product Requirements Document and `PROGRESS.md` for development progress.

## Common Commands

### Database Setup
```bash
mysql -u root -p123456 < xuanjiao-backend/sql/init_all.sql
```

### Backend (Java Spring Boot with COLA Architecture)
```bash
cd xuanjiao-backend
# Build all modules
mvn clean install
# Run the application (runs on port 8080)
mvn spring-boot:run -pl xuanjiao-start
```

### Frontend (Vue 3 + TypeScript + Vite)
```bash
cd xuanjiao-frontend
npm install
npm run dev      # Development server on port 3000
npm run build    # Production build (TypeScript compilation + Vite build)
npm run preview  # Preview production build
```

### Access URLs
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- Swagger/Knife4j Documentation: http://localhost:8080/api/doc.html
- Default Login: admin / 123456

### Database Migration
Run SQL migrations in order:
```bash
cd xuanjiao-backend/sql
mysql -u root -p123456 < init_17_workflow_refactor.sql
mysql -u root -p123456 < init_18_add_sub_workflow_approver_ids.sql
mysql -u root -p123456 < init_19_add_sub_workflow_approver_ids_to_instance.sql
```

## Architecture

### COLA Framework Structure (Backend)
The backend follows Alibaba's COLA (Clean Object-Oriented and Layered Architecture) with **module-based packaging** within each layer:

```
xuanjiao-backend/
├── xuanjiao-client/       # Client Layer - DTOs, API request/response definitions
├── xuanjiao-domain/       # Domain Layer - Entities, domain services, repository interfaces
│   ├── auth/              # Authentication module entities
│   ├── user/              # User module (entity, repository)
│   ├── dept/              # Department module (entity, repository)
│   ├── role/              # Role module (entity, repository)
│   ├── menu/              # Menu module (entity, repository)
│   ├── asset/             # Asset module (entity, repository)
│   ├── material/          # Material application module (entity, repository)
│   ├── usage/             # Usage application module (entity, repository)
│   ├── workflow/          # Workflow definition module (entity, repository)
│   ├── approval/          # Approval execution module (entity, repository)
│   └── log/               # Log module (entity, repository)
├── xuanjiao-app/          # Application Layer - Business logic, service implementations
│   ├── auth/              # Authentication services
│   ├── user/              # User services
│   ├── dept/              # Department services
│   ├── role/              # Role services
│   ├── menu/              # Menu services
│   ├── asset/             # Asset services
│   ├── material/          # Material application services
│   ├── usage/             # Usage application services
│   ├── workflow/          # Workflow services (WorkflowService, WorkflowEngineService, ApproverSelectionService)
│   └── approval/          # Approval services
├── xuanjiao-infrastructure/ # Infrastructure Layer - MyBatis mappers, repository implementations
│   ├── user/              # User mappers and repository impl
│   ├── dept/              # Department mappers
│   ├── role/              # Role mappers
│   ├── menu/              # Menu mappers
│   ├── asset/             # Asset mappers and repository impl
│   ├── material/          # Material application mappers and repository impl
│   ├── usage/             # Usage application mappers and repository impl
│   ├── workflow/          # Workflow mappers
│   ├── approval/          # Approval mappers
│   ├── dataobject/        # DO classes (database-mapped entities)
│   └── config/            # Infrastructure config (MyBatisPlusConfig)
├── xuanjiao-adapter/      # Adapter Layer - REST controllers, external integrations
│   ├── auth/              # AuthController
│   ├── user/              # UserController
│   ├── dept/              # DeptController
│   ├── role/              # RoleController
│   ├── menu/              # MenuController
│   ├── asset/             # AssetController, TagController
│   ├── material/          # MaterialApplicationController
│   ├── usage/             # UsageApplyController, UsageLogController
│   ├── workflow/          # WorkflowController, ApproverSelectionController
│   └── approval/          # ApprovalController
└── xuanjiao-start/        # Start Module - Spring Boot application entry point
```

**Key architectural principles:**
- **COLA layers**: Dependencies flow inward: `adapter` -> `app` -> `domain` (client is shared by all layers)
- **Module-based packaging**: Within each layer, code is organized by business module (auth, user, dept, role, menu, asset, material, usage, workflow, approval, log)
- **Module isolation**: Modules within the same layer should not directly depend on each other
- `infrastructure` implements interfaces defined in `domain`
- Controllers in `adapter` handle HTTP, delegate to services in `app`
- `app` orchestrates business logic using `domain` entities and `infrastructure` repositories

**Cross-module interaction**: When modules need to interact, use the domain layer (repositories) rather than direct service-to-service calls within the same layer.

### Frontend Architecture
```
xuanjiao-frontend/src/
├── api/              # Axios-based API client (one file per domain)
├── router/           # Vue Router configuration with auth guards
├── stores/           # Pinia state stores (user, asset, workflow, etc.)
├── layouts/          # Layout components (MainLayout with sidebar/header)
├── views/            # Page components (assets, workflow, approval, etc.)
└── main.ts           # Application entry point
```

## Technology Stack

### Backend
- Java 8, Spring Boot 2.7.18
- COLA 4.3.2 (Clean Architecture framework)
- MyBatis Plus 3.5.3.1 (ORM with BaseMapper)
- MySQL 8.0
- JWT authentication
- Knife4j 4.1.0 (Swagger UI)
- MapStruct 1.5.5 (DTO mapping)

### Frontend
- Vue 3.4 with Composition API and TypeScript
- Vite 5.0 for dev server and builds
- Element Plus 2.4.4 (UI components)
- Pinia for state management
- Vue Router 4
- vuedraggable for drag-and-drop workflow designer

## Approval Workflow Model

The system uses a **"Layer-sequential + Intra-layer parallel"** architecture with **hierarchical sub-workflows**:
- **Between layers (串行)**: Stages execute sequentially; Stage N must complete before Stage N+1
- **Within layers (并行)**: All approvers in a stage receive tasks simultaneously
- **Layer rules**: Counter-sign (会签 - all must approve) or Or-sign (或签 - any one approves)
- **Sub-workflows**: Independent approval processes triggered at the approver level, running in parallel without blocking the main flow

### Sub-Workflow Architecture

Sub-workflows are configured at the **approver level** (not stage level):
- **Configuration**: Each `stage_approver` can have a `sub_workflow_id` linking to another workflow
- **Trigger**: Manually triggered by the first approver when completing their task
- **Execution**: Independent instances that run parallel to main flow (non-blocking)
- **Completion**: Both main flow AND all sub-workflows must complete for final approval
- **First Approver**: In OR-sign, first to approve; in AND-sign, marked at task creation
- **Approver Selection**: Upper-level approver selects sub-workflow's first-stage approvers

### Database Schema (Approval Tables)
- `workflow` - Workflow definitions (includes `bound_role_id`, `workflow_type`)
- `workflow_stage` - Stages within a workflow (includes `sub_workflow_id`)
- `stage_approver` - Approvers assigned to stages (includes `sub_workflow_id`, `check_secondary_dept`)
- `approval_instance` - Runtime instances (includes parent/child relationships, `sub_workflow_approver_ids`)
- `approval_task` - Individual tasks (includes `is_first_approver`, `next_stage_approver_ids`, `sub_workflow_approver_ids`)
- `approval_progress` - Approval progress tracking with sub-workflow support

### Instance Status Flow
```
PENDING → MAIN_COMPLETED (waiting for sub-workflows) → APPROVED
         ↳ REJECTED (any rejection)
```

## Key Vue Components (Workflow Designer)

- `WorkflowEditor.vue` - Main canvas with drag-and-drop stage management
- `StageContainer.vue` - Individual stage card with approver tags
- `ApproverSelector.vue` - Modal for selecting users/departments/roles
- `ConfigPanel.vue` - Right drawer for stage settings (name, type, approvers)

## Key Vue Components (Approval Flow)

- `material-entry.vue` - Asset submission with main/sub-workflow approver selection
- `approval/index.vue` - Task list, approval actions, progress display, approver selection

## Key Services (Backend)

### ApproverSelectionService
Located at `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/ApproverSelectionService.java`
Handles all approver selection logic:
- `getFirstStageApprovers()` - Get first stage approvers for a workflow
- `getNextStageApprovers()` - Get next stage approvers (with search)
- `getSubWorkflowFirstStageApprovers()` - Get sub-workflow first stage approvers
- `selectFirstStageApproversWithSubWorkflows()` - Select first stage with sub-workflows
- `selectNextStageApproversWithSubWorkflows()` - Select next stage with sub-workflows
- `getApprovalProgress()` - Get approval progress including sub-workflows
- `getTaskDetail()` - Get task details with approver selection info

### WorkflowEngineService
Located at `xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/WorkflowEngineService.java`
Core workflow orchestration:
- `createInstance()` - Create approval instance
- `completeTask()` - Complete task (approve/reject)
- `selectFirstStageApprovers()` - Initialize first stage approvers
- `checkAndMoveToNextStage()` - Check stage completion and advance
- `startSubProcessesForStage()` - Trigger sub-workflows for a stage
- `areAllSubWorkflowsComplete()` - Check if all sub-workflows finished
- `checkParentCompletion()` - Check parent completion after sub-workflow ends

## Configuration Files

### Backend Configuration
`xuanjiao-start/src/main/resources/application.yml`
- Server: port 8080, context path `/api`
- Database: MySQL at 127.0.0.1:3306/xuanjiao_s
- File upload: D:/xuanjiao/uploads/, max 100MB
- JWT secret and expiration settings

### Frontend Configuration
`vite.config.ts`
- Dev server port 3000
- Proxy: `/api` -> `http://localhost:8080`
- Plugin: `@vitejs/plugin-vue`

## Business Rules

### Asset Management
- Assets require approval before use (unless workflow configured otherwise)
- Only designated approvers can process approval tasks
- Used assets cannot be deleted (only marked unavailable via logical deletion)
- Duplicate files (same MD5) are not stored twice
- Three user roles: Regular users, Approvers, Administrators

### Workflow & Sub-Workflow Rules
- **Role-based binding**: Workflows can be bound to specific roles via `bound_role_id`
- **Workflow types**: ASSET_UPLOAD (素材录入), ASSET_USAGE (素材使用)
- **First approver selection**: Only first approver selects next-stage and sub-workflow approvers
- **OR-sign**: First to approve becomes first approver; other tasks auto-cancel
- **AND-sign**: Task marked `is_first_approver=1` at creation selects approvers
- **Sub-workflow triggering**: Happens when stage completes, uses pre-selected approvers
- **Completion condition**: Main flow + ALL sub-workflows must be APPROVED or REJECTED
- **Parent-child instances**: Sub-workflows have `parent_instance_id` and `parent_task_id`
- **Error resilience**: Missing/invalid sub-workflows log warnings and are skipped (don't block flow)

## Development Notes

- The project uses MapStruct for DTO mapping between layers - look for `*Mapper.java` files with `@Mapper` annotation
- MyBatis Plus provides `BaseMapper<T>` with CRUD methods - extend it for basic queries
- JWT tokens are stored in localStorage and sent via `Authorization: Bearer <token>` header
- File uploads are handled by MultipartFile and stored locally with MD5-based filenames
- The workflow designer saves JSON structure directly to database, parsed when creating approval instances
- All workflow operations use `@Transactional(rollbackFor = Exception.class)` for data consistency
- Sub-workflow approver IDs are stored as JSON in `sub_workflow_approver_ids` fields
- See `WORKFLOW_REFACTOR_SUMMARY.md` for comprehensive workflow system documentation

### Module-Based Package Structure (Post-Refactoring)

As of January 2025, the backend has been refactored to use module-based packaging within each COLA layer. The following business modules are defined:

| Module Code | Module Name | Description |
|-------------|-------------|-------------|
| `auth` | Authentication | Login, logout, token management |
| `user` | User | User CRUD, user permission queries |
| `dept` | Department | Department tree, department CRUD |
| `role` | Role | Role CRUD, role permissions |
| `menu` | Menu | Menu tree, menu configuration |
| `asset` | Asset | Asset upload, download, management, tags |
| `material` | Material Application | Asset entry applications (including future delete function) |
| `usage` | Usage Application | Asset usage applications, usage logs |
| `workflow` | Workflow Definition | Workflow design, template management, workflow engine, approver selection |
| `approval` | Approval Execution | Approval instances, task processing |
| `log` | Log | Operation log recording |

**File location patterns:**
- Controllers: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/{module}/`
- Service interfaces: `xuanjiao-app/src/main/java/com/xuanjiao/app/{module}/`
- Service implementations: `xuanjiao-app/src/main/java/com/xuanjiao/app/{module}/impl/`
- Domain entities: `xuanjiao-domain/src/main/java/com/xuanjiao/domain/{module}/entity/`
- Repository interfaces: `xuanjiao-domain/src/main/java/com/xuanjiao/domain/{module}/repository/`
- Mappers: `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/`
- Repository implementations: `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/`

**Important**: When adding new features, follow the module-based structure. Place code in the appropriate module subdirectory within each layer.
