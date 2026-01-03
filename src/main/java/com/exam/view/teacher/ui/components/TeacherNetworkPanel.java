package com.exam.view.teacher.ui.components;

import com.exam.util.NetworkUtil;
import com.exam.util.UIUtil;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 教师端网络通信面板
 * 作为Server端，接收学生端的连接和消息
 * 
 * 知识点：
 * 1. ServerSocket - 服务端监听
 * 2. 多客户端并发处理
 * 3. 消息广播
 * 4. Swing界面与网络通信的集成
 * 
 * @author 在线考试系统开发组
 * @version 1.0
 */
public class TeacherNetworkPanel extends JPanel {
    
    private NetworkUtil.TcpServer server;
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton broadcastButton;
    private JTextField portField;
    private JTextField messageField;
    private JLabel statusLabel;
    private JLabel clientCountLabel;
    private boolean isServerRunning = false;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public TeacherNetworkPanel() {
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("网络通信服务器（教师端）");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(UIUtil.PRIMARY_COLOR);
        
        JLabel descLabel = new JLabel("启动服务器后，学生端可以连接并发送消息");
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
        controlPanel.setPreferredSize(new Dimension(350, 0));
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
        
        // 服务器配置
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBackground(Color.WHITE);
        configPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "服务器配置",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 13)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 端口配置
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel portLabel = new JLabel("监听端口:");
        portLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        configPanel.add(portLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        portField = new JTextField(String.valueOf(NetworkUtil.DEFAULT_PORT));
        portField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        configPanel.add(portField, gbc);
        
        configPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.add(configPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // 状态显示
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "服务器状态",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 13)
        ));
        
        statusLabel = new JLabel("状态: 未启动");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(UIUtil.TEXT_GRAY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        clientCountLabel = new JLabel("在线客户端: 0");
        clientCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clientCountLabel.setForeground(UIUtil.TEXT_GRAY);
        clientCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(clientCountLabel);
        statusPanel.add(Box.createVerticalStrut(5));
        
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.add(statusPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // 控制按钮
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 8));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        startButton = new JButton("启动服务器");
        startButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        startButton.setBackground(UIUtil.SUCCESS_COLOR);
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> startServer());
        
        stopButton = new JButton("停止服务器");
        stopButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        stopButton.setBackground(UIUtil.DANGER_COLOR);
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopServer());
        
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // 广播消息区域
        JPanel broadcastPanel = new JPanel(new BorderLayout(5, 5));
        broadcastPanel.setBackground(Color.WHITE);
        broadcastPanel.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "广播消息",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 13)
        ));
        
        messageField = new JTextField();
        messageField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        messageField.setEnabled(false);
        
        broadcastButton = new JButton("发送");
        broadcastButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        broadcastButton.setBackground(UIUtil.PRIMARY_COLOR);
        broadcastButton.setForeground(Color.WHITE);
        broadcastButton.setFocusPainted(false);
        broadcastButton.setEnabled(false);
        broadcastButton.addActionListener(e -> sendBroadcast());
        
        broadcastPanel.add(messageField, BorderLayout.CENTER);
        broadcastPanel.add(broadcastButton, BorderLayout.EAST);
        
        broadcastPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.add(broadcastPanel);
        
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
    
    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            
            appendLog("============ 启动服务器 ============");
            appendLog("监听端口: " + port);
            appendLog("绑定地址: 0.0.0.0 (允许外部连接)");
            
            server = new NetworkUtil.TcpServer(new NetworkUtil.MessageListener() {
                @Override
                public void onMessageReceived(String message, String clientInfo) {
                    SwingUtilities.invokeLater(() -> {
                        appendLog("接收 [" + clientInfo + "]: " + message);
                    });
                }
                
                @Override
                public void onClientConnected(String clientInfo) {
                    SwingUtilities.invokeLater(() -> {
                        appendLog("客户端连接: " + clientInfo);
                        updateClientCount();
                    });
                }
                
                @Override
                public void onClientDisconnected(String clientInfo) {
                    SwingUtilities.invokeLater(() -> {
                        appendLog("客户端断开: " + clientInfo);
                        updateClientCount();
                    });
                }
                
                @Override
                public void onError(String error) {
                    SwingUtilities.invokeLater(() -> {
                        appendLog("错误: " + error);
                        System.err.println("服务器错误: " + error);
                    });
                }
            });
            
            appendLog("正在启动服务器...");
            server.start(port);
            appendLog("服务器启动成功！");
            
            isServerRunning = true;
            
            // 更新UI状态
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);
            messageField.setEnabled(true);
            broadcastButton.setEnabled(true);
            statusLabel.setText("状态: 运行中");
            statusLabel.setForeground(UIUtil.SUCCESS_COLOR);
            
            appendLog("服务器启动成功，监听端口: " + port);
            appendLog("等待客户端连接...");
            appendLog("提示: 确保防火墙允许 " + port + " 端口通信");
            
        } catch (NumberFormatException e) {
            String msg = "端口号格式错误: " + e.getMessage();
            appendLog(msg);
            System.err.println(msg);
            JOptionPane.showMessageDialog(this, "端口号格式错误", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            String msg = "启动服务器失败: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")";
            appendLog(msg);
            appendLog("可能原因:");
            appendLog("1. 端口已被占用");
            appendLog("2. 没有权限绑定该端口");
            appendLog("3. 网络配置错误");
            System.err.println(msg);
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, msg, "启动失败", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            String msg = "未知错误: " + e.getMessage();
            appendLog(msg);
            System.err.println(msg);
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, msg, "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopServer() {
        if (server != null) {
            server.stop();
            isServerRunning = false;
            
            // 更新UI状态
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            portField.setEnabled(true);
            messageField.setEnabled(false);
            broadcastButton.setEnabled(false);
            statusLabel.setText("状态: 已停止");
            statusLabel.setForeground(UIUtil.DANGER_COLOR);
            clientCountLabel.setText("在线客户端: 0");
            
            appendLog("服务器已停止");
        }
    }
    
    private void sendBroadcast() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && server != null) {
            server.broadcast(message);
            appendLog("广播消息: " + message);
            messageField.setText("");
        }
    }
    
    private void updateClientCount() {
        if (server != null) {
            int count = server.getClientCount();
            clientCountLabel.setText("在线客户端: " + count);
        }
    }
    
    private void appendLog(String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
