@echo off

REM ============================================================
REM   JD E-Commerce Platform - Start All Services (Windows)
REM   Starts backend + frontend-user + frontend-admin + frontend-merchant in new windows
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
echo [1/4] Starting backend (Spring Boot) ...
echo       API:        http://localhost:8080/api
echo       Swagger:    http://localhost:8080/api/doc.html
echo.
start "JD-Backend (Spring Boot)" cmd /k "cd /d "%PROJECT_ROOT%\backend" && mvnw.cmd spring-boot:run"
timeout /t 5 /nobreak >nul

REM ============================================================
REM Start Frontend User (Vite, port 5173)
REM ============================================================
echo [2/4] Starting frontend-user (Vite) ...
echo       URL: http://localhost:5173
echo.
start "JD-Frontend-User (Vite)" cmd /k "cd /d "%PROJECT_ROOT%\frontend-user" && npm run dev"
timeout /t 2 /nobreak >nul

REM ============================================================
REM Start Frontend Admin (Vite, port 5174)
REM ============================================================
echo [3/4] Starting frontend-admin (Vite) ...
echo       URL: http://localhost:5174
echo.
start "JD-Frontend-Admin (Vite)" cmd /k "cd /d "%PROJECT_ROOT%\frontend-admin" && npm run dev"
timeout /t 2 /nobreak >nul

REM ============================================================
REM Start Frontend Merchant (Vite, port 5175)
REM ============================================================
echo [4/4] Starting frontend-merchant (Vite) ...
echo       URL: http://localhost:5175
echo.
start "JD-Frontend-Merchant (Vite)" cmd /k "cd /d "%PROJECT_ROOT%\frontend-merchant" && npm run dev"

REM ============================================================
REM Done
REM ============================================================
echo.
echo ============================================================
echo   All services started in new windows!
echo ============================================================
echo.
echo   Service URLs:
echo     Backend API:         http://localhost:8080/api
echo     Swagger Doc:         http://localhost:8080/api/doc.html
echo     Frontend User:       http://localhost:5173
echo     Frontend Admin:      http://localhost:5174
echo     Frontend Merchant:   http://localhost:5175
echo.
echo   Test Accounts:
echo     Admin      admin / 123456
echo     Merchant   merchant1 / 123456
echo     User       user1 / 123456
echo.
echo   To stop: run scripts\stop-all.bat or close the CMD windows
echo.
pause
