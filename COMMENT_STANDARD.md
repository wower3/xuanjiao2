# 项目注释规范方案

## 项目注释补充方案（完整版）

> 目标：满足SonarLint要求，为所有Java/TypeScript/Vue文件添加完整注释

---

## 📊 当前现状总览

| 层级 | 模块 | 文件数 | 类注释 | 字段注释 | 方法注释 | 行内注释 |
|------|------|--------|--------|----------|----------|----------|
| **后端** | Domain实体 | ~15 | ❌ 几乎无 | ❌ 几乎无 | - | - |
| **后端** | Infrastructure DO | ~20 | ❌ 几乎无 | ❌ 几乎无 | - | - |
| **后端** | Service接口 | ~30 | ✅ 较好 | - | ⚠️ 部分缺失 | - |
| **后端** | Service实现 | ~30 | ⚠️ 部分 | - | ⚠️ 部分 | ⚠️ 部分 |
| **后端** | Controller | ~20 | ✅ Swagger | - | ✅ Swagger | ⚠️ 极少 |
| **后端** | Mapper | ~40 | ⚠️ 极少 | - | ⚠️ 极少 | ⚠️ 极少 |
| **前端** | Vue组件 | ~50 | ❌ 极少 | - | ⚠️ ️ 极少 |
极少 | ⚠| **前端** | API文件 | ~20 | ❌ 极少 | - | ⚠️ 极少 | - |
| **前端** | Stores | ~10 | ❌ 极少 | - | ⚠️ 极少 | ⚠️ 极少 |
| **前端** | Utils/Helpers | ~15 | ❌ 极少 | - | ⚠️ 极少 | ⚠️ 极少 |

---

## 🎯 目标标准（SonarLint要求）

### 后端（Java）

| 类型 | 要求 | 示例 |
|------|------|------|
| **类注释** | 每个public类必须有JavaDoc，说明职责 | `/** 素材服务实现类，提供素材的CRUD、上传下载等功能 */` |
| **字段注释** | 所有字段必须有`/** */`注释 | `/** 素材状态：PENDING-待审批、APPROVED-已通过 */` |
| **方法注释** | 所有public/protected方法必须有JavaDoc | `/** 新增素材，上传文件并保存记录 */` |
| **参数注释** | @param标注所有参数 | `@param file 上传的文件对象` |
| **返回值注释** | @return标注返回值（非void方法） | `@return 生成的素材ID` |

### 前端（Vue/TypeScript）

| 类型 | 要求 | 示例 |
|------|------|------|
| **文件注释** | 每个.ts/.vue文件开头必须有块注释 | `/** 素材管理API接口，提供素材的增删改查等功能 */` |
| **组件注释** | Vue组件的props、emits、expose需注释 | `/** 素材列表属性 */` |
| **函数注释** | 所有导出函数必须有JSDoc | `/** 获取素材分页列表 */` |
| **类型注释** | TypeScript interface/type需注释 | `/** 素材查询参数 */` |
| **复杂逻辑** | 超过5行的逻辑块需行内注释 | `// 当前页为1时，重置到第一页` |

---

## 📝 注释规范模板

### 1. 后端 - Domain实体类

```java
/**
 * 素材实体
 * <p>代表系统中的媒体资产文件，包括视频、图片、文档等</p>
 * <p>具有状态流转特性：DRAFT → PENDING → APPROVED/REJECTED → DELET * <p>ED</p>
主要业务流程：素材录入申请 → 审批 → 素材使用/删除申请 → 审批</p>
 *
 * @author system
 * @version 1.0
 * @see AssetDO
 */
@Data
public class Asset {
    /** 素材唯一标识，自增主键 */
    private Long id;

    /** 素材名称，用户上传时指定，最大100字符 */
    private String name;

    /** 素材类型：IMAGE-图片、VIDEO-视频、DOCUMENT-文档 */
    private String type;

    /** 素材状态：DRAFT-草稿、PENDING-待审批、APPROVED-已通过、REJECTED-已拒绝、DELETED-已删除 */
    private String status;
}
```

### 2. 后端 - Service接口

```java
/**
 * 素材服务接口
 * <p>提供素材的查询、上传、下载、标签管理等功能</p>
 * <p>素材操作涉及审批流程：录入需审批、删除需审批</p>
 * <p>素材状态变更：PENDING → APPROVED/REJECTED</p>
 *
 * @author system
 * @version 1.0
 * @see AssetServiceImpl
 */
public interface AssetService {

    /**
     * 分页查询素材列表
     * <p>支持按名称、类型、状态、标签等条件筛选</p>
     * <p>只返回未软删除的素材（deleted=0）</p>
     *
     * @param query 查询参数，包含分页和筛选条件
     * @return 分页结果，包含素材基本信息
     */
    PageResult<AssetDTO> listByPage(AssetQuery query);

    /**
     * 获取素材详情
     * <p>包含素材基本信息、标签信息、审批信息</p>
     *
     * @param id 素材ID
     * @return 素材详情DTO，不存在返回null
     * @throws BusinessException 素材不存在或已删除
     */
    AssetDTO getDetail(Long id);
}
```

