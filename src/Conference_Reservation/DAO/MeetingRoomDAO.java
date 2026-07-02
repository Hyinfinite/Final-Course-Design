package Conference_Reservation.DAO;

import Conference_Reservation.Util.SqlUtil;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class MeetingRoomDAO {
    public List<String> RoomOption() {
        List<String> Room = new ArrayList<String>();
        String sql = "SELECT room_id, room_name, capacity FROM meeting_room ORDER BY room_id";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            stm = con.prepareStatement(sql);
            rs = stm.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("room_id");
                String name = rs.getString("room_name");
                int capacity = rs.getInt("capacity");
                Room.add(id + " " + name + "(可容纳人数: " + capacity + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return Room;
    }

    public int getCapacityByRoomId(long roomId) {
        String sql = "SELECT capacity FROM meeting_room WHERE room_id = ?";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            stm = con.prepareStatement(sql);
            stm.setLong(1, roomId);
            rs = stm.executeQuery();
            if (rs.next()) {
                return rs.getInt("capacity");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return -1;
    }
}
