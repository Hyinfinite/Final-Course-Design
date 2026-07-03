package com.reservation.project.model;

public class User {
    private long staffId;
    private String staffNO;
    private String staffName;
    private long deptId;
    private String accessLevel;

    public long getStaffId() {
        return staffId;
    }
    public void setStaffId(long staffId) {
        this.staffId = staffId;
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

    public long getDeptId() {
        return deptId;
    }
    public void setDeptId(long deptId) {
        this.deptId = deptId;
    }

    public String getAccessLevel() {
        return accessLevel;
    }
    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}
