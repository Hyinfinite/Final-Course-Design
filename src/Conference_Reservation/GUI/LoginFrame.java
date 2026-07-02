package Conference_Reservation.GUI;

import Conference_Reservation.DAO.UserDAO;
import Conference_Reservation.Model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField tfNo = new JTextField(15);
    private JPasswordField tfPwd = new JPasswordField(15);
    private JComboBox<String> cbRole = new JComboBox<String>(new String[]{"SYS_Admin","ROOM_Admin","Staff"});

    public LoginFrame() {
        setTitle("会议预约系统 - 登录");
        setSize(420, 240);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4,2,8,8));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        panel.add(new JLabel("工号"));
        panel.add(tfNo);
        panel.add(new JLabel("密码"));
        panel.add(tfPwd);
        panel.add(new JLabel("角色"));
        panel.add(cbRole);

        JButton btn = new JButton("登录");
        panel.add(new JLabel(""));
        panel.add(btn);
        add(panel);

        btn.addActionListener(e -> doLogin());
        setVisible(true);
    }

    private void doLogin() {
        String no = tfNo.getText().trim();
        String pwd = new String(tfPwd.getPassword()).trim();
        String role = (String) cbRole.getSelectedItem();

        User u = new UserDAO().Login(no, pwd, role);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "登录失败");
            return;
        }
        dispose();
        if ("SYS_Admin".equals(u.getAccessLevel())) new AdminFrame(u);
        else if ("ROOM_Admin".equals(u.getAccessLevel())) new ManagerFrame(u);
        else new StaffFrame(u);
    }
}