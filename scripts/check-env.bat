@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM   JD E-Commerce Platform - Environment Check Script (Windows)
REM   Checks JDK / Maven / MySQL / Node.js and prints versions
REM   Usage: double-click or run in CMD: scripts\check-env.bat
REM ============================================================

echo ============================================================
echo   JD E-Commerce Platform - Environment Check
echo ============================================================
echo.

set "ALL_OK=1"

REM ============================================================
REM 1. JDK 17+
REM ============================================================
echo [1/5] JDK 17+ ...
where java >nul 2>nul
if errorlevel 1 (
    echo   [X] JDK not found
    echo       Install JDK 17+: https://adoptium.net/temurin/releases/?version=17
    echo       Set JAVA_HOME and add %%JAVA_HOME%%\bin to PATH
    set "ALL_OK=0"
) else (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set "JVER=%%v"
        set "JVER=!JVER:"=!"
    )
    for /f "tokens=1 delims=." %%n in ("!JVER!") do set "JMAJOR=%%n"
    if "!JMAJOR!"=="1" (
        for /f "tokens=2 delims=." %%n in ("!JVER!") do set "JMAJOR=%%n"
    )
    if !JMAJOR! geq 17 (
        echo   [OK] JDK !JVER!
        if defined JAVA_HOME (
            echo       JAVA_HOME = !JAVA_HOME!
        ) else (
            echo       [!] JAVA_HOME not set, recommended to configure it
        )
    ) else (
        echo   [X] JDK version !JVER! too low, need 17+
        set "ALL_OK=0"
    )
)
echo.

REM ============================================================
REM 2. Maven 3.8+
REM ============================================================
echo [2/5] Maven 3.8+ ...
where mvn >nul 2>nul
if errorlevel 1 (
    echo   [X] Maven not found
    echo       Install Maven 3.8+: https://maven.apache.org/download.cgi
    echo       Set MAVEN_HOME and add %%MAVEN_HOME%%\bin to PATH
    set "ALL_OK=0"
) else (
    for /f "tokens=2-3" %%a in ('mvn -v 2^>^&1 ^| findstr /i "Apache Maven"') do (
        echo   [OK] Maven %%b
        goto :maven_done
    )
    :maven_done
)
echo.

REM ============================================================
REM 3. MySQL 8.0+
REM ============================================================
echo [3/5] MySQL 8.0+ ...
where mysql >nul 2>nul
if errorlevel 1 (
    echo   [X] MySQL client not found
    echo       Install MySQL 8.0+: https://dev.mysql.com/downloads/installer/
    echo       Remember the root password during installation
    set "ALL_OK=0"
) else (
    for /f "delims=" %%v in ('mysql --version 2^>^&1') do set "MVER=%%v"
    echo   [OK] !MVER!
)
echo.

REM ============================================================
REM 4. Node.js 18+
REM ============================================================
echo [4/5] Node.js 18+ ...
where node >nul 2>nul
if errorlevel 1 (
    echo   [X] Node.js not found
    echo       Install Node.js 18+: https://nodejs.org/
    set "ALL_OK=0"
) else (
    for /f "delims=" %%v in ('node --version 2^>^&1') do set "NVER=%%v"
    set "NMAJOR=!NVER:~1!"
    for /f "tokens=1 delims=." %%n in ("!NMAJOR!") do set "NMAJOR=%%n"
    if !NMAJOR! geq 18 (
        echo   [OK] Node.js !NVER!
        where npm >nul 2>nul
        if not errorlevel 1 (
            for /f "delims=" %%v in ('npm --version 2^>^&1') do echo       npm %%v
        )
    ) else (
        echo   [X] Node.js version !NVER! too low, need 18+
        set "ALL_OK=0"
    )
)
echo.

REM ============================================================
REM 5. Project directories & node_modules
REM ============================================================
echo [5/5] Project directories ...
if not exist "%~dp0..\backend" (
    echo   [X] backend directory not found
    set "ALL_OK=0"
) else (
    echo   [OK] backend
)
if not exist "%~dp0..\frontend-user" (
    echo   [X] frontend-user directory not found
    set "ALL_OK=0"
) else (
    echo   [OK] frontend-user
)
if not exist "%~dp0..\frontend-admin" (
    echo   [X] frontend-admin directory not found
    set "ALL_OK=0"
) else (
    echo   [OK] frontend-admin
)
if not exist "%~dp0..\frontend-merchant" (
    echo   [X] frontend-merchant directory not found
    set "ALL_OK=0"
) else (
    echo   [OK] frontend-merchant
)
echo   Checking node_modules ...
if not exist "%~dp0..\frontend-user\node_modules" (
    echo   [!] frontend-user/node_modules not found - run scripts\setup-env.bat
)
if not exist "%~dp0..\frontend-admin\node_modules" (
    echo   [!] frontend-admin/node_modules not found - run scripts\setup-env.bat
)
if not exist "%~dp0..\frontend-merchant\node_modules" (
    echo   [!] frontend-merchant/node_modules not found - run scripts\setup-env.bat
)
echo.

REM ============================================================
REM Summary
REM ============================================================
echo ============================================================
if "!ALL_OK!"=="1" (
    echo   All environment dependencies are ready!
    echo   Next: run scripts\setup-env.bat to import DB and install deps
    echo   Then: run scripts\start-all.bat to start all services
) else (
    echo   Some dependencies are missing or outdated.
    echo   Please install/upgrade according to the tips above.
)
echo ============================================================
echo.
pause
