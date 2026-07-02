package Conference_Reservation.GUI;

import Conference_Reservation.DAO.ReservationDAO;
import Conference_Reservation.Model.ReservationList;
import Conference_Reservation.Model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManagerFrame extends JFrame {
    private User user;
    private DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","申请人","会议室","开始","结束","状态"},0);
    private JTextField tfComment = new JTextField(20);

    public ManagerFrame(User user) {
        this.user = user;
        setTitle("会议室管理员 - " + user.getStaffName());
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnPass = new JButton("通过");
        JButton btnReject = new JButton("驳回");
        JButton btnRefresh = new JButton("刷新");
        JPanel south = new JPanel();
        south.add(new JLabel("意见:"));
        south.add(tfComment);
        south.add(btnPass);
        south.add(btnReject);
        south.add(btnRefresh);
        add(south, BorderLayout.SOUTH);

        btnPass.addActionListener(e -> process(table, "已确认"));
        btnReject.addActionListener(e -> process(table, "已驳回"));
        btnRefresh.addActionListener(e -> loadPending());

        loadPending();
        setVisible(true);
    }

    private void process(JTable table, String status) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "请选择一条记录"); return; }
        long reservationId = Long.parseLong(model.getValueAt(row,0).toString());
        boolean ok = new ReservationDAO().processReservation(reservationId, user.getStaffID(), status, tfComment.getText().trim());
        JOptionPane.showMessageDialog(this, ok ? "处理成功" : "处理失败");
        loadPending();
    }

    private void loadPending() {
        model.setRowCount(0);
        List<ReservationList> list = new ReservationDAO().searchPendingReservation();
        for (ReservationList r : list) {
            model.addRow(new Object[]{r.getReservationID(), r.getReservationNO(), r.getMeetingTopic(), r.getApplicantName(),
                    r.getRoomName(), r.getStartTime(), r.getEndTime(), r.getProcess()});
        }
    }
}