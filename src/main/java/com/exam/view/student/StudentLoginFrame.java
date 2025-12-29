package com.exam.view.student;

import com.exam.exception.AuthenticationException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
import com.exam.util.UIUtil;
import com.exam.view.RoleSelectionFrame;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * 学生登录注册界面
 */
public class StudentLoginFrame extends JFrame {
    private final UserService userService;
    private JTextField realNameField;
    private JTextField studentNumberField;
    private JPasswordField passwordField;

    public StudentLoginFrame() {
        this.userService = new UserService();
        initComponents();
        setTitle("小考试系统 - 学生登录");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 设置窗口图标
        try {
            setIconImage(new ImageIcon(getClass().getClassLoader().getResource("pic/logo.png")).getImage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 添加窗口关闭监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 关闭所有窗口
                System.exit(0);
            }
        });
        
        UIUtil.centerWindow(this);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // 左侧背景图片面板
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    ImageIcon imageIcon = new ImageIcon(getClass().getResource("/pic/lan.jpg"));
                    Image image = imageIcon.getImage();
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    // 如果图片加载失败，使用默认背景色
                    setBackground(new Color(33, 150, 243));
                }
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(350, 500));

        mainPanel.add(leftPanel, BorderLayout.WEST);

        // 右侧登录表单面板
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));

        JLabel welcomeLabel = new JLabel("欢迎回来");
        welcomeLabel.setFont(new Font("SimHei", Font.BOLD, 32));
        welcomeLabel.setForeground(new Color(33, 33, 33));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(welcomeLabel);

        rightPanel.add(Box.createVerticalStrut(10));

        JLabel tipLabel = new JLabel("请登录您的账号继续使用");
        tipLabel.setFont(new Font("SimHei", Font.PLAIN, 14));
        tipLabel.setForeground(new Color(120, 120, 120));
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(tipLabel);

        rightPanel.add(Box.createVerticalStrut(40));

        // 姓名输入
        JLabel nameLabel = new JLabel("姓名");
        nameLabel.setFont(new Font("SimHei", Font.PLAIN, 14));
        nameLabel.setForeground(new Color(60, 60, 60));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(nameLabel);
        rightPanel.add(Box.createVerticalStrut(8));

        realNameField = new JTextField();
        realNameField.setFont(new Font("SimHei", Font.PLAIN, 14));
        realNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        realNameField.setBorder(new RoundedBorder(new Color(200, 200, 200), 1, 8));
        realNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFieldFocusListener(realNameField);
        rightPanel.add(realNameField);

        rightPanel.add(Box.createVerticalStrut(20));

        // 学号输入
        JLabel numberLabel = new JLabel("学号");
        numberLabel.setFont(new Font("SimHei", Font.PLAIN, 14));
        numberLabel.setForeground(new Color(60, 60, 60));
        numberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(numberLabel);
        rightPanel.add(Box.createVerticalStrut(8));

        studentNumberField = new JTextField();
        studentNumberField.setFont(new Font("SimHei", Font.PLAIN, 14));
        studentNumberField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        studentNumberField.setBorder(new RoundedBorder(new Color(200, 200, 200), 1, 8));
        studentNumberField.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFieldFocusListener(studentNumberField);
        rightPanel.add(studentNumberField);

        rightPanel.add(Box.createVerticalStrut(20));

        // 密码输入
        JLabel passwordLabel = new JLabel("密码");
        passwordLabel.setFont(new Font("SimHei", Font.PLAIN, 14));
        passwordLabel.setForeground(new Color(60, 60, 60));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(passwordLabel);
        rightPanel.add(Box.createVerticalStrut(8));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SimHei", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setBorder(new RoundedBorder(new Color(200, 200, 200), 1, 8));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFieldFocusListener(passwordField);
        rightPanel.add(passwordField);

        rightPanel.add(Box.createVerticalStrut(30));

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // 登录按钮
        JButton loginButton = createStyledButton("登录", new Color(33, 150, 243), Color.WHITE, true);
        loginButton.setPreferredSize(new Dimension(150, 45));
        loginButton.setMaximumSize(new Dimension(150, 45));
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);

        buttonPanel.add(Box.createHorizontalStrut(15));

        // 注册按钮
        JButton registerButton = createStyledButton("注册", Color.WHITE, new Color(33, 150, 243), false);
        registerButton.setPreferredSize(new Dimension(150, 45));
        registerButton.setMaximumSize(new Dimension(150, 45));
        registerButton.addActionListener(e -> showRegisterDialog());
        buttonPanel.add(registerButton);

        buttonPanel.add(Box.createHorizontalGlue());

        rightPanel.add(buttonPanel);

        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // 回车直接登录
        passwordField.addActionListener(e -> handleLogin());

        add(mainPanel);
    }

    /**
     * 输入框焦点监听器
     */
    private void addFieldFocusListener(JTextField field) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(new RoundedBorder(new Color(33, 150, 243), 2, 8));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(new RoundedBorder(new Color(200, 200, 200), 1, 8));
            }
        });
    }

    /**
     * 注册对话框输入框焦点监听器
     */
    private void addFocusListener(JTextField field) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(new RoundedBorder(new Color(33, 150, 243), 2, 8));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(new RoundedBorder(new Color(144, 202, 249), 1, 8));
            }
        });
    }

    private JButton createStyledButton(String text, Color bg, Color fg, boolean filled) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (filled) {
                    g2d.setColor(getBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else {
                    g2d.setColor(getBackground());
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2d.setColor(new Color(33, 150, 243));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                }
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SimHei", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (filled) {
                    btn.setBackground(bg.darker());
                } else {
                    btn.setBackground(new Color(240, 248, 255));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    /**
     * 登录逻辑
     */
    private void handleLogin() {
        String realName = realNameField.getText().trim();
        String studentNumber = studentNumberField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (realName.isEmpty()) {
            UIUtil.showWarning(this, "请输入姓名");
            realNameField.requestFocus();
            return;
        }
        if (studentNumber.isEmpty()) {
            UIUtil.showWarning(this, "请输入学号");
            studentNumberField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            UIUtil.showWarning(this, "请输入密码");
            passwordField.requestFocus();
            return;
        }

        try {
            User user = userService.login(realName, studentNumber, password, UserRole.STUDENT);
            SwingUtilities.invokeLater(() -> {
                new StudentMainFrame(user).setVisible(true);
                dispose();
            });
        } catch (AuthenticationException ex) {
            UIUtil.showError(this, ex.getMessage());
            passwordField.setText("");
            passwordField.requestFocus();
        } catch (Exception ex) {
            UIUtil.showError(this, "登录失败：" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * 注册对话框
     */
    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "学生注册", true);
        dialog.setSize(400, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(new Color(230, 240, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 标题
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(230, 240, 250));
        JLabel titleLabel = new JLabel("学生账户注册");
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 150, 243));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(titleLabel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 表单
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(new Color(245, 250, 255));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField nameField = new JTextField();
        JTextField numField = new JTextField();
        JPasswordField pwdField = new JPasswordField();
        JPasswordField confirmPwdField = new JPasswordField();

        formPanel.add(createFormField("姓 名", nameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormField("学 号", numField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormField("密 码", pwdField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormField("确认密码", confirmPwdField));

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(new Color(230, 240, 250));

        JButton registerBtn = createStyledButton("注册", new Color(33, 150, 243), Color.WHITE, true);
        registerBtn.addActionListener(e -> {
            String rn = nameField.getText().trim();
            String sn = numField.getText().trim();
            String pw = new String(pwdField.getPassword());
            String cpw = new String(confirmPwdField.getPassword());

            if (rn.isEmpty()) { UIUtil.showWarning(dialog, "请输入姓名"); return; }
            if (sn.isEmpty()) { UIUtil.showWarning(dialog, "请输入学号"); return; }
            if (pw.isEmpty()) { UIUtil.showWarning(dialog, "请输入密码"); return; }
            if (!pw.equals(cpw)) { UIUtil.showWarning(dialog, "两次密码不一致"); return; }

            try {
                User user = new User();
                user.setRealName(rn);
                user.setStudentNumber(sn);
                user.setPassword(pw);
                user.setRole(UserRole.STUDENT);
                user.setGender("MALE");
                user.setStatus("ACTIVE");
                userService.register(user);
                UIUtil.showInfo(dialog, "注册成功，请登录！");
                dialog.dispose();
                realNameField.setText(rn);
                studentNumberField.setText(sn);
                passwordField.setText("");
                passwordField.requestFocus();
            } catch (Exception ex) {
                UIUtil.showError(dialog, "注册失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JButton cancelBtn = createStyledButton("取消", new Color(120, 144, 156), Color.WHITE, true);
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(registerBtn);
        buttonPanel.add(cancelBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createFormField(String label, JTextField field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 250, 255));

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SimHei", Font.PLAIN, 12));
        fieldLabel.setForeground(new Color(30, 70, 120));
        fieldLabel.setOpaque(false);
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(fieldLabel);

        if (field instanceof JPasswordField) {
            field.setFont(new Font("SimHei", Font.PLAIN, 10));
        } else {
            field.setFont(new Font("SimHei", Font.PLAIN, 12));
        }
        field.setBorder(new RoundedBorder(new Color(144, 202, 249), 1, 8));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFocusListener(field);
        panel.add(field);

        return panel;
    }

    /**
     * 圆角边框类
     */
    static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = thickness + 2;
            return insets;
        }
    }
}
