package Conference_Reservation.GUI;

import Conference_Reservation.DAO.StaffDAO;
import Conference_Reservation.Model.StaffInfo;
import Conference_Reservation.Model.User;

import javax.swing.*;
import java.awt.*;

public class ProfileDialog extends JDialog {
    private final User loginUser;
    private final StaffDAO dao = new StaffDAO();

    private JTextField tfName = new JTextField(16);
    private JTextField tfGender = new JTextField(8);
    private JTextField tfPos = new JTextField(16);
    private JTextField tfPhone = new JTextField(16);

    private JPasswordField oldPwd = new JPasswordField(16);
    private JPasswordField newPwd = new JPasswordField(16);
    private JPasswordField confirmPwd = new JPasswordField(16);

    public ProfileDialog(Frame owner, User user) {
        super(owner, "个人信息与密码修改", true);
        this.loginUser = user;

        setSize(520, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("个人信息", buildProfilePanel());
        tabs.addTab("修改密码", buildPasswordPanel());

        add(tabs, BorderLayout.CENTER);
        loadProfile();
    }

    private JPanel buildProfilePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0;c.gridy=0; p.add(new JLabel("姓名"), c);
        c.gridx=1;c.gridy=0; p.add(tfName, c);

        c.gridx=0;c.gridy=1; p.add(new JLabel("性别"), c);
        c.gridx=1;c.gridy=1; p.add(tfGender, c);

        c.gridx=0;c.gridy=2; p.add(new JLabel("职务"), c);
        c.gridx=1;c.gridy=2; p.add(tfPos, c);

        c.gridx=0;c.gridy=3; p.add(new JLabel("电话"), c);
        c.gridx=1;c.gridy=3; p.add(tfPhone, c);

        JButton btnSave = new JButton("保存信息");
        c.gridx=1;c.gridy=4; p.add(btnSave, c);

        btnSave.addActionListener(e -> {
            boolean ok = dao.updateOwnProfile(
                    loginUser.getStaffID(),
                    tfName.getText().trim(),
                    tfGender.getText().trim(),
                    tfPos.getText().trim(),
                    tfPhone.getText().trim()
            );
            JOptionPane.showMessageDialog(this, ok ? "保存成功" : "保存失败");
        });

        return p;
    }

    private JPanel buildPasswordPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx=0;c.gridy=0; p.add(new JLabel("旧密码"), c);
        c.gridx=1;c.gridy=0; p.add(oldPwd, c);

        c.gridx=0;c.gridy=1; p.add(new JLabel("新密码"), c);
        c.gridx=1;c.gridy=1; p.add(newPwd, c);

        c.gridx=0;c.gridy=2; p.add(new JLabel("确认新密码"), c);
        c.gridx=1;c.gridy=2; p.add(confirmPwd, c);

        JButton btnSave = new JButton("修改密码");
        c.gridx=1;c.gridy=3; p.add(btnSave, c);

        btnSave.addActionListener(e -> {
            String o = new String(oldPwd.getPassword()).trim();
            String n = new String(newPwd.getPassword()).trim();
            String cfm = new String(confirmPwd.getPassword()).trim();

            if (o.isEmpty() || n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "密码不能为空");
                return;
            }
            if (!n.equals(cfm)) {
                JOptionPane.showMessageDialog(this, "两次新密码不一致");
                return;
            }

            boolean ok = dao.changePassword(loginUser.getStaffID(), o, n);
            JOptionPane.showMessageDialog(this, ok ? "修改成功" : "修改失败（旧密码错误）");
        });

        return p;
    }

    private void loadProfile() {
        StaffInfo s = dao.findByID(loginUser.getStaffID());
        if (s == null) return;
        tfName.setText(s.getStaffName());
        tfGender.setText(s.getGender());
        tfPos.setText(s.getPosition());
        tfPos.setText(s.getPosition());
        tfPhone.setText(s.getPhone());
    }
}