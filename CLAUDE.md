# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**宣传教育平台 (Propaganda/Education Platform)** - An enterprise media asset management system for managing corporate media assets (videos, images, documents), handling approval workflows, and tracking asset usage.

See `REQUIREMENTS.md` for the complete Product Requirements Document and `PROGRESS.md` for development progress.

## Common Commands

### Database Setup
```bash
# Initialize complete database (run once for new setup)
mysql -u root -p123456 < xuanjiao-backend/sql/init_all.sql
```

**Important**: The default MySQL credentials are `root` / `123456`. Update these in `xuanjiao-start/src/main/resources/application.yml` if your setup differs.

### Backend (Java Spring Boot with COLA Architecture)
```bash
cd xuanjiao-backend
# Build all modules (skips tests)
mvn clean install -DskipTests
# Run the application (runs on port 8080)
mvn spring-boot:run -pl xuanjiao-start
# Or run with specific profile
mvn spring-boot:run -pl xuanjiao-start -Dspring-boot.run.profiles=dev
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

### Database Migration Scripts

Core schema initialization:
```bash
cd xuanjiao-backend/sql
mysql -u root -p123456 < init_all.sql
```

**Workflow sub-workflow support (required):**
```bash
mysql -u root -p123456 < init_17_sub_workflow_refactor.sql
mysql -u root -p123456 < init_18_add_sub_workflow_approver_ids.sql
mysql -u root -p123456 < init_19_add_sub_workflow_approver_ids_to_instance.sql
```

**Usage application module (many-to-many refactor):**
```bash
mysql -u root -p123456 < init_21_add_usage_menu.sql
mysql -u root -p123456 < init_22_extend_asset_for_usage.sql
mysql -u root -p123456 < init_23_extend_usage_log.sql
mysql -u root -p123456 < init_24_refactor_to_intermediate_table.sql
```

**Other feature migrations:**
```bash
mysql -u root -p123456 < init_20_add_material_approval_menu.sql
mysql -u root -p123456 < init_22_add_returned_status.sql
mysql -u root -p123456 < init_23_add_task_type.sql
```

**Note**: Some migration scripts have conflicting numbers (e.g., init_21, init_22, init_23, init_24). Always check the script filename description to determine the correct order. When in doubt, check the creation date in the file or consult the team.

## Development Workflow

### Git Workflow
- Main development branch: `GLM/progress` (see git status)
- Feature branches should follow convention: `feature/<description>` or `bugfix/<description>`
- Always pull latest changes before starting work
- Commit messages should follow conventional commit format (e.g., `feat: add user login`, `fix: resolve approval bug`)

### Troubleshooting Common Issues

**Backend fails to start:**
- Check MySQL is running: `mysql -u root -p123456 -e "SELECT 1;"`
- Verify database exists: `mysql -u root -p123456 -e "SHOW DATABASES LIKE 'xuanjiao_s';"`
- Check port 8080 is not already in use
- Review logs in console output

**Frontend build errors:**
- Delete `node_modules` and run `npm install` again
- Check TypeScript errors: `npm run build` shows detailed compilation errors
- Clear Vite cache: delete `node_modules/.vite` folder

**Database migration failures:**
- Check if migration has already been applied (MySQL doesn't track by default)
- For conflicting migration numbers, check file contents to determine dependencies
- Use `mysql -u root -p123456 xuanjiao_s` to connect directly and inspect tables

**Approval workflow issues:**
- Check `WORKFLOW_REFACTOR_SUMMARY.md` for detailed workflow architecture
- Verify workflow has at least one stage configured
- Check that approvers exist in the `user` table
- Review approval progress in `approval_progress` table

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
│   ├── deletion/          # Asset deletion application module (entity, repository)
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
│   ├── deletion/          # Asset deletion application services
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
│   ├── deletion/          # Asset deletion application mappers and repository impl
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
│   ├── deletion/          # AssetDeletionController
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
├── components/       # Reusable components (AssetSelector, etc.)
├── layouts/          # Layout components (MainLayout with sidebar/header)
├── views/            # Page components (assets, workflow, approval, etc.)
└── main.ts           # Application entry point
```