### 3. 后端 - Service实现类

```java
/**
 * 素材服务实现类
 * <p>实现AssetService接口，封装素材业务逻辑</p>
 * <p>核心功能：文件上传（MD5去重）、分页查询、标签管理</p>
 *
 * @author system
 * @version 1.0
 */
@Service
@Slf4j
public class AssetServiceImpl implements AssetService {

    /**
     * 分页查询素材列表
     * <p>实现逻辑：</p>
     * <ol>
     *   <li>构建动态SQL查询条件</li>
     *   <li>执行分页查询</li>
     *   <li>转换DO为DTO</li>
     *   <li>补充标签信息</li>
     * </ol>
     *
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<AssetDTO> listByPage(AssetQuery query) {
        // 构建查询条件
        AssetQueryWrapper wrapper = new AssetQueryWrapper();
        wrapper.setNameLike(query.getName());
        wrapper.setTypeEqual(query.getType());
        wrapper.setStatusEqual(query.getStatus());
        wrapper.setDeletedIsNull(); // 只查询未删除的

        // 执行分页查询
        Page<AssetDO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<AssetDO> resultPage = assetMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<AssetDTO> dtoList = resultPage.getRecords().stream()
            .map(assetDO -> assetMapper.toDTO(assetDO))
            .collect(Collectors.toList());

        return new PageResult<>(dtoList, resultPage.getTotal());
    }
}
```

### 4. 后端 - Controller

```java
/**
 * 素材管理控制器
 * <p>提供素材相关的RESTful API接口</p>
 * <p>所有接口均需登录认证，通过JWT Token验证用户身份</p>
 *
 * @author system
 * @version 1.0
 */
@RestController
@RequestMapping("/asset")
@RequiredArgsConstructor
@Tag(name = "素材管理", description = "素材的上传、下载、查询等接口")
public class AssetController {

    /**
     * 分页查询素材列表
     *
     * @param qry 查询参数
     * @return 分页结果
     */
    @PostMapping("/listByPage")
    @Operation(summary = "分页查询素材列表")
    public Result<PageResult<AssetDTO>> listByPage(@Valid @RequestBody AssetListByPageQry qry) {
        return Result.success(assetService.listByPage(qry));
    }
}
```

### 5. 后端 - Infrastructure DO

```java
/**
 * 素材数据对象
 * <p>对应数据库表 asset，存储素材的持久化数据</p>
 * <p>设计说明：使用软删除策略，通过deleted字段控制</p>
 *
 * @author system
 * @version 1.0
 * @see Asset
 */
@Data
@TableName("asset")
public class AssetDO {
    /** 主键，自增策略 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 素材名称 */
    private String name;

    /** 素材类型：IMAGE、VIDEO、DOCUMENT */
    private String type;

    /** 素材状态：DRAFT、PENDING、APPROVED、REJECTED、DELETED */
    private String status;

    /** 文件相对路径，相对于配置的上传目录 */
    private String filePath;

    /** 文件MD5值，用于去重和完整性校验 */
    private String fileMd5;

    /** 文件大小，单位字节 */
    private Long fileSize;

    /** 业务状态：0-草稿、1-待审批、2-已通过、3-已拒绝、4-已删除 */
    private Integer statusCd;

    /** 逻辑删除标识：0-未删除、1-已删除，查询时自动过滤 */
    @TableLogic
    private Integer deleted;
}
```

### 6. 后端 - Mapper接口

```java
/**
 * 素材Mapper接口
 * <p>定义素材的数据访问方法，对应SQL操作</p>
 * <p>所有方法在XML中实现，使用动态SQL</p>
 *
 * @author system
 * @version 1.0
 * @see AssetMapper.xml
 */
@Mapper
public interface AssetMapper {

    /**
     * 根据ID查询素材
     *
     * @param id 素材ID
     * @return 素材DO，不存在返回null
     */
    AssetDO selectById(@Param("id") Long id);

    /**
     * 分页查询素材列表
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AssetDO> selectPage(Page<Page> page, @Param("query") AssetQuery query);
}
```

---

### 7. 前端 - API文件

