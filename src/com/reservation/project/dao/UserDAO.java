package com.reservation.project.dao;

import com.reservation.project.model.User;
import com.reservation.project.util.SqlUtil;

import java.sql.*;

public class UserDAO {
    public User Login(String staffNo, String password, String accessLevel) {
        String sql = "SELECT staff_id, staff_no, staff_name, dept_id, access_level FROM admin_staff " +
                     "WHERE staff_no = ? AND staff_password = ? AND access_Level = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            ps = con.prepareStatement(sql);
            ps.setString(1, staffNo);
            ps.setString(2, password);
            ps.setString(3, accessLevel);

            rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setStaffId(rs.getLong("staff_id"));
                u.setStaffNO(rs.getString("staff_no"));
                u.setStaffName(rs.getString("staff_name"));
                u.setDeptId(rs.getLong("dept_id"));
                u.setAccessLevel(rs.getString("access_level"));
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return null;
    }
}
