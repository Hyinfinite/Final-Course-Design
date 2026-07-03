package com.reservation.project.gui;

import com.reservation.project.dao.UserDAO;
import com.reservation.project.model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField tfNo = new JTextField(16);
    private JPasswordField tfPwd = new JPasswordField(16);
    private JComboBox<String> cbRole = new JComboBox<String>(new String[]{"SYS_ADMIN","ROOM_ADMIN","STAFF"});

    public LoginFrame() {
        setTitle("会议预约管理系统 - 登录");
        setSize(420, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 最外层使用 FlowLayout，通过它来制造左右和上下的边缘留白
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        // 创建一个容器面板，宽度撑满，形成左右留白的效果
        JPanel container = new JPanel();
        container.setLayout(new GridLayout(4, 2, 10, 8));

        container.add(new JLabel("工号"));
        container.add(tfNo);

        container.add(new JLabel("密码"));
        container.add(tfPwd);

        container.add(new JLabel("角色"));
        container.add(cbRole);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton btnLogin = new JButton("登录");
        btnPanel.add(btnLogin);
        container.add(new JLabel());
        container.add(btnPanel);

        btnLogin.addActionListener(e -> doLogin());

        add(container);
        setVisible(true);
    }

    private void doLogin() {
        String no = tfNo.getText().trim();
        String pwd = new String(tfPwd.getPassword()).trim();
        String role = String.valueOf(cbRole.getSelectedItem());

        if (no.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "工号和密码不能为空");
            return;
        }

        User u = new UserDAO().Login(no, pwd, role);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "登录失败，请检查账号密码或角色");
            return;
        }

        dispose();
        if ("SYS_ADMIN".equalsIgnoreCase(u.getAccessLevel())) {
            new AdminFrame(u);
        } else if ("ROOM_ADMIN".equalsIgnoreCase(u.getAccessLevel())) {
            new ManagerFrame(u);
        } else {
            new StaffFrame(u);
        }
    }
}