package com.reservation.project.gui;

import com.reservation.project.dao.*;
import com.reservation.project.model.Participant;
import com.reservation.project.model.ReservationList;
import com.reservation.project.model.StaffInfo;
import com.reservation.project.model.User;
import com.reservation.project.util.DateTimeUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * StaffFrame类 - 行政人员界面
 * 继承自JFrame，为行政人员提供会议室预约管理功能
 */
public class StaffFrame extends JFrame {
    private final User user; // 当前登录用户信息

    // 提交预约组件
    private JComboBox<String> cbRoom = new JComboBox<>();
    private JTextField tfTopic = new JTextField(18);
    private JTextField tfDesc = new JTextField(18);
    private TimePanel timePicker = new TimePanel();

    // 参会人员勾选列表
    private JPanel checkboxPanel = new JPanel(new GridLayout(0, 2, 5, 2));
    private java.util.List<JCheckBox> staffCheckBoxes = new java.util.ArrayList<>();
    private JLabel lblSelectedCount = new JLabel("已选 0 人");
    private JLabel lblTotalCount = new JLabel("总人数：0");


    // 我的预约表格
    private DefaultTableModel myModel = new DefaultTableModel(
            new Object[]{"ID","预约号","主题","会议室","开始","结束","参会人数","状态","审批意见"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    // 空闲查询表格
    private DefaultTableModel freeModel = new DefaultTableModel(
            new Object[]{"会议室ID","会议室","位置","容量","状态"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    // 签到管理表格
    private DefaultTableModel signModel = new DefaultTableModel(
            new Object[]{"记录ID","预约号","主题","开始时间","结束时间","参会人工号","参会人","签到状态","签到时间"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    /**
     * 行政人员界面构造函数
     * @param user 当前登录的用户对象
     */
    public StaffFrame(User user) {
        this.user = user;  // 保存用户对象

        // 设置窗口标题、大小、位置和关闭操作
        setTitle("行政人员 - " + user.getStaffName());
        setSize(1220, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8,8));  // 使用边界布局，组件间距为8像素

        // 创建顶部标签，显示当前用户信息
        JLabel head = new JLabel("当前用户：" + user.getStaffName() + "（STAFF）");
        head.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));  // 设置边距

        // 创建选项卡面板，包含四个功能选项卡
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("提交预约", buildSubmitPanel());      // 提交预约选项卡
        tabs.addTab("我的预约", buildMyPanel());        // 我的预约选项卡
        tabs.addTab("会议室空闲查询", buildFreePanel());  // 会议室空闲查询选项卡
        tabs.addTab("签到管理", buildSignPanel());      // 签到管理选项卡

        // 创建底部面板，包含个人信息按钮
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnProfile = new JButton("个人信息/修改密码");
        bottom.add(btnProfile);  // 将按钮添加到底部面板



        // 将各组件添加到窗口中
        add(head, BorderLayout.NORTH);    // 顶部标签
        add(tabs, BorderLayout.CENTER);    // 选项卡面板
        add(bottom, BorderLayout.SOUTH);   // 底部按钮面板

        // 为个人信息按钮添加点击事件，打开个人信息对话框
        btnProfile.addActionListener(e -> new ProfileDialog(this, user).setVisible(true));



        // 加载必要的数据
        loadRoomOptions();              // 加载会议室选项
        refreshStaffCheckList();          // 加载本部门员工列表
        loadMyReservations();           // 加载当前用户的预约记录
        setVisible(true);               // 显示窗口
    }

    // -------------------- 提交预约面板 --------------------
    /**
     * 构建提交预约的面板
     * @return 配置好的提交面板
     */
    private JPanel buildSubmitPanel() {
        // 创建主面板，使用边界布局，设置组件间的水平和垂直间距为8像素
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 表单区域 - 使用 FlowLayout 确保标签和输入框在同一行
        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));

        // 第一行：会议主题
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row1.add(new JLabel("会议主题"));
        row1.add(tfTopic);
        form.add(row1);

        // 第二行：会议室
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row2.add(new JLabel("会议室"));
        row2.add(cbRoom);
        form.add(row2);

        // 第三行：会议说明
        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row3.add(new JLabel("会议说明"));
        row3.add(tfDesc);
        form.add(row3);

        // 第四行：会议时间
        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        row4.add(new JLabel("会议时间"));
        row4.add(timePicker);
        form.add(row4);

        // 参会人员选择区域 - 保持不变
        JPanel staffPanel = new JPanel(new BorderLayout(5, 5));
        staffPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // 在提交预约面板的参会人员选择区域添加人数统计
        JLabel lblTotalCount = new JLabel("总人数：0");
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSelectAll = new JButton("全选");
        JButton btnDeselectAll = new JButton("取消全选");
        controlPanel.add(btnSelectAll);
        controlPanel.add(btnDeselectAll);
        controlPanel.add(lblSelectedCount);
        controlPanel.add(lblTotalCount);

        JScrollPane scrollStaff = new JScrollPane(checkboxPanel);
        scrollStaff.setPreferredSize(new Dimension(400, 100));

        staffPanel.add(controlPanel, BorderLayout.NORTH);
        staffPanel.add(scrollStaff, BorderLayout.CENTER);

        // 操作按钮区域
        JButton btnSubmit = new JButton("提交预约");
        JButton btnReloadRoom = new JButton("刷新会议室");
        JButton btnReloadStaff = new JButton("刷新人员");
        JPanel op = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        op.add(btnReloadRoom);
        op.add(btnReloadStaff);
        op.add(btnSubmit);

        // 事件监听

        // btnReloadRoom按钮点击时调用loadRoomOptions方法加载房间选项
        btnReloadRoom.addActionListener(e -> loadRoomOptions());
        // btnReloadStaff按钮点击时调用refreshStaffCheckList方法刷新员工清单
        btnReloadStaff.addActionListener(e -> refreshStaffCheckList());
        // btnSubmit按钮点击时调用addReservation方法添加预订
        btnSubmit.addActionListener(e -> addReservation());

        // 为全选和取消全选按钮添加事件监听器
        // btnSelectAll按钮点击时，将所有员工复选框设置为选中状态，并更新选中计数
        btnSelectAll.addActionListener(e -> {
            for (JCheckBox cb : staffCheckBoxes) {
                cb.setSelected(true);
            }
            updateSelectedCount();
        });

        // btnDeselectAll按钮点击时，将所有员工复选框设置为未选中状态，并更新选中计数
        btnDeselectAll.addActionListener(e -> {
            for (JCheckBox cb : staffCheckBoxes) {
                cb.setSelected(false);
            }
            updateSelectedCount();
        });

        // 将各个面板添加到主面板中，并使用边界布局(BorderLayout)进行排列
        panel.add(form, BorderLayout.NORTH);
        panel.add(staffPanel, BorderLayout.CENTER);
        panel.add(op, BorderLayout.SOUTH);
        // 返回组装完成的主面板
        return panel;
    }



    /**
     * 刷新员工复选框列表
     * 该方法会清空现有的复选框列表，然后从数据库中获取当前部门的员工信息，
     * 为每个员工创建一个复选框，并添加到界面上
     */
    private void refreshStaffCheckList() {
        // 清空员工复选框列表和界面上的复选框面板
        staffCheckBoxes.clear();
        checkboxPanel.removeAll();
        // 创建员工数据访问对象，获取当前部门的员工列表
        StaffDAO dao = new StaffDAO();
        List<StaffInfo> staffList = dao.getStaffByDept(user.getDeptId());
        // 遍历员工列表，为每个员工创建复选框
        for (StaffInfo s : staffList) {
            // 创建复选框，显示员工姓名和工号
            JCheckBox cb = new JCheckBox(s.getStaffName() + " (" + s.getStaffNo() + ")");
            // 将员工ID存储在复选框的客户端属性中，方便后续获取
            cb.putClientProperty("staffId", s.getStaffId());
            // 添加动作监听器，当复选框状态改变时更新选中数量
            cb.addActionListener(e -> updateSelectedCount());
            // 将复选框添加到列表和面板中
            staffCheckBoxes.add(cb);
            checkboxPanel.add(cb);
            // 如果是当前登录员工，则默认选中该复选框
            if (s.getStaffId() == user.getStaffId()) {
                cb.setSelected(true);
            }
        }
        // 重新验证和重绘画布，确保界面更新
        checkboxPanel.revalidate();
        checkboxPanel.repaint();
        // 更新已选择的员工数量显示
        updateSelectedCount();
    }

    // 更新已选人数显示
    /**
     * 更新已选人数和总人数的显示
     * 该方法会遍历所有员工复选框，统计被选中的数量，并更新界面上的标签显示
     */
    private void updateSelectedCount() {
        // 计算已选人数
        long count = staffCheckBoxes.stream().filter(JCheckBox::isSelected).count();
        // 更新已选人数标签
        lblSelectedCount.setText("已选 " + count + " 人");
        // 更新总人数标签
        lblTotalCount.setText("总人数：" + staffCheckBoxes.size());
    }

    // -------------------- 我的预约面板 --------------------
    /**
     * 构建用户界面面板
     * @return 配置好的JPanel面板，包含操作按钮和表格
     */
    private JPanel buildMyPanel() {
        // 创建使用BorderLayout布局的面板，组件间水平和垂直间距为8像素
        JPanel panel = new JPanel(new BorderLayout(8,8));
        // 创建基于myModel数据模型的表格，并设置行高为28像素
        JTable table = new JTable(myModel);
        table.setRowHeight(28);

        // 创建"刷新"和"撤销选中预约"按钮
        JButton btnRefresh = new JButton("刷新");
        JButton btnCancel = new JButton("撤销选中预约");
        // 创建使用右对齐FlowLayout布局的操作面板
        JPanel op = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // 将按钮添加到操作面板
        op.add(btnRefresh);
        op.add(btnCancel);

        // 为刷新按钮添加动作监听器，点击时加载用户预约信息
        btnRefresh.addActionListener(e -> loadMyReservations());
        // 为撤销按钮添加动作监听器
        btnCancel.addActionListener(e -> {
            // 获取用户选中的表格行
            int row = table.getSelectedRow();
            // 如果没有选中任何行，提示用户并返回
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一条记录");
                return;
            }
            // 获取选中行的预约ID
            long reservationId = Long.parseLong(myModel.getValueAt(row, 0).toString());
            // 调用ReservationDAO取消预约，传入预约ID和员工ID
            boolean ok = new ReservationDAO().cancelReservation(reservationId, user.getStaffId());
            // 显示操作结果提示
            JOptionPane.showMessageDialog(this, ok ? "撤销成功" : "撤销失败（仅待确认可撤销）");
            // 重新加载预约信息
            loadMyReservations();
        });

        // 将操作面板添加到面板的北部（顶部）
        panel.add(op, BorderLayout.NORTH);
        // 将表格添加到面板的中央，并添加滚动条支持
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // -------------------- 空闲查询面板 --------------------
    /**
     * 构建一个显示空闲房间的面板
     * @return JPanel 配置好的空闲房间面板
     */
    private JPanel buildFreePanel() {
        // 创建使用BorderLayout布局的面板，设置水平和垂直间距为8
        JPanel panel = new JPanel(new BorderLayout(8,8));
        // 创建表格并设置数据模型，同时设置行高为28像素
        JTable table = new JTable(freeModel);
        table.setRowHeight(28);

        // 创建日期输入框，并设置为当前日期
        JTextField tfDate = new JTextField(10);
        tfDate.setText(java.time.LocalDate.now().toString());

        // 创建查询按钮
        JButton btnQuery = new JButton("按日期查询");
        // 创建顶部面板，使用FlowLayout左对齐布局
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // 向顶部面板添加日期标签、输入框和查询按钮
        top.add(new JLabel("日期(yyyy-MM-dd)"));
        top.add(tfDate);
        top.add(btnQuery);

        // 为查询按钮添加事件监听器
        btnQuery.addActionListener(e -> {
            // 清空表格数据
            freeModel.setRowCount(0);
            // 调用DAO查询指定日期的空闲房间信息
            List<String[]> list = new RoomAvailabilityDAO().queryByDate(tfDate.getText().trim());
            // 将查询结果逐行添加到表格模型中
            for (String[] x : list) {
                freeModel.addRow(x);
            }
        });

        // 将顶部面板添加到主面板的北部，将带滚动条的表格添加到中心区域
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // -------------------- 签到管理面板 --------------------
    /**
     * 构建签到面板
     * @return 返回一个包含会议签到相关控件的JPanel面板
     */
    private JPanel buildSignPanel() {
        // 创建使用BorderLayout布局的面板，设置组件之间的水平和垂直间距为8像素
        JPanel panel = new JPanel(new BorderLayout(8,8));

        // 创建使用FlowLayout布局的面板，设置左对齐和组件间距
        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        // 创建下拉框用于选择会议
        JComboBox<String> cbReservation = new JComboBox<>();

        // 创建各个功能按钮
        JButton btnLoadParticipants = new JButton("加载参会人员");
        JButton btnSignDepartment = new JButton("部门签到");
        JButton btnRefresh = new JButton("刷新会议列表");
        JButton btnSignSelected = new JButton("签到选中记录");

        // 向查询面板添加组件
        queryPanel.add(new JLabel("选择会议"));
        queryPanel.add(cbReservation);
        queryPanel.add(btnLoadParticipants);
        queryPanel.add(btnSignDepartment);
        queryPanel.add(btnRefresh);
        queryPanel.add(btnSignSelected);

        // 创建表格显示参会人员信息，并设置行高
        JTable table = new JTable(signModel);
        table.setRowHeight(28);

        // 加载指定部门的所有已确认会议
        Runnable loadReservations = () -> {
            cbReservation.removeAllItems();
            signModel.setRowCount(0);  // 清空表格数据
            List<ReservationList> list = new ReservationDAO().searchConfirmedReservationsByDept(user.getDeptId());  // 获取部门已开始会议列表
            if (list.isEmpty()) {
                cbReservation.addItem("暂无已开始会议");  // 如果没有会议，则显示"暂无已开始会议"
            } else {
                for (ReservationList r : list) {
                    String display = r.getReservationId() + " - " + r.getMeetingTopic()
                            + " (" + r.getStartTime() + ")";  // 将会议信息格式化后添加到下拉框中（会议ID - 会议主题 (开始时间)）
                    cbReservation.addItem(display);
                }
            }
        };


        // 为加载参会人员按钮添加动作监听器
        btnLoadParticipants.addActionListener(e -> {
            // 清空表格数据模型中的所有行
            signModel.setRowCount(0);
            // 获取当前选中的会议预订项
            String selected = (String) cbReservation.getSelectedItem();
            // 检查是否选择了有效的会议
            if (selected == null || selected.startsWith("暂无")) {
                // 如果没有选择有效会议，显示提示信息并返回
                JOptionPane.showMessageDialog(this, "请先选择有效的会议");
                return;
            }
            try {
                // 从选中的会议项中提取会议ID
                long rid = Long.parseLong(selected.split(" - ")[0]);
                // 通过会议ID获取参会人员列表
                List<Participant> list = new ParticipantDAO().listParticipantsByReservation(rid);
                // 检查参会人员列表是否为空
                if (list.isEmpty()) {
                    // 如果没有参会人员，显示提示信息
                    JOptionPane.showMessageDialog(this, "该会议暂无参会人员");
                }
                // 遍历参会人员列表，将每个参会人员的信息添加到表格模型中
                for (Participant p : list) {
                    signModel.addRow(new Object[]{
                            p.getParticipantId(),           // 参会人员ID
                            p.getReservationNo(),           // 预订号
                            p.getMeetingTopic(),            // 会议主题
                            p.getStartTime(),               // 开始时间
                            p.getEndTime(),                 // 结束时间
                            p.getParticipantStaffNo(),      // 参会员工号
                            p.getParticipantName(),         // 参会人员姓名
                            p.getSignInProcess(),           // 签到流程
                            p.getSignInTime()               // 签到时间
                    });
                }
            } catch (Exception ex) {
                // 处理可能出现的异常，并显示错误信息
                JOptionPane.showMessageDialog(this, "加载失败：" + ex.getMessage());
            }
        });


        // 为部门签到按钮添加动作监听器
        btnSignDepartment.addActionListener(e -> {
            // 获取当前选中的会议项
            String selected = (String) cbReservation.getSelectedItem();
            // 检查是否选中了有效会议
            if (selected == null || selected.startsWith("暂无")) {
                JOptionPane.showMessageDialog(this, "请先选择有效的会议");
                return;
            }
            // 从选中项中解析出会议ID
            long rid = Long.parseLong(selected.split(" - ")[0]);

            // 检查当前用户是否为会议申请人
            if (!new ReservationDAO().isReservationApplicant(rid, user.getStaffId())) {
                JOptionPane.showMessageDialog(this, "只有会议申请人可以进行部门签到");
                return;
            }

            // 执行部门签到操作，获取签到人数
            int count = new ParticipantDAO().signInAllParticipants(rid);
            // 显示签到成功提示信息
            JOptionPane.showMessageDialog(this, "部门签到成功，已签到 " + count + " 人");
            // 刷新参会人员列表
            btnLoadParticipants.doClick();
        });


        // 为签到按钮添加动作监听器
        btnSignSelected.addActionListener(e -> {
            // 获取当前选中的表格行索引
            int row = table.getSelectedRow();
            // 如果没有选中任何行，提示用户并返回
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选中一条记录");
                return;
            }
            // 获取选中行的第一列（ID列）的值
            Object idObj = signModel.getValueAt(row, 0);
            // 检查ID是否为空或为空字符串
            if (idObj == null || idObj.toString().isEmpty()) {
                JOptionPane.showMessageDialog(this, "所选记录无效，请重新加载参会人员");
                return;
            }
            // 将ID转换为长整型
            long participantId = Long.parseLong(idObj.toString());
            // 调用DAO方法执行签到操作，传入参会者ID和当前用户ID
            boolean ok = new ParticipantDAO().signIn(participantId, user.getStaffId());
            // 根据签到结果显示相应消息
            JOptionPane.showMessageDialog(this, ok ? "签到成功" : "签到失败（可能已签到、会议未开始或无权限）");
            // 重新加载参会人员列表
            btnLoadParticipants.doClick();
        });


        // 为刷新按钮添加动作监听器
        btnRefresh.addActionListener(e -> loadReservations.run());
        loadReservations.run();  // 加载预订信息

        panel.add(queryPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // -------------------- 辅助方法 --------------------
    /**
     * 加载会议室选项的下拉列表
     * 该方法会清除现有的所有选项，然后从数据库中获取最新的会议室列表并添加到下拉框中
     */
    private void loadRoomOptions() {
        // 清空下拉框中的所有现有选项
        cbRoom.removeAllItems();
        // 通过MeetingRoomDAO从数据库获取所有可用的会议室选项列表
        List<String> opts = new MeetingRoomDAO().roomOption();
        // 遍历列表中的每个选项，并添加到下拉框中
        for (String s : opts) {
            cbRoom.addItem(s);
        }
    }

    /**
     * 添加会议室预约的方法
     * 该方法负责收集用户输入的预约信息，进行多项校验，然后提交预约数据
     */
    private void addReservation() {
        try {
            // 1. 解析会议室信息
            // 从下拉框中获取选中的会议室文本
            String roomText = (String) cbRoom.getSelectedItem();
            // 检查是否选择了会议室
            if (roomText == null || roomText.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先选择会议室");
                return;
            }
            // 使用正则表达式提取会议室ID
            Matcher matcher = Pattern.compile("^(\\d+)").matcher(roomText.trim());
            if (!matcher.find()) {
                JOptionPane.showMessageDialog(this, "会议室格式异常，请刷新后重试");
                return;
            }
            // 将提取的会议室ID转换为长整型
            long roomId = Long.parseLong(matcher.group(1));

            // 2. 解析时间
            Timestamp start = DateTimeUtil.getTime(timePicker.getStartText());
            Timestamp end = DateTimeUtil.getTime(timePicker.getEndText());
            if (!end.after(start)) {
                JOptionPane.showMessageDialog(this, "结束时间必须大于开始时间");
                return;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (start.before(now)) {
                JOptionPane.showMessageDialog(this, "开始时间不能早于当前时间");
                return;
            }

            // 3. 会议主题
            String topic = tfTopic.getText().trim();
            if (topic.isEmpty()) {
                JOptionPane.showMessageDialog(this, "会议主题不能为空");
                return;
            }

            // 4. 参会人员（勾选）
            List<Long> selectedStaffIds = staffCheckBoxes.stream()  // 使用流处理
                    .filter(JCheckBox::isSelected)  // 筛选已勾选的复选框
                    .map(cb -> (Long) cb.getClientProperty("staffId"))  // 把JCheckBox对象转换为了Long类型的员工ID
                    .collect(Collectors.toList());  // 转换为列表
            // 确保当前用户在参会人员列表中
            if (!selectedStaffIds.contains(user.getStaffId())) {
                selectedStaffIds.add(user.getStaffId());
            }
            // 添加参会人数校验
            if (selectedStaffIds.size() < 2) {
                JOptionPane.showMessageDialog(this, "参会人数必须大于等于2人");
                return;
            }

            // 5. 容量检查（使用勾选人数）
            int cap = new MeetingRoomDAO().getCapacityByRoomId(roomId);
            if (cap <= 0) {
                JOptionPane.showMessageDialog(this, "读取会议室容量失败");
                return;
            }
            if (selectedStaffIds.size() > cap) {
                JOptionPane.showMessageDialog(this, "参会人数超出会议室容量上限（最大 " + cap + " 人）");
                return;
            }

            // 6. 冲突检查
            ReservationDAO dao = new ReservationDAO();
            if (dao.hasConflict(roomId, start, end)) {
                JOptionPane.showMessageDialog(this, "会议室时间冲突，请更换时间或会议室");
                return;
            }

            // 7. 提交预约（同时插入参会人员）
            String desc = tfDesc.getText().trim();
            boolean ok = dao.addReservation(
                    topic, user.getDeptId(), user.getStaffId(), roomId,
                    start, end, selectedStaffIds.size(), desc, selectedStaffIds
            );
            JOptionPane.showMessageDialog(this, ok ? "提交成功（待确认）" : "提交失败");
            if (ok) {
                loadMyReservations();
                // 清空表单
                tfTopic.setText("");
                tfDesc.setText("");
                //刷新会议室和人员勾选列表
                loadRoomOptions();
                refreshStaffCheckList();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "提交失败：" + e.getMessage());
        }
    }

    /**
     * 加载用户预订信息的方法
     * 从数据库中获取当前用户的预订记录，并将其加载到表格模型中
     */
    private void loadMyReservations() {
        // 清空表格中的所有行
        myModel.setRowCount(0);
        // 从数据库查询当前用户的预订记录
        List<ReservationList> list = new ReservationDAO().searchMyReservation(user.getStaffId());
        // 遍历预订记录列表，将每条记录添加到表格模型中
        for (ReservationList r : list) {
            // 向表格模型中添加一行数据，包含预订ID、预订编号、会议主题、
            // 会议室名称、开始时间、结束时间、参与人数、处理状态和备注信息
            myModel.addRow(new Object[]{
                    r.getReservationId(), r.getReservationNO(), r.getMeetingTopic(),
                    r.getRoomName(), r.getStartTime(), r.getEndTime(), r.getParticipantCount(), r.getProcess(), r.getComment()
            });
        }
    }
}