package com.reservation.project.dao;

import com.reservation.project.model.ReservationList;
import com.reservation.project.util.ReservationNOUtil;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 预约数据访问对象，提供会议室预约相关的数据操作功能
 * 包含预约的增删改查、冲突检查、状态处理等方法
 */
public class ReservationDAO {
    /**
     * 检查指定房间在给定时间段内是否存在预约冲突
     * @param room_id 房间ID
     * @param start_time 开始时间
     * @param end_time 结束时间
     * @return 如果存在冲突返回true，否则返回false
     */
    public boolean hasConflict(long room_id, Timestamp start_time, Timestamp end_time) {
        // SQL查询语句，检查指定房间在给定时间段内是否有已确认或待确认的预约
        String sql = "SELECT COUNT(*) FROM reservation " +
                "WHERE reservation_room_id = ? " +
                "AND reservation_process IN ('待确认','已确认') " +  // 只检查待确认和已确认状态的预约
                "AND (? < end_time AND ? > start_time)";  // 时间重叠条件
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return true;  // 获取数据库连接失败，视为有冲突
            }

            ps = con.prepareStatement(sql);
            ps.setLong(1, room_id);  // 设置房间ID参数
            ps.setTimestamp(2, start_time);  // 设置开始时间参数
            ps.setTimestamp(3, end_time);  // 设置结束时间参数
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;  // 如果查询结果大于0，说明存在冲突
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);  // 关闭所有数据库资源
        }
        return true;  // 发生异常时默认返回有冲突
    }

    /**
     * 生成唯一的预约编号
     * @return 返回格式为"RES"加时间戳加随机数的预约编号
     */
    private String generateReservationNo() {
        return "RES" + System.currentTimeMillis() + String.format("%03d", new Random().nextInt(1000));
    }

    /**
     * 添加新的会议室预约，包含参会人员列表
     * @param topic 会议主题
     * @param deptId 申请部门ID
     * @param applicantStaffId 申请人ID
     * @param roomId 会议室ID
     * @param start 开始时间
     * @param end 结束时间
     * @param count 参会人数
     * @param desc 会议描述
     * @param participantIds 参会人员ID列表
     * @return 添加成功返回true，否则返回false
     */
    // 新增带参会人员列表的 addReservation 重载方法
    public boolean addReservation(String topic, long deptId, long applicantStaffId, long roomId,
                                  Timestamp start, Timestamp end, int count, String desc,
                                  List<Long> participantIds) {
        String reservationNo = generateReservationNo();  // 生成预约编号
        // 插入预约的SQL语句
        String sql = "INSERT INTO reservation(reservation_no, meeting_topic, apply_dept_id, applicant_staff_id, " +
                "reservation_room_id, start_time, end_time, participant_count, meeting_desc) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement psPart = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);  // 开启事务

            // 1. 插入预约
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, reservationNo);  // 设置预约编号
            ps.setString(2, topic);  // 设置会议主题
            ps.setLong(3, deptId);  // 设置申请部门ID
            ps.setLong(4, applicantStaffId);  // 设置申请人ID
            ps.setLong(5, roomId);  // 设置会议室ID
            ps.setTimestamp(6, start);  // 设置开始时间
            ps.setTimestamp(7, end);  // 设置结束时间
            ps.setInt(8, count);  // 设置参会人数
            ps.setString(9, desc);  // 设置会议描述
            int affected = ps.executeUpdate();
            if (affected <= 0) {
                con.rollback();  // 插入失败，回滚事务
                return false;
            }

            // 获取生成的 reservation_id
            ResultSet rs = ps.getGeneratedKeys();
            long reservationId = 0;
            if (rs.next()) {
                reservationId = rs.getLong(1);  // 获取生成的预约ID
            }
            rs.close();

            // 2. 插入参会人员（状态未签到）
            if (participantIds != null && !participantIds.isEmpty()) {
                String partSql = "INSERT INTO participant(reservation_id, participant_staff_id, sign_in_process) VALUES(?, ?, '未签到')";
                psPart = con.prepareStatement(partSql);
                for (Long sid : participantIds) {
                    psPart.setLong(1, reservationId);  // 设置预约ID
                    psPart.setLong(2, sid);  // 设置参会人员ID
                    psPart.addBatch();  // 添加到批处理
                }
                psPart.executeBatch();  // 批量插入参会人员
            }

            con.commit();  // 提交事务
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }  // 发生异常，回滚事务
            return false;
        } finally {
            SqlUtil.closeAll(null, psPart, null);  // 关闭参会人员插入的数据库资源
            SqlUtil.closeAll(con, ps, null);  // 关闭预约插入的数据库资源
        }
    }



    /**
     * 查询指定用户的预约列表
     * @param applicant_id 申请人ID
     * @return 返回该用户的预约列表
     */
    public List<ReservationList> searchMyReservation(double applicant_id) {
        // 初始化预约列表集合
        List<ReservationList> list = new ArrayList<ReservationList>();

        // 查询预约列表的SQL语句，包括会议室、申请人信息以及最新的审批意见
        String sql = "SELECT " +
                "r.reservation_id, r.reservation_no, r.meeting_topic, " +  // 预约基本信息
                "m.room_name, r.start_time, r.end_time, r.reservation_process, a.staff_name, " +  // 会议室和申请人信息
                "cl.confirm_comment, r.participant_count " +  // 参与人数和审批意见
                "FROM reservation r " +  // 预约表
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +  // 关联会议室表
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +  // 关联管理员表
                "LEFT JOIN (SELECT reservation_id, confirm_comment FROM confirmation_log WHERE confirm_id IN (SELECT MAX(confirm_id) FROM confirmation_log GROUP BY reservation_id)) cl ON r.reservation_id = cl.reservation_id " +  // 获取最新审批意见
                "WHERE r.applicant_staff_id = ? " +  // 筛选条件：申请人ID
                "ORDER BY r.created_at DESC";  // 按创建时间降序排列



        // 数据库相关对象声明
        Connection con = null;  // 数据库连接对象
        PreparedStatement ps = null;  // 预处理语句对象
        ResultSet rs = null;  // 结果集对象
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;  // 连接失败返回空列表
            }

            // 创建预处理语句并设置参数
            ps = con.prepareStatement(sql);
            ps.setDouble(1, applicant_id);  // 设置申请人ID参数
            rs = ps.executeQuery();  // 执行查询

            // 遍历结果集，将每条记录转换为ReservationList对象并添加到列表
            while (rs.next()) {
                ReservationList ri = new ReservationList();
                ri.setReservationId((long) rs.getDouble("reservation_id"));  // 设置预约ID
                ri.setReservationNO(rs.getString("reservation_no"));  // 设置预约编号
                ri.setMeetingTopic(rs.getString("meeting_topic"));  // 设置会议主题
                ri.setRoomName(rs.getString("room_name"));  // 设置会议室名称
                ri.setStartTime(rs.getString("start_time"));  // 设置开始时间
                ri.setEndTime(rs.getString("end_time"));  // 设置结束时间
                ri.setParticipantCount(rs.getInt("participant_count"));  // 设置参与人数
                ri.setProcess(rs.getString("reservation_process"));  // 设置预约状态
                ri.setApplicantName(rs.getString("staff_name"));  // 设置申请人姓名
                ri.setComment(rs.getString("confirm_comment"));  // 设置审批意见
                list.add(ri);  // 添加到列表
            }
        } catch (Exception e) {
            e.printStackTrace();  // 异常处理
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;  // 返回预约列表
    }

    /**
     * 取消预约
     * @param reservation_id 预约ID
     * @param applicant_id 申请人ID
     * @return 取消成功返回true，否则返回false
     */
    public boolean cancelReservation(long reservation_id, long applicant_id) {
        // SQL更新语句，将预约状态更新为"已取消"
        // 条件是预约ID匹配、申请人ID匹配且当前状态为"待确认"
        String sql = "UPDATE reservation SET reservation_process = '已取消' " +
                "WHERE reservation_id = ? AND applicant_staff_id = ? AND reservation_process = '待确认'";
        Connection con = null;  // 数据库连接对象
        PreparedStatement ps = null;  // 预处理语句对象
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 检查连接是否获取成功
            if (con == null) {
                return false;
            }

            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置第一个参数为预约ID
            ps.setLong(1, reservation_id);
            // 设置第二个参数为申请人ID
            ps.setLong(2, applicant_id);
            // 执行更新操作，如果影响行数大于0则表示更新成功
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
            return false;
        } finally {
            // 确保关闭所有资源，包括连接、预处理语句和结果集
            SqlUtil.closeAll(con, ps, null);
        }
    }

    /**
     * 查询所有待确认的预约
     * @return 返回待确认的预约列表
     */
    public List<ReservationList> searchPendingReservation() {
        // 创建一个ReservationList列表用于存储查询结果
        List<ReservationList> list = new ArrayList<ReservationList>();
        // 编写SQL查询语句，查询待确认的预约信息，包括预约ID、预约编号、会议主题、会议室名称、开始时间、结束时间、预约流程、申请人姓名、确认意见和参与人数
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name, " +
                "r.start_time, r.end_time, r.reservation_process, a.staff_name, cl.confirm_comment, r.participant_count " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                "LEFT JOIN (SELECT reservation_id, confirm_comment FROM confirmation_log WHERE confirm_id IN (SELECT MAX(confirm_id) FROM confirmation_log GROUP BY reservation_id)) cl ON r.reservation_id = cl.reservation_id " +
                "WHERE r.reservation_process = '待确认' ORDER BY r.created_at DESC";
        // 声明数据库连接、预处理结果集和结果集变量
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 如果连接获取失败，直接返回空列表
            if (con == null) {
                return list;
            }

            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 执行查询
            rs = ps.executeQuery();
            // 遍历结果集，将每条记录封装为ReservationList对象并添加到列表中
            while (rs.next()) {
                ReservationList ri = new ReservationList();
                ri.setReservationId((long) rs.getDouble("reservation_id"));
                ri.setReservationNO(rs.getString("reservation_no"));
                ri.setMeetingTopic(rs.getString("meeting_topic"));
                ri.setRoomName(rs.getString("room_name"));
                ri.setStartTime(rs.getString("start_time"));
                ri.setEndTime(rs.getString("end_time"));
                ri.setParticipantCount(rs.getInt("participant_count"));
                ri.setProcess(rs.getString("reservation_process"));
                ri.setApplicantName(rs.getString("staff_name"));
                ri.setComment(rs.getString("confirm_comment"));
                list.add(ri);
            }
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        // 返回查询结果列表
        return list;
    }

    /**
     * 处理预约（确认或拒绝）
     * @param reservation_id 预约ID
     * @param manager_id 处理人ID
     * @param process 处理状态（"已确认"或"已拒绝"）
     * @param comment 处理意见
     * @return 处理成功返回true，否则返回false
     */
    public boolean processReservation(long reservation_id, long manager_id, String process, String comment) {
        // 更新预约状态的SQL语句
        String updateSql = "UPDATE reservation SET reservation_process = ? " +
                "WHERE reservation_id = ? AND reservation_process = '待确认'";
        // 插入审批日志的SQL语句
        String confirmSql = "INSERT INTO confirmation_log (reservation_id, confirmer_staff_id, confirm_process, confirm_comment) " +
                "VALUES (?, ?, ?, ?)";
        // 初始化数据库连接和预处理语句
        Connection con = null;
        PreparedStatement ps1 = null, ps2 = null;

        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) return false;
            // 开启事务
            con.setAutoCommit(false);

            // 1. 更新预约状态
            ps1 = con.prepareStatement(updateSql);
            ps1.setString(1, process);  // 设置处理状态
            ps1.setLong(2, reservation_id);  // 设置预约ID
            // 如果更新失败，则回滚事务并返回false
            if (ps1.executeUpdate() <= 0) {
                con.rollback();
                return false;
            }

            // 2. 记录审批日志
            ps2 = con.prepareStatement(confirmSql);
            ps2.setLong(1, reservation_id);  // 设置预约ID
            ps2.setLong(2, manager_id);  // 设置处理人ID
            ps2.setString(3, process);  // 设置处理状态
            ps2.setString(4, comment);  // 设置处理意见
            ps2.executeUpdate();

            // 提交事务
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps2, null);
            SqlUtil.closeAll(null, ps1, null);
        }
    }

    /**
     * 根据部门ID查询已确认且已开始的会议（用于签到选择）
     * @param deptId 部门ID
     * @return 预约列表
     */
    public List<ReservationList> searchConfirmedReservationsByDept(long deptId) {
    // 创建一个空的预约列表用于存储查询结果
        List<ReservationList> list = new ArrayList<>();
    // 定义SQL查询语句，查询已确认且已开始的会议信息
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name, " +
                "r.start_time, r.end_time, r.reservation_process, a.staff_name, cl.confirm_comment, r.participant_count " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +  // 关联会议室表
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +    // 关联管理员表
                "LEFT JOIN (SELECT reservation_id, confirm_comment FROM confirmation_log WHERE confirm_id IN (SELECT MAX(confirm_id) FROM confirmation_log GROUP BY reservation_id)) cl ON r.reservation_id = cl.reservation_id " +  // 关联确认日志表
                "WHERE r.apply_dept_id = ? AND r.reservation_process = '已确认' " +  // 筛选条件：部门ID和已确认状态
                "AND r.start_time <= NOW() " +  // 筛选条件：会议已开始
                "ORDER BY r.start_time DESC";   // 按开始时间降序排列
    // 初始化数据库连接对象
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) return list;  // 如果连接失败，返回空列表
        // 创建预处理语句
            ps = con.prepareStatement(sql);
            ps.setLong(1, deptId);  // 设置部门ID参数
        // 执行查询
            rs = ps.executeQuery();
        // 遍历结果集，将每条记录转换为ReservationList对象并添加到列表中
            while (rs.next()) {
                ReservationList ri = new ReservationList();
                ri.setReservationId(rs.getLong("reservation_id"));  // 设置预约ID
                ri.setReservationNO(rs.getString("reservation_no"));  // 设置预约编号
                ri.setMeetingTopic(rs.getString("meeting_topic"));  // 设置会议主题
                ri.setRoomName(rs.getString("room_name"));  // 设置会议室名称
                ri.setStartTime(String.valueOf(rs.getTimestamp("start_time")));  // 设置开始时间
                ri.setEndTime(String.valueOf(rs.getTimestamp("end_time")));  // 设置结束时间
                ri.setParticipantCount(rs.getInt("participant_count"));  // 设置参与人数
                ri.setProcess(rs.getString("reservation_process"));  // 设置预约状态
                ri.setApplicantName(rs.getString("staff_name"));  // 设置申请人姓名
                ri.setComment(rs.getString("confirm_comment"));  // 设置确认意见
                list.add(ri);  // 将对象添加到列表中
            }
        } catch (Exception e) {
            e.printStackTrace();  // 打印异常信息
        } finally {
        // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;  // 返回查询结果列表
    }

    /**
     * 检查指定员工是否是预约的申请人
     * @param reservationId 预约ID
     * @param staffId 员工ID
     * @return 如果是申请人返回true，否则返回false
     */
    public boolean isReservationApplicant(long reservationId, long staffId) {
        // 定义SQL查询语句，检查指定预约ID和员工ID是否匹配
        String sql = "SELECT 1 FROM reservation WHERE reservation_id = ? AND applicant_staff_id = ?";
        try (Connection con = SqlUtil.getConnection();  // 获取数据库连接
             PreparedStatement ps = con.prepareStatement(sql)) {  // 创建预处理语句
            // 设置第一个参数为预约ID
            ps.setLong(1, reservationId);
            // 设置第二个参数为员工ID
            ps.setLong(2, staffId);
            // 执行查询并检查是否有结果，有结果表示该员工是预约申请人
            return ps.executeQuery().next();
        } catch (Exception e) {
            // 捕获并打印异常
            e.printStackTrace();
            // 发生异常时返回false
            return false;
        }
    }

}