# API_TASK.md - 我的任务接口文档

> **模块**: 我的任务 (task)
> **Controller**: `TaskController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/task/`
> **创建时间**: 2026-03-04

---

## 目录

1. [查询草稿箱（支持按类型和标题筛选）](#1-查询草稿箱支持按类型和标题筛选)

---

## 1. 查询草稿箱（支持按类型和标题筛选）

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 查询草稿箱（支持按类型和标题筛选） |
| 请求方法 | POST |
| 请求路径 | `/api/task/queryDrafts` |
| 接口描述 | 查询当前用户保存的草稿申请，包括素材录入、素材使用、素材删除三种类型 |

### 请求参数

**TaskQueryDraftsQry**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "draftType": "MATERIAL_ENTRY",
  "title": "宣传素材"
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| pageNum | Integer | 是 | 页码 | @Min(1) |
| pageSize | Integer | 是 | 每页数量 | @Min(1) |
| draftType | String | 否 | 草稿类型筛选 | - |
| title | String | 否 | 标题筛选（模糊查询） | - |

### 草稿类型说明

| draftType | 说明 | 对应DTO |
|-----------|------|---------|
| MATERIAL_ENTRY | 素材录入 | MaterialApplicationDTO |
| ASSET_USAGE | 素材使用 | UsageApplyDTO |
| ASSET_DELETION | 素材删除 | AssetDeletionApplicationDTO |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "type": "MATERIAL_ENTRY",
        "data": {
          "id": 50,
          "title": "2024年3月宣传素材",
          "applicantId": 10,
          "applicantName": "张三",
          "deptId": 5,
          "deptName": "宣传部",
          "status": "DRAFT",
          "createTime": "2024-03-01 10:00:00",
          "assets": [
            {
              "assetId": 100,
              "assetName": "宣传海报.jpg",
              "assetType": "IMAGE",
              "tagId": 10,
              "tagName": "宣传类"
            }
          ]
        }
      },
      {
        "type": "ASSET_USAGE",
        "data": {
          "id": 30,
          "title": "2024年3月素材使用申请",
          "userId": 10,
          "username": "张三",
          "status": "DRAFT",
          "createTime": "2024-03-02 11:00:00",
          "assets": [
            {
              "assetId": 101,
              "assetName": "产品图片.jpg",
              "usageDescription": "用于官网展示"
            }
          ]
        }
      },
      {
        "type": "ASSET_DELETION",
        "data": {
          "id": 20,
          "title": "过期素材清理申请",
          "applicantId": 10,
          "applicantName": "张三",
          "status": "DRAFT",
          "createTime": "2024-03-03 09:00:00",
          "assets": [
            {
              "assetId": 102,
              "assetName": "过期海报.jpg"
            }
          ]
        }
      }
    ],
    "total": 15,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `TaskController.queryDrafts()` | 接收HTTP请求，注入userId |
| App | `MaterialApplicationService.queryDrafts()` | 查询素材录入草稿 |
| App | `UsageApplyService.queryDrafts()` | 查询素材使用草稿 |
| App | `AssetDeletionApplicationService.queryDrafts()` | 查询素材删除草稿 |
| Adapter | `getCreateTime()` | 反射获取创建时间用于排序 |
| Adapter | `内存排序+分页` | 合并多类型草稿后排序分页 |

### 数据聚合逻辑

```
1. 根据draftType决定查询哪些类型的草稿：
   - draftType=null: 查询所有三种类型
   - draftType="MATERIAL_ENTRY": 仅查询素材录入
   - draftType="ASSET_USAGE": 仅查询素材使用
   - draftType="ASSET_DELETION": 仅查询素材删除

2. 将各类型草稿包装为DraftItemDTO：
   - 设置type为业务类型
   - 设置data为对应业务DTO

3. 按createTime倒序排序（反射获取）

4. 内存分页返回结果
```

---

## 附录：草稿数据结构

