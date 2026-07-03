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

    public boolean addReservation(String topic, long deptId, long applicantStaffId, long roomId,
                                  Timestamp start, Timestamp end, int count, String desc) {
        String reservationNo = generateReservationNo();
        String sql = "INSERT INTO reservation(reservation_no, meeting_topic, apply_dept_id, applicant_staff_id, " +
                "reservation_room_id, start_time, end_time, participant_count, meeting_desc) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            con.setAutoCommit(false);
            ps = con.prepareStatement(sql);
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
            if (affected > 0) {
                // 获取新创建的预约ID
                long reservationId = getReservationIdByNo(reservationNo);
                if (reservationId > 0) {
                    // 生成参会人员列表（这里可以根据需要修改）
                    List<Long> participants = new ArrayList<>();
                    participants.add(applicantStaffId);  // 添加申请人自己
                    // 这里可以添加更多的参会人员，例如：
                    // participants.addAll(getDepartmentStaffIds(deptId));

                    // 使用batchAddParticipants方法添加参会人员
                    ParticipantDAO participantDAO = new ParticipantDAO();
                    if (!participantDAO.batchAddParticipants(reservationId, participants)) {
                        throw new Exception("添加参会人员失败");
                    }
                }
                con.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
        return false;
    }


    public List<ReservationList> searchMyReservation(double applicant_id) {
        List<ReservationList> list = new ArrayList<ReservationList>();

        String sql = "SELECT " +
                "r.reservation_id, r.reservation_no, r.meeting_topic, " +
                "m.room_name, r.start_time, r.end_time, r.reservation_process, a.staff_name " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
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
                "r.start_time, r.end_time, r.reservation_process, a.staff_name " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
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
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            con.setAutoCommit(false);

            ps1 = con.prepareStatement(updateSql);
            ps1.setString(1, process);
            ps1.setLong(2, reservation_id);
            int n = ps1.executeUpdate();
            if (n <= 0) {
                con.rollback();
                return false;
            }

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
        } finally {
            SqlUtil.closeAll(con, ps2, null);
            SqlUtil.closeAll(con, ps1, null);
        }
        return false;
    }


    public int[] processCount() {
        int[] c = new int[3];
        String sql = "SELECT COUNT(*) AS total, " +
                "SUM(CASE WHEN reservation_process = '待确认' THEN 1 ELSE 0 END) AS pending, " +
                "SUM(CASE WHEN reservation_process = '已确认' THEN 1 ELSE 0 END) AS confirmed " +
                "FROM reservation";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return c;
            }

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                c[0] = rs.getInt("total");
                c[1] = rs.getInt("pending");
                c[2] = rs.getInt("confirmed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return c;
    }

    public long getReservationIdByNo(String reservationNo) {
        String sql = "SELECT reservation_id FROM reservation WHERE reservation_no = ?";
        try (Connection conn = SqlUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reservationNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("reservation_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}