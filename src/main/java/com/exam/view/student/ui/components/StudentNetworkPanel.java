package com.exam.view.student.ui.components;

import com.exam.model.User;
import com.exam.util.NetworkUtil;
import com.exam.util.UIUtil;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private JTextField hostField;
    private JTextField portField;
    private JTextField messageField;
    private JLabel statusLabel;
    private boolean isConnected = false;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public StudentNetworkPanel(User student) {
        this.student = student;
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
        
        // 主机配置
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel hostLabel = new JLabel("服务器地址:");
        hostLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        configPanel.add(hostLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        hostField = new JTextField(NetworkUtil.DEFAULT_HOST);
        hostField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        configPanel.add(hostField, gbc);
        
        // 端口配置
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel portLabel = new JLabel("端口:");
        portLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        configPanel.add(portLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        portField = new JTextField(String.valueOf(NetworkUtil.DEFAULT_PORT));
        portField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        configPanel.add(portField, gbc);
        
        configPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.add(configPanel);
        panel.add(Box.createVerticalStrut(10));
        
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
        
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createVerticalStrut(8));
        statusPanel.add(userLabel);
        statusPanel.add(Box.createVerticalStrut(5));
        
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
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
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText());
            
            client = new NetworkUtil.TcpClient(new NetworkUtil.ClientMessageListener() {
                @Override
                public void onMessageReceived(String message) {
                    SwingUtilities.invokeLater(() -> {
                        appendLog("接收: " + message);
                    });
                }
                
                @Override
                public void onConnected() {
                    SwingUtilities.invokeLater(() -> {
                        isConnected = true;
                        connectButton.setEnabled(false);
                        disconnectButton.setEnabled(true);
                        hostField.setEnabled(false);
                        portField.setEnabled(false);
                        messageField.setEnabled(true);
                        sendButton.setEnabled(true);
                        statusLabel.setText("状态: 已连接");
                        statusLabel.setForeground(UIUtil.SUCCESS_COLOR);
                        appendLog("连接成功！");
                        
                        // 发送用户信息
                        String userInfo = "[" + student.getRealName() + "-" + student.getStudentNumber() + "] 已上线";
                        client.sendMessage(userInfo);
                    });
                }
                
                @Override
                public void onDisconnected() {
                    SwingUtilities.invokeLater(() -> {
                        isConnected = false;
                        connectButton.setEnabled(true);
                        disconnectButton.setEnabled(false);
                        hostField.setEnabled(true);
                        portField.setEnabled(true);
                        messageField.setEnabled(false);
                        sendButton.setEnabled(false);
                        statusLabel.setText("状态: 已断开");
                        statusLabel.setForeground(UIUtil.DANGER_COLOR);
                        appendLog("连接已断开");
                    });
                }
                
                @Override
                public void onError(String error) {
                    SwingUtilities.invokeLater(() -> {
                        appendLog("错误: " + error);
                    });
                }
            });
            
            client.connect(host, port);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口号格式错误", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接服务器失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            appendLog("连接失败: " + e.getMessage());
        }
    }
    
    private void disconnectFromServer() {
        if (client != null) {
            // 发送下线通知
            String userInfo = "[" + student.getRealName() + "-" + student.getStudentNumber() + "] 已下线";
            client.sendMessage(userInfo);
            
            client.disconnect();
        }
    }
    
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && client != null && isConnected) {
            String fullMessage = "[" + student.getRealName() + "]: " + message;
            client.sendMessage(fullMessage);
            appendLog("发送: " + message);
            messageField.setText("");
        }
    }
    
    private void appendLog(String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
