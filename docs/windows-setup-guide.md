# Windows 环境配置指南

> 本文档详细说明在 Windows 10/11 上搭建京东风格电商平台开发环境的完整步骤。
> 如果你希望快速配置，可以直接运行 `scripts/setup-env.bat`（一键配置脚本），本指南供手动配置或排查问题时参考。

---

## 目录

- [1. 前置环境清单](#1-前置环境清单)
- [2. JDK 17+ 安装与配置](#2-jdk-17-安装与配置)
- [3. Maven 安装与配置](#3-maven-安装与配置)
- [4. MySQL 8.0+ 安装与配置](#4-mysql-80-安装与配置)
- [5. Node.js 18+ 安装](#5-nodejs-18-安装)
- [6. VS Code 推荐插件](#6-vs-code-推荐插件)
- [7. 数据库导入](#7-数据库导入)
- [8. 启动项目](#8-启动项目)
- [9. 常见问题排查](#9-常见问题排查)

---

## 1. 前置环境清单

| 组件 | 最低版本 | 用途 |
|------|----------|------|
| JDK | 17 | 后端 Spring Boot 编译运行 |
| Maven | 3.8 | 后端依赖管理与构建 |
| MySQL | 8.0 | 数据库 |
| Node.js | 18 | 前端构建与开发服务器 |
| npm | 随 Node.js 安装 | 前端包管理 |
| Git | 2.30+ | 版本管理（可选） |
| VS Code | 最新版 | 推荐 IDE |

> **快速检查**：运行 `scripts\check-env.bat` 可一键检测以上环境是否已正确安装。

---

## 2. JDK 17+ 安装与配置

### 2.1 下载

推荐使用 Eclipse Temurin（免费、开源）：

- 下载地址：https://adoptium.net/temurin/releases/?version=17
- 选择 **Windows x64** 的 `.msi` 安装包

### 2.2 安装

1. 运行下载的 `.msi` 安装包
2. 安装向导中勾选 **Set JAVA_HOME variable** 和 **Add to PATH**（默认已勾选）
3. 记住安装路径（默认 `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`）
4. 完成安装

### 2.3 验证

打开**新的** CMD 窗口（安装后需重开终端才能生效）：

```cmd
java -version
```

应显示类似：

```
openjdk version "17.0.x" 2024-xx-xx
```

### 2.4 手动配置环境变量（如安装时未自动配置）

1. 右键「此电脑」→「属性」→「高级系统设置」→「环境变量」
2. 在「系统变量」中点击「新建」：
   - 变量名：`JAVA_HOME`
   - 变量值：`C:\Program Files\Eclipse Adoptium\jdk-17.x.x`（你的实际安装路径）
3. 找到「系统变量」中的 `Path`，点击「编辑」，新增一行：
   - `%JAVA_HOME%\bin`
4. 确定保存，重新打开 CMD 验证

---

## 3. Maven 安装与配置

### 3.1 下载

- 下载地址：https://maven.apache.org/download.cgi
- 下载 **Binary zip archive**（`apache-maven-3.x.x-bin.zip`）

### 3.2 安装

1. 将 zip 包解压到一个无空格无中文的路径，例如 `C:\maven\`（最终结构为 `C:\maven\apache-maven-3.x.x\`）
2. 配置环境变量：
   - 新建系统变量 `MAVEN_HOME`，值为 `C:\maven\apache-maven-3.x.x`
   - 在 `Path` 中新增 `%MAVEN_HOME%\bin`

### 3.3 验证

重新打开 CMD：

```cmd
mvn -v
```

应显示类似：

```
Apache Maven 3.9.x
```

### 3.4 配置国内镜像（推荐）

编辑 `%USERPROFILE%\.m2\settings.xml`（如不存在则创建），添加阿里云镜像加速依赖下载：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

---

## 4. MySQL 8.0+ 安装与配置

### 4.1 下载

- 下载地址：https://dev.mysql.com/downloads/installer/
- 推荐下载 **MySQL Installer for Windows**（MSI 安装包）

### 4.2 安装

1. 运行 MySQL Installer
2. 选择 **Custom** 安装类型，安装 **MySQL Server** 和 **MySQL Workbench**（可选 GUI 工具）
3. 配置类型选择 **Development Computer**（开发机）
4. 认证方式建议选择 **Use Strong Password Encryption**
5. 设置 root 密码（**请牢记此密码**，后端配置中需要使用）
6. Windows Service 名称保持默认 `MySQL80`，勾选 **Start at System Startup**
7. 完成安装

### 4.3 验证

打开 CMD：

```cmd
mysql --version
```

应显示类似：

```
mysql  Ver 8.0.xx for Win64
```

### 4.4 确认服务运行

打开「服务」（`services.msc`），确认 `MySQL80` 服务状态为「正在运行」。

或在 CMD 中检查：

```cmd
sc query MySQL80
```

`STATE` 应为 `RUNNING`。

---

## 5. Node.js 18+ 安装

### 5.1 下载

- 下载地址：https://nodejs.org/
- 推荐下载 **LTS 版本**（长期支持版）

### 5.2 安装

1. 运行 `.msi` 安装包
2. 勾选 **Add to PATH**（默认已勾选）
3. 可选：勾选安装必要工具（`Tools for Native Modules`）
4. 完成安装

### 5.3 验证

重新打开 CMD：

```cmd
node --version
npm --version
```

应显示类似：

```
v20.x.x
10.x.x
```

### 5.4 配置 npm 国内镜像（推荐）

```cmd
npm config set registry https://registry.npmmirror.com
```

---

## 6. VS Code 推荐插件

项目 `.vscode/extensions.json` 已预设推荐插件列表，打开项目时 VS Code 会自动弹出推荐安装提示。

### 推荐插件清单

| 插件 | 插件 ID | 用途 |
|------|---------|------|
| **Extension Pack for Java** | `vscjava.vscode-java-pack` | Java 开发全家桶（语言支持、调试、Maven、测试） |
| **Spring Boot Extension Pack** | `vmware.vscode-boot-dev-pack` | Spring Boot 开发支持（应用属性提示、Bean 导航） |
| **Vue - Official (Volar)** | `Vue.volar` | Vue 3 语言支持（语法高亮、类型检查、智能提示） |
| **MySQL (Weijan Chen)** | `cweijan.vscode-mysql-client2` | MySQL 数据库客户端（在 VS Code 内查询数据库） |
| **Prettier** | `esbenp.prettier-vscode` | 代码格式化（Vue / JS / CSS） |

### 安装方式

**方式一（推荐）**：用 VS Code 打开项目根目录，会自动弹出推荐插件提示，点击「Install All」。

**方式二**：手动安装：

```cmd
code --install-extension vscjava.vscode-java-pack
code --install-extension vmware.vscode-boot-dev-pack
code --install-extension Vue.volar
code --install-extension cweijan.vscode-mysql-client2
code --install-extension esbenp.prettier-vscode
```

### VS Code 项目配置说明

项目已预设以下 VS Code 配置（`.vscode/` 目录）：

| 文件 | 内容 |
|------|------|
| `settings.json` | 编码 UTF-8、保存自动格式化、Java/Vue/JS 格式化器、文件排除规则 |
| `launch.json` | 三个调试配置：Spring Boot 后端、用户端 Vite、管理后台 Vite |
| `tasks.json` | 构建任务：MySQL 导入、后端启动/打包、前端 install/build |
| `extensions.json` | 推荐插件列表 |

---

## 7. 数据库导入

### 方式一：使用一键脚本（推荐）

```cmd
scripts\setup-env.bat
```

脚本会提示输入 root 密码，自动创建数据库、导入表结构和测试数据，并自动更新后端配置文件中的密码。

### 方式二：CMD 手动导入

```cmd
REM 登录 MySQL
mysql -u root -p

REM 在 MySQL 交互界面中执行：
mysql> source backend/src/main/resources/sql/schema.sql;
mysql> source backend/src/main/resources/sql/data.sql;
mysql> exit;
```

或通过命令行管道导入（会提示输入密码）：

```cmd
mysql -u root -p < backend\src\main\resources\sql\schema.sql
mysql -u root -p < backend\src\main\resources\sql\data.sql
```

### 方式三：PowerShell 手动导入

```powershell
# 在项目根目录下执行
Get-Content backend\src\main\resources\sql\schema.sql | mysql -u root -p
Get-Content backend\src\main\resources\sql\data.sql | mysql -u root -p
```

> **注意**：PowerShell 中使用 `Get-Content` 管道方式导入时，文件编码需为 UTF-8。如遇乱码，请使用 CMD 方式或在 PowerShell 中先执行 `chcp 65001`。

### 验证数据库导入

```cmd
mysql -u root -p -e "USE jd_ecommerce; SHOW TABLES; SELECT COUNT(*) FROM user;"
```

应显示 15 张表，且 `user` 表有 7 条记录。

---

## 8. 启动项目

### 方式一：一键启动脚本（推荐）

```cmd
scripts\start-all.bat
```

脚本会打开三个新的 CMD 窗口，分别启动：
- 后端 Spring Boot（端口 8080）
- 用户端 Vite（端口 5173）
- 管理后台 Vite（端口 5174）

### 方式二：VS Code 调试模式

1. 用 VS Code 打开项目根目录
2. 按 `F5` 或在「运行和调试」面板选择配置：
   - **Spring Boot (backend)** — 启动后端（带断点调试）
   - **Vite: 用户端 (frontend-user)** — 启动用户端
   - **Vite: 管理后台 (frontend-admin)** — 启动管理后台
3. 可同时启动多个配置（先启动后端，再启动前端）

### 方式三：手动启动

打开三个终端窗口，分别执行：

```cmd
REM 终端 1 - 后端
cd backend
mvn spring-boot:run

REM 终端 2 - 用户端
cd frontend-user
npm install
npm run dev

REM 终端 3 - 管理后台
cd frontend-admin
npm install
npm run dev
```

### 方式四：VS Code Tasks 面板

按 `Ctrl+Shift+P` → 输入 `Tasks: Run Task`，可看到预设任务：
- `MySQL: 建库+导入数据`
- `后端: Maven 启动`
- `后端: Maven 打包 (跳过测试)`
- `前端用户端: npm install`
- `前端管理后台: npm install`
- `前端用户端: build`
- `前端管理后台: build`

### 访问地址

| 服务 | 地址 |
|------|------|
| 后端 API | http://localhost:8080/api |
| Swagger 文档 | http://localhost:8080/api/doc.html |
| 用户端 | http://localhost:5173 |
| 管理后台 | http://localhost:5174 |

### 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 商家 | merchant1 | 123456 |
| 普通用户 | user1 | 123456 |

---

## 9. 常见问题排查

### 9.1 端口被占用

**现象**：启动后端或前端时报 `Port 8080/5173/5174 is already in use`。

**排查**：

```cmd
REM 查看占用 8080 端口的进程
netstat -ano | findstr :8080

REM 根据 PID 查看进程名
tasklist | findstr <PID>

REM 终止进程
taskkill /PID <PID> /F
```

**解决方案**：
- 终止占用端口的旧进程
- 或修改端口：后端在 `application.yml` 中修改 `server.port`；前端在 `vite.config.js` 中修改 `server.port`

### 9.2 中文乱码 / 编码问题

**现象**：CMD 中输出中文乱码，或数据库导入后中文显示为 `???`。

**解决方案**：

1. **CMD 编码**：在脚本或命令前执行 `chcp 65001` 切换为 UTF-8 编码（项目脚本已内置）
2. **MySQL 编码**：确认 MySQL 字符集为 `utf8mb4`：
   ```cmd
   mysql -u root -p -e "SHOW VARIABLES LIKE 'character_set%';"
   ```
   `character_set_database` 和 `character_set_server` 应为 `utf8mb4`
3. **文件编码**：确保 SQL 文件以 UTF-8 编码保存（项目文件已为 UTF-8）
4. **VS Code**：项目 `settings.json` 已设置 `"files.encoding": "utf8"`
5. **PowerShell 导入**：如 `Get-Content` 导入乱码，改用 CMD 方式或执行 `chcp 65001` 后再导入

### 9.3 路径包含空格或中文

**现象**：Maven 或 npm 命令报路径相关错误。

**解决方案**：
- 将项目放在无空格无中文的路径下，如 `C:\projects\jd-ecommerce\`
- 避免放在 `C:\Users\My Name\Documents\项目\` 这类路径下
- 脚本中已使用 `%~dp0` 相对路径和引号包裹，但仍建议项目路径不含空格

### 9.4 JAVA_HOME 未设置或不正确

**现象**：VS Code 中 Java 扩展报错，或 Maven 编译失败。

**解决方案**：
1. 确认 `JAVA_HOME` 指向 JDK 安装目录（如 `C:\Program Files\Eclipse Adoptium\jdk-17.x.x`）
2. 确认 `JAVA_HOME\bin` 在 `Path` 中
3. VS Code 中检查：`Ctrl+Shift+P` → `Java: Configure Java Runtime`，确认识别到 JDK 17+
4. 项目 `.vscode/settings.json` 中已配置 `"spring-boot-tools.ls.java.home": "${env:JAVA_HOME}"`

### 9.5 Maven 依赖下载失败 / 超时

**现象**：`mvn spring-boot:run` 时报 `Could not transfer artifact` 或网络超时。

**解决方案**：
1. 配置阿里云 Maven 镜像（见 [3.4 配置国内镜像](#34-配置国内镜像推荐)）
2. 如已有失败的缓存，删除 `%USERPROFILE%\.m2\repository` 下对应目录后重试
3. 检查网络代理设置，确保 Maven 能访问外网

### 9.6 npm install 失败 / 缓慢

**现象**：`npm install` 超时或报网络错误。

**解决方案**：
1. 配置国内镜像：`npm config set registry https://registry.npmmirror.com`
2. 清除缓存重试：`npm cache clean --force`
3. 删除 `node_modules` 和 `package-lock.json` 后重新安装
4. 如使用 pnpm：`npm install -g pnpm && pnpm install`

### 9.7 MySQL 连接被拒绝

**现象**：后端启动报 `Communications link failure` 或 `Access denied`。

**排查步骤**：
1. 确认 MySQL 服务正在运行：`sc query MySQL80`
2. 确认端口 3306 未被防火墙拦截
3. 确认 `application-dev.yml` 中的用户名和密码正确
4. 确认数据库 `jd_ecommerce` 已创建：`mysql -u root -p -e "SHOW DATABASES;" | findstr jd_ecommerce`
5. 如密码含特殊字符，在 YAML 中用引号包裹

### 9.8 PowerShell 执行策略限制

**现象**：运行 `.ps1` 脚本时报 `无法加载文件，因为在此系统上禁止运行脚本`。

**解决方案**：

```powershell
# 以管理员身份运行 PowerShell，修改执行策略
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 然后重新运行
powershell -ExecutionPolicy Bypass -File scripts\setup-env.ps1
```

### 9.9 VS Code Java 扩展不识别项目

**现象**：Java 文件显示红色波浪线，或无法跳转。

**解决方案**：
1. `Ctrl+Shift+P` → `Java: Clean Java Language Server Workspace` → 选择 `Restart and delete`
2. 确认 `backend/pom.xml` 能被 VS Code 识别（左下角状态栏显示 Java 项目）
3. 等待 Maven 导入完成（右下角进度条）
4. 确认 JDK 版本 ≥ 17

### 9.10 前端页面空白 / API 请求 404

**现象**：前端页面打开但内容空白，或浏览器控制台报 API 404。

**排查步骤**：
1. 确认后端已启动：访问 http://localhost:8080/api/doc.html
2. 检查前端 `vite.config.js` 中的 `proxy` 配置是否指向 `http://localhost:8080`
3. 打开浏览器开发者工具 → Network 面板，查看 API 请求的实际 URL 和响应
4. 确认数据库已导入测试数据
