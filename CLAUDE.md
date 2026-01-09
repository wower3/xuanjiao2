# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**宣传教育平台 (Propaganda/Education Platform)** - An enterprise media asset management system for managing corporate media assets (videos, images, documents), handling approval workflows, and tracking asset usage.

This is a greenfield project currently in the requirements/planning phase. See `REQUIREMENTS.md` for the complete Product Requirements Document.

## Planned Architecture

### Technology Stack (Recommended)
- **Frontend**: Vue.js with component-based architecture
- **Backend**: To be determined (Python recommended given project location)
- **Database**: Relational database for persistent storage

### Core Modules
1. **Asset Management** - Upload, preview, search, delete media files (video/image/document)
2. **Approval Workflow Engine** - Configurable multi-stage approval system
3. **User Management** - Three roles: Regular users, Approvers, Administrators
4. **Usage Logging** - Track all asset operations

### Approval Workflow Model
The system uses a **"Layer-sequential + Intra-layer parallel"** architecture:
- **Between layers (串行)**: Stages execute sequentially; Stage N must complete before Stage N+1
- **Within layers (并行)**: All approvers in a stage receive tasks simultaneously
- **Layer rules**: Counter-sign (会签 - all must approve) or Or-sign (或签 - any one approves)
- **Auto-skip**: Empty stages are automatically skipped

### Key Vue Components (Workflow Designer)
- `WorkflowEditor.vue` - Main canvas managing stages array
- `StageContainer.vue` - Stage card component with approver tags
- `ApproverSelector.vue` - Personnel selection modal
- `ConfigPanel.vue` - Right drawer for stage configuration

### Core Data Entities
- Users, Departments, Roles
- Assets (with MD5 deduplication)
- Approval Workflows, Instances, Tasks
- Usage Logs

## Business Constraints
- Assets require approval before use (unless workflow configured otherwise)
- Only designated approvers can process approval tasks
- Used assets cannot be deleted (only marked unavailable)
- Duplicate files (same MD5) are not stored twice

## Performance Requirements
- Page load: < 2 seconds
- Concurrent users: 100
- Max file upload: 100MB
- Asset capacity: 100,000+ items
