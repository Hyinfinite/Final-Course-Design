package com.reservation.project.model;

/**
 * RoomUsageStat类用于存储房间使用情况的统计数据
 * 包含房间ID、房间名称、使用次数和使用率等属性
 */
public class RoomUsageStat {
    private long roomId;  // 房间ID
    private String roomName;  // 房间名称
    private long usedCount;  // 使用次数
    private double usageRate;  // 使用率

    /**
     * 获取房间ID
     * @return 返回房间ID
     */
    public long getRoomId() {
        return roomId;
    }

    /**
     * 设置房间ID
     * @param roomId 要设置的房间ID
     */
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    /**
     * 获取房间名称
     * @return 返回房间名称
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * 设置房间名称
     * @param roomName 要设置的房间名称
     */
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /**
     * 获取房间使用次数
     * @return 返回房间使用次数
     */
    public long getUsedCount() {
        return usedCount;
    }

    /**
     * 设置房间使用次数
     * @param usedCount 要设置的房间使用次数
     */
    public void setUsedCount(long usedCount) {
        this.usedCount = usedCount;
    }

    /**
     * 获取房间使用率
     * @return 返回房间使用率
     */
    public double getUsageRate() {
        return usageRate;
    }

    /**
     * 设置房间使用率
     * @param usageRate 要设置的房间使用率
     */
    public void setUsageRate(double usageRate) {
        this.usageRate = usageRate;
    }
}