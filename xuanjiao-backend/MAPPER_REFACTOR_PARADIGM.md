# Mapper 重构范式文档（UserModule 参考标准）

**版本**: 1.0
**日期**: 2025-02-02
**参考模块**: User Module

---

## 一、重构目标

将 Infrastructure 层的 Mapper 从 `BaseMapper<T>` + 注解方式改造为 XML Mapper 方式。

### 改造前
```java
// Mapper.java
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
    // 无需声明方法，继承 BaseMapper
}

// App 层调用
LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(UserDO::getDeptId, deptId).eq(UserDO::getStatus, 1);
List<UserDO> users = userMapper.selectList(wrapper);
```

### 改造后
```java
// Mapper.java
@Mapper
public interface UserMapper {
    UserDO selectById(@Param("id") Long id);
    List<UserDO> selectList(UserQuery query);
    // 显式声明所有方法
}

// Query.java (新建)
@Data
public class UserQuery {
    private Long deptId;
    private Integer status;
    // 封装查询参数
}

// Mapper.xml (新建)
<select id="selectList" resultMap="BaseResultMap">
    SELECT ... FROM sys_user
    <where>
        <if test="deptId != null">AND dept_id = #{deptId}</if>
    </where>
</select>

// App 层调用
UserQuery query = new UserQuery();
query.setDeptId(deptId);
query.setStatus(1);
List<UserDO> users = userMapper.selectList(query);
```

---

## 二、重构步骤（标准流程）

### Step 1: 分析现有代码

#### 1.1 查找所有使用该 Mapper 的地方
```bash
# 在 app 层搜索 Mapper 使用
grep -r "roleMapper" xuanjiao-app/src/main/java/
```

#### 1.2 统计需要修改的调用点
| 文件 | 方法 | 调用方式 | 改造方式 |
|------|------|----------|----------|
| RoleServiceImpl.java | list() | selectList(null) | selectList(new RoleQuery()) |
| ApprovalServiceImpl.java | getTasks() | LambdaQueryWrapper | RoleQuery |

#### 1.3 分析查询模式
- 空查询（无条件）
- 单参数查询（eq）
- IN 查询（in）
- OR 条件查询（or）
- 组合查询

---

### Step 2: 创建 Query 对象

#### 2.1 文件位置
```
xuanjiao-infrastructure/src/main/java/com/xuanjiao/infrastructure/{module}/{Module}Query.java
```

#### 2.2 代码模板
```java
package com.xuanjiao.infrastructure.user;

import lombok.Data;
import java.util.List;

/**
 * 用户查询条件对象
 * 用于动态构建查询条件
 */
@Data
public class UserQuery {
    /** 主键ID */
    private Long id;

    /** 部门ID */
    private Long deptId;

    /** 部门ID列表（IN查询） */
    private List<Long> deptIds;

    /** 用户状态（0:禁用, 1:启用） */
    private Integer status;

    /** 删除标记（0:未删除, 1:已删除） */
    private Integer deleted;

    /** 用户名关键字（模糊查询） */
    private String usernameKeyword;

    /** 真实姓名关键字（模糊查询） */
    private String realNameKeyword;

    /** 通用关键字（同时搜索username和realName） */
    private String keyword;

    /** 用户ID列表（IN查询） */
    private List<Long> userIds;

    /** 角色ID列表（IN查询） */
    private List<Long> roleIds;
}
```

#### 2.3 字段设计原则
- 与数据库字段对应
- 支持单值和列表（IN 查询）
- 字符串字段同时支持 null 和空字符串判断

---

### Step 3: 创建 Mapper XML 文件

#### 3.1 文件位置
```
xuanjiao-infrastructure/src/main/resources/mapper/{Module}Mapper.xml
```

