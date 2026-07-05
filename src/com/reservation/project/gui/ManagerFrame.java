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
    // 当前登录的用户对象，用于获取用户信息和身份验证
    private final User user;

    // 待确认预约的表格模型，包含ID、预约号、主题、会议室、开始时间、结束时间、状态和申请人等列
    private DefaultTableModel pendingModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","参会人数","状态","申请人","审批意见"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;  // 设置表格单元格不可编辑
        }
    };

    // 历史记录的表格模型，包含与待确认预约表格相同的列
    private DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","参会人数","状态","申请人","审批意见"}, 0) {
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
        btnPass.addActionListener(e -> {
            processSelected(table, "已确认", tfComment.getText().trim());
            tfComment.setText("");  // 清空审批意见输入框
        });
        // 为驳回按钮添加事件监听器，处理选中的预约，状态改为"已驳回"
        btnReject.addActionListener(e -> {
            processSelected(table, "已驳回", tfComment.getText().trim());
            tfComment.setText("");  // 清空审批意见输入框
        });
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
        // 创建一个使用BorderLayout布局的面板，设置水平和垂直间距为8像素
        JPanel p = new JPanel(new BorderLayout(8,8));
        // 创建一个使用historyModel作为数据模型的表格，并设置行高为28像素
        JTable table = new JTable(historyModel);
        table.setRowHeight(28);

        // 创建一个"刷新历史"按钮
        JButton btnRefresh = new JButton("刷新历史");
        // 创建一个使用FlowLayout(左对齐)的面板用于放置操作按钮
        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // 将刷新按钮添加到操作面板中
        op.add(btnRefresh);
        // 为刷新按钮添加点击事件监听器，点击时调用loadHistory方法
        btnRefresh.addActionListener(e -> loadHistory());

        // 将操作面板添加到历史记录面板的北部(上方)
        p.add(op, BorderLayout.NORTH);
        // 将表格添加到带滚动面板的容器中，并放置在历史记录面板的中央
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        // 返回配置好的历史记录面板
        return p;
    }

    /**
     * 处理选中的预约记录
     * @param table 表格对象
     * @param process 处理结果（"已确认"或"已驳回"）
     * @param comment 处理意见
     */
    private void processSelected(JTable table, String process, String comment) {
        // 获取用户选中的表格行号
        int row = table.getSelectedRow();
        // 如果没有选中任何行，提示用户并返回
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选中一条待处理记录");
            return;
        }

        // 从表格中获取预约ID，并转换为Long类型
        long reservationId = Long.parseLong(pendingModel.getValueAt(row, 0).toString());
        // 调用ReservationDAO处理预约，传入预约ID、员工ID、处理结果和处理意见
        boolean ok = new ReservationDAO().processReservation(reservationId, user.getStaffId(), process, comment);
        // 根据处理结果显示成功或失败的提示信息
        JOptionPane.showMessageDialog(this, ok ? "处理成功" : "处理失败");

        // 如果处理成功，重新加载待确认预约和历史记录
        if (ok) {
            loadPending();  // 重新加载待确认预约
            loadHistory();  // 重新加载历史记录
        }
    }

    /**
     * 加载待确认预约数据
     * 从数据库查询待处理的预约记录并更新到表格模型中
     */
    private void loadPending() {  // 私有方法，用于加载待确认的预约数据
        pendingModel.setRowCount(0);  // 清空表格数据，为加载新数据做准备
        List<ReservationList> list = new ReservationDAO().searchPendingReservation();  // 调用数据访问对象获取待处理预约列表
        // 遍历预约列表，将每条预约信息添加到表格模型中
        for (ReservationList r : list) {
            // 向表格模型中添加一行数据，包含预约ID、预约编号、会议主题、会议室名称、
            // 开始时间、结束时间、参与人数、处理状态、申请人姓名和备注信息
            pendingModel.addRow(new Object[]{
                    r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                    r.getStartTime(), r.getEndTime(), r.getParticipantCount(), r.getProcess(),
                    r.getApplicantName(), r.getComment()
            });
        }
    }

    /**
     * 加载历史记录数据
     * 从数据库查询已处理的预约记录并更新到表格模型中
     */
    private void loadHistory() {
        // 清空表格数据，为加载新数据做准备
        historyModel.setRowCount(0);  // 清空表格数据
        // 通过ManagerDAO从数据库查询已处理的预约记录
        List<ReservationList> list = new ManagerDAO().searchProcessedReservation();
        // 遍历查询结果，将每条预约记录添加到表格模型中
        for (ReservationList r : list) {
            // 向表格模型中添加一行数据，包含预约的各种信息
            historyModel.addRow(new Object[]{
                    r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                    r.getStartTime(), r.getEndTime(), r.getParticipantCount(), r.getProcess(),
                    r.getApplicantName(), r.getComment()
            });
        }
    }
}