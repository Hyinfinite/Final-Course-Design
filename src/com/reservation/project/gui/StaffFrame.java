package com.reservation.project.gui;

import com.reservation.project.dao.*;
import com.reservation.project.dao.MeetingRoomDAO;
import com.reservation.project.dao.ParticipantDAO;
import com.reservation.project.dao.ReservationDAO;
import com.reservation.project.dao.RoomAvailabilityDAO;
import com.reservation.project.model.Participant;
import com.reservation.project.model.ReservationList;
import com.reservation.project.model.User;
import com.reservation.project.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 行政人员：空闲查询 + 提交预约 + 我的预约 + 撤销 + 签到 + 修改密码
 */
public class StaffFrame extends JFrame {
    private final User user;

    private JComboBox<String> cbRoom = new JComboBox<String>();
    private JTextField tfTopic = new JTextField(18);
    private JTextField tfCount = new JTextField("10", 18);
    private JTextField tfDesc = new JTextField(18);
    private TimePanel timePicker = new TimePanel();

    private DefaultTableModel myModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","状态"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private DefaultTableModel freeModel = new DefaultTableModel(
            new Object[]{"会议室ID","会议室","位置","容量","状态"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private DefaultTableModel signModel = new DefaultTableModel(
            new Object[]{"记录ID","预约号","主题","开始时间","结束时间","参会部门","签到状态","签到时间"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public StaffFrame(User user) {
        this.user = user;

        setTitle("行政人员 - " + user.getStaffName());
        setSize(1220, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));

        JLabel head = new JLabel("当前用户：" + user.getStaffName() + "（STAFF）");
        head.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("提交预约", buildSubmitPanel());
        tabs.addTab("我的预约", buildMyPanel());
        tabs.addTab("会议室空闲查询", buildFreePanel());
        tabs.addTab("签到管理", buildSignPanel());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnProfile = new JButton("个人信息/修改密码");
        bottom.add(btnProfile);

        add(head, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnProfile.addActionListener(e -> new ProfileDialog(this, user).setVisible(true));

        loadRoomOptions();
        loadMyReservations();
        loadSignRecords();
        setVisible(true);
    }

    private JPanel buildSubmitPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 创建表单面板，使用垂直FlowLayout
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        // 第一行表单
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row1.add(new JLabel("会议主题"));
        row1.add(tfTopic);
        row1.add(new JLabel("会议室"));
        row1.add(cbRoom);
        form.add(row1);

        // 第二行表单
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row2.add(new JLabel("参会人数"));
        row2.add(tfCount);
        row2.add(new JLabel("会议说明"));
        row2.add(tfDesc);
        form.add(row2);

        // 第三行表单
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row3.add(new JLabel("会议时间"));
        row3.add(timePicker);
        form.add(row3);

        // 创建操作按钮面板
        JButton btnSubmit = new JButton("提交预约");
        JButton btnReloadRoom = new JButton("刷新会议室");
        JPanel op = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        op.add(btnReloadRoom);
        op.add(btnSubmit);

        // 添加按钮事件监听器
        btnReloadRoom.addActionListener(e -> loadRoomOptions());
        btnSubmit.addActionListener(e -> addReservation());

        // 将组件添加到主面板
        panel.add(form, BorderLayout.CENTER);
        panel.add(op, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildMyPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        JTable table = new JTable(myModel);
        table.setRowHeight(28);

        JButton btnRefresh = new JButton("刷新");
        JButton btnCancel = new JButton("撤销选中预约");
        JPanel op = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        op.add(btnRefresh); op.add(btnCancel);

        btnRefresh.addActionListener(e -> loadMyReservations());
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一条记录"); return; }
            long reservationId = Long.parseLong(myModel.getValueAt(row, 0).toString());
            boolean ok = new ReservationDAO().cancelReservation(reservationId, user.getStaffId());
            JOptionPane.showMessageDialog(this, ok ? "撤销成功" : "撤销失败（仅待确认可撤销）");
            loadMyReservations();
        });

        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFreePanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));
        JTable table = new JTable(freeModel);
        table.setRowHeight(28);

