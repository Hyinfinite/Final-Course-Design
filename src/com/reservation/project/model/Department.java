package com.reservation.project.model;

/**
 * Department类表示一个部门，包含部门ID和部门名称两个属性
 * 提供了获取和设置部门ID和部门名称的方法
 */
public class Department {
    // 部门ID，使用long类型存储
    private long deptId;
    // 部门名称，使用String类型存储
    private String deptName;

    /**
     * 获取部门ID
     * @return 返回部门ID的long值
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
     * 获取部门名称
     * @return 返回部门名称的字符串
     */
    public String getDeptName() {
        return deptName;
    }
    /**
     * 设置部门名称
     * @param deptName 要设置的部门名称字符串
     */
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }
}
