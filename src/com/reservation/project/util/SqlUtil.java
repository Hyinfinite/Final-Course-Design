package com.reservation.project.util;

import java.sql.*;

/**
 * SqlUtil类是一个用于管理数据库连接的工具类
 * 提供了获取数据库连接、关闭数据库资源等功能
 */
public class SqlUtil {
    // 数据库连接URL
    private static final String url = "jdbc:mysql://localhost:3306/conference_system";
    // 数据库用户名
    private static final String user = "root";
    // 数据库密码
    private static final String password = "@lolcxayjm!1";

    // 静态代码块，用于加载MySQL驱动
    static {
        try {
            // 加载MySQL JDBC驱动类
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // 如果驱动加载失败，打印错误信息
            System.out.println("SQL驱动加载失败" + e);
        }
    }

    /**
     * 获取数据库连接
     * @return 返回一个Connection对象，如果连接失败则返回null
     */
    public static Connection getConnection() {
        try {
            // 尝试获取数据库连接并返回
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            // 如果连接失败，打印错误信息并返回null
            System.out.println("数据库连接失败" + e);
            return null;
        }
    }

    /**
     * 关闭数据库资源
     * @param con 数据库连接对象
     * @param stm Statement对象
     * @param rs ResultSet对象
     */
    public static void closeAll(Connection con, Statement stm, ResultSet rs) {
        try {
            // 如果ResultSet对象不为空，则关闭它
            if (rs != null) {
                rs.close();
            }
            // 如果Statement对象不为空，则关闭它
            if (stm != null) {
                stm.close();
            }
            // 如果Connection对象不为空，则关闭它
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            // 如果关闭资源时发生异常，打印错误信息
            System.out.println("数据库关闭失败" + e);
        }
    }
}