```typescript
/**
 * 素材管理API接口
 * <p>提供素材的增删改查等功能</p>
 * <p>所有接口均通过POST方法调用</p>
 *
 * @module assetApi
 */

/**
 * 素材查询参数类型
 */
export interface AssetQuery {
  pageNum: number;      // 页码，从1开始
  pageSize: number;     // 每页数量
  name?: string;        // 素材名称（模糊查询）
  type?: string;        // 素材类型：IMAGE、VIDEO、DOCUMENT
  status?: string;      // 素材状态
  tagIds?: number[];    // 标签ID数组
}

/**
 * 素材详情DTO类型
 */
export interface AssetDTO {
  id: number;           // 素材ID
  name: string;        // 素材名称
  type: string;        // 素材类型
  status: string;       // 素材状态
  filePath: string;    // 文件路径
  fileSize: number;    // 文件大小（字节）
  fileMd5: string;     // 文件MD5
  createTime: string;  // 创建时间
  tags: TagDTO[];      // 标签列表
}

/**
 * 分页查询素材列表
 * <p>支持多条件筛选，返回分页结果</p>
 *
 * @param query - 查询参数
 * @returns Promise<PageResult<AssetDTO>> 分页结果
 */
export function getAssetList(query: AssetQuery): Promise<Result<PageResult<AssetDTO>>> {
  return request.post('/asset/listByPage', query);
}

/**
 * 获取素材详情
 *
 * @param id - 素材ID
 * @returns Promise<AssetDTO> 素材详情
 */
export function getAssetDetail(id: number): Promise<Result<AssetDTO>> {
  return request.post('/asset/getDetail', { id });
}
```

### 8. 前端 - Vue组件

```vue
<template>
  <!-- 模板区域 -->
</template>

<script setup lang="ts">
/**
 * 素材列表组件
 * <p>展示素材列表，支持搜索、筛选、分页</p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>素材列表展示（卡片/表格两种视图）</li>
 *   <li>条件筛选（名称、类型、状态、标签）</li>
 *   <li>分页查询</li>
 *   <li>素材操作（预览、下载、编辑、删除）</li>
 * </ul>
 *
 * @component AssetList
 * @version 1.0
 */
import { ref, reactive, onMounted, watch } from 'vue';
import { ElMessage } from 'element-plus';

// Props 定义（如果使用 props）
interface Props {
  /** 是否显示操作列，默认为true */
  showOperation?: boolean;
  /** 是否支持多选，默认为true */
  selectable?: boolean;
}

// emits 定义
const emit = defineEmits<{
  /** 选择变化事件 */
  (e: 'selectionChange', selection: AssetDTO[]): void;
  /** 行点击事件 */
  (e: 'rowClick', row: AssetDTO): void;
}>();

// 组件状态
const loading = ref(false);           // 加载状态
const assetList = ref<AssetDTO[]>([]); // 素材列表
const total = ref(0);                 // 总数量

/**
 * 加载素材列表
 * <p>实现逻辑：</p>
 * <ol>
 *   <li>设置加载状态</li>
 *   <li>调用API获取数据</li>
 *   <li>更新列表数据</li>
 *   <li>捕获异常并提示</li>
 * </ol>
 */
const loadAssetList = async () => {
  loading.value = true;
  try {
    const res = await getAssetList(queryParams);
    assetList.value = res.data.list;
    total.value = res.data.total;
  } catch (error) {
    ElMessage.error('加载素材列表失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 处理搜索
 * <p>搜索参数变化时触发，重置到第一页</p>
 */
const handleSearch = () => {
  queryParams.pageNum = 1;
  loadAssetList();
};
</script>

<style scoped>
/* 样式区域 */
</style>
```

### 9. 前端 - Store文件

```typescript
/**
 * 素材状态管理
 * <p>管理素材相关的全局状态</p>
 * <p>主要状态：</p>
 * <ul>
 *   <li>素材列表数据</li>
 *   <li>当前选中素材</li>
 *   <li>筛选参数</li>
 * </ul>
 *
 * @module assetStore
 */
import { defineStore } from 'pinia';

interface AssetState {
  /** 当前选中素材 */
  currentAsset: AssetDTO | null;
  /** 选中素材列表（多选） */
  selectedAssets: AssetDTO[];
  /** 是否显示预览弹窗 */
  previewVisible: boolean;
  /** 预览素材 */
  previewAsset: AssetDTO | null;
}

export const useAssetStore = defineStore('asset', {
  /**
   * 状态定义
   */
  state: (): AssetState => ({
    currentAsset: null,
    selectedAssets: [],
    previewVisible: false,
    previewAsset: null,
  }),

  /**
   * 计算属性
   */
  getters: {
    /** 是否已选择素材 */
    hasSelection: (state) => state.selectedAssets.length > 0,
    /** 选择数量 */
    selectionCount: (state) => state.selectedAssets.length,
  },

  /**
   * actions
   */
  actions: {
    /**
     * 设置当前素材
     * @param asset - 素材对象
     */
    setCurrentAsset(asset: AssetDTO | null) {
      this.currentAsset = asset;
    },

    /**
     * 添加选中素材
     * @param asset - 素材对象
     */
    addSelection(asset: AssetDTO) {
      if (!this.selectedAssets.find(a => a.id === asset.id)) {
        this.selectedAssets.push(asset);
      }
    },
  },
});
```

