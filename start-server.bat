@echo off
chcp 65001 >nul
REM 无界面教师端服务器启动脚本 (Windows)

echo =========================================
echo   在线考试系统 - 教师端服务器
echo   Teacher Server Headless
echo =========================================
echo.

REM 检查Java环境
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境
    echo 请先安装Java 17或更高版本
    pause
    exit /b 1
)

echo 检测到Java环境
java -version
echo.

REM 查找jar包
set JAR_FILE=target\exam-server-headless.jar

if not exist "%JAR_FILE%" (
    echo 错误: 未找到jar包 %JAR_FILE%
    echo 请先运行: mvn clean package
    pause
    exit /b 1
)

echo 找到jar包: %JAR_FILE%
echo.

REM 启动服务器
echo 正在启动服务器...
echo 按 Ctrl+C 停止服务器
echo.

java -Dfile.encoding=UTF-8 -jar "%JAR_FILE%"

pause
