package org.unipus.ui;

/* (っ*´Д`)っ 小代码要被看光啦 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;

public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JCheckBox rememberMe;
    private JCheckBox showPassword;
    private JLabel statusLabel;
    private char defaultEchoChar;

    // Preferences key
    private static final String PREF_KEY_REMEMBER = "remember_username";
    private static final String PREF_KEY_USERNAME = "saved_username";
    private final Preferences prefs = Preferences.userNodeForPackage(LoginPanel.class);

    public LoginPanel() {
        setupUI();
        loadRememberedCredentials();
        setupUsability();
    }

    private void setupUI() {
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;

        JLabel usernameLabel = new JLabel("用户名/手机号/邮箱:");
        this.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setToolTipText("请输入用户名");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("密码:");
        this.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setToolTipText("请输入密码");
        defaultEchoChar = passwordField.getEchoChar();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(passwordField, gbc);

        // Remember me and show password
        rememberMe = new JCheckBox("记住我");
        rememberMe.setToolTipText("登录后保存用户名，下次自动填写");
        showPassword = new JCheckBox("显示密码");
        showPassword.setToolTipText("切换是否显示密码");
        showPassword.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (showPassword.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar(defaultEchoChar);
                }
            }
        });

        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        options.add(rememberMe);
        options.add(showPassword);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        this.add(options, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        this.add(statusLabel, gbc);
        gbc.gridwidth = 1;

        // Buttons
        loginButton = new JButton("登录");
        loginButton.setToolTipText("登录 (回车)");
        cancelButton = new JButton("取消");
        cancelButton.setToolTipText("取消并清空");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttons.add(loginButton);
        buttons.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        this.add(buttons, gbc);
    }

    // 新增：设置回车触发、初始焦点和默认行为
    private void setupUsability() {
        // 回车触发登录（用户名和密码字段）
        usernameField.addActionListener(e -> loginButton.doClick());
        passwordField.addActionListener(e -> loginButton.doClick());

        // 取消按钮默认清空输入
        cancelButton.addActionListener(e -> clearFields());

        // 可访问名
        usernameField.getAccessibleContext().setAccessibleName("用户名");
        passwordField.getAccessibleContext().setAccessibleName("密码");
        loginButton.getAccessibleContext().setAccessibleName("登录按钮");

        // 初始焦点
        SwingUtilities.invokeLater(() -> {
            if (usernameField.getText() == null || usernameField.getText().isEmpty()) {
                usernameField.requestFocusInWindow();
            } else {
                passwordField.requestFocusInWindow();
            }
        });
    }

    // 将登录按钮设为窗口的默认按钮（如果有父窗口）
    public void setDefaultButtonOnRootPane(JRootPane root) {
        if (root != null) {
            root.setDefaultButton(loginButton);
        }
    }

    // 便捷方法：注册登录按钮监听器
    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    // 便捷方法：注册取消按钮监听器
    public void addCancelListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public boolean isRememberMeSelected() {
        return rememberMe.isSelected();
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message == null ? " " : message);
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        rememberMe.setSelected(false);
        setStatusMessage(null);
    }

    public void setEnabledDuringLogin(boolean enabled) {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        rememberMe.setEnabled(enabled);
        showPassword.setEnabled(enabled);
        loginButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }

    // 持久化：保存/加载记住的用户名（不保存密码）
    private void loadRememberedCredentials() {
        boolean remember = prefs.getBoolean(PREF_KEY_REMEMBER, false);
        String saved = prefs.get(PREF_KEY_USERNAME, "");
        rememberMe.setSelected(remember);
        if (remember && saved != null && !saved.isEmpty()) {
            usernameField.setText(saved);
        }
    }

    // 在外部在登录成功时调用以保存首选项
    public void persistRememberedCredentialsOnSuccess() {
        if (rememberMe.isSelected()) {
            prefs.putBoolean(PREF_KEY_REMEMBER, true);
            prefs.put(PREF_KEY_USERNAME, usernameField.getText() == null ? "" : usernameField.getText());
        } else {
            prefs.putBoolean(PREF_KEY_REMEMBER, false);
            prefs.remove(PREF_KEY_USERNAME);
        }
    }
}
