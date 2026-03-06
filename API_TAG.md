# API_TAG.md - 标签管理接口文档

> **模块**: 标签管理 (tag)
> **Controller**: `TagController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/asset/`
> **创建时间**: 2026-03-04

---

## 目录

1. [创建标签](#1-创建标签)
2. [获取所有标签](#2-获取所有标签)
3. [按分类获取标签](#3-按分类获取标签)
4. [删除标签](#4-删除标签)

---

## 1. 创建标签

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 创建标签 |
| 请求方法 | POST |
| 请求路径 | `/api/tag/create` |
| 接口描述 | 创建新的素材标签，用于素材分类和管理 |

### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 位置 |
|-------|------|------|------|------|
| name | String | 是 | 标签名称 | URL参数 |
| category | String | 否 | 标签分类 | URL参数 |

**请求示例**:
```
POST /api/tag/create?name=宣传海报&category=素材类型
```

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "name": "宣传海报",
    "category": "素材类型",
    "createTime": "2024-01-01 10:00:00"
  }
}
```

### 调用链路

| 层级 | 类名/方法 | 文件位置 | 说明 |
|-----|----------|---------|------|
| Adapter | `TagController.create()` | `adapter/web/asset/TagController.java:62` | 接收HTTP请求 |
| App | `TagService.create()` | `app/asset/TagService.java:35` | 业务逻辑 |
| App | `TagServiceImpl.create()` | `app/asset/impl/TagServiceImpl.java:36` | 实现逻辑 |
| Infrastructure | `TagMapper.insert()` | `infrastructure/asset/TagMapper.java:58` | SQL插入 |
| Database | `tag` 表 | MySQL | 数据表 |

### 数据库表

| 表名 | 操作 | 字段 |
|-----|------|------|
| tag | INSERT | name, category, create_time |

---

## 2. 获取所有标签

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取所有标签 |
| 请求方法 | POST |
| 请求路径 | `/api/tag/getList` |
| 接口描述 | 查询系统中所有标签，按分类排序 |

### 请求参数

**TagGetListQry**:
```json
{}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| - | - | - | 当前无过滤参数 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "宣传",
      "category": "主题",
      "createTime": "2024-01-01 10:00:00"
    },
    {
      "id": 2,
      "name": "海报",
      "category": "素材类型",
      "createTime": "2024-01-01 10:00:00"
    },
    {
      "id": 3,
      "name": "视频",
      "category": "素材类型",
      "createTime": "2024-01-01 10:00:00"
    }
  ]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `TagController.list()` | 接收HTTP请求 |
| App | `TagService.list()` | 业务逻辑 |
| App | `TagServiceImpl.list()` | 查询所有标签，按category排序 |
| Infrastructure | `TagMapper.selectList()` | SQL查询，ORDER BY category ASC |

### 排序规则

| 排序字段 | 方向 |
|---------|------|
| category | ASC |

---

## 3. 按分类获取标签

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 按分类获取标签 |
| 请求方法 | POST |
| 请求路径 | `/api/tag/getListByCategory` |
| 接口描述 | 查询指定分类下的所有标签 |

### 请求参数

**TagGetListByCategoryQry**:
```json
{
  "category": "素材类型"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| category | String | 是 | 标签分类 |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2,
      "name": "海报",
      "category": "素材类型",
      "createTime": "2024-01-01 10:00:00"
    },
    {
      "id": 3,
      "name": "视频",
      "category": "素材类型",
      "createTime": "2024-01-01 10:00:00"
    },
    {
      "id": 4,
      "name": "图片",
      "category": "素材类型",
      "createTime": "2024-01-01 10:00:00"
    }
  ]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `TagController.listByCategory()` | 接收HTTP请求 |
| App | `TagService.listByCategory()` | 业务逻辑 |
| App | `TagServiceImpl.listByCategory()` | 按分类查询，按name排序 |
| Infrastructure | `TagMapper.selectList()` | SQL查询，WHERE category = ? |

### 排序规则

| 排序字段 | 方向 |
|---------|------|
| name | ASC |

---

## 4. 删除标签

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除标签 |
| 请求方法 | POST |
| 请求路径 | `/api/tag/delete` |
| 接口描述 | 删除指定的标签（软删除） |

### 请求参数

**TagDeleteCmd**:
```json
{
  "id": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 标签ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `TagController.delete()` | 接收HTTP请求 |
| App | `TagService.delete()` | 业务逻辑 |
| App | `TagServiceImpl.delete()` | 实现逻辑 |
| Infrastructure | `TagMapper.deleteById()` | 软删除 |

### 注意事项

> 删除标签后：
> - 标签表中的记录会被软删除（deleted=1）
> - asset_tag关联表中的记录仍保留，但不影响使用
> - 已关联该标签的素材不受影响

---

## 附录：数据库表结构

### tag (标签表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 标签名称 |
| category | VARCHAR | 标签分类（如：素材类型、主题、来源等） |
| deleted | INT | 逻辑删除标记（0=正常，1=已删除） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### asset_tag (素材标签关联表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| asset_id | BIGINT | 素材ID |
| tag_id | BIGINT | 标签ID |

---

## 附录：常见标签分类

| 分类 | 示例标签 |
|-----|---------|
| 素材类型 | 图片、视频、文档、海报 |
| 主题 | 宣传、教育、培训、活动 |
| 来源 | 自制、购买、授权 |
| 部门 | 宣传部、培训部、风控部 |

---

## 附录：前端调用示例

### 获取所有标签

```typescript
// xuanjiao-frontend/src/api/tag.ts
export const getTagList = () => {
  return request.post<TagDTO[]>('/tag/getList', {})
}

// 使用示例
import { getTagList } from '@/api/tag'

const loadTags = async () => {
  try {
    const tags = await getTagList()
    console.log('标签列表:', tags.data)
    // 按分类分组展示
    const grouped = groupByCategory(tags.data)
    return grouped
  } catch (error) {
    console.error('加载标签失败', error)
  }
}
```

### 创建标签

```typescript
export const createTag = (name: string, category?: string) => {
  return request.post<TagDTO>(`/tag/create?name=${name}${category ? '&category=' + category : ''}`)
}
```

---

*本文档由API接口文档生成方法论自动生成。*
