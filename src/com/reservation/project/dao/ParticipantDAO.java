package com.reservation.project.dao;

import com.reservation.project.model.Participant;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * 参会人员数据访问对象(DAO)，用于处理参会人员相关的数据库操作
 * 该类提供了参会人员签到、批量签到和查询参会人员信息等功能
 */
public class ParticipantDAO {

    /**
     * 参会人员签到方法
     * @param participantId 参会人员ID
     * @param operatorId 操作者ID
     * @return 签到成功返回true，否则返回false
     */
    public boolean signIn(long participantId, long operatorId) {
        // 首先检查是否已经签到
        String checkSql = "SELECT sign_in_process FROM participant WHERE participant_id = ?";
        Connection con1 = null;
        PreparedStatement ps1 = null;
        try {
            con1 = SqlUtil.getConnection();
            if (con1 == null) {
                return false;
            }
            ps1 = con1.prepareStatement(checkSql);
            ps1.setLong(1, participantId);
            ResultSet rs = ps1.executeQuery();
            if (rs.next() && "已签到".equals(rs.getString("sign_in_process"))) {
                return false; // 已经签到
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con1, ps1, null);
        }

        // 检查会议是否已经开始
        String checkTimeSql = "SELECT start_time FROM participant p " +
                "JOIN reservation r ON p.reservation_id = r.reservation_id " +
                "WHERE p.participant_id = ?";
        Connection con2 = null;
        PreparedStatement ps2 = null;
        try {
            con2 = SqlUtil.getConnection();
            if (con2 == null) {
                return false;
            }
            ps2 = con2.prepareStatement(checkTimeSql);
            ps2.setLong(1, participantId);
            ResultSet rs = ps2.executeQuery();
            if (rs.next()) {
                Timestamp startTime = rs.getTimestamp("start_time");
                if (startTime.after(new Timestamp(System.currentTimeMillis()))) {
                    return false; // 会议还未开始
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con2, ps2, null);
        }

        // 检查操作者身份
        String checkIdentitySql = "SELECT p.participant_id, r.applicant_staff_id, p.participant_staff_id " +
                "FROM participant p " +
                "JOIN reservation r ON p.reservation_id = r.reservation_id " +
                "WHERE p.participant_id = ?";
        Connection con3 = null;
        PreparedStatement ps3 = null;
        try {
            con3 = SqlUtil.getConnection();
            if (con3 == null) {
                return false;
            }
            ps3 = con3.prepareStatement(checkIdentitySql);
            ps3.setLong(1, participantId);
            ResultSet rs = ps3.executeQuery();
            // 检查操作者身份
            if (rs.next()) {
                long applicantStaffId = rs.getLong("applicant_staff_id");
                long participantStaffId = rs.getLong("participant_staff_id");
                // 只允许申请人和参会人员本人签到
                if (operatorId != applicantStaffId && operatorId != participantStaffId) {
                    return false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con3, ps3, null);
        }

        // 执行签到
        String sql = "UPDATE participant SET sign_in_process = '已签到', " +
                "sign_in_time = NOW() WHERE participant_id = ? AND sign_in_process<>'已签到'";
        Connection con4 = null;
        PreparedStatement ps4 = null;
        try {
            con4 = SqlUtil.getConnection();
            if (con4 == null) {
                return false;
            }
            ps4 = con4.prepareStatement(sql);
            ps4.setLong(1, participantId);
            return ps4.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SqlUtil.closeAll(con4, ps4, null);
        }
    }


    /**
     * 为指定会议的所有参会人员执行签到
     * @param reservationId 预约ID
     * @return 成功签到的参会人员数量
     */
    public int signInAllParticipants(long reservationId) {
        // SQL更新语句，将未签到的参会人员标记为已签到，并记录签到时间
        String sql = "UPDATE participant SET sign_in_process = '已签到', sign_in_time = NOW() " +
                "WHERE reservation_id = ? AND sign_in_process <> '已签到'";
        Connection con = null; // 数据库连接对象
        PreparedStatement ps = null; // 预处理语句对象
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 如果连接获取失败，返回0
            if (con == null) return 0;
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置预处理语句中的参数，为预约ID
            ps.setLong(1, reservationId);
            // 执行更新操作，并返回受影响的行数
            return ps.executeUpdate();
        } catch (SQLException e) {
            // 打印异常堆栈信息
            e.printStackTrace();
            return 0;
        } finally {
            // 确保关闭所有数据库资源，防止资源泄露
            SqlUtil.closeAll(con, ps, null);
        }
    }

    /**
     * 根据预约ID查询参与者列表
     * @param reservationId 预约ID
     * @return 参与者列表，如果查询失败则返回空列表
     */
    public List<Participant> listParticipantsByReservation(long reservationId) {
        // 创建参与者列表用于存储查询结果
        List<Participant> list = new ArrayList<>();
        // SQL查询语句，关联participant、reservation和admin_staff三张表
        String sql = "SELECT p.participant_id, p.reservation_id, r.reservation_no, r.meeting_topic, " +
                "r.start_time, r.end_time, p.participant_staff_id, a.staff_name, " +
                "p.sign_in_process, p.sign_in_time , a.staff_no " +
                "FROM participant p " +
                "JOIN reservation r ON p.reservation_id = r.reservation_id " +
                "JOIN admin_staff a ON p.participant_staff_id = a.staff_id " +
                "WHERE p.reservation_id = ? " +
                "ORDER BY p.participant_id";
        // 声明数据库连接、预处理结果集和查询结果集
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // 获取数据库连接
            con = SqlUtil.getConnection();
            // 如果连接获取失败，直接返回空列表
            if (con == null) return list;
            // 创建预处理语句
            ps = con.prepareStatement(sql);
            // 设置查询参数
            ps.setLong(1, reservationId);
            // 执行查询
            rs = ps.executeQuery();
            // 遍历查询结果集
            while (rs.next()) {
                // 创建参与者对象
                Participant x = new Participant();
                // 设置参与者属性
                x.setParticipantId(rs.getLong("participant_id"));
                x.setReservationId(rs.getLong("reservation_id"));
                x.setReservationNo(rs.getString("reservation_no"));
                x.setMeetingTopic(rs.getString("meeting_topic"));
                x.setStartTime(String.valueOf(rs.getTimestamp("start_time")));
                x.setEndTime(String.valueOf(rs.getTimestamp("end_time")));
                x.setParticipantStaffId(rs.getLong("participant_staff_id"));
                x.setParticipantStaffNo(rs.getString("staff_no"));
                x.setParticipantName(rs.getString("staff_name"));
                x.setSignInProcess(rs.getString("sign_in_process"));
                // 处理签到时间为空的情况
                x.setSignInTime(rs.getTimestamp("sign_in_time") == null ? "" : String.valueOf(rs.getTimestamp("sign_in_time")));
                // 将参与者添加到列表中
                list.add(x);
            }
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭所有数据库资源
            SqlUtil.closeAll(con, ps, rs);
        }
        // 返回参与者列表
        return list;
    }
}