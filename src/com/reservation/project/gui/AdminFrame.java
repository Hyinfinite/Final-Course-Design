package com.reservation.project.gui;

import com.reservation.project.dao.*;
import com.reservation.project.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 系统管理员界面类，继承自JFrame，提供系统管理功能
 * 包括部门管理、会议室管理、行政人员管理、预约记录管理、统计报表和修改密码等功能
 */
public class AdminFrame extends JFrame {
    private final User loginUser; // 当前登录用户对象

    /**
     * 构造函数，初始化管理员界面
     * @param user 当前登录用户对象
     */
    public AdminFrame(User user) {
        this.loginUser = user;

        // 设置窗口标题和基本属性
        setTitle("系统管理员 - " + user.getStaffName());
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 顶部信息标签，显示当前用户信息
        JLabel topInfo = new JLabel("当前用户：" + user.getStaffName() + "（SYS_ADMIN）");
        topInfo.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // 创建选项卡面板，包含各种管理功能
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("部门管理", buildDeptPanel());
        tabs.addTab("会议室管理", buildRoomPanel());
        tabs.addTab("行政人员管理", buildStaffPanel());
        tabs.addTab("预约记录管理", buildReservationQueryPanel());
        tabs.addTab("统计报表", buildReportPanel());
        tabs.addTab("修改密码", buildPasswordPanel());

        // 添加组件到窗口
        add(topInfo, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        setVisible(true);
    }

    // ===================== 1. 部门管理 =====================
    /**
     * 构建部门管理面板
     * 提供部门的增删改查功能，包括：
     * - 显示所有部门列表
     * - 新增部门
     * - 修改部门名称
     * - 删除部门
     * @return 部门管理面板
     */
    private JPanel buildDeptPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 创建表格模型和表格，用于显示部门信息
        // 表格包含ID和部门名称两列，且不可编辑
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "部门名称"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        // 创建输入框和按钮
        JTextField tfName = new JTextField(18);
        JButton btnAdd = new JButton("新增");
        JButton btnUpd = new JButton("修改");
        JButton btnDel = new JButton("删除");
        JButton btnRef = new JButton("刷新");

        // 操作面板，包含输入框和按钮
        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("部门名称"));
        op.add(tfName);
        op.add(btnAdd);
        op.add(btnUpd);
        op.add(btnDel);
        op.add(btnRef);

        // 创建DAO对象，用于数据库操作
        DepartmentDAO dao = new DepartmentDAO();

        // 数据加载任务，从数据库获取所有部门信息并显示在表格中
        Runnable load = () -> {
            model.setRowCount(0);
            List<Department> list = dao.listAll();
            for (Department d : list) model.addRow(new Object[]{d.getDeptId(), d.getDeptName()});
        };

