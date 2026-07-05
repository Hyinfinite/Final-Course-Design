package com.reservation.project.model;

/**
 * 预约列表类，用于存储和管理会议室预约信息
 * 包含预约的基本信息，如预约ID、会议室名称、会议主题等
 */
public class ReservationList {
    private long reservationId;      // 预约ID，用于唯一标识一个预约记录
    private String reservationNO;    // 预约编号，用于系统内部标识和查询
    private String roomName;         // 会议室名称
    private String meetingTopic;     // 会议主题
    private long applicantId;        // 申请人ID，标识发起预约的用户
    private String startTime;        // 会议开始时间
    private String endTime;          // 会议结束时间
    private String process;          // 预约处理状态
    private String applicantName;    // 申请人姓名
    private int participantCount;    // 参与人数
    private String comment;          // 备注信息

    /**
     * 获取预约ID的方法
     * @return 返回预约ID，数据类型为long
     */
    public long getReservationId() {
        return reservationId;
    }

    /**
     * 设置预约ID的方法
     * @param reservationId 要设置的预约ID，数据类型为long
     */
    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * 获取预约编号的方法
     * @return 返回预约编号，数据类型为String
     */
    public String getReservationNO() {
        return reservationNO;
    }

    /**
     * 设置预约编号的方法
     * @param reservationNO 要设置的预约编号，数据类型为String
     */
    public void setReservationNO(String reservationNO) {
        this.reservationNO = reservationNO;
    }

    /**
     * 获取会议室名称的方法
     * @return 返回会议室名称，数据类型为String
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * 设置会议室名称的方法
     * @param roomName 要设置的会议室名称，数据类型为String
     */
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /**
     * 获取会议主题的方法
     * @return 返回会议主题，数据类型为String
     */
    public String getMeetingTopic() {
        return meetingTopic;
    }

    /**
     * 设置会议主题的方法
     * @param meetingTopic 要设置的会议主题，数据类型为String
     */
    public void setMeetingTopic(String meetingTopic) {
        this.meetingTopic = meetingTopic;
    }

    /**
     * 获取申请人ID的方法
     * @return 返回申请人ID，数据类型为long
     */
    public long getApplicantId() {
        return applicantId;
    }

    /**
     * 设置申请人ID的方法
     * @param applicantId 要设置的申请人ID，数据类型为long
     */
    public void setApplicantId(long applicantId) {
        this.applicantId = applicantId;
    }

    /**
     * 获取开始时间的方法
     * @return 返回会议开始时间，数据类型为String
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * 设置开始时间的方法
     * @param startTime 要设置的会议开始时间，数据类型为String
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * 获取结束时间的方法
     * @return 返回会议结束时间，数据类型为String
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * 设置结束时间的方法
     * @param endTime 要设置的会议结束时间，数据类型为String
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * 获取会议流程状态的方法
     * @return 返回会议流程状态，数据类型为String
     */
    public String getProcess() {
        return process;
    }

    /**
     * 设置会议流程状态的方法
     * @param process 要设置的会议流程状态，数据类型为String
     */
    public void setProcess(String process) {
        this.process = process;
    }

    /**
     * 获取申请人姓名的方法
     * @return 返回申请人姓名，数据类型为String
     */
    public String getApplicantName() {
        return applicantName;
    }

    /**
     * 设置申请人姓名的方法
     * @param applicantName 要设置的申请人姓名，数据类型为String
     */
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    /**
     * 获取参与人数的方法
     * @return 返回会议参与人数，数据类型为int
     */
    public int getParticipantCount() {
        return participantCount;
    }

    /**
     * 设置参与人数的方法
     * @param participantCount 要设置的会议参与人数，数据类型为int
     */
    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    /**
     * 获取会议备注信息的方法
     * @return 返回会议备注信息，数据类型为String
     */
    public String getComment() {
        return comment;
    }

    /**
     * 设置会议备注信息的方法
     * @param comment 要设置的会议备注信息，数据类型为String
     */
    public void setComment(String comment) {
        this.comment = comment;
    }
}
