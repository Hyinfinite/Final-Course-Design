package com.reservation.project.gui;

import com.reservation.project.dao.ManagerDAO;
import com.reservation.project.dao.ReservationDAO;
import com.reservation.project.model.ReservationList;
import com.reservation.project.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 会议室管理员主界面类
 * 提供管理员处理会议室预约的功能，包括待确认预约管理、历史记录查看和个人信息管理
 * 继承自JFrame，实现了基本的窗口界面功能
 */
public class ManagerFrame extends JFrame {
    /** 当前登录的用户对象，用于获取用户信息和身份验证 */
    private final User user;

    /** 待确认预约的表格模型，包含ID、预约号、主题、会议室、开始时间、结束时间、状态和申请人等列 */
    private DefaultTableModel pendingModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","状态","申请人"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;  // 设置表格单元格不可编辑
        }
    };

    /** 历史记录的表格模型，包含与待确认预约表格相同的列 */
    private DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","状态","申请人"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;  // 设置表格单元格不可编辑
        }
    };

    /**
     * 构造函数，初始化管理员界面
     * @param user 当前登录的用户对象
     */
    public ManagerFrame(User user) {
        this.user = user;

        setTitle("会议室管理员 - " + user.getStaffName());  // 设置窗口标题
        setSize(1180, 720);  // 设置窗口大小
        setLocationRelativeTo(null);  // 窗口居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 设置关闭操作
        setLayout(new BorderLayout(8,8));  // 设置边框布局，组件间距为8像素

        // 创建顶部标签，显示当前用户信息
        JLabel head = new JLabel("当前用户：" + user.getStaffName() + "（ROOM_ADMIN）");  // 设置边距
        head.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        // 创建选项卡面板，包含"待确认预约"和"确认历史"两个选项卡
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("待确认预约", buildPendingPanel());
        tabs.addTab("确认历史", buildHistoryPanel());

        // 创建顶部按钮面板，包含"个人信息/修改密码"按钮
        JPanel topBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnProfile = new JButton("个人信息/修改密码");
        topBtn.add(btnProfile);

        // 添加组件到主窗口
        add(head, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(topBtn, BorderLayout.SOUTH);

        // 为个人信息按钮添加事件监听器，点击后显示个人信息对话框
        btnProfile.addActionListener(e -> new ProfileDialog(this, user).setVisible(true));

        // 加载待确认预约和历史记录数据
        loadPending();
        loadHistory();

        // 显示窗口
        setVisible(true);
    }

    /**
     * 构建待确认预约面板
     * 创建包含审批操作面板和表格的面板
     * @return 配置好的待确认预约面板
     */
    private JPanel buildPendingPanel() {
        // 设置表格行高
        JPanel p = new JPanel(new BorderLayout(8,8));
        JTable table = new JTable(pendingModel);
        table.setRowHeight(28);  // 设置表格行高为28像素

        // 创建审批意见输入框和操作按钮
        JTextField tfComment = new JTextField(28);  // 设置输入框宽度为28列
        JButton btnPass = new JButton("通过");      // 通过按钮
        JButton btnReject = new JButton("驳回");    // 驳回按钮
        JButton btnRefresh = new JButton("刷新");   // 刷新按钮

        // 创建操作面板，使用左对流的流式布局
        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("审批意见"));  // 添加审批意见标签
        op.add(tfComment);              // 添加审批意见输入框
        op.add(btnPass);                // 添加通过按钮
        op.add(btnReject);              // 添加驳回按钮
        op.add(btnRefresh);             // 添加刷新按钮

        // 为通过按钮添加事件监听器，处理选中的预约，状态改为"已确认"
        btnPass.addActionListener(e -> processSelected(table, "已确认", tfComment.getText().trim()));
        // 为驳回按钮添加事件监听器，处理选中的预约，状态改为"已驳回"
        btnReject.addActionListener(e -> processSelected(table, "已驳回", tfComment.getText().trim()));
        // 为刷新按钮添加事件监听器，重新加载待确认预约列表
        btnRefresh.addActionListener(e -> loadPending());

        // 将操作面板添加到主面板的北部
        p.add(op, BorderLayout.NORTH);
        // 将表格添加到主面板的中央，并添加滚动条
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    /**
     * 构建历史记录面板
     * 创建包含历史记录表格和刷新按钮的面板
     * @return 配置好的历史记录面板
     */
    private JPanel buildHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        JTable table = new JTable(historyModel);
        table.setRowHeight(28);

        JButton btnRefresh = new JButton("刷新历史");
        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(btnRefresh);
        btnRefresh.addActionListener(e -> loadHistory());

        p.add(op, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    /**
     * 处理选中的预约记录
     * @param table 表格对象
     * @param process 处理结果（"已确认"或"已驳回"）
     * @param comment 处理意见
     */
    private void processSelected(JTable table, String process, String comment) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选中一条待处理记录");
            return;
        }

        long reservationId = Long.parseLong(pendingModel.getValueAt(row, 0).toString());
        boolean ok = new ReservationDAO().processReservation(reservationId, user.getStaffId(), process, comment);
        JOptionPane.showMessageDialog(this, ok ? "处理成功" : "处理失败");

        if (ok) {
            loadPending();  // 重新加载待确认预约
            loadHistory();  // 重新加载历史记录
        }
    }

    /**
     * 加载待确认预约数据
     * 从数据库查询待处理的预约记录并更新到表格模型中
     */
    private void loadPending() {
        pendingModel.setRowCount(0);  // 清空表格数据
        List<ReservationList> list = new ReservationDAO().searchPendingReservation();
        for (ReservationList r : list) {
            pendingModel.addRow(new Object[]{
                    r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                    r.getStartTime(), r.getEndTime(), r.getProcess(), r.getApplicantName()
            });
        }
    }

    /**
     * 加载历史记录数据
     * 从数据库查询已处理的预约记录并更新到表格模型中
     */
    private void loadHistory() {
        historyModel.setRowCount(0);  // 清空表格数据
        List<ReservationList> list = new ManagerDAO().searchProcessedReservation();
        for (ReservationList r : list) {
            historyModel.addRow(new Object[]{
                    r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                    r.getStartTime(), r.getEndTime(), r.getProcess(), r.getApplicantName()
            });
        }
    }
}