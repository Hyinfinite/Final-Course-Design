package com.reservation.project.dao;

import com.reservation.project.model.ReservationList;
import com.reservation.project.util.ReservationNOUtil;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReservationDAO {
    public boolean hasConflict(long room_id, Timestamp start_time, Timestamp end_time) {
        String sql = "SELECT COUNT(*) FROM reservation " +
                "WHERE reservation_room_id = ? " +
                "AND reservation_process IN ('待确认','已确认') " +
                "AND (? < end_time AND ? > start_time)";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return true;
            }

            ps = con.prepareStatement(sql);
            ps.setLong(1, room_id);
            ps.setTimestamp(2, start_time);
            ps.setTimestamp(3, end_time);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return true;
    }

    private String generateReservationNo() {
        return "RES" + System.currentTimeMillis() + String.format("%03d", new Random().nextInt(1000));
    }

    // 新增带参会人员列表的 addReservation 重载方法
    public boolean addReservation(String topic, long deptId, long applicantStaffId, long roomId,
                                  Timestamp start, Timestamp end, int count, String desc,
                                  List<Long> participantIds) {
        String reservationNo = generateReservationNo();
        String sql = "INSERT INTO reservation(reservation_no, meeting_topic, apply_dept_id, applicant_staff_id, " +
                "reservation_room_id, start_time, end_time, participant_count, meeting_desc) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement psPart = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);

            // 1. 插入预约
            ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, reservationNo);
            ps.setString(2, topic);
            ps.setLong(3, deptId);
            ps.setLong(4, applicantStaffId);
            ps.setLong(5, roomId);
            ps.setTimestamp(6, start);
            ps.setTimestamp(7, end);
            ps.setInt(8, count);
            ps.setString(9, desc);
            int affected = ps.executeUpdate();
            if (affected <= 0) {
                con.rollback();
                return false;
            }

            // 获取生成的 reservation_id
            ResultSet rs = ps.getGeneratedKeys();
            long reservationId = 0;
            if (rs.next()) {
                reservationId = rs.getLong(1);
            }
            rs.close();

            // 2. 插入参会人员（状态未签到）
            if (participantIds != null && !participantIds.isEmpty()) {
                String partSql = "INSERT INTO participant(reservation_id, participant_staff_id, sign_in_process) VALUES(?, ?, '未签到')";
                psPart = con.prepareStatement(partSql);
                for (Long sid : participantIds) {
                    psPart.setLong(1, reservationId);
                    psPart.setLong(2, sid);
                    psPart.addBatch();
                }
                psPart.executeBatch();
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            SqlUtil.closeAll(null, psPart, null);
            SqlUtil.closeAll(con, ps, null);
        }
    }



    public List<ReservationList> searchMyReservation(double applicant_id) {
        List<ReservationList> list = new ArrayList<ReservationList>();

        String sql = "SELECT " +
                "r.reservation_id, r.reservation_no, r.meeting_topic, " +
                "m.room_name, r.start_time, r.end_time, r.reservation_process, a.staff_name, " +
                "cl.confirm_comment " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                "LEFT JOIN (SELECT reservation_id, confirm_comment FROM confirmation_log WHERE confirm_id IN (SELECT MAX(confirm_id) FROM confirmation_log GROUP BY reservation_id)) cl ON r.reservation_id = cl.reservation_id " +
                "WHERE r.applicant_staff_id = ? " +
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
            ps.setDouble(1, applicant_id);
            rs = ps.executeQuery();

            while (rs.next()) {
                ReservationList ri = new ReservationList();
                ri.setReservationId((long) rs.getDouble("reservation_id"));
                ri.setReservationNO(rs.getString("reservation_no"));
                ri.setMeetingTopic(rs.getString("meeting_topic"));
                ri.setRoomName(rs.getString("room_name"));
                ri.setStartTime(rs.getString("start_time"));
                ri.setEndTime(rs.getString("end_time"));
                ri.setProcess(rs.getString("reservation_process"));
                ri.setApplicantName(rs.getString("staff_name"));
                ri.setComment(rs.getString("confirm_comment"));
                list.add(ri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    public boolean cancelReservation(long reservation_id, long applicant_id) {
        String sql = "UPDATE reservation SET reservation_process = '已取消' " +
                "WHERE reservation_id = ? AND applicant_staff_id = ? AND reservation_process = '待确认'";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }

            ps = con.prepareStatement(sql);
            ps.setLong(1, reservation_id);
            ps.setLong(2, applicant_id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public List<ReservationList> searchPendingReservation() {
        List<ReservationList> list = new ArrayList<ReservationList>();
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name, " +
                "r.start_time, r.end_time, r.reservation_process, a.staff_name, cl.confirm_comment " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                "LEFT JOIN (SELECT reservation_id, confirm_comment FROM confirmation_log WHERE confirm_id IN (SELECT MAX(confirm_id) FROM confirmation_log GROUP BY reservation_id)) cl ON r.reservation_id = cl.reservation_id " +
                "WHERE r.reservation_process = '待确认' ORDER BY r.created_at DESC";
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
                ReservationList ri = new ReservationList();
                ri.setReservationId((long) rs.getDouble("reservation_id"));
                ri.setReservationNO(rs.getString("reservation_no"));
                ri.setMeetingTopic(rs.getString("meeting_topic"));
                ri.setRoomName(rs.getString("room_name"));
                ri.setStartTime(rs.getString("start_time"));
                ri.setEndTime(rs.getString("end_time"));
                ri.setProcess(rs.getString("reservation_process"));
                ri.setApplicantName(rs.getString("staff_name"));
                ri.setComment(rs.getString("confirm_comment"));
                list.add(ri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    public boolean processReservation(long reservation_id, long manager_id, String process, String comment) {
        String updateSql = "UPDATE reservation SET reservation_process = ? " +
                "WHERE reservation_id = ? AND reservation_process = '待确认'";
        String confirmSql = "INSERT INTO confirmation_log (reservation_id, confirmer_staff_id, confirm_process, confirm_comment) " +
                "VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps1 = null, ps2 = null;

        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);

            // 1. 更新预约状态
            ps1 = con.prepareStatement(updateSql);
            ps1.setString(1, process);
            ps1.setLong(2, reservation_id);
            if (ps1.executeUpdate() <= 0) {
                con.rollback();
                return false;
            }

            // 2. 记录审批日志
            ps2 = con.prepareStatement(confirmSql);
            ps2.setLong(1, reservation_id);
            ps2.setLong(2, manager_id);
            ps2.setString(3, process);
            ps2.setString(4, comment);
            ps2.executeUpdate();

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
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
        List<ReservationList> list = new ArrayList<>();
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name, " +
                "r.start_time, r.end_time, r.reservation_process, a.staff_name, cl.confirm_comment " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                "LEFT JOIN (SELECT reservation_id, confirm_comment FROM confirmation_log WHERE confirm_id IN (SELECT MAX(confirm_id) FROM confirmation_log GROUP BY reservation_id)) cl ON r.reservation_id = cl.reservation_id " +
                "WHERE r.apply_dept_id = ? AND r.reservation_process = '已确认' " +
                "AND r.start_time <= NOW() " +
                "ORDER BY r.start_time DESC";
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
                ReservationList ri = new ReservationList();
                ri.setReservationId(rs.getLong("reservation_id"));
                ri.setReservationNO(rs.getString("reservation_no"));
                ri.setMeetingTopic(rs.getString("meeting_topic"));
                ri.setRoomName(rs.getString("room_name"));
                ri.setStartTime(String.valueOf(rs.getTimestamp("start_time")));
                ri.setEndTime(String.valueOf(rs.getTimestamp("end_time")));
                ri.setProcess(rs.getString("reservation_process"));
                ri.setApplicantName(rs.getString("staff_name"));
                ri.setComment(rs.getString("confirm_comment"));
                list.add(ri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    public boolean isReservationApplicant(long reservationId, long staffId) {
        String sql = "SELECT 1 FROM reservation WHERE reservation_id = ? AND applicant_staff_id = ?";
        try (Connection con = SqlUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            ps.setLong(2, staffId);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}