### MaterialApplicationDTO（素材录入草稿）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Long | 申请单ID |
| title | String | 申请标题 |
| applicantId | Long | 申请人ID |
| applicantName | String | 申请人姓名 |
| deptId | Long | 部门ID |
| deptName | String | 部门名称 |
| status | String | 状态（固定为DRAFT） |
| createTime | LocalDateTime | 创建时间 |
| assets | List\<AssetDTO\> | 素材列表 |

### UsageApplyDTO（素材使用草稿）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Long | 申请单ID |
| title | String | 申请标题 |
| userId | Long | 申请人ID |
| username | String | 申请人姓名 |
| status | String | 状态（固定为DRAFT） |
| createTime | LocalDateTime | 创建时间 |
| assets | List\<UsageAssetDTO\> | 素材列表（含使用配置） |

### AssetDeletionApplicationDTO（素材删除草稿）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | Long | 申请单ID |
| title | String | 申请标题 |
| applicantId | Long | 申请人ID |
| applicantName | String | 申请人姓名 |
| status | String | 状态（固定为DRAFT） |
| createTime | LocalDateTime | 创建时间 |
| assets | List\<DeletionAssetDTO\> | 素材列表 |

---

## 附录：使用场景

### 场景1：草稿箱页面

前端统一草稿箱页面，展示用户所有类型的草稿：
- 用户可看到所有未提交的申请
- 按时间倒序显示最新草稿
- 支持按类型筛选
- 支持按标题搜索

### 场景2：数据聚合展示

```typescript
// 前端根据 type 字段展示不同的卡片类型
const renderDraftItem = (item: DraftItemDTO) => {
  switch (item.type) {
    case 'MATERIAL_ENTRY':
      return <MaterialEntryDraftCard data={item.data} />
    case 'ASSET_USAGE':
      return <UsageDraftCard data={item.data} />
    case 'ASSET_DELETION':
      return <DeletionDraftCard data={item.data} />
  }
}
```

---

## 附录：前端调用示例

### 查询草稿箱

```typescript
// xuanjiao-frontend/src/api/task.ts
export const queryDrafts = (data: {
  pageNum: number
  pageSize: number
  draftType?: 'MATERIAL_ENTRY' | 'ASSET_USAGE' | 'ASSET_DELETION'
  title?: string
}) => {
  return request.post('/task/queryDrafts', data)
}
```

### 查询所有草稿

```typescript
const fetchAllDrafts = async () => {
  const result = await queryDrafts({
    pageNum: 1,
    pageSize: 50,
    draftType: undefined  // 不指定类型，查询所有
  })
  return result.data.list
}
```

### 按类型筛选

```typescript
const fetchMaterialDrafts = async () => {
  const result = await queryDrafts({
    pageNum: 1,
    pageSize: 10,
    draftType: 'MATERIAL_ENTRY'
  })
  return result.data.list
}
```

### 按标题搜索

```typescript
const searchDrafts = async (keyword: string) => {
  const result = await queryDrafts({
    pageNum: 1,
    pageSize: 20,
    title: keyword
  })
  return result.data.list
}
```

---

## 附录：与其他模块的关系

### 依赖的服务

| 服务 | 方法 | 说明 |
|-----|------|------|
| MaterialApplicationService | queryDrafts() | 查询素材录入草稿 |
| UsageApplyService | queryDrafts() | 查询素材使用草稿 |
| AssetDeletionApplicationService | queryDrafts() | 查询素材删除草稿 |

### 数据流向

```
前端请求 → TaskController
    ↓
并行调用三个服务的 queryDrafts()
    ↓
MaterialApplicationService → material_application 表
UsageApplyService → usage_apply 表
AssetDeletionApplicationService → asset_deletion_application 表
    ↓
合并结果 → 按时间排序 → 内存分页
    ↓
返回统一格式的 DraftItemDTO 列表
```

---

*本文档由API接口文档生成方法论自动生成。*
