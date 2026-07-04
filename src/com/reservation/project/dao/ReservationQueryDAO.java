package com.reservation.project.dao;

import com.reservation.project.model.ReservationList;
import com.reservation.project.util.SqlUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 给系统管理员用：按日期/部门/会议室筛选全部预约
 * 该类用于实现数据库查询操作，提供按不同条件筛选预约信息的功能
 */
public class ReservationQueryDAO {

/**
 * 查询所有预约信息
 * @param dateStr 日期字符串，用于筛选特定日期的预约
 * @param deptId 部门ID，用于筛选特定部门的预约
 * @param roomId 房间ID，用于筛选特定房间的预约
 * @return 返回预约列表，包含预约详细信息
 */
    public List<ReservationList> queryAll(String dateStr, long deptId, long roomId) {
    // 创建预约列表用于存储查询结果
        List<ReservationList> list = new ArrayList<ReservationList>();

    // 构建SQL查询语句
        StringBuilder sb = new StringBuilder();
    // 查询字段包括预约ID、预约号、会议主题、会议室名称、开始时间、结束时间、预约状态、申请人姓名、参与人数和确认评论
        sb.append("SELECT r.reservation_id, r.reservation_no, r.meeting_topic, m.room_name,");
        sb.append("r.start_time, r.end_time, r.reservation_process, a.staff_name, r.participant_count, cl.confirm_comment ");
    // 关联预约表、会议室表和员工表
        sb.append("FROM reservation r ");
        sb.append("JOIN meeting_room m ON r.reservation_room_id = m.room_id ");
        sb.append("JOIN admin_staff a ON r.applicant_staff_id = a.staff_id ");
    // 左连接确认日志表，获取每个预约的最新确认评论
        sb.append("LEFT JOIN (SELECT reservation_id, confirm_comment FROM confirmation_log WHERE confirm_id IN (SELECT MAX(confirm_id) FROM confirmation_log GROUP BY reservation_id)) cl ON r.reservation_id = cl.reservation_id ");
    // 设置查询条件基础
        sb.append("WHERE 1=1 ");

    // 创建参数列表，用于存储SQL查询的参数
        List<Object> params = new ArrayList<Object>();

    // 如果日期字符串不为空，添加日期条件
        if (dateStr != null && dateStr.trim().length() > 0) {
            sb.append("AND DATE(r.start_time)=? ");
            params.add(dateStr.trim());
        }
    // 如果部门ID大于0，添加部门条件
        if (deptId > 0) {
            sb.append("AND r.apply_dept_id=? ");
            params.add(deptId);
        }
    // 如果房间ID大于0，添加房间条件
        if (roomId > 0) {
            sb.append("AND r.reservation_room_id = ? ");
            params.add(roomId);
        }

    // 按创建时间降序排列
        sb.append("ORDER BY r.created_at DESC");

    // 初始化数据库连接对象
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
            ps = con.prepareStatement(sb.toString());

        // 设置查询参数
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

        // 执行查询
            rs = ps.executeQuery();
        // 遍历结果集，将每条记录转换为ReservationList对象并添加到列表中
            while (rs.next()) {
                ReservationList r = new ReservationList();
                r.setReservationId(rs.getLong("reservation_id"));
                r.setReservationNO(rs.getString("reservation_no"));
                r.setMeetingTopic(rs.getString("meeting_topic"));
                r.setRoomName(rs.getString("room_name"));
                r.setStartTime(String.valueOf(rs.getTimestamp("start_time")));
                r.setEndTime(String.valueOf(rs.getTimestamp("end_time")));
                r.setParticipantCount(rs.getInt("participant_count"));
                r.setProcess(rs.getString("reservation_process"));
                r.setApplicantName(rs.getString("staff_name"));
                r.setComment(rs.getString("confirm_comment"));
                list.add(r);
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