        JTextField tfDate = new JTextField(10);
        tfDate.setText(java.time.LocalDate.now().toString());

        JButton btnQuery = new JButton("按日期查询");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("日期(yyyy-MM-dd)"));
        top.add(tfDate);
        top.add(btnQuery);

        btnQuery.addActionListener(e -> {
            freeModel.setRowCount(0);
            List<String[]> list = new RoomAvailabilityDAO().queryByDate(tfDate.getText().trim());
            for (String[] x : list) freeModel.addRow(x);
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSignPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));

        // 上部：操作区域，使用 FlowLayout 左对齐，默认组件大小
        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        // 会议下拉框（不设置首选大小，采用默认）
        JComboBox<String> cbReservation = new JComboBox<>();

        // 按钮
        JButton btnLoadParticipants = new JButton("加载参会人员");
        JButton btnSignDepartment = new JButton("部门签到");
        JButton btnRefresh = new JButton("刷新会议列表");
        JButton btnSignSelected = new JButton("签到选中记录");

        // 添加组件
        queryPanel.add(new JLabel("选择会议"));
        queryPanel.add(cbReservation);
        queryPanel.add(btnLoadParticipants);
        queryPanel.add(btnSignDepartment);
        queryPanel.add(btnRefresh);
        queryPanel.add(btnSignSelected);

        // 表格
        JTable table = new JTable(signModel);
        table.setRowHeight(28);

        // ====== 加载会议列表（仅本部门已确认且已开始的会议） ======
        Runnable loadReservations = () -> {
            cbReservation.removeAllItems();
            List<ReservationList> list = new ReservationDAO().searchConfirmedReservationsByDept(user.getDeptId());
            if (list.isEmpty()) {
                cbReservation.addItem("暂无已开始会议");
            } else {
                for (ReservationList r : list) {
                    String display = r.getReservationId() + " - " + r.getMeetingTopic()
                            + " (" + r.getStartTime() + ")";
                    cbReservation.addItem(display);
                }
            }
        };

        // ====== 加载参会人员 ======
        btnLoadParticipants.addActionListener(e -> {
            signModel.setRowCount(0);
            String selected = (String) cbReservation.getSelectedItem();
            if (selected == null || selected.startsWith("暂无")) return;
            try {
                long rid = Long.parseLong(selected.split(" - ")[0]);
                List<Participant> list = new ParticipantDAO().listParticipantsByReservation(rid);
                for (Participant p : list) {
                    signModel.addRow(new Object[]{
                            p.getParticipantId(),
                            p.getReservationNo(),
                            p.getMeetingTopic(),
                            p.getStartTime(),
                            p.getEndTime(),
                            p.getParticipantName(),
                            p.getSignInProcess(),
                            p.getSignInTime()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "加载失败：" + ex.getMessage());
            }
        });

        // ====== 部门签到（一键签到全部门） ======
        btnSignDepartment.addActionListener(e -> {
            String selected = (String) cbReservation.getSelectedItem();
            if (selected == null || selected.startsWith("暂无")) {
                JOptionPane.showMessageDialog(this, "请先选择有效的会议");
                return;
            }
            long rid = Long.parseLong(selected.split(" - ")[0]);
            int count = new ParticipantDAO().signInDepartmentByReservation(rid);
            JOptionPane.showMessageDialog(this, "部门签到成功，已签到 " + count + " 人");
            btnLoadParticipants.doClick(); // 刷新表格
        });

        // ====== 刷新会议列表 ======
        btnRefresh.addActionListener(e -> loadReservations.run());

        // ====== 单条签到（保留） ======
        btnSignSelected.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一条记录");
                return;
            }
            long participantId = Long.parseLong(signModel.getValueAt(row, 0).toString());
            boolean ok = new ParticipantDAO().signIn(participantId);
            JOptionPane.showMessageDialog(this, ok ? "签到成功" : "签到失败（可能已签到或会议未开始）");
            btnLoadParticipants.doClick();
        });

        // 初始加载会议列表
        loadReservations.run();

        panel.add(queryPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }


    private void loadRoomOptions() {
        cbRoom.removeAllItems();
        List<String> opts = new MeetingRoomDAO().roomOption();
        for (String s : opts) cbRoom.addItem(s);
    }

    private void addReservation() {
        try {
            String roomText = (String) cbRoom.getSelectedItem();
            if (roomText == null || roomText.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先选择会议室");
                return;
            }

            Matcher matcher = Pattern.compile("^(\\d+)").matcher(roomText.trim());
            if (!matcher.find()) {
                JOptionPane.showMessageDialog(this, "会议室格式异常，请刷新后重试");
                return;
            }
            long roomId = Long.parseLong(matcher.group(1));

            Timestamp start = DateTimeUtil.getTime(timePicker.getStartText());
            Timestamp end = DateTimeUtil.getTime(timePicker.getEndText());
            if (!end.after(start)) {
                JOptionPane.showMessageDialog(this, "结束时间必须大于开始时间");
                return;
            }

            String topic = tfTopic.getText() == null ? "" : tfTopic.getText().trim();
            if (topic.isEmpty()) {
                JOptionPane.showMessageDialog(this, "会议主题不能为空");
                return;
            }

            String countText = tfCount.getText();
            if (countText == null) countText = "";
            countText = countText.trim()
                    .replace('０','0').replace('１','1').replace('２','2').replace('３','3').replace('４','4')
                    .replace('５','5').replace('６','6').replace('７','7').replace('８','8').replace('９','9');
            if (!countText.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "参会人数必须是数字（例如：10）");
                return;
            }
            int count = Integer.parseInt(countText);
            if (count <= 0) {
                JOptionPane.showMessageDialog(this, "参会人数必须大于0");
                return;
            }

            int cap = new MeetingRoomDAO().getCapacityByRoomId(roomId);
            if (cap <= 0) {
                JOptionPane.showMessageDialog(this, "读取会议室容量失败");
                return;
            }
            if (count > cap) {
                JOptionPane.showMessageDialog(this, "参会人数超出会议室容量上限（最大 " + cap + " 人）");
                return;
            }

            ReservationDAO dao = new ReservationDAO();
            if (dao.hasConflict(roomId, start, end)) {
                JOptionPane.showMessageDialog(this, "会议室时间冲突，请更换时间或会议室");
                return;
            }

            boolean ok = dao.addReservation(
                    topic, user.getDeptId(), user.getStaffId(), roomId,
                    start, end, count, tfDesc.getText() == null ? "" : tfDesc.getText().trim()
            );
            JOptionPane.showMessageDialog(this, ok ? "提交成功（待确认）" : "提交失败");
            if (ok) loadMyReservations();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "提交失败：" + e.getMessage());
        }
    }

    private void loadMyReservations() {
        myModel.setRowCount(0);
        List<ReservationList> list = new ReservationDAO().searchMyReservation(user.getStaffId());
        for (ReservationList r : list) {
            myModel.addRow(new Object[]{
                    r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(),
                    r.getRoomName(), r.getStartTime(), r.getEndTime(), r.getProcess()
            });
        }
    }

    private void loadSignRecords() {
        signModel.setRowCount(0);
        List<Participant> list = new ParticipantDAO().listSignRecordsByDept(user.getDeptId());
        System.out.println("Found " + list.size() + " sign records for department " + user.getDeptId());
        for (Participant x : list) {
            signModel.addRow(new Object[]{
                    x.getParticipantId(),
                    x.getReservationNo(),
                    x.getMeetingTopic(),
                    x.getStartTime(),
                    x.getEndTime(),
                    x.getParticipantName(),
                    x.getSignInProcess(),
                    x.getSignInTime()
            });
        }
    }
}