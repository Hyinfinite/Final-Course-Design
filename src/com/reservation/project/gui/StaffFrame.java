package com.reservation.project.gui;

import com.reservation.project.dao.*;
import com.reservation.project.model.Participant;
import com.reservation.project.model.ReservationList;
import com.reservation.project.model.StaffInfo;
import com.reservation.project.model.User;
import com.reservation.project.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StaffFrame extends JFrame {
    private final User user;

    // 提交预约组件
    private JComboBox<String> cbRoom = new JComboBox<>();
    private JTextField tfTopic = new JTextField(18);
    private JTextField tfDesc = new JTextField(18);
    private TimePanel timePicker = new TimePanel();

    // 参会人员勾选列表
    private JPanel checkboxPanel = new JPanel(new GridLayout(0, 2, 5, 2));
    private java.util.List<JCheckBox> staffCheckBoxes = new java.util.ArrayList<>();
    private JLabel lblSelectedCount = new JLabel("已选 0 人");
    private JLabel lblTotalCount = new JLabel("总人数：0");


    // 我的预约表格
    private DefaultTableModel myModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","参会人数","状态","审批意见"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // 空闲查询表格
    private DefaultTableModel freeModel = new DefaultTableModel(
            new Object[]{"会议室ID","会议室","位置","容量","状态"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // 签到管理表格
    private DefaultTableModel signModel = new DefaultTableModel(
            new Object[]{"记录ID","预约号","主题","开始时间","结束时间","参会人工号","参会人","签到状态","签到时间"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    public StaffFrame(User user) {
        this.user = user;

        setTitle("行政人员 - " + user.getStaffName());
        setSize(1220, 780);
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
        refreshStaffCheckList();          // 加载本部门员工
        loadMyReservations();
        setVisible(true);
    }

    // -------------------- 提交预约面板 --------------------
    private JPanel buildSubmitPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 表单区域 - 使用 FlowLayout 确保标签和输入框在同一行
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));

        // 第一行：会议主题
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row1.add(new JLabel("会议主题"));
        row1.add(tfTopic);
        form.add(row1);

        // 第二行：会议室
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row2.add(new JLabel("会议室"));
        row2.add(cbRoom);
        form.add(row2);

        // 第三行：会议说明
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row3.add(new JLabel("会议说明"));
        row3.add(tfDesc);
        form.add(row3);

        // 第四行：会议时间
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row4.add(new JLabel("会议时间"));
        row4.add(timePicker);
        form.add(row4);

        // 参会人员选择区域 - 保持不变
        JPanel staffPanel = new JPanel(new BorderLayout(5, 5));
        staffPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // 在提交预约面板的参会人员选择区域添加人数统计
        JLabel lblTotalCount = new JLabel("总人数：0");
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSelectAll = new JButton("全选");
        JButton btnDeselectAll = new JButton("取消全选");
        controlPanel.add(btnSelectAll);
        controlPanel.add(btnDeselectAll);
        controlPanel.add(lblSelectedCount);
        controlPanel.add(lblTotalCount);

        JScrollPane scrollStaff = new JScrollPane(checkboxPanel);
        scrollStaff.setPreferredSize(new Dimension(400, 100));

        staffPanel.add(controlPanel, BorderLayout.NORTH);
        staffPanel.add(scrollStaff, BorderLayout.CENTER);

        // 操作按钮区域 - 保持不变
        JButton btnSubmit = new JButton("提交预约");
        JButton btnReloadRoom = new JButton("刷新会议室");
        JButton btnReloadStaff = new JButton("刷新人员");
        JPanel op = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        op.add(btnReloadRoom);
        op.add(btnReloadStaff);
        op.add(btnSubmit);

        // 事件监听 - 保持不变
        btnReloadRoom.addActionListener(e -> loadRoomOptions());
        btnReloadStaff.addActionListener(e -> refreshStaffCheckList());
        btnSubmit.addActionListener(e -> addReservation());

        btnSelectAll.addActionListener(e -> {
            staffCheckBoxes.forEach(cb -> cb.setSelected(true));
            updateSelectedCount();
        });
        btnDeselectAll.addActionListener(e -> {
            staffCheckBoxes.forEach(cb -> cb.setSelected(false));
            updateSelectedCount();
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(staffPanel, BorderLayout.CENTER);
        panel.add(op, BorderLayout.SOUTH);
        return panel;
    }



    // 刷新员工复选框列表
    private void refreshStaffCheckList() {
        staffCheckBoxes.clear();
        checkboxPanel.removeAll();
        StaffDAO dao = new StaffDAO();
        List<StaffInfo> staffList = dao.getStaffByDept(user.getDeptId());
        for (StaffInfo s : staffList) {
            JCheckBox cb = new JCheckBox(s.getStaffName() + " (" + s.getStaffNo() + ")");
            cb.putClientProperty("staffId", s.getStaffId());
            cb.addActionListener(e -> updateSelectedCount());
            staffCheckBoxes.add(cb);
            checkboxPanel.add(cb);
            if (s.getStaffId() == user.getStaffId()) {
                cb.setSelected(true);
            }
        }
        checkboxPanel.revalidate();
        checkboxPanel.repaint();
        updateSelectedCount();
    }

    // 更新已选人数显示
    private void updateSelectedCount() {
        // 计算已选人数
        long count = staffCheckBoxes.stream().filter(JCheckBox::isSelected).count();
        // 更新已选人数标签
        lblSelectedCount.setText("已选 " + count + " 人");
        // 更新总人数标签
        lblTotalCount.setText("总人数：" + staffCheckBoxes.size());
    }

    // -------------------- 我的预约面板 --------------------
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

    // -------------------- 空闲查询面板 --------------------
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

    // -------------------- 签到管理面板 --------------------
    private JPanel buildSignPanel() {
        JPanel panel = new JPanel(new BorderLayout(8,8));

        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JComboBox<String> cbReservation = new JComboBox<>();

        JButton btnLoadParticipants = new JButton("加载参会人员");
        JButton btnSignDepartment = new JButton("部门签到");
        JButton btnRefresh = new JButton("刷新会议列表");
        JButton btnSignSelected = new JButton("签到选中记录");

        queryPanel.add(new JLabel("选择会议"));
        queryPanel.add(cbReservation);
        queryPanel.add(btnLoadParticipants);
        queryPanel.add(btnSignDepartment);
        queryPanel.add(btnRefresh);
        queryPanel.add(btnSignSelected);

        JTable table = new JTable(signModel);
        table.setRowHeight(28);

        // 加载会议列表
        Runnable loadReservations = () -> {
            cbReservation.removeAllItems();
            signModel.setRowCount(0);
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

        // 加载参会人员
        btnLoadParticipants.addActionListener(e -> {
            signModel.setRowCount(0);
            String selected = (String) cbReservation.getSelectedItem();
            if (selected == null || selected.startsWith("暂无")) {
                JOptionPane.showMessageDialog(this, "请先选择有效的会议");
                return;
            }
            try {
                long rid = Long.parseLong(selected.split(" - ")[0]);
                List<Participant> list = new ParticipantDAO().listParticipantsByReservation(rid);
                if (list.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "该会议暂无参会人员");
                }
                for (Participant p : list) {
                    signModel.addRow(new Object[]{
                            p.getParticipantId(),
                            p.getReservationNo(),
                            p.getMeetingTopic(),
                            p.getStartTime(),
                            p.getEndTime(),
                            p.getParticipantStaffNo(),
                            p.getParticipantName(),
                            p.getSignInProcess(),
                            p.getSignInTime()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "加载失败：" + ex.getMessage());
            }
        });

        // 部门签到：签到该会议所有参会人员（已勾选的人员）
        btnSignDepartment.addActionListener(e -> {
            String selected = (String) cbReservation.getSelectedItem();
            if (selected == null || selected.startsWith("暂无")) {
                JOptionPane.showMessageDialog(this, "请先选择有效的会议");
                return;
            }
            long rid = Long.parseLong(selected.split(" - ")[0]);

            // 检查当前用户是否为会议申请人
            if (!new ReservationDAO().isReservationApplicant(rid, user.getStaffId())) {
                JOptionPane.showMessageDialog(this, "只有会议申请人可以进行部门签到");
                return;
            }

            int count = new ParticipantDAO().signInAllParticipants(rid);
            JOptionPane.showMessageDialog(this, "部门签到成功，已签到 " + count + " 人");
            btnLoadParticipants.doClick();
        });

        // 单条签到
        btnSignSelected.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一条记录");
                return;
            }
            Object idObj = signModel.getValueAt(row, 0);
            if (idObj == null || idObj.toString().isEmpty()) {
                JOptionPane.showMessageDialog(this, "所选记录无效，请重新加载参会人员");
                return;
            }
            long participantId = Long.parseLong(idObj.toString());
            boolean ok = new ParticipantDAO().signIn(participantId, user.getStaffId());
            JOptionPane.showMessageDialog(this, ok ? "签到成功" : "签到失败（可能已签到、会议未开始或无权限）");
            btnLoadParticipants.doClick();
        });


        btnRefresh.addActionListener(e -> loadReservations.run());

        loadReservations.run();

        panel.add(queryPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // -------------------- 辅助方法 --------------------
    private void loadRoomOptions() {
        cbRoom.removeAllItems();
        List<String> opts = new MeetingRoomDAO().roomOption();
        for (String s : opts) cbRoom.addItem(s);
    }

    private void addReservation() {
        try {
            // 1. 解析会议室
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

            // 2. 解析时间
            Timestamp start = DateTimeUtil.getTime(timePicker.getStartText());
            Timestamp end = DateTimeUtil.getTime(timePicker.getEndText());
            if (!end.after(start)) {
                JOptionPane.showMessageDialog(this, "结束时间必须大于开始时间");
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (start.before(now)) {
                JOptionPane.showMessageDialog(this, "开始时间不能早于当前时间");
                return;
            }

            // 3. 会议主题
            String topic = tfTopic.getText().trim();
            if (topic.isEmpty()) {
                JOptionPane.showMessageDialog(this, "会议主题不能为空");
                return;
            }

            // 4. 参会人员（勾选）
            List<Long> selectedStaffIds = staffCheckBoxes.stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> (Long) cb.getClientProperty("staffId"))
                    .collect(Collectors.toList());
            // 确保当前用户在参会人员列表中
            if (!selectedStaffIds.contains(user.getStaffId())) {
                selectedStaffIds.add(user.getStaffId());
            }
            // 添加参会人数校验
            if (selectedStaffIds.size() < 2) {
                JOptionPane.showMessageDialog(this, "参会人数必须大于等于2人");
                return;
            }

            // 5. 容量检查（使用勾选人数）
            int cap = new MeetingRoomDAO().getCapacityByRoomId(roomId);
            if (cap <= 0) {
                JOptionPane.showMessageDialog(this, "读取会议室容量失败");
                return;
            }
            if (selectedStaffIds.size() > cap) {
                JOptionPane.showMessageDialog(this, "参会人数超出会议室容量上限（最大 " + cap + " 人）");
                return;
            }

            // 6. 冲突检查
            ReservationDAO dao = new ReservationDAO();
            if (dao.hasConflict(roomId, start, end)) {
                JOptionPane.showMessageDialog(this, "会议室时间冲突，请更换时间或会议室");
                return;
            }

            // 7. 提交预约（同时插入参会人员）
            String desc = tfDesc.getText().trim();
            boolean ok = dao.addReservation(
                    topic, user.getDeptId(), user.getStaffId(), roomId,
                    start, end, selectedStaffIds.size(), desc, selectedStaffIds
            );
            JOptionPane.showMessageDialog(this, ok ? "提交成功（待确认）" : "提交失败");
            if (ok) {
                loadMyReservations();
                // 清空表单
                tfTopic.setText("");
                tfDesc.setText("");
                //刷新会议室和人员勾选列表
                loadRoomOptions();
                refreshStaffCheckList();
            }

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
                    r.getRoomName(), r.getStartTime(), r.getEndTime(), r.getParticipantCount(), r.getProcess(), r.getComment()
            });
        }
    }
}