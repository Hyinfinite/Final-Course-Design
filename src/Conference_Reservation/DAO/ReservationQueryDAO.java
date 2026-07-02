package Conference_Reservation.DAO;

import Conference_Reservation.Model.ReservationList;
import Conference_Reservation.Util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 给系统管理员用：按日期/部门/会议室筛选全部预约
 */
public class ReservationQueryDAO {

    // dateStr: yyyy-MM-dd，可为空字符串
    // deptId/roomId: 0 表示不过滤
    public List<ReservationList> queryAll(String dateStr, long deptId, long roomId) {
        List<ReservationList> list = new ArrayList<ReservationList>();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name,");
        sb.append("r.start_time, r.end_time, r.reservation_process, a.staff_name ");
        sb.append("FROM reservation r ");
        sb.append("JOIN meeting_room m ON r.reservation_room_id = m.room_id ");
        sb.append("JOIN admin_staff a ON r.applicant_staff_id = a.staff_id ");
        sb.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<Object>();

        if (dateStr != null && dateStr.trim().length() > 0) {
            sb.append("AND DATE(r.start_time)=? ");
            params.add(dateStr.trim());
        }
        if (deptId > 0) {
            sb.append("AND r.apply_dept_id=? ");
            params.add(deptId);
        }
        if (roomId > 0) {
            sb.append("AND r.reservation_room_id = ? ");
            params.add(roomId);
        }

        sb.append("ORDER BY r.created_at DESC");

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return list;
            ps = con.prepareStatement(sb.toString());

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                ReservationList r = new ReservationList();
                r.setReservationID(rs.getLong("reservation_id"));
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