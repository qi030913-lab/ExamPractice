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
    public static final String DEFAULT_HOST = "localhost";
    
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
            
            serverSocket = new ServerSocket(port);
            isRunning = true;
            
            // 创建接受连接的线程
            acceptThread = new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
                        
                        // 为每个客户端创建处理线程
                        ClientHandler handler = new ClientHandler(clientSocket, clientInfo);
                        clients.add(handler);
                        handler.start();
                        
                        if (listener != null) {
                            listener.onClientConnected(clientInfo);
                        }
                    } catch (IOException e) {
                        if (isRunning && listener != null) {
                            listener.onError("接受连接失败: " + e.getMessage());
                        }
                    }
                }
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
            
            socket = new Socket(host, port);
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
