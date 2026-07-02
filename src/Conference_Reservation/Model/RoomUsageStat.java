package Conference_Reservation.Model;

public class RoomUsageStat {
    private long roomID;
    private String roomName;
    private double usedMinutes;
    private double usageRate;

    public long getRoomID() {
        return roomID;
    }

    public void setRoomID(long roomID) {
        this.roomID = roomID;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public double getUsedMinutes() {
        return usedMinutes;
    }

    public void setUsedMinutes(double usedMinutes) {
        this.usedMinutes = usedMinutes;
    }

    public double getUsageRate() {
        return usageRate;
    }

    public void setUsageRate(double usageRate) {
        this.usageRate = usageRate;
    }
}