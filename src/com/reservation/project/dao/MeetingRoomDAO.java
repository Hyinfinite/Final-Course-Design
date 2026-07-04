package com.reservation.project.dao;

import com.reservation.project.util.SqlUtil;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

/**
 * MeetingRoomDAO类，用于处理会议室相关的数据库操作
 * 包括获取所有会议室的选项信息、根据房间ID获取房间的容纳人数等功能
 */
public class MeetingRoomDAO {
    /**
     * 获取所有会议房间的选项信息，包括房间ID、名称和容纳人数
     * @return 包含房间信息的字符串列表，格式为"ID 名称(可容纳人数: 人数)"
     */
    public List<String> roomOption() {
        // 创建用于存储房间信息的列表
        List<String> Room = new ArrayList<String>();
        // SQL查询语句，获取所有房间的ID、名称和容量，并按ID排序
        String sql = "SELECT room_id, room_name, capacity FROM meeting_room ORDER BY room_id";
        // 声明数据库连接和操作对象
        Connection con = null;  // 数据库连接对象
        PreparedStatement ps = null;  // 预处理语句对象
        ResultSet rs = null;  // 结果集对象
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 执行查询
            rs = ps.executeQuery();
            // 遍历结果集，将每个房间的信息添加到列表中
            while (rs.next()) {
                long id = rs.getLong("room_id");  // 获取房间ID
                String name = rs.getString("room_name");  // 获取房间名称
                int capacity = rs.getInt("capacity");  // 获取房间容量
                // 格式化房间信息并添加到列表
                Room.add(id + " " + name + "(可容纳人数: " + capacity + ")");
            }
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        // 返回房间信息列表
        return Room;
    }

    /**
     * 根据房间ID获取房间的容纳人数
     * @param roomId 房间ID
     * @return 房间的容纳人数，如果未找到则返回-1
     */
    public int getCapacityByRoomId(long roomId) {
        // SQL查询语句，根据房间ID获取容量
        String sql = "SELECT capacity FROM meeting_room WHERE room_id = ?";
        // 声明数据库连接和操作对象
        Connection con = null;  // 数据库连接对象
        PreparedStatement ps = null;  // 预处理语句对象
        ResultSet rs = null;  // 结果集对象
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数（房间ID）
            ps.setLong(1, roomId);
            // 执行查询
            rs = ps.executeQuery();
            // 如果查询结果有下一行，返回容量值
            if (rs.next()) {
                return rs.getInt("capacity");  // 获取并返回房间容量
            }
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        // 如果未找到房间，返回-1
        return -1;
    }
}
