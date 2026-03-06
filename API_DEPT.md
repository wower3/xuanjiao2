# API_DEPT.md - 部门管理接口文档

> **模块**: 部门管理 (dept)
> **Controller**: `DeptController.java`
> **路径**: `xuanjiao-adapter/src/main/java/com/xuanjiao/adapter/web/dept/`
> **创建时间**: 2026-03-04

---

## 目录

1. [部门列表](#1-部门列表)
2. [部门树](#2-部门树)
3. [部门详情](#3-部门详情)
4. [保存部门](#4-保存部门)
5. [更新部门](#5-更新部门)
6. [删除部门](#6-删除部门)
7. [生成部门编号](#7-生成部门编号)

---

## 1. 部门列表

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 部门列表 |
| 请求方法 | POST |
| 请求路径 | `/api/dept/getList` |
| 接口描述 | 查询系统中所有部门的列表信息（平铺结构） |

### 请求参数

**DeptGetListQry**:
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
      "name": "总公司",
      "code": "DEPT001",
      "parentId": null,
      "level": 1,
      "fullCode": "DEPT001",
      "sort": 1
    },
    {
      "id": 10,
      "name": "北京分公司",
      "code": "DEPT010",
      "parentId": 1,
      "level": 2,
      "fullCode": "DEPT001-DEPT010",
      "sort": 1
    }
  ]
}
```

### 调用链路

| 层级 | 类名/方法 | 文件位置 | 说明 |
|-----|----------|---------|------|
| Adapter | `DeptController.list()` | `adapter/web/dept/DeptController.java:63` | 接收HTTP请求 |
| App | `DeptService.list()` | `app/dept/DeptService.java:32` | 业务逻辑 |
| App | `DeptServiceImpl.list()` | `app/dept/impl/DeptServiceImpl.java:35` | 实现逻辑 |
| Infrastructure | `DeptMapper.selectList()` | `infrastructure/dept/DeptMapper.java:57` | SQL查询 |
| Database | `sys_dept` 表 | MySQL | 数据表 |

### 数据库表

| 表名 | 操作 | 字段 |
|-----|------|------|
| sys_dept | SELECT | id, name, code, parent_id, level, full_code, sort |

---

## 2. 部门树

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 部门树 |
| 请求方法 | POST |
| 请求路径 | `/api/dept/getTree` |
| 接口描述 | 查询部门树形结构，用于前端部门选择器展示 |

### 请求参数

**DeptGetTreeQry**:
```json
{}
```

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "总公司",
      "code": "DEPT001",
      "parentId": null,
      "level": 1,
      "sort": 1,
      "children": [
        {
          "id": 10,
          "name": "北京分公司",
          "code": "DEPT010",
          "parentId": 1,
          "level": 2,
          "sort": 1,
          "children": [
            {
              "id": 101,
              "name": "海淀营业部",
              "code": "DEPT101",
              "parentId": 10,
              "level": 3,
              "sort": 1,
              "children": []
            }
          ]
        },
        {
          "id": 11,
          "name": "上海分公司",
          "code": "DEPT011",
          "parentId": 1,
          "level": 2,
          "sort": 2,
          "children": []
        }
      ]
    }
  ]
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `DeptController.tree()` | 接收HTTP请求 |
| App | `DeptService.getTree()` | 获取树形结构 |
| App | `DeptServiceImpl.getTree()` | 查询所有部门后递归构建树 |
| App | `buildTree()` | 递归构建树形结构 |
| Infrastructure | `DeptMapper.selectAll()` | 查询所有部门 |

### 树形结构构建算法

```
1. 查询所有部门列表（按 level, sort 排序）
2. 递归构建树:
   - 从根节点开始（parentId = 0 或 null）
   - 对每个节点，递归查找其子节点
   - 将子节点放入 children 数组
3. 返回树形结构
```

---

## 3. 部门详情

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 获取部门详情 |
| 请求方法 | POST |
| 请求路径 | `/api/dept/getDetail` |
| 接口描述 | 根据部门ID查询部门的详细信息 |

### 请求参数

**DeptGetDetailQry**:
```json
{
  "id": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 部门ID |

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10,
    "name": "北京分公司",
    "code": "DEPT010",
    "parentId": 1,
    "level": 2,
    "fullCode": "DEPT001-DEPT010",
    "sort": 1
  }
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `DeptController.getById()` | 接收HTTP请求 |
| App | `DeptService.getById()` | 业务逻辑 |
| Infrastructure | `DeptMapper.selectById()` | SQL查询 |

---

## 4. 保存部门

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 保存部门 |
| 请求方法 | POST |
| 请求路径 | `/api/dept/create` |
| 接口描述 | 创建新的部门，自动生成部门编号和层级信息 |

### 请求参数

**DeptDTO**:
```json
{
  "name": "深圳分公司",
  "parentId": 1,
  "sort": 3
}
```

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|-------|------|------|------|---------|
| name | String | 是 | 部门名称 | @NotBlank |
| parentId | Long | 否 | 父部门ID，顶级部门传null或0 | - |
| code | String | 否 | 部门编码，不传则自动生成 | - |
| sort | Integer | 否 | 排序号 | - |

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
| Adapter | `DeptController.save()` | 接收HTTP请求 |
| App | `DeptService.save()` | 业务逻辑 |
| App | `DeptServiceImpl.save()` | 实现逻辑 |
| App | `generateCode()` | 生成部门编号（如未提供） |
| App | `计算level和fullCode` | 根据父部门计算层级和完整编码 |
| Infrastructure | `DeptMapper.insert()` | 插入数据库 |

### 自动生成逻辑

| 字段 | 生成规则 |
|-----|---------|
| code | 6位随机字符串（大写字母+数字，排除易混淆字符） |
| level | 父部门level + 1（顶级部门为1） |
| fullCode | 父部门fullCode + "-" + code（顶级部门为自己的code） |

### 部门编号生成规则

```
字符集: ABCDEFGHJKLMNPQRSTUVWXYZ23456789（排除 I, O, 0, 1）
长度: 6位
唯一性: 循环检查直到生成不重复的编号
```

---

## 5. 更新部门

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 更新部门 |
| 请求方法 | POST |
| 请求路径 | `/api/dept/update` |
| 接口描述 | 修改部门信息（名称、编码、父部门、排序等） |

### 请求参数

**DeptUpdateCmd**:
```json
{
  "id": 10,
  "name": "北京分公司（更名）",
  "code": "DEPT010",
  "parentId": 1,
  "sort": 1
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 部门ID |
| name | String | 否 | 部门名称 |
| code | String | 否 | 部门编码 |
| parentId | Long | 否 | 父部门ID |
| sort | Integer | 否 | 排序号 |

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
| Adapter | `DeptController.update()` | 接收请求，转换DTO |
| App | `DeptService.update()` | 业务逻辑 |
| Infrastructure | `DeptMapper.updateById()` | 更新数据库 |

---

## 6. 删除部门

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 删除部门 |
| 请求方法 | POST |
| 请求路径 | `/api/dept/delete` |
| 接口描述 | 删除指定部门（逻辑删除） |

### 请求参数

**DeptDeleteCmd**:
```json
{
  "id": 10
}
```

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| id | Long | 是 | 部门ID |

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
| Adapter | `DeptController.delete()` | 接收HTTP请求 |
| App | `DeptService.delete()` | 业务逻辑 |
| Infrastructure | `DeptMapper.deleteById()` | 逻辑删除 |

### 注意事项

> 删除部门前需要确保：
> 1. 部门下没有子部门
> 2. 部门下没有关联用户
>
> 如果有子部门或关联用户，需要先处理这些数据。

---

## 7. 生成部门编号

### 基础信息

| 项目 | 内容 |
|-----|------|
| 接口名称 | 生成部门编号 |
| 请求方法 | GET |
| 请求路径 | `/api/dept/generate-code` |
| 接口描述 | 自动生成唯一的部门编号 |

### 请求参数

无（GET请求）

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": "A3B7K9"
}
```

### 调用链路

| 层级 | 方法 | 说明 |
|-----|------|------|
| Adapter | `DeptController.generateCode()` | 接收HTTP请求 |
| App | `DeptService.generateCode()` | 业务逻辑 |
| App | `DeptServiceImpl.generateCode()` | 生成随机编码，循环检查唯一性 |
| Infrastructure | `DeptMapper.selectByCode()` | 检查编码是否已存在 |

---

## 附录：数据库表结构

### sys_dept (部门表)

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR | 部门名称 |
| code | VARCHAR | 部门编码（唯一） |
| parent_id | BIGINT | 父部门ID，顶级部门为NULL |
| level | INT | 层级（1=一级机构，2=二级机构...） |
| full_code | VARCHAR | 完整编码（如：DEPT001-DEPT010-DEPT101） |
| sort | INT | 排序号 |
| deleted | INT | 逻辑删除标记（0=正常，1=已删除） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

---

## 附录：部门树形结构示例

```
一级机构 (level=1)
├── 总公司 (id=1, code=DEPT001)
│   ├── 北京分公司 (id=10, code=DEPT010, level=2)
│   │   ├── 海淀营业部 (id=101, code=DEPT101, level=3)
│   │   └── 朝阳营业部 (id=102, code=DEPT102, level=3)
│   ├── 上海分公司 (id=11, code=DEPT011, level=2)
│   │   ├── 浦东营业部 (id=111, code=DEPT111, level=3)
│   │   └── 徐汇营业部 (id=112, code=DEPT112, level=3)
│   └── 深圳分公司 (id=12, code=DEPT012, level=2)
│       └── 南山营业部 (id=121, code=DEPT121, level=3)
```

---

## 附录：前端调用示例

### 获取部门树

```typescript
// xuanjiao-frontend/src/api/dept.ts
export const getDeptTree = () => {
  return request.post<DeptDTO[]>('/dept/getTree', {})
}

// 使用示例
import { getDeptTree } from '@/api/dept'

const loadDeptTree = async () => {
  try {
    const tree = await getDeptTree()
    console.log('部门树:', tree.data)
  } catch (error) {
    console.error('加载部门树失败', error)
  }
}
```

### 保存部门

```typescript
export const createDept = (data: {
  name: string
  parentId?: number
  sort?: number
}) => {
  return request.post('/dept/create', data)
}
```

---

*本文档由API接口文档生成方法论自动生成。*
