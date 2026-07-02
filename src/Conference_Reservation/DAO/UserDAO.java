package Conference_Reservation.DAO;

import Conference_Reservation.Model.User;
import Conference_Reservation.Util.SqlUtil;

import java.sql.*;

public class UserDAO {
    public User Login(String staffNo, String password, String accessLevel) {
        String sql = "SELECT staff_id, staff_no, staff_name, dept_id, access_level FROM admin_staff WHERE staffNo = ? AND password = ? AND accessLevel = ?";
        Connection con = null;
        Statement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            stm = con.createStatement();
            rs = stm.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return null;
    }
}
