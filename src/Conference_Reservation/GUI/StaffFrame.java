package Conference_Reservation.GUI;

import Conference_Reservation.DAO.MeetingRoomDAO;
import Conference_Reservation.DAO.ReservationDAO;
import Conference_Reservation.Model.ReservationList;
import Conference_Reservation.Model.User;
import Conference_Reservation.Util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class StaffFrame extends JFrame {
    private User user;

    private JComboBox<String> cbRoom = new JComboBox<String>();
    private JTextField tfTopic = new JTextField(18);
    private JTextField tfCount = new JTextField("10", 18);
    private JTextField tfDesc = new JTextField(18);

    private TimePanel timePicker = new TimePanel();

    private DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","状态"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public StaffFrame(User user) {
        this.user = user;

        setTitle("行政人员 - " + user.getStaffName());
        setSize(1120, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel top = buildTopPanel();
        JTable table = new JTable(model);
        table.setRowHeight(28);
        JScrollPane tablePane = new JScrollPane(table);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnAdd = new JButton("提交预约");
        JButton btnRefresh = new JButton("刷新我的预约");
        JButton btnCancel = new JButton("撤销选中预约");
        bottom.add(btnAdd);
        bottom.add(btnRefresh);
        bottom.add(btnCancel);

        add(top, BorderLayout.NORTH);
        add(tablePane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addReservation());
        btnRefresh.addActionListener(e -> loadMyReservations());
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请选择记录");
                return;
            }
            long reservationId = Long.parseLong(model.getValueAt(row,0).toString());
            boolean ok = new ReservationDAO().cancelReservation(reservationId, user.getStaffID());
            JOptionPane.showMessageDialog(this, ok ? "撤销成功" : "撤销失败（仅待确认可撤销）");
            loadMyReservations();
        });

        loadMyReservations();
        setVisible(true);
    }

    private JPanel buildTopPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder("提交预约"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        for (String s : new MeetingRoomDAO().RoomOption()) cbRoom.addItem(s);

        int row = 0;

        c.gridx = 0; c.gridy = row; c.weightx = 0; form.add(new JLabel("会议主题"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1; form.add(tfTopic, c);
        c.gridx = 2; c.gridy = row; c.weightx = 0; form.add(new JLabel("会议室"), c);
        c.gridx = 3; c.gridy = row; c.weightx = 1; form.add(cbRoom, c);

        row++;
        c.gridx = 0; c.gridy = row; c.weightx = 0; form.add(new JLabel("参会人数"), c);
        c.gridx = 1; c.gridy = row; c.weightx = 1; form.add(tfCount, c);
        c.gridx = 2; c.gridy = row; c.weightx = 0; form.add(new JLabel("会议说明"), c);
        c.gridx = 3; c.gridy = row; c.weightx = 1; form.add(tfDesc, c);

        row++;
        c.gridx = 0; c.gridy = row; c.weightx = 0; c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("会议时间"), c);
        c.gridx = 1; c.gridy = row; c.gridwidth = 3; c.weightx = 1;
        form.add(timePicker, c);

        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private void addReservation() {
        try {
            // 1) 解析会议室ID（兼容： "3 - xxx" / "3 xxx" / "3:xxx"）
            String roomText = (String) cbRoom.getSelectedItem();
            if (roomText == null || roomText.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先选择会议室");
                return;
            }

            String t = roomText.trim();
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(\\d+)").matcher(t);
            if (!matcher.find()) {
                JOptionPane.showMessageDialog(this, "会议室数据格式异常，请刷新后重试");
                return;
            }
            long roomId = Long.parseLong(matcher.group(1));

            // 2) 时间获取与校验
            Timestamp start = DateTimeUtil.getTime(timePicker.getStartText());
            Timestamp end = DateTimeUtil.getTime(timePicker.getEndText());

            if (!end.after(start)) {
                JOptionPane.showMessageDialog(this, "结束时间必须大于开始时间");
                return;
            }

            // 3) 会议主题校验
            String topic = tfTopic.getText() == null ? "" : tfTopic.getText().trim();
            if (topic.isEmpty()) {
                JOptionPane.showMessageDialog(this, "会议主题不能为空");
                return;
            }

            // 4) 参会人数校验（支持全角数字）
            String countText = tfCount.getText();
            if (countText == null) countText = "";
            countText = countText.trim()
                    .replace('０','0').replace('１','1').replace('２','2')
                    .replace('３','3').replace('４','4').replace('５','5')
                    .replace('６','6').replace('７','7').replace('８','8').replace('９','9');

            if (!countText.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "参会人数必须是数字（例如：10）");
                return;
            }
            int count = Integer.parseInt(countText);
            MeetingRoomDAO roomDAO = new MeetingRoomDAO();
            int capacity = roomDAO.getCapacityByRoomId(roomId);
            if (capacity <= 0) {
                JOptionPane.showMessageDialog(this, "读取会议室容量失败，请刷新后重试");
                return;
            }
            if (count > capacity) {
                JOptionPane.showMessageDialog(this, "参会人数超出会议室容量上限（最大 " + capacity + " 人）");
                return;
            }
            if (count <= 0) {
                JOptionPane.showMessageDialog(this, "参会人数必须大于0");
                return;
            }

            // 5) 备注
            String desc = tfDesc.getText() == null ? "" : tfDesc.getText().trim();

            // 6) 冲突校验 + 提交
            ReservationDAO dao = new ReservationDAO();
            if (dao.hasConflict(roomId, start, end)) {
                JOptionPane.showMessageDialog(this, "会议室时间冲突，请更换时间或会议室");
                return;
            }

            boolean ok = dao.addReservation(
                    topic,
                    user.getDeptID(),
                    user.getStaffID(),
                    roomId,
                    start,
                    end,
                    count,
                    desc
            );

            JOptionPane.showMessageDialog(this, ok ? "提交成功（待确认）" : "提交失败，请稍后重试");
            if (ok) {
                loadMyReservations();
            }

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "时间格式错误，请检查日期与时间选择");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "提交失败：" + e.getMessage());
        }
    }

    private void loadMyReservations() {
        model.setRowCount(0);
        List<ReservationList> list = new ReservationDAO().searchMyReservation(user.getStaffID());
        for (ReservationList r : list) {
            model.addRow(new Object[]{
                    r.getReservationID(),
                    r.getReservationNO(),
                    r.getMeetingTopic(),
                    r.getRoomName(),
                    r.getStartTime(),
                    r.getEndTime(),
                    r.getProcess()
            });
        }
    }
}