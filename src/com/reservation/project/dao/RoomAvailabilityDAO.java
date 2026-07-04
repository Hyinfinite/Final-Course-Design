package com.reservation.project.dao;

import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 行政人员：按日期查询会议室空闲状态
 * 该类用于查询会议室在指定日期的可用性状态，返回会议室的基本信息和状态
 */
public class RoomAvailabilityDAO {

    /**
     * dateStr: yyyy-MM-dd
     * 返回字段拼装为字符串，便于直接展示在列表/表格
     */
    public List<String[]> queryByDate(String dateStr) {
    // 创建一个String类型的列表，用于存储查询结果
        List<String[]> list = new ArrayList<String[]>();

    // SQL查询语句，查询会议室信息并根据预订状态判断忙碌或空闲
        String sql = "SELECT m.room_id, m.room_name, m.location, m.capacity," +
                "CASE WHEN EXISTS (" +
                "   SELECT 1 FROM reservation r " +
                "   WHERE r.reservation_room_id = m.room_id " +
                "   AND r.reservation_process IN ('待确认','已确认') " +
                "   AND DATE(r.start_time) = ?" +
                ") THEN '忙碌' ELSE '空闲' END AS status " +
                "FROM meeting_room m ORDER BY m.room_id";

    // 初始化数据库连接对象和预处理结果集
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        // 获取数据库连接
            con = SqlUtil.getConnection();
        // 如果连接获取失败，直接返回空列表
            if (con == null) {
                return list;
            }
        // 创建预处理语句
            ps = con.prepareStatement(sql);
        // 设置查询参数
            ps.setString(1, dateStr);
        // 执行查询
            rs = ps.executeQuery();
        // 遍历结果集，将每条记录转换为字符串数组并添加到列表中
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