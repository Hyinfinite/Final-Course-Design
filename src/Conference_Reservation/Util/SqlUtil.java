package Conference_Reservation.Util;

import java.sql.*;

public class SqlUtil {
    private static final String url = "jdbc:mysql://localhost:3306/Conference_reservation";
    private static final String user = "root";
    private static final String password = "@lolcxayjm!1";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("SQL驱动加载失败" + e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("数据库连接失败" + e);
            return null;
        }
    }

    public static void closeAll(Connection con, Statement stm, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stm != null) {
                stm.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            System.out.println("数据库关闭失败" + e);
        }
    }
}