        // 按钮事件监听器
        btnAdd.addActionListener(e -> {
            String name = tfName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "部门名称不能为空");
                return;
            }
            JOptionPane.showMessageDialog(this, dao.addDepartment(name) ? "新增成功" : "新增失败（可能重名）");
            load.run(); // 刷新表格
        });

        btnUpd.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一行");
                return;
            }
            long id = Long.parseLong(model.getValueAt(row, 0).toString());
            String name = tfName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入新部门名称");
                return;
            }
            JOptionPane.showMessageDialog(this, dao.updateDepartment(id, name) ? "修改成功" : "修改失败");
            load.run(); // 刷新表格
        });

        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一行");
                return;
            }
            long id = Long.parseLong(model.getValueAt(row, 0).toString());
            int ok = JOptionPane.showConfirmDialog(this, "确认删除该部门？", "确认", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            JOptionPane.showMessageDialog(this, dao.deleteDepartment(id) ? "删除成功" : "删除失败（可能被人员/预约引用）");
            load.run(); // 刷新表格
        });

        btnRef.addActionListener(e -> load.run()); // 刷新表格

        // 表格选择监听器，当选中一行时，将部门名称显示在输入框中
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                tfName.setText(String.valueOf(model.getValueAt(row, 1)));
            }
        });

        // 添加组件到面板
        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        load.run(); // 初始加载数据
        return panel;
    }

    // ===================== 2. 会议室管理 =====================
    /**
     * 构建会议室管理面板
     * 提供会议室的增删改查功能，包括：
     * - 显示所有会议室列表
     * - 新增会议室
     * - 修改会议室信息
     * - 删除会议室
     * @return 会议室管理面板
     */
    private JPanel buildRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 创建表格模型和表格，用于显示会议室信息
        // 表格包含ID、编号、名称、位置、容量、投影仪、音响等列，且不可编辑
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID","编号","名称","位置","容量","投影仪","音响"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        // 创建输入框和复选框
        JTextField tfCode = new JTextField(8);
        JTextField tfName = new JTextField(10);
        JTextField tfLoc = new JTextField(10);
        JTextField tfCap = new JTextField(6);
        JCheckBox cbProj = new JCheckBox("投影仪");
        JCheckBox cbAudio = new JCheckBox("音响");

        // 创建按钮
        JButton btnAdd = new JButton("新增");
        JButton btnUpd = new JButton("修改");
        JButton btnDel = new JButton("删除");
        JButton btnRef = new JButton("刷新");

        // 操作面板，包含输入框、复选框和按钮
        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("编号")); op.add(tfCode);
        op.add(new JLabel("名称")); op.add(tfName);
        op.add(new JLabel("位置")); op.add(tfLoc);
        op.add(new JLabel("容量")); op.add(tfCap);
        op.add(cbProj); op.add(cbAudio);
        op.add(btnAdd); op.add(btnUpd); op.add(btnDel); op.add(btnRef);

        // 创建DAO对象，用于数据库操作
        MeetingRoomManageDAO dao = new MeetingRoomManageDAO();

        // 数据加载任务，从数据库获取所有会议室信息并显示在表格中
        Runnable load = () -> {
            model.setRowCount(0);
            for (MeetingRoom m : dao.listAll()) {
                model.addRow(new Object[]{
                        m.getRoomId(), m.getRoomCode(), m.getRoomName(), m.getLocation(),
                        m.getCapacity(), m.getHasProjector() == 1 ? "有" : "无", m.getHasAudio() == 1 ? "有" : "无"
                });
            }
        };

        // 为添加按钮添加动作监听器
        btnAdd.addActionListener(e -> {
            try {
                // 获取并去除文本框中的前后空格
                String code = tfCode.getText().trim();    // 获取房间编号
                String name = tfName.getText().trim();    // 获取房间名称
                String loc = tfLoc.getText().trim();      // 获取房间位置
                int cap = Integer.parseInt(tfCap.getText().trim());  // 获取房间容量并转换为整数
                // 验证输入数据的有效性
                if (code.isEmpty() || name.isEmpty() || cap <= 0) {
                    // 如果编号或名称为空，或者容量小于等于0，显示错误提示
                    JOptionPane.showMessageDialog(this, "编号/名称不能为空，容量>0");
                    return;  // 终止执行
                }
                // 调用数据访问对象添加房间信息
                // 检查项目设备和音频设备是否选中，选中则值为1，否则为0
                boolean ok = dao.addRoom(code, name, loc, cap, cbProj.isSelected() ? 1 : 0, cbAudio.isSelected() ? 1 : 0);
                // 根据添加结果显示相应的成功或失败提示
                JOptionPane.showMessageDialog(this, ok ? "新增成功" : "新增失败（编号可能重复）");
                load.run(); // 刷新表格数据
            } catch (Exception ex) {
                // 如果容量输入的不是有效数字，显示错误提示
                JOptionPane.showMessageDialog(this, "容量必须是数字");
            }
        });

        // 为更新按钮添加动作监听器
        btnUpd.addActionListener(e -> {
            // 获取当前选中行的索引
            int row = table.getSelectedRow();
            // 检查是否选中了行
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一行");
                return;
            }
            try {
                // 获取选中行的ID
                long id = Long.parseLong(model.getValueAt(row, 0).toString());

                // 获取并处理输入框中的文本信息
                String code = tfCode.getText().trim();  // 获取编号并去除前后空格
                String name = tfName.getText().trim();  // 获取名称并去除前后空格
                String loc = tfLoc.getText().trim();    // 获取位置并去除前后空格
                int cap = Integer.parseInt(tfCap.getText().trim());  // 获取容量并转换为整数
                // 更新房间信息，包括编号、名称、位置、容量以及是否配备投影设备和音响设备
                boolean ok = dao.updateRoom(id, code, name, loc, cap, cbProj.isSelected() ? 1 : 0, cbAudio.isSelected() ? 1 : 0);
                // 显示操作结果
                JOptionPane.showMessageDialog(this, ok ? "修改成功" : "修改失败");
                load.run(); // 刷新表格数据
            } catch (Exception ex) {
                // 捕获异常并提示用户输入不合法
                JOptionPane.showMessageDialog(this, "输入不合法");
            }
        });

        // 删除按钮的事件监听器
        btnDel.addActionListener(e -> {
            // 获取当前选中的行索引
            int row = table.getSelectedRow();
            // 如果没有选中任何行，提示用户并返回
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一行");
                return;
            }
            // 获取选中行的第一列值（ID），并转换为Long类型
            long id = Long.parseLong(model.getValueAt(row, 0).toString());
            // 弹出确认对话框，让用户确认是否删除
            int ok = JOptionPane.showConfirmDialog(this, "确认删除该会议室？", "确认", JOptionPane.YES_NO_OPTION);
            // 如果用户点击了"否"，则直接返回
            if (ok != JOptionPane.YES_OPTION) {
                return;
            }
            // 执行删除操作，并根据结果显示成功或失败信息
            // 删除失败可能是因为该会议室被预约引用了
            JOptionPane.showMessageDialog(this, dao.deleteRoom(id) ? "删除成功" : "删除失败（可能被预约引用）");
            load.run(); // 刷新表格
        });

        btnRef.addActionListener(e -> load.run()); // 刷新表格

        // 表格选择监听器，当选中一行时，将会议室信息显示在输入框和复选框中
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

        // 添加组件到面板
        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        load.run(); // 初始加载数据
        return panel;
    }

    // ===================== 3. 行政人员管理 =====================
    /**
     * 构建行政人员管理面板
     * 提供行政人员的增删改查功能，包括：
     * - 显示所有行政人员列表
     * - 新增行政人员
     * - 修改行政人员信息
     * - 重置密码
     * - 设置会议室管理员权限
     * @return 行政人员管理面板
     */
    private JPanel buildStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 创建表格模型和表格，用于显示行政人员信息
        // 表格包含ID、工号、姓名、部门ID、部门、性别、职务、电话、角色等列，且不可编辑
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID","工号","姓名","部门ID","部门","性别","职务","电话","角色"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        // 创建输入框和下拉框
        JTextField tfNo = new JTextField(8);
        JTextField tfName = new JTextField(8);
        JTextField tfDept = new JTextField(5);
        JTextField tfGender = new JTextField(4);
        JTextField tfPos = new JTextField(8);
        JTextField tfPhone = new JTextField(10);
        JComboBox<String> cbRole = new JComboBox<String>(new String[]{"STAFF","ROOM_ADMIN","SYS_ADMIN"});

        // 创建按钮
        JButton btnAdd = new JButton("新增人员");
        JButton btnUpd = new JButton("修改信息");
        JButton btnReset = new JButton("重置密码123456");
        JButton btnSetManager = new JButton("设为会议室管理员");
        JButton btnRef = new JButton("刷新");

        // 操作面板，包含输入框、下拉框和按钮
        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("工号")); op.add(tfNo);
        op.add(new JLabel("姓名")); op.add(tfName);
        op.add(new JLabel("部门ID")); op.add(tfDept);
        op.add(new JLabel("性别")); op.add(tfGender);
        op.add(new JLabel("职务")); op.add(tfPos);
        op.add(new JLabel("电话")); op.add(tfPhone);
        op.add(new JLabel("角色")); op.add(cbRole);
        op.add(btnAdd); op.add(btnUpd); op.add(btnReset); op.add(btnSetManager); op.add(btnRef);

        // 创建DAO对象，用于数据库操作
        StaffDAO dao = new StaffDAO();

        // 数据加载任务，从数据库获取所有行政人员信息并显示在表格中
        Runnable load = () -> {
            model.setRowCount(0);
            for (StaffInfo s : dao.listAll()) {
                model.addRow(new Object[]{
                        s.getStaffId(), s.getStaffNo(), s.getStaffName(), s.getDeptId(), s.getDeptName(),
                        s.getGender(), s.getPosition(), s.getPhone(), s.getAccessLevel()
                });
            }
        };

        // 为添加按钮添加动作监听器
        btnAdd.addActionListener(e -> {
            try {
                // 获取并清理输入框中的文本
                String no = tfNo.getText().trim();      // 获取工号
                String name = tfName.getText().trim();  // 获取姓名
                long deptId = Long.parseLong(tfDept.getText().trim());  // 获取并转换部门ID
                String gender = tfGender.getText().trim();  // 获取性别
                String pos = tfPos.getText().trim();    // 获取职位
                String phone = tfPhone.getText().trim(); // 获取电话
                String role = String.valueOf(cbRole.getSelectedItem());  // 获取角色选择

                // 验证工号和姓名是否为空
                if (no.isEmpty() || name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "工号和姓名不能为空");
                    return;
                }
                // 尝试添加员工信息
                boolean ok = dao.addStaff(no, name, deptId, gender, pos, phone, role);
                // 根据添加结果显示相应的提示信息
                JOptionPane.showMessageDialog(this, ok ? "新增成功（默认密码123456）" : "新增失败（工号重复或部门不存在）");
                load.run(); // 刷新表格数据
            } catch (Exception ex) {
                // 捕获并处理输入异常
                JOptionPane.showMessageDialog(this, "输入不合法");
            }
        });

        // 为更新按钮添加动作监听器
        btnUpd.addActionListener(e -> {
            // 获取当前选中的表格行索引
            int row = table.getSelectedRow();
            // 如果没有选中任何行，提示用户并返回
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一行");
                return;
            }
            try {
                // 获取选中行第一列的值作为员工ID
                long staffId = Long.parseLong(model.getValueAt(row, 0).toString());
                // 获取并处理各个输入框的文本内容
                String no = tfNo.getText().trim();      // 员工编号
                String name = tfName.getText().trim();  // 员工姓名
                long deptId = Long.parseLong(tfDept.getText().trim()); // 部门ID
                String gender = tfGender.getText().trim(); // 性别
                String pos = tfPos.getText().trim();     // 职位
                String phone = tfPhone.getText().trim(); // 电话
                // 调用DAO方法更新员工基本信息
                boolean ok = dao.updateStaffBasic(staffId, no, name, deptId, gender, pos, phone);
                // 显示操作结果提示
                JOptionPane.showMessageDialog(this, ok ? "修改成功" : "修改失败");
                load.run(); // 刷新表格
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "输入不合法");
            }
        });

        // 重置按钮的事件监听器
        btnReset.addActionListener(e -> {
            int row = table.getSelectedRow(); // 获取当前选中的行索引
            if (row < 0) { // 如果没有选中任何行
                JOptionPane.showMessageDialog(this, "请先选中一行"); // 提示用户先选中一行
                return; // 提前返回，不执行后续操作
            }
            long staffId = Long.parseLong(model.getValueAt(row, 0).toString()); // 获取选中行的第一列值并转换为长整型
            boolean ok = dao.resetPassword(staffId, "123456"); // 调用DAO方法重置密码为"123456"
            // 显示重置结果提示信息
            JOptionPane.showMessageDialog(this, ok ? "密码已重置为123456" : "重置失败");
        });

        // 设置管理员按钮的事件监听器
        btnSetManager.addActionListener(e -> {
            int row = table.getSelectedRow(); // 获取当前选中的行索引
            if (row < 0) { // 如果没有选中任何行
                JOptionPane.showMessageDialog(this, "请先选中一行"); // 提示用户先选中一行
                return; // 提前返回，不执行后续操作
            }
            long staffId = Long.parseLong(model.getValueAt(row, 0).toString()); // 获取选中行的第一列值并转换为长整型
            boolean ok = dao.setRoomAdmin(staffId); // 调用DAO方法设置会议室管理员
            // 显示设置结果提示信息
            JOptionPane.showMessageDialog(this, ok ? "已设置为会议室管理员" : "设置失败");
            load.run(); // 刷新表格数据
        });

        btnRef.addActionListener(e -> load.run()); // 刷新表格

        // 表格选择监听器，当选中一行时，将行政人员信息显示在输入框和下拉框中
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

        // 添加组件到面板
        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        load.run(); // 初始加载数据
        return panel;
    }

    // ===================== 4. 预约记录管理（筛选） =====================
    /**
     * 构建预约记录查询面板
     * 提供预约记录的查询功能，支持按日期、部门ID和会议室ID进行筛选
     * @return 预约记录查询面板
     */
    private JPanel buildReservationQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 创建表格模型和表格，用于显示预约记录信息
        // 表格包含ID、预约号、主题、会议室、开始、结束、状态、申请人等列，且不可编辑
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID","预约号","主题","会议室","开始","结束","参会人数","状态","申请人","审批意见"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(28);

        // 创建输入框和按钮
        JTextField tfDate = new JTextField(10); // yyyy-MM-dd
        JTextField tfDeptId = new JTextField(5);
        JTextField tfRoomId = new JTextField(5);
        JButton btnQuery = new JButton("查询");
        JButton btnReset = new JButton("重置");

        // 操作面板，包含输入框和按钮
        JPanel op = new JPanel(new FlowLayout(FlowLayout.LEFT));
        op.add(new JLabel("日期(yyyy-MM-dd)")); op.add(tfDate);
        op.add(new JLabel("部门ID")); op.add(tfDeptId);
        op.add(new JLabel("会议室ID")); op.add(tfRoomId);
        op.add(btnQuery); op.add(btnReset);

        // 创建DAO对象，用于数据库操作
        ReservationQueryDAO dao = new ReservationQueryDAO();

        // 数据加载任务，从数据库获取所有预约记录并显示在表格中
        // 使用Lambda表达式创建一个Runnable任务，用于加载所有预约数据
        Runnable loadAll = () -> {
            // 清空表格模型中的所有行，为加载数据做准备
            model.setRowCount(0);
            // 从数据访问对象(DAO)查询所有预约记录，不进行分页
            List<ReservationList> list = dao.queryAll("", 0, 0);
            // 遍历查询到的预约列表
            for (ReservationList r : list) {
                // 将每个预约记录的信息添加到表格模型中
                model.addRow(new Object[]{
                        r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                        r.getStartTime(), r.getEndTime(), r.getParticipantCount(), r.getProcess(), r.getApplicantName(), r.getComment()
                });
            }
        };

        // 查询按钮事件监听器
        // 添加查询按钮的事件监听器
        btnQuery.addActionListener(e -> {
            // 清空表格数据模型中的所有行
            model.setRowCount(0);
            // 获取日期输入框的文本并去除前后空格
            String date = tfDate.getText().trim();
            // 初始化部门ID和会议室ID为0
            long deptId = 0;
            long roomId = 0;
            try {
                // 如果部门ID输入框不为空，则将其转换为长整型
                if (!tfDeptId.getText().trim().isEmpty()) {
                    deptId = Long.parseLong(tfDeptId.getText().trim());
                }
                // 如果会议室ID输入框不为空，则将其转换为长整型
                if (!tfRoomId.getText().trim().isEmpty()) {
                    roomId = Long.parseLong(tfRoomId.getText().trim());
                }
            } catch (Exception ex) {
                // 如果转换失败，弹出错误提示对话框
                JOptionPane.showMessageDialog(this, "部门ID/会议室ID必须是数字");
                return; // 终止当前操作
            }
            // 根据条件查询所有预约记录
            List<ReservationList> list = dao.queryAll(date, deptId, roomId);
            // 遍历查询结果，将每条记录添加到表格模型中
            for (ReservationList r : list) {
                model.addRow(new Object[]{
                        r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(), r.getRoomName(),
                        r.getStartTime(), r.getEndTime(), r.getParticipantCount(), r.getProcess(),
                        r.getApplicantName(), r.getComment()
                });
            }
        });

        // 重置按钮事件监听器，清空输入框并加载所有预约记录
        btnReset.addActionListener(e -> {
            tfDate.setText("");
            tfDeptId.setText("");
            tfRoomId.setText("");
            loadAll.run();
        });

        // 添加组件到面板
        panel.add(op, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        loadAll.run(); // 初始加载数据
        return panel;
    }

    // ===================== 5. 统计报表 =====================
    /**
     * 构建统计报表面板
     * 提供会议室使用情况和部门会议次数的统计功能
     * @return 统计报表面板
     */
    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 创建输入框和按钮，用于选择统计月份和生成报表
        JTextField tfMonth = new JTextField(7); // yyyy-MM
        tfMonth.setText(java.time.LocalDate.now().toString().substring(0, 7));
        JButton btnStat = new JButton("生成报表");

        // 顶部面板，包含月份输入框和生成按钮
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("统计月份(yyyy-MM)"));
        top.add(tfMonth);
        top.add(btnStat);

        // 创建会议室使用情况表格模型和表格
        DefaultTableModel roomModel = new DefaultTableModel(
                new Object[]{"会议室ID","会议室","已用次数","使用率(%)"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable roomTable = new JTable(roomModel);
        roomTable.setRowHeight(26);

        // 创建部门会议次数表格模型和表格
        DefaultTableModel deptModel = new DefaultTableModel(
                new Object[]{"部门ID","部门","会议次数"}, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable deptTable = new JTable(deptModel);
        deptTable.setRowHeight(26);

        // 创建分割面板，将会议室使用情况和部门会议次数上下排列
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(roomTable), new JScrollPane(deptTable));
        split.setDividerLocation(280);

        // 创建DAO对象，用于数据库操作
        ReportDAO dao = new ReportDAO();

        // 生成报表按钮事件监听器
        btnStat.addActionListener(e -> {
            String month = tfMonth.getText().trim();
            roomModel.setRowCount(0);
            deptModel.setRowCount(0);

            // 获取并显示会议室使用情况统计
            List<RoomUsageStat> roomStats = dao.roomUsageByMonth(month);
            for (RoomUsageStat s : roomStats) {
                roomModel.addRow(new Object[]{s.getRoomId(), s.getRoomName(), s.getUsedCount(), s.getUsageRate()});
            }

            // 获取并显示部门会议次数统计
            List<DeptMeetingStat> deptStats = dao.deptMeetingCountByMonth(month);
            for (DeptMeetingStat s : deptStats) {
                deptModel.addRow(new Object[]{s.getDeptId(), s.getDeptName(), s.getMeetingCount()});
            }
        });

        // 添加组件到面板
        panel.add(top, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    // ===================== 6. 修改密码 =====================
    /**
     * 构建修改密码面板
     * 提供修改当前用户密码的功能
     * 使用GridLayout布局，包含旧密码、新密码、确认新密码输入框和保存按钮
     * @return 修改密码面板
     */
    private JPanel buildPasswordPanel() {
        // 使用FlowLayout布局，设置水平和垂直间距为5像素
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        // 创建密码输入框和按钮
        JPasswordField oldPwd = new JPasswordField(16);  // 旧密码输入框，设置长度为16
        JPasswordField newPwd = new JPasswordField(16);  // 新密码输入框，设置长度为16
        JPasswordField confirmPwd = new JPasswordField(16);  // 确认密码输入框，设置长度为16
        JButton btnSave = new JButton("保存修改");  // 保存修改按钮

        // 创建水平面板，用于排列标签和输入框
        JPanel oldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));  // 旧密码面板
        oldPanel.add(new JLabel("旧密码"));  // 添加旧密码标签
        oldPanel.add(oldPwd);  // 添加旧密码输入框

        JPanel newPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));  // 新密码面板
        newPanel.add(new JLabel("新密码"));  // 添加新密码标签
        newPanel.add(newPwd);  // 添加新密码输入框

        JPanel confirmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));  // 确认密码面板
        confirmPanel.add(new JLabel("确认新密码"));  // 添加确认密码标签
        confirmPanel.add(confirmPwd);  // 添加确认密码输入框

        // 创建按钮面板，用于居中显示按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));  // 按钮面板
        btnPanel.add(btnSave);  // 添加保存按钮

        // 将所有组件添加到主面板
        panel.add(oldPanel);  // 添加旧密码面板
        panel.add(newPanel);  // 添加新密码面板
        panel.add(confirmPanel);  // 添加确认密码面板
        panel.add(btnPanel);  // 添加按钮面板

        // 创建DAO对象，用于数据库操作
        StaffDAO dao = new StaffDAO();

        // 保存修改按钮事件监听器
        btnSave.addActionListener(e -> {
            String o = new String(oldPwd.getPassword()).trim();  // 获取旧密码并去除前后空格
            String n = new String(newPwd.getPassword()).trim();  // 获取新密码并去除前后空格
            String cfm = new String(confirmPwd.getPassword()).trim();  // 获取确认密码并去除前后空格

            // 验证密码是否为空
            if (o.isEmpty() || n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "密码不能为空");  // 显示错误提示
                return;
            }
            // 验证两次输入的新密码是否一致
            if (!n.equals(cfm)) {
                JOptionPane.showMessageDialog(this, "两次新密码不一致");  // 显示错误提示
                return;
            }

            // 调用DAO层方法更新密码，并显示结果
            boolean ok = dao.changePassword(loginUser.getStaffId(), o, n);  // 执行密码修改
            JOptionPane.showMessageDialog(this, ok ? "修改成功，请重新登录生效" : "修改失败（旧密码错误）");  // 显示操作结果
        });

        return panel;  // 返回构建好的密码修改面板
    }
}