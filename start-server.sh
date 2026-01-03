#!/bin/bash
# 无界面教师端服务器启动脚本
# 用于在Linux服务器上运行

echo "========================================="
echo "  在线考试系统 - 教师端服务器"
echo "  Teacher Server Headless"
echo "========================================="
echo ""

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境"
    echo "请先安装Java 17或更高版本"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
echo "检测到Java版本: $JAVA_VERSION"

if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "警告: Java版本过低，建议使用Java 17或更高版本"
fi

# 查找jar包
JAR_FILE="target/exam-server-headless.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "错误: 未找到jar包 $JAR_FILE"
    echo "请先运行: mvn clean package"
    exit 1
fi

echo "找到jar包: $JAR_FILE"
echo ""

# 启动服务器
echo "正在启动服务器..."
echo "按 Ctrl+C 停止服务器"
echo ""

# 设置UTF-8编码
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

# 运行服务器
java -Dfile.encoding=UTF-8 -jar "$JAR_FILE"
