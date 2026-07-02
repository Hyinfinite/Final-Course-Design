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
        Statement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            stm = con.createStatement();
            rs = stm.executeQuery(sql);
            while (rs.next()) {
                Room.add(rs.getDouble("room_id") + ": " + rs.getString("room_name") + "可容纳人数: " + rs.getInt("capacity"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return Room;
    }
}
