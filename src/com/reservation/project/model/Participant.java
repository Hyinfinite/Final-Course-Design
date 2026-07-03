package com.reservation.project.model;

public class Participant {
    private long participantID;
    private long reservationID;
    private String reservationNo;
    private String meetingTopic;
    private String startTime;
    private String endTime;
    private long participantStaffID;
    private String participantName;
    private String signInProcess;
    private String signInTime;

    public long getParticipantID() {
        return participantID;
    }
    public void setParticipantID(long participantID) {
        this.participantID = participantID;
    }

    public long getReservationID() {
        return reservationID;
    }
    public void setReservationID(long reservationID) {
        this.reservationID = reservationID;
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

    public long getParticipantStaffID() {
        return participantStaffID;
    }
    public void setParticipantStaffID(long participantStaffID) {
        this.participantStaffID = participantStaffID;
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