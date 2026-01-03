package com.exam;

import com.exam.util.NetworkUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * 无界面教师端通信服务器
 * 适用于在Linux服务器上运行，不需要图形界面
 * 
 * 功能：
 * 1. 启动TCP服务器监听学生端连接
 * 2. 接收学生端消息并显示
 * 3. 广播消息给所有在线学生
 * 4. 支持命令行交互控制
 * 
 * 使用方法：
 * 1. 编译: mvn clean package
 * 2. 运行: java -cp target/classes:target/test-classes TeacherServerHeadless
 * 3. 或者打包后: java -cp untitled-1.0-SNAPSHOT.jar TeacherServerHeadless
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class TeacherServerHeadless {
    
    private static NetworkUtil.TcpServer server;
    private static boolean isRunning = false;
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // 服务器配置
    private static final int PORT = NetworkUtil.DEFAULT_PORT; // 8888
    private static final String BIND_ADDRESS = "0.0.0.0"; // 允许外部连接
    
    public static void main(String[] args) {
        printBanner();
        printSystemInfo();
        
        // 启动服务器
        startServer();
        
        // 命令行交互
        startCommandLine();
    }
    
    /**
     * 打印欢迎横幅
     */
    private static void printBanner() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           在线考试系统 - 教师端通信服务器 (无界面)             ║");
        System.out.println("║                    Teacher Server Headless                    ║");
        System.out.println("║                         Version 1.0                           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * 打印系统信息
     */
    private static void printSystemInfo() {
        log("系统信息", "INFO");
        System.out.println("  - Java版本: " + System.getProperty("java.version"));
        System.out.println("  - 操作系统: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("  - 服务器端口: " + PORT);
        System.out.println("  - 绑定地址: " + BIND_ADDRESS);
        System.out.println("  - 启动时间: " + LocalDateTime.now().format(timeFormatter));
        System.out.println();
    }
    
    /**
     * 启动服务器
     */
    private static void startServer() {
        try {
            log("正在启动服务器...", "INFO");
            
            server = new NetworkUtil.TcpServer(new NetworkUtil.MessageListener() {
                @Override
                public void onMessageReceived(String message, String clientInfo) {
                    log("接收 [" + clientInfo + "]: " + message, "MESSAGE");
                }
                
                @Override
                public void onClientConnected(String clientInfo) {
                    log("客户端连接: " + clientInfo, "CONNECT");
                    log("当前在线客户端数: " + server.getClientCount(), "INFO");
                }
                
                @Override
                public void onClientDisconnected(String clientInfo) {
                    log("客户端断开: " + clientInfo, "DISCONNECT");
                    log("当前在线客户端数: " + server.getClientCount(), "INFO");
                }
                
                @Override
                public void onError(String error) {
                    log("错误: " + error, "ERROR");
                }
            });
            
            server.start(PORT);
            isRunning = true;
            
            log("服务器启动成功！", "SUCCESS");
            log("监听端口: " + PORT, "INFO");
            log("绑定地址: " + BIND_ADDRESS, "INFO");
            log("等待学生端连接...", "INFO");
            log("提示: 确保防火墙允许 " + PORT + " 端口通信", "WARN");
            System.out.println();
            
        } catch (IOException e) {
            log("启动服务器失败: " + e.getMessage(), "ERROR");
            log("异常类型: " + e.getClass().getName(), "ERROR");
            log("可能原因:", "ERROR");
            log("  1. 端口 " + PORT + " 已被占用", "ERROR");
            log("  2. 没有权限绑定该端口 (尝试使用 sudo 或端口 > 1024)", "ERROR");
            log("  3. 网络配置错误", "ERROR");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 停止服务器
     */
    private static void stopServer() {
        if (server != null) {
            log("正在停止服务器...", "INFO");
            server.stop();
            isRunning = false;
            log("服务器已停止", "INFO");
        }
    }
    
    /**
     * 命令行交互
     */
    private static void startCommandLine() {
        Scanner scanner = new Scanner(System.in);
        
        printHelp();
        
        while (true) {
            System.out.print("\n服务器> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toLowerCase();
            
            switch (command) {
                case "help":
                case "h":
                    printHelp();
                    break;
                    
                case "status":
                case "s":
                    printStatus();
                    break;
                    
                case "broadcast":
                case "b":
                    if (parts.length > 1) {
                        broadcast(parts[1]);
                    } else {
                        System.out.println("用法: broadcast <消息内容>");
                    }
                    break;
                    
                case "clients":
                case "c":
                    printClients();
                    break;
                    
                case "restart":
                case "r":
                    restartServer();
                    break;
                    
                case "stop":
                    stopServer();
                    break;
                    
                case "exit":
                case "quit":
                case "q":
                    log("正在退出...", "INFO");
                    stopServer();
                    scanner.close();
                    System.exit(0);
                    break;
                    
                case "clear":
                case "cls":
                    clearScreen();
                    break;
                    
                default:
                    System.out.println("未知命令: " + command);
                    System.out.println("输入 'help' 查看可用命令");
            }
        }
    }
    
    /**
     * 打印帮助信息
     */
    private static void printHelp() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("可用命令:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  help, h              - 显示此帮助信息");
        System.out.println("  status, s            - 显示服务器状态");
        System.out.println("  broadcast <消息>, b  - 广播消息给所有在线学生");
        System.out.println("  clients, c           - 显示在线客户端数量");
        System.out.println("  restart, r           - 重启服务器");
        System.out.println("  stop                 - 停止服务器");
        System.out.println("  exit, quit, q        - 退出程序");
        System.out.println("  clear, cls           - 清屏");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    /**
     * 打印服务器状态
     */
    private static void printStatus() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("服务器状态:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  运行状态: " + (isRunning ? "运行中 ✓" : "已停止 ✗"));
        System.out.println("  监听端口: " + PORT);
        System.out.println("  绑定地址: " + BIND_ADDRESS);
        if (server != null) {
            System.out.println("  在线客户端: " + server.getClientCount());
        }
        System.out.println("  当前时间: " + LocalDateTime.now().format(timeFormatter));
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    /**
     * 广播消息
     */
    private static void broadcast(String message) {
        if (server != null && isRunning) {
            server.broadcast("[教师端广播]: " + message);
            log("已广播消息: " + message, "BROADCAST");
        } else {
            log("服务器未运行，无法广播消息", "ERROR");
        }
    }
    
    /**
     * 显示在线客户端
     */
    private static void printClients() {
        if (server != null) {
            int count = server.getClientCount();
            System.out.println("\n当前在线客户端数: " + count);
        } else {
            System.out.println("\n服务器未运行");
        }
    }
    
    /**
     * 重启服务器
     */
    private static void restartServer() {
        log("正在重启服务器...", "INFO");
        stopServer();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startServer();
    }
    
    /**
     * 清屏
     */
    private static void clearScreen() {
        try {
            String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // 如果清屏失败，打印空行
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
    
    /**
     * 日志输出
     */
    private static void log(String message, String level) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String prefix;
        
        switch (level) {
            case "SUCCESS":
                prefix = "✓ [成功]";
                break;
            case "ERROR":
                prefix = "✗ [错误]";
                break;
            case "WARN":
                prefix = "⚠ [警告]";
                break;
            case "MESSAGE":
                prefix = "✉ [消息]";
                break;
            case "CONNECT":
                prefix = "↗ [连接]";
                break;
            case "DISCONNECT":
                prefix = "↘ [断开]";
                break;
            case "BROADCAST":
                prefix = "📢 [广播]";
                break;
            default:
                prefix = "ℹ [信息]";
        }
        
        System.out.println("[" + timestamp + "] " + prefix + " " + message);
    }
}