### 10. 前端 - 类型定义文件

```typescript
/**
 * 审批相关类型定义
 * <p>定义审批流程中使用的核心数据类型</p>
 *
 * @module approvalTypes
 */

/**
 * 审批任务类型枚举
 */
export enum ApprovalTaskType {
  /** 普通审批任务 */
  NORMAL = 'NORMAL',
  /** 重新发起子流程任务 */
  RESTART_SUB_WORKFLOW = 'RESTART_SUB_WORKFLOW',
}

/**
 * 业务类型枚举
 */
export enum BusinessType {
  /** 素材录入 */
  MATERIAL_ENTRY = 'MATERIAL_ENTRY',
  /** 素材使用 */
  ASSET_USAGE = 'ASSET_USAGE',
  /** 素材删除 */
  ASSET_DELETION = 'ASSET_DELETION',
}

/**
 * 审批任务查询参数
 */
export interface ApprovalTaskQuery {
  userId: number;           // 用户ID（必填）
  pageNum: number;          // 页码
  pageSize: number;         // 每页数量
  businessType?: BusinessType; // 业务类型筛选
}

/**
 * 审批进度项类型
 */
export interface ApprovalProgressItem {
  id: number;              // 进度ID
  stageName: string;       // 阶段名称
  status: string;          // 状态：PENDING/APPROVED/REJECTED
  approverName: string;   // 审批人名称
  approveTime: string;     // 审批时间
  comment?: string;        // 审批意见
}
```

---

## 📋 实施步骤

### 阶段一：后端Domain实体（优先级 P0）

**文件列表：**
```
xuanjiao-domain/src/main/java/com/xuanjiao/domain/
├── asset/entity/Asset.java              ⭐ 核心实体
├── user/entity/User.java
├── user/entity/UserRole.java
├── dept/entity/Dept.java
├── role/entity/Role.java
├── role/entity/RoleMenu.java
├── menu/entity/Menu.java
├── material/entity/MaterialApplication.java  ⭐
├── material/entity/MaterialApplicationDetail.java
├── usage/entity/UsageApply.java         ⭐
├── usage/entity/UsageApplyAsset.java
├── deletion/entity/AssetDeletionApplication.java
├── notification/entity/Notification.java
├── workflow/entity/Workflow.java
├── workflow/entity/WorkflowStage.java
├── workflow/entity/StageApprover.java
├── approval/entity/ApprovalInstance.java ⭐
├── approval/entity/ApprovalTask.java
├── approval/entity/ApprovalProgress.java
└── log/entity/OperationLog.java
```

### 阶段二：后端Infrastructure DO（优先级 P0）

**文件列表：**
```
xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/dataobject/
├── AssetDO.java                         ⭐ 核心DO
├── UserDO.java
├── WorkflowDO.java
├── WorkflowStageDO.java
├── StageApproverDO.java
├── ApprovalInstanceDO.java              ⭐
├── ApprovalTaskDO.java
├── ApprovalProgressDO.java
├── UsageApplyDO.java
├── UsageApplyAssetDO.java
├── MaterialApplicationDO.java
├── MaterialApplicationDetailDO.java
├── AssetDeletionApplicationDO.java
├── NotificationDO.java
└── OperationLogDO.java
```

### 阶段三：后端Service接口（优先级 P1）

**文件列表：**
```
xuanjiao-app/src/main/java/com/xuanjiao/app/
├── asset/AssetService.java
├── usage/UsageApplyService.java
├── material/MaterialApplicationService.java
├── deletion/AssetDeletionApplicationService.java
├── notification/NotificationService.java
├── workflow/WorkflowService.java
├── workflow/WorkflowEngineService.java
├── workflow/ApproverSelectionService.java
├── approval/ApprovalService.java
└── log/OperationLogService.java
```

### 阶段四：后端Mapper接口（优先级 P1）

