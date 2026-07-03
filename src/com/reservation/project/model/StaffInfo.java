package com.reservation.project.model;

public class StaffInfo {
    private long staffID;
    private String staffNo;
    private String staffName;
    private long deptID;
    private String deptName;
    private String gender;
    private String position;
    private String phone;
    private String accessLevel;

    public long getStaffID() {
        return staffID;
    }
    public void setStaffID(long staffID) {
        this.staffID = staffID;
    }

    public String getStaffNo() {
        return staffNo;
    }
    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
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

    public String getDeptName() {
        return deptName;
    }
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPosition() {
        return position;
    }
    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAccessLevel() {
        return accessLevel;
    }
    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}