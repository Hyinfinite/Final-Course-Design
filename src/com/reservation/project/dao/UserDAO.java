package com.reservation.project.dao;

import com.reservation.project.model.User;
import com.reservation.project.util.SqlUtil;

import java.sql.*;

/**
 * 用户数据访问对象类，用于处理用户相关的数据库操作
 * 提供用户登录验证功能
 */
public class UserDAO {
    /**
     * 用户登录方法
     * @param staffNo 员工工号
     * @param password 密码
     * @param accessLevel 访问权限级别
     * @return 如果验证成功返回User对象，否则返回null
     */
    public User Login(String staffNo, String password, String accessLevel) {
        // SQL查询语句，用于验证用户登录信息
        String sql = "SELECT staff_id, staff_no, staff_name, dept_id, access_level FROM admin_staff " +
                     "WHERE staff_no = ? AND staff_password = ? AND access_Level = ?";
        Connection con = null;    // 数据库连接对象
        PreparedStatement ps = null; // 预处理语句对象
        ResultSet rs = null;     // 结果集对象
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数：员工工号
            ps.setString(1, staffNo);
            // 设置参数：密码
            ps.setString(2, password);
            // 设置参数：访问权限级别
            ps.setString(3, accessLevel);

            // 执行查询
            rs = ps.executeQuery();
            // 如果查询结果有数据，则创建并返回User对象
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
            // 打印异常堆栈信息
            e.printStackTrace();
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        // 登录失败返回null
        return null;
    }
}
