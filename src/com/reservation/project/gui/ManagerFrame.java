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
 * 会议室管理员：待确认 + 排程 + 历史 + 个人信息/改密
 */
public class ManagerFrame extends JFrame {
    private final User user;

    private DefaultTableModel pendingModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","状态","申请人"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private DefaultTableModel historyModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","状态","申请人"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public ManagerFrame(User user) {
        this.user = user;

        setTitle("会议室管理员 - " + user.getStaffName());
        setSize(1180, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        JLabel head = new JLabel("当前用户：" + user.getStaffName() + "（ROOM_ADMIN）");
        head.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("待确认预约", buildPendingPanel());
        tabs.addTab("确认历史", buildHistoryPanel());

        JPanel topBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnProfile = new JButton("个人信息/修改密码");
        topBtn.add(btnProfile);

        add(head, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(topBtn, BorderLayout.SOUTH);

        btnProfile.addActionListener(e -> new ProfileDialog(this, user).setVisible(true));

        loadPending();
        loadHistory();
        setVisible(true);
    }

    private JPanel buildPendingPanel() {
        JPanel p = new JPanel(new BorderLayout(8,8));
        JTable table = new JTable(pendingModel);
        table.setRowHeight(28);

        JTextField tfComment = new JTextField(28);
        JButton btnPass = new JButton("通过");
        JButton btnReject = new JButton("驳回");
        JButton btnRefresh = new JButton("刷新");

        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("审批意见"));
        op.add(tfComment);
        op.add(btnPass);
        op.add(btnReject);
        op.add(btnRefresh);

        btnPass.addActionListener(e -> processSelected(table, "已确认", tfComment.getText().trim()));
        btnReject.addActionListener(e -> processSelected(table, "已驳回", tfComment.getText().trim()));
        btnRefresh.addActionListener(e -> loadPending());

        p.add(op, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

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
            loadPending();
            loadHistory();
        }
    }

    private void loadPending() {
        pendingModel.setRowCount(0);
        List<ReservationList> list = new ReservationDAO().searchPendingReservation();
        for (ReservationList r : list) {
            pendingModel.addRow(new Object[]{
                    r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                    r.getStartTime(), r.getEndTime(), r.getProcess(), r.getApplicantName()
            });
        }
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        List<ReservationList> list = new ManagerDAO().searchProcessedReservation();
        for (ReservationList r : list) {
            historyModel.addRow(new Object[]{
                    r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                    r.getStartTime(), r.getEndTime(), r.getProcess(), r.getApplicantName()
            });
        }
    }
}