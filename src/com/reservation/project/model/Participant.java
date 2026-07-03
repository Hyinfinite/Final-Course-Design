package com.reservation.project.model;

public class Participant {
    private long participantId;
    private long reservationId;
    private String reservationNo;
    private String meetingTopic;
    private String startTime;
    private String endTime;
    private long participantStaffId;
    private String participantName;
    private String signInProcess;
    private String signInTime;

    public long getParticipantId() {
        return participantId;
    }
    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }

    public long getReservationId() {
        return reservationId;
    }
    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservationNo() {
        return reservationNo;
    }
    public void setReservationNo(String reservationNo) {
        this.reservationNo = reservationNo;
    }

    public String getMeetingTopic() {
        return meetingTopic;
    }
    public void setMeetingTopic(String meetingTopic) {
        this.meetingTopic = meetingTopic;
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

    public long getParticipantStaffId() {
        return participantStaffId;
    }
    public void setParticipantStaffId(long participantStaffId) {
        this.participantStaffId = participantStaffId;
    }

    public String getParticipantName() {
        return participantName;
    }
    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getSignInProcess() {
        return signInProcess;
    }
    public void setSignInProcess(String signInProcess) {
        this.signInProcess = signInProcess;
    }

    public String getSignInTime() {
        return signInTime;
    }
    public void setSignInTime(String signInTime) {
        this.signInTime = signInTime;
    }
}