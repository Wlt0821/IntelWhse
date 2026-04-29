@echo off
chcp 65001 >nul
echo ============================================================
echo    仓库管理系统 - 一键启动脚本
echo ============================================================
echo.

REM 获取脚本所在目录
set "BASE_DIR=%~dp0"
set "BACKEND_DIR=%BASE_DIR%wms-backend"
set "FRONTEND_DIR=%BASE_DIR%wms-frontend"

echo [1/3] 正在启动后端...
start "后端 - Maven" cmd /k "cd /d %BACKEND_DIR% && mvn spring-boot:run"

timeout /t 5 >nul

echo [2/3] 正在启动前端...
start "前端 - Vite" cmd /k "cd /d %FRONTEND_DIR% && npm run dev"

timeout /t 5 >nul

echo [3/3] 正在启动 Copaw...
start "Copaw" cmd /k "cd /d %BASE_DIR% && copaw app"

echo.
echo ============================================================
echo  ✅ 所有服务已启动！
echo  📋 服务地址:
echo    - 后端: http://localhost:8080
echo    - 前端: http://localhost:3000 (或查看前端窗口)
echo    - Copaw: 查看 Copaw 窗口
echo.
echo  按任意键退出此窗口（不会关闭其他服务）
echo ============================================================
pause >nul
