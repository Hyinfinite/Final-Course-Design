package com.reservation.project.dao;

import com.reservation.project.model.DeptMeetingStat;
import com.reservation.project.model.RoomUsageStat;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 报告数据访问对象，用于生成各类统计报告
 */
public class ReportDAO {

    /**
     * 按月份查询会议室使用情况统计
     * @param yearMonth 年份与月份，格式为"yyyy-MM"
     * @return 会议室使用统计列表，每个统计包含会议室ID、名称、使用次数和使用率
     */
    public List<RoomUsageStat> roomUsageByMonth(String yearMonth) {
        // 初始化返回结果列表
        List<RoomUsageStat> list = new ArrayList<RoomUsageStat>();
        // SQL查询语句：统计各会议室在指定月份的使用情况
        String sql = "SELECT m.room_id , m.room_name, " +
                "COUNT(r.reservation_id) AS used_count, " +
                "(SELECT COUNT(*) FROM reservation " +
                "WHERE reservation_process = '已确认' " +
                "AND DATE_FORMAT(start_time, '%Y-%m') = ?) AS total_count " +
                "FROM meeting_room m " +
                "LEFT JOIN reservation r ON m.room_id = r.reservation_room_id " +
                "AND r.reservation_process = '已确认' " +
                "AND DATE_FORMAT(r.start_time, '%Y-%m') = ? " +
                "GROUP BY m.room_id, m.room_name " +
                "ORDER BY m.room_id";
        // 数据库相关对象
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            // 准备SQL语句
            ps = con.prepareStatement(sql);
            ps.setString(1, yearMonth);
            ps.setString(2, yearMonth);
            // 执行查询
            rs = ps.executeQuery();
            // 处理查询结果
            while (rs.next()) {
                RoomUsageStat x = new RoomUsageStat();
                x.setRoomId(rs.getLong("room_id"));
                x.setRoomName(rs.getString("room_name"));
                long usedCount = rs.getLong("used_count");
                long totalCount = rs.getLong("total_count");
                x.setUsedCount(usedCount);
                // 计算使用率，保留2位小数
                double usageRate = (double) usedCount / totalCount;
                x.setUsageRate(Math.round(usageRate * 100.0));
                list.add(x);
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
     * 按月份查询各部门会议数量统计
     * @param yearMonth 年份与月份，格式为"yyyy-MM"
     * @return 部门会议统计列表，每个统计包含部门ID、名称和会议数量
     */
    public List<DeptMeetingStat> deptMeetingCountByMonth(String yearMonth) {
        // 初始化返回结果列表
        List<DeptMeetingStat> list = new ArrayList<DeptMeetingStat>();
        // SQL查询语句：统计各部门在指定月份的会议数量
        String sql = "SELECT d.dept_id, d.dept_name, COUNT(r.reservation_id) AS meeting_count " +
                "FROM department d " +
                "LEFT JOIN reservation r ON d.dept_id = r.apply_dept_id " +
                "AND r.reservation_process = '已确认' " +
                "AND DATE_FORMAT(r.start_time, '%Y-%m') = ? " +
                "GROUP BY d.dept_id, d.dept_name " +
                "ORDER BY meeting_count DESC,d.dept_id";
        // 数据库相关对象
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            // 准备SQL语句
            ps = con.prepareStatement(sql);
            ps.setString(1, yearMonth);
            // 执行查询
            rs = ps.executeQuery();
            // 处理查询结果
            while (rs.next()) {
                DeptMeetingStat x = new DeptMeetingStat();
                x.setDeptId(rs.getLong("dept_id"));
                x.setDeptName(rs.getString("dept_name"));
                x.setMeetingCount(rs.getInt("meeting_count"));
                list.add(x);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }
}