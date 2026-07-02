package Conference_Reservation.Model;

public class User {
    private long staffID;
    private String staffNO;
    private String staffName;
    private long deptID;
    private String accessLevel;

    public long getStaffID() {
        return staffID;
    }
    public void setStaffID(long staffID) {
        this.staffID = staffID;
    }

    public String getStaffNO() {
        return staffNO;
    }
    public void setStaffNO(String staffNO) {
        this.staffNO = staffNO;
    }

    public String getStaffName() {
        return staffName;
    }
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public long getDeptID() {
        return deptID;
    }
    public void setDeptID(long deptID) {
        this.deptID = deptID;
    }

    public String getAccessLevel() {
        return accessLevel;
    }
    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}
