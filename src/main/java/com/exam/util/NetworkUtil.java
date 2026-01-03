package com.exam.util;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络工具类
 * 提供TCP Socket通信的基础功能
 * 
 * 知识点：
 * 1. Socket编程 - TCP/IP通信
 * 2. ServerSocket - 服务端监听
 * 3. 多线程处理客户端连接
 * 4. 输入输出流 - 网络数据传输
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class NetworkUtil {
    
    // 默认服务器端口
    public static final int DEFAULT_PORT = 8888;
    
    // 默认服务器地址
    public static final String DEFAULT_HOST = "112.124.44.144";
    
    /**
     * 服务端监听器接口
     * 用于处理接收到的消息
     */
    public interface MessageListener {
        void onMessageReceived(String message, String clientInfo);
        void onClientConnected(String clientInfo);
        void onClientDisconnected(String clientInfo);
        void onError(String error);
    }
    
    /**
     * 客户端消息监听器接口
     */
    public interface ClientMessageListener {
        void onMessageReceived(String message);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }
    
    /**
     * TCP服务器类
     * 教师端使用，作为Server监听学生端连接
     */
    public static class TcpServer {
        private ServerSocket serverSocket;
        private boolean isRunning = false;
        private MessageListener listener;
        private List<ClientHandler> clients = new ArrayList<>();
        private Thread acceptThread;
        
        public TcpServer(MessageListener listener) {
            this.listener = listener;
        }
        
        /**
         * 启动服务器
         */
        public void start(int port) throws IOException {
            if (isRunning) {
                throw new IllegalStateException("服务器已经在运行中");
            }
            
            // 绑定到所有网络接口（0.0.0.0），允许外部连接
            System.out.println("[DEBUG] 正在启动服务器...");
            System.out.println("[DEBUG] 端口: " + port);
            
            try {
                InetAddress bindAddr = InetAddress.getByName("0.0.0.0");
                System.out.println("[DEBUG] 绑定地址: " + bindAddr.getHostAddress());
                
                serverSocket = new ServerSocket(port, 50, bindAddr);
                
                System.out.println("[DEBUG] ServerSocket创建成功");
                System.out.println("[DEBUG] 本地地址: " + serverSocket.getInetAddress());
                System.out.println("[DEBUG] 本地端口: " + serverSocket.getLocalPort());
                System.out.println("[DEBUG] 等待客户端连接...");
                
            } catch (IOException e) {
                System.err.println("[ERROR] 服务器启动失败: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
            isRunning = true;
            
            // 创建接受连接的线程
            acceptThread = new Thread(() -> {
                System.out.println("[DEBUG] 接受连接线程已启动");
                while (isRunning) {
                    try {
                        System.out.println("[DEBUG] 等待客户端连接... (阻塞中)");
                        Socket clientSocket = serverSocket.accept();
                        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                        
                        System.out.println("[DEBUG] 接受到客户端连接: " + clientInfo);
                        System.out.println("[DEBUG] 客户端远程地址: " + clientSocket.getRemoteSocketAddress());
                        
                        // 为每个客户端创建处理线程
                        ClientHandler handler = new ClientHandler(clientSocket, clientInfo);
                        clients.add(handler);
                        handler.start();
                        
                        if (listener != null) {
                            listener.onClientConnected(clientInfo);
                        }
                    } catch (IOException e) {
                        System.err.println("[ERROR] 接受连接失败: " + e.getMessage());
                        e.printStackTrace();
                        if (isRunning && listener != null) {
                            listener.onError("接受连接失败: " + e.getMessage());
                        }
                    }
                }
                System.out.println("[DEBUG] 接受连接线程已退出");
            }, "Server-Accept-Thread");
            
            acceptThread.start();
        }
        
        /**
         * 停止服务器
         */
        public void stop() {
            isRunning = false;
            
            // 关闭所有客户端连接
            for (ClientHandler handler : clients) {
                handler.close();
            }
            clients.clear();
            
            // 关闭服务器Socket
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        /**
         * 广播消息给所有客户端
         */
        public void broadcast(String message) {
            for (ClientHandler handler : clients) {
                handler.sendMessage(message);
            }
        }
        
        /**
         * 获取在线客户端数量
         */
        public int getClientCount() {
            return clients.size();
        }
        
        /**
         * 客户端处理器（内部类）
         */
        private class ClientHandler extends Thread {
            private Socket socket;
            private String clientInfo;
            private BufferedReader reader;
            private PrintWriter writer;
            private boolean isConnected = true;
            
            public ClientHandler(Socket socket, String clientInfo) {
                this.socket = socket;
                this.clientInfo = clientInfo;
                setName("ClientHandler-" + clientInfo);
            }
            
            @Override
            public void run() {
                try {
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                    
                    String message;
                    while (isConnected && (message = reader.readLine()) != null) {
                        if (listener != null) {
                            listener.onMessageReceived(message, clientInfo);
                        }
                    }
                } catch (IOException e) {
                    if (isConnected && listener != null) {
                        listener.onError("客户端通信异常 [" + clientInfo + "]: " + e.getMessage());
                    }
                } finally {
                    close();
                    clients.remove(this);
                    if (listener != null) {
                        listener.onClientDisconnected(clientInfo);
                    }
                }
            }
            
            public void sendMessage(String message) {
                if (writer != null && isConnected) {
                    writer.println(message);
                }
            }
            
            public void close() {
                isConnected = false;
                try {
                    if (reader != null) reader.close();
                    if (writer != null) writer.close();
                    if (socket != null && !socket.isClosed()) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * TCP客户端类
     * 学生端使用，作为Client连接教师端服务器
     */
    public static class TcpClient {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private ClientMessageListener listener;
        private Thread receiveThread;
        private boolean isConnected = false;
        
        public TcpClient(ClientMessageListener listener) {
            this.listener = listener;
        }
        
        /**
         * 连接到服务器
         */
        public void connect(String host, int port) throws IOException {
            if (isConnected) {
                throw new IllegalStateException("已经连接到服务器");
            }
            
            System.out.println("[DEBUG] 客户端开始连接...");
            System.out.println("[DEBUG] 目标主机: " + host);
            System.out.println("[DEBUG] 目标端口: " + port);
            
            try {
                // 尝试解析主机地址
                System.out.println("[DEBUG] 步骤1: 解析主机名...");
                InetAddress addr = InetAddress.getByName(host);
                System.out.println("[DEBUG] 解析后的IP地址: " + addr.getHostAddress());
                System.out.println("[DEBUG] 主机名: " + addr.getHostName());
                
                // 测试主机可达性
                System.out.println("[DEBUG] 步骤2: 测试主机可达性 (ping测试, 5秒超时)...");
                boolean reachable = false;
                try {
                    reachable = addr.isReachable(5000);
                    System.out.println("[DEBUG] Ping结果: " + (reachable ? "可达" : "不可达"));
                } catch (Exception e) {
                    System.out.println("[DEBUG] Ping测试失败: " + e.getMessage());
                }
                
                if (!reachable) {
                    System.out.println("[WARNING] 主机ping不通，但仍尝试TCP连接...");
                }
                
                // 获取本地网络信息
                System.out.println("[DEBUG] 步骤3: 获取本地网络信息...");
                try {
                    InetAddress localAddr = InetAddress.getLocalHost();
                    System.out.println("[DEBUG] 本地主机名: " + localAddr.getHostName());
                    System.out.println("[DEBUG] 本地IP地址: " + localAddr.getHostAddress());
                } catch (Exception e) {
                    System.out.println("[DEBUG] 获取本地信息失败: " + e.getMessage());
                }
                
                // 创建Socket连接
                System.out.println("[DEBUG] 步骤4: 创建Socket连接...");
                System.out.println("[DEBUG] 连接超时设置: 10000ms (10秒)");
                socket = new Socket();
                
                long startTime = System.currentTimeMillis();
                socket.connect(new InetSocketAddress(host, port), 10000);
                long endTime = System.currentTimeMillis();
                
                System.out.println("[DEBUG] Socket连接成功! 耗时: " + (endTime - startTime) + "ms");
                System.out.println("[DEBUG] 本地地址: " + socket.getLocalAddress() + ":" + socket.getLocalPort());
                System.out.println("[DEBUG] 远程地址: " + socket.getRemoteSocketAddress());
                System.out.println("[DEBUG] 连接状态: connected=" + socket.isConnected() + ", closed=" + socket.isClosed());
                
            } catch (UnknownHostException e) {
                System.err.println("[ERROR] 无法解析主机名: " + host);
                System.err.println("[ERROR] 异常类型: UnknownHostException");
                System.err.println("[ERROR] 错误详情: " + e.getMessage());
                e.printStackTrace();
                throw new IOException("无法解析主机名 '" + host + "': " + e.getMessage(), e);
            } catch (SocketTimeoutException e) {
                System.err.println("[ERROR] 连接超时");
                System.err.println("[ERROR] 异常类型: SocketTimeoutException");
                System.err.println("[ERROR] 超时时间: 10秒");
                e.printStackTrace();
                throw new IOException("连接超时 (10秒)，服务器可能不可达", e);
            } catch (ConnectException e) {
                System.err.println("[ERROR] 连接被拒绝");
                System.err.println("[ERROR] 异常类型: ConnectException");
                System.err.println("[ERROR] 目标: " + host + ":" + port);
                System.err.println("[ERROR] 原因分析:");
                System.err.println("[ERROR]   1. 服务器端未在该端口监听 (最可能)");
                System.err.println("[ERROR]   2. 防火墙阻止了连接");
                System.err.println("[ERROR]   3. 服务器只监听localhost，未绑定0.0.0.0");
                System.err.println("[ERROR] 错误详情: " + e.getMessage());
                e.printStackTrace();
                throw new IOException("连接被拒绝，请确认服务器已启动并监听 " + port + " 端口", e);
            } catch (IOException e) {
                System.err.println("[ERROR] IO异常");
                System.err.println("[ERROR] 异常类型: " + e.getClass().getName());
                System.err.println("[ERROR] 错误详情: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            isConnected = true;
            
            if (listener != null) {
                listener.onConnected();
            }
            
            // 启动接收消息的线程
            receiveThread = new Thread(() -> {
                try {
                    String message;
                    while (isConnected && (message = reader.readLine()) != null) {
                        if (listener != null) {
                            listener.onMessageReceived(message);
                        }
                    }
                } catch (IOException e) {
                    if (isConnected && listener != null) {
                        listener.onError("接收消息失败: " + e.getMessage());
                    }
                } finally {
                    disconnect();
                }
            }, "Client-Receive-Thread");
            
            receiveThread.start();
        }
        
        /**
         * 发送消息到服务器
         */
        public void sendMessage(String message) {
            if (writer != null && isConnected) {
                writer.println(message);
            }
        }
        
        /**
         * 断开连接
         */
        public void disconnect() {
            isConnected = false;
            
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (listener != null) {
                listener.onDisconnected();
            }
        }
        
        /**
         * 检查是否已连接
         */
        public boolean isConnected() {
            return isConnected && socket != null && !socket.isClosed();
        }
    }
}
