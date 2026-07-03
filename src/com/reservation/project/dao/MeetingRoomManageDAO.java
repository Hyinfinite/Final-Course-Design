package com.reservation.project.dao;

import com.reservation.project.model.MeetingRoom;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeetingRoomManageDAO {

    public List<MeetingRoom> listAll() {
        List<MeetingRoom> list = new ArrayList<MeetingRoom>();
        String sql = "SELECT room_id, room_code, room_name, location, capacity, has_projector, has_audio " +
                "FROM meeting_room ORDER BY room_id";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return list;
            }
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
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
            SqlUtil.closeAll(con, ps, rs);
        }
        return list;
    }

    public boolean addRoom(String roomCode, String roomName, String location, int capacity, int hasProjector, int hasAudio) {
        String sql = "INSERT INTO meeting_room(room_code, room_name, location, capacity, has_projector, has_audio) " +
                "VALUES(?,?,?,?,?,?)";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, roomCode);
            ps.setString(2, roomName);
            ps.setString(3, location);
            ps.setInt(4, capacity);
            ps.setInt(5, hasProjector);
            ps.setInt(6, hasAudio);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public boolean updateRoom(long roomId, String roomCode, String roomName, String location, int capacity, int hasProjector, int hasAudio) {
        String sql = "UPDATE meeting_room SET room_code = ?, room_name = ?, location = ?, capacity = ?, has_projector = ?, has_audio = ? " +
                "WHERE room_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setString(1, roomCode);
            ps.setString(2, roomName);
            ps.setString(3, location);
            ps.setInt(4, capacity);
            ps.setInt(5, hasProjector);
            ps.setInt(6, hasAudio);
            ps.setLong(7, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }

    public boolean deleteRoom(long roomId) {
        String sql = "DELETE FROM meeting_room WHERE room_id = ?";
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = SqlUtil.getConnection();
            if (con == null) {
                return false;
            }
            ps = con.prepareStatement(sql);
            ps.setLong(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con, ps, null);
        }
    }
}