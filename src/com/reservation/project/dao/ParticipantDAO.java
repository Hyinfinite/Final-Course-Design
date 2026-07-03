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
            if (con == null) {
                return false;
            }
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
            return false;
        } finally {
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
                "WHERE r.apply_dept_id = ? " +  // 移除状态限制，显示所有状态的会议
                "ORDER BY r.start_time DESC, p.participant_id DESC";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            ps = con.prepareStatement(sql);
            ps.setLong(1, deptId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Participant x = new Participant();
                x.setParticipantId(rs.getLong("participant_id"));
                x.setReservationId(rs.getLong("reservation_id"));
                x.setReservationNo(rs.getString("reservation_no"));
                x.setMeetingTopic(rs.getString("meeting_topic"));
                x.setStartTime(String.valueOf(rs.getTimestamp("start_time")));
                x.setEndTime(String.valueOf(rs.getTimestamp("end_time")));
                x.setParticipantStaffId(rs.getLong("participant_staff_id"));
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

    public boolean signIn(long participantId, long operatorId) {
        // 首先检查是否已经签到
        String checkSql = "SELECT sign_in_process FROM participant WHERE participant_id = ?";
        Connection con1 = null;
        PreparedStatement ps1 = null;
        try {
            con1 = SqlUtil.getConnection();
            if (con1 == null) {
                return false;
            }
            ps1 = con1.prepareStatement(checkSql);
            ps1.setLong(1, participantId);
            ResultSet rs = ps1.executeQuery();
            if (rs.next() && "已签到".equals(rs.getString("sign_in_process"))) {
                return false; // 已经签到
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con1, ps1, null);
        }

        // 检查会议是否已经开始
        String checkTimeSql = "SELECT start_time FROM participant p " +
                "JOIN reservation r ON p.reservation_id = r.reservation_id " +
                "WHERE p.participant_id = ?";
        Connection con2 = null;
        PreparedStatement ps2 = null;
        try {
            con2 = SqlUtil.getConnection();
            if (con2 == null) {
                return false;
            }
            ps2 = con2.prepareStatement(checkTimeSql);
            ps2.setLong(1, participantId);
            ResultSet rs = ps2.executeQuery();
            if (rs.next()) {
                Timestamp startTime = rs.getTimestamp("start_time");
                if (startTime.after(new Timestamp(System.currentTimeMillis()))) {
                    return false; // 会议还未开始
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con2, ps2, null);
        }

        // 检查操作者身份
        String checkIdentitySql = "SELECT p.participant_id, r.applicant_staff_id FROM participant p " +
                "JOIN reservation r ON p.reservation_id = r.reservation_id " +
                "WHERE p.participant_id = ?";
        Connection con3 = null;
        PreparedStatement ps3 = null;
        try {
            con3 = SqlUtil.getConnection();
            if (con3 == null) {
                return false;
            }
            ps3 = con3.prepareStatement(checkIdentitySql);
            ps3.setLong(1, participantId);
            ResultSet rs = ps3.executeQuery();
            if (rs.next()) {
                long applicantStaffId = rs.getLong("applicant_staff_id");
                if (operatorId != applicantStaffId && operatorId != rs.getLong("participant_staff_id")) {
                    return false; // 操作者既不是申请人也不是参会人员本人
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con3, ps3, null);
        }

        // 执行签到
        String sql = "UPDATE participant SET sign_in_process = '已签到', " +
                "sign_in_time = NOW() WHERE participant_id = ? AND sign_in_process<>'已签到'";
        Connection con4 = null;
        PreparedStatement ps4 = null;
        try {
            con4 = SqlUtil.getConnection();
            if (con4 == null) {
                return false;
            }
            ps4 = con4.prepareStatement(sql);
            ps4.setLong(1, participantId);
            return ps4.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con4, ps4, null);
        }
    }


    public List<Participant> listSignRecordsByDateAndDept(String date, long deptId) {
        String sql = "SELECT p.* FROM participant p " +
                "JOIN reservation r ON p.reservation_id = r.reservation_id " +
                "WHERE DATE(r.start_time) = ? AND r.apply_dept_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        List<Participant> list = new ArrayList<>();
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, date);
            ps.setLong(2, deptId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Participant p = new Participant();
                p.setParticipantId(rs.getLong("participant_id"));
                p.setReservationNo(rs.getString("reservation_no"));
                p.setMeetingTopic(rs.getString("meeting_topic"));
                p.setStartTime(rs.getString("start_time"));
                p.setEndTime(rs.getString("end_time"));
                p.setParticipantName(rs.getString("participant_name"));
                p.setSignInProcess(rs.getString("sign_in_process"));
                p.setSignInTime(rs.getString("sign_in_time"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
        return list;
    }

    public int signInAllParticipants(long reservationId) {
        String sql = "UPDATE participant SET sign_in_process = '已签到', sign_in_time = NOW() " +
                "WHERE reservation_id = ? AND sign_in_process <> '已签到'";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return 0;
            ps = con.prepareStatement(sql);
            ps.setLong(1, reservationId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    /**
     * 根据预约ID获取该会议的所有参会人员签到记录（用于显示名单）
     * @param reservationId 预约ID
     * @return 参会人员列表
     */
    public List<Participant> listParticipantsByReservation(long reservationId) {
        List<Participant> list = new ArrayList<>();
        String sql = "SELECT p.participant_id, p.reservation_id, r.reservation_no, r.meeting_topic, " +
                "r.start_time, r.end_time, p.participant_staff_id, a.staff_name, " +
                "p.sign_in_process, p.sign_in_time " +
                "FROM participant p " +
                "JOIN reservation r ON p.reservation_id = r.reservation_id " +
                "JOIN admin_staff a ON p.participant_staff_id = a.staff_id " +
                "WHERE p.reservation_id = ? " +
                "ORDER BY p.participant_id";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return list;
            ps = con.prepareStatement(sql);
            ps.setLong(1, reservationId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Participant x = new Participant();
                x.setParticipantId(rs.getLong("participant_id"));
                x.setReservationId(rs.getLong("reservation_id"));
                x.setReservationNo(rs.getString("reservation_no"));
                x.setMeetingTopic(rs.getString("meeting_topic"));
                x.setStartTime(String.valueOf(rs.getTimestamp("start_time")));
                x.setEndTime(String.valueOf(rs.getTimestamp("end_time")));
                x.setParticipantStaffId(rs.getLong("participant_staff_id"));
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
}