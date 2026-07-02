package Conference_Reservation.GUI;

import Conference_Reservation.DAO.*;
import Conference_Reservation.Model.Participant;
import Conference_Reservation.Model.ReservationList;
import Conference_Reservation.Model.User;
import Conference_Reservation.Util.DateTimeUtil;

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
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private DefaultTableModel freeModel = new DefaultTableModel(
            new Object[]{"会议室ID","会议室","位置","容量","状态"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private DefaultTableModel signModel = new DefaultTableModel(
            new Object[]{"记录ID","预约号","主题","开始","结束","参会人","签到状态","签到时间"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
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
        JPanel panel = new JPanel(new BorderLayout(8,8));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,8,6,8);
        c.fill = GridBagConstraints.HORIZONTAL;
        int r = 0;

        c.gridx=0;c.gridy=r;c.weightx=0; form.add(new JLabel("会议主题"), c);
        c.gridx=1;c.gridy=r;c.weightx=1; form.add(tfTopic, c);
        c.gridx=2;c.gridy=r;c.weightx=0; form.add(new JLabel("会议室"), c);
        c.gridx=3;c.gridy=r;c.weightx=1; form.add(cbRoom, c);

        r++;
        c.gridx=0;c.gridy=r;c.weightx=0; form.add(new JLabel("参会人数"), c);
        c.gridx=1;c.gridy=r;c.weightx=1; form.add(tfCount, c);
        c.gridx=2;c.gridy=r;c.weightx=0; form.add(new JLabel("会议说明"), c);
        c.gridx=3;c.gridy=r;c.weightx=1; form.add(tfDesc, c);

        r++;
        c.gridx=0;c.gridy=r;c.weightx=0; form.add(new JLabel("会议时间"), c);
        c.gridx=1;c.gridy=r;c.gridwidth=3;c.weightx=1; form.add(timePicker, c);

        JButton btnSubmit = new JButton("提交预约");
        JButton btnReloadRoom = new JButton("刷新会议室");
        JPanel op = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        op.add(btnReloadRoom); op.add(btnSubmit);

        btnReloadRoom.addActionListener(e -> loadRoomOptions());
        btnSubmit.addActionListener(e -> addReservation());

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
            boolean ok = new ReservationDAO().cancelReservation(reservationId, user.getStaffID());
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
        JTable table = new JTable(signModel);
        table.setRowHeight(28);

        JButton btnRefresh = new JButton("刷新签到列表");
        JButton btnSign = new JButton("签到选中记录");

        JPanel op = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        op.add(btnRefresh); op.add(btnSign);

        btnRefresh.addActionListener(e -> loadSignRecords());
        btnSign.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一条记录"); return; }
            long participantId = Long.parseLong(signModel.getValueAt(row, 0).toString());
            boolean ok = new ParticipantDAO().signIn(participantId);
            JOptionPane.showMessageDialog(this, ok ? "签到成功" : "签到失败（可能已签到）");
            loadSignRecords();
        });

        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadRoomOptions() {
        cbRoom.removeAllItems();
        List<String> opts = new MeetingRoomDAO().RoomOption();
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
                    topic, user.getDeptID(), user.getStaffID(), roomId,
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
        List<ReservationList> list = new ReservationDAO().searchMyReservation(user.getStaffID());
        for (ReservationList r : list) {
            myModel.addRow(new Object[]{
                    r.getReservationID(), r.getReservationNO(), r.getMeetingTopic(),
                    r.getRoomName(), r.getStartTime(), r.getEndTime(), r.getProcess()
            });
        }
    }

    private void loadSignRecords() {
        signModel.setRowCount(0);
        List<Participant> list = new ParticipantDAO().listSignRecordsByDept(user.getDeptID());
        for (Participant x : list) {
            signModel.addRow(new Object[]{
                    x.getParticipantID(),
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