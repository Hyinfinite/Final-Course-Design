package com.reservation.project.model;

public class ReservationList {
    private long reservationId;
    private String reservationNO;
    private String roomName;
    private String meetingTopic;
    private long applicantId;
    private String startTime;
    private String endTime;
    private String process;
    private String applicantName;

    public long getReservationId() {
        return reservationId;
    }
    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationNO() {
        return reservationNO;
    }
    public void setReservationNO(String reservationNO) {
        this.reservationNO = reservationNO;
    }

    public String getRoomName() {
        return roomName;
    }
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getMeetingTopic() {
        return meetingTopic;
    }
    public void setMeetingTopic(String meetingTopic) {
        this.meetingTopic = meetingTopic;
    }

    public long getApplicantId() {
        return applicantId;
    }
    public void setApplicantId(long applicantId) {
        this.applicantId = applicantId;
    }

    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getProcess() {
        return process;
    }
    public void setProcess(String process) {
        this.process = process;
    }

    public String getApplicantName() {
        return applicantName;
    }
    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }
}
