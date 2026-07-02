package Conference_Reservation.Model;

public class ReservationList {
    private double reservationID;
    private double reservationNO;
    private String roomName;
    private String meetingTopic;
    private double applicantID;
    private String startTime;
    private String endTime;
    private String process;
    private String applicantName;

    public double getReservationID() {
        return reservationID;
    }
    public void setReservationID(double reservationID) {
        this.reservationID = reservationID;
    }

    public double getReservationNO() {
        return reservationNO;
    }
    public void setReservationNO(double reservationNO) {
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

    public double getApplicantID() {
        return applicantID;
    }
    public void setApplicantID(double applicantID) {
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
