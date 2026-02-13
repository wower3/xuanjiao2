# MyBatis-Plus 分页准则

## 概述

本项目统一使用 MyBatis-Plus 进行分页查询，本文档定义分页使用的规范和准则。

## 分页使用场景

### 1. 使用 MyBatis-Plus 分页的场景

| 场景 | 说明 | 示例 |
|------|------|------|
| **列表分页查询** | 用户界面的分页列表展示 | 用户列表、素材列表、审批列表 |
| **API 分页接口** | 任何返回分页结果的 REST API | `GET /users?pageNum=1&pageSize=10` |
| **带条件的分页查询** | 需要按条件筛选并分页的结果集 | 按状态筛选的用户列表 |
| **JOIN 查询分页** | 多表关联后的分页查询 | 用户+部门+角色的联合查询分页 |
| **统计带分页** | 需要同时返回数据和总数 | 分页结果 + 总数的查询 |

### 2. 不使用 MyBatis-Plus 分页的场景

| 场景 | 说明 | 替代方案 |
|------|------|----------|
| **全量数据导出** | 导出所有数据（不限制条数） | 使用 `selectList` 配合内存分页或流式查询 |
| **数据统计汇总** | 统计类查询，不需要分页 | 使用 `selectCount` 或聚合查询 |
| **下拉选项加载** | 加载少量选项数据 | 使用 `selectList` 限制返回条数 |
| **批量处理** | 需要处理全部数据 | 分批查询后处理 |

## 分页实现规范

### 1. Mapper 接口定义

```java
// 分页查询方法（使用 MyBatis-Plus）
IPage<DTO> selectPage(Page<DTO> page, @Param("query") Query query);

// 统计查询方法
Long selectCount(Query query);
```

### 2. Mapper XML 实现

```xml
<!-- 分页查询 -->
<select id="selectPage" resultMap="ResultMap">
    SELECT ...
    FROM table
    <where>
        <!-- 使用 query. 前缀访问查询参数 -->
        <if test="query.keyword != null and query.keyword != ''">
            AND name LIKE CONCAT('%', #{query.keyword}, '%')
        </if>
    </where>
    <!-- 不需要手动写 LIMIT，由 MyBatis-Plus 自动处理 -->
</select>
```

### 3. Service 层调用

```java
public PageResult<UserDTO> getUserPage(UserPageQry qry) {
    Page<UserDO> page = new Page<>(qry.getPageNum(), qry.getPageSize());
    IPage<UserDO> result = userMapper.selectPage(page, convertToQuery(qry));

    return PageResult.of(
        convertToDTOList(result.getRecords()),
        result.getTotal(),
        qry.getPageNum(),
        qry.getPageSize()
    );
}
```

## Query 对象规范

### 1. 继承 BasePageQry

所有分页查询对象应继承 `BasePageQry`：

```java
// xuanjiao-client/src/main/java/com/xuanjiao/client/dto/xxx/xxxQry.java
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQry extends BasePageQry {
    private String keyword;
    private Long roleId;
    private Long deptId;
    // 其他查询条件...
}
```

### 2. Query 类位置

- **客户端层** (`xuanjiao-client`): API 请求参数，继承 `BasePageQry`
- **基础设施层** (`xuanjiao-infrastructure`): 内部查询对象，不继承 `BasePageQry`，使用 `@Param("query")` 注解

### 3. BasePageQry 定义

```java
// xuanjiao-client/src/main/java/com/xuanjiao/client/dto/common/BasePageQry.java
@Data
public abstract class BasePageQry {
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer pageSize = 10;
}
```

## 分页参数处理

### 1. 参数命名规范

| 层级 | 参数前缀 | 示例 |
|------|----------|------|
| Mapper 接口 | `query` | `@Param("query") Query query` |
| Mapper XML | `query.` | `#{query.keyword}` |
| Page 对象 | 无 | `Page<T> page` |

### 2. 避免的手动分页方式

```java
// ❌ 错误：手动计算 offset/limit
int offset = (pageNum - 1) * pageSize;
query.setOffset(offset);
query.setLimit(pageSize);

// ✅ 正确：使用 MyBatis-Plus
Page<T> page = new Page<>(pageNum, pageSize);
mapper.selectPage(page, query);
```

## 注意事项

### 1. 分页参数校验

- `pageNum` 最小值为 1
- `pageSize` 范围限制为 1-100（通过 `@Max` 注解）
- 建议在 Service 层进行边界检查

### 2. 空结果处理

MyBatis-Plus 分页在无数据时返回空列表，不会返回 null：

```java
IPage<T> result = mapper.selectPage(page, query);
// result.getRecords() 永远不会是 null
// result.getTotal() 返回 0
```

### 3. ORDER BY 处理

XML 中使用 `ORDER BY` 时，字段名需要使用 `${}` 而不是 `#{}`：

```xml
<if test="query.orderByField != null and query.orderByField != ''">
    ORDER BY ${query.orderByField}
    <if test="query.orderByDirection != null">
        ${query.orderByDirection}
    </if>
</if>
```

## 项目模块分页使用情况

### 使用 MyBatis-Plus 分页的模块

| 模块 | Mapper | 分页方法 |
|------|--------|----------|
| 用户 | UserMapper | `selectPageWithDeptRole` |
| 素材 | AssetMapper | `selectPage` |
| 使用申请 | UsageApplyMapper | `selectPageWithUser` |
| 删除申请 | AssetDeletionApplicationMapper | `selectPage` |
| 素材申请 | MaterialApplicationMapper | `selectPage`, `selectPageWithDetails` |
| 使用日志 | UsageLogMapper | `selectPage`, `selectPageWithUser` |

### 保留 selectList 的模块（用于非分页场景）

| 模块 | Mapper | 用途 |
|------|--------|------|
| 素材 | AssetMapper | 导出、统计（无条件分页） |
| 通知 | NotificationMapper | Repository 层内部使用 |

## 常见问题

### Q1: 分页不生效？

检查以下内容：
1. 确保配置了 MyBatis-Plus 分页插件
2. Mapper 方法返回类型是 `IPage<T>` 不是 `Page<T>`
3. XML 中没有手动添加 `LIMIT` 子句

### Q2: 总数为 0 但有数据？

检查 `selectCount` 方法是否与 `selectPage` 使用相同的查询条件。

### Q3: 大数据量分页性能问题？

对于大数据量（百万级以上）：
- 考虑使用游标分页（`WHERE id > lastId`）
- 避免深度分页（`pageNum` 过大）
- 考虑使用覆盖索引

## 相关配置

### application.yml

```yaml
mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

### MyBatisConfig 配置类

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }
}
```
