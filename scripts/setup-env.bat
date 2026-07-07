@echo off
REM ============================================================
REM   JD E-Commerce Platform - Environment Setup Script (Windows)
REM   Checks JDK/Maven/MySQL/Node.js, imports database, installs deps
REM   Usage: double-click or run in CMD: scripts\setup-env.bat
REM ============================================================

echo ============================================================
echo   JD E-Commerce Platform - Environment Setup
echo ============================================================
echo.

REM ============================================================
REM 1. Check JDK 17+
REM ============================================================
echo [1/6] Checking JDK ...
java -version 2>nul | findstr /i "version"
if errorlevel 1 (
    echo   [X] JDK not found. Please install JDK 17+
    echo       Download: https://adoptium.net/temurin/releases/?version=17
    echo       Set JAVA_HOME env var and add %%JAVA_HOME%%\bin to PATH
    pause
    exit /b 1
)
echo   [OK] JDK is installed
echo.

REM ============================================================
REM 2. Check Maven
REM ============================================================
echo [2/6] Checking Maven ...
call mvn -v 2>nul | findstr /i "Apache Maven"
if errorlevel 1 (
    echo   [X] Maven not found. Please install Maven 3.8+
    echo       Download: https://maven.apache.org/download.cgi
    echo       Set MAVEN_HOME and add %%MAVEN_HOME%%\bin to PATH
    pause
    exit /b 1
)
echo   [OK] Maven is installed
echo.

REM ============================================================
REM 3. Check MySQL
REM ============================================================
echo [3/6] Checking MySQL ...
mysql --version 2>nul
if errorlevel 1 (
    echo   [X] MySQL not found. Please install MySQL 8.0+
    echo       Download: https://dev.mysql.com/downloads/installer/
    echo       Remember the root password during installation
    pause
    exit /b 1
)
echo   [OK] MySQL is installed
echo.

REM ============================================================
REM 4. Check Node.js
REM ============================================================
echo [4/6] Checking Node.js ...
node --version 2>nul
if errorlevel 1 (
    echo   [X] Node.js not found. Please install Node.js 18+
    echo       Download: https://nodejs.org/
    pause
    exit /b 1
)
echo   [OK] Node.js is installed
echo.

REM ============================================================
REM 5. Import Database
REM ============================================================
echo [5/6] Importing database ...
echo   Please enter MySQL root password:
set /p MYSQL_PWD="  root password: "

echo   Creating database and tables...
mysql -u root -p%MYSQL_PWD% < "%~dp0..\backend\src\main\resources\sql\schema.sql" 2>nul
if errorlevel 1 (
    echo   [X] Schema import failed. Check password or MySQL service.
    pause
    exit /b 1
)
echo   Importing test data...
mysql -u root -p%MYSQL_PWD% < "%~dp0..\backend\src\main\resources\sql\data.sql" 2>nul
if errorlevel 1 (
    echo   [X] Data import failed.
    pause
    exit /b 1
)
echo   [OK] Database jd_ecommerce created, 15 tables + test data imported
echo.

REM ============================================================
REM 6. Update backend config & install frontend deps
REM ============================================================
echo [6/6] Configuring backend and installing frontend deps ...

REM Update application-dev.yml with MySQL password
echo   Updating backend database config...
powershell -Command "(Get-Content '%~dp0..\backend\src\main\resources\application-dev.yml') -replace 'password: root', 'password: %MYSQL_PWD%' | Set-Content '%~dp0..\backend\src\main\resources\application-dev.yml'"
echo   [OK] application-dev.yml password updated

echo   Installing frontend-user dependencies...
cd /d "%~dp0..\frontend-user"
call npm install
if errorlevel 1 (
    echo   [X] frontend-user npm install failed
) else (
    echo   [OK] frontend-user deps installed
)

echo   Installing frontend-admin dependencies...
cd /d "%~dp0..\frontend-admin"
call npm install
if errorlevel 1 (
    echo   [X] frontend-admin npm install failed
) else (
    echo   [OK] frontend-admin deps installed
)

echo   Installing frontend-merchant dependencies...
cd /d "%~dp0..\frontend-merchant"
call npm install
if errorlevel 1 (
    echo   [X] frontend-merchant npm install failed
) else (
    echo   [OK] frontend-merchant deps installed
)

echo.
echo ============================================================
echo   Environment Setup Complete!
echo ============================================================
echo.
echo   Test Accounts:
echo     Admin      admin / 123456
echo     Merchant   merchant1 / 123456
echo     User       user1 / 123456
echo.
echo   Start the project:
echo     Run scripts\start-all.bat  (starts all services)
echo     Or press F5 in VS Code (use launch configs)
echo.
pause