### Usage Application Module (Many-to-Many Architecture)
The usage application module supports multiple assets per application and multiple applications per asset:

**Database Schema:**
- `usage_apply` - Usage application records (title, status, applicant info)
- `usage_apply_asset` - Intermediate table (many-to-many relationship)
  - Links `usage_apply_id` to `asset_id`
  - Stores per-asset configuration: usage_description, usage_publish_channel, usage_is_secondary_creation, usage_attachment_path
- `asset` table extended with usage fields (via init_22) - NOTE: Later refactored to use intermediate table instead

**Key Components:**
- `AssetSelector.vue` - Reusable asset selection component with multi-select support
- `usage-apply.vue` - Create/edit usage applications with asset configuration
- `usage-list.vue` - List view of usage applications with detail dialog

**Backend Services:**
- `UsageApplyService` / `UsageApplyServiceImpl` - Business logic for usage applications
- `UsageApplyAssetRepository` - Repository for the intermediate table
- `UsageLogService` - Records asset usage/download logs

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
- `usage-apply.vue` - Usage application creation with asset selector and multi-asset configuration
- `usage-list.vue` - Usage application list with detail view
- `approval/index.vue` - Task list, approval actions, progress display, approver selection
- `AssetSelector.vue` - Reusable asset selection component with search and multi-select

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

### AssetDeletionCleanupTask
Located at `xuanjiao-app/src/main/java/com/xuanjiao/app/schedule/AssetDeletionCleanupTask.java`
Scheduled task for asset soft deletion:
- Runs daily at 2:00 AM (`@Scheduled(cron = "0 0 2 * * ?")`)
- Finds assets with `status='DELETED'` AND `deletion_approve_time < 7 days ago` AND `deleted=0`
- Sets `deleted=1` to soft delete (MyBatis Plus @TableLogic hides these from queries)
- `cleanupDeletedAssetsManually()` - Public method for manual triggering (used by admin API)

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

### Asset Deletion Workflow
The system supports a two-stage asset deletion process:

**Stage 1: DELETED Status**
- Triggered by approval of asset deletion application (ASSET_DELETION workflow)
- Sets `status = 'DELETED'` and `deletion_approve_time = NOW()`
- Asset remains visible in list but marked as "已删除"
- Asset cannot be used for new applications or downloaded

**Stage 2: Soft Delete (deleted=1)**
- Triggered by scheduled task 7 days after deletion approval
- Sets `deleted = 1` (MyBatis Plus @TableLogic)
- Asset completely hidden from all queries (MyBatis Plus automatically filters)
- Asset is effectively removed from the system

**Scheduled Task:**
- Class: `AssetDeletionCleanupTask` in `xuanjiao-app/src/main/java/com/xuanjiao/app/schedule/`
- Schedule: Every day at 2:00 AM (`@Scheduled(cron = "0 0 2 * * ?")`)
- Logic: Find assets where `status='DELETED'` AND `deletion_approve_time < 7 days ago` AND `deleted=0`, then set `deleted=1`

**Admin Testing Features (ROLE_ID=1 only):**
- `PUT /asset/admin/{id}/adjust-delete-time` - Sets `deletion_approve_time` to 7 days ago (for testing scheduled task)
- `POST /asset/admin/trigger-cleanup` - Manually triggers the cleanup task (returns count of deleted assets)
- Frontend button "执行清理" on asset list page (top-right corner)

**Status Visibility Rules:**
| Status | Admin | Regular User | Notes |
|--------|-------|--------------|-------|
| APPROVED | ✅ | ✅ | Normal visible assets |
| PENDING | ✅ | ❌ | Pending approval |
| DELETED | ✅ | ✅ | Marked as deleted, visible but unusable |
| DRAFT | ❌ | ❌ | Never shown |
| deleted=1 | ❌ | ❌ | Completely hidden (soft deleted) |

### Workflow & Sub-Workflow Rules
- **Role-based binding**: Workflows can be bound to specific roles via `bound_role_id`
- **Workflow types**: ASSET_UPLOAD (素材录入), ASSET_USAGE (素材使用), ASSET_DELETION (素材删除)
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

