package Conference_Reservation.DAO;

import Conference_Reservation.Model.ReservationList;
import Conference_Reservation.Util.ReservationNOUtil;
import Conference_Reservation.Util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    public boolean hasConflict(long room_id, Timestamp start_time, Timestamp end_time) {
        String sql = "SELECT COUNT(*) FROM reservation " +
                "WHERE reservation_room_id = ? " +
                "AND reservation_process IN ('待确认','已确认') " +
                "AND (? < end_time AND ? > start_time)";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return true;

            stm = con.prepareStatement(sql);
            stm.setLong(1, room_id);
            stm.setTimestamp(2, start_time);
            stm.setTimestamp(3, end_time);
            rs = stm.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return true;
    }

    public boolean addReservation(String topic, long dept_id, long applicant_id, long room_id, Timestamp start_time, Timestamp end_time, int count, String desc) {
        String sql = "INSERT INTO reservation " +
                "(reservation_no, meeting_topic, apply_dept_id, applicant_staff_id, reservation_room_id, start_time, end_time, participant_count, meeting_desc, reservation_process) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '待确认')";
        Connection con = null;
        PreparedStatement stm = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;

            stm = con.prepareStatement(sql);
            stm.setString(1, ReservationNOUtil.ReservationNO());
            stm.setString(2, topic);
            stm.setLong(3, dept_id);
            stm.setLong(4, applicant_id);
            stm.setLong(5, room_id);
            stm.setTimestamp(6, start_time);
            stm.setTimestamp(7, end_time);
            stm.setInt(8, count);
            stm.setString(9, desc);
            return stm.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, stm, null);
        }
    }

    public List<ReservationList> searchMyReservation(long applicant_id) {
        List<ReservationList> list = new ArrayList<ReservationList>();
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name, " +
                "r.start_time, r.end_time, r.reservation_process, a.staff_name " +
                "FROM reservation r " +
                "JOIN meeting_room m ON r.reservation_room_id = m.room_id " +
                "JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                "WHERE r.applicant_staff_id = ? ORDER BY r.created_at DESC";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return list;

            stm = con.prepareStatement(sql);
            stm.setLong(1, applicant_id);
            rs = stm.executeQuery();
            while (rs.next()) {
                ReservationList ri = new ReservationList();
                ri.setReservationID((long) rs.getDouble("reservation_id"));
                ri.setReservationNO(rs.getLong("reservation_no"));
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
            SqlUtil.closeAll(con, stm, rs);
        }
        return list;
    }

    public boolean cancelReservation(long reservation_id, long applicant_id) {
        String sql = "UPDATE reservation SET reservation_process = '已取消' " +
                "WHERE reservation_id = ? AND applicant_staff_id = ? AND reservation_process = '待确认'";
        Connection con = null;
        PreparedStatement stm = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;

            stm = con.prepareStatement(sql);
            stm.setLong(1, reservation_id);
            stm.setLong(2, applicant_id);
            return stm.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, stm, null);
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
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return list;

            stm = con.prepareStatement(sql);
            rs = stm.executeQuery();
            while (rs.next()) {
                ReservationList ri = new ReservationList();
                ri.setReservationID((long) rs.getDouble("reservation_id"));
                ri.setReservationNO(rs.getLong("reservation_no"));
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
            SqlUtil.closeAll(con, stm, rs);
        }
        return list;
    }

    public boolean processReservation(long reservation_id, long manager_id, String process, String comment) {
        String updateSql = "UPDATE reservation SET reservation_process = ? " +
                "WHERE reservation_id = ? AND reservation_process = '待确认'";
        String confirmSql = "INSERT INTO confirmation_log (reservation_id, confirmer_staff_id, confirm_process, confirm_comment) " +
                "VALUES (?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement stm1 = null;
        PreparedStatement stm2 = null;

        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            con.setAutoCommit(false);

            stm1 = con.prepareStatement(updateSql);
            stm1.setLong(2, reservation_id);
            int n = stm1.executeUpdate();
            if (n <= 0) {
                con.rollback();
                return false;
            }

            stm2 = con.prepareStatement(confirmSql);
            stm2.setLong(1, reservation_id);
            stm2.setDouble(2, manager_id);
            stm2.setString(3, process);
            stm2.setString(4, comment);
            stm2.executeUpdate();

            con.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm1, null);
            SqlUtil.closeAll(null, stm2, null);
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
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return c;

            stm = con.prepareStatement(sql);
            rs = stm.executeQuery();
            if (rs.next()) {
                c[0] = rs.getInt("total");
                c[1] = rs.getInt("pending");
                c[2] = rs.getInt("confirmed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return c;
    }
}