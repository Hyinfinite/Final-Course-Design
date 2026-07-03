package com.reservation.project.dao;

import com.reservation.project.model.ReservationList;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 会议室管理员扩展：已处理记录查询
 */
public class ManagerDAO {

    public List<ReservationList> searchProcessedReservation () {
        List<ReservationList> list = new ArrayList<ReservationList>();
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name," +
                "r.start_time, r.end_time, r.reservation_process, a.staff_name " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                "WHERE r.reservation_process IN ('已确认','已驳回','已取消') " +
                "ORDER BY r.created_at DESC";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
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
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }
}