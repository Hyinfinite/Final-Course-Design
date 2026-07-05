package com.reservation.project.model;

/**
 * 参会者类，用于存储会议参与者的相关信息
 */
public class Participant {
    // 参会者ID
    private long participantId;
    // 预约ID
    private long reservationId;
    // 预约编号
    private String reservationNo;
    // 会议主题
    private String meetingTopic;
    // 开始时间
    private String startTime;
    // 结束时间
    private String endTime;
    // 参会员工ID
    private long participantStaffId;
    // 参会员工编号
    private String participantStaffNo;
    // 参会者姓名
    private String participantName;
    // 签到流程
    private String signInProcess;
    // 签到时间
    private String signInTime;

    /**
    * 获取参与者ID的方法
    * @return 返回参与者的唯一标识符(long类型)
    */
    public long getParticipantId() {
        return participantId;
    }
    /**
    * 设置参与者ID的方法
    * @param participantId 要设置的参与者ID值，类型为long
    */
    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }

    /**
    * 获取预约ID
    * @return 返回预约ID的值
    */
    public long getReservationId() {
        return reservationId;
    }
    /**
    * 设置预约ID
    * @param reservationId 要设置的预约ID值
    */
    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    /**
    * 获取预约编号
    * @return 返回预约编号的值
    */
    public String getReservationNo() {
        return reservationNo;
    }
    /**
    * 设置预约编号
    * @param reservationNo 要设置的预约编号值
    */
    public void setReservationNo(String reservationNo) {
        this.reservationNo = reservationNo;
    }

    /**
    * 获取会议主题
    * @return 返回会议主题的值
    */
    public String getMeetingTopic() {
        return meetingTopic;
    }
    /**
    * 设置会议主题
    * @param meetingTopic 要设置的会议主题值
    */
    public void setMeetingTopic(String meetingTopic) {
        this.meetingTopic = meetingTopic;
    }

    /**
    * 获取开始时间
    * @return 返回开始时间的值
    */
    public String getStartTime() {
        return startTime;
    }
    /**
    * 设置开始时间
    * @param startTime 要设置的开始时间值
    */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
    * 获取结束时间
    * @return 返回结束时间的值
    */
    public String getEndTime() {
        return endTime;
    }
    /**
    * 设置结束时间
    * @param endTime 要设置的结束时间值
    */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
    * 获取参与员工ID
    * @return 返回参与员工ID的值
    */
    public long getParticipantStaffId() {
        return participantStaffId;
    }
    /**
    * 设置参与员工ID
    * @param participantStaffId 要设置的参与员工ID值
    */
    public void setParticipantStaffId(long participantStaffId) {
        this.participantStaffId = participantStaffId;
    }

    /**
    * 获取参与员工编号
    * @return 返回参与员工编号的值
    */
    public String getParticipantStaffNo() {
        return participantStaffNo;
    }
    /**
    * 设置参与员工编号
    * @param participantStaffNo 要设置的参与员工编号值
    */
    public void setParticipantStaffNo(String participantStaffNo) {
        this.participantStaffNo = participantStaffNo;
    }

    /**
    * 获取参与员工姓名
    * @return 返回参与员工姓名的值
    */
    public String getParticipantName() {
        return participantName;
    }
    /**
    * 设置参与员工姓名
    * @param participantName 要设置的参与员工姓名值
    */
    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    /**
    * 获取签到流程
    * @return 返回签到流程的值
    */
    public String getSignInProcess() {
        return signInProcess;
    }
    /**
    * 设置签到流程
    * @param signInProcess 要设置的签到流程值
    */
    public void setSignInProcess(String signInProcess) {
        this.signInProcess = signInProcess;
    }

    /**
    * 获取签到时间
    * @return 返回签到时间的值
    */
    public String getSignInTime() {
        return signInTime;
    }
    /**
    * 设置签到时间
    * @param signInTime 要设置的签到时间值
    */
    public void setSignInTime(String signInTime) {
        this.signInTime = signInTime;
    }
}