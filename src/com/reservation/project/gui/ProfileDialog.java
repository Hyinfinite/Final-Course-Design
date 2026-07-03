package com.reservation.project.gui;

import com.reservation.project.dao.StaffDAO;
import com.reservation.project.model.StaffInfo;
import com.reservation.project.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * 个人信息与密码修改对话框类
 * 继承自JDialog，提供用户界面用于修改个人信息和密码
 */
public class ProfileDialog extends JDialog {
    // 当前登录用户对象
    private final User loginUser;
    // 数据访问对象，用于数据库操作
    private final StaffDAO dao = new StaffDAO();

    // 个人信息输入框
    private JTextField tfName = new JTextField(16);    // 姓名输入框
    private JTextField tfGender = new JTextField(8);   // 性别输入框
    private JTextField tfPos = new JTextField(16);     // 职务输入框
    private JTextField tfPhone = new JTextField(16);    // 电话输入框

    // 密码输入框
    private JPasswordField oldPwd = new JPasswordField(16);   // 旧密码输入框
    private JPasswordField newPwd = new JPasswordField(16);   // 新密码输入框
    private JPasswordField confirmPwd = new JPasswordField(16); // 确认新密码输入框

    /**
     * 构造函数
     * @param owner 父窗口
     * @param user 当前登录用户
     */
    public ProfileDialog(Frame owner, User user) {
        super(owner, "个人信息与密码修改", true);
        this.loginUser = user;

        // 设置对话框大小和位置
        setSize(520, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("个人信息", buildProfilePanel());
        tabs.addTab("修改密码", buildPasswordPanel());

        add(tabs, BorderLayout.CENTER);
        loadProfile();
    }

/**
 * 构建个人信息面板
 * 该方法创建一个包含5行2列的网格布局面板，用于显示和编辑用户个人信息
 * @return 配置好的JPanel面板，包含姓名、性别、职务、电话等输入框和保存按钮
 */
    private JPanel buildProfilePanel() {
        // 创建一个使用5行2列布局，并设置水平和垂直间距为10像素的面板
        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));

        // 添加姓名标签和文本框
        p.add(new JLabel("姓名"));
        p.add(tfName);

        // 添加性别标签和文本框
        p.add(new JLabel("性别"));
        p.add(tfGender);

        // 添加职务标签和文本框
        p.add(new JLabel("职务"));
        p.add(tfPos);

        // 添加电话标签和文本框
        p.add(new JLabel("电话"));
        p.add(tfPhone);

        // 添加一个空标签用于占位
        p.add(new JLabel());
        // 创建保存按钮并添加到面板
        JButton btnSave = new JButton("保存信息");
        p.add(btnSave);

        // 为保存按钮添加事件监听器，点击时调用DAO更新用户个人信息
        btnSave.addActionListener(e -> {
                // 调用DAO更新用户信息，并获取操作结果
            boolean ok = dao.updateOwnProfile(
                    loginUser.getStaffId(),
                    tfName.getText().trim(),
                    tfGender.getText().trim(),
                    tfPos.getText().trim(),
                    tfPhone.getText().trim()
            );
            // 根据操作结果显示相应的提示信息
            JOptionPane.showMessageDialog(this, ok ? "保存成功" : "保存失败");
        });

        // 返回构建好的面板
        return p;
    }


    /**
     * 构建密码修改面板
     * 该方法创建一个4行2列的网格布局面板，用于修改用户密码
     * 包含旧密码、新密码和确认新密码的输入框，以及一个修改密码按钮
     * 点击按钮后，会验证输入并调用DAO层方法更新密码
     *
     * @return 配置好的密码修改面板
     */
    private JPanel buildPasswordPanel() {
        // 创建一个4行2列的网格布局面板，水平和垂直间距均为10像素
        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));

        // 第一行：旧密码标签和密码输入框
        p.add(new JLabel("旧密码"));
        p.add(oldPwd);

        // 第二行：新密码标签和密码输入框
        p.add(new JLabel("新密码"));
        p.add(newPwd);

        // 第三行：确认新密码标签和密码输入框
        p.add(new JLabel("确认新密码"));
        p.add(confirmPwd);

        // 第四行：空白标签和修改密码按钮
        p.add(new JLabel());
        JButton btnSave = new JButton("修改密码");
        p.add(btnSave);

        // 为修改密码按钮添加点击事件监听器
        btnSave.addActionListener(e -> {
            // 获取并修剪三个密码输入框的文本
            String o = new String(oldPwd.getPassword()).trim();
            String n = new String(newPwd.getPassword()).trim();
            String cfm = new String(confirmPwd.getPassword()).trim();

            // 验证密码是否为空
            if (o.isEmpty() || n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "密码不能为空");
                return;
            }
            // 验证两次输入的新密码是否一致
            if (!n.equals(cfm)) {
                JOptionPane.showMessageDialog(this, "两次新密码不一致");
                return;
            }

            // 调用DAO层方法更新密码，并显示结果
            boolean ok = dao.changePassword(loginUser.getStaffId(), o, n);
            JOptionPane.showMessageDialog(this, ok ? "修改成功" : "修改失败（旧密码错误）");
        });

        return p;
    }

    /**
     * 加载用户个人信息到界面
     * 该方法从数据库中查询当前登录用户的详细信息，并更新到界面上的文本框中
     * 如果查询失败（返回null），则直接返回不做任何操作
     */
    private void loadProfile() {
        // 通过DAO层根据当前登录用户的ID查询用户信息
        StaffInfo s = dao.findByID(loginUser.getStaffId());
        // 如果查询结果为null，直接返回
        if (s == null) return;

        // 将查询到的用户信息更新到界面上的各个文本框中
        tfName.setText(s.getStaffName());    // 设置姓名
        tfGender.setText(s.getGender());     // 设置性别
        tfPos.setText(s.getPosition());      // 设置职务
        tfPos.setText(s.getPosition());      // 设置职务（重复赋值，可能是笔误）
        tfPhone.setText(s.getPhone());      // 设置电话
    }
}