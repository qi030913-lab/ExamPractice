package com.exam.view.teacher;

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
 * 教师登录注册界面
 */
public class TeacherLoginFrame extends JFrame {
    private final UserService userService;
    private JTextField realNameField;
    private JTextField teacherNumberField;
    private JPasswordField passwordField;

    public TeacherLoginFrame() {
        this.userService = new UserService();
        initComponents();
        setTitle("考试练习系统 - 教师登录");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
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
        mainPanel.setBackground(new Color(230, 240, 250));

        // 顶部 Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(new Color(230, 240, 250));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JLabel titleLabel = new JLabel("教师登录");
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 24));
        titleLabel.setForeground(new Color(76, 175, 80));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(titleLabel);

        JLabel subTitleLabel = new JLabel("TEACHER LOGIN");
        subTitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subTitleLabel.setForeground(new Color(102, 187, 106));
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(subTitleLabel);

        mainPanel.add(logoPanel, BorderLayout.NORTH);

        // 中间表单面板
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(230, 240, 250));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(new Color(129, 199, 132), 1, 15),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        formPanel.setPreferredSize(new Dimension(400, 260));

        // 姓名输入
        formPanel.add(createInputRow("姓　　名：", realNameField = new JTextField()));
        formPanel.add(Box.createVerticalStrut(15));

        // 教工号输入
        formPanel.add(createInputRow("教 工 号：", teacherNumberField = new JTextField()));
        formPanel.add(Box.createVerticalStrut(15));

        // 密码输入
        formPanel.add(createInputRow("密　　码：", passwordField = new JPasswordField()));
        formPanel.add(Box.createVerticalStrut(20));

        // 按钮
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(new Color(245, 250, 255));
        JButton loginButton = createStyledButton("登录", new Color(76, 175, 80), Color.WHITE);
        loginButton.addActionListener(e -> handleLogin());

        JButton registerButton = createStyledButton("注册", new Color(102, 187, 106), Color.WHITE);
        registerButton.addActionListener(e -> showRegisterDialog());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        formPanel.add(buttonPanel);

        centerPanel.add(formPanel);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 回车直接登录
        passwordField.addActionListener(e -> handleLogin());

        add(mainPanel);
    }

    /**
     * 输入行
     */
    private JPanel createInputRow(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(new Color(245, 250, 255));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SimHei", Font.PLAIN, 14));
        label.setForeground(new Color(30, 70, 120));
        label.setPreferredSize(new Dimension(85, 30));
        label.setOpaque(false);
        panel.add(label, BorderLayout.WEST);

        field.setFont(new Font("SimHei", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)));
        addFocusListener(field);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 焦点边框变化
     */
    private void addFocusListener(JTextField field) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(new RoundedBorder(new Color(76, 175, 80), 2, 8));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(new RoundedBorder(new Color(129, 199, 132), 1, 8));
            }
        });
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SimHei", Font.BOLD, 14));
        btn.setForeground(Color.BLACK);
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(bg.darker()); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(bg); }
        });
        return btn;
    }

    /**
     * 登录逻辑
     */
    private void handleLogin() {
        String realName = realNameField.getText().trim();
        String teacherNumber = teacherNumberField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (realName.isEmpty()) {
            UIUtil.showWarning(this, "请输入姓名");
            realNameField.requestFocus();
            return;
        }
        if (teacherNumber.isEmpty()) {
            UIUtil.showWarning(this, "请输入教工号");
            teacherNumberField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            UIUtil.showWarning(this, "请输入密码");
            passwordField.requestFocus();
            return;
        }

        try {
            User user = userService.login(realName, teacherNumber, password, UserRole.TEACHER);
            SwingUtilities.invokeLater(() -> {
                new TeacherMainFrame(user).setVisible(true);
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
        JDialog dialog = new JDialog(this, "教师注册", true);
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
        JLabel titleLabel = new JLabel("教师账户注册");
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 18));
        titleLabel.setForeground(new Color(76, 175, 80));
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
        formPanel.add(createFormField("教工号", numField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormField("密 码", pwdField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFormField("确认密码", confirmPwdField));

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(new Color(230, 240, 250));

        JButton registerBtn = createStyledButton("注册", new Color(76, 175, 80), Color.WHITE);
        registerBtn.addActionListener(e -> {
            String rn = nameField.getText().trim();
            String tn = numField.getText().trim();
            String pw = new String(pwdField.getPassword());
            String cpw = new String(confirmPwdField.getPassword());

            if (rn.isEmpty()) { UIUtil.showWarning(dialog, "请输入姓名"); return; }
            if (tn.isEmpty()) { UIUtil.showWarning(dialog, "请输入教工号"); return; }
            if (pw.isEmpty()) { UIUtil.showWarning(dialog, "请输入密码"); return; }
            if (!pw.equals(cpw)) { UIUtil.showWarning(dialog, "两次密码不一致"); return; }

            try {
                User user = new User();
                user.setRealName(rn);
                user.setStudentNumber(tn);
                user.setPassword(pw);
                user.setRole(UserRole.TEACHER);
                user.setGender("MALE");
                user.setStatus("ACTIVE");
                userService.register(user);
                UIUtil.showInfo(dialog, "注册成功，请登录！");
                dialog.dispose();
                realNameField.setText(rn);
                teacherNumberField.setText(tn);
                passwordField.setText("");
                passwordField.requestFocus();
            } catch (Exception ex) {
                UIUtil.showError(dialog, "注册失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JButton cancelBtn = createStyledButton("取消", new Color(120, 144, 156), Color.WHITE);
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
        field.setBorder(new RoundedBorder(new Color(129, 199, 132), 1, 8));
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
