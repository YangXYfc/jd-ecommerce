@ECHO OFF
chcp 65001 >nul
TITLE 修复前端依赖 (Windows)
ECHO ============================================
ECHO   修复前端 node_modules (Windows 重建)
ECHO ============================================
ECHO.

REM 检查 node 是否可用
where node >nul 2>&1
if %ERRORLEVEL% neq 0 (
    ECHO [错误] 未找到 node，请确保 Node.js 已安装并加入 PATH
    pause
    exit /b 1
)

REM 检查 npm 是否可用
where npm >nul 2>&1
if %ERRORLEVEL% neq 0 (
    ECHO [错误] 未找到 npm，请确保 npm 已安装并加入 PATH
    pause
    exit /b 1
)

ECHO [信息] Node: 
node --version
ECHO [信息] npm: 
npm --version
ECHO.

REM 获取项目根目录
SET "PROJECT_ROOT=%~dp0.."
SET "FRONTEND_USER=%PROJECT_ROOT%\frontend-user"
SET "FRONTEND_ADMIN=%PROJECT_ROOT%\frontend-admin"

ECHO ============================================
ECHO  1/4  清理 frontend-user\node_modules
ECHO ============================================
if exist "%FRONTEND_USER%\node_modules" (
    rmdir /s /q "%FRONTEND_USER%\node_modules"
    ECHO [完成] 已删除 frontend-user\node_modules
) else (
    ECHO [跳过] frontend-user\node_modules 不存在
)
ECHO.

ECHO ============================================
ECHO  2/4  清理 frontend-admin\node_modules
ECHO ============================================
if exist "%FRONTEND_ADMIN%\node_modules" (
    rmdir /s /q "%FRONTEND_ADMIN%\node_modules"
    ECHO [完成] 已删除 frontend-admin\node_modules
) else (
    ECHO [跳过] frontend-admin\node_modules 不存在
)
ECHO.

ECHO ============================================
ECHO  3/4  重新安装 frontend-user 依赖
ECHO ============================================
cd /d "%FRONTEND_USER%"
ECHO [运行] npm install...
call npm install
if %ERRORLEVEL% neq 0 (
    ECHO [错误] frontend-user npm install 失败
    pause
    exit /b 1
)
ECHO [完成] frontend-user 依赖安装成功
ECHO.

ECHO ============================================
ECHO  4/4  重新安装 frontend-admin 依赖
ECHO ============================================
cd /d "%FRONTEND_ADMIN%"
ECHO [运行] npm install...
call npm install
if %ERRORLEVEL% neq 0 (
    ECHO [错误] frontend-admin npm install 失败
    pause
    exit /b 1
)
ECHO [完成] frontend-admin 依赖安装成功
ECHO.

ECHO ============================================
ECHO  全部完成！现在可以启动前端服务了
ECHO ============================================
ECHO.
ECHO  启动用户端:  cd frontend-user ^&^& npm run dev
ECHO  启动管理端:  cd frontend-admin ^&^& npm run dev
ECHO.
pause
