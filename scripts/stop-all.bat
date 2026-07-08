@echo off
setlocal enabledelayedexpansion

REM ============================================================
REM   JD E-Commerce Platform - Stop All Services (Windows)
REM   Stops backend (port 8080) + frontend-user (5173) + frontend-admin (5174) + frontend-merchant (5175)
REM   Usage: double-click or run in CMD: scripts\stop-all.bat
REM ============================================================

echo ============================================================
echo   JD E-Commerce Platform - Stop All Services
echo ============================================================
echo.

set "STOPPED=0"

REM ============================================================
REM 1. Stop Backend (Spring Boot, port 8080)
REM ============================================================
echo [1/4] Stopping backend (port 8080) ...
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING"') do (
    taskkill /PID %%p /F >nul 2>nul
    if !errorlevel! equ 0 (
        echo   [OK] Killed PID %%p ^(backend^)
        set "STOPPED=1"
    )
)
if "!STOPPED!"=="0" echo   [--] Backend not running or already stopped
echo.

REM ============================================================
REM 2. Stop Frontend dev servers (Vite, ports 5173/5174/5175)
REM ============================================================
echo [2/4] Stopping frontend dev servers (ports 5173/5174/5175) ...
set "FE_STOPPED=0"

for %%P in (5173 5174 5175) do (
    for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%%P " ^| findstr "LISTENING"') do (
        taskkill /PID %%p /F >nul 2>nul
        if !errorlevel! equ 0 (
            echo   [OK] Killed PID %%p ^(port %%P^)
            set "FE_STOPPED=1"
        )
    )
)
if "!FE_STOPPED!"=="0" echo   [--] No frontend dev servers found on ports 5173/5174/5175

REM ============================================================
REM Also kill any lingering node processes from Vite
REM ============================================================
taskkill /F /IM node.exe /FI "WINDOWTITLE eq JD-Frontend*" >nul 2>nul
taskkill /F /IM java.exe /FI "WINDOWTITLE eq JD-Backend*" >nul 2>nul

echo.
echo ============================================================
echo   Stop complete.
echo ============================================================
echo.
pause
