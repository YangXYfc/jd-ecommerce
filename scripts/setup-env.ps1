#Requires -Version 5.1
<#
.SYNOPSIS
    京东风格电商平台 - 一键环境配置脚本 (Windows PowerShell)
.DESCRIPTION
    检查 JDK / Maven / MySQL / Node.js，导入数据库，配置后端连接，安装前端依赖
.NOTES
    在项目根目录下运行: powershell -ExecutionPolicy Bypass -File scripts\setup-env.ps1
#>

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

function Write-Step { param([string]$msg) Write-Host "`n[>] $msg" -ForegroundColor Cyan }
function Write-OK   { param([string]$msg) Write-Host "    [OK] $msg" -ForegroundColor Green }
function Write-Err  { param([string]$msg) Write-Host "    [X] $msg" -ForegroundColor Red }

Write-Host "============================================================" -ForegroundColor Yellow
Write-Host "  京东风格电商平台 - 一键环境配置 (PowerShell)" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Yellow

# ============================================================
# 1. JDK 17+
# ============================================================
Write-Step "检查 JDK ..."
try {
    $javaVer = (java -version 2>&1 | Select-Object -First 1).ToString()
    $verNum = [regex]::Match($javaVer, '"(\d+)').Groups[1].Value
    if ([int]$verNum -ge 17) {
        Write-OK "JDK $verNum 已安装"
    } else {
        Write-Err "JDK 版本 $verNum 过低，需要 17+"
        Write-Host "    下载: https://adoptium.net/temurin/releases/?version=17" -ForegroundColor DarkYellow
        exit 1
    }
} catch {
    Write-Err "未检测到 JDK，请安装 JDK 17+"
    Write-Host "    下载: https://adoptium.net/temurin/releases/?version=17" -ForegroundColor DarkYellow
    exit 1
}

# ============================================================
# 2. Maven 3.8+
# ============================================================
Write-Step "检查 Maven ..."
try {
    $mvnVer = (mvn -v 2>&1 | Select-String "Apache Maven").ToString()
    Write-OK $mvnVer.Trim()
} catch {
    Write-Err "未检测到 Maven，请安装 Maven 3.8+"
    Write-Host "    下载: https://maven.apache.org/download.cgi" -ForegroundColor DarkYellow
    exit 1
}

# ============================================================
# 3. MySQL 8.0+
# ============================================================
Write-Step "检查 MySQL ..."
try {
    $mysqlVer = (mysql --version 2>&1).ToString()
    Write-OK $mysqlVer.Trim()
} catch {
    Write-Err "未检测到 MySQL，请安装 MySQL 8.0+"
    Write-Host "    下载: https://dev.mysql.com/downloads/installer/" -ForegroundColor DarkYellow
    exit 1
}

# ============================================================
# 4. Node.js 18+
# ============================================================
Write-Step "检查 Node.js ..."
try {
    $nodeVer = (node --version 2>&1).ToString()
    $verMajor = [int]([regex]::Match($nodeVer, 'v(\d+)').Groups[1].Value)
    if ($verMajor -ge 18) {
        Write-OK "Node.js $nodeVer"
    } else {
        Write-Err "Node.js 版本 $nodeVer 过低，需要 18+"
        Write-Host "    下载: https://nodejs.org/" -ForegroundColor DarkYellow
        exit 1
    }
} catch {
    Write-Err "未检测到 Node.js，请安装 Node.js 18+"
    Write-Host "    下载: https://nodejs.org/" -ForegroundColor DarkYellow
    exit 1
}

# ============================================================
# 5. 导入数据库
# ============================================================
Write-Step "导入数据库 ..."
$mysqlPwd = Read-Host "    请输入 MySQL root 密码" -AsSecureString
$plainPwd = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($mysqlPwd)
)

$schemaPath = Join-Path $ProjectRoot "backend\src\main\resources\sql\schema.sql"
$dataPath   = Join-Path $ProjectRoot "backend\src\main\resources\sql\data.sql"

Write-Host "    正在创建数据库和表..."
Get-Content $schemaPath -Raw | mysql -u root -p$plainPwd 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Err "建表失败，请检查密码或 MySQL 服务是否启动"
    exit 1
}

Write-Host "    正在导入测试数据..."
Get-Content $dataPath -Raw | mysql -u root -p$plainPwd 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Err "数据导入失败"
    exit 1
}
Write-OK "数据库 jd_ecommerce 创建完成 (15 张表 + 测试数据)"

# ============================================================
# 6. 配置后端 & 安装前端依赖
# ============================================================
Write-Step "配置后端数据库连接 ..."
$devYml = Join-Path $ProjectRoot "backend\src\main\resources\application-dev.yml"
$content = Get-Content $devYml -Raw -Encoding UTF8
$content = $content -replace 'password:\s*root\s*', "password: $plainPwd"
Set-Content $devYml -Value $content -Encoding UTF8
Write-OK "application-dev.yml 已更新数据库密码"

Write-Step "安装前端用户端依赖 (frontend-user) ..."
Push-Location (Join-Path $ProjectRoot "frontend-user")
npm install 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) { Write-OK "frontend-user 依赖安装完成" }
else { Write-Err "frontend-user npm install 失败" }
Pop-Location

Write-Step "安装前端管理后台依赖 (frontend-admin) ..."
Push-Location (Join-Path $ProjectRoot "frontend-admin")
npm install 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) { Write-OK "frontend-admin 依赖安装完成" }
else { Write-Err "frontend-admin npm install 失败" }
Pop-Location

# ============================================================
# 完成
# ============================================================
Write-Host ""
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host "  环境配置完成！" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "  测试账号:" -ForegroundColor White
Write-Host "    管理员  admin / 123456"
Write-Host "    商家    merchant1 / 123456"
Write-Host "    用户    user1 / 123456"
Write-Host ""
Write-Host "  启动方式 (VS Code 终端):" -ForegroundColor White
Write-Host "    后端:    cd backend && mvn spring-boot:run"
Write-Host "    用户端:  cd frontend-user && npm run dev"
Write-Host "    管理后台: cd frontend-admin && npm run dev"
Write-Host ""
Write-Host "  或在 VS Code 中按 F5 使用预设的 launch 配置" -ForegroundColor DarkGray
Write-Host ""
