@echo off
REM ============================================================
REM   Start Frontend User (frontend-user) - Vite Dev Server
REM   Port: 5173
REM ============================================================

cd /d "%~dp0..\frontend-user"

echo.
echo   Starting frontend-user (Vite)...
echo   URL: http://localhost:5173
echo   Press Ctrl+C to stop
echo.

call npm run dev

pause
