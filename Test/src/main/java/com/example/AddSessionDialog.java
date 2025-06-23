package com.example;

import javax.swing.*;
import java.awt.*;

// 新增会话窗口类
class AddSessionDialog extends JDialog {
    private JTextField hostField, portField, userField;
    private JPasswordField passwordField;
    private JButton okBtn;
    private JButton cancelBtn;
    private AixLogFinder mainFrame;

    public AddSessionDialog(AixLogFinder mainFrame) {
        super(mainFrame, "新增会话配置", true);
        this.mainFrame = mainFrame;
        initComponents();
        setupLayout();
        setupListeners();
        setSize(300, 200);
        setLocationRelativeTo(mainFrame);
    }

    private void initComponents() {
        hostField = new JTextField("10.196.61.10");
        portField = new JTextField("22");
        userField = new JTextField("username");
        passwordField = new JPasswordField("password");
    }

    private void setupLayout() {
        JPanel formPanel = new JPanel(new GridLayout(4, 2));
        formPanel.add(new JLabel("主机:"));
        formPanel.add(hostField);
        formPanel.add(new JLabel("端口:"));
        formPanel.add(portField);
        formPanel.add(new JLabel("用户名:"));
        formPanel.add(userField);
        formPanel.add(new JLabel("密码:"));
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        okBtn = new JButton("保存");
        cancelBtn = new JButton("取消");
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        okBtn.addActionListener(e -> saveSession());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void saveSession() {
        String host = hostField.getText().trim();
        int port = parsePort();
        String user = userField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (host.isEmpty() || user.isEmpty() || port <= 0) {
            JOptionPane.showMessageDialog(this, "请填写完整有效信息", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        mainFrame.addSessionToModel(new AixLogFinder.SessionConfig(host, port, user, password));
        dispose();
    }

    private int parsePort() {
        try {
            return Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "端口必须为有效整数", "错误", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
}

