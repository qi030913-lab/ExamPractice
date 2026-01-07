#!/bin/bash
# macOS 打包脚本
# 请在 macOS 系统上运行此脚本

echo "=========================================="
echo "  在线考试系统 - macOS 打包工具"
echo "=========================================="

# 检查是否在macOS上运行
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "错误：此脚本必须在 macOS 系统上运行！"
    echo "如需在 Windows 上构建，请运行 mvn package"
    exit 1
fi

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "错误：未找到 Java！请先安装 JDK 17 或更高版本。"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "当前 Java 版本: $JAVA_VERSION"

if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "错误：需要 JDK 17 或更高版本！"
    exit 1
fi

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "错误：未找到 Maven！请先安装 Maven。"
    exit 1
fi

echo ""
echo "请选择打包方式："
echo "1) 生成 .app 应用程序（推荐）"
echo "2) 生成 .dmg 安装包"
echo "3) 仅生成 jar 文件"
echo ""
read -p "请输入选项 (1/2/3): " choice

case $choice in
    1)
        echo ""
        echo "正在构建 .app 应用程序..."
        mvn clean package -Pmac -DskipTests
        if [ $? -eq 0 ]; then
            echo ""
            echo "=========================================="
            echo "构建成功！"
            echo "应用程序位置: target/mac/"
            echo "  - 在线考试系统-学生端.app"
            echo "  - 在线考试系统-教师端.app"
            echo "=========================================="
        fi
        ;;
    2)
        echo ""
        echo "正在构建 .dmg 安装包..."
        mvn clean package -Pmac-dmg -DskipTests
        if [ $? -eq 0 ]; then
            echo ""
            echo "=========================================="
            echo "构建成功！"
            echo "安装包位置: target/mac-dmg/"
            echo "  - 在线考试系统-学生端-1.0.0.dmg"
            echo "  - 在线考试系统-教师端-1.0.0.dmg"
            echo "=========================================="
        fi
        ;;
    3)
        echo ""
        echo "正在构建 jar 文件..."
        mvn clean package -DskipTests
        if [ $? -eq 0 ]; then
            echo ""
            echo "=========================================="
            echo "构建成功！"
            echo "jar 文件位置: target/"
            echo "  - exam-student.jar (学生端)"
            echo "  - exam-teacher.jar (教师端)"
            echo ""
            echo "运行方式："
            echo "  双击 start-student-mac.command 启动学生端"
            echo "  双击 start-teacher-mac.command 启动教师端"
            echo "=========================================="
        fi
        ;;
    *)
        echo "无效选项！"
        exit 1
        ;;
esac
