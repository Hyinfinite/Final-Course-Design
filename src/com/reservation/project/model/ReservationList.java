package com.reservation.project.model;

public class ReservationList {
    private long reservationID;
    private String reservationNO;
    private String roomName;
    private String meetingTopic;
    private long applicantID;
    private String startTime;
    private String endTime;
    private String process;
    private String applicantName;

    public long getReservationID() {
        return reservationID;
    }
    public void setReservationID(long reservationID) {
        this.reservationID = reservationID;
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

    public long getApplicantID() {
        return applicantID;
    }
    public void setApplicantID(long applicantID) {
        this.applicantID = applicantID;
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
