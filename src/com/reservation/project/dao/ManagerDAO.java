package com.reservation.project.dao;

import com.reservation.project.model.ReservationList;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 会议室管理员扩展：已处理记录查询
 * 该类用于实现会议室管理系统中管理员查询已处理预约记录的功能
 */
public class ManagerDAO {

    /**
     * 查询所有已处理的预约记录
     * 已处理状态包括：已确认、已驳回、已取消
     * @return 返回已处理预约记录的列表，每个记录包含预约ID、预约编号、会议主题、会议室名称、开始时间、结束时间、处理状态和申请人姓名等信息
     */
    public List<ReservationList> searchProcessedReservation () {
        // 初始化结果列表
        List<ReservationList> list = new ArrayList<ReservationList>();
        // SQL查询语句，用于获取已处理的预约记录及其相关信息
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name," +
                "r.start_time, r.end_time, r.reservation_process, a.staff_name " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                "WHERE r.reservation_process IN ('已确认','已驳回','已取消') " +
                "ORDER BY r.created_at DESC";

        // 声明数据库连接对象、预处理语句对象和结果集对象
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 如果连接为空，直接返回空列表
            if (con == null) {
                return list;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 执行查询，获取结果集
            rs = ps.executeQuery();
            // 遍历结果集，将每条记录转换为ReservationList对象并添加到结果列表中
            while (rs.next()) {
                ReservationList r = new ReservationList();
                r.setReservationId(rs.getLong("reservation_id"));
                r.setReservationNO(rs.getString("reservation_no"));
                r.setMeetingTopic(rs.getString("meeting_topic"));
                r.setRoomName(rs.getString("room_name"));
                r.setStartTime(String.valueOf(rs.getTimestamp("start_time")));
                r.setEndTime(String.valueOf(rs.getTimestamp("end_time")));
                r.setProcess(rs.getString("reservation_process"));
                r.setApplicantName(rs.getString("staff_name"));
                list.add(r);
            }
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        // 返回结果列表
        return list;
    }
}