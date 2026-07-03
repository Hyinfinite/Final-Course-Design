package com.reservation.project.gui;

import com.reservation.project.dao.*;
import com.reservation.project.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminFrame extends JFrame {
    private final User loginUser;

    public AdminFrame(User user) {
        this.loginUser = user;

        setTitle("系统管理员 - " + user.getStaffName());
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel topInfo = new JLabel("当前用户：" + user.getStaffName() + "（SYS_ADMIN）");
        topInfo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("部门管理", buildDeptPanel());
        tabs.addTab("会议室管理", buildRoomPanel());
        tabs.addTab("行政人员管理", buildStaffPanel());
        tabs.addTab("预约记录管理", buildReservationQueryPanel());
        tabs.addTab("统计报表", buildReportPanel());
        tabs.addTab("修改密码", buildPasswordPanel());

        add(topInfo, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    // ===================== 1. 部门管理 =====================
    private JPanel buildDeptPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "部门名称"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        JTextField tfName = new JTextField(18);
        JButton btnAdd = new JButton("新增");
        JButton btnUpd = new JButton("修改");
        JButton btnDel = new JButton("删除");
        JButton btnRef = new JButton("刷新");

        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("部门名称"));
        op.add(tfName);
        op.add(btnAdd); op.add(btnUpd); op.add(btnDel); op.add(btnRef);

        DepartmentDAO dao = new DepartmentDAO();

        Runnable load = () -> {
            model.setRowCount(0);
            List<Department> list = dao.listAll();
            for (Department d : list) model.addRow(new Object[]{d.getDeptId(), d.getDeptName()});
        };

        btnAdd.addActionListener(e -> {
            String name = tfName.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "部门名称不能为空"); return; }
            JOptionPane.showMessageDialog(this, dao.addDepartment(name) ? "新增成功" : "新增失败（可能重名）");
            load.run();
        });

        btnUpd.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一行"); return; }
            long id = Long.parseLong(model.getValueAt(row, 0).toString());
            String name = tfName.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "请输入新部门名称"); return; }
            JOptionPane.showMessageDialog(this, dao.updateDepartment(id, name) ? "修改成功" : "修改失败");
            load.run();
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一行"); return; }
            long id = Long.parseLong(model.getValueAt(row, 0).toString());
            int ok = JOptionPane.showConfirmDialog(this, "确认删除该部门？", "确认", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            JOptionPane.showMessageDialog(this, dao.deleteDepartment(id) ? "删除成功" : "删除失败（可能被人员/预约引用）");
            load.run();
        });

        btnRef.addActionListener(e -> load.run());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) tfName.setText(String.valueOf(model.getValueAt(row, 1)));
        });

        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        load.run();
        return panel;
    }

    // ===================== 2. 会议室管理 =====================
    private JPanel buildRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID","编号","名称","位置","容量","投影仪","音响"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        JTextField tfCode = new JTextField(8);
        JTextField tfName = new JTextField(10);
        JTextField tfLoc = new JTextField(10);
        JTextField tfCap = new JTextField(6);
        JCheckBox cbProj = new JCheckBox("投影仪");
        JCheckBox cbAudio = new JCheckBox("音响");

        JButton btnAdd = new JButton("新增");
        JButton btnUpd = new JButton("修改");
        JButton btnDel = new JButton("删除");
        JButton btnRef = new JButton("刷新");

        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("编号")); op.add(tfCode);
        op.add(new JLabel("名称")); op.add(tfName);
        op.add(new JLabel("位置")); op.add(tfLoc);
        op.add(new JLabel("容量")); op.add(tfCap);
        op.add(cbProj); op.add(cbAudio);
        op.add(btnAdd); op.add(btnUpd); op.add(btnDel); op.add(btnRef);

        MeetingRoomManageDAO dao = new MeetingRoomManageDAO();

        Runnable load = () -> {
            model.setRowCount(0);
            for (MeetingRoom m : dao.listAll()) {
                model.addRow(new Object[]{
                        m.getRoomId(), m.getRoomCode(), m.getRoomName(), m.getLocation(),
                        m.getCapacity(), m.getHasProjector() == 1 ? "有" : "无", m.getHasAudio() == 1 ? "有" : "无"
                });
            }
        };

        btnAdd.addActionListener(e -> {
            try {
                String code = tfCode.getText().trim();
                String name = tfName.getText().trim();
                String loc = tfLoc.getText().trim();
                int cap = Integer.parseInt(tfCap.getText().trim());
                if (code.isEmpty() || name.isEmpty() || cap <= 0) {
                    JOptionPane.showMessageDialog(this, "编号/名称不能为空，容量>0");
                    return;
                }
                boolean ok = dao.addRoom(code, name, loc, cap, cbProj.isSelected()?1:0, cbAudio.isSelected()?1:0);
                JOptionPane.showMessageDialog(this, ok ? "新增成功" : "新增失败（编号可能重复）");
                load.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "容量必须是数字");
            }
        });

        btnUpd.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一行"); return; }
            try {
                long id = Long.parseLong(model.getValueAt(row, 0).toString());
                String code = tfCode.getText().trim();
                String name = tfName.getText().trim();
                String loc = tfLoc.getText().trim();
                int cap = Integer.parseInt(tfCap.getText().trim());
                boolean ok = dao.updateRoom(id, code, name, loc, cap, cbProj.isSelected()?1:0, cbAudio.isSelected()?1:0);
                JOptionPane.showMessageDialog(this, ok ? "修改成功" : "修改失败");
                load.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "输入不合法");
            }
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一行"); return; }
            long id = Long.parseLong(model.getValueAt(row, 0).toString());
            int ok = JOptionPane.showConfirmDialog(this, "确认删除该会议室？", "确认", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            JOptionPane.showMessageDialog(this, dao.deleteRoom(id) ? "删除成功" : "删除失败（可能被预约引用）");
            load.run();
        });

        btnRef.addActionListener(e -> load.run());

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                tfCode.setText(String.valueOf(model.getValueAt(row, 1)));
                tfName.setText(String.valueOf(model.getValueAt(row, 2)));
                tfLoc.setText(String.valueOf(model.getValueAt(row, 3)));
                tfCap.setText(String.valueOf(model.getValueAt(row, 4)));
                cbProj.setSelected("有".equals(String.valueOf(model.getValueAt(row, 5))));
                cbAudio.setSelected("有".equals(String.valueOf(model.getValueAt(row, 6))));
            }
        });

        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        load.run();
        return panel;
    }

    // ===================== 3. 行政人员管理 =====================
    private JPanel buildStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID","工号","姓名","部门ID","部门","性别","职务","电话","角色"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        JTextField tfNo = new JTextField(8);
        JTextField tfName = new JTextField(8);
        JTextField tfDept = new JTextField(5);
        JTextField tfGender = new JTextField(4);
        JTextField tfPos = new JTextField(8);
        JTextField tfPhone = new JTextField(10);
        JComboBox<String> cbRole = new JComboBox<String>(new String[]{"STAFF","ROOM_ADMIN","SYS_ADMIN"});

        JButton btnAdd = new JButton("新增人员");
        JButton btnUpd = new JButton("修改信息");
        JButton btnReset = new JButton("重置密码123456");
        JButton btnSetManager = new JButton("设为会议室管理员");
        JButton btnRef = new JButton("刷新");

        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("工号")); op.add(tfNo);
        op.add(new JLabel("姓名")); op.add(tfName);
        op.add(new JLabel("部门ID")); op.add(tfDept);
        op.add(new JLabel("性别")); op.add(tfGender);
        op.add(new JLabel("职务")); op.add(tfPos);
        op.add(new JLabel("电话")); op.add(tfPhone);
        op.add(new JLabel("角色")); op.add(cbRole);
        op.add(btnAdd); op.add(btnUpd); op.add(btnReset); op.add(btnSetManager); op.add(btnRef);

        StaffDAO dao = new StaffDAO();

        Runnable load = () -> {
            model.setRowCount(0);
            for (StaffInfo s : dao.listAll()) {
                model.addRow(new Object[]{
                        s.getStaffId(), s.getStaffNo(), s.getStaffName(), s.getDeptId(), s.getDeptName(),
                        s.getGender(), s.getPosition(), s.getPhone(), s.getAccessLevel()
                });
            }
        };

        btnAdd.addActionListener(e -> {
            try {
                String no = tfNo.getText().trim();
                String name = tfName.getText().trim();
                long deptId = Long.parseLong(tfDept.getText().trim());
                String gender = tfGender.getText().trim();
                String pos = tfPos.getText().trim();
                String phone = tfPhone.getText().trim();
                String role = String.valueOf(cbRole.getSelectedItem());

                if (no.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "工号和姓名不能为空");
                    return;
                }
                boolean ok = dao.addStaff(no, name, deptId, gender, pos, phone, role);
                JOptionPane.showMessageDialog(this, ok ? "新增成功（默认密码123456）" : "新增失败（工号重复或部门不存在）");
                load.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "输入不合法");
            }
        });

        btnUpd.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一行"); return; }
            try {
                long staffId = Long.parseLong(model.getValueAt(row, 0).toString());
                String name = tfName.getText().trim();
                long deptId = Long.parseLong(tfDept.getText().trim());
                String gender = tfGender.getText().trim();
                String pos = tfPos.getText().trim();
                String phone = tfPhone.getText().trim();
                boolean ok = dao.updateStaffBasic(staffId, name, deptId, gender, pos, phone);
                JOptionPane.showMessageDialog(this, ok ? "修改成功" : "修改失败");
                load.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "输入不合法");
            }
        });

        btnReset.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一行"); return; }
            long staffId = Long.parseLong(model.getValueAt(row, 0).toString());
            boolean ok = dao.resetPassword(staffId, "123456");
            JOptionPane.showMessageDialog(this, ok ? "密码已重置为123456" : "重置失败");
        });

        btnSetManager.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "请先选中一行"); return; }
            long staffId = Long.parseLong(model.getValueAt(row, 0).toString());
            boolean ok = dao.setRoomAdmin(staffId);
            JOptionPane.showMessageDialog(this, ok ? "已设置为会议室管理员" : "设置失败");
            load.run();
        });

        btnRef.addActionListener(e -> load.run());

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                tfNo.setText(String.valueOf(model.getValueAt(row, 1)));
                tfName.setText(String.valueOf(model.getValueAt(row, 2)));
                tfDept.setText(String.valueOf(model.getValueAt(row, 3)));
                tfGender.setText(String.valueOf(model.getValueAt(row, 5)));
                tfPos.setText(String.valueOf(model.getValueAt(row, 6)));
                tfPhone.setText(String.valueOf(model.getValueAt(row, 7)));
                cbRole.setSelectedItem(String.valueOf(model.getValueAt(row, 8)));
            }
        });

        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        load.run();
        return panel;
    }

    // ===================== 4. 预约记录管理（筛选） =====================
    private JPanel buildReservationQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID","预约号","主题","会议室","开始","结束","状态","申请人"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        JTextField tfDate = new JTextField(10); // yyyy-MM-dd
        JTextField tfDeptId = new JTextField(5);
        JTextField tfRoomId = new JTextField(5);
        JButton btnQuery = new JButton("查询");
        JButton btnReset = new JButton("重置");

        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("日期(yyyy-MM-dd)")); op.add(tfDate);
        op.add(new JLabel("部门ID")); op.add(tfDeptId);
        op.add(new JLabel("会议室ID")); op.add(tfRoomId);
        op.add(btnQuery); op.add(btnReset);

        ReservationQueryDAO dao = new ReservationQueryDAO();

        Runnable loadAll = () -> {
            model.setRowCount(0);
            List<ReservationList> list = dao.queryAll("", 0, 0);
            for (ReservationList r : list) {
                model.addRow(new Object[]{
                        r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                        r.getStartTime(), r.getEndTime(), r.getProcess(), r.getApplicantName()
                });
            }
        };

        btnQuery.addActionListener(e -> {
            model.setRowCount(0);
            String date = tfDate.getText().trim();
            long deptId = 0;
            long roomId = 0;
            try {
                if (!tfDeptId.getText().trim().isEmpty()) deptId = Long.parseLong(tfDeptId.getText().trim());
                if (!tfRoomId.getText().trim().isEmpty()) roomId = Long.parseLong(tfRoomId.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "部门ID/会议室ID必须是数字");
                return;
            }
            List<ReservationList> list = dao.queryAll(date, deptId, roomId);
            for (ReservationList r : list) {
                model.addRow(new Object[]{
                        r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                        r.getStartTime(), r.getEndTime(), r.getProcess(), r.getApplicantName()
                });
            }
        });

        btnReset.addActionListener(e -> {
            tfDate.setText(""); tfDeptId.setText(""); tfRoomId.setText("");
            loadAll.run();
        });

        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadAll.run();
        return panel;
    }

    // ===================== 5. 统计报表 =====================
    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JTextField tfMonth = new JTextField(7); // yyyy-MM
        tfMonth.setText(java.time.LocalDate.now().toString().substring(0, 7));
        JButton btnStat = new JButton("生成报表");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("统计月份(yyyy-MM)"));
        top.add(tfMonth);
        top.add(btnStat);

        DefaultTableModel roomModel = new DefaultTableModel(
                new Object[]{"会议室ID","会议室","已用分钟","使用率(%)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable roomTable = new JTable(roomModel);
        roomTable.setRowHeight(26);

        DefaultTableModel deptModel = new DefaultTableModel(
                new Object[]{"部门ID","部门","会议次数"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable deptTable = new JTable(deptModel);
        deptTable.setRowHeight(26);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(roomTable), new JScrollPane(deptTable));
        split.setDividerLocation(280);

        ReportDAO dao = new ReportDAO();

        btnStat.addActionListener(e -> {
            String month = tfMonth.getText().trim();
            roomModel.setRowCount(0);
            deptModel.setRowCount(0);

            List<RoomUsageStat> roomStats = dao.roomUsageByMonth(month);
            for (RoomUsageStat s : roomStats) {
                roomModel.addRow(new Object[]{s.getRoomId(), s.getRoomName(), s.getUsedMinutes(), s.getUsageRate()});
            }

            List<DeptMeetingStat> deptStats = dao.deptMeetingCountByMonth(month);
            for (DeptMeetingStat s : deptStats) {
                deptModel.addRow(new Object[]{s.getDeptId(), s.getDeptName(), s.getMeetingCount()});
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    // ===================== 6. 修改密码 =====================
    private JPanel buildPasswordPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        JPasswordField oldPwd = new JPasswordField(16);
        JPasswordField newPwd = new JPasswordField(16);
        JPasswordField confirmPwd = new JPasswordField(16);
        JButton btnSave = new JButton("保存修改");

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("旧密码"), c);
        c.gridx = 1; c.gridy = 0; panel.add(oldPwd, c);

        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("新密码"), c);
        c.gridx = 1; c.gridy = 1; panel.add(newPwd, c);

        c.gridx = 0; c.gridy = 2; panel.add(new JLabel("确认新密码"), c);
        c.gridx = 1; c.gridy = 2; panel.add(confirmPwd, c);

        c.gridx = 1; c.gridy = 3; panel.add(btnSave, c);

        StaffDAO dao = new StaffDAO();
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

            boolean ok = dao.changePassword(loginUser.getStaffId(), o, n);
            JOptionPane.showMessageDialog(this, ok ? "修改成功，请重新登录生效" : "修改失败（旧密码错误）");
        });

        return panel;
    }
}