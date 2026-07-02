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
    private JTextField tfTopic = new JTextField(14);
    private JTextField tfStart = new JTextField("2026-07-01 09:00:00", 14);
    private JTextField tfEnd = new JTextField("2026-07-01 10:00:00", 14);
    private JTextField tfCount = new JTextField("10", 14);
    private JTextField tfDesc = new JTextField(14);

    private DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","状态"},0);

    public StaffFrame(User user) {
        this.user = user;
        setTitle("行政人员 - " + user.getStaffName());
        setSize(980, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel top = new JPanel(new GridLayout(3,4,8,8));
        top.setBorder(BorderFactory.createTitledBorder("提交预约"));
        for (String s : new MeetingRoomDAO().RoomOption()) cbRoom.addItem(s);

        top.add(new JLabel("会议主题")); top.add(tfTopic);
        top.add(new JLabel("会议室")); top.add(cbRoom);
        top.add(new JLabel("开始时间")); top.add(tfStart);
        top.add(new JLabel("结束时间")); top.add(tfEnd);
        top.add(new JLabel("参会人数")); top.add(tfCount);
        top.add(new JLabel("会议说明")); top.add(tfDesc);

        JTable table = new JTable(model);
        JButton btnAdd = new JButton("提交预约");
        JButton btnRefresh = new JButton("刷新");
        JButton btnCancel = new JButton("撤销选中");

        JPanel bottom = new JPanel();
        bottom.add(btnAdd); bottom.add(btnRefresh); bottom.add(btnCancel);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addReservation());
        btnRefresh.addActionListener(e -> loadMyReservations());
        btnCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请选择记录"); return; }
            long reservationId = Long.parseLong(model.getValueAt(row,0).toString());
            boolean ok = new ReservationDAO().cancelReservation(reservationId, user.getStaffID());
            JOptionPane.showMessageDialog(this, ok ? "撤销成功" : "撤销失败（仅待确认可撤销）");
            loadMyReservations();
        });

        loadMyReservations();
        setVisible(true);
    }

    private void addReservation() {
        try {
            String roomText = (String) cbRoom.getSelectedItem();
            long roomId = Long.parseLong(roomText.split(" - ")[0]);

            Timestamp start = DateTimeUtil.getTime(tfStart.getText());
            Timestamp end = DateTimeUtil.getTime(tfEnd.getText());
            if (!end.after(start)) {
                JOptionPane.showMessageDialog(this, "结束时间必须大于开始时间");
                return;
            }

            ReservationDAO dao = new ReservationDAO();
            if (dao.hasConflict(roomId, start, end)) {
                JOptionPane.showMessageDialog(this, "会议室时间冲突");
                return;
            }

            boolean ok = dao.addReservation(tfTopic.getText().trim(), user.getDeptID(), user.getStaffID(), roomId,
                    start, end, Integer.parseInt(tfCount.getText().trim()), tfDesc.getText().trim());

            JOptionPane.showMessageDialog(this, ok ? "提交成功" : "提交失败");
            if (ok) loadMyReservations();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "输入格式错误，时间格式 yyyy-MM-dd HH:mm:ss");
        }
    }

    private void loadMyReservations() {
        model.setRowCount(0);
        List<ReservationList> list = new ReservationDAO().searchMyReservation(user.getStaffID());
        for (ReservationList r : list) {
            model.addRow(new Object[]{r.getReservationID(), r.getReservationNO(), r.getMeetingTopic(),
                    r.getRoomName(), r.getStartTime(), r.getEndTime(), r.getProcess()});
        }
    }
}