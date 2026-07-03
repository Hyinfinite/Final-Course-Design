package com.reservation.project.dao;

import com.reservation.project.model.Participant;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipantDAO {

    // 生成参会名单（可在预约通过后调用；这里提供手动方法）
    // participants: staff_id 列表
    public boolean batchAddParticipants(long reservationId, List<Long> participants) {
        if (participants == null || participants.isEmpty()) return true;
        String sql = "INSERT IGNORE INTO participant(reservation_id, participant_staff_id, sign_in_process) " +
                "VALUES(?,?, '未签到')";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);

            ps = con.prepareStatement(sql);
            for (Long sid : participants) {
                ps.setLong(1, reservationId);
                ps.setLong(2, sid);
                ps.addBatch();
            }
            ps.executeBatch();
            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null) con.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (Exception ignored) {}
            SqlUtil.closeAll(con, ps, null);
        }
    }

    // 行政人员：查看本部门可签到记录（已确认会议）
    public List<Participant> listSignRecordsByDept(long deptId) {
        List<Participant> list = new ArrayList<Participant>();
        String sql = "SELECT p.participant_id, p.reservation_id, r.reservation_no, r.meeting_topic, r.start_time, r.end_time," +
                "p.participant_staff_id, a.staff_name, p.sign_in_process, p.sign_in_time " +
                "FROM participant p " +
                "JOIN reservation r ON p.reservation_id = r.reservation_id " +
                "JOIN admin_staff a ON p.participant_staff_id = a.staff_id " +
                "WHERE r.reservation_process = '已确认' AND r.apply_dept_id = ? " +
                "ORDER BY r.start_time DESC, p.participant_id DESC";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return list;
            ps = con.prepareStatement(sql);
            ps.setLong(1, deptId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Participant x = new Participant();
                x.setParticipantID(rs.getLong("participant_id"));
                x.setReservationID(rs.getLong("reservation_id"));
                x.setReservationNo(rs.getString("reservation_no"));
                x.setMeetingTopic(rs.getString("meeting_topic"));
                x.setStartTime(String.valueOf(rs.getTimestamp("start_time")));
                x.setEndTime(String.valueOf(rs.getTimestamp("end_time")));
                x.setParticipantStaffID(rs.getLong("participant_staff_id"));
                x.setParticipantName(rs.getString("staff_name"));
                x.setSignInProcess(rs.getString("sign_in_process"));
                x.setSignInTime(rs.getTimestamp("sign_in_time") == null ? "" : String.valueOf(rs.getTimestamp("sign_in_time")));
                list.add(x);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    public boolean signIn(long participantId) {
        String sql = "UPDATE participant SET sign_in_process = '已签到', sign_in_time = NOW() " +
                "WHERE participant_id = ? AND sign_in_process<>'已签到'";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            ps = con.prepareStatement(sql);
            ps.setLong(1, participantId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }
}