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

    public boolean signIn(long participantId) {
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

            // 执行签到
            String sql = "UPDATE participant SET sign_in_process = '已签到', " +
                    "sign_in_time = NOW() WHERE participant_id = ? AND sign_in_process<>'已签到'";
            Connection con3 = null;
            PreparedStatement ps3 = null;
            try {
                con3 = SqlUtil.getConnection();
                if (con3 == null) {
                    return false;
                }
                ps3 = con3.prepareStatement(sql);
                ps3.setLong(1, participantId);
                return ps3.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                SqlUtil.closeAll(con3, ps3, null);
            }
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

    // ========== 在 ParticipantDAO 类的末尾添加以下方法 ==========

    /**
     * 指定会议，将该会议所属部门的所有员工签到（自动插入或更新参会记录）
     * @param reservationId 会议ID
     * @return 成功签到的人数
     */
    public int signInDepartmentByReservation(long reservationId) {
        // 1. 查询会议部门ID、开始时间、状态
        String infoSql = "SELECT apply_dept_id, start_time FROM reservation " +
                "WHERE reservation_id = ? AND reservation_process = '已确认'";
        Connection con = null;
        PreparedStatement psInfo = null;
        ResultSet rsInfo = null;
        long deptId = -1;
        Timestamp startTime = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return 0;
            }
            psInfo = con.prepareStatement(infoSql);
            psInfo.setLong(1, reservationId);
            rsInfo = psInfo.executeQuery();
            if (rsInfo.next()) {
                deptId = rsInfo.getLong("apply_dept_id");
                startTime = rsInfo.getTimestamp("start_time");
            } else {
                return 0; // 会议不存在或未确认
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            SqlUtil.closeAll(con, psInfo, rsInfo);
        }

        if (deptId <= 0 || startTime == null) {
            return 0;
        }
        // 检查会议是否已开始
        if (startTime.after(new Timestamp(System.currentTimeMillis()))) {
            return 0; // 会议未开始
        }

        // 2. 获取该部门所有员工ID
        List<Long> staffIds = new StaffDAO().getStaffIdsByDept(deptId);
        if (staffIds.isEmpty()) {
            return 0;
        }

        // 3. 先删除该会议原有的参会记录，再批量插入新记录（状态为已签到）
        int signedCount = 0;
        con = null;
        try {
            con = SqlUtil.getConnection();
            con.setAutoCommit(false);

            // 删除旧记录
            String deleteSql = "DELETE FROM participant WHERE reservation_id = ?";
            try (PreparedStatement psDel = con.prepareStatement(deleteSql)) {
                psDel.setLong(1, reservationId);
                psDel.executeUpdate();
            }

            // 插入新记录
            String insertSql = "INSERT INTO participant (reservation_id, participant_staff_id, sign_in_process, sign_in_time) " +
                    "VALUES (?, ?, '已签到', NOW())";
            try (PreparedStatement psIns = con.prepareStatement(insertSql)) {
                for (long sid : staffIds) {
                    psIns.setLong(1, reservationId);
                    psIns.setLong(2, sid);
                    psIns.addBatch();
                }
                int[] results = psIns.executeBatch();
                for (int r : results) {
                    if (r > 0) signedCount++;
                }
            }

            con.commit();
            return signedCount;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            SqlUtil.closeAll(con, null, null);
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