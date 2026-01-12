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

## Architecture

### COLA Framework Structure (Backend)
The backend follows Alibaba's COLA (Clean Object-Oriented and Layered Architecture):

```
xuanjiao-backend/
├── xuanjiao-client/       # Client Layer - DTOs, API request/response definitions
├── xuanjiao-domain/       # Domain Layer - Entities, domain services, repository interfaces
├── xuanjiao-app/          # Application Layer - Business logic, service implementations
├── xuanjiao-infrastructure/ # Infrastructure Layer - MyBatis mappers, repository implementations
├── xuanjiao-adapter/      # Adapter Layer - REST controllers, external integrations
└── xuanjiao-start/        # Start Module - Spring Boot application entry point
```

**Key architectural principles:**
- Dependencies flow inward: `adapter` -> `app` -> `domain` (client is shared by all layers)
- `infrastructure` implements interfaces defined in `domain`
- Controllers in `adapter` handle HTTP, delegate to services in `app`
- `app` orchestrates business logic using `domain` entities and `infrastructure` repositories

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

The system uses a **"Layer-sequential + Intra-layer parallel"** architecture:
- **Between layers (串行)**: Stages execute sequentially; Stage N must complete before Stage N+1
- **Within layers (并行)**: All approvers in a stage receive tasks simultaneously
- **Layer rules**: Counter-sign (会签 - all must approve) or Or-sign (或签 - any one approves)
- **Auto-skip**: Empty stages are automatically skipped

### Database Schema (Approval Tables)
- `workflow` - Workflow definitions
- `workflow_stage` - Stages within a workflow
- `stage_approver` - Approvers assigned to stages
- `approval_instance` - Runtime instances for each asset submission
- `approval_task` - Individual tasks for each approver

## Key Vue Components (Workflow Designer)

- `WorkflowEditor.vue` - Main canvas with drag-and-drop stage management
- `StageContainer.vue` - Individual stage card with approver tags
- `ApproverSelector.vue` - Modal for selecting users/departments/roles
- `ConfigPanel.vue` - Right drawer for stage settings (name, type, approvers)

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

- Assets require approval before use (unless workflow configured otherwise)
- Only designated approvers can process approval tasks
- Used assets cannot be deleted (only marked unavailable via logical deletion)
- Duplicate files (same MD5) are not stored twice
- Three user roles: Regular users, Approvers, Administrators

## Development Notes

- The project uses MapStruct for DTO mapping between layers - look for `*Mapper.java` files with `@Mapper` annotation
- MyBatis Plus provides `BaseMapper<T>` with CRUD methods - extend it for basic queries
- JWT tokens are stored in localStorage and sent via `Authorization: Bearer <token>` header
- File uploads are handled by MultipartFile and stored locally with MD5-based filenames
- The workflow designer saves JSON structure directly to database, parsed when creating approval instances