**文件列表：**
```
xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/asset/AssetMapper.java
xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/workflow/WorkflowMapper.java
xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/approval/ApprovalMapper.java
// ... 其他Mapper
```

### 阶段五：后端Service实现类（优先级 P2）

**文件列表：**
```
xuanjiao-app/src/main/java/com/xuanjiao/app/asset/impl/AssetServiceImpl.java
xuanjiao-app/src/main/java/com/xuanjiao/app/workflow/impl/WorkflowEngineServiceImpl.java
xuanjiao-app/src/main/java/com/xuanjiao/app/approval/impl/ApprovalServiceImpl.java
xuanjiao-app/src/main/java/com/xuanjiao/app/approval/impl/ApproverSelectionServiceImpl.java
// ... 其他ServiceImpl
```

### 阶段六：前端组件（优先级 P2）

**核心组件列表：**
```
xuanjiao-frontend/src/views/
├── asset/
│   ├── asset-list.vue                  ⭐ 核心组件
│   ├── asset-upload.vue
│   ├── asset-detail.vue
│   └── asset-selector.vue
├── workflow/
│   ├── workflow-editor.vue             ⭐
│   ├── stage-container.vue
│   └── approver-selector.vue
├── approval/
│   ├── pending-approval.vue            ⭐
│   ├── approval-progress.vue
│   └── notification-detail.vue
└── material/
    ├── material-entry.vue              ⭐
    └── material-list.vue
```

### 阶段七：前端API文件（优先级 P2）

**文件列表：**
```
xuanjiao-frontend/src/api/
├── asset.ts                            ⭐
├── workflow.ts
├── approval.ts
├── notification.ts
├── usage.ts
└── material.ts
```

### 阶段八：前端Stores（优先级 P3）

**文件列表：**
```
xuanjiao-frontend/src/stores/
├── asset.ts
├── workflow.ts
├── user.ts
└── notification.ts
```

---

## ✅ SonarLint检查标准对照表

| SonarLint规则 | 代码元素 | 合格标准 |
|---------------|---------|----------|
| squid:S1176 | public类 | 有JavaDoc，说明职责 |
| squid:S1176 | public方法 | 有JavaDoc，@param/@return完整 |
| squid:S1176 | 类字段 | 有`/** */`注释 |
| squid:S1176 | 接口方法 | 有JavaDoc，说明功能 |
| squid:S1176 | TypeScript导出函数 | 有JSDoc，说明功能 |
| squid:S1176 | Vue组件 | 有块注释说明用途 |
| squid:S1176 | TS类型定义 | 有注释说明用途 |

---

## 🚀 执行建议

1. **先跑SonarLint基线**：在IDE中运行SonarLint，记录当前违规数量
2. **分批执行**：按8个阶段分批，每批完成后运行SonarLint验证
3. **利用IDE功能**：
   - IDEA: `Tools > Generate > Generate Doc Comments`
   - VSCode: 使用 `Document This` 插件
4. **保持风格一致**：先制定注释模板，所有人遵循同一格式
5. **审核时检查**：PR审核时检查注释完整性

---

## 📁 文件清单

### 后端需要注释的文件统计

| 模块 | 实体/DO类 | Service接口 | Mapper接口 | Service实现 | 合计 |
|------|----------|------------|------------|-------------|------|
| asset | 1 | 1 | 1 | 1 | 4 |
| user | 2 | 1 | 1 | 1 | 5 |
| dept | 1 | 1 | 1 | 1 | 4 |
| role | 2 | 1 | 1 | 1 | 5 |
| menu | 1 | 1 | 1 | 1 | 4 |
| material | 2 | 1 | 1 | 1 | 5 |
| usage | 2 | 1 | 2 | 1 | 6 |
| deletion | 1 | 1 | 1 | 1 | 4 |
| notification | 1 | 1 | 1 | 1 | 4 |
| workflow | 3 | 2 | 2 | 2 | 9 |
| approval | 3 | 2 | 2 | 2 | 9 |
| log | 1 | 1 | 1 | 1 | 4 |
| **合计** | **20** | **13** | **14** | **13** | **60** |

### 前端需要注释的文件统计

| 类型 | 文件数 | 主要类型 |
|------|--------|----------|
| Vue组件 | ~50 | views/*.vue, components/*.vue |
| API文件 | ~20 | src/api/*.ts |
| Stores | ~10 | src/stores/*.ts |
| 类型定义 | ~15 | src/types/*.ts |
| Utils | ~10 | src/utils/*.ts |
| **合计** | **~105** | |

---

## 文档版本

| 版本 | 日期 | 修改内容 |
|------|------|----------|
| 1.0 | 2026-02-06 | 初始版本 |
