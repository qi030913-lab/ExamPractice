package com.exam.view;

import com.exam.exception.AuthenticationException;
import com.exam.model.User;
import com.exam.model.enums.UserRole;
import com.exam.service.UserService;
import com.exam.util.UIUtil;
import com.exam.view.student.StudentMainFrame;
import com.exam.view.teacher.TeacherMainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * 登录界面（优化版：紧凑布局）
 */
public class LoginFrame extends JFrame {
    private final UserService userService;
    private JTextField realNameField;
    private JTextField idNumberField;  // 通用ID字段，学生为学号，教师为教工号
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JPanel idNumberPanel;

    public LoginFrame() {
        this.userService = new UserService();
        initComponents();
        setTitle("考试练习系统 - 登录");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        UIUtil.centerWindow(this);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(230, 240, 250));

        // 顶部 Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(new Color(230, 240, 250));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 20, 0));

        JLabel titleLabel = new JLabel("考试练习系统");
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 118, 210));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(titleLabel);

        JLabel subTitleLabel = new JLabel("ONLINE EXAM SYSTEM");
        subTitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subTitleLabel.setForeground(new Color(66, 133, 244));
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
                BorderFactory.createLineBorder(new Color(144, 202, 249), 1),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        formPanel.setPreferredSize(new Dimension(400, 290));

        // 角色选择（放在第一栏）
        formPanel.add(createRoleRow());
        formPanel.add(Box.createVerticalStrut(12));

        // 姓名输入
        formPanel.add(createInputRow("姓　　名：", realNameField = new JTextField()));
        formPanel.add(Box.createVerticalStrut(12));

        // ID号输入（根据角色动态显示标签）
        idNumberPanel = createInputRow("学　　号：", idNumberField = new JTextField());
        formPanel.add(idNumberPanel);
        formPanel.add(Box.createVerticalStrut(12));

        // 密码输入
        formPanel.add(createInputRow("密　　码：", passwordField = new JPasswordField()));
        formPanel.add(Box.createVerticalStrut(18));

        // 按钮
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(new Color(245, 250, 255));
        JButton loginButton = createStyledButton("登录", new Color(33, 150, 243), new Color(25, 25, 25));
        loginButton.addActionListener(e -> handleLogin());

        JButton registerButton = createStyledButton("注册", new Color(66, 165, 245), new Color(25, 25, 25));
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
     * 角色选择行
     */
    private JPanel createRoleRow() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(new Color(245, 250, 255));
        JLabel label = new JLabel("角　　色：");
        label.setFont(new Font("SimHei", Font.PLAIN, 14));
        label.setForeground(new Color(30, 70, 120));
        label.setPreferredSize(new Dimension(85, 30));
        label.setOpaque(false);
        panel.add(label, BorderLayout.WEST);

        roleComboBox = new JComboBox<>(new String[]{"学生", "教师"});
        roleComboBox.setFont(new Font("SimHei", Font.PLAIN, 14));
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setForeground(new Color(30, 70, 120));
        roleComboBox.setBorder(BorderFactory.createLineBorder(new Color(144, 202, 249), 1));
        roleComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加角色切换监听器
        roleComboBox.addActionListener(e -> {
            String selectedRole = (String) roleComboBox.getSelectedItem();
            // 根据角色显示相应的ID字段标签
            updateIdFieldLabel(selectedRole);
            revalidate();
            repaint();
        });
        
        // 初始化标签
        SwingUtilities.invokeLater(() -> updateIdFieldLabel("学生"));
        
        panel.add(roleComboBox, BorderLayout.CENTER);
        return panel;
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
     * 焦点边框变化（用于注册表单）
     */
    private void addFocusListener(JTextField field) {
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createLineBorder(new Color(33, 150, 243), 2));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createLineBorder(new Color(144, 202, 249), 1));
            }
        });
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SimHei", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
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
        String idNumber = idNumberField.getText().trim();
        String password = new String(passwordField.getPassword());
        String roleStr = (String) roleComboBox.getSelectedItem();
        UserRole selectedRole = "教师".equals(roleStr) ? UserRole.TEACHER : UserRole.STUDENT;

        if (realName.isEmpty()) {
            UIUtil.showWarning(this, "请输入姓名");
            realNameField.requestFocus();
            return;
        }
        // 学生角色需要验证学号，教师角色需要验证教工号
        if (selectedRole == UserRole.STUDENT && idNumber.isEmpty()) {
            UIUtil.showWarning(this, "请输入学号");
            idNumberField.requestFocus();
            return;
        }
        if (selectedRole == UserRole.TEACHER && idNumber.isEmpty()) {
            UIUtil.showWarning(this, "请输入教工号");
            idNumberField.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            UIUtil.showWarning(this, "请输入密码");
            passwordField.requestFocus();
            return;
        }

        try {
            User user = userService.login(realName, idNumber, password, selectedRole);
            SwingUtilities.invokeLater(() -> {
                if (user.getRole() == UserRole.TEACHER) new TeacherMainFrame(user).setVisible(true);
                else if (user.getRole() == UserRole.STUDENT) new StudentMainFrame(user).setVisible(true);
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
     * 根据角色更新ID字段标签
     */
    private void updateIdFieldLabel(String selectedRole) {
        // 更新ID字段的标签文本
        JLabel label = (JLabel) idNumberPanel.getComponent(0); // 第一个组件是标签
        if ("教师".equals(selectedRole)) {
            label.setText("教工号：");
        } else {
            label.setText("学　号：");
        }
    }

    /**
     * 注册对话框（保留原功能）
     */
    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "用户注册", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(new Color(230, 240, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 标题
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(230, 240, 250));
        JLabel titleLabel = new JLabel("创建新账户");
        titleLabel.setFont(new Font("SimHei", Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 118, 210));
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
        JComboBox<String> regRoleComboBox = new JComboBox<>(new String[]{"学生", "教师"});
        regRoleComboBox.setFont(new Font("SimHei", Font.PLAIN, 12));
        regRoleComboBox.setBorder(BorderFactory.createLineBorder(new Color(144, 202, 249), 1));
        
        // 创建ID号字段面板
        JPanel idFieldPanel = createFormField("学 号", numField);
        
        // 第一栏：角色选择
        formPanel.add(createRoleFormField("角 色", regRoleComboBox));
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(createFormField("姓 名", nameField));
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(idFieldPanel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(createFormField("密 码", pwdField));
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(createFormField("确认密码", confirmPwdField));
        
        // 为注册角色下拉框添加监听器，更新ID字段标签
        regRoleComboBox.addActionListener(e -> {
            String selectedRole = (String) regRoleComboBox.getSelectedItem();
            updateRegisterIdFieldLabel(idFieldPanel, selectedRole);
        });

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        buttonPanel.setBackground(new Color(230, 240, 250));

        JButton registerBtn = createStyledButton("注册", new Color(33, 150, 243), new Color(25, 25, 25));
        registerBtn.addActionListener(e -> {
            String rn = nameField.getText().trim();
            String sn = numField.getText().trim();
            String pw = new String(pwdField.getPassword());
            String cpw = new String(confirmPwdField.getPassword());

            if (rn.isEmpty()) { UIUtil.showWarning(dialog, "请输入姓名"); return; }
            if (sn.isEmpty()) {
                String roleStr = (String) regRoleComboBox.getSelectedItem();
                if ("教师".equals(roleStr)) {
                    UIUtil.showWarning(dialog, "请输入教工号");
                } else {
                    UIUtil.showWarning(dialog, "请输入学号");
                }
                return;
            }
            if (pw.isEmpty()) { UIUtil.showWarning(dialog, "请输入密码"); return; }
            if (!pw.equals(cpw)) { UIUtil.showWarning(dialog, "两次密码不一致"); return; }

            String roleStr = (String) regRoleComboBox.getSelectedItem();
            UserRole role = "教师".equals(roleStr) ? UserRole.TEACHER : UserRole.STUDENT;

            try {
                User user = new User();
                user.setRealName(rn);
                user.setStudentNumber(sn);
                user.setPassword(pw);
                user.setRole(role);
                user.setGender("MALE");
                user.setStatus("ACTIVE");
                userService.register(user);
                UIUtil.showInfo(dialog, "注册成功，请登录！");
                dialog.dispose();
                realNameField.setText(rn);
                idNumberField.setText(sn); // 更新为idNumberField
                passwordField.setText("");
                passwordField.requestFocus();
            } catch (Exception ex) {
                UIUtil.showError(dialog, "注册失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });

        JButton cancelBtn = createStyledButton("取消", new Color(120, 144, 156), new Color(25, 25, 25));
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
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);  // 确保标签左对齐
        panel.add(fieldLabel);

        // 如果是密码框，设置更小的字体
        if (field instanceof JPasswordField) {
            field.setFont(new Font("SimHei", Font.PLAIN, 10));
        } else {
            field.setFont(new Font("SimHei", Font.PLAIN, 12));
        }
        field.setBorder(BorderFactory.createLineBorder(new Color(144, 202, 249), 1));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);  // 确保输入框左对齐
        addFocusListener(field);
        panel.add(field);

        return panel;
    }

    private JPanel createRoleFormField(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 250, 255));

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SimHei", Font.PLAIN, 12));
        fieldLabel.setForeground(new Color(30, 70, 120));
        fieldLabel.setOpaque(false);
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);  // 确保标签左对齐
        panel.add(fieldLabel);

        comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);  // 确保下拉框左对齐
        panel.add(comboBox);

        return panel;
    }
    
    /**
     * 创建左对齐的角色选择表单字段
     */
    private JPanel createRoleFormFieldLeftAlign(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(new Color(245, 250, 255));

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SimHei", Font.PLAIN, 12));
        fieldLabel.setForeground(new Color(30, 70, 120));
        fieldLabel.setOpaque(false);
        
        panel.add(fieldLabel, BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.CENTER);

        return panel;
    }
    
    /**
     * 根据角色更新注册对话框中的ID字段标签
     */
    private void updateRegisterIdFieldLabel(JPanel idFieldPanel, String selectedRole) {
        // 更新注册对话框中ID字段的标签文本
        JLabel label = (JLabel) idFieldPanel.getComponent(0); // 第一个组件是标签
        if ("教师".equals(selectedRole)) {
            label.setText("教工号");
        } else {
            label.setText("学 号");
        }
    }
}
