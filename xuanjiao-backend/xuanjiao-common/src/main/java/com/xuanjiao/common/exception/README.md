# 自定义异常迁移指南

本文档说明如何将现有代码中的 `RuntimeException` 替换为自定义异常类。

## 异常类结构

```
BaseException (基础异常)
├── BusinessException      (业务异常)
├── NotFoundException      (资源未找到异常)
├── PermissionException    (权限异常)
├── ValidationException    (参数验证异常)
└── SystemException        (系统异常)
```

## 异常类型选择指南

### 1. NotFoundException (资源未找到)
**使用场景：** 查询的资源不存在
- 用户不存在
- 流程不存在
- 任务不存在
- 素材不存在
- 申请单不存在
- 审批实例不存在

**示例：**
```java
// 修改前
throw new RuntimeException("用户不存在");

// 修改后
throw new NotFoundException("用户不存在");
// 或使用便捷构造函数
throw new NotFoundException("用户", userId);
```

### 2. PermissionException (权限异常)
**使用场景：** 权限不足
- 无权操作
- 无权分配角色
- 只有管理员才能执行此操作
- 分消保管理岗只能管理其所属二级机构的用户

**示例：**
```java
// 修改前
throw new RuntimeException("无权操作");

// 修改后
throw new PermissionException("无权操作");
```

### 3. BusinessException (业务异常)
**使用场景：** 业务规则验证失败
- 只有草稿状态可以修改
- 只能使用已通过审批的素材
- 只有审批中的工单才能追回
- 只能修改自己的申请单
- 只有待审批任务才能退回
- 该素材已被删除，无法使用

**示例：**
```java
// 修改前
throw new RuntimeException("只有草稿状态可以修改");

// 修改后
throw new BusinessException("只有草稿状态可以修改");
```

### 4. ValidationException (参数验证异常)
**使用场景：** 参数验证失败
- 角色类型不能为空
- 角色类型只能包含大写字母、数字和下划线
- 请至少上传一个素材文件
- 请至少选择一个素材并配置使用信息

**示例：**
```java
// 修改前
throw new RuntimeException("角色类型不能为空");

// 修改后
throw new ValidationException("角色类型不能为空");
// 或使用便捷构造函数
throw new ValidationException("角色类型", "不能为空");
```

### 5. SystemException (系统异常)
**使用场景：** 系统级错误
- 文件上传失败
- 对象转换失败
- 数据库操作失败
- 其他系统级错误

**示例：**
```java
// 修改前
throw new RuntimeException("文件上传失败", e);

// 修改后
throw new SystemException("文件上传失败", e);
```

## HTTP 状态码映射

全局异常处理器会将自定义异常映射到相应的 HTTP 状态码：

| 异常类型 | HTTP 状态码 | 日志级别 |
|---------|------------|---------|
| ValidationException | 400 | WARN |
| PermissionException | 403 | WARN |
| NotFoundException | 404 | WARN |
| BusinessException | 500 | WARN |
| SystemException | 500 | ERROR |

## 批量替换建议

可以使用 IDE 的批量替换功能进行迁移：

### IntelliJ IDEA / Android Studio
1. 使用 `Ctrl+Shift+R` (Windows) 或 `Cmd+Shift+R` (Mac) 打开替换对话框
2. 在 "Replace in path" 中进行替换

### VS Code
1. 使用 `Ctrl+Shift+H` (Windows) 或 `Cmd+Shift+H` (Mac) 打开替换对话框
2. 在文件搜索中进行替换

### 替换模式

#### NotFoundException
```
查找: throw new RuntimeException\("([^"]*(?:不存在|不存在:|不存在：)[^"]*)"\)
替换: throw new NotFoundException("$1")
```

#### PermissionException
```
查找: throw new RuntimeException\("([^"]*(?:无权|只有.*才能|只能)[^"]*)"\)
替换: throw new PermissionException("$1")
```

**注意：** 自动替换后需要手动检查，确保异常类型选择正确。

## 事务处理

所有自定义异常都继承自 `RuntimeException`，因此会触发事务回滚（默认情况下）。如果不需要事务回滚，可以在 `@Transactional` 注解中指定：

```java
@Transactional(rollbackFor = Exception.class)
@Transactional(noRollbackFor = BusinessException.class)
```

## 错误码扩展

如果需要更细粒度的错误码控制，可以在抛出异常时指定自定义错误码：

```java
throw new NotFoundException("USER_NOT_FOUND", "用户不存在");
throw new BusinessException("INVALID_STATUS", "只有草稿状态可以修改");
```

## 完整示例

### 修改前
```java
public void updateUser(Long userId, UserUpdateCmd cmd) {
    User user = userRepository.findById(userId);
    if (user == null) {
        throw new RuntimeException("用户不存在");
    }

    if (!canModify(user)) {
        throw new RuntimeException("无权操作");
    }

    if (user.getStatus() != Status.DRAFT) {
        throw new RuntimeException("只有草稿状态可以修改");
    }

    // ... 业务逻辑
}
```

### 修改后
```java
public void updateUser(Long userId, UserUpdateCmd cmd) {
    User user = userRepository.findById(userId);
    if (user == null) {
        throw new NotFoundException("用户不存在");
    }

    if (!canModify(user)) {
        throw new PermissionException("无权操作");
    }

    if (user.getStatus() != Status.DRAFT) {
        throw new BusinessException("只有草稿状态可以修改");
    }

    // ... 业务逻辑
}
```

## 迁移优先级

建议按以下优先级进行迁移：

1. **高优先级**：Controller 层和 Service 层的用户可见错误
2. **中优先级**：业务逻辑验证错误
3. **低优先级**：不太可能发生的边界情况错误
