package com.reservation.project.model;

/**
 * MeetingRoom类表示一个会议室，包含会议室的基本信息和相关属性的获取与设置方法
 */
public class MeetingRoom {
    // 会议室ID
    private long roomId;
    // 会议室编号
    private String roomCode;
    // 会议室名称
    private String roomName;
    // 会议室位置
    private String Location;
    // 会议室容量（可容纳人数）
    private int capacity;
    // 是否有投影仪（1表示有，0表示没有）
    private int hasProjector;
    // 是否有音响设备（1表示有，0表示没有）
    private int hasAudio;

    /**
     * 获取会议室ID
     * @return 会议室ID
     */
    public long getRoomId() {
        return roomId;
    }

    /**
     * 设置会议室ID
     * @param roomId 要设置的会议室ID
     */
    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    /**
     * 获取会议室编号
     * @return 会议室编号
     */
    public String getRoomCode() {
        return roomCode;
    }

    /**
     * 设置会议室编号
     * @param roomCode 要设置的会议室编号
     */
    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    /**
     * 获取会议室名称
     * @return 会议室名称
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * 设置会议室名称
     * @param roomName 要设置的会议室名称
     */
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /**
     * 获取会议室位置
     * @return 会议室位置
     */
    public String getLocation() {
        return Location;
    }

    /**
     * 设置会议室位置
     * @param location 要设置的会议室位置
     */
    public void setLocation(String location) {
        Location = location;
    }

    /**
     * 获取会议室容量
     * @return 会议室容量（可容纳人数）
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 设置会议室容量
     * @param capacity 要设置的会议室容量
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 获取是否有投影仪
     * @return 1表示有投影仪，0表示没有
     */
    public int getHasProjector() {
        return hasProjector;
    }

    /**
     * 设置是否有投影仪
     * @param hasProjector 1表示有投影仪，0表示没有
     */
    public void setHasProjector(int hasProjector) {
        this.hasProjector = hasProjector;
    }

    /**
     * 获取是否有音响设备
     * @return 1表示有音响设备，0表示没有
     */
    public int getHasAudio() {
        return hasAudio;
    }

    /**
     * 设置是否有音响设备
     * @param hasAudio 1表示有音响设备，0表示没有
     */
    public void setHasAudio(int hasAudio) {
        this.hasAudio = hasAudio;
    }
}