#### 3.2 XML 模板
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.xuanjiao.infrastructure.user.UserMapper">

    <!-- 结果映射：column是数据库字段（下划线），property是Java属性（驼峰） -->
    <resultMap id="BaseResultMap" type="com.xuanjiao.infrastructure.dataobject.UserDO">
        <id column="id" property="id" jdbcType="BIGINT"/>
        <result column="username" property="username" jdbcType="VARCHAR"/>
        <result column="real_name" property="realName" jdbcType="VARCHAR"/>
        <result column="dept_id" property="deptId" jdbcType="BIGINT"/>
        <result column="role_id" property="roleId" jdbcType="BIGINT"/>
        <!-- ... 更多字段 -->
    </resultMap>

    <!-- 基础字段列表（数据库字段，下划线命名） - 禁止 SELECT * -->
    <sql id="Base_Column_List">
        id, username, password, real_name, email, phone, dept_id, role_id,
        status, create_time, update_time, deleted
    </sql>

    <!-- 根据主键查询 -->
    <select id="selectById" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_user
        <where>
            id = #{id}
            AND deleted = 0
        </where>
        LIMIT 1
    </select>

    <!-- 动态条件查询 -->
    <select id="selectList" resultMap="BaseResultMap">
        SELECT <include refid="Base_Column_List"/>
        FROM sys_user
        <where>
            <!-- deleted 参数处理 -->
            <if test="deleted != null">
                deleted = #{deleted}
            </if>
            <if test="deleted == null">
                deleted = 0
            </if>

            <!-- 单值参数 -->
            <if test="roleId != null">
                AND role_id = #{roleId}
            </if>
            <if test="deptId != null">
                AND dept_id = #{deptId}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>

            <!-- IN 查询 -->
            <if test="roleIds != null and roleIds.size() > 0">
                AND role_id IN
                <foreach collection="roleIds" item="roleId" open="(" separator="," close=")">
                    #{roleId}
                </foreach>
            </if>
            <if test="deptIds != null and deptIds.size() > 0">
                AND dept_id IN
                <foreach collection="deptIds" item="deptId" open="(" separator="," close=")">
                    #{deptId}
                </foreach>
            </if>

            <!-- 字符串模糊查询 -->
            <if test="usernameKeyword != null and usernameKeyword != ''">
                AND username LIKE CONCAT('%', #{usernameKeyword}, '%')
            </if>
            <if test="realNameKeyword != null and realNameKeyword != ''">
                AND real_name LIKE CONCAT('%', #{realNameKeyword}, '%')
            </if>

            <!-- OR 条件查询 -->
            <if test="keyword != null and keyword != ''">
                AND (username LIKE CONCAT('%', #{keyword}, '%')
                     OR real_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
        </where>
        ORDER BY id
    </select>

    <!-- 插入 -->
    <insert id="insert" parameterType="com.xuanjiao.infrastructure.dataobject.UserDO"
            useGeneratedKeys="true" keyProperty="id">
        INSERT INTO sys_user (
            username, password, real_name, email, phone, dept_id, role_id,
            status, create_time, update_time, deleted
        ) VALUES (
            #{username}, #{password}, #{realName}, #{email}, #{phone}, #{deptId}, #{roleId},
            #{status}, #{createTime}, #{updateTime}, #{deleted}
        )
    </insert>

    <!-- 更新 -->
    <update id="updateById" parameterType="com.xuanjiao.infrastructure.dataobject.UserDO">
        UPDATE sys_user
        <set>
            <if test="username != null and username != ''">username = #{username},</if>
            <if test="password != null and password != ''">password = #{password},</if>
            <if test="realName != null and realName != ''">real_name = #{realName},</if>
            <if test="email != null">email = #{email},</if>
            <if test="phone != null">phone = #{phone},</if>
            <if test="deptId != null">dept_id = #{deptId},</if>
            <if test="roleId != null">role_id = #{roleId},</if>
            <if test="status != null">status = #{status},</if>
            <if test="updateTime != null">update_time = #{updateTime},</if>
            <if test="deleted != null">deleted = #{deleted},</if>
        </set>
        <where>
            id = #{id}
        </where>
    </update>

    <!-- 删除（逻辑删除） -->
    <update id="deleteById">
        UPDATE sys_user
        <set>
            deleted = 1
        </set>
        <where>
            id = #{id}
        </where>
    </update>
</mapper>
```

#### 3.3 XML 编写规范

| 规范 | 要求 | 示例 |
|------|------|------|
| **禁止 SELECT *** | 必须显式列出所有字段 | `SELECT id, username, ...` |
| **resultMap 映射** | column 用 snake_case，property 用 camelCase | `<result column="real_name" property="realName"/>` |
| **WHERE 子句** | 使用 `<where></where>` 标签 | `<where><if test="...">AND ...</if></where>` |
| **非字符串判空** | 只判断 null | `<if test="status != null">` |
| **字符串判空** | 判断 null 和空字符串 | `<if test="keyword != null and keyword != ''">` |
| **IN 查询** | 使用 `<foreach>` + size 判断 | `<if test="list != null and list.size() > 0">` |
| **默认值处理** | null 时应用默认值 | `<if test="deleted == null">deleted = 0</if>` |

---

### Step 4: 修改 Mapper 接口

#### 4.1 移除 BaseMapper 继承
```java
// 改造前
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}

// 改造后
@Mapper
public interface UserMapper {
    UserDO selectById(@Param("id") Long id);
    UserDO selectOneByUsername(@Param("username") String username);
    List<UserDO> selectList(UserQuery query);
    int insert(UserDO user);
    int updateById(UserDO user);
    int deleteById(@Param("id") Long id);
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}
```

#### 4.2 显式声明所有方法
- 每个方法添加 `@Param` 注解（单参数可省略）
- Query 对象参数不需要 `@Param`

---

### Step 5: 修改 App 层调用

#### 5.1 Repository 实现类
```java
// 改造前
public UserDO selectOne(String username) {
    LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(UserDO::getUsername, username);
    return userMapper.selectOne(wrapper);
}

// 改造后
public UserDO selectOne(String username) {
    return userMapper.selectOneByUsername(username);
}
```

#### 5.2 Service 实现类 - 空查询
```java
// 改造前
List<UserDO> allUsers = userMapper.selectList(null);

// 改造后
List<UserDO> allUsers = userMapper.selectList(new UserQuery());
```

#### 5.3 Service 实现类 - 单条件查询
```java
// 改造前
LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(UserDO::getDeptId, deptId).eq(UserDO::getStatus, 1);
List<UserDO> users = userMapper.selectList(wrapper);

// 改造后
UserQuery query = new UserQuery();
query.setDeptId(deptId);
query.setStatus(1);
List<UserDO> users = userMapper.selectList(query);
```

#### 5.4 Service 实现类 - IN 查询
```java
// 改造前
LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
wrapper.in(UserDO::getRoleIds, roleIds).eq(UserDO::getStatus, 1);
List<UserDO> users = userMapper.selectList(wrapper);

// 改造后
UserQuery query = new UserQuery();
query.setRoleIds(roleIds);
query.setStatus(1);
List<UserDO> users = userMapper.selectList(query);
```

#### 5.5 Service 实现类 - 组合查询
```java
// 改造前
LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
wrapper.in(UserDO::getId, userIdSet);
if (keyword != null && !keyword.trim().isEmpty()) {
    wrapper.and(w -> w.like(UserDO::getUsername, keyword)
                        .or()
                        .like(UserDO::getRealName, keyword));
}
List<UserDO> users = userMapper.selectList(wrapper);

// 改造后
UserQuery query = new UserQuery();
query.setUserIds(new ArrayList<>(userIdSet));
if (keyword != null && !keyword.trim().isEmpty()) {
    query.setKeyword(keyword.trim());
}
List<UserDO> users = userMapper.selectList(query);
```

---

### Step 6: 编译验证

```bash
cd xuanjiao-backend
mvn clean compile -DskipTests
```

**检查项**：
- [ ] 编译成功，无错误
- [ ] 无 "Type mismatch" 错误
- [ ] 无 "Cannot find symbol" 错误

---

### Step 7: 功能验证

#### 7.1 应用启动验证
```bash
mvn spring-boot:run -pl xuanjiao-start
```

**检查项**：
- [ ] 应用成功启动
- [ ] 无 MyBatis mapper 错误
- [ ] 无 Bean 创建错误

#### 7.2 API 测试（必需）
```bash
# 1. 启动应用（如果未运行）
cd xuanjiao-backend
mvn spring-boot:run -pl xuanjiao-start

# 2. 获取 Token
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | \
  grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 3. 测试列表接口
echo "=== Test 1: List ==="
RESULT=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/{module}/list" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}')
HTTP_CODE=$(echo "$RESULT" | tail -n1)
echo "$RESULT" | head -n1 | grep -q '"code":200' && echo "PASS" || echo "FAIL"

# 4. 测试详情接口
echo "=== Test 2: Get Detail ==="
RESULT=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/{module}/getDetail" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"id":1}')
HTTP_CODE=$(echo "$RESULT" | tail -n1)
echo "$RESULT" | head -n1 | grep -q '"code":200' && echo "PASS" || echo "FAIL"

# 5. 测试带筛选条件的接口
echo "=== Test 3: Query with Filter ==="
RESULT=$(curl -s -w "\n%{http_code}" -X POST "http://localhost:8080/api/{module}/list" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10,"status":"APPROVED"}')
HTTP_CODE=$(echo "$RESULT" | tail -n1)
echo "$RESULT" | head -n1 | grep -q '"code":200' && echo "PASS" || echo "FAIL"
```

**API 测试覆盖要求**：

| 接口类型 | 测试内容 | 验证点 |
|---------|---------|--------|
| 列表接口 | 空查询 | 返回200，数据结构正确 |
| 详情接口 | 按 ID 查询 | 返回200，字段映射正确 |
| 筛选接口 | 带条件查询 | 筛选条件生效 |
| 特殊接口 | 模块特有功能 | 业务逻辑正确 |

**检查项**：
- [ ] 至少测试 3 个主要 API 接口
- [ ] 所有 API 返回 200 状态码
- [ ] 数据结构与改造前一致
- [ ] 字段映射正确（snake_case → camelCase）

#### 7.3 SQL 直接验证
```bash
mysql -u root -p123456 xuanjiao_s << 'EOF'
-- 验证查询条件
SELECT id, username, real_name FROM sys_user WHERE deleted = 0 LIMIT 5;
EOF
```

**检查项**：
- [ ] SQL 查询结果与 API 返回一致
- [ ] 字段数量和内容匹配

---

### Step 8: 单元测试（必需）

测试策略：**集成测试放在 start 模块，单元测试放在 app 模块**

#### 8.1 测试文件位置

```
xuanjiao-app/src/test/java/com/xuanjiao/app/{module}/
├── {Module}ServiceImplTest.java     # Service 层单元测试
└── ...
```

#### 8.2 测试依赖配置（pom.xml）

```xml
<!-- xuanjiao-app/pom.xml -->
<dependencies>
    <!-- Test Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>2.22.2</version>
        </plugin>
    </plugins>
</build>
```

#### 8.3 测试模板（Service 层单元测试）

```java
package com.xuanjiao.app.user;

import com.xuanjiao.client.user.UserDTO;
import com.xuanjiao.infrastructure.dataobject.UserDO;
import com.xuanjiao.infrastructure.user.UserMapper;
import com.xuanjiao.infrastructure.user.UserQuery;
import com.xuanjiao.app.user.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 验证 UserMapper 重构后 UserService 功能正确
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDO testUser;

    @BeforeEach
    public void setUp() {
        testUser = new UserDO();
        testUser.setId(1L);
        testUser.setUsername("test_user");
        testUser.setRealName("测试用户");
        testUser.setStatus(1);
    }

    @Test
    @Order(1)
    public void testList_EmptyQuery() {
        // 测试空查询
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        List<UserDTO> result = userService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userMapper).selectList(argThat(query ->
            query != null  // 验证 Query 对象被正确创建
        ));
        System.out.println("✓ UserService.list() - 空查询测试通过");
    }

    @Test
    @Order(2)
    public void testList_WithStatusFilter() {
        // 测试带状态筛选的查询
        when(userMapper.selectList(any(UserQuery.class)))
                .thenReturn(Arrays.asList(testUser));

        List<UserDTO> result = userService.list();

        assertNotNull(result);
        // 验证 UserQuery 被正确调用
        verify(userMapper).selectList(argThat(query ->
            query != null && query.getStatus() == 1
        ));
        System.out.println("✓ UserService.list(status筛选) - 测试通过");
    }
}
```

#### 8.4 测试覆盖要求

每个 ServiceImpl 需要测试的 Mapper 调用点：

| 测试类型 | 测试内容 | 示例 |
|---------|---------|------|
| 空查询 | `selectList(new Query())` | list() 方法 |
| 单条件 | `query.setField(value)` | 按 status 查询 |
| IN 查询 | `query.setIds(Arrays.asList(...))` | 按 roleIds 查询 |
| 组合查询 | 多个条件组合 | deptId + status |
| 关键词搜索 | `query.setKeyword(keyword)` | 模糊搜索 |

#### 8.5 私有方法测试（使用反射）

```java
@Test
public void testPrivateMethod() throws Exception {
    // 使用反射测试私有方法
    Method method = WorkflowEngineServiceImpl.class.getDeclaredMethod(
        "getActualApproverIds", StageApproverDO.class, Long.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<Long> result = (List<Long>) method.invoke(workflowEngineService, approver, 1L);

    assertNotNull(result);
}
```

#### 8.6 运行测试

```bash
# 运行所有测试
cd xuanjiao-backend/xuanjiao-app
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 运行特定测试方法
mvn test -Dtest=UserServiceTest#testList_EmptyQuery
```

#### 8.7 测试验证标准

- [ ] 所有测试通过（Tests run: N, Failures: 0, Errors: 0）
- [ ] 覆盖所有 Mapper 调用点
- [ ] 验证 Query 参数正确传递
- [ ] 使用 argThat 验证查询条件

---

### Step 9: 前端验证

#### 9.1 浏览器测试
```
1. 启动前端: cd xuanjiao-frontend && npm run dev
2. 访问相关页面
3. F12 → Network 查看请求
```

**检查项**：
- [ ] 页面正常加载
- [ ] 数据正确显示
- [ ] 筛选功能正常
- [ ] 增删改查功能正常

#### 9.2 Vue DevTools 检查
```
1. F12 → Vue DevTools
2. 查看组件状态
3. 查看 Pinia Store
```

---

## 三、验证规范（Checklist）

### 3.1 代码质量检查

| 检查项 | 标准 | 检查方法 |
|--------|------|----------|
| 无 SELECT * | 所有 SELECT 显式列出字段 | 检查 XML 文件 |
| resultMap 映射 | column snake_case, property camelCase | 检查 resultMap 标签 |
| WHERE 规范 | 使用 `<where></where>` 标签 | 检查 XML 文件 |
| null 判断 | 非字符串: `!= null`, 字符串: `!= null and != ''` | 检查 `<if>` 标签 |
| IN 查询 | 先判断 `size() > 0` | 检查 `<foreach>` 前的条件 |
| 默认值 | null 时应用业务默认值 | 检查 deleted、status 等字段 |

### 3.2 编译检查

```bash
mvn clean compile -DskipTests
```

| 检查项 | 预期结果 |
|--------|----------|
| 编译状态 | BUILD SUCCESS |
| 错误数 | 0 |
| 警告数 | 0（或仅无关警告） |

### 3.3 运行时检查

| 检查项 | 预期结果 |
|--------|----------|
| 应用启动 | 成功启动，端口监听 |
| MyBatis 初始化 | mapper XML 成功加载 |
| Bean 创建 | 无错误 |

### 3.4 功能检查

| 检查项 | 测试方法 | 预期结果 |
|--------|----------|----------|
| 列表查询 | API 调用 | 返回正确数据 |
| 筛选功能 | 带参数 API 调用 | 筛选条件生效 |
| IN 查询 | 多值参数 | 返回匹配结果 |
| 分页功能 | pageNum/pageSize | 分页正确 |
| 字段映射 | 检查响应 JSON | snake_case → camelCase |

### 3.5 数据一致性检查

| 检查项 | 对比方法 | 预期结果 |
|--------|----------|----------|
| 记录数 | SQL COUNT vs API total | 一致 |
| 字段值 | SQL 查询 vs API 返回 | 一致 |
| 排序 | SQL ORDER BY vs API 顺序 | 一致 |

---

## 四、文档输出

### 4.1 创建的文件

| 文件 | 模板 | 说明 |
|------|------|------|
| `{Module}Query.java` | 见 Step 2 | 查询参数对象 |
| `{Module}Mapper.xml` | 见 Step 3 | SQL 映射文件 |

### 4.2 修改的文件

| 文件 | 修改内容 | 影响范围 |
|------|----------|----------|
| `{Module}Mapper.java` | 移除 BaseMapper，添加方法声明 | 1 个文件 |
| `{Module}RepositoryImpl.java` | LambdaQueryWrapper → 直接调用 | 1-3 处 |
| `*ServiceImpl.java` | 构建对象替代 Lambda | N 处调用点 |

### 4.3 测试文件

| 文件 | 说明 |
|------|------|
| `verify_{module}_api.sh` | API 自动化测试脚本 |
| `{module}_test_cases.md` | 手动测试用例 |

### 4.4 更新的文档

| 文档 | 更新内容 |
|------|----------|
| `MAPPER_REFACTOR_LOG.md` | 模块完成状态 |
| `MAPPER_REFACTOR_VERIFICATION_REPORT.md` | 验证结果（如有） |

---

## 五、常见问题与解决方案

### 5.1 编译错误

**问题**: `void cannot be converted to {Type}`
```
// 错误示例
UserDO saved = repository.save(user);  // save() 返回 void
```

**解决**: 检查 Repository.save() 的返回类型
```java
// 如果返回 void
repository.save(user);
// 使用传入的 user 对象

// 如果返回实体
UserDO saved = repository.save(user);
```

### 5.2 运行时错误

**问题**: `Invalid bound statement (not found)`

**原因**: XML 文件位置或 namespace 不匹配

**解决**:
1. 检查 XML 文件路径：`resources/mapper/{Module}Mapper.xml`
2. 检查 namespace：`<mapper namespace="com.xuanjiao.infrastructure.{module}.{Module}Mapper">`
3. 检查方法名：XML 中的 id 与 Mapper 接口方法名一致

### 5.3 查询结果为空

**问题**: 改造后查询返回空列表

**可能原因**:
1. Query 对象未设置值
2. XML 中条件判断错误
3. resultMap 映射错误

**解决**:
1. 打印 SQL 日志（MyBatis 配置）
2. 对比改造前后的 SQL
3. 检查 Query 对象字段值

### 5.4 字段值为 null

**问题**: 响应中某些字段为 null

**可能原因**:
1. resultMap 未映射该字段
2. 数据库字段名与 column 不匹配

**解决**:
1. 检查 resultMap 是否包含所有字段
2. 检查 column 名称是否与数据库一致

---

## 六、重构统计

### UserModule 重构统计（参考标准）

| 项目 | 数量 |
|------|------|
| 创建文件 | 2 个（Query、XML） |
| 修改文件 | 6 个 |
| selectList 调用点 | 14 处 |
| 总代码改动 | ~50 行 |
| 耗时 | ~2 小时 |

### 下一个模块预估

| 模块 | 预估调用点 | 预估耗时 |
|------|-----------|----------|
| Role | ~10 | ~1.5 小时 |
| Dept | ~8 | ~1.5 小时 |
| Asset | ~15 | ~3 小时 |

---

## 七、完成标准

一个模块重构完成的标准：

- [ ] **代码完成**: Query.java 和 Mapper.xml 已创建
- [ ] **编译通过**: mvn clean compile 成功
- [ ] **启动成功**: 应用正常启动
- [ ] **API 验证**: 至少测试 3 个主要接口
- [ ] **前端验证**: 相关页面功能正常
- [ ] **文档更新**: MAPPER_REFACTOR_LOG.md 已更新
- [ ] **代码审查**: 符合 XML 编写规范

---

## 八、快速参考卡片

### XML 动态 SQL 模板

```xml
<!-- 单条件 -->
<if test="field != null">AND field = #{field}</if>

<!-- 字符串条件 -->
<if test="strField != null and strField != ''">AND str_field LIKE CONCAT('%', #{strField}, '%')</if>

<!-- IN 查询 -->
<if test="ids != null and ids.size() > 0">
    AND id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</if>

<!-- OR 条件 -->
<if test="keyword != null and keyword != ''">
    AND (field1 LIKE CONCAT('%', #{keyword}, '%')
         OR field2 LIKE CONCAT('%', #{keyword}, '%'))
</if>
```

### Query 对象模板

```java
@Data
public class XxxQuery {
    // 单值
    private Long id;
    private Integer status;

    // 列表（IN 查询）
    private List<Long> ids;

    // 字符串（模糊查询）
    private String keyword;
}
```

---

**文档维护**: 每完成一个模块，更新相关统计和验证规范。
