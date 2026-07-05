package com.reservation.project.model;

/**
 * StaffInfo类用于存储员工信息
 * 包含员工的基本属性，如员工ID、工号、姓名、部门ID、部门名称、性别、职位、电话和访问级别
 * 提供了相应的getter和setter方法用于访问和修改这些属性
 */
public class StaffInfo {
    private long staffId;  // 员工ID
    private String staffNo;  // 员工工号
    private String staffName;  // 员工姓名
    private long deptId;  // 员工部门ID
    private String deptName;  // 员工部门名称
    private String gender;  // 员工性别
    private String position;  // 员工职位
    private String phone;  //
    private String accessLevel;  // 员工访问级别

    /**
     * 获取员工ID的方法
     * @return 返回员工ID，数据类型为long
     */
    public long getStaffId() {
        return staffId;
    }

    /**
     * 设置员工ID的方法
     * @param staffId 要设置的员工ID，数据类型为long
     */
    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    /**
     * 获取员工编号的方法
     * @return 返回员工编号，数据类型为String
     */
    public String getStaffNo() {
        return staffNo;
    }

    /**
     * 设置员工编号的方法
     * @param staffNo 要设置的员工编号，数据类型为String
     */
    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    /**
     * 获取员工姓名的方法
     * @return 返回员工姓名，数据类型为String
     */
    public String getStaffName() {
        return staffName;
    }

    /**
     * 设置员工姓名的方法
     * @param staffName 要设置的员工姓名，数据类型为String
     */
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    /**
     * 获取部门ID的方法
     * @return 返回部门ID，数据类型为long
     */
    public long getDeptId() {
        return deptId;
    }

    /**
     * 设置部门ID的方法
     * @param deptId 要设置的部门ID，数据类型为long
     */
    public void setDeptId(long deptId) {
        this.deptId = deptId;
    }

    /**
     * 获取部门名称的方法
     * @return 返回部门名称，数据类型为String
     */
    public String getDeptName() {
        return deptName;
    }

    /**
     * 设置部门名称的方法
     * @param deptName 要设置的部门名称，数据类型为String
     */
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    /**
     * 获取性别的方法
     * @return 返回员工性别，数据类型为String
     */
    public String getGender() {
        return gender;
    }

    /**
     * 设置性别的方法
     * @param gender 要设置的员工性别，数据类型为String
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * 获取职位的方法
     * @return 返回员工职位，数据类型为String
     */
    public String getPosition() {
        return position;
    }

    /**
     * 设置职位的方法
     * @param position 要设置的员工职位，数据类型为String
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * 获取联系电话的方法
     * @return 返回员工联系电话，数据类型为String
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置联系电话的方法
     * @param phone 要设置的员工联系电话，数据类型为String
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取访问级别的方法
     * @return 返回员工访问级别，数据类型为String
     */
    public String getAccessLevel() {
        return accessLevel;
    }

    /**
     * 设置访问级别的方法
     * @param accessLevel 要设置的员工访问级别，数据类型为String
     */
    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}