#!/bin/bash
# 在线考试系统 - 教师端 (macOS)
# 双击此文件启动教师端

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# 检查Java版本
if ! command -v java &> /dev/null; then
    osascript -e 'display dialog "未找到Java运行环境！\n请先安装JDK 17或更高版本。\n\n下载地址：https://adoptium.net/" buttons {"确定"} default button 1 with title "错误" with icon stop'
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    osascript -e 'display dialog "Java版本过低！\n当前版本：'"$JAVA_VERSION"'\n需要JDK 17或更高版本。" buttons {"确定"} default button 1 with title "错误" with icon stop'
    exit 1
fi

# 查找jar文件
JAR_FILE=""
if [ -f "target/exam-teacher.jar" ]; then
    JAR_FILE="target/exam-teacher.jar"
elif [ -f "exam-teacher.jar" ]; then
    JAR_FILE="exam-teacher.jar"
else
    osascript -e 'display dialog "未找到教师端jar文件！\n请先执行 mvn package 构建项目。" buttons {"确定"} default button 1 with title "错误" with icon stop'
    exit 1
fi

# 启动应用
java -Dfile.encoding=UTF-8 \
     -Dapple.awt.application.appearance=system \
     -Dapple.laf.useScreenMenuBar=true \
     -Xdock:name="在线考试系统-教师端" \
     -jar "$JAR_FILE"
