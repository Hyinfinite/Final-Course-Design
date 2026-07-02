package Conference_Reservation.DAO;

import Conference_Reservation.Model.Department;
import Conference_Reservation.Util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> listAll() {
        List<Department> list = new ArrayList<Department>();
        String sql = "SELECT dept_id, dept_name FROM department ORDER BY dept_id";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return list;
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Department d = new Department();
                d.setDeptID(rs.getLong("dept_id"));
                d.setDeptName(rs.getString("dept_name"));
                list.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    public boolean addDepartment(String deptName) {
        String sql = "INSERT INTO department(dept_name) VALUES(?)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            ps = con.prepareStatement(sql);
            ps.setString(1, deptName);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // 重名
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public boolean updateDepartment(long deptId, String deptName) {
        String sql = "UPDATE department SET dept_name=? WHERE dept_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            ps = con.prepareStatement(sql);
            ps.setString(1, deptName);
            ps.setLong(2, deptId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public boolean deleteDepartment(long deptId) {
        String sql = "DELETE FROM department WHERE dept_id=?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return false;
            ps = con.prepareStatement(sql);
            ps.setLong(1, deptId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            // 有外键引用时会失败
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }
}