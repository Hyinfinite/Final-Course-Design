package com.reservation.project.gui;

import com.reservation.project.dao.UserDAO;
import com.reservation.project.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * 会议预约管理系统的登录界面类
 * 继承自JFrame，创建了一个登录窗口，包含工号、密码输入框和角色选择下拉框
 */
public class LoginFrame extends JFrame {
    // 工号输入文本框，长度为16个字符
    private JTextField tfNo = new JTextField(16);
    // 密码输入密码框，长度为16个字符，输入内容会以掩码形式显示
    private JPasswordField tfPwd = new JPasswordField(16);
    // 角色选择下拉框，包含三种角色：系统管理员(SYS_ADMIN)、房间管理员(ROOM_ADMIN)和员工(STAFF)
    private JComboBox<String> cbRole = new JComboBox<String>(new String[]{"SYS_ADMIN","ROOM_ADMIN","STAFF"});

    /**
     * 构造方法，初始化登录窗口
     */
    public LoginFrame() {
        // 设置窗口标题
        setTitle("会议预约管理系统 - 登录");
        // 设置窗口大小为420x260像素
        setSize(420, 260);
        // 设置窗口居中显示
        setLocationRelativeTo(null);
        // 设置关闭窗口时退出程序
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 最外层使用 FlowLayout，通过它来制造左右和上下的边缘留白
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        // 创建一个容器面板，宽度撑满，形成左右留白的效果
        JPanel container = new JPanel();
        // 使用4行2列的网格布局，组件水平间隔10像素，垂直间隔8像素
        container.setLayout(new GridLayout(4, 2, 10, 8));

        // 添加工号标签和输入框
        container.add(new JLabel("工号"));
        container.add(tfNo);

        // 添加密码标签和输入框
        container.add(new JLabel("密码"));
        container.add(tfPwd);

        // 添加角色标签和下拉框
        container.add(new JLabel("角色"));
        container.add(cbRole);

        // 创建按钮面板，使用左对齐的流式布局
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        // 创建登录按钮
        JButton btnLogin = new JButton("登录");
        // 将登录按钮添加到按钮面板
        btnPanel.add(btnLogin);
        // 添加空标签用于占位
        container.add(new JLabel());
        // 将按钮面板添加到容器
        container.add(btnPanel);

        // 为登录按钮添加点击事件监听器，点击时调用doLogin方法
        btnLogin.addActionListener(e -> doLogin());

        // 将容器添加到窗口
        add(container);
        // 设置窗口可见
        setVisible(true);
    }

    /**
     * 处理登录逻辑的方法
     */
    private void doLogin() {
        // 获取并去除工号和密码的前后空格
        String no = tfNo.getText().trim();
        String pwd = new String(tfPwd.getPassword()).trim();
        // 获取当前选中的角色
        String role = String.valueOf(cbRole.getSelectedItem());

        // 检查工号和密码是否为空
        if (no.isEmpty() || pwd.isEmpty()) {
            // 显示错误提示信息
            JOptionPane.showMessageDialog(this, "工号和密码不能为空");
            return;
        }

        // 调用UserDAO的Login方法进行用户验证
        User u = new UserDAO().Login(no, pwd, role);
        if (u == null) {
            // 登录失败，显示错误提示
            JOptionPane.showMessageDialog(this, "登录失败，请检查账号密码或角色");
            return;
        }

        // 关闭当前登录窗口
        dispose();
        // 根据用户角色打开相应的界面
        if ("SYS_ADMIN".equalsIgnoreCase(u.getAccessLevel())) {
            // 系统管理员界面
            new AdminFrame(u);
        } else if ("ROOM_ADMIN".equalsIgnoreCase(u.getAccessLevel())) {
            // 房间管理员界面
            new ManagerFrame(u);
        } else {
            // 普通员工界面
            new StaffFrame(u);
        }
    }
}