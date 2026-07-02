package Conference_Reservation.DAO;

import Conference_Reservation.Model.ReservationList;
import Conference_Reservation.Util.ReservationNOUtil;
import Conference_Reservation.Util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    public boolean hasConflict(long room_id, Timestamp start_time, Timestamp end_time) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE room_id = ? AND ((start_time < ? AND end_time > ?) AND process IN ('待确认', '已确认')";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            stm = con.prepareStatement(sql);
            stm.setLong(1, room_id);
            stm.setTimestamp(2, start_time);
            stm.setTimestamp(3, end_time);
            rs = stm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return true;
    }

    public boolean addReservation(String topic, long dept_id, long applicant_id, long room_id, Timestamp start_time, Timestamp end_time, int count, String desc) {
        String sql = "INSERT INTO reservation (reservation_no, meeting_topic, apply_dept_id, applicant_staff_id, reservation_room_id, start_time, end_time, participant_count, meeting_desc, reservation_process) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, '待确认')";
        Connection con = null;
        PreparedStatement stm = null;
        try {
            con = SqlUtil.getConnection();
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
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, r.room_name, r.start_time, r.end_time, r.reservation_process, a.staff_name " +
                     "FROM reservation r JOIN meeting_room m ON r.room_id = m.room_id JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                     "WHERE r.applicant_staff_id = ? ORDER BY r.created_at desc";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            stm = con.prepareStatement(sql);
            stm.setDouble(1, applicant_id);
            rs = stm.executeQuery();
            while (rs.next()) {
                ReservationList reservationInfo = new ReservationList();
                reservationInfo.setReservationID(rs.getLong("reservation_id"));
                reservationInfo.setReservationNO(rs.getLong("reservation_no"));
                reservationInfo.setMeetingTopic(rs.getString("meeting_topic"));
                reservationInfo.setRoomName(rs.getString("room_name"));
                reservationInfo.setStartTime(rs.getString("start_time"));
                reservationInfo.setEndTime(rs.getString("end_time"));
                reservationInfo.setProcess(rs.getString("reservation_process"));
                reservationInfo.setApplicantName(rs.getString("staff_name"));
                list.add(reservationInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return list;
    }

    public boolean cancelReservation(long reservation_id, long applicant_id) {
        String sql = "UPDATE reservation SET reservation_process = '已取消' WHERE reservation_id = ? AND applicant_staff_id = ? AND reservation_process = '待确认'";
        Connection con = null;
        PreparedStatement stm = null;
        try {
            con = SqlUtil.getConnection();
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
        String sql = "SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name, r.start_time, r.end_time, r.reservation_process, a.staff_name " +
                     "FROM reservation r JOIN meeting_room m ON r.room_id = m.room_id JOIN admin_staff a ON r.applicant_staff_id = a.staff_id " +
                     "WHERE r.reservation_process = '待确认' ORDER BY r.created_at desc";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            stm = con.prepareStatement(sql);
            rs = stm.executeQuery();
            while (rs.next()) {
                ReservationList reservationInfo = new ReservationList();
                reservationInfo.setReservationID(rs.getLong("reservation_id"));
                reservationInfo.setReservationNO(rs.getLong("reservation_no"));
                reservationInfo.setMeetingTopic(rs.getString("meeting_topic"));
                reservationInfo.setRoomName(rs.getString("room_name"));
                reservationInfo.setStartTime(rs.getString("start_time"));
                reservationInfo.setEndTime(rs.getString("end_time"));
                reservationInfo.setProcess(rs.getString("reservation_process"));
                reservationInfo.setApplicantName(rs.getString("staff_name"));
                list.add(reservationInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return list;
    }

    public boolean processReservation(double reservation_id, double manager_id, String process, String comment) {
        String UpdateSql = "UPDATE reservation SET reservation_process = ? WHERE reservation_id = ? AND reservation_process = '待确认'";
        Connection con = null;
        PreparedStatement stm1 = null;
        try {
            con = SqlUtil.getConnection();
            stm1 = con.prepareStatement(UpdateSql);
            stm1.setString(1, process);
            stm1.setDouble(2, reservation_id);
            int rs = stm1.executeUpdate();
            if (rs <= 0) {
                return false;
            }

            String ConfirmSql = "INSERT INTO confirmation_log (reservation_id, confirmer_staff_id, confirm_process, confirm_comment) VALUES (?, ?, ?, ?)";
            PreparedStatement stm2 = con.prepareStatement(ConfirmSql);
            stm2.setDouble(1, reservation_id);
            stm2.setDouble(2, manager_id);
            stm2.setString(3, process);
            stm2.setString(4, comment);
            stm2.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, stm1, null);
        }
    }

    public int[] processCount() {
        int[] c = new int[3];
        String sql = "SELECT COUNT(*) total " +
                     "SUM(CASE WHEN reservation_process = '待确认' THEN 1 ELSE 0 END) pending, " +
                     "SUM(CASE WHEN reservation_process = '已确认' THEN 1 ELSE 0 END) confirmed";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
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
