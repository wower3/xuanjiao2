# 新环境部署指南

本文档说明将宣传教育平台部署到新环境时需要修改的配置项。

## 目录
- [环境要求](#环境要求)
- [数据库配置](#数据库配置)
- [后端服务配置](#后端服务配置)
- [前端服务配置](#前端服务配置)
- [内网环境特殊配置](#内网环境特殊配置)
- [部署检查清单](#部署检查清单)

---

## 环境要求

### 软件要求
| 软件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 1.8+ | 后端运行环境 |
| MySQL | 8.0+ | 数据库服务 |
| Node.js | 16+ | 前端构建环境 |
| Nginx | 1.20+ | 前端反向代理(生产环境) |

### 服务器要求
| 组件 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2核 | 4核+ |
| 内存 | 4GB | 8GB+ |
| 磁盘 | 50GB | 100GB+ |
| 带宽 | 1Mbps | 10Mbps+ |

---

## 数据库配置

### 1. 创建数据库
```sql
CREATE DATABASE xuanjiao_s DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. 导入初始化脚本
```bash
mysql -u root -p123456 < xuanjiao-backend/sql/init_complete.sql
```

### 3. 需要修改的数据库配置

**文件位置**: `xuanjiao-start/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xuanjiao_s?useUnicode=true&characterEncoding=utf8mb4
    username: root          # 修改为实际用户名
    password: 123456        # 修改为实际密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

| 配置项 | 说明 | 示例值 |
|-------|------|-------|
| `url` | 数据库连接地址 | `jdbc:mysql://192.168.1.100:3306/xuanjiao_s` |
| `username` | 数据库用户名 | `xuanjiao` |
| `password` | 数据库密码 | `your_password_here` |

---

## 后端服务配置

### 1. 配置文件位置
```
xuanjiao-start/src/main/resources/application.yml
```

### 2. 需要修改的配置项

```yaml
# 服务器配置
server:
  port: 8080                    # 修改为实际端口
  servlet:
    context-path: /api          # API路径前缀

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xuanjiao_s?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: root              # 数据库用户名
    password: 123456            # 数据库密码

# 文件上传配置
file:
  upload-path: D:/xuanjiao/uploads/    # 修改为实际文件存储路径
  max-size: 104857600                    # 最大上传大小(100MB)

# JWT配置
jwt:
  secret: your-jwt-secret-key-here       # 修改为复杂密钥
  expiration: 86400000                  # 过期时间(毫秒)
```

### 3. 完整配置示例

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://192.168.1.100:3306/xuanjiao_s?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: xuanjiao_user
    password: Xuanjiao@2024!
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000

file:
  upload-path: /data/xuanjiao/uploads/
  max-size: 104857600

jwt:
  secret: VGhpc0lzQVNlY3VyZUp3dFNlY3JldEtleUZvclRoZVBpY2tGb3JXaWRlbyVDaGVuZ2luZSUyMHdpdGglMjBXaWRlbyVDd2l0aCUyME1vbmdvREI=
  expiration: 86400000

mybatis:
  mapper-locations: classpath*:**/mapper/*.xml
  type-aliases-package: com.xuanjiao.infrastructure.dataobject
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 4. 后端启动

**开发环境**:
```bash
cd xuanjiao-backend
mvn spring-boot:run -pl xuanjiao-start
```

**生产环境**:
```bash
cd xuanjiao-backend
mvn clean package -DskipTests
java -jar xuanjiao-start/target/xuanjiao-start.jar
```

---

## 前端服务配置

### 1. 开发环境配置

**文件位置**: `vite.config.ts`

```typescript
export default defineConfig({
  server: {
    port: 3000,                    // 修改为实际端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080',  // 修改为后端实际地址
        changeOrigin: true
      }
    }
  }
})
```

### 2. 生产环境配置(内网/外网)

**创建环境变量文件**: `.env.production`

```bash
# API基础地址 - 根据内网/外网环境修改
VITE_API_BASE_URL=http://192.168.1.100:8080/api

# 或使用相对路径(推荐内网环境)
VITE_API_BASE_URL=/api
```

**文件位置**: `vite.config.ts`

```typescript
export default defineConfig({
  server: {
    port: 3000
  },
  base: './',                      // 使用相对路径
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})
```

### 3. Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /var/www/xuanjiao/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API代理 - 指向后端服务
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # 文件上传大小限制
        client_max_body_size 100M;
    }

    # 文件预览代理
    location /asset/ {
        proxy_pass http://127.0.0.1:8080/;
    }
}
```

---

## 内网环境特殊配置

### 1. 前后端地址配置

如果内网环境前后端地址不同，需要修改以下配置：

**前端API地址**:
```typescript
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://192.168.1.100:8080',  // 后端内网地址
        changeOrigin: true
      }
    }
  }
})
```

**生产环境Nginx**:
```nginx
# 外网用户访问前端，前端通过Nginx代理访问内网后端
location /api/ {
    proxy_pass http://192.168.1.100:8080/api/;  # 内网后端地址
}
```

### 2. 文件存储路径

如果内网有共享存储(如NAS/Samba)，需要配置：

```yaml
# 后端 application.yml
file:
  upload-path: \\192.168.1.200\share\uploads\   # Windows共享
  # 或
  upload-path: /mnt/nas/xuanjiao/uploads/        # Linux NFS
```

### 3. 数据库连接

内网数据库地址配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://192.168.1.100:3306/xuanjiao_s?useUnicode=true&characterEncoding=utf8mb4
    username: xuanjiao_user
    password: your_password
```

---

## 部署检查清单

### 部署前检查
- [ ] 数据库已创建并导入初始化数据
- [ ] 数据库用户权限已配置
- [ ] 文件存储目录已创建并有写权限
- [ ] 防火墙已开放所需端口(80, 443, 8080等)
- [ ] 域名已解析(如使用域名访问)

### 数据库配置
- [ ] `application.yml` 中的数据库连接地址正确
- [ ] 数据库用户名和密码正确
- [ ] 数据库名称 `xuanjiao_s` 存在

### 后端配置
- [ ] 服务端口未冲突
- [ ] JWT密钥已修改(生产环境)
- [ ] 文件上传路径正确
- [ ] 数据库连接池配置合理

### 前端配置
- [ ] API代理地址指向正确的后端
- [ ] 生产环境Nginx配置正确
- [ ] 静态资源路径正确

### 测试验证
- [ ] 后端服务启动正常
- [ ] 数据库连接成功
- [ ] 前端页面可访问
- [ ] 登录功能正常
- [ ] API接口可访问

---

## 常见问题排查

### 1. 数据库连接失败
```
检查项:
- 数据库服务是否启动
- 3306端口是否开放
- 用户名密码是否正确
- 数据库是否已创建
```

### 2. 文件上传失败
```
检查项:
- 上传目录是否有写权限
- 目录路径是否正确
- 文件大小是否超过限制
```

### 3. API接口无法访问
```
检查项:
- 后端服务是否启动
- 端口是否正确
- 跨域配置是否正确
- Nginx代理是否配置正确
```

### 4. 登录失败
```
检查项:
- 数据库中是否有测试用户
- 密码是否正确(默认: 123456)
- JWT配置是否正确
```

---

## 联系信息

如遇到问题，请联系系统管理员或查看日志文件进行排查。
