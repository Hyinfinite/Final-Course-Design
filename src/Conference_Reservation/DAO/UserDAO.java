package Conference_Reservation.DAO;

import Conference_Reservation.Model.User;
import Conference_Reservation.Util.SqlUtil;

import java.sql.*;

public class UserDAO {
    public User Login(String staffNo, String password, String accessLevel) {
        String sql = "SELECT staff_id, staff_no, staff_name, dept_id, access_level FROM admin_staff WHERE staffNo = ? AND password = ? AND accessLevel = ?";
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            stm = con.prepareStatement(sql);
            rs = stm.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setStaffID(rs.getDouble("staff_id"));
                u.setStaffNO(rs.getString("staff_no"));
                u.setStaffName(rs.getString("staff_name"));
                u.setDeptID(rs.getDouble("dept_id"));
                u.setAccessLevel(rs.getString("access_level"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, stm, rs);
        }
        return null;
    }
}
