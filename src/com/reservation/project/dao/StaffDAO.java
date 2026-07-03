package com.reservation.project.dao;

import com.reservation.project.model.StaffInfo;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public List<StaffInfo> listAll() {
        List<StaffInfo> list = new ArrayList<StaffInfo>();
        String sql = "SELECT a.staff_id, a.staff_no, a.staff_name, a.dept_id, d.dept_name, a.gender, a.position, a.phone, a.access_level " +
                "FROM admin_staff a LEFT JOIN department d ON a.dept_id = d.dept_id ORDER BY a.staff_id";
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
                StaffInfo s = new StaffInfo();
                s.setStaffId(rs.getLong("staff_id"));
                s.setStaffNo(rs.getString("staff_no"));
                s.setStaffName(rs.getString("staff_name"));
                s.setDeptId(rs.getLong("dept_id"));
                s.setDeptName(rs.getString("dept_name"));
                s.setGender(rs.getString("gender"));
                s.setPosition(rs.getString("position"));
                s.setPhone(rs.getString("phone"));
                s.setAccessLevel(rs.getString("access_level"));
                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    public boolean addStaff(String staffNo, String staffName, long deptId, String gender,
                            String position, String phone, String accessLevel) {
        String sql = "INSERT INTO admin_staff(staff_no, staff_name, dept_id, staff_password, gender, position, phone, access_level) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, staffNo);
            ps.setString(2, staffName);
            ps.setLong(3, deptId);
            ps.setString(4, "123456"); // 默认密码
            ps.setString(5, gender);
            ps.setString(6, position);
            ps.setString(7, phone);
            ps.setString(8, accessLevel); // STAFF / ROOM_ADMIN / SYS_ADMIN
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // staff_no 重复
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public boolean updateStaffBasic(long staffId, String staffNo, String staffName, long deptId, String gender,
                                    String position, String phone) {
        String sql = "UPDATE admin_staff SET staff_no = ?, staff_name = ?, dept_id = ?, gender = ?, position = ?, phone = ? " +
                "WHERE staff_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, staffNo);
            ps.setString(2, staffName);
            ps.setLong(3, deptId);
            ps.setString(4, gender);
            ps.setString(5, position);
            ps.setString(6, phone);
            ps.setLong(7, staffId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public boolean resetPassword(long staffId, String newPwd) {
        String sql = "UPDATE admin_staff SET staff_password = ? WHERE staff_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, newPwd);
            ps.setLong(2, staffId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    // 指定行政人员为会议室管理员
    public boolean setRoomAdmin(long staffId) {
        String sql = "UPDATE admin_staff SET access_level = 'ROOM_ADMIN' WHERE staff_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setLong(1, staffId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public boolean updateOwnProfile(long staffId, String staffNo, String staffName, String gender, String position, String phone) {
        String sql = "UPDATE admin_staff SET staff_no = ?, staff_name = ?, gender = ?, position = ?, phone = ? " +
                "WHERE staff_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, staffNo);
            ps.setString(2, staffName);
            ps.setString(3, gender);
            ps.setString(4, position);
            ps.setString(5, phone);
            ps.setLong(6, staffId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public boolean changePassword(long staffId, String oldPwd, String newPwd) {
        String sql = "UPDATE admin_staff SET staff_password = ? WHERE staff_id = ? AND staff_password = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, newPwd);
            ps.setLong(2, staffId);
            ps.setString(3, oldPwd);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public StaffInfo findByID(long staffId) {
        String sql = "SELECT a.staff_id, a.staff_no, a.staff_name, a.dept_id, d.dept_name, a.gender, a.position, a.phone, a.access_level " +
                "FROM admin_staff a LEFT JOIN department d ON a.dept_id = d.dept_id WHERE a.staff_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return null;
            }
            ps = con.prepareStatement(sql);
            ps.setLong(1, staffId);
            rs = ps.executeQuery();
            if (rs.next()) {
                StaffInfo s = new StaffInfo();
                s.setStaffId(rs.getLong("staff_id"));
                s.setStaffNo(rs.getString("staff_no"));
                s.setStaffName(rs.getString("staff_name"));
                s.setDeptId(rs.getLong("dept_id"));
                s.setDeptName(rs.getString("dept_name"));
                s.setGender(rs.getString("gender"));
                s.setPosition(rs.getString("position"));
                s.setPhone(rs.getString("phone"));
                s.setAccessLevel(rs.getString("access_level"));
                return s;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return null;
    }

    // ========== 在 StaffDAO 类的末尾添加以下方法 ==========

    /**
     * 根据部门ID获取该部门所有行政人员的staff_id列表
     * @param deptId 部门ID
     * @return 员工ID列表
     */
    public List<Long> getStaffIdsByDept(long deptId) {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT staff_id FROM admin_staff WHERE dept_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return ids;
            ps = con.prepareStatement(sql);
            ps.setLong(1, deptId);
            rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getLong("staff_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return ids;
    }

    // 根据部门ID获取该部门所有员工信息
    public List<StaffInfo> getStaffByDept(long deptId) {
        List<StaffInfo> list = new ArrayList<>();
        String sql = "SELECT staff_id, staff_no, staff_name, dept_id, gender, position, phone, access_level " +
                "FROM admin_staff WHERE dept_id = ? ORDER BY staff_name";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) return list;
            ps = con.prepareStatement(sql);
            ps.setLong(1, deptId);
            rs = ps.executeQuery();
            while (rs.next()) {
                StaffInfo s = new StaffInfo();
                s.setStaffId(rs.getLong("staff_id"));
                s.setStaffNo(rs.getString("staff_no"));
                s.setStaffName(rs.getString("staff_name"));
                s.setDeptId(rs.getLong("dept_id"));
                s.setGender(rs.getString("gender"));
                s.setPosition(rs.getString("position"));
                s.setPhone(rs.getString("phone"));
                s.setAccessLevel(rs.getString("access_level"));
                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }
}