@echo off

REM ============================================================
REM   JD E-Commerce Platform - Start All Services (Windows)
REM   Starts backend + frontend-user + frontend-admin in new windows
REM   Usage: double-click or run in CMD: scripts\start-all.bat
REM ============================================================

set "PROJECT_ROOT=%~dp0.."

echo ============================================================
echo   JD E-Commerce Platform - Start All Services
echo ============================================================
echo.
echo   Project root: %PROJECT_ROOT%
echo.

REM ============================================================
REM Start Backend (Spring Boot, port 8080)
REM ============================================================
echo [1/3] Starting backend (Spring Boot) ...
echo       API:        http://localhost:8080/api
echo       Swagger:    http://localhost:8080/api/doc.html
echo.
start "JD-Backend (Spring Boot)" cmd /k "cd /d "%PROJECT_ROOT%\backend" && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

REM ============================================================
REM Start Frontend User (Vite, port 5173)
REM ============================================================
echo [2/3] Starting frontend-user (Vite) ...
echo       URL: http://localhost:5173
echo.
start "JD-Frontend-User (Vite)" cmd /k "cd /d "%PROJECT_ROOT%\frontend-user" && npm run dev"
timeout /t 2 /nobreak >nul

REM ============================================================
REM Start Frontend Admin (Vite, port 5174)
REM ============================================================
echo [3/3] Starting frontend-admin (Vite) ...
echo       URL: http://localhost:5174
echo.
start "JD-Frontend-Admin (Vite)" cmd /k "cd /d "%PROJECT_ROOT%\frontend-admin" && npm run dev"

REM ============================================================
REM Done
REM ============================================================
echo.
echo ============================================================
echo   All services started in new windows!
echo ============================================================
echo.
echo   Service URLs:
echo     Backend API:    http://localhost:8080/api
echo     Swagger Doc:    http://localhost:8080/api/doc.html
echo     Frontend User:  http://localhost:5173
echo     Frontend Admin: http://localhost:5174
echo.
echo   Test Accounts:
echo     Admin      admin / 123456
echo     Merchant   merchant1 / 123456
echo     User       user1 / 123456
echo.
echo   To stop: close the corresponding CMD windows
echo.
pause
