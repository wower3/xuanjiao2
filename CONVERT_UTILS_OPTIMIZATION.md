# 对象转换工具类优化方案

## 一、背景

项目中使用 `org.springframework.beans.BeanUtils.copyProperties()` 进行对象属性复制，散落在 App 层和 Infrastructure 层共计 21 处。为统一管理对象转换逻辑，提升代码可维护性，特创建 `xuanjiao-common` 公共模块。

## 二、方案概述

### 1. 新建 xuanjiao-common 模块

```
xuanjiao-backend/xuanjiao-common/
├── pom.xml
└── src/main/java/com/xuanjiao/common/
      └── ConvertUtils.java
```

### 2. 模块依赖关系

所有业务模块依赖 common 模块：

```xml
<!-- xuanjiao-app/pom.xml -->
<dependency>
    <groupId>com.xuanjiao</groupId>
    <artifactId>xuanjiao-common</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- xuanjiao-infrastructure/pom.xml -->
<dependency>
    <groupId>com.xuanjiao</groupId>
    <artifactId>xuanjiao-common</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- xuanjiao-adapter/pom.xml -->
<dependency>
    <groupId>com.xuanjiao</groupId>
    <artifactId>xuanjiao-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 三、ConvertUtils 工具类

### 1. 核心功能

| 方法 | 说明 |
|------|------|
| `copyProperties(source, target)` | 复制属性，**忽略 source 中的 null 值** |
| `copyPropertiesIncludeNull(source, target)` | 复制属性，**包含 null 值**（覆盖目标） |
| `copyProperties(source, TargetClass)` | 复制并创建新对象，返回 TargetClass 实例 |

### 2. 源代码

```java
package com.xuanjiao.common;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * 对象转换工具类
 */
public class ConvertUtils {

    /**
     * 复制对象属性（忽略 null 值）
     * 注意：source 为 null 时不做任何处理
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null) {
            return;
        }
        org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 复制对象属性（包含 null 值，覆盖目标对象的所有属性）
     * 注意：source 为 null 时不做任何处理
     */
    public static void copyPropertiesIncludeNull(Object source, Object target) {
        if (source == null) {
            return;
        }
        org.springframework.beans.BeanUtils.copyProperties(source }

    /**
     * 复制对象, target);
   属性并返回目标对象（忽略 null 值）
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("对象转换失败: " + targetClass.getName(), e);
        }
    }

    /**
     * 获取对象中 null 值的属性名
     */
    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }

        return emptyNames.toArray(new String[0]);
    }
}
```

### 3. 使用示例

```java
import com.xuanjiao.common.ConvertUtils;

// 例1：Entity → DTO（忽略 null 值）
ConvertUtils.copyProperties(entity, dto);

// 例2：DTO → Entity（忽略 null 值）
ConvertUtils.copyProperties(dto, entity);

// 例3：需要覆盖 null 值时（谨慎使用）
ConvertUtils.copyPropertiesIncludeNull(source, target);

