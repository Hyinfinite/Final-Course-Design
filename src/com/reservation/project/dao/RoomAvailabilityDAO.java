package com.reservation.project.dao;

import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 行政人员：按日期查询会议室空闲状态
 */
public class RoomAvailabilityDAO {

    /**
     * dateStr: yyyy-MM-dd
     * 返回字段拼装为字符串，便于直接展示在列表/表格
     */
    public List<String[]> queryByDate(String dateStr) {
        List<String[]> list = new ArrayList<String[]>();

        String sql = "SELECT m.room_id, m.room_name, m.location, m.capacity," +
                "CASE WHEN EXISTS (" +
                "   SELECT 1 FROM reservation r " +
                "   WHERE r.reservation_room_id = m.room_id " +
                "   AND r.reservation_process IN ('待确认','已确认') " +
                "   AND DATE(r.start_time) = ?" +
                ") THEN '忙碌' ELSE '空闲' END AS status " +
                "FROM meeting_room m ORDER BY m.room_id";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, dateStr);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                        String.valueOf(rs.getLong("room_id")),
                        rs.getString("room_name"),
                        rs.getString("location"),
                        String.valueOf(rs.getInt("capacity")),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }
}