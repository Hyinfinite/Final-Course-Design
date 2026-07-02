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
            new Object[]{"ID","预约号","主题","会议室","开始","结束","状态"},0);

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
            String roomText = (String) cbRoom.getSelectedItem();
            if (roomText == null || roomText.trim().length() == 0) {
                JOptionPane.showMessageDialog(this, "请先选择会议室");
                return;
            }

            long roomId = Long.parseLong(roomText.split(" - ")[0].trim());

            Timestamp start = DateTimeUtil.getTime(timePicker.getStartText());
            Timestamp end = DateTimeUtil.getTime(timePicker.getEndText());

            if (!end.after(start)) {
                JOptionPane.showMessageDialog(this, "结束时间必须大于开始时间");
                return;
            }

            int count = Integer.parseInt(tfCount.getText().trim());
            if (count <= 0) {
                JOptionPane.showMessageDialog(this, "参会人数必须大于0");
                return;
            }

            ReservationDAO dao = new ReservationDAO();
            if (dao.hasConflict(roomId, start, end)) {
                JOptionPane.showMessageDialog(this, "会议室时间冲突，请更换时间或会议室");
                return;
            }

            boolean ok = dao.addReservation(
                    tfTopic.getText().trim(),
                    user.getDeptID(),
                    user.getStaffID(),
                    roomId,
                    start, end,
                    count,
                    tfDesc.getText().trim()
            );

            JOptionPane.showMessageDialog(this, ok ? "提交成功（待确认）" : "提交失败");
            if (ok) loadMyReservations();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "参会人数必须是数字");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "日期不合法，请检查年月日");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "输入有误，请检查后重试");
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