# SonarQube 代码质量检查使用指南

## 方式一：本地 SonarQube 扫描（完整报告）

### 第一步：启动 SonarQube 服务器

双击运行 `start-sonarqube.bat`

等待 1-2 分钟后访问：http://localhost:9000

默认登录账号：
- 用户名：`admin`
- 密码：`admin`

### 第二步：执行代码扫描

双击运行 `scan-sonarqube.bat`

扫描完成后，在 SonarQube 控制台查看结果：
- 项目地址：http://localhost:9000/dashboard?id=xuanjiao-backend
- 查看问题、代码覆盖率、安全漏洞等

---

## 方式二：IDE 插件方式（推荐给开发者）

### IntelliJ IDEA

1. **安装插件**
   - `File` → `Settings` → `Plugins`
   - 搜索并安装 `SonarLint`

2. **配置规则**
   - `File` → `Settings` → `Tools` → `SonarLint`
   - 选择 `Use connected mode` 或 `Use standalone mode`
   - Java 项目选择内置规则集

3. **运行扫描**
   - 右键项目 → `Analyze` → `Analyze with SonarLint`
   - 或使用快捷键：`Ctrl + Shift + S`

4. **查看结果**
   - 底部工具栏会显示 SonarLint 面板
   - 显示代码问题、安全漏洞、代码异味等

### VS Code

1. **安装扩展**
   ```
   code --install-extension SonarSource.sonarlint-vscode
   ```

2. **配置**
   - 打开设置，搜索 "sonarlint"
   - 配置 Java 分析规则

3. **扫描**
   - 打开 Java 文件后自动分析
   - 或使用命令面板：`Ctrl + Shift + P` → "SonarLint: Analyze All Files"

---

## 方式三：命令行快速扫描（无需启动服务器）

如果您已启动 SonarQube 服务器，可以直接运行：

```bash
cd xuanjiao-backend
mvn clean compile sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin
```

---

## 常见问题

### 1. Docker 未安装
- 下载 Docker Desktop：https://www.docker.com/products/docker-desktop/
- 安装后重启电脑

### 2. 端口 9000 被占用
- 修改 `start-sonarqube.bat` 中的端口号：`-p 9090:9000`
- 同时修改 `scan-sonarqube.bat` 中的 URL：`-Dsonar.host.url=http://localhost:9090`

### 3. 内存不足
- 修改 `start-sonarqube.bat` 中的 JVM 参数：
  ```
  -e SONAR_JAVA_OPTS="-Xmx1024m -Xms256m"
  ```

### 4. 扫描超时
- 增加超时时间，在扫描命令中添加：
  ```
  -Dsonar.ws.timeout=600
  ```

---

## 扫描报告说明

SonarQube 会检查以下方面：

### 代码质量
- **Bug**: 潜在的错误
- **Vulnerability**: 安全漏洞
- **Code Smell**: 代码异味（可维护性问题）

### 规则示例
- 空指针检查
- 资源未关闭
- 复杂度过高
- 重复代码
- 命名规范
- 注释规范

### 质量阈（Quality Gate）
- 代码覆盖率 >= 80%
- 新增代码问题 = 0
- 安全漏洞 = 0
- 阻塞问题 = 0

---

## 自动化集成（CI/CD）

### GitLab CI

```yaml
sonarqube-check:
  stage: test
  image: maven:3.8-openjdk-8
  script:
    - mvn sonar:sonar
      -Dsonar.host.url=$SONAR_URL
      -Dsonar.login=$SONAR_TOKEN
  only:
    - merge_requests
    - master
```

### Jenkins Pipeline

```groovy
stage('SonarQube Analysis') {
    steps {
        script {
            def scannerHome = tool 'SonarQube Scanner'
            withSonarQubeEnv('SonarQube') {
                sh "${scannerHome}/bin/sonar-scanner"
            }
        }
    }
}
```
