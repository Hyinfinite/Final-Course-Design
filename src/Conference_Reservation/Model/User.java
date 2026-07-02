package Conference_Reservation.Model;

public class User {
    private double staffID;
    private String staffNO;
    private String staffName;
    private double deptID;
    private String accessLevel;

    public double getStaffID() {
        return staffID;
    }
    public void setStaffID(double staffID) {
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

    public double getDeptID() {
        return deptID;
    }
    public void setDeptID(double deptID) {
        this.deptID = deptID;
    }

    public String getAccessLevel() {
        return accessLevel;
    }
    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}
