package com.reservation.project.dao;

import com.reservation.project.model.ReservationList;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 给系统管理员用：按日期/部门/会议室筛选全部预约
 * 该类用于实现数据库查询操作，提供按不同条件筛选预约信息的功能
 */
public class ReservationQueryDAO {

/**
 * 查询所有预约信息
 * @param dateStr 日期字符串，用于筛选特定日期的预约
 * @param deptId 部门ID，用于筛选特定部门的预约
 * @param roomId 房间ID，用于筛选特定房间的预约
 * @return 返回预约列表，包含预约详细信息
 */
    public List<ReservationList> queryAll(String dateStr, long deptId, long roomId) {
        // 创建预约列表用于存储查询结果
        List<ReservationList> list = new ArrayList<ReservationList>();

        // 构建SQL查询语句，包含多表连接和子查询
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name," +  // 主查询：选择预约相关信息
                "r.start_time, r.end_time, r.reservation_process, a.staff_name, r.participant_count, cl.confirm_comment " +  // 选择字段：包括预约ID、编号、主题、会议室名称、开始时间、结束时间、流程状态、申请人姓名、参与人数和确认评论
                "FROM reservation r " +  // 主表：预约信息表
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +  // 连接会议室表，通过房间ID关联
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +  // 连接管理员员工表，通过员工ID关联
                "LEFT JOIN (SELECT reservation_id, confirm_comment " +  // 子查询：获取每个预约的最新确认评论
                "FROM confirmation_log " +
                "WHERE confirm_id IN (SELECT MAX(confirm_id) " +  // 子查询：获取每个预约的最大确认ID
                "FROM confirmation_log " +
                "GROUP BY reservation_id)) cl ON r.reservation_id = cl.reservation_id " + // 连接确认日志表，通过预约ID关联
                "WHERE 1=1 ";  // 条件占位符，便于后续添加条件

        // 创建参数列表，用于存储SQL查询的参数
        List<Object> params = new ArrayList<Object>();

        // 如果日期字符串不为空，添加日期条件
        if (dateStr != null && dateStr.trim().length() > 0) {
            sql += "AND DATE(r.start_time)=? ";    // 添加日期筛选条件
            params.add(dateStr.trim());    // 添加日期参数
        }
        // 如果部门ID大于0，添加部门条件
        if (deptId > 0) {
            sql += "AND r.apply_dept_id=? ";    // 添加部门筛选条件
            params.add(deptId);    // 添加部门参数
        }
        // 如果房间ID大于0，添加房间条件
        if (roomId > 0) {
            sql += "AND r.reservation_room_id = ? ";    // 添加房间筛选条件
            params.add(roomId);    // 添加房间参数
        }

        // 按创建时间降序排列，确保最新的预约显示在最前面
        sql += "ORDER BY r.create_time DESC";

        // 初始化数据库连接对象
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);

            // 设置查询参数
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            // 执行查询
            rs = ps.executeQuery();
            // 遍历结果集，将每条记录转换为ReservationList对象并添加到列表中
            while (rs.next()) {
                ReservationList r = new ReservationList();
                r.setReservationId(rs.getLong("reservation_id")); // 获取预约ID
                r.setReservationNO(rs.getString("reservation_no")); // 获取预约编号
                r.setMeetingTopic(rs.getString("meeting_topic")); // 获取会议主题
                r.setRoomName(rs.getString("room_name")); // 获取会议室名称
                r.setStartTime(String.valueOf(rs.getTimestamp("start_time"))); // 获取开始时间
                r.setEndTime(String.valueOf(rs.getTimestamp("end_time"))); // 获取结束时间
                r.setParticipantCount(rs.getInt("participant_count")); // 获取参与人数
                r.setProcess(rs.getString("reservation_process")); // 获取预约状态
                r.setApplicantName(rs.getString("staff_name")); // 获取申请人姓名
                r.setComment(rs.getString("confirm_comment")); // 获取审批意见
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }
}