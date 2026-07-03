package com.reservation.project.dao;

import com.reservation.project.model.DeptMeetingStat;
import com.reservation.project.model.RoomUsageStat;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // month 格式：2026-07
    // 假设每个会议室每月可用总时长：30天 * 8小时 * 60分钟 = 14400（用于课程设计近似统计）
    public List<RoomUsageStat> roomUsageByMonth(String month) {
        List<RoomUsageStat> list = new ArrayList<RoomUsageStat>();
        String sql = "SELECT m.room_id , m.room_name, " +
                "COALESCE(SUM(TIMESTAMPDIFF(MINUTE, r.start_time, r.end_time)),0) AS used_minutes " +
                "FROM meeting_room m " +
                "LEFT JOIN reservation r ON m.room_id = r.reservation_room_id " +
                "AND r.reservation_process = '已确认' " +
                "AND DATE_FORMAT(r.start_time, '%Y-%m') = ? " +
                "GROUP BY m.room_id, m.room_name " +
                "ORDER BY m.room_id";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, month);
            rs = ps.executeQuery();
            while (rs.next()) {
                RoomUsageStat x = new RoomUsageStat();
                x.setRoomId(rs.getLong("room_id"));
                x.setRoomName(rs.getString("room_name"));
                double used = rs.getDouble("used_minutes");
                x.setUsedMinutes(used);
                x.setUsageRate(Math.round((used / 14400.0) * 10000.0) / 100.0); // 保留2位
                list.add(x);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    public List<DeptMeetingStat> deptMeetingCountByMonth(String month) {
        List<DeptMeetingStat> list = new ArrayList<DeptMeetingStat>();
        String sql = "SELECT d.dept_id, d.dept_name, COUNT(r.reservation_id) AS meeting_count " +
                "FROM department d " +
                "LEFT JOIN reservation r ON d.dept_id = r.apply_dept_id " +
                "AND r.reservation_process = '已确认' " +
                "AND DATE_FORMAT(r.start_time, '%Y-%m') = ? " +
                "GROUP BY d.dept_id, d.dept_name " +
                "ORDER BY meeting_count DESC,d.dept_id";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, month);
            rs = ps.executeQuery();
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
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }
}