### API Design Conventions

**POST-First Approach for All Endpoints:**

The project follows a POST-first approach for API design to ensure consistency and avoid RESTful ambiguity:

**Default Rule: Use POST for all operations**
- Query operations: `POST /{module}/get{Action}` (e.g., `POST /asset/getDetail`)
- Command operations: `POST /{module}/{action}` (e.g., `POST /asset/delete`)
- All request parameters should be in the request body as DTO objects
- Use `@PostMapping` with `@RequestBody @Valid` for all controller methods

**DTO Naming Convention:**
- Query DTOs: `{Action}Qry` (e.g., `AssetGetDetailQry`, `ApprovalGetMyTasksQry`)
- Command DTOs: `{Action}Cmd` (e.g., `AssetDeleteCmd`, `ApprovalApproveCmd`)
- Always use JSR-303 validation annotations (`@NotNull`, `@NotBlank`, `@Min`, etc.)

**Exceptions (When to use GET):**
Use `@GetMapping` only when POST is impractical:
- **File preview endpoints** for `<img>` tags: Browsers can only send GET requests for image sources
  - Example: `@GetMapping("/asset/preview/{id}")` - Must be GET for browser compatibility
- **File download endpoints** when triggered by direct browser URL access
  - Example: `@GetMapping("/asset/download/{id}")` - Allows direct download links
- **Public endpoints** that need to be accessible from external systems without POST capabilities

**Why POST-first?**
1. **Consistency**: All endpoints follow the same pattern, reducing cognitive load
2. **Type safety**: DTOs with validation ensure data integrity
3. **Flexibility**: Easy to add parameters without breaking existing clients
4. **Logging**: Request bodies are easier to log and audit than URL parameters
5. **Security**: POST bodies are not logged in access logs or browser history

**Example Controller Pattern:**
```java
@PostMapping("/getDetail")
public Result<AssetDTO> getDetail(@Valid @RequestBody AssetGetDetailQry qry) {
    return Result.success(assetService.getById(qry.getId()));
}

// Only use GET for browser-compatible file access
@GetMapping("/preview/{id}")
public ResponseEntity<FileSystemResource> preview(@PathVariable Long id) {
    // ... implementation
}
```

### MyBatis Plus Best Practices

**Always use LambdaQueryWrapper/LambdaUpdateWrapper instead of QueryWrapper:**

```java
// ❌ AVOID: String-based field names (error-prone)
QueryWrapper<AssetDO> wrapper = new QueryWrapper<>();
wrapper.eq("status", "DELETED")
       .lt("deletion_approve_time", oneWeekAgo);  // Typo risk!

// ✅ PREFER: Lambda expressions (type-safe)
LambdaUpdateWrapper<AssetDO> wrapper = new LambdaUpdateWrapper<>();
wrapper.eq(AssetDO::getStatus, "DELETED")
       .lt(AssetDO::getDeletionApproveTime, oneWeekAgo)
       .set(AssetDO::getDeleted, 1);
```

**Why Lambda is better:**
- Compile-time checking - typos caught at build time
- IDE auto-completion support
- Refactor-friendly (field renames automatically update)
- No confusion between database column names (underscore) vs Java field names (camelCase)

**MyBatis Plus @TableLogic Update Limitation:**
- `updateById()` method cannot directly update fields with `@TableLogic` annotation
- Use `LambdaUpdateWrapper` with `.set()` to bypass this restriction:
```java
// ❌ Does NOT work
asset.setDeleted(1);
assetMapper.updateById(asset);

// ✅ Works correctly
LambdaUpdateWrapper<AssetDO> wrapper = new LambdaUpdateWrapper<>();
wrapper.eq(AssetDO::getId, assetId)
       .set(AssetDO::getDeleted, 1);
assetMapper.update(null, wrapper);
```

