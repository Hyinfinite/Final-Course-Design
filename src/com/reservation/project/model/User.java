package com.reservation.project.model;

/**
 * User类，用于表示系统用户信息
 * 包含员工ID、员工编号、员工姓名、部门ID和访问级别等属性
 */
public class User {
    private long staffId;  // 员工ID
    private String staffNO;  // 员工编号
    private String staffName;  // 员工姓名
    private long deptId;  // 部门ID
    private String accessLevel;  // 访问级别

    /**
     * 获取员工ID
     * @return 返回员工ID的长整型值
     */
    public long getStaffId() {
        return staffId;
    }
    /**
     * 设置员工ID
     * @param staffId 要设置的员工ID值
     */
    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    /**
     * 获取员工编号
     * @return 返回员工编号的字符串
     */
    public String getStaffNO() {
        return staffNO;
    }
    /**
     * 设置员工编号
     * @param staffNO 要设置的员工编号字符串
     */
    public void setStaffNO(String staffNO) {
        this.staffNO = staffNO;
    }

    /**
     * 获取员工姓名
     * @return 返回员工姓名的字符串
     */
    public String getStaffName() {
        return staffName;
    }
    /**
     * 设置员工姓名
     * @param staffName 要设置的员工姓名字符串
     */
    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    /**
     * 获取部门ID
     * @return 返回部门ID的长整型值
     */
    public long getDeptId() {
        return deptId;
    }
    /**
     * 设置部门ID
     * @param deptId 要设置的部门ID值
     */
    public void setDeptId(long deptId) {
        this.deptId = deptId;
    }

    /**
     * 获取访问级别
     * @return 返回访问级别的字符串
     */
    public String getAccessLevel() {
        return accessLevel;
    }
    /**
     * 设置访问级别
     * @param accessLevel 要设置的访问级别字符串
     */
    public void setAccessLevel(String accessLevel) {
        this.accessLevel = accessLevel;
    }
}
