# Department Tree Debug Guide

## Changes Made

### Frontend (xuanjiao-frontend/src/views/system/user.vue)

Added comprehensive logging to debug the department tree issue:

1. **`loadDeptTree()`**: Logs the original department tree structure from the backend
2. **`loadDefaultFilter()`**: Logs the API response including `rootDeptId`
3. **`buildDeptTreeFromRoot()`**: Detailed logging of:
   - Input parameters (rootId and its type)
   - Original tree structure
   - Each node being checked during search
   - The found root node
   - The final result tree

## How to Test

1. **Start the frontend dev server** with browser console open
2. **Login as a 分消保管理岗 user** (e.g., `fen1_xb_manager_1` / `123456`)
3. **Navigate to 用户管理 page**
4. **Check the browser console** for the debug logs

## Expected Behavior

For user `fen1_xb_manager_1` (dept_id=202, 分消保部):

1. `loadDeptTree` should log the full tree starting from 总公司 (id=100)
2. `loadDefaultFilter` should log:
   - `hasFilter: true`
   - `deptId: 202` (user's current dept)
   - `rootDeptId: 102` (分部门1, the level=2 parent)
3. `buildDeptTreeFromRoot` should:
   - Find node with id=102
   - Return a tree starting from 分部门1

## Expected Tree Structure

**Full tree (from backend):**
```
总公司 (100)
├── 总部门 (101)
│   ├── 总经办 (1)
│   ├── 技术部 (2)
│   ├── 市场部 (3)
│   ├── 财务部 (4)
│   ├── 人事部 (5)
│   └── 总消保部 (201)
├── 分部门1 (102)
│   ├── 分消保部 (202)
│   └── 二分部门1 (206)
└── 分部门2 (204)
    ├── 分消保部2 (205)
    └── 二分部门2 (207)
```

**Filtered tree for 分消保管理岗 in dept 202:**
```
分部门1 (102)
├── 分消保部 (202)
└── 二分部门1 (206)
```

## Console Output to Look For

```
[loadDeptTree] 开始加载部门树
[loadDeptTree] API响应: [{id: 100, name: "总公司", children: [...]}]
[loadDeptTree] 初始化deptTree: [...]

[loadDefaultFilter] 开始获取默认筛选条件
[loadDefaultFilter] API响应: {hasFilter: true, deptId: 202, rootDeptId: 102, ...}
[loadDefaultFilter] hasFilter=true, deptId: 202, rootDeptId: 102
[loadDefaultFilter] 开始构建部门树，rawDeptTree: [...]
[buildDeptTreeFromRoot] 输入 - rootId: 102, rootId类型: number
[buildDeptTreeFromRoot] 原始树结构: [...]
[findNode] 检查节点: 100 number vs 102 number
[findNode] 检查节点: 101 number vs 102 number
[findNode] 检查节点: 102 number vs 102 number
[buildDeptTreeFromRoot] 找到的根节点: {id: 102, name: "分部门1", ...}
[buildDeptTreeFromRoot] 构建的结果树: [{id: 102, ...}]
[loadDefaultFilter] 构建后的deptTree: [{id: 102, ...}]
```

## Possible Issues

1. **If `rootDeptId` is null/undefined**:
   - Check if the user has role_type `BRANCH_MGMT`
   - Check if `getSecondaryDeptId()` returns correctly

2. **If node not found in tree**:
   - Check if the tree structure matches the database
   - Check if there's a type mismatch (string vs number)

3. **If tree shows "总公司" as root**:
   - The `deptTree.value` might not be updated properly
   - Check if `buildDeptTreeFromRoot` is actually called
