@echo off
REM ============================================================
REM   Start Frontend Admin (frontend-admin) - Vite Dev Server
REM   Port: 5174
REM ============================================================

cd /d "%~dp0..\frontend-admin"

echo.
echo   Starting frontend-admin (Vite)...
echo   URL: http://localhost:5174
echo   Press Ctrl+C to stop
echo.

call npm run dev

pause