**MyBatis Plus updateById() Ignores Null Values:**
- `updateById()` method, by default, **ignores null values** and will NOT update fields to null
- This is the default FieldStrategy behavior in MyBatis Plus
- Use `LambdaUpdateWrapper` with `.set()` to force update a field to null:
```java
// ❌ Does NOT work - null value is ignored, approvers keeps original value
progress.setApprovers(null);
progressMapper.updateById(progress);

// ✅ Works correctly - forces the field to null
LambdaUpdateWrapper<ApprovalProgressDO> wrapper = new LambdaUpdateWrapper<>();
wrapper.eq(ApprovalProgressDO::getId, progressId)
       .set(ApprovalProgressDO::getApprovers, null);  // Force to null
progressMapper.update(null, wrapper);
```

### Important Constraints & Gotchas

**Workflow System:**
- Sub-workflows must be configured at the **approver level**, not stage level
- First approver selection differs between OR-sign (first to approve) and AND-sign (marked at creation)
- Sub-workflows run independently but must complete for parent approval
- Missing/invalid sub-workflow configurations log warnings but don't block the flow
- **AND-sign stage completion**: Stage is complete when NO tasks have status PENDING or RETURNED (CANCELLED tasks don't block completion)
  - This handles workflow return/resubmit scenarios where old tasks are marked CANCELLED
  - Original bug: Required ALL tasks to be APPROVED, which blocked completion after returns
- **Duplicate approvers display**: When showing "other approvers" in the same stage, filter by status=PENDING and deduplicate by approver_id
  - This prevents showing the same approver multiple times after workflow returns
  - Original bug: Queried all tasks without filtering, showing old CANCELLED/APPROVED task approvers

**Usage Applications:**
- Many-to-many relationship via `usage_apply_asset` intermediate table
- One application can include multiple assets
- Per-asset configuration stored in intermediate table, not asset table
- Cannot delete applications that are in "APPROVED" status

**Asset Management:**
- Assets cannot be deleted if they have associated usage records (check `usage_apply_asset`)
- **Two-stage deletion process**: DELETED status (visible, unusable) → deleted=1 (hidden)
- MyBatis Plus `@TableLogic` on `deleted` field automatically filters out soft-deleted records
- MD5 deduplication means same file content shares storage
- DELETED status assets cannot be used in new usage applications or downloaded
- Only admins (ROLE_ID=1) can trigger manual cleanup or adjust deletion times

**Database Schema Changes:**
- Always create a new migration script with a descriptive name
- Use version numbers that don't conflict (check existing scripts first)
- Some migration numbers are duplicated (e.g., init_21, init_22, init_23) - check file descriptions to determine order
- Test migrations on a copy of production data if possible
- Document breaking changes in this CLAUDE.md file

### Adding a New Feature

**Backend (COLA layers):**
1. Create/modify entities in `xuanjiao-domain/src/main/java/com/xuanjiao/domain/{module}/entity/`
2. Create repository interface in `xuanjiao-domain/src/main/java/com/xuanjiao/domain/{module}/repository/`
3. Create mapper in `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/`
4. Implement repository in `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/`
5. Create service interface in `xuanjiao-app/src/main/java/com/xuanjiao/app/{module}/`
6. Implement service in `xuanjiao-app/src/main/java/com/xuanjiao/app/{module}/impl/`
7. Create DTOs in `xuanjiao-client/src/main/java/com/xuanjiao/client/dto/`
   - Follow naming convention: `{Action}Qry` for queries, `{Action}Cmd` for commands
   - Add JSR-303 validation annotations (`@Valid`, `@NotNull`, `@NotBlank`)
8. Create controller in `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/{module}/`
   - Use `@PostMapping` for all endpoints (except file preview/download which require GET)
   - Use `@RequestBody @Valid` for all parameters
   - Follow URL pattern: `POST /{module}/get{Action}` or `POST /{module}/{action}`

**Frontend:**
1. Add API client methods in `xuanjiao-frontend/src/api/{domain}.ts`
   - Use `request.post()` for all API calls (matching backend POST endpoints)
   - Pass parameters in request body, not as URL params
2. Create Pinia store in `xuanjiao-frontend/src/stores/{domain}.ts` (if needed)
3. Create page component in `xuanjiao-frontend/src/views/{path}/`
4. Add route in `xuanjiao-frontend/src/router/index.ts`
5. Add menu entry in backend `menu` table if needed

### Understanding Module Boundaries

The COLA architecture enforces strict layer dependencies:
- `adapter` → `app` → `domain` (dependencies flow inward)
- `client` is shared by all layers
- `infrastructure` implements `domain` interfaces
- Cross-module communication within a layer should use repositories/domain entities, not direct service calls

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
| `material` | Material Application | Asset entry applications |
| `usage` | Usage Application | Asset usage applications, usage logs, usage_apply_asset intermediate table |
| `deletion` | Asset Deletion | Asset deletion applications, two-stage deletion process (DELETED → soft delete) |
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

### Recent Database Architecture Changes (January 2025)

**Usage Application Refactoring (init_24_refactor_to_intermediate_table.sql):**
- Changed from direct `asset_id` foreign key in `usage_apply` table to a many-to-many relationship
- Created `usage_apply_asset` intermediate table to support:
  - One application can include multiple assets
  - One asset can be used by multiple applications
  - Per-asset usage configuration (description, channel, secondary creation, attachment)

**Previous Schema (Deprecated):**
- `usage_apply.asset_id` - Direct foreign key (one-to-one)
- `asset` table had usage fields (usage_description, usage_publish_channel, etc.)

**Current Schema:**
- `usage_apply` - No direct asset_id
- `usage_apply_asset` - Intermediate table with `usage_apply_id`, `asset_id`, and per-asset config fields

## Environment Configuration

### Backend Configuration
File: `xuanjiao-start/src/main/resources/application.yml`

**Key settings to verify:**
- Database connection: `spring.datasource.url` (default: `127.0.0.1:3306/xuanjiao_s`)
- Database credentials: `spring.datasource.username/password` (default: `root`/`123456`)
- File upload path: `file.upload-path` (default: `D:/xuanjiao/uploads/`)
- Server port: `server.port` (default: `8080`)
- JWT secret and expiration: Used for token generation and validation

### Frontend Configuration
File: `xuanjiao-frontend/vite.config.ts`

**Key settings:**
- Dev server port: `server.port` (default: `3000`)
- API proxy: Routes `/api` requests to `http://localhost:8080`

**Environment variables (optional):**
Create `.env` file in `xuanjiao-frontend/`:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## Working with This Codebase

### Adding a New Feature

1. **Backend (COLA layers)**:
   - Create/modify entities in `xuanjiao-domain/src/main/java/com/xuanjiao/domain/{module}/entity/`
   - Create repository interface in `xuanjiao-domain/src/main/java/com/xuanjiao/domain/{module}/repository/`
   - Create mapper in `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/`
   - Implement repository in `xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/`
   - Create service interface in `xuanjiao-app/src/main/java/com/xuanjiao/app/{module}/`
   - Implement service in `xuanjiao-app/src/main/java/com/xuanjiao/app/{module}/impl/`
   - Create DTOs in `xuanjiao-client/src/main/java/com/xuanjiao/client/dto/`
   - Create controller in `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/{module}/`

2. **Frontend**:
   - Add API client methods in `xuanjiao-frontend/src/api/{domain}.ts`
   - Create Pinia store in `xuanjiao-frontend/src/stores/{domain}.ts` (if needed)
   - Create page component in `xuanjiao-frontend/src/views/{path}/`
   - Add route in `xuanjiao-frontend/src/router/index.ts`
   - Add menu entry in backend `menu` table if needed

### Debugging Approval Workflows

When troubleshooting workflow issues:
1. Check the workflow definition in `workflow`, `workflow_stage`, and `stage_approver` tables
2. Inspect the approval instance in `approval_instance` table
3. View active tasks in `approval_task` table
4. Review progress tracking in `approval_progress` table
5. Check logs for workflow engine operations
6. Refer to `WORKFLOW_REFACTOR_SUMMARY.md` for detailed architecture

### Understanding Module Boundaries

The COLA architecture enforces strict layer dependencies:
- `adapter` → `app` → `domain` (dependencies flow inward)
- `client` is shared by all layers
- `infrastructure` implements `domain` interfaces
- Cross-module communication within a layer should use repositories/domain entities, not direct service calls