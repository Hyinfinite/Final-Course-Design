package Conference_Reservation.GUI;

import Conference_Reservation.DAO.ReservationDAO;
import Conference_Reservation.Model.User;

import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {
    public AdminFrame(User user) {
        setTitle("系统管理员 - " + user.getStaffName());
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int[] s = new ReservationDAO().processCount();
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setText("欢迎，" + user.getStaffName() + "\n\n"
                   + "总预约数：" + s[0] + "\n"
                   + "已确认：" + s[1] + "\n"
                   + "待确认：" + s[2] + "\n");

        add(new JScrollPane(area), BorderLayout.CENTER);
        setVisible(true);
    }
}