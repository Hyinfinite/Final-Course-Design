package com.reservation.project.dao;

import com.reservation.project.model.MeetingRoom;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MeetingRoomManageDAO类是一个用于管理会议室数据访问对象(Data Access Object)的类
 * 该类提供了会议室的增删改查等基本操作，使用JDBC与数据库进行交互
 */
public class MeetingRoomManageDAO {

    /**
     * 查询所有会议室信息
     * @return 返回包含所有会议室信息的List集合，如果没有数据则返回空List
     */
    public List<MeetingRoom> listAll() {
        // 创建一个空的会议室列表
        List<MeetingRoom> list = new ArrayList<MeetingRoom>();
        // 定义SQL查询语句，按room_id排序
        String sql = "SELECT room_id, room_code, room_name, location, capacity, has_projector, has_audio " +
                "FROM meeting_room ORDER BY room_id";
        // 初始化数据库连接和相关对象
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
            // 遍历结果集，将数据封装到MeetingRoom对象中
            while (rs.next()) {
                MeetingRoom m = new MeetingRoom();
                m.setRoomId(rs.getLong("room_id"));
                m.setRoomCode(rs.getString("room_code"));
                m.setRoomName(rs.getString("room_name"));
                m.setLocation(rs.getString("location"));
                m.setCapacity(rs.getInt("capacity"));
                m.setHasProjector(rs.getInt("has_projector"));
                m.setHasAudio(rs.getInt("has_audio"));
                list.add(m);
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
     * 添加新会议室
     * @param roomCode 会议室代码
     * @param roomName 会议室名称
     * @param location 会议室位置
     * @param capacity 会议室容量
     * @param hasProjector 是否有投影仪(1表示有，0表示没有)
     * @param hasAudio 是否有音响设备(1表示有，0表示没有)
     * @return 添加成功返回true，失败返回false
     */
    public boolean addRoom(String roomCode, String roomName, String location, int capacity, int hasProjector, int hasAudio) {
        // 定义SQL插入语句
        String sql = "INSERT INTO meeting_room(room_code, room_name, location, capacity, has_projector, has_audio) " +
                "VALUES(?,?,?,?,?,?)";
        // 初始化数据库连接和预处理语句
        Connection con = null;
        PreparedStatement ps = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数
            ps.setString(1, roomCode);
            ps.setString(2, roomName);
            ps.setString(3, location);
            ps.setInt(4, capacity);
            ps.setInt(5, hasProjector);
            ps.setInt(6, hasAudio);
            // 执行更新，如果影响行数大于0则返回true
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            // 处理唯一约束冲突异常
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭数据库资源
            SqlUtil.closeAll(con, ps, null);
        }
    }

    /**
     * 更新会议室信息
     * @param roomId 会议室ID
     * @param roomCode 会议室代码
     * @param roomName 会议室名称
     * @param location 会议室位置
     * @param capacity 会议室容量
     * @param hasProjector 是否有投影仪(1表示有，0表示没有)
     * @param hasAudio 是否有音响设备(1表示有，0表示没有)
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateRoom(long roomId, String roomCode, String roomName, String location, int capacity, int hasProjector, int hasAudio) {
        // 定义SQL更新语句
        String sql = "UPDATE meeting_room SET room_code = ?, room_name = ?, location = ?, capacity = ?, has_projector = ?, has_audio = ? " +
                "WHERE room_id = ?";
        // 初始化数据库连接和预处理语句
        Connection con = null;
        PreparedStatement ps = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数
            ps.setString(1, roomCode);
            ps.setString(2, roomName);
            ps.setString(3, location);
            ps.setInt(4, capacity);
            ps.setInt(5, hasProjector);
            ps.setInt(6, hasAudio);
            ps.setLong(7, roomId);
            // 执行更新，如果影响行数大于0则返回true
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            // 处理唯一约束冲突异常
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭数据库资源
            SqlUtil.closeAll(con, ps, null);
        }
    }

    /**
     * 删除会议室
     * @param roomId 要删除的会议室ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteRoom(long roomId) {
        // 定义SQL删除语句
        String sql = "DELETE FROM meeting_room WHERE room_id = ?";
        // 初始化数据库连接和预处理语句
        Connection con = null;
        PreparedStatement ps = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置参数
            ps.setLong(1, roomId);
            // 执行更新，如果影响行数大于0则返回true
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            // 处理外键约束冲突异常
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            // 关闭数据库资源
            SqlUtil.closeAll(con, ps, null);
        }
    }
}