// 例4：直接创建并转换
UserDTO dto = ConvertUtils.copyProperties(userEntity, UserDTO.class);
```

## 四、涉及修改的文件清单

### App 层（13 个文件）

| 序号 | 文件路径 | 修改内容 |
|------|----------|----------|
| 1 | `xuanjiao-app/.../approval/impl/ApprovalServiceImpl.java` | BeanUtils → ConvertUtils |
| 2 | `xuanjiao-app/.../asset/impl/AssetServiceImpl.java` | BeanUtils → ConvertUtils |
| 3 | `xuanjiao-app/.../asset/impl/TagServiceImpl.java` | BeanUtils → ConvertUtils |
| 4 | `xuanjiao-app/.../auth/impl/AuthServiceImpl.java` | BeanUtils → ConvertUtils |
| 5 | `xuanjiao-app/.../dept/impl/DeptServiceImpl.java` | BeanUtils → ConvertUtils |
| 6 | `xuanjiao-app/.../role/impl/RoleServiceImpl.java` | BeanUtils → ConvertUtils |
| 7 | `xuanjiao-app/.../menu/impl/MenuServiceImpl.java` | BeanUtils → ConvertUtils |
| 8 | `xuanjiao-app/.../user/impl/UserServiceImpl.java` | BeanUtils → ConvertUtils |
| 9 | `xuanjiao-app/.../workflow/impl/WorkflowServiceImpl.java` | BeanUtils → ConvertUtils |
| 10 | `xuanjiao-app/.../notification/impl/NotificationServiceImpl.java` | BeanUtils → ConvertUtils |
| 11 | `xuanjiao-app/.../usage/impl/UsageApplyServiceImpl.java` | BeanUtils → ConvertUtils |
| 12 | `xuanjiao-app/.../material/impl/MaterialApplicationServiceImpl.java` | BeanUtils → ConvertUtils |
| 13 | `xuanjiao-app/.../deletion/impl/AssetDeletionApplicationServiceImpl.java` | BeanUtils → ConvertUtils |

### Infrastructure 层（8 个文件）

| 序号 | 文件路径 | 修改内容 |
|------|----------|----------|
| 14 | `xuanjiao-infrastructure/.../asset/AssetRepositoryImpl.java` | BeanUtils → ConvertUtils |
| 15 | `xuanjiao-infrastructure/.../notification/NotificationRepositoryImpl.java` | BeanUtils → ConvertUtils |
| 16 | `xuanjiao-infrastructure/.../deletion/repository/AssetDeletionApplicationRepositoryImpl.java` | BeanUtils → ConvertUtils |
| 17 | `xuanjiao-infrastructure/.../material/MaterialApplicationRepositoryImpl.java` | BeanUtils → ConvertUtils |
| 18 | `xuanjiao-infrastructure/.../user/UserRepositoryImpl.java` | BeanUtils → ConvertUtils |
| 19 | `xuanjiao-infrastructure/.../usage/UsageApplyAssetRepositoryImpl.java` | BeanUtils → ConvertUtils |
| 20 | `xuanjiao-infrastructure/.../usage/UsageApplyRepositoryImpl.java` | BeanUtils → ConvertUtils |
| 21 | `xuanjiao-infrastructure/.../log/repository/OperationLogRepositoryImpl.java` | BeanUtils → ConvertUtils |

### 不修改的文件

| 层次 | 文件 | 原因 |
|------|------|------|
| **Controller** | UserController, RoleController, DeptController, WorkflowController, MenuController | 手动赋值，复用性低 |
| **App** | ApprovalServiceImpl.convertToPendingTaskDTO | 包含关联查询 |
| **App** | AssetDeletionApplicationServiceImpl.convertToDTO | 包含关联查询 |
| **App** | NotificationServiceImpl.convertToDTO | 包含关联查询 |
| **App** | ApproverSelectionServiceImpl.convertToSelectionDTO | 包含关联查询 |

## 五、实施步骤

### 1. 创建 xuanjiao-common 模块

```bash
# 1.1 创建目录结构
mkdir -p xuanjiao-backend/xuanjiao-common/src/main/java/com/xuanjiao/common

# 1.2 创建 pom.xml（见上文）
# 1.3 创建 ConvertUtils.java（见上文）
```

### 2. 修改父 pom.xml

在 `<modules>` 中添加（放在最前面）：
```xml
<module>xuanjiao-common</module>
```

### 3. 添加模块依赖

在 xuanjiao-app、xuanjiao-infrastructure 的 pom.xml 中添加：
```xml
<dependency>
    <groupId>com.xuanjiao</groupId>
    <artifactId>xuanjiao-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 4. 批量替换代码

统一替换（21 个文件）：
```java
// 替换前
import org.springframework.beans.BeanUtils;
BeanUtils.copyProperties(source, target);

// 替换后
import com.xuanjiao.common.ConvertUtils;
ConvertUtils.copyProperties(source, target);
```

## 六、变更说明

| 项目 | 变更前 | 变更后 |
|------|--------|--------|
| 导入方式 | `org.springframework.beans.BeanUtils` | `com.xuanjiao.common.ConvertUtils` |
| null 值处理 | Spring 默认（覆盖目标） | **默认忽略 null**（更安全） |
| 新增功能 | 无 | `copyPropertiesIncludeNull`、`copyProperties(source, Class)` |

## 七、注意事项

1. **null 值行为变更**：原有代码使用 `BeanUtils.copyProperties` 会覆盖目标的 null 值，新工具类默认忽略 null，可能影响现有逻辑。如需覆盖 null，使用 `copyPropertiesIncludeNull`。

2. **保留方法**：包含业务逻辑（如关联查询）的转换方法保留在原 Service 中，不抽到工具类。

3. **Controller 层**：手动赋值方式暂不统一，保持现有代码风格。
