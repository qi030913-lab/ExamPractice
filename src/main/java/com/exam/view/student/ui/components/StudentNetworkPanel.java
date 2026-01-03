package com.exam.view.student.ui.components;

import com.exam.model.User;
import com.exam.model.NetworkLog;
import com.exam.dao.NetworkLogDao;
import com.exam.util.NetworkUtil;
import com.exam.util.UIUtil;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 学生端网络通信面板
 * 作为Client端，连接教师端服务器并发送消息
 * 
 * 知识点：
 * 1. Socket客户端编程
 * 2. 网络连接管理
 * 3. 消息收发
 * 4. 异步消息处理
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class StudentNetworkPanel extends JPanel {
    
    private User student;
    private NetworkUtil.TcpClient client;
    private JTextArea logArea;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton sendButton;
    private JTextField messageField;
    private JLabel statusLabel;
    private boolean isConnected = false;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private NetworkLogDao networkLogDao;
    
    public StudentNetworkPanel(User student) {
        this.student = student;
        this.networkLogDao = new NetworkLogDao();
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("网络通信客户端（学生端）");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        
        JLabel descLabel = new JLabel("连接到教师端服务器，进行实时通信");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(UIUtil.TEXT_GRAY);
        
        JPanel titleContent = new JPanel();
        titleContent.setLayout(new BoxLayout(titleContent, BoxLayout.Y_AXIS));
        titleContent.setBackground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleContent.add(titleLabel);
        titleContent.add(Box.createVerticalStrut(5));
        titleContent.add(descLabel);
        
        titlePanel.add(titleContent, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);
        
        // 主内容区
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(Color.WHITE);
        
        // 左侧控制面板
        JPanel controlPanel = createControlPanel();
        controlPanel.setPreferredSize(new Dimension(300, 0));
        mainPanel.add(controlPanel, BorderLayout.WEST);
        
        // 右侧日志面板
        JPanel logPanel = createLogPanel();
        mainPanel.add(logPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 状态显示
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "连接状态",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 13)
        ));
        
        statusLabel = new JLabel("状态: 未连接");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(UIUtil.TEXT_GRAY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel userLabel = new JLabel("用户: " + student.getRealName() + " (" + student.getStudentNumber() + ")");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        userLabel.setForeground(UIUtil.TEXT_GRAY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
//        JLabel serverLabel = new JLabel("服务器: " + NetworkUtil.DEFAULT_HOST + ":" + NetworkUtil.DEFAULT_PORT);
//        serverLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        serverLabel.setForeground(UIUtil.TEXT_GRAY);
//        serverLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(userLabel);
        statusPanel.add(Box.createVerticalStrut(5));
//        statusPanel.add(serverLabel);
        statusPanel.add(Box.createVerticalStrut(5));
        
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.add(statusPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // 连接按钮
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 8));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        connectButton = new JButton("连接服务器");
        connectButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        connectButton.setBackground(UIUtil.SUCCESS_COLOR);
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        connectButton.addActionListener(e -> connectToServer());
        
        disconnectButton = new JButton("断开连接");
        disconnectButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        disconnectButton.setBackground(UIUtil.DANGER_COLOR);
        disconnectButton.setForeground(Color.WHITE);
        disconnectButton.setFocusPainted(false);
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnectFromServer());
        
        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // 发送消息区域
        JPanel sendPanel = new JPanel(new BorderLayout(5, 5));
        sendPanel.setBackground(Color.WHITE);
        sendPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "发送消息",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 13)
        ));
        
        messageField = new JTextField();
        messageField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        messageField.setEnabled(false);
        messageField.addActionListener(e -> sendMessage());
        
        sendButton = new JButton("发送");
        sendButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        sendButton.setBackground(UIUtil.PRIMARY_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendMessage());
        
        sendPanel.add(messageField, BorderLayout.CENTER);
        sendPanel.add(sendButton, BorderLayout.EAST);
        
        sendPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.add(sendPanel);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "通信日志",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 13)
        ));
        
        logArea = new JTextArea();
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void connectToServer() {
        // 显示连接中提示
        connectButton.setEnabled(false);
        connectButton.setText("连接中...");
        statusLabel.setText("状态: 正在连接...");
        statusLabel.setForeground(new Color(255, 165, 0)); // 橙色
        
        // 使用SwingWorker异步连接，避免界面卡顿
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            private Exception connectionError = null;
            
            @Override
            protected Void doInBackground() {
                try {
                    String host = NetworkUtil.DEFAULT_HOST;
                    int port = NetworkUtil.DEFAULT_PORT;
                    
                    client = new NetworkUtil.TcpClient(new NetworkUtil.ClientMessageListener() {
                        @Override
                        public void onMessageReceived(String message) {
                            SwingUtilities.invokeLater(() -> {
                                // 解析消息，提取用户名和内容
                                appendChatLog(message, "RECEIVE");
                            });
                        }
                        
                        @Override
                        public void onConnected() {
                            SwingUtilities.invokeLater(() -> {
                                isConnected = true;
                                connectButton.setText("连接服务器");
                                connectButton.setEnabled(false);
                                disconnectButton.setEnabled(true);
                                messageField.setEnabled(true);
                                sendButton.setEnabled(true);
                                statusLabel.setText("状态: 已连接");
                                statusLabel.setForeground(UIUtil.SUCCESS_COLOR);
                                
                                // 加载历史聊天记录
                                loadHistoryLogs();
                                
                                // 发送用户信息（不记录到日志）
                                String userInfo = "[" + student.getRealName() + "-" + student.getStudentNumber() + "] 已上线";
                                client.sendMessage(userInfo);
                            });
                        }
                        
                        @Override
                        public void onDisconnected() {
                            SwingUtilities.invokeLater(() -> {
                                isConnected = false;
                                connectButton.setText("连接服务器");
                                connectButton.setEnabled(true);
                                disconnectButton.setText("断开连接");
                                disconnectButton.setEnabled(false);
                                messageField.setEnabled(false);
                                sendButton.setEnabled(false);
                                statusLabel.setText("状态: 已断开");
                                statusLabel.setForeground(UIUtil.DANGER_COLOR);
                            });
                        }
                        
                        @Override
                        public void onError(String error) {
                            SwingUtilities.invokeLater(() -> {
                                System.err.println("客户端错误: " + error);
                            });
                        }
                    });
                    
                    client.connect(host, port);
                    
                } catch (IOException e) {
                    connectionError = e;
                } catch (Exception e) {
                    connectionError = e;
                }
                return null;
            }
            
            @Override
            protected void done() {
                if (connectionError != null) {
                    // 连接失败，恢复按钮状态
                    connectButton.setText("连接服务器");
                    connectButton.setEnabled(true);
                    statusLabel.setText("状态: 连接失败");
                    statusLabel.setForeground(UIUtil.DANGER_COLOR);
                    
                    String msg = connectionError instanceof IOException 
                        ? "连接服务器失败: " + connectionError.getMessage() 
                        : "未知错误: " + connectionError.getMessage();
                    JOptionPane.showMessageDialog(StudentNetworkPanel.this, 
                        msg + "\n\n可能原因:\n1. 服务器未启动\n2. 服务器防火墙阻止了连接\n3. 网络不可达\n4. IP地址或端口错误", 
                        "连接失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void disconnectFromServer() {
        if (client != null) {
            // 禁用断开按钮，防止重复点击
            disconnectButton.setEnabled(false);
            disconnectButton.setText("断开中...");
            
            // 使用SwingWorker异步断开连接，避免UI卡顿
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        // 发送下线通知
                        String userInfo = "[" + student.getRealName() + "-" + student.getStudentNumber() + "] 已下线";
                        client.sendMessage(userInfo);
                        
                        // 短暂延迟，确保消息发送完成
                        Thread.sleep(100);
                        
                        // 断开连接
                        client.disconnect();
                    } catch (Exception e) {
                        // 如果断开失败，也要恢复UI状态
                        SwingUtilities.invokeLater(() -> {
                            isConnected = false;
                            connectButton.setText("连接服务器");
                            connectButton.setEnabled(true);
                            disconnectButton.setText("断开连接");
                            disconnectButton.setEnabled(false);
                            messageField.setEnabled(false);
                            sendButton.setEnabled(false);
                            statusLabel.setText("状态: 已断开");
                            statusLabel.setForeground(UIUtil.DANGER_COLOR);
                        });
                    }
                    return null;
                }
            };
            worker.execute();
        }
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && client != null && isConnected) {
            String fullMessage = "[" + student.getRealName() + "]: " + message;
            client.sendMessage(fullMessage);
            // 显示自己发送的消息
            appendChatMessage(student.getRealName(), message, "SEND");
            messageField.setText("");
        }
    }
    
    /**
     * 解析并显示聊天消息
     */
    private void appendChatLog(String message, String messageType) {
        // 解析消息格式: [用户名]: 消息内容
        if (message.contains("]: ")) {
            int nameStart = message.indexOf("[");
            int nameEnd = message.indexOf("]:");
            if (nameStart >= 0 && nameEnd > nameStart) {
                String userName = message.substring(nameStart + 1, nameEnd);
                String content = message.substring(nameEnd + 2).trim();
                appendChatMessage(userName, content, messageType);
            }
        }
    }
    
    /**
     * 显示聊天消息
     */
    private void appendChatMessage(String userName, String message, String messageType) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        logArea.append("[" + timestamp + "] " + userName + ": " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        
        // 保存到数据库
        try {
            String logContent = userName + ": " + message;
            NetworkLog log = new NetworkLog(student.getUserId(), messageType, logContent);
            networkLogDao.insert(log);
        } catch (Exception e) {
            // 数据库保存失败不影响显示
            System.err.println("保存通信日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 加载历史聊天记录
     */
    private void loadHistoryLogs() {
        try {
            List<NetworkLog> logs = networkLogDao.findByStudentId(student.getUserId());
            if (!logs.isEmpty()) {
                logArea.append("============ 历史聊天记录 ============\n");
                for (NetworkLog log : logs) {
                    String timestamp = log.getCreateTime().format(timeFormatter);
                    logArea.append("[" + timestamp + "] " + log.getMessageContent() + "\n");
                }
                logArea.append("============ 当前会话 ============\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        } catch (Exception e) {
            System.err.println("加载历史聊天记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 公开的断开连接方法，供外部调用（如窗口关闭时）
     */
    public void disconnect() {
        if (client != null && isConnected) {
            disconnectFromServer();
        }
    }
}
