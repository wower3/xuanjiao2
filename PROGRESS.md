# 宣传教育平台 - 智能体开发坑

## 一、后端项目

### 1.数据库操作相关

模型倾向使用mybatis plus的baseMapper作为操作数据库的对象，但是行内倾向使用原生mybatis，即使用mapper.xml，因此需要提供给大模型这个背景信息：
- MyBatis (Native XML Mapper approach - no BaseMapper)
- MyBatis uses native XML mappers with explicitly defined methods (no BaseMapper inheritance)

#### 1.1 MyBatis 开发规范

##### Mapper 接口定义

**所有 Mapper 必须显式定义方法，不继承 BaseMapper**

```java
@Mapper
public interface AssetMapper {
    // 基础 CRUD 方法
    AssetDO selectById(@Param("id") Long id);
    AssetDO selectOne(AssetQuery query);
    List<AssetDO> selectList(AssetQuery query);
    Long selectCount(AssetQuery query);
    IPage<AssetDO> selectPage(Page<AssetDO> page, @Param("query") AssetQuery query);
    int insert(AssetDO assetDO);
    int updateById(AssetDO assetDO);

    // 业务特定方法
    AssetDO selectByMd5(@Param("md5") String md5);
    List<AssetDO> selectByApplicationId(@Param("applicationId") Long applicationId);
}
```

##### Mapper XML 开发规范

###### ResultMap 定义
```xml
<resultMap id="BaseResultMap" type="com.xuanjiao.infrastructure.dataobject.AssetDO">
    <id column="id" property="id" jdbcType="BIGINT"/>
    <result column="name" property="name" jdbcType="VARCHAR"/>
    <result column="type" property="type" jdbcType="VARCHAR"/>
    <!-- 明确映射 column (数据库) 到 property (Java) -->
</resultMap>
```

###### 列定义规范
```xml
<sql id="Base_Column_List">
    id, name, type, file_path, thumbnail_path, file_size,
    md5, status, deleted, create_time, update_time
</sql>
```

**禁止使用 `SELECT *`**

###### 动态 SQL 规范
```xml
<!-- WHERE 子句：必须使用 <where> 标签 -->
<select id="selectList" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM asset
    <where>
        <if test="id != null">
            AND id = #{id}
        </if>
        <if test="name != null and name != ''">
            AND name = #{name}
        </if>
        <if test="deleted != null">
            AND deleted = #{deleted}
        </if>
        <if test="deletedIsNull != null and deletedIsNull">
            AND deleted IS NULL
        </if>
        <if test="statusIn != null and statusIn.size() > 0">
            AND status IN
            <foreach collection="statusIn" item="status" open="(" separator="," close=")">
                #{status}
            </foreach>
        </if>
    </where>
    ORDER BY create_time DESC
</select>
```

###### UPDATE 语句规范
```xml
<!-- SET 子句：使用 <set> 标签 -->
<update id="updateById" parameterType="com.xuanjiao.infrastructure.dataobject.AssetDO">
    UPDATE asset
    <set>
        <if test="name != null and name != ''">name = #{name},</if>
        <if test="type != null and type != ''">type = #{type},</if>
        <if test="status != null and status != ''">status = #{status},</if>
        <if test="deleted != null">deleted = #{deleted},</if>
    </set>
    WHERE id = #{id}
</update>
```

###### INSERT 语句规范
```xml
<!-- 明确列出所有字段 -->
<insert id="insert" parameterType="com.xuanjiao.infrastructure.dataobject.AssetDO"
        useGeneratedKeys="true" keyProperty="id">
    INSERT INTO asset (
        name, type, file_path, thumbnail_path, file_size,
        md5, status, deleted, create_time, update_time
    ) VALUES (
        #{name}, #{type}, #{filePath}, #{thumbnailPath}, #{fileSize},
        #{md5}, #{status}, #{deleted}, #{createTime}, #{updateTime}
    )
</insert>
```

##### 特殊查询场景

###### IS NULL / IS NOT NULL 查询
```java
// Query 对象中添加 Boolean 字段
@Data
public class ApprovalProgressQuery {
    private Long parentInstanceId;
    private Boolean parentInstanceIdIsNull;  // IS NULL 查询
}
```

```xml
<if test="parentInstanceIdIsNull != null and parentInstanceIdIsNull">
    AND parent_instance_id IS NULL
</if>
<if test="parentInstanceIdIsNull != null and !parentInstanceIdIsNull">
    AND parent_instance_id IS NOT NULL
</if>
```

###### IN 查询
```java
@Data
public class AssetQuery {
    private List<String> statusIn;
}
```

```xml
<if test="statusIn != null and statusIn.size() > 0">
    AND status IN
    <foreach collection="statusIn" item="status" open="(" separator="," close=")">
        #{status}
    </foreach>
</if>
```

###### != 查询
```java
@Data
public class ApprovalTaskQuery {
    private Long idNotEqual;  // != 查询
}
```

```xml
<if test="idNotEqual != null">
    AND id != #{idNotEqual}
</if>
```

##### 分页查询规范

```java
// Mapper 接口
IPage<AssetDO> selectPage(Page<AssetDO> page, @Param("query") AssetQuery query);

// 调用方式
IPage<AssetDO> page = assetMapper.selectPage(
    new Page<>(pageNum, pageSize),
    query
);
```

##### 强制设置字段为 NULL

**问题**: `updateById()` 方法无法将字段更新为 NULL

**解决方案**: 使用显式 XML 方法

```java
// Mapper 接口添加方法
int resetApprovers(@Param("id") Long id);

// Mapper XML
<update id="resetApprovers">
    UPDATE approval_progress
    SET approvers = NULL
    WHERE id = #{id}
</update>
```

##### 字段映射规范

**column**: 数据库字段名（下划线命名，如 `role_id`）
**property**: Java 属性名（驼峰命名，如 `roleId`）

```xml
<result column="role_id" property="roleId" jdbcType="BIGINT"/>
<result column="create_time" property="createTime" jdbcType="TIMESTAMP"/>
```

### 2.分页相关
可以明确规定分页使用Ipage，每页默认值为10，最大可以设计成100，设计BasePageQry，设计分页查询的对象继承该类。

### 3.infrastructure中的实体类型
使用Query对象作为查询条件对象，使用DO类作为接受结果对象。
