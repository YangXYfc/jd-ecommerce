@echo off
REM ============================================================
REM   JD E-Commerce Platform - Backend Start Script (Windows)
REM   Uses Maven Wrapper, no global Maven required
REM ============================================================

echo ============================================================
echo   JD E-Commerce Platform - Backend Start (Maven Wrapper)
echo ============================================================
echo.

REM ------------------------------------------------------------
REM 1. Check JAVA_HOME
REM ------------------------------------------------------------
echo [1/3] Checking Java environment ...
if "%JAVA_HOME%" == "" (
    echo   [X] JAVA_HOME is not set!
    echo       Please install JDK 17+ and set JAVA_HOME
    echo       Download: https://adoptium.net/temurin/releases/?version=17
    echo.
    echo       Or run scripts\setup-env.bat for guided setup
    pause
    exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo   [X] JAVA_HOME path is invalid: %JAVA_HOME%
    echo       Please check JAVA_HOME points to JDK install directory
    pause
    exit /b 1
)
echo   [OK] JAVA_HOME = %JAVA_HOME%
"%JAVA_HOME%\bin\java.exe" -version 2>&1 | findstr /i "version"
echo.

REM ------------------------------------------------------------
REM 2. Switch to backend directory
REM ------------------------------------------------------------
echo [2/3] Switching to backend directory ...
cd /d "%~dp0..\backend"
if errorlevel 1 (
    echo   [X] Cannot switch to backend directory
    pause
    exit /b 1
)
echo   [OK] Current directory: %CD%
echo.

REM ------------------------------------------------------------
REM 3. Start Spring Boot with Maven Wrapper
REM ------------------------------------------------------------
echo [3/3] Starting Spring Boot backend ...
echo   Running: mvnw.cmd spring-boot:run
echo   First run will download Maven, please wait...
echo.

call mvnw.cmd spring-boot:run

if errorlevel 1 (
    echo.
    echo   [X] Backend startup failed, check error messages above
    echo       Common issues:
    echo       1. MySQL not running or wrong password (check application-dev.yml)
    echo       2. Port 8080 is occupied
    echo       3. Network issue causing dependency download failure
    pause
    exit /b 1
)

echo.
echo   [OK] Backend has stopped
pause
