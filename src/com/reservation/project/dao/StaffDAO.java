package com.reservation.project.dao;

import com.reservation.project.model.StaffInfo;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * StaffDAO类，用于处理员工相关数据库操作
 */
public class StaffDAO {

    /**
     * 获取所有员工信息列表
     * @return 返回包含所有员工信息的List集合
     */
    public List<StaffInfo> listAll() {
        List<StaffInfo> list = new ArrayList<StaffInfo>();
        // SQL查询语句，获取员工信息并关联部门表
        String sql = "SELECT a.staff_id, a.staff_no, a.staff_name, a.dept_id, d.dept_name, a.gender, a.position, a.phone, a.access_level " +
                "FROM admin_staff a LEFT JOIN department d ON a.dept_id = d.dept_id ORDER BY a.staff_id";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 执行查询
            rs = ps.executeQuery();
            // 遍历结果集
            while (rs.next()) {
                StaffInfo s = new StaffInfo();
                // 设置员工信息
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
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    /**
     * 添加新员工
     * @param staffNo 员工编号
     * @param staffName 员工姓名
     * @param deptId 部门ID
     * @param gender 性别
     * @param position 职位
     * @param phone 电话
     * @param accessLevel 访问级别
     * @return 添加成功返回true，失败返回false
     */
    public boolean addStaff(String staffNo, String staffName, long deptId, String gender,
                            String position, String phone, String accessLevel) {
        // SQL插入语句
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
            // 设置参数
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

    /**
     * 更新员工基本信息
     * @param staffId 员工ID
     * @param staffNo 员工编号
     * @param staffName 员工姓名
     * @param deptId 部门ID
     * @param gender 性别
     * @param position 职位
     * @param phone 电话
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateStaffBasic(long staffId, String staffNo, String staffName, long deptId, String gender,
                                    String position, String phone) {
        // SQL更新语句
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
            // 设置参数
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

    /**
     * 重置员工密码
     * @param staffId 员工ID
     * @param newPwd 新密码
     * @return 重置成功返回true，失败返回false
     */
    public boolean resetPassword(long staffId, String newPwd) {
        // SQL更新语句
        String sql = "UPDATE admin_staff SET staff_password = ? WHERE staff_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            // 设置参数
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

    /**
     * 设置员工为房间管理员
     * @param staffId 员工ID
     * @return 设置成功返回true，失败返回false
     */
    public boolean setRoomAdmin(long staffId) {
        // SQL更新语句，将员工的访问级别设置为'ROOM_ADMIN'
        String sql = "UPDATE admin_staff SET access_level = 'ROOM_ADMIN' WHERE staff_id = ?";
        Connection con = null;  // 声明数据库连接对象
        PreparedStatement ps = null;  // 声明预处理语句对象
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 检查连接是否成功获取
            if (con == null) {
                return false;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数，staffId对应SQL语句中的问号占位符
            ps.setLong(1, staffId);
            // 执行更新操作，如果影响行数大于0则返回true
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
            return false;
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, null);
        }
    }

/**
 * 更新员工个人资料信息的方法
 * @param staffId 员工ID
 * @param staffNo 员工工号
 * @param staffName 员工姓名
 * @param gender 性别
 * @param position 职位
 * @param phone 电话号码
 * @return 更新成功返回true，失败返回false
 */
    public boolean updateOwnProfile(long staffId, String staffNo, String staffName, String gender, String position, String phone) {
    // SQL更新语句，用于更新admin_staff表中的员工信息
        String sql = "UPDATE admin_staff SET staff_no = ?, staff_name = ?, gender = ?, position = ?, phone = ? " +
                "WHERE staff_id = ?";
        Connection con = null; // 数据库连接对象
        PreparedStatement ps = null; // 预处理语句对象
        try {
        // 获取数据库连接
            con = SqlUtil.getConnection();
        // 检查连接是否成功
            if (con == null) {
                return false;
            }
        // 创建预处理语句
            ps = con.prepareStatement(sql);

        // 设置预处理语句的参数，依次对应SQL语句中的问号占位符
            ps.setString(1, staffNo);    // 设置员工工号
            ps.setString(2, staffName);  // 设置员工姓名
            ps.setString(3, gender);     // 设置性别
            ps.setString(4, position);   // 设置职位
            ps.setString(5, phone);      // 设置电话号码
            ps.setLong(6, staffId);      // 设置员工ID
        // 执行更新操作，如果影响的行数大于0则返回true
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
        // 打印异常信息
            e.printStackTrace();
            return false;
        } finally {
        // 关闭所有数据库资源，包括连接、预处理语句和结果集
            SqlUtil.closeAll(con, ps, null);
        }
    }

/**
 * 修改管理员密码的方法
 * @param staffId 管理员ID
 * @param oldPwd 原密码
 * @param newPwd 新密码
 * @return 密码修改成功返回true，失败返回false
 */
    public boolean changePassword(long staffId, String oldPwd, String newPwd) {
    // SQL更新语句，用于修改管理员密码
        String sql = "UPDATE admin_staff SET staff_password = ? WHERE staff_id = ? AND staff_password = ?";
        Connection con = null;  // 数据库连接对象
        PreparedStatement ps = null;  // 预处理语句对象
        try {
        // 获取数据库连接
            con = SqlUtil.getConnection();
        // 检查连接是否成功
            if (con == null) {
                return false;
            }
        // 创建预处理语句
            ps = con.prepareStatement(sql);
        // 设置参数：新密码
            ps.setString(1, newPwd);
        // 设置参数：管理员ID
            ps.setLong(2, staffId);
        // 设置参数：原密码
            ps.setString(3, oldPwd);
        // 执行更新操作，如果影响行数大于0则返回true
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
        // 打印异常信息
            e.printStackTrace();
            return false;
        } finally {
        // 关闭所有资源
            SqlUtil.closeAll(con, ps, null);
        }
    }

/**
 * 根据员工ID查询员工信息
 * @param staffId 员工ID
 * @return StaffInfo 员工信息对象，如果未找到则返回null
 */
    public StaffInfo findByID(long staffId) {
    // SQL查询语句，从admin_staff表左连接department表，查询指定员工ID的员工信息
        String sql = "SELECT a.staff_id, a.staff_no, a.staff_name, a.dept_id, d.dept_name, a.gender, a.position, a.phone, a.access_level " +
                "FROM admin_staff a LEFT JOIN department d ON a.dept_id = d.dept_id WHERE a.staff_id = ?";
    // 初始化数据库连接、预处理结果集和结果集变量
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return null;
            }
        // 创建预处理语句
            ps = con.prepareStatement(sql);
        // 设置查询参数（员工ID）
            ps.setLong(1, staffId);
        // 执行查询
            rs = ps.executeQuery();
        // 如果查询结果有下一行，则创建StaffInfo对象并设置属性
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
        // 打印异常堆栈信息
            e.printStackTrace();
        } finally {
        // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
    // 如果未找到员工信息，返回null
        return null;
    }


/**
 * 根据部门ID获取该部门下的所有员工信息
 * @param deptId 部门ID
 * @return 返回包含该部门所有员工信息的List集合，如果没有数据则返回空List
 */
    public List<StaffInfo> getStaffByDept(long deptId) {
    // 创建一个ArrayList用于存放查询结果
        List<StaffInfo> list = new ArrayList<>();
    // 定义SQL查询语句，查询指定部门的员工信息，并按姓名排序
        String sql = "SELECT staff_id, staff_no, staff_name, dept_id, gender, position, phone, access_level " +
                "FROM admin_staff WHERE dept_id = ? ORDER BY staff_name";
    // 声明数据库连接、预处理结果集和结果集变量
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        // 获取数据库连接
            con = SqlUtil.getConnection();
        // 如果连接获取失败则直接返回空列表
            if (con == null) return list;
        // 创建预处理语句
            ps = con.prepareStatement(sql);
        // 设置查询参数（部门ID）
            ps.setLong(1, deptId);
        // 执行查询
            rs = ps.executeQuery();
        // 遍历结果集，将每条记录封装为StaffInfo对象并添加到列表中
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
        // 打印异常堆栈信息
            e.printStackTrace();
        } finally {
        // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
    // 返回查询结果列表
        return list;
    }
}