package com.reservation.project.model;

/**
 * 部门会议统计类，用于存储和获取部门ID、部门名称以及会议数量信息
 */
public class DeptMeetingStat {
    // 部门ID，用于唯一标识一个部门
    private long deptId;
    // 部门名称，用于显示部门的名称
    private String deptName;
    // 会议数量，用于统计该部门组织的会议总数
    private int meetingCount;

    /**
     * 获取部门ID
     * @return 返回部门ID
     */
    public long getDeptId() {
        return deptId;
    }

    /**
     * 设置部门ID
     * @param deptId 要设置的部门ID
     */
    public void setDeptId(long deptId) {
        this.deptId = deptId;
    }

    /**
     * 获取部门名称
     * @return 返回部门名称
     */
    public String getDeptName() {
        return deptName;
    }

    /**
     * 设置部门名称
     * @param deptName 要设置的部门名称
     */
    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    /**
     * 获取会议数量
     * @return 返回会议数量
     */
    public int getMeetingCount() {
        return meetingCount;
    }

    /**
     * 设置会议数量
     * @param meetingCount 要设置的会议数量
     */
    public void setMeetingCount(int meetingCount) {
        this.meetingCount = meetingCount;
    }
}