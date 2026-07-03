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
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0;c.gridy=0; add(new JLabel("工号"), c);
        c.gridx=1;c.gridy=0; add(tfNo, c);

        c.gridx=0;c.gridy=1; add(new JLabel("密码"), c);
        c.gridx=1;c.gridy=1; add(tfPwd, c);

        c.gridx=0;c.gridy=2; add(new JLabel("角色"), c);
        c.gridx=1;c.gridy=2; add(cbRole, c);

        JButton btnLogin = new JButton("登录");
        c.gridx=1;c.gridy=3; add(btnLogin, c);

        btnLogin.addActionListener(e -> doLogin());

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}